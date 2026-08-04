# Gestion d'état — détail et pièges classiques

## Les quatre catégories d'état, et pourquoi les distinguer change tout

Un projet qui traite tout état de la même façon (tout dans un store global, ou tout en state local dupliqué) accumule de la dette rapidement. Distinguer ces catégories avant de choisir un outil :

1. **État local d'UI** (un menu est-il ouvert, un champ a-t-il le focus) — vit et meurt avec le composant, jamais besoin de le faire remonter plus haut que nécessaire.
2. **État dérivé** (un total calculé à partir d'une liste, un filtre appliqué à des données déjà en mémoire) — ne doit jamais être stocké séparément ; le stocker crée un risque de désynchronisation avec la donnée source. Le calculer à l'affichage, éventuellement avec mémoïsation si le calcul est coûteux.
3. **État serveur** (données qui viennent d'une API : liste d'utilisateurs, contenu d'une commande) — a des besoins spécifiques que le state management générique ne couvre pas bien : cache, invalidation, refetch en arrière-plan, gestion du chargement/erreur, déduplication de requêtes concurrentes. Un outil dédié à ce type d'état (cache de requêtes) résout ces problèmes une fois pour toutes plutôt que de les réimplémenter à la main dans un store générique.
4. **État global client réel** (préférence de thème, utilisateur connecté, panier d'achat non encore synchronisé) — la seule catégorie qui justifie un store global classique.

## Piège : tout mettre dans un store global "pour être sûr"

Symptôme fréquent chez les équipes qui ont eu une mauvaise expérience de prop drilling (état passé manuellement à travers de nombreux niveaux de composants qui n'en ont pas besoin eux-mêmes). La réaction "on met tout dans un store global" résout le prop drilling mais crée un nouveau problème : n'importe quel composant peut désormais lire et modifier un état qui ne le concerne pas, rendant difficile de tracer qui modifie quoi. La bonne réponse au prop drilling n'est pas toujours un store global — c'est souvent une meilleure composition (un composant qui a besoin d'un état profondément imbriqué peut souvent être restructuré pour être rendu directement là où l'état existe, plutôt que de faire voyager la donnée).

## Piège : dupliquer l'état serveur dans un store local

Symptôme : une liste récupérée d'une API est copiée dans un store global, puis modifiée localement (ajout, suppression) sans jamais revalider contre le serveur. Résultat : l'état local diverge silencieusement de la réalité serveur (un autre utilisateur modifie la même donnée, un refresh de page perd les optimistic updates non confirmés). Préférer une source de vérité serveur avec mise à jour optimiste explicite et réconciliation (rollback si le serveur rejette la mutation) plutôt qu'une copie qui vit sa vie indépendamment.

## Piège : re-render en cascade par un store trop grossier

Symptôme : un store global unique où toute mise à jour d'une petite partie de l'état déclenche un nouveau rendu de tous les composants qui lisent le store, même ceux qui ne dépendent pas de la partie modifiée. Vérifier que l'outil choisi permet un abonnement fin (sélecteurs) plutôt qu'un abonnement à l'objet complet — sinon la performance se dégrade en silence à mesure que l'application grossit.

## Machine à états explicite pour les flux complexes

Quand un composant a plusieurs états mutuellement exclusifs avec des transitions précises (ex: un formulaire multi-étapes : brouillon → validation → soumission → succès/erreur), représenter cela avec des booléens indépendants (`isLoading`, `isError`, `isSuccess` combinables entre eux de façon incohérente) est une source fréquente de bugs d'état impossible (`isLoading` et `isError` vrais en même temps). Une machine à états explicite (un seul champ `status` avec des valeurs mutuellement exclusives) élimine cette classe de bug par construction, indépendamment du framework.

## Critère final avant d'introduire un outil de state management externe

Se demander : le problème que je résous est-il un problème de *portée* (l'état doit être visible ailleurs) ou un problème de *synchronisation avec le serveur* (l'état vient d'une API et doit rester à jour) ? Le premier cas appelle un store global léger ou une meilleure composition ; le second appelle un outil de cache de données serveur. Confondre les deux mène à choisir le mauvais outil et à réinventer maladroitement les fonctionnalités de l'autre.
