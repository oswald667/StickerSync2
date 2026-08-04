---
name: master-prompt-architect
description: Transforme une demande de tâche — même courte, floue ou incomplète — en un prompt maître (master prompt) de niveau expert, exhaustif, prêt à être exécuté sans aucun aller-retour supplémentaire avec l'utilisateur. Utiliser dès que l'utilisateur demande de "rédiger un prompt", "créer un prompt optimisé", "écrire un master prompt", "préparer un prompt pour Claude Code / Cursor / une autre IA", "transformer cette idée en prompt exploitable", ou dit "j'ai besoin d'un prompt béton pour...". S'applique à toute tâche (rédaction, marketing, code, projet logiciel, contenu...), pas seulement aux projets web — le prompt final embarque déjà tout ce dont l'exécutant a besoin (cahier des charges, user stories détaillées, règles métier, charte graphique sans patterns IA génériques, architecture technique, plan de tests, environnement local si pertinent) pour produire un résultat viable et reproductible d'une IA à l'autre.
---

# Master Prompt Architect

## Champ d'application

Ce skill s'applique à **toute** demande de l'utilisateur, pas seulement aux projets logiciels ou aux pages web : rédaction, analyse, recherche, contenu marketing, script vidéo, présentation, plan de formation, stratégie, recette, etc. Le Mode Projet (Étape 4) n'est qu'un cas particulier — le plus exigeant — déclenché uniquement quand la tâche construit un produit numérique. Pour tout le reste, le Mode Standard (Étape 3) s'applique, avec exactement la même exigence de précision et, dès qu'une composante visuelle existe, la même exclusion des patterns génériques IA (Étape 5).

## Philosophie

Un prompt maître réussi, c'est un prompt qui a déjà répondu à toutes les questions que l'exécutant (une autre IA, ou une future session) aurait besoin de poser. Le travail de ce skill n'est donc pas "d'écrire un joli texte" : c'est de faire, maintenant, tout le travail de clarification, de décision et de cadrage qui serait sinon fait à retardement pendant l'exécution — et de coucher ce travail dans un document structuré.

Concrètement, cela veut dire : avant d'écrire la moindre ligne du prompt final, il faut se demander "si je donnais cette tâche telle quelle à quelqu'un de compétent mais qui ne connaît rien du contexte, quelles questions me poserait-il en retour ?" — puis répondre à chacune d'elles dans le prompt, soit parce que l'utilisateur les a précisées, soit via une hypothèse raisonnable explicitement assumée.

Un prompt maître qui laisse une zone grise n'a pas atteint son objectif, même s'il est long et bien écrit.

**Le test de reproductibilité** — c'est le critère ultime pour juger si un prompt maître est terminé : *si je donnais ce prompt à deux IA différentes, qui ne se parlent pas entre elles, est-ce qu'elles produiraient un résultat quasiment identique ?* Si sur un point précis la réponse est "non, ça dépendrait de l'interprétation de chacune", ce point n'est pas assez spécifié — ce n'est pas un détail à laisser à l'appréciation de l'exécutant, c'est un trou à combler. Une phrase comme "l'utilisateur reçoit une confirmation" est une intention ; "un bandeau vert apparaît en haut de l'écran avec le texte exact 'Habitude ajoutée', visible 3 secondes" est une spécification. Vise systématiquement le second niveau, pas le premier.

## Étape 1 — Cadrage express (obligatoire avant d'écrire quoi que ce soit)

Ne jamais commencer à rédiger le prompt final sans avoir d'abord clarifié :

1. **La tâche exacte et son livrable final concret** — qu'est-ce qui doit exister à la fin de l'exécution du prompt (un texte, un fichier, une appli fonctionnelle, un plan) ?
2. **Le contexte d'usage** — pour qui, dans quel but, avec quelles contraintes déjà connues (délai, public, plateforme, budget, outils imposés) ?
3. **Le niveau de profondeur nécessaire** — voir Étape 2 pour choisir entre Mode Standard et Mode Projet.
4. **Les préférences de forme/ton** si elles sont pertinentes pour la tâche.

Pose ces questions de façon groupée et ciblée (utilise l'outil de questions à choix si disponible, sinon une liste courte dans le message). Ne demande que ce qui changerait réellement le contenu du prompt final — pour tout le reste, pose une hypothèse raisonnable et assume-la ouvertement plutôt que de multiplier les allers-retours. L'utilisateur doit pouvoir répondre en une fois.

**Le réflexe clé de cette étape** : passe explicitement en revue "qu'est-ce que l'IA qui exécutera ce prompt pourrait avoir besoin de savoir et que je n'ai pas encore ?" — format de sortie, contraintes de style, cas limites, exemples de ce qui est réussi/raté, ressources disponibles, environnement technique. Chaque question sans réponse anticipée est une brèche qui obligera l'utilisateur final à intervenir, ce qui est précisément ce que ce skill doit éliminer.

## Étape 2 — Choix du mode

- **Mode Standard** : la tâche est une tâche "normale" — rédaction, analyse, campagne marketing, script, fonctionnalité isolée, tâche de recherche, création de contenu, etc.
- **Mode Projet (approfondi)** : la tâche implique de construire un produit ou un projet numérique complet ou substantiel (application, site, feature end-to-end avec plusieurs écrans/flux). Dans ce cas, lis et suis `references/project_mode_template.md`, qui détaille la structure complète attendue (cahier des charges, user stories, charte graphique, assets, règles métier, architecture technique, plan de tests, environnement de test local, definition of done).

Si tu hésites entre les deux, pose la question à l'utilisateur plutôt que de deviner — c'est une décision structurante qui change la taille et la nature du livrable.

## Étape 3 — Anatomie d'un prompt maître (Mode Standard)

Un bon prompt maître en Mode Standard contient, dans cet ordre, les sections suivantes (adapte les intitulés à la tâche, mais ne saute pas le contenu) :

1. **Rôle & posture** — quelle expertise/personnalité l'IA exécutante doit incarner (ex : "Tu es un rédacteur technique senior spécialisé en...").
2. **Contexte** — qui est le destinataire final, pourquoi cette tâche existe, quelles contraintes sont déjà fixées.
3. **Objectif final & définition du succès** — ce que "réussi" veut dire concrètement, si possible avec des critères mesurables.
4. **Livrable attendu** — format exact (fichier .md/.docx/.pptx, longueur, structure imposée, ton).
5. **Contraintes et règles à respecter** — ce qu'il faut faire et ce qu'il faut éviter, styles interdits, éléments obligatoires. Dès que la tâche a une composante visuelle (page web, poster, présentation, maquette, post à mettre en forme...), c'est ici que doit apparaître l'exclusion explicite des patterns génériques IA décrite dans `references/anti_patterns_ui.md` — ce n'est pas réservé au Mode Projet, une simple landing page ou un visuel ponctuel y est tout autant exposé.
6. **Étapes de raisonnement demandées** — décompose la tâche en sous-étapes logiques si elle est complexe, pour guider l'exécution plutôt que de tout demander en vrac.
7. **Exemples / contre-exemples** — quand la qualité attendue est subjective (ton, style), un exemple de ce qui marche et de ce qui ne marche pas vaut mieux qu'une longue description abstraite.
8. **Hypothèses posées** — liste explicite de toutes les décisions prises à la place de l'utilisateur pendant le cadrage, avec une phrase de justification chacune. C'est la section qui garantit qu'aucune zone grise n'a été laissée sans réponse.
9. **Checklist d'auto-vérification** — les points que l'exécutant doit vérifier lui-même avant de considérer la tâche terminée (voir Étape 5).

Utilise des balises ou des titres Markdown clairs pour séparer ces sections : cela aide énormément l'IA exécutante à s'y retrouver et à ne rien oublier, surtout sur un prompt long.

## Étape 4 — Mode Projet

Dès que le Mode Projet est choisi, ouvre `references/project_mode_template.md` avant d'écrire quoi que ce soit — il détaille section par section comment construire un prompt qui, exécuté seul par n'importe quelle IA, doit produire un projet viable de bout en bout et **le même projet, quelle que soit l'IA qui l'exécute** : cahier des charges complet, cas d'utilisation, user stories décrivant précisément la saisie utilisateur et le comportement côté client et côté serveur, règles métier, machines d'état pour toute entité ayant un statut, charte graphique, liste d'assets, micro-interactions et animations, architecture technique avec schéma de données complet, plan de tests avec cas de test utilisateurs concrets, et instructions pour monter un environnement de test local fonctionnel.

Ne réinvente pas cette structure à chaque fois : le fichier de référence existe justement pour que le Mode Projet reste complet et cohérent d'une exécution à l'autre.

## Étape 5 — Techniques de prompt engineering à toujours injecter

Quel que soit le mode, applique systématiquement ces principes issus des bonnes pratiques de prompt engineering — ils sont ce qui distingue un prompt "correct" d'un prompt réellement de niveau expert :

- **Structure explicite** : sépare les sections avec des titres ou des balises, plutôt qu'un bloc de texte continu — un exécutant s'y retrouve beaucoup mieux et oublie moins de détails.
- **Raisonnement étape par étape** : pour toute tâche à plusieurs composantes, demande explicitement de décomposer et traiter chaque sous-partie avant de conclure, plutôt que d'espérer que l'exécutant s'organise seul.
- **Format de sortie sans ambiguïté** : précise toujours la forme exacte attendue (longueur, fichier, structure, ce qui doit et ne doit pas y figurer). C'est la cause la plus fréquente de résultats hors sujet.
- **Exemples positifs et négatifs** : dès que la qualité attendue est subjective (ton, style visuel, niveau de détail), un exemple vaut mieux qu'un adjectif. Montre ce qui est réussi et, si possible, ce qui serait raté.
- **Anticipation des cas limites** : liste les situations ambiguës prévisibles (données manquantes, contraintes contradictoires, choix multiples possibles) et donne la décision à prendre dans chaque cas, pour que l'exécutant n'ait pas à improviser ni à demander.
- **Critères de vérification intégrés** : termine toujours par une checklist ou des critères d'acceptation que l'exécutant doit lui-même valider avant de livrer — cela transforme le prompt en boucle auto-corrective plutôt qu'en simple instruction unidirectionnelle.
- **Exclusion des patterns visuels génériques IA** : dès que la tâche implique une interface visuelle (page web, application, maquette), lis `references/anti_patterns_ui.md` avant de rédiger la section charte graphique. Les IA génératives convergent vers un tout petit nombre de looks reconnaissables (dégradés violets par défaut, fond crème + serif + terracotta, glassmorphism, emojis en guise d'icônes d'interface, sur-animation systématique) — un prompt maître doit explicitement les exclure et imposer une direction visuelle assumée, justifiée et spécifique au produit plutôt que de laisser l'exécutant y retomber par réflexe.

## Étape 6 — Auto-vérification avant livraison

Avant de livrer le prompt final, relis-le et vérifie point par point :

- [ ] Toute information nécessaire à l'exécution figure dans le prompt — en le lisant seul, sans accès à l'utilisateur, l'exécutant n'aurait aucune question bloquante à poser.
- [ ] Le format de sortie attendu est précisé sans ambiguïté (nature du livrable, longueur, structure).
- [ ] Les critères de succès sont concrets et si possible mesurables ou vérifiables.
- [ ] **Test de reproductibilité passé** : pour chaque comportement décrit (une interaction, un calcul, un affichage), le prompt dit *quoi exactement*, pas seulement *quoi en principe* — deux exécutants différents liraient la même chose et produiraient le même résultat.
- [ ] La section "Hypothèses posées" existe et couvre toutes les décisions prises à la place de l'utilisateur.
- [ ] Si Mode Projet : le prompt couvre bien cahier des charges, cas d'utilisation, user stories détaillées (saisie/comportement client/comportement serveur/résultat), règles métier, machines d'état, charte graphique, assets, micro-interactions, architecture technique avec schéma de données complet, plan de tests (avec cas concrets) et mise en place de l'environnement de test local — voir la checklist détaillée dans `references/project_mode_template.md`.
- [ ] Si la tâche implique une interface visuelle : la charte graphique exclut explicitement les patterns génériques IA (dégradés par défaut, emojis en icônes d'interface, fond crème + terracotta, sur-animation...) et impose une direction assumée et justifiée — voir `references/anti_patterns_ui.md`.
- [ ] Le prompt est rédigé à l'impératif, clair, sans jargon superflu, et reste lisible malgré sa longueur (titres, listes, sections).
- [ ] Rien n'a été laissé "à l'appréciation de l'exécutant" sur un point qui aurait pu être tranché à l'avance.

Si un point de cette checklist échoue, corrige le prompt avant de le livrer — ne livre jamais un prompt qui échouerait à sa propre checklist.

## Étape 7 — Livraison

Le prompt final est un livrable à part entière : crée-le comme fichier Markdown (`prompt-maitre-<slug-de-la-tache>.md`) plutôt que de le laisser uniquement dans le fil de conversation, afin que l'utilisateur puisse le réutiliser tel quel dans une autre session, un autre outil, ou avec un autre modèle.

Le fichier doit être auto-suffisant : quelqu'un qui l'ouvre sans avoir lu la conversation doit pouvoir comprendre la tâche et l'exécuter directement.

Accompagne la livraison d'un message bref qui résume ce qui a été construit et les hypothèses principales posées — pas besoin de réexpliquer tout le contenu du fichier, l'utilisateur peut l'ouvrir.
