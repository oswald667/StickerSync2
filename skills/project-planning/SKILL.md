---
name: project-planning
description: >
  Planification de projets web et mobile pour un développeur fullstack freelance ou en équipe.
  Utiliser ce skill pour : découper un projet en phases et tâches, rédiger des user stories,
  estimer les charges de développement, prioriser un backlog, créer une roadmap technique,
  rédiger un cahier des charges technique, planifier les sprints, identifier les risques,
  structurer la livraison à un client. Déclencher sur : "planifier", "roadmap", "backlog",
  "user story", "estimation", "sprint", "planning", "cahier des charges", "découpage",
  "priorisation", "MVP", "phases du projet", "livraison client", "charge de travail",
  "timeline", "jalons", "risques projet".
---

# Project Planning

Planification de projets web/mobile fullstack — du brief client au backlog actionnable.

---

## 1. Processus de planification — Vue globale

```
Brief client / besoin
       ↓
1. Clarification & périmètre          ← Éviter les malentendus avant de commencer
       ↓
2. Découpage fonctionnel (Epics)      ← Vue macro du produit
       ↓
3. User Stories par Epic              ← Granularité utilisable
       ↓
4. Estimation des charges             ← Points ou jours/homme
       ↓
5. Priorisation (MoSCoW / Impact)     ← Ce qui va dans le MVP
       ↓
6. Roadmap et phases                  ← Séquençage dans le temps
       ↓
7. Identification des risques         ← Ce qui peut dérailler
       ↓
8. Découpage en sprints               ← Plan d'action immédiat
```

---

## 2. Clarification du périmètre — Questions obligatoires

Avant tout découpage, obtenir les réponses à ces questions :

### Périmètre produit
- Quelles sont les fonctionnalités **absolument indispensables** au lancement ?
- Quelles fonctionnalités sont "nice to have" ?
- Y a-t-il des intégrations tierces (paiement, email, SMS, maps) ?
- L'app doit-elle fonctionner hors-ligne ?

### Utilisateurs et contexte
- Qui sont les utilisateurs (rôles, droits différents) ?
- Volume estimé d'utilisateurs au lancement ? Dans 6 mois ?
- Contexte d'utilisation : mobile, desktop, les deux ?
- Contrainte de langue / internationalisation ?

### Technique et contraintes
- Stack imposée ou libre ?
- Intégration avec des systèmes existants (legacy, ERP, CRM) ?
- Contraintes réglementaires (RGPD, données médicales, financières) ?
- Budget et délai maximum ?

### Livraison
- Qui valide les livrables (interlocuteur technique ? décideur métier ?) ?
- Fréquence de livraison attendue (hebdo, bi-mensuelle) ?
- Environnement de recette fourni ou à mettre en place ?

---

## 3. Découpage en Epics

Une **Epic** = une grande fonctionnalité cohérente, trop grande pour un sprint.

**Template d'Epic :**
```
Epic : [Nom court]
Objectif : [Ce que l'utilisateur peut faire une fois l'epic terminée]
Critère de succès : [Comment mesurer que c'est réussi]
Dépendances : [Quelle autre epic doit être faite avant]
```

**Exemple pour une marketplace :**

| ID | Epic | Objectif utilisateur | Dépendances |
|----|------|---------------------|-------------|
| E1 | Authentification | Se connecter / créer un compte | — |
| E2 | Catalogue produits | Parcourir et rechercher des produits | E1 |
| E3 | Panier & Commande | Passer une commande | E2 |
| E4 | Paiement | Payer en ligne | E3 |
| E5 | Espace vendeur | Gérer ses produits et commandes | E1 |
| E6 | Notifications | Recevoir des alertes en temps réel | E3, E4 |
| E7 | Administration | Gérer la plateforme | E1 |

---

## 4. User Stories — Rédaction précise

**Format standard :**
```
En tant que [rôle utilisateur],
Je veux [action / fonctionnalité],
Afin de [bénéfice / objectif].

Critères d'acceptation :
  - Scénario 1 : Étant donné [contexte], Quand [action], Alors [résultat attendu]
  - Scénario 2 : ...

Définition of Done :
  - [ ] Tests unitaires écrits et passants
  - [ ] Tests d'intégration couvrant les cas nominaux et erreurs
  - [ ] Code revu par pair / auto-revu
  - [ ] Comportement validé sur mobile et desktop
  - [ ] Documentation technique mise à jour si API modifiée
```

**Exemple détaillé :**
```
US-E1-01 : Inscription par email

En tant que visiteur,
Je veux créer un compte avec mon email et un mot de passe,
Afin d'accéder aux fonctionnalités réservées aux membres.

Critères d'acceptation :
  - [Nominal] Email valide + mdp ≥ 8 chars → compte créé, email de confirmation envoyé, redirection vers /dashboard
  - [Email existant] → message "Cet email est déjà utilisé." sans créer de compte
  - [Email invalide] → message d'erreur sous le champ en temps réel
  - [Mdp < 8 chars] → indicateur de force et message "8 caractères minimum"
  - [Serveur down] → message "Erreur temporaire, réessayez dans quelques instants."

Contraintes techniques :
  - Mot de passe hashé (bcrypt / Django set_password)
  - Email de confirmation via template HTML
  - Token de confirmation expirant après 24h

Estimation : 3 points (M)
```

---

## 5. Estimation des charges

### 5.1 Fibonacci pour les story points

```
1 pt  → Très simple (changer un label, ajouter un champ)       ~2h
2 pts → Simple (endpoint CRUD basique, composant simple)         ~4h
3 pts → Standard (feature complète avec validation + tests)      ~1j
5 pts → Complexe (intégration tierce, logique métier avancée)   ~2j
8 pts → Très complexe (refactoring, architecture nouvelle)       ~3-4j
13 pts → Trop grande → découper en user stories plus petites
```

### 5.2 Coefficients à appliquer

```
Incertitude technique     × 1.3   (nouvelle librairie, API inconnue)
Intégration tierce        × 1.5   (paiement, OAuth, SMS...)
Migration de données      × 1.5   (données existantes à transformer)
Tests complets            × 1.3   (unit + intégration + E2E)
Revue de code en équipe   × 1.1
Buffer projet             × 1.2   (imprévus, allers-retours client)
```

### 5.3 Table d'estimation rapide par type de tâche (React + Django)

| Tâche | Estimation de base |
|-------|--------------------|
| Endpoint Django CRUD simple (list, create, retrieve, update, delete) | 0.5j |
| Endpoint avec logique métier complexe + permissions | 1j |
| Intégration paiement (Stripe) | 2-3j |
| Authentification complète (login, register, reset pwd, JWT) | 2j |
| Upload de fichiers (S3 + validation) | 1j |
| Notifications email (templates + envoi) | 1j |
| Page React simple (liste + filtre) | 0.5j |
| Formulaire React complexe (multi-step, validation, erreurs API) | 1j |
| Dashboard avec graphiques | 1-2j |
| Configuration CI/CD de base | 1j |
| Mise en production initiale (Docker + VPS + SSL) | 1-2j |

---

## 6. Priorisation — MoSCoW

| Catégorie | Signification | Critère |
|-----------|--------------|---------|
| **Must have** | Indispensable au MVP | Sans ça, le produit n'a pas de valeur |
| **Should have** | Important mais pas bloquant | Frustrant à l'absence mais surmontable |
| **Could have** | Amélioration appréciable | Ajoute de la valeur si le temps le permet |
| **Won't have (this time)** | Hors scope v1 | À planifier pour une version future |

**Règle MVP** : le Must have ne doit jamais dépasser 60% du budget total. Les 40% restants absorbent les imprévus.

---

## 7. Roadmap — Template de phases

```
Phase 0 — Setup & fondations            (Semaine 1-2)
  → Infrastructure : repo Git, environnements dev/staging/prod
  → CI/CD de base (tests automatiques, déploiement staging)
  → Structure du projet (Django + React, Docker, DB)
  → Authentification (Must have — bloque tout le reste)
  Livrable : Environnements fonctionnels, login/register opérationnel

Phase 1 — MVP core                      (Semaines 3-6)
  → Toutes les user stories "Must have"
  → Tests unitaires et d'intégration
  Livrable : Version testable par le client (staging)

Phase 2 — Stabilisation & qualité       (Semaines 7-8)
  → Corrections issues identifiées en phase 1
  → Tests E2E sur les flux critiques
  → Performance : optimisation requêtes DB, lazy loading
  → UX : ajustements sur les retours client
  Livrable : Version production-ready

Phase 3 — Should have                   (Semaines 9-12)
  → Features importantes non bloquantes
  → Monitoring (Sentry, métriques)
  Livrable : Version enrichie

Phase 4 — Could have & évolutions        (À partir de la semaine 13)
  → Features de confort
  → Nouvelles fonctionnalités selon retours utilisateurs
```

---

## 8. Identification des risques

```
Template d'évaluation des risques :

| Risque | Probabilité (1-3) | Impact (1-3) | Score | Mitigation |
|--------|-------------------|--------------|-------|------------|
| API tierce indisponible | 2 | 3 | 6 | Mode dégradé + retry + monitoring |
| Dérive du périmètre | 3 | 2 | 6 | Contrat fixe + CR d'évolution facturé |
| Complexité sous-estimée | 2 | 2 | 4 | Buffer 20% sur les estimations |
| Changement de specs en cours | 3 | 3 | 9 | Gel des specs après validation phase 0 |
| Données de prod non disponibles | 1 | 3 | 3 | Demander accès au départ, pas à la fin |
```

**Score > 6 → plan de contingence obligatoire avant de démarrer.**

---

## 9. Template de sprint (2 semaines)

```
Sprint N — [Objectif en une phrase]
Dates : du [date] au [date]

Engagement du sprint :
  US-E1-03 : Reset password                    [3 pts]
  US-E2-01 : Liste des produits avec filtres   [5 pts]
  US-E2-02 : Page détail produit               [3 pts]
  US-E2-03 : Recherche full-text              [5 pts]
  Total : 16 points

Points de synchronisation :
  - Lundi matin : point rapide (15 min) — blockers ?
  - Mercredi midi : mi-sprint review — on est dans les clous ?
  - Vendredi fin de sprint : démo client + rétrospective

Definition of Done du sprint :
  [ ] Toutes les US livrées en staging
  [ ] Tests passants (CI verte)
  [ ] Démo réalisée et validée par le client
  [ ] Backlog mis à jour (nouvelles US identifiées ajoutées)
```

---

## 10. Livrables client — Ce qu'on produit

### Document de cadrage initial (avant démarrage)
```
1. Compréhension du besoin (reformulation du brief)
2. Périmètre du projet (inclus / exclu)
3. Epics et user stories principales
4. Estimations de charge par Epic
5. Budget et délai estimés
6. Phases de livraison proposées
7. Risques identifiés et mitigations
8. Prérequis client (accès, ressources, décisions à prendre)
9. Modalités de collaboration (réunions, outils, process de validation)
```

### Rapport d'avancement (chaque semaine)
```
[Date] — Semaine N

✅ Terminé cette semaine :
  - US-E2-01 : Liste produits avec filtres — en staging
  - US-E2-02 : Page détail produit — en staging

🔄 En cours :
  - US-E2-03 : Recherche full-text (70% — livraison lundi)

⚠️ Points d'attention :
  - L'intégration Stripe prend plus de temps que prévu (+1j)
  - Besoin d'accès aux credentials de la sandbox Stripe avant vendredi

📅 Semaine prochaine :
  - Finaliser US-E2-03
  - Démarrer US-E3-01 : Panier
```

---

## Références complémentaires

- `references/estimation-templates.md` — Tableaux d'estimation préconfigurés par type de projet (marketplace, SaaS, app mobile)
- `references/client-communication.md` — Templates d'emails pour jalons, retards, évolutions de scope
