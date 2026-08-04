# Gabarit Mode Projet

Ce gabarit s'applique quand la tâche demande de construire un projet numérique complet (application, site, feature end-to-end). L'objectif du prompt maître produit ici n'est pas seulement qu'il donne un résultat viable une fois exécuté — c'est qu'il donne **le même résultat**, quelle que soit l'IA qui l'exécute. Deux exécutants différents, qui ne se parlent pas, doivent arriver à des projets quasi indiscernables l'un de l'autre. C'est le fil conducteur de tout ce gabarit : chaque section existe pour éliminer un point où deux exécutants pourraient légitimement interpréter différemment la même intention.

Avant de remplir ce gabarit, assure-toi d'avoir obtenu (ou posé comme hypothèse explicite) : nature du projet, utilisateurs cibles, stack technique préférée ou imposée, contraintes de délai/budget, existence ou non d'une identité visuelle déjà définie.

Le prompt maître final doit contenir, dans cet ordre, les sections suivantes.

## 1. Rôle & cadrage général

Définit la posture de l'exécutant et rappelle l'objectif final : produire un projet viable, lançable, testé, identique à ce qu'un autre exécutant produirait à partir du même prompt.

## 2. Cahier des charges

- **Contexte et problème résolu**, **objectifs mesurables**, **périmètre in/out of scope**, **contraintes techniques et business** — comme un cahier des charges classique.
- **Hypothèses posées** : toute décision prise pendant le cadrage à la place de l'utilisateur, justifiée en une phrase.

## 3. Cas d'utilisation

Avant de détailler les user stories, liste tous les cas d'utilisation (use cases) qui composent le projet, sous forme de tableau ou de liste structurée. Pour chaque cas d'utilisation :

- **Acteur** : qui déclenche ce cas (utilisateur connecté, visiteur anonyme, administrateur...).
- **Déclencheur** : l'action précise qui démarre le scénario.
- **Précondition** : l'état du système requis pour que ce cas soit possible (ex : "l'utilisateur doit être connecté", "au moins une habitude doit exister").
- **Scénario nominal** : la suite d'étapes dans le cas normal, en une liste numérotée.
- **Scénarios alternatifs/erreurs** : ce qui se passe si une précondition n'est pas remplie ou si une étape échoue.

Cette section sert de carte d'ensemble ; chaque cas d'utilisation listé ici sera ensuite détaillé au niveau interface/serveur dans la section User Stories.

## 4. User stories — niveau de détail exécutable

C'est la section la plus critique pour la reproductibilité inter-IA. Une user story rédigée seulement comme "En tant que X, je veux Y, afin de Z" est une bonne intention mais laisse trop de place à l'interprétation : deux IA différentes vont imaginer des champs différents, des messages différents, des comportements différents. Pour chaque user story, complète **systématiquement** les cinq sous-sections suivantes :

> ### US-[numéro] — [titre court]
> **Récit** : En tant que [rôle], je veux [action], afin de [bénéfice].
> **Priorité** : MVP obligatoire / important / nice-to-have.
>
> **1. Entrée utilisateur** — liste exhaustive des champs/actions saisis par l'utilisateur, avec pour chacun : nom du champ, type (texte, nombre, date, sélection parmi une liste fermée...), obligatoire ou non, valeur par défaut si applicable, règle de validation exacte (longueur min/max, format, plage de valeurs) et message d'erreur exact affiché si la validation échoue.
>
> **2. Comportement côté client (immédiat)** — ce qui se passe visuellement dès l'action de l'utilisateur, avant même la réponse du serveur si pertinent : état de chargement (ex : bouton désactivé + spinner), changement visuel immédiat, verrouillage de champs. Sois explicite sur le texte exact affiché, pas une paraphrase ("le bouton affiche le texte 'Enregistrement...'" et non "le bouton indique que ça charge").
>
> **3. Comportement côté serveur** — l'endpoint appelé (méthode HTTP + chemin), la forme exacte de la requête (champs envoyés), la logique appliquée côté serveur (calculs, vérifications, effets de bord en base de données — quelles tables/lignes sont créées, modifiées, supprimées), et la forme exacte de la réponse pour le cas de succès (code de statut HTTP + structure de la donnée retournée) et pour chaque cas d'échec (code de statut + message d'erreur exact).
>
> **4. Résultat observable par l'utilisateur** — ce que l'utilisateur voit une fois la réponse serveur reçue : message de confirmation exact, changement d'état visuel exact (couleur, texte, position), redirection éventuelle vers un écran précis. Précise la durée d'affichage des messages temporaires (ex : toast visible 3 secondes).
>
> **5. Cas d'erreur** — pour chaque erreur possible (validation échouée, ressource introuvable, absence de droits, conflit), précise le déclencheur exact et le message exact affiché à l'utilisateur ainsi que le code HTTP retourné.

Regroupe les user stories par épopée/fonctionnalité. Ne laisse jamais une de ces cinq sous-sections vide "car évidente" — c'est précisément l'évidence supposée qui fait diverger deux exécutants différents.

## 5. Règles métier

Liste toutes les règles de gestion non évidentes à deviner : calculs spécifiques (formule exacte), statuts possibles d'une entité et transitions autorisées, permissions par rôle, cas limites métier. Chaque règle doit être formulée comme une condition testable ("si X alors Y"), pas comme un principe général.

## 6. Machines d'état & transitions

Pour toute entité du projet qui possède un statut ou un cycle de vie (ex : une commande, une tâche, un compte utilisateur), décris sa machine d'état explicitement :

- La liste complète des statuts possibles.
- Pour chaque transition autorisée : l'état de départ, l'événement déclencheur exact, l'état d'arrivée, et les effets de bord exacts qu'elle produit (ce qui change en base de données, ce qui est notifié, ce qui s'affiche).
- Précise aussi les transitions **interdites** qu'on pourrait être tenté d'ajouter, si elles sont ambiguës (ex : "un statut 'archivé' ne peut jamais repasser à 'actif' dans ce MVP").

Un schéma texte simple (`brouillon → soumis → validé → archivé`) accompagné du détail de chaque flèche suffit — pas besoin de diagramme graphique.

## 7. Charte graphique

Avant de rédiger cette section, lis `references/anti_patterns_ui.md` et applique-le strictement : le prompt final doit exclure explicitement les looks génériques IA (dégradés par défaut, fond crème + serif + terracotta, glassmorphism, emojis en icônes d'interface, sur-animation) et imposer une direction visuelle assumée, spécifique au produit décrit dans le cahier des charges.

- **Palette de couleurs** avec codes hexadécimaux exacts (primaire, secondaire, accent, succès, erreur, alerte, fonds, textes), chacune justifiée en une phrase par rapport au produit — pas une palette qui conviendrait à n'importe quel autre projet.
- **Typographie** : police(s) exacte(s) choisie(s) avec intention (pas une police système par défaut sans justification), tailles exactes en px/rem pour chaque niveau (titre principal, sous-titre, corps, légende).
- **Grille et espacement** : unité de base d'espacement (ex : 8px) et règle de composition (marges, paddings des cartes/boutons).
- **Composants clés** : rayon de bordure exact, épaisseur et couleur des bordures, ombres (valeurs exactes si utilisées) — sans glassmorphism ni dégradé décoratif par défaut.
- **Accessibilité** : contraste texte/fond conforme, zones de tap d'au moins 44px sur mobile, état de focus clavier visible.

Si l'utilisateur a fourni une charte existante, retranscris-la fidèlement plutôt que d'improviser. Sinon, assume des valeurs précises plutôt que des adjectifs ("bleu" n'est pas une spécification, `#2563EB` en est une), et termine la section par l'instruction d'auto-critique décrite dans `references/anti_patterns_ui.md`.

## 8. Assets

Liste tous les assets nécessaires (logo, favicon, icônes, illustrations) avec, pour chacun, soit la source exacte à utiliser (fichier fourni), soit l'instruction de substitution précise en son absence (ex : "bibliothèque d'icônes Lucide, taille 20px, couleur du texte courant"). Les icônes fonctionnelles de l'interface (navigation, actions, statuts) doivent toujours provenir d'une bibliothèque d'icônes cohérente, jamais d'emojis natifs — voir `references/anti_patterns_ui.md`. Un emoji n'est acceptable que s'il est lui-même un contenu choisi par l'utilisateur final du produit, jamais comme élément de chrome de l'interface.

## 9. Micro-interactions & animations

Toute animation ou transition visuelle notable doit être spécifiée avec ses paramètres exacts, pas décrite en intention :

- **Élément concerné** et **déclencheur** (au clic, à l'apparition, au changement d'état).
- **Propriété animée** (opacité, échelle, position, couleur) avec **valeurs de départ et d'arrivée**.
- **Durée** en millisecondes et **courbe d'accélération** (ease-in, ease-out, linear).

Exemple de niveau de précision attendu : "Au moment où l'utilisateur coche une habitude, l'icône passe d'une échelle 1 à 1.15 puis revient à 1 sur 200ms au total (ease-out), simultanément le fond de la carte transitionne vers la couleur succès sur 150ms." N'invente pas d'animation non nécessaire ; précise uniquement celles qui comptent pour l'expérience définie dans les user stories — une animation sur chaque élément par réflexe est justement l'un des tells d'interface générée par IA listés dans `references/anti_patterns_ui.md`.

## 10. Architecture technique

- **Stack retenue** (frontend, backend, base de données, outils) avec justification brève si choisie par hypothèse.
- **Structure de dossiers/projet** attendue.
- **Schéma de données complet** : pour chaque entité, la liste exhaustive de ses champs avec, pour chacun, le nom exact, le type de donnée, obligatoire/optionnel, valeur par défaut, contraintes (unique, longueur, plage), et les relations avec les autres entités (clé étrangère, cardinalité). Ne décris pas seulement les entités "principales" en survol — un champ non listé ici est un champ que chaque exécutant va nommer différemment.
- **Contrats d'API** : pour chaque endpoint significatif, méthode + chemin + forme de la requête + forme de la réponse (déjà largement couvert dans les user stories section 4, mais regroupe ici la liste complète des endpoints pour vue d'ensemble).
- **Décisions d'architecture notables** (gestion d'état frontend, stratégie d'authentification, gestion des erreurs globales) tranchées à l'avance.

## 11. Plan de tests et cas de test utilisateurs

- **Cas de test fonctionnels** dérivés directement des critères des user stories (action précise → résultat exact attendu, y compris le texte/état visuel).
- **Cas limites et cas d'erreur** à couvrir explicitement, avec le résultat exact attendu pour chacun.
- **Niveau de test attendu** : tests unitaires, tests d'intégration, tests manuels guidés — précise ce qui est exigé plutôt que de laisser l'exécutant décider seul.

## 12. Environnement de test local

- Commandes d'installation des dépendances.
- Commandes de lancement (serveur de dev, base de données locale, éventuel docker-compose).
- Données de démonstration/seed précises (comptes de test avec identifiants exacts, données factices représentatives déjà dans l'état voulu pour tester immédiatement les cas de la section 11).
- Comment vérifier que l'environnement tourne correctement (URL exacte, état attendu à l'écran).

## 13. Definition of Done & critères de livraison

Liste finale et vérifiable de ce qui doit être vrai pour que le projet soit considéré comme livré, incluant explicitement :

- [ ] Toutes les user stories priorité MVP sont implémentées et chacune des 5 sous-sections (entrée, client, serveur, résultat, erreurs) est respectée telle que spécifiée.
- [ ] Toutes les machines d'état de la section 6 sont respectées, transitions interdites incluses.
- [ ] L'environnement de test local est fonctionnel et documenté.
- [ ] Les cas de test définis en section 11 ont été exécutés.
- [ ] La charte graphique et les micro-interactions sont appliquées avec les valeurs exactes spécifiées, pas approximées, et aucun pattern générique IA listé dans `references/anti_patterns_ui.md` (dégradé décoratif, emoji en icône d'interface, sur-animation) ne s'est glissé dans l'implémentation.
- [ ] Aucune règle métier de la section 5 n'est contredite par l'implémentation.

## Checklist de complétude du prompt Mode Projet

Avant de livrer le prompt maître en Mode Projet, vérifie qu'il couvre bien chacune des 13 sections sans section vide ni placeholder du type "à définir", et applique le test de reproductibilité à chaque user story : *si je donnais uniquement cette user story à deux développeurs qui ne communiquent pas entre eux, produiraient-ils exactement les mêmes champs de formulaire, le même endpoint, la même réponse serveur, le même message affiché ?* Si un doute subsiste sur un point précis, complète-le avec une hypothèse assumée plutôt que de le laisser flou — ne compte jamais sur le "bon sens" de l'exécutant pour combler un trou de spécification.
