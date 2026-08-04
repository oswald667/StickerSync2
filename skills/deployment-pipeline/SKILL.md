---
name: deployment-pipeline
description: >
  CI/CD et déploiement pour une stack React + Django + PostgreSQL. Utiliser ce skill pour :
  dockeriser une application Django ou React, configurer GitHub Actions, déployer sur un VPS
  (Nginx + Gunicorn), configurer les variables d'environnement en production, mettre en place
  le reverse proxy, gérer les migrations en déploiement, configurer les certificats SSL,
  monitorer les logs et les erreurs (Sentry), automatiser les backups PostgreSQL.
  Déclencher sur : "Docker", "Dockerfile", "docker-compose", "CI/CD", "GitHub Actions",
  "déploiement", "Nginx", "Gunicorn", "VPS", "production", "SSL", "Certbot", "Sentry",
  "variables d'environnement", "backup PostgreSQL", "pipeline de déploiement".
---

# Deployment Pipeline

CI/CD et déploiement production React + Django — du Dockerfile au monitoring.

---

## 1. Dockerisation — Django

```dockerfile
# Dockerfile.backend
FROM python:3.12-slim AS base

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1

WORKDIR /app

# Installer les dépendances système (psycopg2 binaires)
RUN apt-get update && apt-get install -y --no-install-recommends \
    libpq-dev gcc \
    && rm -rf /var/lib/apt/lists/*

# Copier les requirements en premier (cache Docker)
COPY requirements/base.txt requirements/prod.txt ./requirements/
RUN pip install -r requirements/prod.txt

# Copier le code
COPY . .

# Collecte des static files
RUN python manage.py collectstatic --noinput

# Utilisateur non-root (sécurité)
RUN addgroup --system app && adduser --system --group app
USER app

EXPOSE 8000

CMD ["gunicorn", "config.wsgi:application", \
     "--bind", "0.0.0.0:8000", \
     "--workers", "4", \
     "--worker-class", "gthread", \
     "--threads", "2", \
     "--timeout", "60", \
     "--access-logfile", "-", \
     "--error-logfile", "-"]
```

```dockerfile
# Dockerfile.frontend — Build multi-stage
FROM node:20-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm ci --production=false

COPY . .
RUN npm run build

# Stage 2 : Nginx pour servir le build
FROM nginx:alpine AS production
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx/frontend.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

---

## 2. Docker Compose — Environnement complet

```yaml
# docker-compose.yml (développement)
version: "3.9"

services:
  db:
    image: postgres:16-alpine
    volumes:
      - postgres_data:/var/lib/postgresql/data
    environment:
      POSTGRES_DB: ${DB_NAME:-myapp}
      POSTGRES_USER: ${DB_USER:-postgres}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER:-postgres}"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    volumes:
      - redis_data:/data

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile.backend
    volumes:
      - ./backend:/app        # Hot reload en dev
    ports:
      - "8000:8000"
    environment:
      - DATABASE_URL=postgresql://${DB_USER}:${DB_PASSWORD}@db:5432/${DB_NAME}
      - REDIS_URL=redis://redis:6379/0
      - SECRET_KEY=${SECRET_KEY}
      - DEBUG=${DEBUG:-False}
    depends_on:
      db:
        condition: service_healthy
    command: >
      sh -c "python manage.py migrate &&
             python manage.py runserver 0.0.0.0:8000"

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile.frontend
      target: builder      # Stage builder pour le dev
    volumes:
      - ./frontend:/app
      - /app/node_modules  # Ne pas monter node_modules du host
    ports:
      - "5173:5173"
    command: npm run dev -- --host

  celery:
    build:
      context: ./backend
      dockerfile: Dockerfile.backend
    command: celery -A config worker --loglevel=info --concurrency=4
    environment:
      - DATABASE_URL=postgresql://${DB_USER}:${DB_PASSWORD}@db:5432/${DB_NAME}
      - REDIS_URL=redis://redis:6379/0
    depends_on: [db, redis]

volumes:
  postgres_data:
  redis_data:
```

---

## 3. Configuration Nginx — Production

```nginx
# nginx/nginx.conf
upstream django_backend {
    server backend:8000;
    keepalive 32;
}

server {
    listen 80;
    server_name monapp.com www.monapp.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name monapp.com www.monapp.com;

    ssl_certificate /etc/letsencrypt/live/monapp.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/monapp.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # Headers de sécurité
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header Content-Security-Policy "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'";

    # Fichiers statiques React (servis directement par Nginx)
    location / {
        root /var/www/frontend/dist;
        try_files $uri $uri/ /index.html;    # SPA fallback
        expires 1h;
        add_header Cache-Control "public, no-transform";
    }

    # Assets avec hash → cache long
    location ~* \.(js|css|png|jpg|ico|woff2)$ {
        root /var/www/frontend/dist;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # API Django
    location /api/ {
        proxy_pass http://django_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;

        # Upload size
        client_max_body_size 20M;
    }

    # Django Admin
    location /admin/ {
        proxy_pass http://django_backend;
        proxy_set_header Host $host;
        # Restreindre aux IPs de confiance
        allow 203.0.113.0/24;  # IP bureau
        deny all;
    }

    # Fichiers statiques Django (collectstatic)
    location /static/ {
        alias /app/staticfiles/;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # Fichiers média (uploads utilisateurs)
    location /media/ {
        alias /app/media/;
        expires 7d;
    }
}
```

---

## 4. GitHub Actions — Pipeline CI/CD complet

```yaml
# .github/workflows/deploy.yml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  # ─── Tests ─────────────────────────────────────────────────────
  test-backend:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16
        env:
          POSTGRES_DB: test_db
          POSTGRES_USER: postgres
          POSTGRES_PASSWORD: postgres
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-python@v5
        with: { python-version: "3.12" }
      - name: Install dependencies
        run: pip install -r backend/requirements/dev.txt
      - name: Run tests
        run: pytest --cov=apps --cov-fail-under=70
        working-directory: backend
        env:
          DATABASE_URL: postgresql://postgres:postgres@localhost:5432/test_db
          SECRET_KEY: test-secret-key

  test-frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: "20", cache: npm, cache-dependency-path: frontend/package-lock.json }
      - run: npm ci
        working-directory: frontend
      - run: npm run type-check && npm run lint && npm run test:coverage
        working-directory: frontend

  # ─── Build & Push Docker ────────────────────────────────────────
  build:
    needs: [test-backend, test-frontend]
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    permissions:
      contents: read
      packages: write

    steps:
      - uses: actions/checkout@v4
      - uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Build & push backend
        uses: docker/build-push-action@v5
        with:
          context: ./backend
          file: ./backend/Dockerfile.backend
          push: true
          tags: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/backend:latest
          cache-from: type=gha
          cache-to: type=gha,mode=max

      - name: Build & push frontend
        uses: docker/build-push-action@v5
        with:
          context: ./frontend
          file: ./frontend/Dockerfile.frontend
          push: true
          tags: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/frontend:latest

  # ─── Deploy sur VPS ─────────────────────────────────────────────
  deploy:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    environment: production   # Approbation manuelle si configurée dans GitHub

    steps:
      - name: Deploy via SSH
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          script: |
            cd /opt/myapp
            docker compose pull
            docker compose up -d --no-deps --scale backend=0  # Drain les connexions
            docker compose run --rm backend python manage.py migrate --no-input
            docker compose up -d
            docker image prune -f
```

---

## 5. Variables d'environnement — Production

```bash
# .env.production (ne jamais committer — utiliser GitHub Secrets)
SECRET_KEY=<clé Django 50+ chars aléatoires>
DEBUG=False
ALLOWED_HOSTS=monapp.com,www.monapp.com

DATABASE_URL=postgresql://user:pass@db:5432/myapp
REDIS_URL=redis://redis:6379/0

# Stockage fichiers (S3 ou équivalent)
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
AWS_STORAGE_BUCKET_NAME=
AWS_S3_REGION_NAME=eu-west-3

# Email transactionnel
EMAIL_HOST=smtp.sendgrid.net
EMAIL_PORT=587
EMAIL_HOST_USER=apikey
EMAIL_HOST_PASSWORD=<sendgrid-api-key>
DEFAULT_FROM_EMAIL=noreply@monapp.com

# Monitoring
SENTRY_DSN=https://xxx@sentry.io/yyy
```

```python
# config/settings/base.py — Lire proprement avec python-decouple
from decouple import config, Csv
import dj_database_url

SECRET_KEY = config("SECRET_KEY")
DEBUG = config("DEBUG", default=False, cast=bool)
ALLOWED_HOSTS = config("ALLOWED_HOSTS", cast=Csv())
DATABASES = {"default": dj_database_url.parse(config("DATABASE_URL"))}
```

---

## 6. Monitoring et erreurs — Sentry

```python
# config/settings/base.py
import sentry_sdk
from sentry_sdk.integrations.django import DjangoIntegration
from sentry_sdk.integrations.celery import CeleryIntegration

sentry_sdk.init(
    dsn=config("SENTRY_DSN", default=""),
    integrations=[DjangoIntegration(), CeleryIntegration()],
    traces_sample_rate=0.1,    # 10% des requêtes tracées (performances)
    send_default_pii=False,    # RGPD : pas de données perso
    environment=config("ENVIRONMENT", default="production"),
)
```

```typescript
// frontend/src/main.tsx
import * as Sentry from "@sentry/react";

Sentry.init({
  dsn: import.meta.env.VITE_SENTRY_DSN,
  environment: import.meta.env.MODE,
  tracesSampleRate: 0.1,
  replaysSessionSampleRate: 0.01,   // 1% des sessions enregistrées
  replaysOnErrorSampleRate: 1.0,    // 100% si erreur
});
```

---

## 7. Backup PostgreSQL automatisé

```bash
#!/bin/bash
# scripts/backup_db.sh

set -e

DB_NAME="${DB_NAME:-myapp}"
BACKUP_DIR="/backups/postgresql"
DATE=$(date +%Y%m%d_%H%M%S)
FILENAME="${BACKUP_DIR}/${DB_NAME}_${DATE}.sql.gz"

mkdir -p $BACKUP_DIR

# Dump compressé
pg_dump $DATABASE_URL | gzip > $FILENAME
echo "Backup créé : $FILENAME ($(du -sh $FILENAME | cut -f1))"

# Upload vers S3
aws s3 cp $FILENAME s3://$BACKUP_BUCKET/db-backups/$(basename $FILENAME)

# Garder seulement les 7 derniers backups locaux
ls -t $BACKUP_DIR/*.sql.gz | tail -n +8 | xargs -r rm

echo "Backup terminé et uploadé."
```

```yaml
# cron sur le serveur : tous les jours à 3h
0 3 * * * /opt/myapp/scripts/backup_db.sh >> /var/log/db_backup.log 2>&1
```

---

## 8. Checklist avant mise en production

```
Sécurité :
  [ ] DEBUG=False
  [ ] SECRET_KEY différent du dev
  [ ] ALLOWED_HOSTS configuré
  [ ] HTTPS forcé (redirect HTTP → HTTPS)
  [ ] Headers de sécurité Nginx en place
  [ ] Admin Django restreint par IP

Base de données :
  [ ] Migrations appliquées avant le restart
  [ ] Backup automatique configuré et testé
  [ ] Connection pooling (pgBouncer) si charge élevée

Monitoring :
  [ ] Sentry configuré (back + front)
  [ ] Logs centralisés (docker logs ou service externe)
  [ ] Health check endpoint /api/health/ répond 200

Performances :
  [ ] Static files servis par Nginx (pas Django)
  [ ] Cache Nginx pour les assets front
  [ ] Compression Gzip activée dans Nginx
```

---

## Références complémentaires

- `references/vps-setup.md` — Provisioning initial d'un VPS Ubuntu (utilisateurs, firewall, fail2ban, SSH keys)
- `references/zero-downtime.md` — Stratégies blue-green et rolling deployments
