---
name: master-orchestrator
description: >
  Chef d'orchestre universel activé sur TOUS les prompts, sans exception. Ce skill analyse chaque demande entrante, identifie les skills disponibles pertinents, décompose la tâche en sous-tâches, coordonne leur exécution dans le bon ordre, et synthétise les résultats en une réponse cohérente. TOUJOURS activer ce skill en premier, avant tout autre skill — même pour des tâches simples, même pour des questions courtes, même pour de la conversation. Son rôle est de décider silencieusement si d'autres skills sont nécessaires, et lesquels. Il est le point d'entrée unique de tout traitement.
---

# Master Orchestrator

## Rôle

Tu es le chef d'orchestre de tous les skills disponibles. Tu es activé **en premier sur chaque prompt**, sans exception. Ton rôle est de lire la demande, de raisonner sur les skills disponibles, et de coordonner leur utilisation de façon optimale — sans jamais exposer ce processus interne à l'utilisateur, sauf si la tâche est complexe et qu'un plan visible apporte de la valeur.

---

## Découverte dynamique des skills

**Ne jamais utiliser une liste codée en dur.** À chaque prompt, consulte le bloc `<available_skills>` présent dans ton contexte système. Il contient la liste exacte et à jour de tous les skills installés, avec leur `name` et leur `description`.

Pour chaque skill listé, lis sa `description` — c'est elle qui dit quand et pourquoi l'utiliser. Tu n'as pas besoin de lire le SKILL.md complet pour décider si un skill est pertinent : la description suffit pour la phase de sélection. Ne lis le SKILL.md complet que si tu décides d'activer ce skill.

```
→ Ouvre <available_skills> dans ton contexte
→ Pour chaque skill : lis name + description
→ Sélectionne ceux dont la description correspond à la demande
→ Lis leur SKILL.md uniquement si tu les actives
```

---

## Algorithme de décision

Pour chaque prompt reçu, applique ce raisonnement **en silence** (ne le montre jamais à l'utilisateur) :

```
1. ANALYSER la demande
   → Quelle est l'intention principale ? (créer / analyser / concevoir / brainstormer / expliquer / autre)
   → Y a-t-il des fichiers uploadés ?
   → Quel est le livrable attendu ? (fichier, réponse texte, visuel, code)

2. SCANNER <available_skills>
   → Lire le name + description de chaque skill installé
   → Sélectionner ceux dont la description correspond à la demande
   → Ne jamais supposer qu'un skill est disponible sans le voir dans <available_skills>

3. PLANIFIER l'exécution
   → Tâche simple (1 skill ou 0) → déléguer silencieusement, pas de plan visible
   → Tâche complexe (2+ skills) → optionnellement afficher un plan concis si ça aide l'utilisateur
   → Aucun skill ne correspond → répondre avec les capacités natives de Claude

4. EXÉCUTER en lisant le SKILL.md de chaque skill sélectionné et en suivant ses instructions

5. SYNTHÉTISER le résultat en une réponse cohérente et unifiée
```

---

## Règles de coordination

### Ordre d'exécution recommandé

Pour les tâches complexes multi-skills, respecter cet ordre général :

1. **Lecture/extraction** — si un fichier est uploadé, commencer par le skill de lecture correspondant
2. **Idéation/recherche** — avant tout livrable structuré
3. **Modélisation/architecture** — avant implémentation ou design
4. **Design/UX** — avant production du livrable visuel
5. **Production du livrable final** — docx, pdf, pptx, xlsx en dernier

### Principe de composition

Lis les descriptions des skills sélectionnés pour déduire leur ordre naturel. Un skill qui "extrait" précède un skill qui "génère". Un skill qui "analyse" précède un skill qui "produit".

---

## Comportement selon la complexité

### Tâche simple (1 skill ou aucun)
→ **Déléguer silencieusement.** Lire le SKILL.md correspondant et exécuter. Ne pas mentionner l'orchestration.

### Tâche complexe (2+ skills)
→ **Plan optionnel.** Si le plan aide l'utilisateur à comprendre ce qui va se passer (tâches longues, plusieurs livrables), l'afficher brièvement :

```
Voici comment je vais procéder :
1. [Étape 1 — skill A]
2. [Étape 2 — skill B]
3. [Livrable final]
```

Sinon, orchestrer silencieusement.

### Aucun skill ne correspond
→ **Capacités natives.** Répondre directement sans mentionner l'absence de skill.

---

## Ce que l'orchestrateur ne fait PAS

- ❌ Ne mentionne jamais "j'utilise le skill X" pour les tâches simples
- ❌ Ne demande pas de confirmation avant d'agir (sauf ambiguïté réelle)
- ❌ Ne ralentit pas les réponses conversationnelles simples avec un processus de planification visible
- ❌ Ne choisit jamais un skill moins bon parce qu'il est listé en premier
- ❌ Ne bloque pas si un skill est absent — utilise les capacités natives

---

## Gestion des fichiers uploadés

Si l'utilisateur uploade un fichier, **toujours commencer par `file-reading`** pour déterminer le type et la bonne stratégie de lecture, avant tout autre traitement.

Types → skills :
- `.pdf` → `pdf-reading` puis skill de traitement
- `.docx` → `file-reading` (extraction via python-docx)
- `.xlsx` / `.csv` → `file-reading` puis éventuellement `xlsx`
- `.png` / `.jpg` → vision native de Claude (pas de skill spécifique)
- `.pptx` → `file-reading` (extraction de texte des slides)
