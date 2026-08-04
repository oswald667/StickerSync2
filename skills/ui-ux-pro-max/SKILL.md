---
name: ui-ux-pro-max
description: >
  Expert UI/UX design methodology for professional-grade design decisions, audits, and documentation.
  Use this skill whenever the user asks about: auditing a UI for usability or accessibility; defining
  user flows or information architecture; writing UX copy (microcopy, onboarding, error messages, CTAs);
  designing onboarding, dashboards, forms, or mobile screens; explaining UX heuristics or design system
  principles; producing wireframe specs or design briefs; advising on WCAG accessibility, color contrast,
  or touch targets; choosing between design patterns (modal vs drawer, tab bar vs hamburger); or
  translating product requirements into UX recommendations. Always use this skill before producing any
  UX audit, design spec, UX copy, or design system advice — even quick questions benefit from these frameworks.
---

# UI/UX Pro Max

Professional UI/UX design methodology — from audit to delivery.

---

## 1. Mindset avant tout

Toute décision de design répond à **3 questions** :
1. **Quel est le job-to-be-done de l'utilisateur ?** (pas le besoin fonctionnel, le résultat attendu)
2. **Quel est l'état émotionnel de l'utilisateur à ce moment ?** (frustré, pressé, découvrant, confiant ?)
3. **Quel est le coût d'une erreur ?** (irréversible ? embarrassant ? bloquant ?)

Les réponses guident tout le reste : hiérarchie, ton, densité d'information, friction intentionnelle.

---

## 2. Audit UX — Framework 5 axes

Lors d'un audit ou d'une revue d'interface, évaluer systématiquement :

### Axe 1 — Clarté
- L'utilisateur sait-il **où il est** ? (orientation)
- Sait-il **ce qu'il peut faire** ? (affordances visibles)
- Sait-il **ce qui va se passer** s'il agit ? (feedback prévisible)

### Axe 2 — Efficacité
- Le **chemin critique** (tâche principale) est-il dégagé de friction inutile ?
- Combien d'**étapes / clics** pour accomplir la tâche principale ?
- Y a-t-il des **raccourcis** pour les utilisateurs expérimentés ?

### Axe 3 — Accessibilité
- **Contraste** : ratio minimum 4.5:1 (texte normal), 3:1 (texte large) — WCAG AA
- **Taille des cibles tactiles** : 44×44px minimum (Apple HIG), 48×48dp (Material)
- **Focus visible** : navigation clavier fonctionnelle
- **Labels** : tous les champs de formulaire ont un label explicite (pas juste placeholder)

### Axe 4 — Cohérence
- Les **patterns répétés** sont-ils identiques dans leur forme et comportement ?
- La **typographie** suit-elle une échelle cohérente ?
- Les **espacements** sont-ils basés sur une grille ou un système de tokens ?

### Axe 5 — Confiance & Ton
- L'interface **rassure-t-elle** à chaque étape anxiogène (paiement, suppression, soumission) ?
- Le **microcopy** est-il humain, clair et non-jargonneux ?
- Les **états d'erreur** expliquent-ils le problème ET la solution ?

---

## 3. Heuristiques de Nielsen (10 principes)

Référence pour tout audit ou justification de choix :

| # | Heuristique | Question à se poser |
|---|-------------|---------------------|
| 1 | Visibilité du statut système | L'utilisateur sait-il toujours ce qui se passe ? |
| 2 | Correspondance système / monde réel | Le vocabulaire et les métaphores sont-ils familiers ? |
| 3 | Liberté et contrôle utilisateur | Peut-on annuler, défaire, revenir en arrière ? |
| 4 | Cohérence et standards | Les patterns correspondent-ils aux conventions de la plateforme ? |
| 5 | Prévention des erreurs | L'interface empêche-t-elle les erreurs avant qu'elles surviennent ? |
| 6 | Reconnaissance plutôt que rappel | L'utilisateur doit-il mémoriser des infos entre étapes ? |
| 7 | Flexibilité et efficacité | Y a-t-il des raccourcis pour les experts ? |
| 8 | Design esthétique et minimaliste | Chaque élément est-il nécessaire ? |
| 9 | Aide à la reconnaissance et récupération d'erreur | Les messages d'erreur sont-ils utiles et actionnables ? |
| 10 | Aide et documentation | L'aide est-elle contextuelle et accessible sans quitter la tâche ? |

---

## 4. Patterns de design — Choisir le bon

### Navigation mobile
| Pattern | Quand l'utiliser | Quand éviter |
|---------|-----------------|--------------|
| Tab bar (bottom) | 3–5 sections principales, fréquentes | Plus de 5 items, si l'app est hiérarchique |
| Hamburger menu | Items secondaires ou rares | Navigation principale (cache les options) |
| Top tabs | Contenu segmenté au même niveau | Navigation entre sections très différentes |
| Drawer | Beaucoup d'items, app complexe | Apps simples, si le contenu prime |

### Modales vs Drawers
- **Modal** : action bloquante, confirmation critique, formulaire court
- **Bottom sheet** : sélection rapide, actions contextuelles, options non-bloquantes
- **Full screen** : formulaire complexe, flux multi-étapes

### Formulaires
- Un seul champ par ligne (mobile) → meilleure complétion
- Labels **au-dessus** du champ (pas placeholder seul → disparaît à la saisie)
- Validation **en temps réel sur blur** (pas au submit) pour les champs non-critiques
- Bouton submit **toujours visible**, pas derrière le clavier
- Champs optionnels marqués `(optionnel)`, pas les obligatoires marqués `*`

### États vides (Empty states)
Tout état vide doit avoir : **illustration ou icône** + **titre explicatif** + **action principale**
```
[Icône/Illustration]
"Vous n'avez pas encore de versus"
[Créer mon premier versus]
```

---

## 5. UX Copy — Microcopy de qualité

### Principes
- **Clair > Court** : la brièveté ne justifie pas l'ambiguïté
- **Actionnable** : les CTAs décrivent ce qui se passe ("Enregistrer les modifications" > "OK")
- **Humain** : éviter le jargon technique dans les messages utilisateur
- **Positif** : formuler ce que l'utilisateur **peut** faire, pas ce qu'il **ne peut pas**

### Messages d'erreur — Anatomie
```
❌ "Erreur 422"                              → inutile
❌ "Une erreur est survenue"                 → vague
✅ "Ce pseudo est déjà utilisé.             → problème clair
    Essayez [pseudo]2 ou choisissez-en un autre."  → solution proposée
```

### États de chargement — Hiérarchie
1. **< 300ms** : pas d'indicateur (risque de flash)
2. **300ms–1s** : spinner ou skeleton
3. **> 1s** : skeleton + message contextuel ("Chargement de votre feed…")
4. **> 3s** : barre de progression + estimation si possible

### Onboarding — Règles d'or
- Maximum **3 écrans** de présentation avant la valeur réelle
- **Valeur d'abord**, compte ensuite (si possible, laisser explorer avant de forcer l'inscription)
- Permissions (caméra, notifs) : demander **au moment du besoin**, avec contexte explicite
- Toujours proposer de **passer** l'onboarding

---

## 6. Design System — Tokens fondamentaux

### Typographie — Échelle recommandée (mobile)

| Token | Taille | Usage |
|-------|--------|-------|
| `display` | 32–40sp | Titres hero, splash |
| `headline` | 24–28sp | Titres de page |
| `title` | 18–20sp | Titres de section |
| `body` | 14–16sp | Contenu principal |
| `caption` | 12sp | Métadonnées, timestamps |
| `label` | 11–12sp | Badges, chips |

### Espacement — Grille en 4dp/px

Utiliser exclusivement des multiples de 4 : `4, 8, 12, 16, 20, 24, 32, 40, 48, 64`

### Couleurs — Rôles sémantiques

```
primary     → action principale, CTA
secondary   → action secondaire, alternative
surface     → fond de card, de modal
background  → fond de page
on-primary  → texte sur primary
error       → erreur, suppression, danger
warning     → attention, information critique
success     → confirmation, validation
```

---

## 7. Accessibilité — Checklist rapide

- [ ] Contraste texte : ratio ≥ 4.5:1 (normal), ≥ 3:1 (grand/bold)
- [ ] Contraste composants UI (boutons, inputs) : ≥ 3:1 sur fond
- [ ] Taille tactile : ≥ 44×44pt (iOS) / 48×48dp (Android)
- [ ] Focus visible et logique pour la navigation clavier
- [ ] Images décoratives : `alt=""` ; images informatives : alt descriptif
- [ ] Formulaires : labels associés (pas juste placeholder)
- [ ] Animations : respecter `prefers-reduced-motion`
- [ ] Pas de couleur comme seul vecteur d'information (ajouter icône ou texte)

---

## 8. Livrable UX — Structure d'un brief design

Quand on produit une spec ou un brief UX :

```
## Contexte
[Qui est l'utilisateur, quel problème on résout, dans quel contexte]

## Objectif UX
[Ce que l'utilisateur doit pouvoir accomplir, critère de succès]

## Flux principal (happy path)
[Étapes numérotées de l'entrée à la sortie]

## Cas limites & erreurs
[Que se passe-t-il si X ? Comment l'interface réagit-elle ?]

## Microcopy
[Titres, labels, CTAs, messages d'erreur, états vides]

## Contraintes techniques
[Limites à respecter : performance, plateforme, existant]

## Métriques de succès
[Comment on mesure que ça fonctionne : taux de complétion, NPS, etc.]
```

---

## Références complémentaires

- `references/mobile-patterns.md` — Patterns UI spécifiques iOS et Android (navigation, gestes, conventions)
- `references/design-audit-template.md` — Template d'audit UX complet avec grille de notation
