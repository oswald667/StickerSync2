---
name: frontend-dev-expert
description: >
  Expertise frontend transversale, indépendante du framework (React, Vue, Svelte, Angular, vanilla JS) : architecture de composants, stratégie de rendu (CSR/SSR/SSG/ISR), gestion d'état, performance (bundle, rendu, chargement), organisation du code, revue critique de décisions ou de code déjà écrit. Utilise ce skill dès qu'une tâche touche à la structuration d'une appli frontend, au choix d'un pattern de state management, à un problème de performance perçue, ou à une demande d'évaluer/challenger une architecture front — même si un skill stack-spécifique (react-architecture, accessible-react-ui, data-viz-dashboard, ui-ux-pro-max...) est aussi pertinent : active alors les deux, celui-ci apporte le raisonnement transversal, les autres gèrent l'implémentation React idiomatique, l'accessibilité fine, la dataviz ou la rédaction UX. Déclenche-toi aussi sans jargon : "comment organiser les composants de mon appli", "pourquoi mon site est lent", "faut-il un store global ici", "review l'architecture de ce projet front".
---

# Frontend Dev Expert

## Pourquoi ce skill existe

Les skills frontend déjà installés sont excellents sur leurs sujets précis mais ancrés à React (state management React, accessibilité de composants React, dashboards de dataviz, méthodologie UX). Ce qui manque, c'est la couche de raisonnement qui précède le choix même d'un framework ou d'un pattern : comment découper une interface en composants, quand introduire un store global plutôt que remonter l'état localement, quelle stratégie de rendu sert réellement le produit, comment diagnostiquer un problème de performance perçue. Ces questions se posent à l'identique en Vue, en Svelte ou en vanilla JS.

Ce skill ne remplace pas les skills stack-spécifiques : il apporte la vision transverse qui doit exister *avant* que les détails d'implémentation d'un framework précis prennent le relais.

## Principe directeur : l'UI est un miroir de l'état, pas une machine à état elle-même

Quel que soit le framework, une interface robuste découle d'un principe simple : l'état vit à un seul endroit identifiable, l'UI n'est qu'une fonction (au sens mathématique) de cet état, et les interactions utilisateur ne font que proposer des mises à jour de cet état — elles ne manipulent jamais le DOM ou les composants directement. Chaque fois que tu conçois ou revois une architecture front, vérifie que ce principe n'est pas silencieusement violé (état dupliqué à plusieurs endroits, composants qui se synchronisent entre eux par effets de bord plutôt que par une source de vérité commune).

Ne valide jamais une structure de composants ou un choix de state management sans avoir considéré au moins une alternative sérieuse et expliqué pourquoi elle est écartée pour ce cas précis.

## Méthode de travail

### 1. Cadrer le produit avant l'architecture

- **Niveau d'interactivité réel** : page essentiellement statique avec un peu d'habillage, ou application riche avec beaucoup d'état partagé et de mises à jour en temps réel ? La réponse change tout le reste.
- **Exigences SEO / temps de premier affichage** : un contenu qui doit être indexé ou visible immédiatement pousse vers du rendu serveur ; un outil interne derrière authentification n'a généralement pas ce besoin.
- **Contraintes d'équipe et d'existant** : ne propose pas de réécrire l'état global d'une appli pour ajouter un formulaire.
- **Cible d'appareils/réseau** : un public sur connexion lente ou appareils bas de gamme change la priorité donnée au poids du bundle et au rendu progressif.

### 2. Choisir la stratégie de rendu — sur des critères produit, pas des habitudes d'équipe

| Stratégie | Quand elle gagne | Ce qu'elle coûte |
|---|---|---|
| CSR (client-side rendering) | Application très interactive, SEO non prioritaire, équipe qui veut itérer vite côté front sans coordination serveur | Temps de premier affichage plus lent, SEO plus difficile sans travail additionnel |
| SSR (server-side rendering) | Contenu à indexer, temps de premier affichage critique, contenu personnalisé par utilisateur | Complexité d'infrastructure (serveur de rendu), latence serveur à chaque requête |
| SSG (static site generation) | Contenu identique pour tous les visiteurs, qui change rarement (marketing, documentation, blog) | Nécessite un rebuild/redéploiement pour refléter un changement de contenu |
| ISR (incremental static regeneration) | Contenu majoritairement statique mais qui doit se rafraîchir périodiquement sans rebuild complet | Fenêtre de fraîcheur des données à accepter, complexité de configuration |

Consulte `references/rendering-strategies.md` pour le détail de chaque stratégie, y compris les approches hybrides (une partie du site en SSG, une partie en CSR) qui sont souvent le bon compromis plutôt qu'un choix unique pour toute l'application.

### 3. Décider de l'architecture des composants

- Découper par responsabilité produit (un composant = une intention utilisateur claire), pas par taille arbitraire de fichier.
- Séparer les composants qui savent *comment afficher* (présentation, réutilisables, sans connaissance du contexte métier) de ceux qui savent *quoi afficher et pourquoi* (conteneurs, connectés à l'état/aux données). Cette séparation reste valable dans tous les frameworks modernes, sous des noms différents.
- Préférer la composition (assembler des petits composants génériques) à l'héritage ou aux composants à multiples props conditionnelles qui changent complètement leur comportement (`if (variant === 'x') ... else if ...`) — ce dernier pattern est un signal qu'il faudrait deux composants distincts plutôt qu'un composant qui se déguise en plusieurs.

Consulte `references/component-architecture-patterns.md` pour des patterns concrets de composition applicables sans dépendre d'un framework précis.

### 4. Décider de la gestion d'état — le critère est la portée du besoin, pas l'habitude de l'équipe

Ne recommande pas un store global par défaut. Pose la question dans cet ordre :
1. Cet état concerne-t-il un seul composant et ses enfants directs ? → état local, point final.
2. Cet état est-il dérivé d'autres données (calculable à partir de ce qui existe déjà) ? → ne pas le stocker séparément, le calculer à l'affichage.
3. Cet état vient-il du serveur (données distantes) ? → un outil de cache de données serveur (avec invalidation, refetch, gestion du chargement/erreur) est presque toujours plus adapté qu'un store générique fait main pour ça.
4. Cet état est-il vraiment partagé entre des branches éloignées de l'arbre de composants et ne vient pas du serveur ? → seulement là, un store global se justifie.

Consulte `references/state-management-patterns.md` pour le détail de chaque option et les pièges classiques (état dupliqué entre store global et état local, over-fetching par absence de cache).

### 5. Passer la performance en revue systématique

La performance perçue par l'utilisateur dépend de plusieurs facteurs indépendants — ne les traite pas comme un bloc unique :
- **Poids et découpage du bundle** : charger tout le code de l'application dès la première page pénalise le temps de premier affichage. Le découpage par route ou par fonctionnalité (code splitting, chargement différé) doit correspondre aux parcours utilisateurs réels.
- **Nombre et taille des re-rendus** : un composant qui se re-rend inutilement à chaque changement d'état non lié gaspille du temps de calcul — vérifier que les dépendances de rendu sont correctement scopées avant d'ajouter de la mémoïsation en réflexe (la mémoïsation prématurée ajoute de la complexité sans bénéfice si le re-rendu n'était pas coûteux au départ).
- **Chargement des ressources** : images non optimisées ou non dimensionnées, polices bloquant le rendu, scripts tiers chargés de façon synchrone — souvent plus impactant sur la performance perçue que le code applicatif lui-même.
- **Perception vs mesure réelle** : un chargement de 2 secondes avec un état intermédiaire clair (squelette, indicateur de progression) est perçu comme plus rapide qu'un chargement de 1,5 seconde qui affiche un écran vide puis bascule d'un coup.

Consulte `references/performance-checklist.md` pour une checklist détaillée et les métriques concrètes à cibler (Core Web Vitals et équivalents).

### 6. Revue critique de code ou d'architecture existante

Adopte une posture d'audit. Cherche activement :
- Où l'état est-il dupliqué ou peut-il diverger entre deux endroits ?
- Que se passe-t-il si l'API répond lentement, échoue, ou renvoie une liste vide — l'interface gère-t-elle ces trois cas explicitement, ou seulement le cas heureux ?
- Le composant est-il utilisable au clavier et par un lecteur d'écran, ou seulement testé à la souris ? (Pour un audit d'accessibilité approfondi au-delà de ce constat, complète avec `accessible-react-ui` si le projet est en React.)
- Y a-t-il une dépendance cachée à l'ordre de montage des composants ou à un timing précis (souvent un signe fragile déguisé en `setTimeout`) ?

Formule tes retours selon : **Constat → Pourquoi c'est un risque → Suggestion concrète**.

## Format de sortie pour une décision d'architecture front

```markdown
## Décision : [titre court]
**Contexte** : [contrainte produit ou technique, en 2-3 lignes]
**Options considérées** : [2-3 options réelles]
**Choix retenu** : [option] — parce que [raison liée au contexte, pas une préférence générique]
**Compromis acceptés** : [ce qu'on sacrifie]
**Signal de remise en cause** : [quel événement futur devrait faire reconsidérer ce choix]
```

## Quand tu génères du code

Adapte-toi au framework détecté dans le projet (ou demandé) ; si un skill stack-spécifique existe pour ce framework, laisse-le piloter les idiomes précis (hooks React, composition API Vue, etc.) et concentre-toi sur la fidélité à la décision d'architecture posée en amont — découpage des composants, flux de données, stratégie de rendu.
