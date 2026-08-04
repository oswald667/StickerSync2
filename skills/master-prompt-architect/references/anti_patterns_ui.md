# Anti-patterns visuels IA à exclure

Ce fichier s'applique dès qu'un prompt maître (Mode Standard ou Mode Projet) implique la conception d'une interface visuelle — page web, application, maquette. Les IA génératives, livrées à elles-mêmes, convergent vers un tout petit nombre de looks reconnaissables : un exécutant qui reçoit un prompt sans direction visuelle imposée retombera très probablement sur l'un d'eux. Le rôle de cette section est d'empêcher ça en imposant des choix assumés et justifiés, jamais des choix par défaut.

Intègre systématiquement, dans la section "Charte graphique" du prompt final, une consigne explicite d'exclusion de ces patterns — ne te contente pas de proposer une palette différente, écris noir sur blanc dans le prompt ce qui est interdit et pourquoi, pour que l'exécutant ne puisse pas y retomber par réflexe.

## Les looks génériques à nommer et exclure explicitement

- **Fond crème + serif + terracotta** : fond quasi-crème (proche `#F4F1EA`), titre en serif à fort contraste, accent terracotta/argile (souvent proche `#D97757`). C'est l'un des looks par défaut les plus reconnaissables des interfaces générées par IA — à exclure sauf si la marque du client utilise déjà réellement ces couleurs.
- **Fond quasi-noir + accent néon unique** : fond noir/anthracite avec un seul accent vif (vert acide, vermillon). Autre look par défaut très reconnaissable.
- **Mise en page "broadsheet"** : colonnes façon journal, filets fins, rayon de bordure à zéro partout. Légitime seulement si le brief appelle explicitement une identité éditoriale/presse.
- **Dégradés décoratifs** : boutons, fonds de héros ou cartes en dégradé (violet→rose, bleu→cyan...) utilisés par défaut sans raison de marque. Une couleur d'accent doit être une teinte unie et assumée, pas un dégradé générique.
- **Glassmorphism par défaut** : cartes translucides avec flou d'arrière-plan utilisées de façon systématique plutôt que pour un besoin précis.
- **Emojis en guise d'icônes d'interface** : utiliser des emojis natifs pour des icônes fonctionnelles (navigation, boutons d'action, statuts, catégories) est un tell immédiat d'interface générée sans direction. Imposer à la place une bibliothèque d'icônes cohérente (Lucide, Heroicons, Phosphor...) avec un poids et une taille définis. Les emojis ne sont acceptables que s'ils sont eux-mêmes le contenu créé par l'utilisateur final du produit (ex : un choix d'avatar), jamais comme élément de chrome de l'interface — et ce choix doit être assumé explicitement dans le prompt, pas laissé par défaut.
- **Marqueurs numérotés décoratifs (01 / 02 / 03)** : à n'utiliser que si le contenu est réellement une séquence ordonnée dont l'ordre porte une information utile (étapes d'un processus). Sinon, c'est de la décoration qui imite un pattern IA reconnaissable.
- **Sur-animation généralisée** : animer systématiquement chaque élément au survol/scroll donne une impression artificielle. N'animer que ce qui sert un besoin d'UX réel et précis (voir la section micro-interactions du gabarit Mode Projet), avec des paramètres exacts plutôt que "quelque chose d'animé".
- **Typographie par défaut sans intention** : utiliser une police système générique (ex : Inter partout) sans justification liée au produit. Le choix de police doit être assumé et cohérent avec la personnalité du produit, avec une vraie hiérarchie de tailles.

## Ce que le prompt final doit imposer à la place

Dans la section Charte graphique, exige explicitement :

- Une palette de 4 à 6 couleurs nommées avec leur code hexadécimal exact, choisie et justifiée en une phrase par rapport au produit précis (pas "une couleur moderne et professionnelle" — une raison concrète liée à ce produit).
- Une paire de polices (une pour les titres, une pour le corps) choisie avec intention, avec une échelle de tailles précise pour chaque niveau hiérarchique.
- Un seul élément signature/distinctif assumé pour l'interface (une façon de présenter une donnée clé, une transition caractéristique, une mise en page de héros spécifique au produit) plutôt qu'une accumulation de décorations.
- Un système de contraste conforme à un niveau d'accessibilité correct (contraste texte/fond suffisant), des zones de tap/clic d'au moins 44px sur mobile, un état de focus clavier visible.
- Des écrans vides et des messages d'erreur écrits dans la voix de l'interface, concrets et orientés action, plutôt que des formulations génériques type "Oups, une erreur est survenue".

## Auto-critique à intégrer dans le prompt

Termine la section Charte graphique du prompt final par une instruction d'auto-critique explicite pour l'exécutant, du type : *"Avant de valider ce choix de direction visuelle, demande-toi si ce même choix serait arrivé sur n'importe quel autre projet similaire, ou s'il est réellement spécifique à ce produit. S'il est générique, reviens en arrière et fais un choix plus caractéristique."* Cette instruction transforme la contrainte en réflexe plutôt qu'en simple interdiction ponctuelle.
