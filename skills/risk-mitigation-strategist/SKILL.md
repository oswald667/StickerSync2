---
name: risk-mitigation-strategist
description: >
  Use this skill whenever the user has a list of identified risks (especially output from critical-risk-review, a pre-mortem, a red-team analysis, or a "verdict" on a risky project) and now wants to know what to DO about them — concrete mitigation strategies, next actions, or how to fix/de-risk a project rather than just diagnose it. Triggers include: "comment on règle", "quelles solutions", "comment mitiger", "trouve des solutions à ces risques", "comment corriger ça", "que faire face à ces risques", "plan d'action", or any follow-up after a critical risk review asking how to move forward. Always chain this skill after critical-risk-review when the user asks for solutions to risks that were just identified — do not silently invent generic advice instead of systematically working through each risk raised.
---

# Risk Mitigation Strategist

## Rôle

Ce skill prend en entrée une liste de risques déjà identifiés — idéalement issus du skill `critical-risk-review`, mais potentiellement fournie par l'utilisateur lui-même ou extraite d'une conversation précédente — et produit un plan d'action concret pour chacun.

Contrairement à `critical-risk-review`, ce skill n'est pas là pour démolir le projet une seconde fois. Mais il n'est pas là non plus pour rassurer à bon compte : une mitigation vague ("communiquer davantage", "faire attention à la qualité") est un échec de ce skill au même titre qu'un déni de risque serait un échec de l'autre. **Chaque mitigation doit être actionnable dès la semaine prochaine, pas une intention.**

---

## Étape 0 — Récupérer la liste de risques

- Si l'utilisateur vient de recevoir une sortie de `critical-risk-review` dans la conversation, réutilise-la telle quelle comme entrée — ne redemande pas à l'utilisateur de la reformuler.
- Si l'utilisateur fournit sa propre liste de risques (informelle, en vrac), reformule-la brièvement en une liste numérotée avant de continuer, pour t'assurer qu'aucun risque n'est perdu ou fusionné à tort avec un autre.
- Si un risque est formulé de façon trop vague pour être traité ("le marché est risqué"), affine-le en une ou deux sous-questions internes avant de proposer une mitigation — ne mitige jamais un risque flou par une réponse floue.

---

## Étape 1 — Classer chaque risque selon les 4 stratégies génériques

Pour CHAQUE risque, choisis explicitement laquelle de ces quatre stratégies s'applique le mieux, et assume ce choix (ne mélange pas tout par défaut en "mitiger un peu") :

1. **Éviter** — changer le plan pour que le risque ne se pose plus du tout (ex: abandonner une fonctionnalité trop risquée plutôt que de la sécuriser à moitié)
2. **Atténuer (mitiger)** — réduire la probabilité ou l'impact sans éliminer le risque
3. **Transférer** — faire porter le risque à un tiers (assurance, sous-traitant, partenaire, clause contractuelle, choix d'un fournisseur qui absorbe le risque technique)
4. **Accepter** — décision consciente de vivre avec le risque tel quel, parce que le coût de le traiter dépasse le bénéfice — mais alors le dire explicitement, avec la raison, plutôt que de l'ignorer silencieusement

Un risque "Fatal" (issu de `critical-risk-review`) ne peut presque jamais être simplement "accepté" — si c'est la conclusion à laquelle tu arrives, justifie-la fortement ou reformule le risque en action d'évitement.

---

## Étape 2 — Pour chaque risque, produire un plan d'action concret

Pour chaque risque, fournis :

- **Action immédiate** (cette semaine) — la plus petite étape vérifiable qui réduit réellement le risque, pas un vœu pieux
- **Action structurelle** (ce mois-ci / cette phase du projet) — le changement plus profond (architecture, contrat, positionnement, embauche, etc.)
- **Signal d'alerte à surveiller** — une métrique ou un événement concret qui indiquerait que le risque est en train de se matérialiser malgré la mitigation (ex: "taux de conversion sous 1% après 3 mois" plutôt que "si ça ne marche pas")
- **Coût/effort de la mitigation** — sois honnête si la mitigation proposée coûte cher en temps ou argent ; ne minimise pas le coût pour rendre la solution plus séduisante

---

## Étape 3 — Prioriser le plan d'action global

Ne présente pas les mitigations dans le même ordre que la liste de risques d'origine. Réordonne selon :
1. Risques Fatals × probabilité élevée d'abord
2. Puis les mitigations à faible coût / fort impact (les "quick wins" réels, pas des vœux pieux)
3. Puis le reste, dans un ordre de dépendance logique (ex: valider un MVP resserré avant de construire l'intégration coûteuse)

Termine par une section "Ce qu'on ne va PAS résoudre maintenant" si certains risques sont sciemment reportés — la transparence sur ce qui n'est pas traité est aussi importante que le plan lui-même.

---

## Ce que ce skill ne fait PAS

- ❌ Ne réduit jamais un risque en changeant simplement sa formulation pour qu'il paraisse moins grave — la mitigation doit changer la réalité du projet, pas le texte du risque.
- ❌ Ne propose pas une mitigation identique et générique pour des risques de nature différente (technique, marché, légal, financier) — chaque catégorie appelle des leviers différents.
- ❌ Ne relance pas une analyse critique des risques elle-même — si l'utilisateur veut challenger à nouveau le projet (y compris le plan de mitigation proposé), c'est le rôle de `critical-risk-review`, pas de ce skill. Il est raisonnable de suggérer de repasser par ce skill pour challenger le plan de mitigation une fois qu'il existe.

---

## Format de sortie

```markdown
# Plan de Mitigation des Risques — [Nom du projet]

## Priorité 1 — Risques fatals à traiter en premier
### Risque : [nom]
- **Stratégie** : Éviter / Atténuer / Transférer / Accepter (+ justification si Accepter)
- **Action immédiate** : ...
- **Action structurelle** : ...
- **Signal d'alerte à surveiller** : ...
- **Coût/effort** : ...

## Priorité 2 — Quick wins (faible coût, fort impact)
[même structure]

## Priorité 3 — Le reste
[même structure]

## Ce qu'on ne va PAS résoudre maintenant
[Liste assumée des risques mis de côté, avec la raison]
```
