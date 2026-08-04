# Checklist de conception d'API

## REST

- **Ressources, pas actions** : les URLs nomment des noms (`/orders/42`), pas des verbes (`/getOrder`). Les verbes HTTP portent l'action.
- **Codes de statut cohérents** : 2xx pour succès, 4xx pour erreur côté client (dont 422 pour une entité syntaxiquement valide mais sémantiquement invalide), 5xx réservé aux erreurs serveur réelles — ne jamais renvoyer 200 avec un champ `"success": false` caché dans le corps, ça casse tous les outils qui lisent le code de statut.
- **Format d'erreur uniforme** dans tout le service, par exemple :
  ```json
  { "error": { "code": "INSUFFICIENT_STOCK", "message": "...", "details": {} } }
  ```
  Un code métier stable (`INSUFFICIENT_STOCK`) permet au client de réagir par logique, contrairement à un message humain qui peut changer.
- **Versioning** : décider tôt de la stratégie (préfixe d'URL `/v1/`, header, ou content negotiation) et surtout de la politique de dépréciation — combien de temps une version reste supportée après la sortie de la suivante, et comment le client est prévenu.
- **Pagination** : cursor-based pour les collections volumineuses ou qui changent souvent (évite le décalage de page classique quand des éléments sont insérés/supprimés pendant la pagination) ; offset-based acceptable pour de petites collections stables.
- **Idempotence** : les opérations `PUT`/`DELETE` doivent être idempotentes par nature de leur définition HTTP — vérifier que l'implémentation le respecte vraiment (un DELETE sur une ressource déjà supprimée doit rester un succès ou un 404 cohérent, pas une erreur 500). Pour les `POST` créant des effets critiques (paiement), exiger une clé d'idempotence fournie par le client.
- **Filtrage/tri** : exposer une syntaxe cohérente et documentée plutôt que d'ajouter des paramètres ad hoc au fil des besoins.
- **HATEOAS** : optionnel, à ne recommander que si les clients bénéficient réellement de la découvrabilité — souvent un coût de complexité non justifié pour des API internes à consommateurs connus.

## GraphQL

- **Un schéma pensé pour les besoins de lecture des clients**, pas un simple miroir du schéma de base de données — sinon on retombe sur les mêmes problèmes de sur/sous-fetching que REST mal conçu.
- **Complexité des requêtes** : sans limite, un client peut construire une requête profondément imbriquée qui génère une charge disproportionnée côté serveur (ex: requêtes N+1 masquées derrière des resolvers). Mettre en place une limite de profondeur/coût de requête.
- **Gestion des erreurs** : GraphQL renvoie souvent un statut 200 même en cas d'erreur partielle — structurer le champ `errors` avec des codes exploitables, et décider explicitement de la politique pour les erreurs partielles (une partie du graphe échoue, le reste est renvoyé).
- **N+1 côté resolvers** : quasi systématique sans précaution — utiliser un dataloader ou équivalent pour batcher les résolutions.
- **Versioning** : GraphQL encourage l'évolution du schéma sans versioning explicite (ajout de champs, dépréciation via directive `@deprecated`) plutôt que des versions parallèles — s'assurer que l'équipe suit bien cette discipline plutôt que de dupliquer des types "v2".

## RPC (gRPC, JSON-RPC, etc.)

- Adapté quand la performance et le typage fort comptent plus que la lisibilité HTTP classique, ou en communication interne entre services.
- Définir le contrat (proto ou schéma équivalent) comme source de vérité versionnée séparément du code, avec compatibilité ascendante explicite (champs optionnels, jamais de renumérotation).
- Gérer explicitement les erreurs via les codes de statut du protocole plutôt que de tout encoder dans le corps de la réponse.

## Authentification / autorisation — indépendant du protocole choisi

- Séparer clairement authentification (qui est l'appelant) et autorisation (ce qu'il a le droit de faire) — les mélanger mène à des vérifications d'accès incohérentes.
- Vérifier l'autorisation au niveau le plus fin nécessaire : un token valide ne garantit pas l'accès à une ressource précise (vérifier l'appartenance/l'ownership, pas seulement le rôle global).
- Ne jamais faire confiance à un identifiant de ressource fourni par le client sans vérifier que l'appelant a le droit d'y accéder (IDOR — Insecure Direct Object Reference — est l'une des failles les plus fréquentes et les plus simples à introduire par inadvertance).

## Documentation du contrat

Le contrat (OpenAPI, schéma GraphQL, .proto) doit être généré ou vérifié automatiquement contre l'implémentation réelle plutôt que maintenu à la main en parallèle — un contrat qui diverge silencieusement de l'implémentation est pire qu'une absence de documentation.
