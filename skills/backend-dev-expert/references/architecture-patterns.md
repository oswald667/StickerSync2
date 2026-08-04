# Patterns d'architecture backend — détail

## Monolithe modulaire

Un seul déployable, mais découpé en modules internes aux frontières strictes (dossiers ou packages séparés, communication via interfaces explicites, pas d'accès direct aux tables d'un autre module).

- Découpe par domaine métier (facturation, catalogue, utilisateurs), jamais par couche technique (tout le SQL ensemble, tout le HTTP ensemble) — ce dernier découpage semble organisé mais rend le changement métier transversal et douloureux.
- Le test de la bonne frontière : pourrait-on extraire ce module en service séparé sans réécrire sa logique interne, seulement sa façon de communiquer ? Si oui, la frontière est saine.
- C'est le point de départ par défaut recommandé pour la majorité des projets — les microservices ajoutent un coût opérationnel qui ne se justifie que par un besoin organisationnel ou de scalabilité différenciée réel, pas par anticipation.

## Microservices

Plusieurs déployables indépendants, chacun propriétaire de ses données.

- Ne découpe jamais les services autour de couches techniques (un service "base de données", un service "API") — découpe autour de domaines métier autonomes (bounded contexts).
- Chaque service doit pouvoir évoluer et être déployé sans coordination avec les autres pour la majorité des changements. Si deux services doivent systématiquement être déployés ensemble, ce sont probablement deux modules d'un même service, pas deux services.
- Communication : privilégier les appels asynchrones (événements) pour les besoins de découplage fort, et les appels synchrones (REST/gRPC) uniquement quand une réponse immédiate est réellement nécessaire.
- Cohérence : la transaction distribuée classique (2PC) est rarement le bon outil. Préférer le pattern Saga (chaîne d'étapes locales, chacune avec une compensation en cas d'échec) pour les processus métier qui traversent plusieurs services.
- Coût réel à anticiper avant de se lancer : observabilité distribuée (tracing), gestion de la cohérence éventuelle côté produit (l'utilisateur peut voir un état transitoire incohérent), duplication de certaines données pour éviter les appels synchrones en cascade, complexité de test d'intégration.

## Architecture event-driven

Les composants communiquent via des événements publiés sur un bus/broker plutôt que par appel direct.

- Bien adapté quand plusieurs consommateurs doivent réagir au même fait métier sans que le producteur les connaisse (ex: "commande créée" déclenche facturation, notification, mise à jour stock).
- Piège fréquent : traiter l'event-driven comme une file d'attente RPC déguisée. Un événement décrit un fait passé ("commande_créée"), pas une commande à exécuter ("créer_facture") — le nommage révèle souvent une confusion de responsabilité.
- Idempotence obligatoire côté consommateur : un message peut être livré plusieurs fois (at-least-once delivery est la norme dans la plupart des brokers). Le traitement doit produire le même résultat qu'il soit exécuté une ou trois fois.
- Ordonnancement : ne pas supposer que les événements arrivent dans l'ordre d'émission sauf garantie explicite du broker (ex: partition Kafka par clé).

## Architecture hexagonale / ports & adapters

Isole la logique métier (le "domaine") des détails techniques (base de données, framework web, services externes) via des interfaces (ports) implémentées par des adaptateurs remplaçables.

- Bénéfice concret : la logique métier se teste sans base de données ni framework web monté, et changer de base de données ou de framework HTTP ne touche pas le cœur métier.
- Ne pas sur-appliquer sur un CRUD simple sans logique métier significative — l'indirection ajoutée coûte plus qu'elle ne rapporte si le domaine est trivial. C'est un investissement qui se justifie quand la logique métier est riche et amenée à évoluer.

## Serverless / FaaS

Fonctions déclenchées par événements (HTTP, queue, cron), sans gestion d'infrastructure persistante.

- Bon calcul économique pour une charge très irrégulière (pics rares, longues périodes d'inactivité) — on ne paie pas de serveur qui attend.
- Cold starts : latence additionnelle sur les invocations peu fréquentes, à anticiper si le produit a des exigences de latence strictes.
- Durée d'exécution limitée par la plateforme — inadapté aux traitements longs sans découpage en étapes.
- Vendor lock-in réel sur l'écosystème (déclencheurs, permissions, observabilité propriétaire) — à peser si la portabilité inter-cloud est un critère.

## Synchrone vs asynchrone — le vrai critère de décision

La question n'est pas "REST ou message queue" en absolu, mais : **le demandeur a-t-il besoin du résultat immédiatement pour continuer son propre traitement ?**

- Oui → appel synchrone, avec timeout et fallback définis.
- Non, mais il a besoin d'une confirmation que la demande est prise en compte → appel synchrone rapide qui enfile un traitement asynchrone, puis notification ou polling pour le résultat final.
- Non, et il n'a même pas besoin de confirmation immédiate → événement asynchrone pur.

Traiter par défaut tout en synchrone crée des chaînes d'appels fragiles (la latence et les pannes s'additionnent). Traiter par défaut tout en asynchrone complexifie inutilement les cas simples et rend le débogage plus dur pour l'utilisateur qui veut juste une réponse immédiate.
