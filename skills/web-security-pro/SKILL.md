---
name: web-security-pro
description: >
  Sécurisation des applications web React + Django. Utiliser ce skill pour : auditer
  la sécurité d'une API Django, configurer CORS et CSRF correctement, protéger contre
  les injections SQL et XSS, mettre en place l'authentification OAuth2 / 2FA, configurer
  les headers de sécurité HTTP, implémenter le rate limiting, sécuriser les uploads de
  fichiers, auditer les dépendances, protéger les données sensibles (chiffrement, RGPD).
  Déclencher sur : "sécurité", "OWASP", "CORS", "CSRF", "XSS", "injection SQL",
  "OAuth2", "2FA", "TOTP", "rate limiting", "headers sécurité", "CSP", "upload sécurisé",
  "audit sécurité", "RGPD", "chiffrement", "HTTPS", "JWT sécurité", "permission".
---

# Web Security Pro

Sécurisation production React + Django — de l'OWASP Top 10 à la conformité RGPD.

---

## 1. OWASP Top 10 — Mapping Django/React

| # | Risque OWASP | Vecteur dans votre stack | Mitigation principale |
|---|--------------|--------------------------|----------------------|
| A01 | Broken Access Control | ViewSet sans permission_classes | IsAuthenticated par défaut + IsOwnerOrReadOnly |
| A02 | Cryptographic Failures | Mots de passe en clair, JWT faible | set_password(), SECRET_KEY longue, HTTPS |
| A03 | Injection | Raw SQL non paramétrisé | ORM Django uniquement, params si RawSQL |
| A04 | Insecure Design | Endpoints trop permissifs | Modèle de permission "refus par défaut" |
| A05 | Security Misconfiguration | DEBUG=True en prod, ALLOWED_HOSTS vide | Settings prod séparés, checklist deploy |
| A06 | Vulnerable Components | Dépendances non auditées | pip-audit + npm audit en CI |
| A07 | Auth Failures | Tokens non expirés, pas de lockout | SimpleJWT court, throttle sur /login |
| A08 | Data Integrity Failures | Uploads non validés | Validation type + taille + antivirus |
| A09 | Logging Failures | Pas de logs d'accès, erreurs silencieuses | Sentry + accès log Nginx/Django |
| A10 | SSRF | Appels URL non filtrés (ex: webhook) | Whitelist de domaines, timeout, pas de 10.x |

---

## 2. CORS — Configuration correcte

```python
# requirements/base.txt
django-cors-headers==4.x

# config/settings/base.py
INSTALLED_APPS += ["corsheaders"]
MIDDLEWARE = ["corsheaders.middleware.CorsMiddleware", ...rest]  # DOIT être en premier

# ✅ Production : whitelist explicite
CORS_ALLOWED_ORIGINS = [
    "https://monapp.com",
    "https://www.monapp.com",
]
CORS_ALLOW_CREDENTIALS = True   # Si les cookies cross-origin sont nécessaires
CORS_ALLOW_HEADERS = [
    "accept", "authorization", "content-type", "x-csrftoken", "x-request-id"
]
CORS_ALLOW_METHODS = ["DELETE", "GET", "OPTIONS", "PATCH", "POST", "PUT"]

# ❌ JAMAIS en production
# CORS_ALLOW_ALL_ORIGINS = True
```

---

## 3. CSRF — Protection API + SPA

```python
# Pour une API REST consommée par un frontend SPA, le CSRF classique ne s'applique pas
# aux endpoints JSON si on utilise JWT (pas de cookie de session).
# Mais si on utilise des cookies HTTP-only pour les tokens :

# config/settings/base.py
CSRF_COOKIE_SECURE = True          # Cookie CSRF uniquement en HTTPS
CSRF_COOKIE_HTTPONLY = False       # Doit être lisible par JS pour l'envoyer en header
CSRF_COOKIE_SAMESITE = "Strict"    # Bloque les requêtes cross-site
CSRF_TRUSTED_ORIGINS = ["https://monapp.com"]

SESSION_COOKIE_SECURE = True
SESSION_COOKIE_HTTPONLY = True
SESSION_COOKIE_SAMESITE = "Strict"
```

```typescript
// React : envoyer le token CSRF si cookies utilisés
import axios from "axios";

function getCsrfToken() {
  return document.cookie.split(";")
    .find(c => c.trim().startsWith("csrftoken="))
    ?.split("=")[1];
}

axios.interceptors.request.use(config => {
  if (["POST", "PUT", "PATCH", "DELETE"].includes(config.method?.toUpperCase() || "")) {
    config.headers["X-CSRFToken"] = getCsrfToken();
  }
  return config;
});
```

---

## 4. Injection SQL — Défense en profondeur

```python
# ✅ ORM Django — immunisé par défaut (requêtes paramétrées)
Product.objects.filter(name=user_input)  # SAFE

# ✅ RawSQL — toujours paramétrer
Product.objects.raw(
    "SELECT * FROM products WHERE name = %s AND price > %s",
    [user_input, min_price]   # JAMAIS de f-string ou .format() ici
)

# ❌ DANGEREUX — injection possible
Product.objects.raw(f"SELECT * FROM products WHERE name = '{user_input}'")

# ✅ extra() / annotate() avec RawSQL — paramétrer aussi
Product.objects.extra(
    where=["LOWER(name) = LOWER(%s)"],
    params=[user_input]
)
```

---

## 5. Protection XSS

```python
# Django escapes automatiquement dans les templates.
# Pour l'API REST (React consomme JSON), pas de XSS côté serveur.
# Le danger est côté React :

# ❌ dangerouslySetInnerHTML sans sanitisation
<div dangerouslySetInnerHTML={{ __html: userContent }} />

# ✅ Sanitiser avec DOMPurify si le HTML utilisateur est obligatoire
import DOMPurify from "dompurify";
<div dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(userContent, {
  ALLOWED_TAGS: ["b", "i", "em", "strong", "a"],
  ALLOWED_ATTR: ["href"],
}) }} />

# ✅ Toujours préférer le rendu texte pur (pas de HTML)
<p>{userContent}</p>  // React échappe automatiquement
```

---

## 6. Authentification sécurisée

### 6.1 JWT — Sécurisation

```python
from datetime import timedelta

SIMPLE_JWT = {
    "ACCESS_TOKEN_LIFETIME": timedelta(minutes=15),  # Court — limiter l'exposition
    "REFRESH_TOKEN_LIFETIME": timedelta(days=7),
    "ROTATE_REFRESH_TOKENS": True,
    "BLACKLIST_AFTER_ROTATION": True,    # Invalider l'ancien refresh

    # Stocker le refresh token en HTTP-only cookie (pas localStorage)
    # → Inaccessible au JavaScript = protection XSS
}
```

```typescript
// ✅ Refresh token en cookie HTTP-only (géré par le backend)
// Access token en mémoire seulement (pas localStorage)
// Jamais : localStorage.setItem("token", accessToken)

// Pattern recommandé : access token dans le state React (Zustand)
// Refresh token en cookie HTTP-only envoyé automatiquement
```

### 6.2 2FA / TOTP

```python
# requirements/base.txt
django-otp==1.x
qrcode==7.x

# apps/users/models.py
from django_otp.plugins.otp_totp.models import TOTPDevice

class User(AbstractBaseUser):
    two_factor_enabled = models.BooleanField(default=False)

# apps/users/views.py
class Enable2FAView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        device, created = TOTPDevice.objects.get_or_create(
            user=request.user, name="default"
        )
        if created:
            # Retourner le QR code pour Google Authenticator
            import qrcode, io, base64
            uri = device.config_url
            img = qrcode.make(uri)
            buffer = io.BytesIO()
            img.save(buffer, format="PNG")
            qr_b64 = base64.b64encode(buffer.getvalue()).decode()
            return Response({"qr_code": f"data:image/png;base64,{qr_b64}"})
        return Response({"detail": "2FA déjà activé."})

class Verify2FAView(APIView):
    def post(self, request):
        token = request.data.get("token")
        device = TOTPDevice.objects.get(user=request.user, name="default")
        if device.verify_token(token):
            # Générer les tokens JWT complets
            return success_response({"access": ..., "refresh": ...})
        return error_response("Code invalide.", code="INVALID_2FA_TOKEN", status=401)
```

---

## 7. Rate Limiting — Protéger les endpoints sensibles

```python
# Protection renforcée sur les endpoints d'authentification
class StrictAuthThrottle(AnonRateThrottle):
    rate = "5/minute"  # 5 tentatives par minute par IP

class PasswordResetThrottle(AnonRateThrottle):
    rate = "3/hour"

class LoginView(APIView):
    throttle_classes = [StrictAuthThrottle]

    def post(self, request):
        serializer = LoginSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)

        user = authenticate(
            email=serializer.validated_data["email"],
            password=serializer.validated_data["password"]
        )
        if not user:
            # ⚠️ Ne pas indiquer si c'est l'email ou le mot de passe qui est faux
            return error_response(
                "Identifiants incorrects.",
                code="AUTH_FAILED",
                status=401
            )
        # ... générer les tokens
```

---

## 8. Sécurisation des uploads de fichiers

```python
# apps/core/validators.py
import magic
from django.core.exceptions import ValidationError

ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/webp"}
ALLOWED_DOCUMENT_TYPES = {"application/pdf"}
MAX_FILE_SIZE = 10 * 1024 * 1024  # 10 MB

def validate_image_upload(file):
    # 1. Vérifier la taille
    if file.size > MAX_FILE_SIZE:
        raise ValidationError(f"Fichier trop volumineux. Maximum : 10 MB.")

    # 2. Vérifier le type MIME réel (pas juste l'extension)
    file_type = magic.from_buffer(file.read(1024), mime=True)
    file.seek(0)
    if file_type not in ALLOWED_IMAGE_TYPES:
        raise ValidationError(f"Type de fichier non autorisé : {file_type}.")

    # 3. Renommer avec UUID (éviter les path traversal via le nom)
    import uuid
    ext = {"image/jpeg": ".jpg", "image/png": ".png", "image/webp": ".webp"}[file_type]
    file.name = f"{uuid.uuid4()}{ext}"

# Servir les fichiers uploadés depuis S3 (pas depuis Django)
# Django ne doit JAMAIS servir les uploads en production
DEFAULT_FILE_STORAGE = "storages.backends.s3boto3.S3Boto3Storage"
```

---

## 9. Headers de sécurité HTTP

```python
# config/settings/base.py
SECURE_BROWSER_XSS_FILTER = True
SECURE_CONTENT_TYPE_NOSNIFF = True
X_FRAME_OPTIONS = "DENY"
SECURE_HSTS_SECONDS = 31536000          # 1 an
SECURE_HSTS_INCLUDE_SUBDOMAINS = True
SECURE_HSTS_PRELOAD = True
SECURE_SSL_REDIRECT = True              # Redirection HTTP → HTTPS

# CSP via django-csp
CONTENT_SECURITY_POLICY = {
    "DIRECTIVES": {
        "default-src": ["'self'"],
        "script-src": ["'self'"],
        "style-src": ["'self'", "'unsafe-inline'"],  # Nécessaire pour certains CSS-in-JS
        "img-src": ["'self'", "data:", "https://s3.eu-west-3.amazonaws.com"],
        "connect-src": ["'self'", "https://sentry.io"],
        "font-src": ["'self'"],
        "frame-ancestors": ["'none'"],
    }
}
```

---

## 10. Audit des dépendances en CI

```yaml
# .github/workflows/security.yml
name: Security Audit

on:
  schedule:
    - cron: "0 8 * * 1"   # Chaque lundi à 8h
  push:
    branches: [main]

jobs:
  audit-backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: pip install pip-audit
      - run: pip-audit -r backend/requirements/base.txt --format=cyclonedx-json

  audit-frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: npm audit --audit-level=high
        working-directory: frontend
```

---

## 11. Checklist sécurité — Avant mise en production

```
Authentification :
  [ ] Tokens JWT avec durée courte (≤ 15 min pour l'access)
  [ ] Refresh token en HTTP-only cookie
  [ ] Rate limiting sur /login, /register, /reset-password
  [ ] 2FA disponible pour les comptes sensibles

API :
  [ ] Toutes les views ont permission_classes explicites
  [ ] IsAuthenticated par défaut dans REST_FRAMEWORK settings
  [ ] CORS whitelist et non CORS_ALLOW_ALL_ORIGINS
  [ ] Throttling global configuré

Données :
  [ ] Aucun mot de passe ou clé dans les logs
  [ ] Fichiers uploadés validés (type MIME réel, taille, UUID renaming)
  [ ] Données sensibles chiffrées au repos si nécessaire (django-encrypted-fields)

Infrastructure :
  [ ] DEBUG=False
  [ ] Headers de sécurité HTTP configurés
  [ ] HTTPS forcé + HSTS
  [ ] Audit des dépendances en CI
  [ ] Admin Django restreint par IP
```

---

## Références complémentaires

- `references/rgpd-checklist.md` — Checklist conformité RGPD pour les apps Django
- `references/penetration-testing.md` — Outils et procédures d'audit manuel (OWASP ZAP, Burp Suite)
