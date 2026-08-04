---
name: fullstack-testing
description: >
  Stratégie de tests complète pour une stack React + Django + PostgreSQL.
  Utiliser ce skill pour : concevoir une stratégie de tests, écrire des tests unitaires
  Python (pytest, Factory Boy), des tests d'intégration Django (APIClient), des tests
  de composants React (Jest + Testing Library), des tests E2E (Playwright), configurer
  la couverture de code (coverage.py, Istanbul), mettre en place les tests dans CI/CD.
  Déclencher sur : "tests", "pytest", "jest", "testing library", "playwright", "TDD",
  "couverture de code", "mocks", "fixtures", "factory boy", "test unitaire", "test intégration",
  "test E2E", "CI tests", "snapshot test", "APIClient Django".
---

# Fullstack Testing

Stratégie de tests React + Django — de la pyramide à la CI.

---

## 1. La pyramide de tests — Stratégie globale

```
         /--------\
        /  E2E     \          Playwright : 5–10% — flux critiques uniquement
       /  Playwright \         Lents, coûteux, fragiles. Réserver au checkout, auth, onboarding.
      /--------------\
     / Integration    \       Jest (React) + DRF APIClient (Django) : 25–35%
    /  tests           \      Tester les unités ensemble (composant + API mock, view + DB)
   /--------------------\
  /    Unit tests        \    Jest (hooks/utils) + Pytest (models/services) : 55–70%
 /________________________\   Rapides, nombreux, isolés

```

**Règle** : ne pas inverser la pyramide. Trop d'E2E = CI lente et fragile.

---

## 2. Tests Django — Pytest

### 2.1 Configuration pytest

```python
# pytest.ini (ou pyproject.toml)
[pytest]
DJANGO_SETTINGS_MODULE = config.settings.test
python_files = test_*.py
python_classes = Test*
python_functions = test_*
addopts = --strict-markers --tb=short --no-header -rN

# config/settings/test.py
from .base import *

DATABASES = {
    "default": {
        "ENGINE": "django.db.backends.postgresql",
        "NAME": "test_db",          # Base dédiée aux tests
        "TEST": {"NAME": "test_db"},
    }
}

# Pas de cache, pas de celery, pas d'envoi email
CACHES = {"default": {"BACKEND": "django.core.cache.backends.dummy.DummyCache"}}
CELERY_TASK_ALWAYS_EAGER = True   # Tasks synchrones en test
EMAIL_BACKEND = "django.core.mail.backends.locmem.EmailBackend"
```

### 2.2 Factory Boy — Données de test réalistes

```python
# apps/users/tests/factories.py
import factory
from factory.django import DjangoModelFactory
from factory import Faker, SubFactory, LazyAttribute
from apps.users.models import User, Profile

class UserFactory(DjangoModelFactory):
    class Meta:
        model = User
        skip_postgeneration_save = True

    email = Faker("email")
    first_name = Faker("first_name")
    last_name = Faker("last_name")
    password = factory.PostGenerationMethodCall("set_password", "testpass123!")
    is_active = True
    role = "user"

    @factory.post_generation
    def profile(self, create, extracted, **kwargs):
        if not create:
            return
        ProfileFactory(user=self, **kwargs)

class AdminUserFactory(UserFactory):
    role = "admin"
    is_staff = True

class ProductFactory(DjangoModelFactory):
    class Meta:
        model = Product

    name = Faker("sentence", nb_words=3)
    price = Faker("pydecimal", left_digits=3, right_digits=2, positive=True)
    owner = SubFactory(UserFactory)
    status = "active"

    class Params:
        # Traits : ProductFactory(draft=True) → status="draft"
        draft = factory.Trait(status="draft")
        featured = factory.Trait(is_featured=True)
```

### 2.3 Tests de modèles

```python
# apps/products/tests/test_models.py
import pytest
from apps.products.tests.factories import ProductFactory

@pytest.mark.django_db
class TestProductModel:
    def test_soft_delete_marks_as_deleted(self):
        product = ProductFactory()
        product.soft_delete()
        product.refresh_from_db()

        assert product.is_deleted is True
        assert product.deleted_at is not None

    def test_soft_deleted_product_excluded_from_default_queryset(self):
        active = ProductFactory()
        deleted = ProductFactory()
        deleted.soft_delete()

        products = Product.objects.all()  # Manager custom exclut is_deleted=True
        assert active in products
        assert deleted not in products

    def test_publish_changes_status_to_active(self):
        product = ProductFactory(draft=True)
        assert product.status == "draft"

        product.publish()
        assert product.status == "active"
```

### 2.4 Tests d'API (APIClient)

```python
# apps/products/tests/test_views.py
import pytest
from rest_framework.test import APIClient
from apps.users.tests.factories import UserFactory, AdminUserFactory
from apps.products.tests.factories import ProductFactory

@pytest.fixture
def api_client():
    return APIClient()

@pytest.fixture
def auth_client(api_client):
    """Client authentifié en tant qu'utilisateur standard."""
    user = UserFactory()
    api_client.force_authenticate(user=user)
    api_client._user = user  # Accessible dans les tests
    return api_client

@pytest.fixture
def admin_client(api_client):
    admin = AdminUserFactory()
    api_client.force_authenticate(user=admin)
    return api_client

@pytest.mark.django_db
class TestProductAPI:
    def test_list_products_is_public(self, api_client):
        ProductFactory.create_batch(5)
        response = api_client.get("/api/v1/products/")
        assert response.status_code == 200
        assert response.data["success"] is True
        assert len(response.data["data"]) == 5

    def test_create_product_requires_auth(self, api_client):
        payload = {"name": "Test", "price": "29.99", "status": "draft"}
        response = api_client.post("/api/v1/products/", payload)
        assert response.status_code == 401
        assert response.data["code"] == "NOT_AUTHENTICATED"

    def test_create_product_success(self, auth_client):
        payload = {
            "name": "Sneakers premium",
            "price": "89.99",
            "status": "draft",
            "category_id": str(CategoryFactory().id),
        }
        response = auth_client.post("/api/v1/products/", payload)
        assert response.status_code == 201
        assert response.data["success"] is True
        assert response.data["data"]["name"] == "Sneakers premium"
        assert response.data["data"]["owner_id"] == str(auth_client._user.id)

    def test_user_cannot_edit_others_product(self, auth_client):
        other_product = ProductFactory()  # Appartient à un autre user
        response = auth_client.patch(f"/api/v1/products/{other_product.id}/", {"name": "Hacked"})
        assert response.status_code == 403

    def test_list_returns_only_active_products_for_anon(self, api_client):
        ProductFactory(status="active")
        ProductFactory(status="draft")
        response = api_client.get("/api/v1/products/")
        assert len(response.data["data"]) == 1  # Seulement l'actif

    @pytest.mark.parametrize("invalid_price", ["-10", "0", "abc", ""])
    def test_create_product_rejects_invalid_price(self, auth_client, invalid_price):
        payload = {"name": "Test", "price": invalid_price, "status": "draft"}
        response = auth_client.post("/api/v1/products/", payload)
        assert response.status_code == 400
        assert "price" in response.data["errors"]
```

---

## 3. Tests React — Jest + Testing Library

### 3.1 Configuration

```typescript
// vitest.config.ts (ou jest.config.ts)
import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";
import { resolve } from "path";

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: "jsdom",
    setupFiles: ["./src/test/setup.ts"],
    coverage: {
      reporter: ["text", "html", "lcov"],
      exclude: ["src/test/**", "**/*.d.ts", "**/*.config.*"],
      thresholds: {
        lines: 70,
        functions: 70,
        branches: 60,
      },
    },
  },
  resolve: { alias: { "@": resolve(__dirname, "./src") } },
});

// src/test/setup.ts
import "@testing-library/jest-dom";
import { server } from "./mocks/server"; // MSW

beforeAll(() => server.listen({ onUnhandledRequest: "error" }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());
```

### 3.2 Mock Service Worker (MSW) — Intercepter les appels API

```typescript
// src/test/mocks/handlers.ts
import { http, HttpResponse } from "msw";

export const handlers = [
  http.get("/api/v1/products/", ({ request }) => {
    const url = new URL(request.url);
    const status = url.searchParams.get("status");
    const products = mockProducts.filter((p) => !status || p.status === status);
    return HttpResponse.json({
      success: true,
      data: products,
      meta: { count: products.length },
    });
  }),

  http.post("/api/v1/products/", async ({ request }) => {
    const body = await request.json() as any;
    return HttpResponse.json(
      { success: true, data: { id: "new-id", ...body }, message: "Produit créé." },
      { status: 201 }
    );
  }),

  http.post("/api/v1/auth/login/", async ({ request }) => {
    const { email, password } = await request.json() as any;
    if (email === "wrong@test.com") {
      return HttpResponse.json(
        { success: false, code: "AUTH_FAILED", message: "Identifiants incorrects." },
        { status: 401 }
      );
    }
    return HttpResponse.json({
      success: true,
      data: { access: "mock-token", user: mockUser },
    });
  }),
];

// src/test/mocks/server.ts
import { setupServer } from "msw/node";
import { handlers } from "./handlers";
export const server = setupServer(...handlers);
```

### 3.3 Tests de composants

```typescript
// features/products/components/__tests__/ProductForm.test.tsx
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClientProvider } from "@tanstack/react-query";
import { ProductForm } from "../ProductForm";
import { createTestQueryClient } from "@/test/utils";

const renderWithProviders = (ui: ReactElement) =>
  render(
    <QueryClientProvider client={createTestQueryClient()}>
      {ui}
    </QueryClientProvider>
  );

describe("ProductForm", () => {
  it("renders all required fields", () => {
    renderWithProviders(<ProductForm onSuccess={vi.fn()} />);
    expect(screen.getByLabelText(/nom du produit/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/prix/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /créer/i })).toBeInTheDocument();
  });

  it("shows validation errors on submit with empty fields", async () => {
    const user = userEvent.setup();
    renderWithProviders(<ProductForm onSuccess={vi.fn()} />);

    await user.click(screen.getByRole("button", { name: /créer/i }));

    expect(await screen.findByText(/minimum 3 caractères/i)).toBeInTheDocument();
  });

  it("calls onSuccess after successful creation", async () => {
    const user = userEvent.setup();
    const onSuccess = vi.fn();
    renderWithProviders(<ProductForm onSuccess={onSuccess} />);

    await user.type(screen.getByLabelText(/nom/i), "Nouveau produit");
    await user.type(screen.getByLabelText(/prix/i), "49.99");
    await user.click(screen.getByRole("button", { name: /créer/i }));

    await waitFor(() => expect(onSuccess).toHaveBeenCalledOnce());
  });
});
```

### 3.4 Tests de hooks custom

```typescript
// features/products/hooks/__tests__/useProducts.test.ts
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { useProducts } from "../useProducts";
import { server } from "@/test/mocks/server";
import { http, HttpResponse } from "msw";

const wrapper = ({ children }: { children: ReactNode }) => (
  <QueryClientProvider client={createTestQueryClient()}>{children}</QueryClientProvider>
);

describe("useProducts", () => {
  it("fetches and returns products", async () => {
    const { result } = renderHook(() => useProducts(), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data?.data).toHaveLength(3); // MSW retourne 3 products par défaut
  });

  it("handles API errors gracefully", async () => {
    server.use(
      http.get("/api/v1/products/", () => HttpResponse.json({}, { status: 500 }))
    );
    const { result } = renderHook(() => useProducts(), { wrapper });
    await waitFor(() => expect(result.current.isError).toBe(true));
  });
});
```

---

## 4. Tests E2E — Playwright

```typescript
// playwright.config.ts
import { defineConfig } from "@playwright/test";

export default defineConfig({
  testDir: "./e2e",
  fullyParallel: true,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: [["html", { open: "never" }]],
  use: {
    baseURL: "http://localhost:5173",
    trace: "on-first-retry",
    screenshot: "only-on-failure",
  },
  projects: [
    { name: "chromium", use: { ...devices["Desktop Chrome"] } },
    { name: "Mobile Chrome", use: { ...devices["Pixel 5"] } },
  ],
});

// e2e/auth.spec.ts — Tester le flux critique d'authentification
import { test, expect } from "@playwright/test";

test.describe("Authentication", () => {
  test("user can login with valid credentials", async ({ page }) => {
    await page.goto("/login");

    await page.getByLabel("Email").fill("user@test.com");
    await page.getByLabel("Mot de passe").fill("testpass123!");
    await page.getByRole("button", { name: "Se connecter" }).click();

    await expect(page).toHaveURL("/dashboard");
    await expect(page.getByText("Tableau de bord")).toBeVisible();
  });

  test("shows error with wrong credentials", async ({ page }) => {
    await page.goto("/login");
    await page.getByLabel("Email").fill("wrong@test.com");
    await page.getByLabel("Mot de passe").fill("wrongpassword");
    await page.getByRole("button", { name: "Se connecter" }).click();

    await expect(page.getByRole("alert")).toContainText("Identifiants incorrects");
  });
});
```

---

## 5. Couverture de code et CI

```yaml
# .github/workflows/tests.yml
name: Tests

on: [push, pull_request]

jobs:
  backend:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16
        env: { POSTGRES_DB: test_db, POSTGRES_PASSWORD: postgres }
        options: --health-cmd pg_isready

    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-python@v5
        with: { python-version: "3.12" }
      - run: pip install -r requirements/dev.txt
      - run: pytest --cov=apps --cov-report=xml --cov-fail-under=70
      - uses: codecov/codecov-action@v4

  frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: "20" }
      - run: npm ci
      - run: npm run test:coverage -- --coverage.thresholds.lines=70

  e2e:
    runs-on: ubuntu-latest
    needs: [backend, frontend]   # E2E seulement si les autres passent
    steps:
      - uses: actions/checkout@v4
      - run: npm ci && npx playwright install --with-deps chromium
      - run: npm run build && npm run preview &
      - run: npx playwright test
```

---

## Références complémentaires

- `references/fixtures-patterns.md` — Fixtures pytest réutilisables (auth, db state, mock services)
- `references/accessibility-testing.md` — Tests a11y automatisés avec axe-playwright
