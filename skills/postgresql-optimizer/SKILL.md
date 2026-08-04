---
name: postgresql-optimizer
description: >
  Optimisation avancée de PostgreSQL dans un contexte Django. Utiliser ce skill pour :
  analyser et corriger des requêtes lentes, concevoir des stratégies d'indexation,
  interpréter EXPLAIN ANALYZE, gérer les migrations Django sans downtime, configurer
  le connection pooling (pgBouncer), optimiser les QuerySets Django, utiliser les
  fonctionnalités avancées PostgreSQL (JSONB, full-text search, window functions,
  partitionnement). Déclencher sur : "requête lente", "index", "EXPLAIN", "N+1",
  "QuerySet optimisation", "migration zero-downtime", "pgBouncer", "full-text search",
  "JSONB", "performance base de données", "pg_stat", "connexions PostgreSQL".
---

# PostgreSQL Optimizer

Optimisation PostgreSQL production-grade dans un contexte Django — du QuerySet à l'index.

---

## 1. Diagnostic — Trouver le problème avant d'optimiser

**Règle d'or** : ne jamais optimiser à l'aveugle. Mesurer d'abord.

### 1.1 Django Debug Toolbar (développement)

```python
# requirements/dev.txt
django-debug-toolbar==4.x

# Repérer les requêtes N+1 dans l'onglet SQL
# Seuil d'alerte : plus de 5 requêtes pour une vue = problème
```

### 1.2 Logging SQL en développement

```python
# config/settings/development.py
LOGGING = {
    "version": 1,
    "handlers": {"console": {"class": "logging.StreamHandler"}},
    "loggers": {
        "django.db.backends": {
            "handlers": ["console"],
            "level": "DEBUG",  # Affiche toutes les requêtes SQL
        }
    },
}
```

### 1.3 pg_stat_statements — Requêtes lentes en production

```sql
-- Activer l'extension (une fois)
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

-- Top 10 requêtes les plus lentes (par temps total)
SELECT
    round(total_exec_time::numeric, 2) AS total_ms,
    round(mean_exec_time::numeric, 2)  AS mean_ms,
    calls,
    round((total_exec_time / sum(total_exec_time) OVER () * 100)::numeric, 2) AS pct,
    query
FROM pg_stat_statements
ORDER BY total_exec_time DESC
LIMIT 10;

-- Réinitialiser les stats (début de session de profilage)
SELECT pg_stat_statements_reset();
```

### 1.4 Requêtes longues en cours

```sql
-- Requêtes actives depuis plus de 5 secondes
SELECT pid, now() - pg_stat_activity.query_start AS duration, query, state
FROM pg_stat_activity
WHERE (now() - pg_stat_activity.query_start) > interval '5 seconds'
  AND state != 'idle';

-- Annuler une requête bloquante (sans tuer la connexion)
SELECT pg_cancel_backend(pid);

-- Terminer une connexion (en dernier recours)
SELECT pg_terminate_backend(pid);
```

---

## 2. EXPLAIN ANALYZE — Lire le plan d'exécution

```sql
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT u.email, p.name
FROM users u
JOIN products p ON p.owner_id = u.id
WHERE u.is_active = true AND p.price > 50
ORDER BY p.created_at DESC
LIMIT 20;
```

### Éléments à surveiller dans le plan

| Indicateur | Problème si... | Action |
|-----------|----------------|--------|
| `Seq Scan` | Sur grande table (> 10k rows) | Ajouter un index |
| `Hash Join` vs `Index Scan` | Hash Join sur petits sets | Vérifier l'indexation FK |
| `rows=X` vs actual `rows=Y` | Écart > 10x | `ANALYZE` la table (stats obsolètes) |
| `cost=X..Y` | Y très élevé | Requête à retravailler |
| `Buffers: shared hit=X read=Y` | Y élevé | Données lues depuis disque (cache miss) |
| `Sort` sans `Index Scan` | Toujours | Ajouter index sur la colonne ORDER BY |

### Exemple de lecture

```
Limit  (cost=1.56..45.87 rows=20)
  -> Index Scan Backward using products_created_at_idx on products  ✅ Index utilisé
       Filter: (price > 50)
       Rows Removed by Filter: 342                                  ⚠️ 342 lignes filtrées post-index
```
→ L'index sur `created_at` est utilisé, mais le filtre `price > 50` élimine 342 lignes. Solution : index composite `(created_at, price)`.

---

## 3. Stratégies d'indexation

### 3.1 Règles de base

```sql
-- Index simple sur colonne de filtre fréquent
CREATE INDEX CONCURRENTLY idx_products_status
ON products (status)
WHERE status = 'active'; -- Index partiel : seulement les actifs (90% plus petit)

-- Index composite : ORDER EST IMPORTANT
-- Pour: WHERE owner_id = X AND status = 'active' ORDER BY created_at DESC
CREATE INDEX CONCURRENTLY idx_products_owner_status_date
ON products (owner_id, status, created_at DESC);
-- Règle : colonnes d'égalité (=) en premier, colonnes de tri en dernier

-- Index sur FK (Django ne les crée pas toujours automatiquement)
CREATE INDEX CONCURRENTLY idx_orders_user_id ON orders (user_id);
CREATE INDEX CONCURRENTLY idx_products_category_id ON products (category_id);
```

### 3.2 Index pour recherche texte

```sql
-- Full-text search PostgreSQL natif
CREATE INDEX CONCURRENTLY idx_products_search
ON products USING GIN (to_tsvector('french', name || ' ' || description));

-- Requête correspondante
SELECT * FROM products
WHERE to_tsvector('french', name || ' ' || description) @@ to_tsquery('french', 'chaussure & sport')
ORDER BY ts_rank(to_tsvector('french', name || ' ' || description), to_tsquery('french', 'chaussure & sport')) DESC;
```

### 3.3 Index JSONB

```sql
-- Index GIN sur tout le champ JSONB (flexible mais plus lourd)
CREATE INDEX CONCURRENTLY idx_metadata_gin ON products USING GIN (metadata);

-- Index sur une clé JSONB spécifique (plus léger, recommandé si requête fixe)
CREATE INDEX CONCURRENTLY idx_metadata_brand
ON products ((metadata->>'brand'));

-- Usage Django
Product.objects.filter(metadata__brand="Nike")
```

### 3.4 Vérifier l'utilisation des index

```sql
-- Index jamais utilisés (candidats à supprimer)
SELECT schemaname, tablename, indexname, idx_scan
FROM pg_stat_user_indexes
WHERE idx_scan = 0 AND indexname NOT LIKE '%pkey%'
ORDER BY tablename;

-- Taille des index
SELECT indexname, pg_size_pretty(pg_relation_size(indexname::regclass))
FROM pg_stat_user_indexes
ORDER BY pg_relation_size(indexname::regclass) DESC;
```

---

## 4. QuerySet Django — Optimisations critiques

### 4.1 Éliminer les N+1

```python
# ❌ N+1 : 1 requête pour les orders + N pour chaque user
orders = Order.objects.all()
for order in orders:
    print(order.user.email)  # Requête par itération

# ✅ select_related : JOIN SQL (ForeignKey / OneToOne)
orders = Order.objects.select_related("user", "user__profile").all()

# ✅ prefetch_related : requêtes séparées optimisées (ManyToMany / reverse FK)
orders = Order.objects.prefetch_related(
    "items",                        # Relation inverse
    "items__product",               # Nested prefetch
    Prefetch(
        "items",
        queryset=OrderItem.objects.filter(is_active=True),
        to_attr="active_items"      # Accessible via order.active_items
    )
)
```

### 4.2 Réduire les données ramenées

```python
# only() : seulement les champs nécessaires
users = User.objects.only("id", "email", "first_name")

# values() : dict au lieu d'objets (plus rapide pour lecture seule)
products = Product.objects.values("id", "name", "price")

# values_list() : tuples
emails = User.objects.values_list("email", flat=True)

# annotate() : calculs en base (évite Python loops)
from django.db.models import Count, Avg, Sum, F, Q

products = Product.objects.annotate(
    order_count=Count("orderitems"),
    revenue=Sum(F("orderitems__quantity") * F("price")),
).filter(order_count__gt=0)
```

### 4.3 Bulk operations

```python
# ❌ Lent : N requêtes INSERT
for item in items_list:
    Product.objects.create(**item)

# ✅ Rapide : 1 requête INSERT
Product.objects.bulk_create(
    [Product(**item) for item in items_list],
    batch_size=500,           # Éviter les paquets trop grands
    ignore_conflicts=True     # Idempotent si UUID déjà présent
)

# ✅ Bulk update : 1 requête UPDATE
Product.objects.filter(category=old_cat).update(category=new_cat, updated_at=now())

# ✅ bulk_update pour champs différents par objet
products = list(Product.objects.filter(id__in=ids))
for p in products:
    p.price = compute_new_price(p)
Product.objects.bulk_update(products, ["price"], batch_size=200)
```

### 4.4 Requêtes complexes avec Q objects

```python
from django.db.models import Q

# OR condition
products = Product.objects.filter(
    Q(status="active") | Q(featured=True)
)

# Exclusion complexe
orders = Order.objects.exclude(
    Q(status="cancelled") & Q(created_at__lt=thirty_days_ago)
)
```

---

## 5. Migrations Django — Zéro downtime

### 5.1 Ajouter une colonne nullable (safe)

```python
# ✅ Safe : nullable = pas de rewrite de table
class Migration(migrations.Migration):
    operations = [
        migrations.AddField(
            model_name="product",
            name="weight",
            field=models.DecimalField(max_digits=8, decimal_places=2, null=True, blank=True),
        ),
    ]
```

### 5.2 Ajouter une colonne NOT NULL (dangereux → procédure en 3 étapes)

```python
# Étape 1 : Ajouter nullable avec default applicatif
migrations.AddField(model_name="product", name="sku",
    field=models.CharField(max_length=50, null=True, blank=True))

# Déploiement 1 → remplir les données en production (data migration ou script)

# Étape 2 : Backfill des données existantes
migrations.RunSQL(
    "UPDATE products SET sku = 'SKU-' || id::text WHERE sku IS NULL",
    reverse_sql=migrations.RunSQL.noop
)

# Étape 3 : Contraindre NOT NULL (après que toutes les lignes sont remplies)
migrations.AlterField(model_name="product", name="sku",
    field=models.CharField(max_length=50))
```

### 5.3 Ajouter un index sans verrouiller la table

```python
# ✅ CONCURRENTLY : pas de lock (peut prendre plus de temps, mais safe en prod)
class Migration(migrations.Migration):
    atomic = False  # OBLIGATOIRE pour CONCURRENTLY

    operations = [
        migrations.RunSQL(
            "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_products_owner "
            "ON products (owner_id)",
            reverse_sql="DROP INDEX CONCURRENTLY IF EXISTS idx_products_owner",
        ),
    ]
```

### 5.4 Supprimer une colonne (procédure en 2 étapes)

```python
# Étape 1 : Rendre la colonne ignorée par Django (sans la supprimer)
class Product(BaseModel):
    # old_field = models.CharField(...)  ← retirer du modèle
    pass

# Déploiement 1 → vérifier que plus aucun code ne lit old_field

# Étape 2 : Supprimer physiquement
migrations.RemoveField(model_name="product", name="old_field")
```

---

## 6. Connection Pooling — pgBouncer

### 6.1 Pourquoi pgBouncer

Django crée une connexion PostgreSQL par worker. Sur un serveur avec 8 workers × 4 processus = 32 connexions. PostgreSQL alloue ~10MB par connexion. pgBouncer permet de multiplexer 200 workers sur 20 connexions réelles.

### 6.2 Configuration pgBouncer

```ini
# /etc/pgbouncer/pgbouncer.ini
[databases]
mydb = host=localhost port=5432 dbname=mydb

[pgbouncer]
listen_addr = 127.0.0.1
listen_port = 6432
auth_type = md5
auth_file = /etc/pgbouncer/userlist.txt

pool_mode = transaction     # ← Mode recommandé pour Django (libère connexion après chaque transaction)
max_client_conn = 200       # Connexions clients max (Django workers)
default_pool_size = 20      # Connexions réelles vers PostgreSQL
min_pool_size = 5
reserve_pool_size = 5
reserve_pool_timeout = 3

# Timeouts
server_idle_timeout = 300
client_idle_timeout = 60
```

```python
# Django pointe vers pgBouncer (port 6432 au lieu de 5432)
DATABASES = {
    "default": {
        "ENGINE": "django.db.backends.postgresql",
        "HOST": "127.0.0.1",
        "PORT": "6432",          # pgBouncer
        "CONN_MAX_AGE": 0,       # IMPORTANT : désactiver persistent connections avec pgBouncer
        # en mode transaction, Django ne doit pas garder les connexions ouvertes
    }
}
```

---

## 7. Fonctionnalités avancées PostgreSQL

### 7.1 JSONB — Données semi-structurées

```python
# models.py
class Product(BaseModel):
    metadata = models.JSONField(default=dict, blank=True)
    # Stocke : {"brand": "Nike", "colors": ["red", "blue"], "specs": {"weight": 250}}

# Requêtes Django JSONB
Product.objects.filter(metadata__brand="Nike")
Product.objects.filter(metadata__colors__contains=["red"])
Product.objects.filter(metadata__specs__weight__gte=200)

# Annotation depuis JSONB
from django.db.models.expressions import RawSQL
Product.objects.annotate(
    brand=RawSQL("metadata->>'brand'", [])
).values("id", "name", "brand")
```

### 7.2 Window Functions

```python
from django.db.models import Window, F
from django.db.models.functions import Rank, RowNumber

# Rank des produits par prix dans leur catégorie
products = Product.objects.annotate(
    price_rank=Window(
        expression=Rank(),
        partition_by=[F("category")],
        order_by=F("price").asc()
    )
)
```

### 7.3 Partitionnement (tables très volumineuses > 10M lignes)

```sql
-- Partitionnement par mois sur la date de création
CREATE TABLE orders (
    id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    ...
) PARTITION BY RANGE (created_at);

CREATE TABLE orders_2024_01 PARTITION OF orders
    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

-- PostgreSQL route automatiquement les requêtes vers la bonne partition
-- Django fonctionne normalement, transparent pour l'ORM
```

---

## 8. Checklist d'optimisation

```
Avant tout déploiement critique :
  [ ] EXPLAIN ANALYZE sur toutes les requêtes des vues principales
  [ ] Aucun Seq Scan sur tables > 10k lignes
  [ ] Toutes les FK ont un index
  [ ] select_related / prefetch_related sur toutes les relations traversées
  [ ] Pas de requête dans une boucle Python (bulk operations)
  [ ] Migrations testées sur snapshot de prod (données réelles)
  [ ] Index CONCURRENTLY pour les nouveaux index en prod
  [ ] pg_stat_statements activé en production
  [ ] ANALYZE planifié après gros imports de données
```

---

## Références complémentaires

- `references/query-patterns.md` — Recettes QuerySet pour les cas complexes (agrégation, sous-requêtes, CTEs avec RawSQL)
- `references/monitoring.md` — Mise en place de alertes pg_stat, PgBadger, Prometheus + postgres_exporter
