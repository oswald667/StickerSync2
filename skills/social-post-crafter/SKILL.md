---
name: social-post-crafter
description: Rédige des posts pour réseaux sociaux (LinkedIn, X/Twitter, Instagram, Facebook...) qui sonnent comme écrits par une vraie personne ou une vraie marque, jamais comme un texte généré par IA. Utiliser dès que l'utilisateur demande de "rédiger un post LinkedIn", "écrire un post pour annoncer...", "faire un post pour les réseaux sociaux", "m'aider à communiquer sur..." un lancement, une réalisation, un recrutement, un événement, une actualité d'entreprise ou personnelle, ou fournit un exemple de post existant à imiter dans son style. S'adapte automatiquement à la plateforme visée (longueur, ton, usage des hashtags et des emojis propre à chaque réseau) et à tout type de post (annonce, recrutement, retour d'événement, avis d'expert, célébration d'équipe, storytelling personnel...). Toujours utiliser ce skill plutôt que de rédiger un post social "à la main", car il applique systématiquement une détection et une élimination des tournures qui trahissent un texte écrit par IA.
---

# Social Post Crafter

## Philosophie

Un post social raté ne l'est presque jamais à cause d'une faute de français ou d'une structure bancale — il est raté parce qu'il *sonne faux* ou parce qu'il n'accroche personne. Ce sont deux problèmes différents et il ne faut jamais confondre "corriger le premier" avec "sacrifier le second". Le tiret cadratin (—), les accroches vides suivies de rien de concret, les listes à puces où chaque élément est interchangeable : voilà ce qui sonne faux. L'enthousiasme, les emojis utilisés avec sens, une structure en points forts, une accroche qui frappe fort : voilà ce qui capte l'attention, et ce n'est jamais à sacrifier au nom de la sobriété.

Si l'utilisateur fournit un exemple de post qu'il aime, ce n'est pas un brouillon à assagir ou à corriger — c'est la cible à égaler sur un autre sujet. Le réflexe à éviter absolument : recevoir un post enthousiaste et structuré, et le retravailler pour le rendre plus neutre en pensant que "sobre" veut dire "plus humain". Ce n'est pas le cas. Le travail de ce skill est d'éliminer les vrais tells (voir Étape 3) tout en préservant, voire en renforçant, ce qui rend un post captivant (voir Étape 3bis).

## Étape 1 — Cadrage express

Avant d'écrire quoi que ce soit, rassemble (par quelques questions ciblées si besoin, ou en les déduisant du message si l'utilisateur les a déjà données) :

1. **Le fait concret à communiquer** — qui, quoi, pourquoi maintenant, quel impact réel. Un post authentique part toujours d'un fait précis, jamais d'une idée abstraite ("on innove") : si l'utilisateur ne donne que l'idée abstraite, demande le detail concret qui la prouve (un chiffre, un nom, une anecdote, une date).
2. **Qui parle** — une entreprise, une équipe, une personne en son nom propre ? Quel est le ton habituel de cette voix (si l'utilisateur donne un exemple de post précédent, c'est la meilleure source pour calibrer le ton — voir Étape 4).
3. **La plateforme visée** — LinkedIn, X/Twitter, Instagram, Facebook, ou autre. Si non précisé, demande-le : la longueur, le ton et l'usage des hashtags en dépendent directement (voir `references/platform_conventions.md`).
4. **L'objectif du post** — informer, recruter, célébrer une équipe, asseoir une expertise, générer de l'engagement, vendre. Ça détermine la structure de la chute et l'éventuel appel à l'action.
5. **Les contraintes de forme** — longueur souhaitée si l'utilisateur en a une en tête, présence ou non de hashtags/emojis, mention de personnes ou de comptes précis à inclure.

Ne multiplie pas les allers-retours : pose ce qui manque vraiment en un minimum de questions groupées, et pour le reste (ton par défaut, usage des emojis) prends une décision assumée plutôt que de demander.

## Étape 2 — Lire les conventions de la plateforme visée

Avant de rédiger, consulte `references/platform_conventions.md` : chaque plateforme a ses propres codes de longueur, de ton, de densité de hashtags et d'usage des emojis, et un post qui ignore ces codes se repère immédiatement comme mal calibré, ce qui est un premier signe de texte non maîtrisé — même sans lien direct avec l'IA, ça nuit à l'authenticité perçue.

## Étape 3 — Éliminer les vrais tells IA (pas la structure ni l'enthousiasme)

Lis `references/ai_writing_tells.md` avant de considérer un brouillon terminé. Ce fichier distingue clairement deux catégories : ce qui trahit réellement un texte IA (le tiret cadratin en premier lieu, les généralités interchangeables, les CTA génériques) et ce qui n'est PAS un problème et ne doit jamais être retiré par réflexe (accroche enthousiaste, listes à puces avec emoji, gras sur les mots clés). Retire uniquement la première catégorie.

## Étape 3bis — Capter l'attention

Un post débarrassé de tout tell IA mais plat n'a résolu que la moitié du travail. Lis `references/captivating_techniques.md` et applique ses leviers : une première ligne qui porte à elle seule le fait le plus fort, un contraste avant/après quand c'est pertinent, des preuves nommées plutôt que des qualificatifs, une chute qui donne du sens plutôt qu'un résumé. L'enthousiasme et la structuration visuelle sont des outils de captation d'attention légitimes, pas des artifices à corriger.

## Étape 4 — S'appuyer sur un exemple fourni, sans le copier bêtement

Si l'utilisateur fournit un post existant comme référence de style (le sien ou celui d'un tiers), analyse-le pour en extraire : le rythme des phrases, la façon dont il structure ses idées (paragraphes courts, listes à puces avec emoji, une seule idée par ligne...), sa densité en emojis et en hashtags, son registre de vocabulaire, sa façon de conclure. Réutilise ces patterns structurels pour le nouveau post, mais **jamais son contenu ni ses tournures de phrases mot pour mot** — le nouveau post doit sonner comme la même voix qui parle d'un sujet différent, pas comme un copier-coller avec les noms changés.

## Étape 5 — Rédiger

Structure générique (à adapter selon la plateforme et le type de post — voir `references/platform_conventions.md`) :

1. **Accroche** — la première ligne doit donner envie de lire la suite sans utiliser une formule clichée (voir la liste d'accroches à éviter dans `references/ai_writing_tells.md`). Elle part idéalement du fait concret le plus frappant, pas d'une généralité.
2. **Corps** — développe le fait concret avec des détails vérifiables (qui a fait quoi, quel chiffre, quelle contrainte surmontée), pas des adjectifs empilés. Si une structuration en liste sert la clarté (plusieurs points distincts de même nature), utilise-la ; sinon garde un texte continu — la liste à puces emoji systématique est elle-même un tell si elle est utilisée par réflexe plutôt que par nécessité.
3. **Chute** — une phrase qui donne le sens de ce que ça représente, ou une reconnaissance humaine (remerciement à une équipe, ouverture d'une question réelle) plutôt qu'un slogan.
4. **Appel à l'action / hashtags** — uniquement si pertinent pour la plateforme et l'objectif, jamais un CTA générique du type "Qu'en pensez-vous ? Dites-le en commentaire !" sans lien avec le contenu précis du post.

## Étape 6 — Auto-vérification avant livraison

Avant de livrer, relis le post et vérifie :

- [ ] **Aucun tiret cadratin (—) nulle part dans le texte** — vérifie caractère par caractère si besoin, c'est le tell le plus reconnaissable.
- [ ] Aucune généralité interchangeable, aucun CTA générique de la liste `references/ai_writing_tells.md` n'apparaît telle quelle.
- [ ] Le post contient au moins un ou deux détails concrets et vérifiables (nom, chiffre, date, anecdote), pas seulement des qualificatifs.
- [ ] La première ligne porte à elle seule le fait le plus fort du post (voir `references/captivating_techniques.md`).
- [ ] Si un exemple de style enthousiaste et structuré a été fourni, cet enthousiasme et cette structuration ont été conservés, pas atténués au nom d'une fausse sobriété.
- [ ] Le format (longueur, hashtags, ton) correspond aux conventions de la plateforme demandée.
- [ ] Lu à voix haute, le texte sonne comme quelqu'un qui raconte vraiment quelque chose avec conviction, pas comme une brochure institutionnelle ni comme un texte artificiellement neutre.
- [ ] Si un exemple de style a été fourni, la voix est cohérente avec cet exemple sans en recopier des phrases.

## Étape 7 — Livraison

Affiche le post directement dans la conversation (pour un copier-coller immédiat) **et** crée un fichier `.md` correspondant (`post-<plateforme>-<slug-du-sujet>.md`) pour que l'utilisateur puisse le retrouver et le réutiliser. Si plusieurs variantes de longueur ou de ton ont du sens, propose-les clairement labellisées plutôt que d'en imposer une seule.
