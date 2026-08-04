---
name: django-api-architect
description: >
  Architecture et conception d'APIs REST robustes avec Django et Django REST Framework.
  Utiliser ce skill pour : concevoir la structure d'une API Django, définir la sérialisation,
  les permissions, le versioning, la pagination, la gestion des erreurs standardisée,
  les ViewSets, les Routers, l'authentification JWT, le throttling, les filtres avancés,
  la documentation OpenAPI/Swagger. Déclencher sur : "API Django", "DRF", "serializer",
  "ViewSet", "endpoint", "permission", "authentification backend", "Django REST", "router",
  "throttle", "filtre API", "pagination API", "versioning API".
---

# Django API Architect

Architecture d'APIs Django REST Framework production-grade — du modèle à la doc OpenAPI.

---

## 1. Philosophie de conception

Toute API Django bien conçue répond à 4 principes :

1. **Contrat stable** : les consommateurs (front React, app Flutter) ne doivent jamais être surpris par la réponse
2. **Fail fast, fail clear** : les erreurs doivent être explicites, typées, et actionnables
3. **Sécurité par défaut** : tout endpoint est privé jusqu'à preuve du contraire
4. **Performance prévisible** : N+1 queries sont inacceptables en production

---

## 2. Structure de projet Django recommandée

```
project/
├── config/
│   ├── settings/
│   │   ├── base.py          # Settings communs
│   │   ├── development.py   # Override dev (DEBUG=True, etc.)
│   │   └── production.py    # Override prod (ALLOWED_HOSTS, etc.)
│   ├── urls.py              # Root URLs uniquement
│   └── wsgi.py / asgi.py
├── apps/
│   ├── users/               # Auth, profils
│   │   ├── models.py
│   │   ├── serializers.py
│   │   ├── views.py
│   │   ├── urls.py
│   │   ├── permissions.py
│   │   └── tests/
│   ├── core/                # Abstractions réutilisables
│   │   ├── models.py        # BaseModel avec created_at, updated_at, uuid
│   │   ├── serializers.py   # BaseSerializer
│   │   ├── exceptions.py    # Exceptions personnalisées
│   │   └── pagination.py    # Paginateurs customisés
│   └── [feature]/           # Une app par domaine métier
├── requirements/
│   ├── base.txt
│   ├── dev.txt
│   └── prod.txt
└── manage.py
```

**Règle** : une app = un domaine métier (users, products, orders). Jamais une app "api" fourre-tout.

---

## 3. BaseModel — Fondation de tous les modèles

```python
# apps/core/models.py
import uuid
from django.db import models

class BaseModel(models.Model):
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    created_at = models.DateTimeField(auto_now_add=True, db_index=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        abstract = True
        ordering = ["-created_at"]

class SoftDeleteModel(BaseModel):
    """Héritage pour les entités qui ne doivent jamais être supprimées physiquement."""
    deleted_at = models.DateTimeField(null=True, blank=True, db_index=True)
    is_deleted = models.BooleanField(default=False, db_index=True)

    class Meta:
        abstract = True

    def soft_delete(self):
        from django.utils import timezone
        self.deleted_at = timezone.now()
        self.is_deleted = True
        self.save(update_fields=["deleted_at", "is_deleted"])
```

---

## 4. Format de réponse universel

**Règle d'or** : toutes les réponses de l'API ont le même squelette. Jamais de réponse "nue".

```python
# apps/core/responses.py
from rest_framework.response import Response

def success_response(data, message="", status=200, meta=None):
    payload = {
        "success": True,
        "message": message,
        "data": data,
    }
    if meta:
        payload["meta"] = meta
    return Response(payload, status=status)

def error_response(message, errors=None, code=None, status=400):
    payload = {
        "success": False,
        "message": message,
        "code": code,          # Code d'erreur machine-readable, ex: "EMAIL_ALREADY_EXISTS"
        "errors": errors or {},
    }
    return Response(payload, status=status)
```

**Exemples de réponses attendues :**

```json
// Succès liste
{
  "success": true,
  "message": "",
  "data": [...],
  "meta": { "count": 42, "next": "...", "previous": null }
}

// Succès objet
{
  "success": true,
  "message": "Profil mis à jour",
  "data": { "id": "uuid", "email": "user@ex.com" }
}

// Erreur validation
{
  "success": false,
  "message": "Les données soumises sont invalides.",
  "code": "VALIDATION_ERROR",
  "errors": { "email": ["Ce champ est requis."], "password": ["8 caractères minimum."] }
}
```

---

## 5. Serializers — Bonnes pratiques

### 5.1 Serializer en lecture vs écriture

```python
# ✅ Séparer Read et Write — pas de serializer monolithique
class UserReadSerializer(serializers.ModelSerializer):
    full_name = serializers.SerializerMethodField()

    class Meta:
        model = User
        fields = ["id", "email", "full_name", "created_at"]
        read_only_fields = fields

    def get_full_name(self, obj):
        return f"{obj.first_name} {obj.last_name}".strip()

class UserWriteSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ["email", "first_name", "last_name", "password"]
        extra_kwargs = {"password": {"write_only": True, "min_length": 8}}

    def create(self, validated_data):
        password = validated_data.pop("password")
        user = User(**validated_data)
        user.set_password(password)
        user.save()
        return user
```

### 5.2 Nested serializers — Éviter les requêtes N+1

```python
class OrderSerializer(serializers.ModelSerializer):
    items = OrderItemSerializer(many=True, read_only=True)  # ⚠️ déclenche N+1

    class Meta:
        model = Order
        fields = ["id", "status", "items", "total"]

# ✅ Dans le ViewSet, toujours prefetch :
def get_queryset(self):
    return Order.objects.prefetch_related("items__product").select_related("user")
```

### 5.3 Validation custom

```python
class RegisterSerializer(serializers.Serializer):
    email = serializers.EmailField()
    password = serializers.CharField(min_length=8, write_only=True)
    password_confirm = serializers.CharField(write_only=True)

    def validate_email(self, value):
        # Vérification champ par champ
        if User.objects.filter(email=value.lower()).exists():
            raise serializers.ValidationError("Cet email est déjà utilisé.")
        return value.lower()

    def validate(self, attrs):
        # Validation cross-champs
        if attrs["password"] != attrs["password_confirm"]:
            raise serializers.ValidationError({"password_confirm": "Les mots de passe ne correspondent pas."})
        return attrs
```

---

## 6. ViewSets — Structure standard

```python
# apps/products/views.py
from rest_framework import viewsets, permissions, status
from rest_framework.decorators import action
from django_filters.rest_framework import DjangoFilterBackend
from rest_framework.filters import SearchFilter, OrderingFilter
from apps.core.responses import success_response, error_response
from .models import Product
from .serializers import ProductReadSerializer, ProductWriteSerializer
from .filters import ProductFilter
from .permissions import IsOwnerOrReadOnly

class ProductViewSet(viewsets.ModelViewSet):
    queryset = Product.objects.select_related("category", "owner").all()
    filter_backends = [DjangoFilterBackend, SearchFilter, OrderingFilter]
    filterset_class = ProductFilter
    search_fields = ["name", "description"]
    ordering_fields = ["price", "created_at", "name"]
    ordering = ["-created_at"]

    def get_serializer_class(self):
        if self.action in ["list", "retrieve"]:
            return ProductReadSerializer
        return ProductWriteSerializer

    def get_permissions(self):
        if self.action in ["list", "retrieve"]:
            return [permissions.AllowAny()]
        return [permissions.IsAuthenticated(), IsOwnerOrReadOnly()]

    def create(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        product = serializer.save(owner=request.user)
        return success_response(
            ProductReadSerializer(product).data,
            message="Produit créé avec succès.",
            status=201
        )

    @action(detail=True, methods=["post"], url_path="publish")
    def publish(self, request, pk=None):
        product = self.get_object()
        if product.status == "published":
            return error_response("Ce produit est déjà publié.", code="ALREADY_PUBLISHED")
        product.publish()
        return success_response(ProductReadSerializer(product).data, message="Produit publié.")
```

---

## 7. Authentification JWT — Configuration complète

```python
# requirements/base.txt
djangorestframework-simplejwt==5.x

# config/settings/base.py
from datetime import timedelta

SIMPLE_JWT = {
    "ACCESS_TOKEN_LIFETIME": timedelta(minutes=30),
    "REFRESH_TOKEN_LIFETIME": timedelta(days=7),
    "ROTATE_REFRESH_TOKENS": True,
    "BLACKLIST_AFTER_ROTATION": True,      # Invalider l'ancien refresh token
    "UPDATE_LAST_LOGIN": True,
    "ALGORITHM": "HS256",
    "AUTH_HEADER_TYPES": ("Bearer",),
    "TOKEN_OBTAIN_SERIALIZER": "apps.users.serializers.CustomTokenObtainPairSerializer",
}

# Ajouter les claims custom au token
class CustomTokenObtainPairSerializer(TokenObtainPairSerializer):
    @classmethod
    def get_token(cls, user):
        token = super().get_token(user)
        token["email"] = user.email
        token["role"] = user.role
        return token
```

---

## 8. Permissions granulaires

```python
# apps/core/permissions.py
from rest_framework import permissions

class IsOwnerOrReadOnly(permissions.BasePermission):
    """Lecture publique, écriture réservée au propriétaire."""
    def has_object_permission(self, request, view, obj):
        if request.method in permissions.SAFE_METHODS:
            return True
        return obj.owner == request.user

class IsAdminOrOwner(permissions.BasePermission):
    """Admins voient tout, autres voient seulement leurs objets."""
    def has_object_permission(self, request, view, obj):
        return request.user.is_staff or obj.owner == request.user

class RolePermission(permissions.BasePermission):
    """Permission basée sur le rôle utilisateur."""
    required_roles = []

    def has_permission(self, request, view):
        return (
            request.user.is_authenticated
            and request.user.role in self.required_roles
        )

# Usage :
class AdminOnlyPermission(RolePermission):
    required_roles = ["admin", "superadmin"]
```

---

## 9. Throttling — Limiter les abus

```python
# config/settings/base.py
REST_FRAMEWORK = {
    "DEFAULT_THROTTLE_CLASSES": [
        "rest_framework.throttling.AnonRateThrottle",
        "rest_framework.throttling.UserRateThrottle",
    ],
    "DEFAULT_THROTTLE_RATES": {
        "anon": "100/hour",
        "user": "1000/hour",
        "auth": "10/hour",       # Throttle spécifique login/register
        "sensitive": "5/minute", # Throttle pour endpoints sensibles (reset password)
    },
}

# Throttle custom par endpoint
class AuthThrottle(UserRateThrottle):
    scope = "auth"

class SensitiveThrottle(UserRateThrottle):
    scope = "sensitive"

class AuthViewSet(viewsets.ViewSet):
    throttle_classes = [AuthThrottle]
```

---

## 10. Pagination standardisée

```python
# apps/core/pagination.py
from rest_framework.pagination import PageNumberPagination
from rest_framework.response import Response

class StandardPagination(PageNumberPagination):
    page_size = 20
    page_size_query_param = "page_size"
    max_page_size = 100

    def get_paginated_response(self, data):
        return Response({
            "success": True,
            "message": "",
            "data": data,
            "meta": {
                "count": self.page.paginator.count,
                "page_size": self.page_size,
                "current_page": self.page.number,
                "total_pages": self.page.paginator.num_pages,
                "next": self.get_next_link(),
                "previous": self.get_previous_link(),
            }
        })
```

---

## 11. Gestion des exceptions — Handler global

```python
# apps/core/exceptions.py
from rest_framework.views import exception_handler
from rest_framework.exceptions import (
    ValidationError, AuthenticationFailed, NotAuthenticated,
    PermissionDenied, NotFound, Throttled
)

def custom_exception_handler(exc, context):
    response = exception_handler(exc, context)

    if response is None:
        # Erreur non gérée par DRF → 500
        return Response(
            {"success": False, "message": "Erreur interne du serveur.", "code": "INTERNAL_ERROR"},
            status=500
        )

    error_map = {
        ValidationError: ("VALIDATION_ERROR", 400),
        AuthenticationFailed: ("AUTH_FAILED", 401),
        NotAuthenticated: ("NOT_AUTHENTICATED", 401),
        PermissionDenied: ("PERMISSION_DENIED", 403),
        NotFound: ("NOT_FOUND", 404),
        Throttled: ("RATE_LIMIT_EXCEEDED", 429),
    }

    exc_type = type(exc)
    code, status_code = error_map.get(exc_type, ("API_ERROR", response.status_code))

    errors = response.data if isinstance(exc, ValidationError) else {}
    message = str(exc.detail) if hasattr(exc, "detail") and not isinstance(exc, ValidationError) else "Erreur de validation."

    response.data = {
        "success": False,
        "message": message,
        "code": code,
        "errors": errors,
    }
    return response

# config/settings/base.py
REST_FRAMEWORK = {
    "EXCEPTION_HANDLER": "apps.core.exceptions.custom_exception_handler",
}
```

---

## 12. Versioning de l'API

```python
# config/urls.py
from django.urls import path, include

urlpatterns = [
    path("api/v1/", include("config.urls_v1")),
    path("api/v2/", include("config.urls_v2")),  # Nouvelle version en parallèle
]

# Strategy : URL Versioning (le plus explicite)
# Éviter le Header Versioning (difficile à tester/déboguer)
# Éviter le Query Param Versioning (cache-busting problématique)
```

---

## 13. Filtres avancés avec django-filter

```python
# apps/products/filters.py
import django_filters
from .models import Product

class ProductFilter(django_filters.FilterSet):
    min_price = django_filters.NumberFilter(field_name="price", lookup_expr="gte")
    max_price = django_filters.NumberFilter(field_name="price", lookup_expr="lte")
    category = django_filters.CharFilter(field_name="category__slug")
    in_stock = django_filters.BooleanFilter(field_name="stock", lookup_expr="gt",
                                             label="En stock uniquement")
    created_after = django_filters.DateFilter(field_name="created_at", lookup_expr="gte")

    class Meta:
        model = Product
        fields = ["status", "min_price", "max_price", "category", "in_stock"]
```

---

## 14. Documentation OpenAPI automatique

```python
# requirements/base.txt
drf-spectacular==0.x

# config/settings/base.py
SPECTACULAR_SETTINGS = {
    "TITLE": "Mon API",
    "DESCRIPTION": "Documentation auto-générée",
    "VERSION": "1.0.0",
    "SERVE_INCLUDE_SCHEMA": False,
    "COMPONENT_SPLIT_REQUEST": True,  # Sépare les schemas read/write
}

# config/urls.py
from drf_spectacular.views import SpectacularAPIView, SpectacularSwaggerView

urlpatterns += [
    path("api/schema/", SpectacularAPIView.as_view(), name="schema"),
    path("api/docs/", SpectacularSwaggerView.as_view(url_name="schema"), name="swagger-ui"),
]

# Annoter les ViewSets pour enrichir la doc
from drf_spectacular.utils import extend_schema, OpenApiParameter

class ProductViewSet(viewsets.ModelViewSet):
    @extend_schema(
        parameters=[OpenApiParameter("category", str, description="Slug de la catégorie")],
        responses={200: ProductReadSerializer(many=True)},
        summary="Lister les produits",
        tags=["products"],
    )
    def list(self, request, *args, **kwargs):
        ...
```

---

## 15. Settings REST_FRAMEWORK complets

```python
# config/settings/base.py
REST_FRAMEWORK = {
    "DEFAULT_AUTHENTICATION_CLASSES": [
        "rest_framework_simplejwt.authentication.JWTAuthentication",
    ],
    "DEFAULT_PERMISSION_CLASSES": [
        "rest_framework.permissions.IsAuthenticated",  # Privé par défaut
    ],
    "DEFAULT_RENDERER_CLASSES": [
        "rest_framework.renderers.JSONRenderer",
        # "rest_framework.renderers.BrowsableAPIRenderer",  # Désactiver en prod
    ],
    "DEFAULT_PAGINATION_CLASS": "apps.core.pagination.StandardPagination",
    "PAGE_SIZE": 20,
    "DEFAULT_FILTER_BACKENDS": [
        "django_filters.rest_framework.DjangoFilterBackend",
        "rest_framework.filters.SearchFilter",
        "rest_framework.filters.OrderingFilter",
    ],
    "DEFAULT_THROTTLE_CLASSES": [
        "rest_framework.throttling.AnonRateThrottle",
        "rest_framework.throttling.UserRateThrottle",
    ],
    "DEFAULT_THROTTLE_RATES": {"anon": "100/hour", "user": "1000/hour"},
    "EXCEPTION_HANDLER": "apps.core.exceptions.custom_exception_handler",
    "DEFAULT_SCHEMA_CLASS": "drf_spectacular.openapi.AutoSchema",
    "TEST_REQUEST_DEFAULT_FORMAT": "json",
    "DATETIME_FORMAT": "%Y-%m-%dT%H:%M:%SZ",
}
```

---

## Références complémentaires

- `references/django-signals.md` — Utilisation des signals pour découpler la logique métier
- `references/celery-tasks.md` — Tâches asynchrones avec Celery + Redis pour les opérations longues
