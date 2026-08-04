---
name: technical-doc-generator
description: >
  Génération de documentation technique professionnelle. Utiliser ce skill pour : rédiger
  une documentation OpenAPI/Swagger à partir de vues DRF, produire des README structurés
  (setup, architecture, contribution), écrire des ADRs (Architecture Decision Records),
  créer des guides d'onboarding développeur, documenter un contrat d'API pour les clients
  front/mobile, rédiger les CHANGELOG et release notes, et créer des diagrammes d'architecture
  en Mermaid. Toujours utiliser ce skill avant de livrer un projet ou d'écrire une documentation
  destinée à un autre développeur ou à un client.
---

# Technical Doc Generator

Documentation technique production-grade.

---

## 1. Principe fondamental : Documentation juste-à-temps

```
Ne documenter que ce qui a de la valeur durable :

  DOCUMENTER :
    Le POURQUOI d'une décision d'architecture  (ADR)
    Le COMMENT démarrer le projet              (README)
    Le CONTRAT de l'API                        (OpenAPI)
    Les BREAKING CHANGES                       (CHANGELOG)

  NE PAS DOCUMENTER :
    Ce que le code dit déjà clairement
    La documentation qui sera obsolète en une semaine
    Les commentaires qui paraphrasent le code ligne à ligne
```

---

## 2. README — Structure standard complète

Le README est la première chose que lit un développeur. Il doit permettre
de lancer le projet en moins de 15 minutes, sans poser de questions.

### Template de README

    # [Nom du projet]
    > [Tagline — ce que fait le projet et pour qui]

    [![CI](badge_url)](lien)  [![Coverage](badge_url)](lien)

    ## Table des matières
    - Aperçu
    - Stack technique
    - Démarrage rapide
    - Structure du projet
    - Variables d'environnement
    - Tests
    - Déploiement
    - Contribution

    ## Aperçu
    [2-3 phrases : quel problème est résolu, qui sont les utilisateurs.]

    Fonctionnalités principales :
    - Feature 1
    - Feature 2

    Démo : https://demo.example.com

    ## Stack technique
    | Couche         | Technologie          | Version |
    |----------------|----------------------|---------|
    | Backend        | Django + DRF         | 5.x     |
    | Base de données| PostgreSQL           | 16      |
    | Cache          | Redis                | 7       |
    | Frontend       | React + Vite         | 18 + 5  |
    | Tests backend  | pytest               | 8       |
    | Tests frontend | Vitest + Testing Lib | 1 + 6   |
    | CI/CD          | GitHub Actions       | —       |

    ## Démarrage rapide

    Prérequis : Docker Desktop 4.x+, Node.js 20+, Python 3.12+

    Installation :
        git clone https://github.com/org/repo.git && cd repo
        cp .env.example .env          # Éditer avec vos valeurs locales
        docker compose up -d db redis

        # Backend
        cd backend
        python -m venv .venv && source .venv/bin/activate
        pip install -r requirements/development.txt
        python manage.py migrate
        python manage.py createsuperuser
        python manage.py runserver

        # Frontend (nouveau terminal)
        cd frontend && npm install && npm run dev

    Accès :
    - Frontend  : http://localhost:5173
    - API       : http://localhost:8000/api/v1/
    - Admin     : http://localhost:8000/admin/
    - Docs API  : http://localhost:8000/api/docs/

    ## Structure du projet
        backend/
          apps/
            users/      # Authentification et profils
            products/   # Gestion des produits
            orders/     # Gestion des commandes
          core/         # Utilitaires partagés
          config/       # Settings, URLs, WSGI
        frontend/
          src/
            app/        # Providers, routing racine
            features/   # Code métier par domaine
            shared/     # Composants et hooks réutilisables
            pages/      # Assemblage de features
          e2e/          # Tests Playwright

    ## Variables d'environnement
    | Variable            | Description                     | Comment obtenir                             |
    |---------------------|---------------------------------|---------------------------------------------|
    | SECRET_KEY          | Clé secrète Django (50+ chars)  | python -c "import secrets; print(secrets.token_urlsafe(50))" |
    | DATABASE_URL        | URL PostgreSQL                  | postgres://user:pass@localhost:5432/db      |
    | REDIS_URL           | URL Redis                       | redis://localhost:6379/0                    |
    | ANTHROPIC_API_KEY   | Clé API Claude                  | console.anthropic.com                       |
    | VITE_API_URL        | URL de l'API pour le frontend   | http://localhost:8000 en dev                |

    ## Tests
        # Backend — tous les tests avec coverage
        cd backend && pytest --cov=apps --cov-fail-under=80

        # Frontend — tests unitaires
        cd frontend && npm run test:coverage

        # Tests E2E Playwright
        npm run test:e2e

    ## Contribution
    1. Créer une branche : git checkout -b feature/ma-feature
    2. Committer : git commit -m 'feat: ajouter ma feature'
    3. Ouvrir une Pull Request

    Conventions de commits :
        feat:     Nouvelle fonctionnalité
        fix:      Correction de bug
        docs:     Documentation uniquement
        refactor: Refactoring sans changement de comportement
        test:     Ajout ou modification de tests
        chore:    Maintenance (dépendances, config CI)

---

## 3. OpenAPI — Documentation DRF avec drf-spectacular

### Configuration

```python
# pip install drf-spectacular

# config/settings/base.py
INSTALLED_APPS = ['drf_spectacular', ...]
REST_FRAMEWORK = {
    'DEFAULT_SCHEMA_CLASS': 'drf_spectacular.openapi.AutoSchema',
}
SPECTACULAR_SETTINGS = {
    'TITLE': 'Mon Projet API',
    'DESCRIPTION': 'API REST pour la plateforme de vente en ligne.',
    'VERSION': '1.0.0',
    'SERVE_INCLUDE_SCHEMA': False,
    'COMPONENT_SPLIT_REQUEST': True,  # Schémas request/response séparés
}

# config/urls.py
from drf_spectacular.views import SpectacularAPIView, SpectacularSwaggerView
urlpatterns += [
    path('api/schema/', SpectacularAPIView.as_view(), name='schema'),
    path('api/docs/', SpectacularSwaggerView.as_view(url_name='schema'), name='swagger-ui'),
]
```

### Annoter les vues pour une doc riche

```python
from drf_spectacular.utils import (
    extend_schema, OpenApiParameter, OpenApiResponse, OpenApiExample
)

@extend_schema(
    tags=['Products'],
    summary='Lister les produits actifs',
    description="""
    Retourne la liste paginée des produits actifs, avec filtres et tri.

    **Filtres disponibles :**
    - `category` : ID de catégorie (UUID)
    - `status`   : `active` | `archived`
    - `search`   : recherche dans le nom et la description
    - `ordering` : `price` | `-price` | `created_at` | `-created_at`

    **Authentification requise** : Bearer token JWT.
    """,
    parameters=[
        OpenApiParameter(
            name='category',
            description='Filtrer par ID de catégorie (UUID)',
            type=str,
            required=False,
        ),
        OpenApiParameter(
            name='search',
            description='Terme de recherche (nom, description)',
            type=str,
            required=False,
        ),
        OpenApiParameter(name='page', description='Numéro de page', type=int, default=1),
        OpenApiParameter(name='page_size', description='Taille de page (max 100)', type=int, default=20),
    ],
    responses={
        200: OpenApiResponse(
            response=ProductSerializer(many=True),
            description='Liste paginée des produits',
            examples=[
                OpenApiExample(
                    'Exemple succès',
                    value={
                        'success': True,
                        'data': [{'id': 'uuid', 'name': 'Chaise design', 'price': 45000.0}],
                        'meta': {'count': 150, 'page': 1, 'page_size': 20, 'total_pages': 8}
                    }
                )
            ]
        ),
        401: OpenApiResponse(description='Non authentifié'),
        403: OpenApiResponse(description='Permissions insuffisantes'),
    }
)
class ProductViewSet(viewsets.ModelViewSet):
    ...
```

---

## 4. ADR — Architecture Decision Record

Format standard pour documenter les décisions qui engagent durablement le projet.

**Quand créer un ADR :**
- Choix de bibliothèque majeure (ORM, state management, auth)
- Décision d'architecture (REST vs GraphQL, mono vs micro)
- Pattern imposé à toute l'équipe (conventions de commits, structure)
- Décision difficile à inverser une fois en production

### Template ADR

```markdown
# ADR-001 — Utilisation de Zustand plutôt que Redux Toolkit

**Date :** 2025-01-15
**Statut :** Accepté
**Décideurs :** [Nom / rôle]

---

## Contexte

Le projet nécessite un state management global pour :
- L'état d'authentification (user, access token)
- Les filtres de recherche partagés entre plusieurs pages
- La sélection multiple dans les listes de produits

## Options considérées

**Option 1 — Redux Toolkit**
- Pour : DevTools puissants, patterns très établis, typage fort
- Contre : Verbosité (boilerplate élevé), over-engineering pour ce périmètre

**Option 2 — Zustand**
- Pour : API minimaliste, TypeScript natif, sélecteurs fins, bundle < 1KB
- Contre : Moins de conventions imposées, DevTools moins riches

## Décision

Zustand est retenu pour sa simplicité et sa performance sur ce projet.

## Conséquences

Positives :
- Setup rapide, state découpé par feature, updates sélectifs sans re-renders

Négatives et mitigation :
- Moins de structure imposée → conventions documentées dans react-architecture
- Conventions : un store par feature, sélecteurs fins obligatoires

## Révision

À réévaluer si le projet dépasse 50 stores ou si les DevTools deviennent bloquants.
```

---

## 5. CHANGELOG — Format Keep a Changelog

```markdown
# Changelog

Format : Keep a Changelog (https://keepachangelog.com/fr/1.0.0/)
Versioning : Semantic Versioning (https://semver.org/lang/fr/)

---

## [Unreleased]

### Added
- Système de notifications en temps réel (WebSocket)

### Changed
- Amélioration des performances de la recherche sémantique (index HNSW)

---

## [1.2.0] — 2025-03-15

### Added
- Recherche sémantique avec pgvector (#98)
- Export CSV des tableaux de données (#112)
- Dashboard analytique : 4 KPIs + 3 graphiques (#118)

### Changed
- Migration de Chart.js vers Recharts (meilleure intégration React)
- Pagination : page_size max augmenté de 50 à 100

### Fixed
- Correction du refresh token expirant prématurément (#142)
- Fix de l'upload d'image sur iOS Safari (#156)

### Security
- Mise à jour django 5.0.4 (patch CVE-2024-XXXX)

---

## [1.1.0] — 2025-02-01

### Added
- Authentification JWT avec rotation des tokens
- Système de permissions granulaires (IsOwnerOrReadOnly)

### Breaking Changes

ATTENTION clients mobiles : le champ "amount" est renommé "price" dans
/api/v1/products/. L'ancien champ "amount" est conservé jusqu'au 2025-06-01
puis sera supprimé. Migrez vos clients avant cette date.

---

## [1.0.0] — 2025-01-15

Première mise en production.

### Added
- CRUD produits complet avec photos
- Authentification (inscription, connexion, déconnexion)
- Page produit publique avec filtres et recherche textuelle
```

---

## 6. Diagrammes Mermaid — Architecture et flux

Les diagrammes Mermaid sont du texte — versionnables, éditables, lisibles dans GitHub.

### Architecture de déploiement

```mermaid
graph TB
    Client[Navigateur / App Flutter]
    CDN[Cloudflare CDN]
    Nginx[Nginx Reverse Proxy 443]
    React[React SPA]
    Django[Django + Gunicorn 8000]
    PG[(PostgreSQL 16)]
    Redis[(Redis 7)]
    R2[Cloudflare R2 - Fichiers media]
    Anthropic[Anthropic API]

    Client --> CDN
    CDN --> Nginx
    Nginx -->|"/ assets statiques"| React
    Nginx -->|"/api/"| Django
    Django --> PG
    Django --> Redis
    Django --> R2
    Django --> Anthropic
```

### Flux d'authentification JWT

```mermaid
sequenceDiagram
    participant C as Client React
    participant A as API Django
    participant D as PostgreSQL

    C->>A: POST /auth/login/ {email, password}
    A->>D: Vérifier credentials (Argon2)
    D-->>A: User valide
    A-->>C: {access 15min, refresh httpOnly cookie 7j}

    Note over C,A: Plus tard — access token expiré

    C->>A: GET /products/ — 401 Unauthorized
    C->>A: POST /auth/refresh/ [cookie httpOnly auto]
    A-->>C: {access nouveau 15min}
    C->>A: GET /products/ [nouveau token]
    A-->>C: {success: true, data: [...]}
```

### Flux de création de produit

```mermaid
sequenceDiagram
    participant U as Utilisateur
    participant F as Frontend React
    participant A as API Django
    participant S as Stockage R2

    U->>F: Remplit le formulaire + photo
    F->>F: Validation Zod client
    F->>A: POST /products/ multipart/form-data
    A->>A: Validation DRF Serializer
    A->>S: Upload photo — URL publique
    A->>A: Création en base (transaction)
    A-->>F: 201 Created {product}
    F->>F: Invalidation cache React Query
    F-->>U: Redirection vers la page produit
```

### Diagramme d'états produit

```mermaid
stateDiagram-v2
    [*] --> Brouillon : Création
    Brouillon --> En_attente : Soumettre
    En_attente --> Actif : Approuver
    En_attente --> Brouillon : Rejeter
    Actif --> Archivé : Archiver
    Archivé --> Actif : Republier
    Actif --> [*] : Supprimer
```

---

## 7. Guide d'onboarding développeur

```markdown
# Onboarding — Nouveau développeur

Bienvenue ! Ce guide vous permet d'être opérationnel en moins d'une heure.

## Étape 1 — Environnement (15 min)
1. Installer : Docker Desktop, Node.js 20, Python 3.12, Git
2. Cloner le repo et suivre le README (section Démarrage rapide)
3. Vérifier que l'app tourne sur http://localhost:5173

## Étape 2 — Comprendre la structure (20 min)
Lire dans cet ordre :
1. docs/architecture.md — Vue d'ensemble du système
2. docs/adr/ — Les 3-5 ADRs les plus récents
3. backend/core/ — Le code partagé (exceptions, pagination, renderers)
4. frontend/src/features/products/ — Un exemple de feature complète

## Étape 3 — Premiers pas (25 min)
1. Créer votre première user story dans le backlog (même mineure)
2. Ouvrir une PR même petite pour vous familiariser avec le workflow
3. Faire tourner la suite de tests complète : make test

## Conventions à retenir
- Commits : Conventional Commits (feat:, fix:, docs:, etc.)
- Branches : feature/nom-court, fix/description-bug
- PR : au moins 1 revieweur, CI verte obligatoire pour merger
- Tout nouvel endpoint : tests + @extend_schema OpenAPI
- Toute décision d'architecture : ADR dans docs/adr/

## Contacts
- Questions code    : Canal Slack #dev
- Accès staging     : [Lien]
- Tickets           : [Lien Jira/Linear/Notion]
```

---

## 8. Contrat API — Document client front/mobile

```markdown
# Contrat API v1

Base URL        : https://api.example.com/api/v1/
Authentification: Bearer token JWT dans le header Authorization
Format          : JSON (Content-Type: application/json)
Doc interactive : https://api.example.com/api/docs/

---

## Format de réponse universel

Succès :
    {
      "success": true,
      "data": {},
      "message": "OK",
      "meta": { "count": 0, "page": 1, "page_size": 20, "total_pages": 1 }
    }

Erreur :
    {
      "success": false,
      "error": {
        "code": "VALIDATION_ERROR",
        "message": "Données invalides",
        "details": { "email": ["Ce champ est requis."] }
      }
    }

## Codes d'erreur machine (stables)

| Code             | HTTP | Description                       |
|------------------|------|-----------------------------------|
| VALIDATION_ERROR | 400  | Données invalides — voir details  |
| UNAUTHORIZED     | 401  | Token absent ou expiré            |
| FORBIDDEN        | 403  | Permissions insuffisantes         |
| NOT_FOUND        | 404  | Ressource introuvable             |
| CONFLICT         | 409  | Conflit (ex : email déjà utilisé) |
| THROTTLED        | 429  | Trop de requêtes                  |
| SERVER_ERROR     | 500  | Erreur serveur                    |

## Endpoints principaux

Auth :
| POST  | /auth/login/    | Connexion — retourne {access, user}   |
| POST  | /auth/refresh/  | Rafraîchir le token                   |
| POST  | /auth/logout/   | Déconnexion (blacklist token)         |

Products :
| GET    | /products/          | Liste paginée                 |
| POST   | /products/          | Créer un produit              |
| GET    | /products/{id}/     | Détail                        |
| PATCH  | /products/{id}/     | Modifier partiellement        |
| DELETE | /products/{id}/     | Supprimer                     |
| POST   | /products/{id}/publish/ | Publier                   |

## Politique de versioning

- Les champs ne sont jamais supprimés sans 90 jours de dépréciation minimum
- Les breaking changes sont annoncés dans le CHANGELOG avec la date de suppression
- Contacter l'équipe backend avant toute migration majeure côté client
```

---

## 9. Anti-patterns critiques

| Anti-pattern | Problème | Solution |
|---|---|---|
| README sans setup testé | Nouveau dev bloqué dès le premier jour | Tester le setup sur machine vierge avant de merger |
| OpenAPI sans exemples de réponse | Front ne sait pas ce qui est retourné | OpenApiExample sur chaque réponse d'endpoint |
| ADR sans contexte ni options refusées | Décision incompréhensible 6 mois plus tard | Toujours documenter le POURQUOI et les alternatives |
| CHANGELOG non maintenu | Impossible de savoir ce qui a changé | Mettre à jour à chaque PR avant le merge |
| Diagrammes en PNG ou JPEG | Impossible à versionner, lent à modifier | Mermaid ou PlantUML — texte dans le repo |
| Doc trop détaillée sur le HOW | Obsolète dès le prochain refactor | Doc sur le WHY, code sur le HOW |
| Suppression de champ sans dépréciation | Clients mobiles cassés immédiatement | Déprécier, documenter, attendre 90j, puis supprimer |
