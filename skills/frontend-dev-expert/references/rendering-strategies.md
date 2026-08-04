# Stratégies de rendu — détail

## CSR (Client-Side Rendering)

Le serveur renvoie une coquille HTML minimale, le JavaScript téléchargé construit l'interface dans le navigateur.

- Bon calcul quand l'application est fortement interactive et que les visiteurs reviennent souvent (le coût du premier chargement est amorti sur la durée de session).
- Le temps avant premier affichage utile dépend entièrement du temps de téléchargement + exécution du JavaScript — sur réseau lent ou appareil peu puissant, ce coût est significatif.
- SEO : les moteurs de recherche modernes exécutent du JavaScript, mais avec des limites (délai, budget de crawl, JS complexe mal interprété) — ne pas compter dessus pour du contenu dont l'indexation est stratégique.

## SSR (Server-Side Rendering)

Le serveur exécute le rendu à chaque requête et renvoie du HTML déjà construit, puis le JavaScript "hydrate" la page côté client pour la rendre interactive.

- Résout le problème de premier affichage et de SEO du CSR pur, au prix d'une charge serveur à chaque requête (contrairement au SSG).
- L'hydratation elle-même a un coût : le HTML est visible avant que l'interface soit interactive, créant une fenêtre où l'utilisateur voit du contenu qu'il ne peut pas encore utiliser pleinement. Les frameworks modernes proposent des techniques de streaming ou d'hydratation partielle/progressive pour réduire cette fenêtre — vérifier ce que propose le framework en place avant de considérer ce problème comme non résolvable.
- Adapté au contenu personnalisé par utilisateur qui doit quand même être rapide et indexable (ex: page de profil publique, résultats de recherche).

## SSG (Static Site Generation)

Le HTML est généré une fois au moment du build, puis servi tel quel (souvent via CDN) à chaque visiteur.

- Le plus rapide et le moins coûteux en infrastructure pour du contenu qui ne change pas par visiteur.
- Inadapté si le contenu doit refléter un état qui change plus vite que le rythme de déploiement, ou s'il est personnalisé par utilisateur.
- Bien combiné avec une couche CSR légère pour les parties réellement dynamiques d'une page par ailleurs statique (ex: un bouton "ajouter au panier" sur une fiche produit statique).

## ISR (Incremental Static Regeneration)

Variante du SSG où les pages sont régénérées en arrière-plan après un délai ou un déclencheur, sans nécessiter un rebuild complet du site.

- Bon compromis quand le contenu est majoritairement stable mais doit se rafraîchir sans attendre un déploiement complet (ex: catalogue produit qui change quotidiennement).
- Implique d'accepter une fenêtre de fraîcheur (le contenu peut être légèrement périmé entre deux régénérations) — vérifier que c'est acceptable pour le cas d'usage (ne l'est pas pour un cours de bourse en temps réel, l'est pour un article de blog).

## Approches hybrides — souvent la bonne réponse

La plupart des applications réelles ne sont pas "tout SSR" ou "tout CSR" : une page marketing en SSG, un tableau de bord authentifié en CSR, une page produit publique en SSR ou ISR. Découper la stratégie de rendu par type de page/route selon son besoin réel plutôt que d'imposer un choix unique à toute l'application évite de payer le coût le plus élevé partout par souci de cohérence artificielle.

## Critère de décision résumé

Poser ces deux questions à chaque route/page :
1. Ce contenu doit-il être indexé par un moteur de recherche ou visible immédiatement sans dépendre de JavaScript ? → SSR, SSG ou ISR selon la fréquence de changement.
2. Ce contenu est-il identique pour tous les visiteurs à un instant donné ? → SSG/ISR possible. Sinon (personnalisé, authentifié) → SSR ou CSR selon le besoin de SEO.
