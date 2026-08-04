---
name: accessible-react-ui
description: >
  Composants React accessibles conformes WCAG 2.1 AA. Utiliser ce skill pour : auditer
  l'accessibilité d'une interface React, corriger les problèmes ARIA, implémenter la
  navigation clavier, assurer les contrastes de couleur, rendre les formulaires accessibles,
  implémenter l'internationalisation (i18n) avec react-i18next, créer des composants
  accessibles (modales, menus déroulants, tooltips, tableaux). Déclencher sur :
  "accessibilité", "a11y", "WCAG", "ARIA", "screen reader", "navigation clavier",
  "contraste", "lecteur d'écran", "formulaire accessible", "i18n", "internationalisation",
  "react-i18next", "focus management", "skip link", "alt text", "rôle ARIA".
---

# Accessible React UI

Accessibilité WCAG 2.1 AA et internationalisation dans React — du composant à l'audit.

---

## 1. Fondamentaux ARIA — Ce qu'il faut savoir

### 1.1 Règles d'utilisation d'ARIA

```
Règle 1 : Toujours préférer le HTML sémantique natif à ARIA
  ✅ <button>  plutôt que  <div role="button">
  ✅ <nav>     plutôt que  <div role="navigation">
  ✅ <h2>      plutôt que  <div role="heading" aria-level="2">

Règle 2 : Ne pas changer la sémantique native inutilement
  ❌ <button role="link">   → button ne devient pas un lien

Règle 3 : Les contrôles interactifs DOIVENT être focusables
  → tabIndex={0} sur les éléments interactifs non-natifs
  → tabIndex={-1} pour gérer le focus programmatiquement

Règle 4 : Les éléments focusables DOIVENT avoir un label
  → aria-label, aria-labelledby, ou texte visible
```

### 1.2 Attributs ARIA essentiels

```tsx
// Labellisation
aria-label="Description directe"
aria-labelledby="id-du-titre"       // Pointe vers un élément texte visible
aria-describedby="id-description"   // Description supplémentaire (hint, erreur)

// État
aria-expanded={isOpen}              // Accordéon, menu déroulant
aria-selected={isSelected}          // Onglet, item de liste
aria-checked={isChecked}            // Checkbox custom
aria-disabled={isDisabled}          // Désactivé (mais toujours focusable)
aria-invalid={!!error}              // Champ en erreur
aria-required={true}                // Champ obligatoire
aria-busy={isLoading}               // Contenu en cours de chargement

// Live regions
aria-live="polite"    // Annonce après l'action en cours
aria-live="assertive" // Annonce immédiate (erreurs critiques seulement)
aria-atomic={true}    // Annoncer le contenu entier si partiellement modifié
```

---

## 2. Gestion du focus

### 2.1 Focus visible — Ne jamais supprimer outline

```css
/* ❌ Jamais */
*:focus { outline: none; }
button:focus { outline: 0; }

/* ✅ Personnaliser sans supprimer */
*:focus-visible {
  outline: 2px solid #005fcc;
  outline-offset: 2px;
  border-radius: 4px;
}

/* :focus-visible = seulement quand navigation clavier (pas au clic) */
```

### 2.2 Gestion du focus dans les modales

```tsx
// hooks/useFocusTrap.ts
import { useEffect, useRef } from "react";

const FOCUSABLE_ELEMENTS = [
  "a[href]",
  "button:not([disabled])",
  "input:not([disabled])",
  "select:not([disabled])",
  "textarea:not([disabled])",
  '[tabindex]:not([tabindex="-1"])',
].join(", ");

export function useFocusTrap(isActive: boolean) {
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!isActive || !containerRef.current) return;

    const container = containerRef.current;
    const focusableElements = Array.from(
      container.querySelectorAll<HTMLElement>(FOCUSABLE_ELEMENTS)
    );
    const firstEl = focusableElements[0];
    const lastEl = focusableElements[focusableElements.length - 1];

    // Mettre le focus sur le premier élément à l'ouverture
    firstEl?.focus();

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key !== "Tab") return;
      if (e.shiftKey) {
        if (document.activeElement === firstEl) {
          e.preventDefault();
          lastEl?.focus();
        }
      } else {
        if (document.activeElement === lastEl) {
          e.preventDefault();
          firstEl?.focus();
        }
      }
    };

    container.addEventListener("keydown", handleKeyDown);
    return () => container.removeEventListener("keydown", handleKeyDown);
  }, [isActive]);

  return containerRef;
}

// Composant Modal accessible
function Modal({ isOpen, onClose, title, children }: ModalProps) {
  const trapRef = useFocusTrap(isOpen);
  const previousFocusRef = useRef<Element | null>(null);

  useEffect(() => {
    if (isOpen) {
      previousFocusRef.current = document.activeElement;
    } else {
      // Retourner le focus à l'élément qui a ouvert la modale
      (previousFocusRef.current as HTMLElement)?.focus();
    }
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-title"
      ref={trapRef}
    >
      <h2 id="modal-title">{title}</h2>
      {children}
      <button onClick={onClose} aria-label="Fermer la modale">✕</button>
    </div>
  );
}
```

### 2.3 Skip Link — Navigation rapide clavier

```tsx
// app/App.tsx — À placer en tout premier dans le DOM
export function App() {
  return (
    <>
      {/* Skip link — visible seulement au focus clavier */}
      <a
        href="#main-content"
        className="sr-only focus:not-sr-only focus:absolute focus:top-4 focus:left-4
                   focus:z-50 focus:bg-white focus:px-4 focus:py-2 focus:rounded focus:shadow"
      >
        Aller au contenu principal
      </a>
      <Navbar />
      <main id="main-content" tabIndex={-1}>
        {/* Contenu principal */}
      </main>
    </>
  );
}
```

---

## 3. Formulaires accessibles

```tsx
// ✅ Formulaire de contact accessible complet
function ContactForm() {
  const { register, handleSubmit, formState: { errors } } = useForm();
  const errorId = (field: string) => `${field}-error`;
  const hintId = (field: string) => `${field}-hint`;

  return (
    <form noValidate onSubmit={handleSubmit(onSubmit)}>
      {/* Résumé des erreurs en haut (annoncé aux screen readers) */}
      {Object.keys(errors).length > 0 && (
        <div
          role="alert"
          aria-live="assertive"
          aria-atomic="true"
        >
          <p>Le formulaire contient {Object.keys(errors).length} erreur(s) :</p>
          <ul>
            {Object.entries(errors).map(([field, error]) => (
              <li key={field}>
                <a href={`#${field}`}>{String(error?.message)}</a>
              </li>
            ))}
          </ul>
        </div>
      )}

      <div>
        {/* Label TOUJOURS visible — jamais juste placeholder */}
        <label htmlFor="email">
          Adresse email
          <span aria-hidden="true"> *</span>
          <span className="sr-only"> (obligatoire)</span>
        </label>

        {/* Hint sous le label, référencé par aria-describedby */}
        <p id={hintId("email")}>Format attendu : nom@domaine.fr</p>

        <input
          id="email"
          type="email"
          autoComplete="email"
          aria-required="true"
          aria-invalid={!!errors.email}
          aria-describedby={`${hintId("email")} ${errors.email ? errorId("email") : ""}`}
          {...register("email", { required: "L'email est obligatoire." })}
        />

        {/* Message d'erreur lié par id */}
        {errors.email && (
          <p id={errorId("email")} role="alert" aria-live="polite">
            {String(errors.email.message)}
          </p>
        )}
      </div>

      <button type="submit">Envoyer</button>
    </form>
  );
}
```

---

## 4. Composants accessibles courants

### 4.1 Bouton icon-only

```tsx
// ❌ Incompréhensible pour un screen reader
<button onClick={onDelete}>🗑️</button>

// ✅ Label explicite
<button onClick={onDelete} aria-label="Supprimer le produit">
  <TrashIcon aria-hidden="true" />
</button>
```

### 4.2 Menu déroulant accessible

```tsx
function Dropdown({ label, options, onSelect }: DropdownProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [activeIndex, setActiveIndex] = useState(-1);
  const buttonRef = useRef<HTMLButtonElement>(null);
  const listRef = useRef<HTMLUListElement>(null);

  const handleKeyDown = (e: React.KeyboardEvent) => {
    switch (e.key) {
      case "ArrowDown":
        e.preventDefault();
        setIsOpen(true);
        setActiveIndex(i => Math.min(i + 1, options.length - 1));
        break;
      case "ArrowUp":
        e.preventDefault();
        setActiveIndex(i => Math.max(i - 1, 0));
        break;
      case "Enter":
      case " ":
        if (isOpen && activeIndex >= 0) {
          onSelect(options[activeIndex]);
          setIsOpen(false);
          buttonRef.current?.focus();
        } else {
          setIsOpen(o => !o);
        }
        break;
      case "Escape":
        setIsOpen(false);
        buttonRef.current?.focus();
        break;
    }
  };

  return (
    <div onKeyDown={handleKeyDown}>
      <button
        ref={buttonRef}
        aria-haspopup="listbox"
        aria-expanded={isOpen}
        aria-controls="dropdown-list"
        onClick={() => setIsOpen(o => !o)}
      >
        {label}
      </button>

      {isOpen && (
        <ul
          id="dropdown-list"
          ref={listRef}
          role="listbox"
          aria-label={label}
        >
          {options.map((option, index) => (
            <li
              key={option.value}
              role="option"
              aria-selected={index === activeIndex}
              onClick={() => { onSelect(option); setIsOpen(false); }}
            >
              {option.label}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
```

### 4.3 Table de données accessible

```tsx
<table aria-label="Liste des commandes" aria-rowcount={totalRows}>
  <caption className="sr-only">
    Tableau des commandes récentes — {totalRows} résultats
  </caption>
  <thead>
    <tr>
      <th scope="col">N° commande</th>
      <th scope="col">
        <button
          aria-sort={sortField === "date" ? sortOrder : "none"}
          onClick={() => handleSort("date")}
        >
          Date
          {sortField === "date" && (
            <span aria-hidden="true">{sortOrder === "ascending" ? "↑" : "↓"}</span>
          )}
        </button>
      </th>
      <th scope="col">Statut</th>
      <th scope="col">Actions</th>
    </tr>
  </thead>
  <tbody>
    {orders.map((order) => (
      <tr key={order.id} aria-rowindex={order.rowIndex}>
        <td>{order.number}</td>
        <td>{formatDate(order.date)}</td>
        <td>
          <span
            aria-label={`Statut : ${order.statusLabel}`}
            className={`badge badge-${order.status}`}
          >
            {order.statusLabel}
          </span>
        </td>
        <td>
          <a
            href={`/orders/${order.id}`}
            aria-label={`Voir le détail de la commande ${order.number}`}
          >
            Détail
          </a>
        </td>
      </tr>
    ))}
  </tbody>
</table>
```

---

## 5. Contraste de couleur — Calcul et outils

```
Ratios minimum WCAG 2.1 AA :
  Texte normal (< 18px) : 4.5:1
  Texte large (≥ 18px ou 14px bold) : 3:1
  Composants UI (boutons, inputs) : 3:1 sur le fond

Outils :
  - whocanuse.com — Simulateur daltonisme + checker contraste
  - colourcontrast.cc — Vérifier toutes les paires couleur/fond
  - axe DevTools (extension Chrome) — Audit automatique

Palette sûre pour fond blanc (#FFFFFF) :
  ✅ Texte principal : #1a1a1a (21:1)
  ✅ Texte secondaire : #595959 (7:1)
  ✅ Texte désactivé : #767676 (4.54:1) ← limite WCAG AA
  ✅ Bleu action : #005fcc (8.59:1)
  ✅ Rouge erreur : #c0392b (5.1:1)
  ✅ Vert succès : #1e7e34 (4.87:1)
  ⚠️ Orange warning : #e67e22 → 2.5:1 sur blanc, utiliser du texte foncé dessus
```

---

## 6. Internationalisation — react-i18next

### 6.1 Configuration

```typescript
// src/i18n/index.ts
import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import LanguageDetector from "i18next-browser-languagedetector";

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    fallbackLng: "fr",
    supportedLngs: ["fr", "en"],
    ns: ["common", "errors", "products"],   // Espaces de noms par feature
    defaultNS: "common",
    interpolation: { escapeValue: false },  // React échappe déjà
    detection: {
      order: ["localStorage", "navigator"],
      caches: ["localStorage"],
    },
  });

// public/locales/fr/products.json
{
  "list": {
    "title": "Catalogue produits",
    "count_one": "{{count}} produit",
    "count_other": "{{count}} produits",   // Pluralisation automatique
    "empty": "Aucun produit disponible pour le moment."
  },
  "form": {
    "name": "Nom du produit",
    "price": "Prix (€)",
    "submit": "Créer le produit",
    "submit_loading": "Création en cours..."
  },
  "errors": {
    "required": "Ce champ est obligatoire.",
    "price_positive": "Le prix doit être positif."
  }
}
```

### 6.2 Usage dans les composants

```tsx
import { useTranslation } from "react-i18next";

function ProductList({ products }: { products: Product[] }) {
  const { t, i18n } = useTranslation("products");

  return (
    <section aria-label={t("list.title")}>
      <h1>{t("list.title")}</h1>

      {/* Pluralisation */}
      <p aria-live="polite">
        {t("list.count", { count: products.length })}
      </p>

      {/* Date formatée selon la locale */}
      <time dateTime={product.createdAt.toISOString()}>
        {new Intl.DateTimeFormat(i18n.language, {
          dateStyle: "medium",
          timeStyle: "short",
        }).format(new Date(product.createdAt))}
      </time>

      {/* Prix formaté selon la locale */}
      {new Intl.NumberFormat(i18n.language, {
        style: "currency",
        currency: "EUR",
      }).format(product.price)}
    </section>
  );
}
```

---

## 7. Tests d'accessibilité automatisés

```typescript
// Axe-playwright pour les tests E2E
import { checkA11y, injectAxe } from "axe-playwright";

test("Products page passes accessibility audit", async ({ page }) => {
  await page.goto("/products");
  await injectAxe(page);
  await checkA11y(page, undefined, {
    detailedReport: true,
    detailedReportOptions: { html: true },
    axeOptions: {
      runOnly: { type: "tag", values: ["wcag2a", "wcag2aa"] },
    },
  });
});

// Jest + jest-axe pour les composants
import { render } from "@testing-library/react";
import { axe, toHaveNoViolations } from "jest-axe";
expect.extend(toHaveNoViolations);

it("ProductForm has no accessibility violations", async () => {
  const { container } = render(<ProductForm onSuccess={() => {}} />);
  const results = await axe(container);
  expect(results).toHaveNoViolations();
});
```

---

## 8. Checklist accessibilité — Audit rapide

```
Structure :
  [ ] Un seul <h1> par page, hiérarchie des titres cohérente (h1→h2→h3)
  [ ] Landmark regions présentes (<main>, <nav>, <header>, <footer>)
  [ ] Skip link en début de page

Images :
  [ ] Images informatives : alt descriptif
  [ ] Images décoratives : alt="" (chaîne vide, pas absente)
  [ ] Icônes avec texte : aria-hidden="true" sur l'icône

Formulaires :
  [ ] Chaque input a un <label> associé (htmlFor / id)
  [ ] Erreurs liées par aria-describedby
  [ ] Champs obligatoires indiqués (aria-required)

Interactivité :
  [ ] Tous les éléments cliquables sont focusables (Tab)
  [ ] Focus visible sur tous les éléments (outline non supprimé)
  [ ] Modales piègent le focus (focus trap)
  [ ] Fermeture modale par Echap
  [ ] Boutons icon-only ont aria-label

Contenu dynamique :
  [ ] Zones mises à jour ont aria-live approprié
  [ ] Messages de succès/erreur annoncés aux screen readers

Contrastes :
  [ ] Texte normal ≥ 4.5:1
  [ ] Texte large ≥ 3:1
  [ ] Composants UI ≥ 3:1
```

---

## Références complémentaires

- `references/screen-reader-testing.md` — Guide de test manuel avec NVDA (Windows), VoiceOver (Mac/iOS), TalkBack (Android)
- `references/i18n-locales.md` — Fichiers de traduction de base (fr/en) pour les patterns communs (erreurs, dates, formulaires)
