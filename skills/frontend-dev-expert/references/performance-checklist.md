# Checklist performance frontend

## Métriques à cibler, pas seulement "c'est rapide"

Une conversation sur la performance sans métrique précise tourne vite en rond. Les Core Web Vitals (ou équivalents) donnent un vocabulaire commun :

- **Temps avant premier affichage utile** (contenu principal visible) — dépend du poids de la page critique et de la latence réseau/serveur.
- **Temps avant interactivité complète** — dépend du temps d'exécution du JavaScript nécessaire pour que les interactions répondent.
- **Stabilité visuelle** (rien ne saute ou ne se déplace pendant le chargement) — dépend de dimensions réservées pour les éléments qui arrivent tardivement (images, publicités, contenu dynamique).

Diagnostiquer un problème de performance sans savoir laquelle de ces trois dimensions est en cause mène souvent à optimiser la mauvaise chose (ex: réduire le JavaScript quand le vrai problème est une image non optimisée qui bloque le rendu).

## Bundle et chargement du code

- **Découpage par route** : ne pas charger le code d'une page que l'utilisateur n'a pas encore visitée. Le découpage par route est le gain le plus simple et le plus impactant sur les applications qui ont grossi sans y prêter attention.
- **Chargement différé des fonctionnalités secondaires** : un widget lourd (éditeur riche, graphique complexe) utilisé rarement ou seulement après une interaction ne doit pas faire partie du chargement initial.
- **Poids des dépendances** : une bibliothèque complète importée pour une seule fonction utilitaire est un gaspillage fréquent — vérifier s'il existe une alternative plus légère ou si un import ciblé (tree-shaking effectif) est possible.
- **Scripts tiers** (analytics, chat, publicité) : souvent le poids le plus lourd et le moins nécessaire immédiatement — charger de façon différée après l'interactivité principale plutôt que bloquer le rendu pour eux.

## Rendu et re-rendus

- Avant d'ajouter de la mémoïsation, vérifier que le re-rendu est réellement coûteux (mesurer, ne pas supposer) — la mémoïsation ajoute de la complexité de lecture et peut elle-même coûter plus cher que le rendu qu'elle évite si mal ciblée.
- Vérifier que les listes longues utilisent une technique de rendu virtualisé (ne monter dans le DOM que les éléments visibles) plutôt que de monter des milliers de nœuds simultanément.
- Éviter de recréer des fonctions ou objets à chaque rendu quand ils sont passés en dépendance à des enfants mémoïsés — ça annule le bénéfice de la mémoïsation en aval.

## Ressources (images, polices, médias)

- Dimensionner explicitement les images (attributs de largeur/hauteur ou équivalent CSS) pour réserver l'espace et éviter les sauts de mise en page pendant le chargement.
- Servir des formats et résolutions adaptés à l'appareil plutôt qu'une seule image lourde pour tous les contextes.
- Charger les images hors du premier écran de façon différée (lazy loading natif ou équivalent) — ne pas différer les images visibles immédiatement, ça retarderait au contraire leur affichage.
- Pour les polices : éviter qu'elles bloquent totalement le rendu du texte (préférer une stratégie qui affiche un texte lisible avec une police de secours pendant le chargement de la police finale).

## Perception plutôt que seule mesure brute

- Un état de chargement explicite (squelette, indicateur de progression) réduit la perception d'attente même à durée égale — ne pas négliger cet aspect au profit de la seule optimisation du temps brut.
- Prioriser le rendu du contenu au-dessus de la ligne de flottaison avant le reste — l'utilisateur juge la rapidité perçue sur ce qu'il voit en premier, pas sur le temps de chargement total de la page.

## Erreur fréquente : optimiser sans mesurer d'abord

Avant toute optimisation, établir où se situe réellement le goulot d'étranglement (réseau, JavaScript, rendu, ressources) plutôt que d'appliquer des techniques d'optimisation générales par réflexe. Une optimisation qui ne cible pas le vrai goulot d'étranglement ajoute de la complexité sans gain mesurable.
