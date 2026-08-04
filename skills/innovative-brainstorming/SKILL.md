---
name: innovative-brainstorming
description: >
  Use this skill whenever the user wants to brainstorm innovative solutions to a problem, analyze existing solutions on the market, or produce a complete specification document (cahier des charges). Triggers include: "brainstorm", "innov", "solution to", "cahier des charges", "spec document", "problem solving", "what solutions exist for", "how to solve", "startup idea", "product idea", "business idea", or any request to compare existing solutions and propose something better. Also trigger when the user uploads a file (PDF, DOCX) describing a problem and asks for analysis or a solution. This skill covers the FULL workflow: research → analysis → innovation → specification document in Markdown. Always use this skill when the user wants a structured output from a problem statement, even if they don't say "brainstorming" explicitly.
---

# Innovative Brainstorming Skill

## Purpose
Transform a problem statement into a complete, investor-ready specification document (cahier des charges) by:
1. Understanding and clarifying the problem
2. Researching and analyzing existing solutions
3. Synthesizing a superior, innovative solution
4. Producing a structured `.md` specification with user stories, business model, roadmap, and risk analysis

---

## Step 0 — Read the Input

The problem may arrive as:
- **Free text**: Use as-is
- **Uploaded file** (PDF, DOCX, image): Read using the file-reading skill or bash tools as needed. Extract the core problem statement before proceeding.

If the problem statement is vague or ambiguous, ask ONE clarifying question (max) before proceeding. Otherwise, make reasonable assumptions and document them in the output.

**Language rule**: Detect the language of the user's input and produce ALL output in that same language.

---

## Step 1 — Clarify & Frame the Problem

Before researching, internally frame the problem along these axes:
- **Who** is affected? (target population)
- **What** pain/need exists?
- **Why** do current approaches fall short?
- **Where/When** does the problem occur?
- **Scale**: local, national, global?

This framing will be used in the final document's "Problème & Pitch" section.

---

## Step 2 — Research Existing Solutions

Use `web_search` to find 4–6 existing solutions (products, services, approaches) that address the problem.

For **each solution**, structure your internal analysis:
```
Solution: [Name]
Type: [Product / Service / Method / Platform]
Description: [1–2 sentences]
✅ Points forts: [bullet list]
❌ Points faibles / angles morts: [bullet list]
💰 Business model: [how they make money]
```

If the user has already provided solutions in their input, analyze those. If partial info is available, supplement with web search.

**Goal**: Identify the recurring weaknesses across all solutions — these are the innovation gaps.

---

## Step 3 — Synthesize the Innovative Solution

Based on the gap analysis, design a solution that:
- Addresses the top 3 recurring weaknesses of existing solutions
- Introduces at least one truly novel mechanism, feature, or approach
- Is realistically buildable (avoid pure science fiction)
- Has a defensible competitive advantage

Structure your innovation reasoning:
```
Gap identified → How our solution addresses it
Gap 1 → [approach]
Gap 2 → [approach]
Gap 3 → [approach]
Novel differentiator: [what no one else does]
```

---

## Step 4 — Design the Winning Business Model

Choose and adapt the most appropriate business model for the solution. Consider:
- **SaaS / Freemium** — recurring subscription with free tier
- **Marketplace** — commission on transactions
- **B2B Licensing** — enterprise contracts
- **Data Monetization** — aggregate insights sold to third parties
- **Hybrid** — combination of the above

Use the **Business Model Canvas** (9 blocks):
1. Customer Segments
2. Value Propositions
3. Channels
4. Customer Relationships
5. Revenue Streams
6. Key Resources
7. Key Activities
8. Key Partnerships
9. Cost Structure

---

## Step 5 — Generate the Cahier des Charges (.md)

Produce the complete specification document using the template below. Save it as a `.md` file and present it to the user.

---

### 📄 OUTPUT TEMPLATE

```markdown
# [Nom de la Solution] — Cahier des Charges

> **Version** : 1.0 | **Date** : [date] | **Statut** : Draft

---

## 1. 🎯 Problème & Pitch

### 1.1 Énoncé du problème
[Clear 2–3 sentence problem statement]

### 1.2 Hypothèses de départ
[Any assumptions made during analysis]

### 1.3 Pitch de la solution (< 60 mots)
[Elevator pitch: who it's for, what it does, why it's better]

---

## 2. 🔍 Analyse des Solutions Existantes

| Solution | Points Forts | Points Faibles | Modèle Éco |
|----------|-------------|----------------|------------|
| [Name]   | ...         | ...            | ...        |
| ...      | ...         | ...            | ...        |

### 2.1 Gaps stratégiques identifiés
[Numbered list of the key weaknesses across all solutions]

---

## 3. 💡 Notre Solution Innovante

### 3.1 Description générale
[What it is, how it works at a high level]

### 3.2 Avantage concurrentiel
[What makes it defensibly better — the "unfair advantage"]

### 3.3 Fonctionnalités clés
| Priorité | Fonctionnalité | Bénéfice utilisateur |
|----------|---------------|----------------------|
| P0 (MVP) | ...           | ...                  |
| P1       | ...           | ...                  |
| P2       | ...           | ...                  |

---

## 4. 👥 User Stories

> Format: **En tant que** [type d'utilisateur], **je veux** [action], **afin de** [bénéfice].

### 4.1 Identification des types d'utilisateurs
[List all user types: end users, admins, partners, etc.]

### 4.2 User Stories par type

#### 🧑 [Type 1 — ex: Utilisateur Final]
- En tant que [Type 1], je veux ... afin de ...
- En tant que [Type 1], je veux ... afin de ...
- En tant que [Type 1], je veux ... afin de ...
[Minimum 5 user stories per type]

#### 🏢 [Type 2 — ex: Administrateur / Entreprise]
- En tant que [Type 2], je veux ... afin de ...
[Minimum 5 user stories per type]

#### 🤝 [Type 3 — ex: Partenaire / Fournisseur]
- En tant que [Type 3], je veux ... afin de ...
[Minimum 5 user stories per type]

[Add more types as relevant to the solution]

---

## 5. 💰 Modèle Économique (Business Model Canvas)

| Bloc | Contenu |
|------|---------|
| **Segments clients** | ... |
| **Proposition de valeur** | ... |
| **Canaux** | ... |
| **Relations clients** | ... |
| **Sources de revenus** | ... |
| **Ressources clés** | ... |
| **Activités clés** | ... |
| **Partenaires clés** | ... |
| **Structure de coûts** | ... |

### 5.1 Projections de revenus (estimées)
| Phase | Horizon | Revenus estimés | Hypothèses |
|-------|---------|----------------|------------|
| MVP   | 0–6 mois | ... | ... |
| Growth | 6–18 mois | ... | ... |
| Scale | 18–36 mois | ... | ... |

---

## 6. 🗺️ Roadmap & Plan de Déploiement

### Phase 1 — MVP (0–3 mois)
**Objectif** : [Core value delivery]
- [ ] [Milestone 1]
- [ ] [Milestone 2]
- [ ] [Milestone 3]

### Phase 2 — Beta (3–6 mois)
**Objectif** : [First users, validation]
- [ ] [Milestone 1]
- [ ] [Milestone 2]

### Phase 3 — Lancement (6–12 mois)
**Objectif** : [Market entry, growth]
- [ ] [Milestone 1]
- [ ] [Milestone 2]

### Phase 4 — Scale (12–24 mois)
**Objectif** : [Expansion, partnerships]
- [ ] [Milestone 1]
- [ ] [Milestone 2]

---

## 7. ⚠️ Analyse des Risques

| # | Risque | Probabilité | Impact | Mitigation |
|---|--------|-------------|--------|------------|
| R1 | [Risk description] | Élevée/Moyenne/Faible | Élevé/Moyen/Faible | [How to mitigate] |
| R2 | ... | ... | ... | ... |
| R3 | ... | ... | ... | ... |

### Catégories de risques à couvrir :
- Risques techniques
- Risques marché / adoption
- Risques réglementaires / légaux
- Risques financiers
- Risques concurrentiels

---

## 8. 📌 Annexes

### 8.1 Glossaire
[Key terms defined]

### 8.2 Sources & références
[Web sources consulted during research]
```

---

## Execution Checklist

When running this skill, verify before outputting:
- [ ] Problem clearly framed (Step 1)
- [ ] At least 4 existing solutions analyzed (Step 2)
- [ ] Innovation gaps explicitly listed (Step 2)
- [ ] Innovative solution clearly differentiated (Step 3)
- [ ] Business model with 9 BMC blocks filled (Step 4)
- [ ] At least 3 user types identified, 5+ stories each (Step 5)
- [ ] Roadmap has 4 phases with milestones (Step 5)
- [ ] At least 5 risks with mitigations (Step 5)
- [ ] Output saved as `.md` file and presented with `present_files`

---

## Output Format

1. **In chat**: Brief summary (3–5 sentences) of the innovative solution and its key differentiator
2. **As file**: Complete cahier des charges saved as `[solution-name]-cahier-des-charges.md` in `/mnt/user-data/outputs/`
3. **Present** using `present_files` tool

Do NOT dump the entire document in the chat. Always save as a file.
