---
name: react-architecture
description: >
  Architecture avancée d'applications React.js production-grade. Utiliser ce skill pour :
  organiser la structure de dossiers d'une app React, choisir et implémenter un state
  management (Zustand, Redux Toolkit), configurer le data fetching (React Query / SWR),
  concevoir des patterns de composition de composants, implémenter le code splitting
  et le lazy loading, configurer Vite, gérer les formulaires (React Hook Form + Zod),
  structurer une feature-based architecture, optimiser les re-renders. Déclencher sur :
  "architecture React", "state management", "React Query", "Zustand", "Redux",
  "composants React", "performance React", "re-render", "code splitting", "hooks custom",
  "React Hook Form", "structure dossiers React", "feature folder".
---

# React Architecture

Architecture React production-grade — des patterns de composants à l'optimisation des re-renders.

---

## 1. Structure de projet — Feature-based architecture

```
src/
├── app/                         # Configuration globale (providers, router, store)
│   ├── App.tsx
│   ├── router.tsx               # Routes React Router v6
│   └── providers.tsx            # Composition des providers globaux
├── features/                    # ← Cœur de l'architecture
│   ├── auth/
│   │   ├── components/          # Composants UI de la feature
│   │   │   ├── LoginForm.tsx
│   │   │   └── AuthGuard.tsx
│   │   ├── hooks/               # Hooks custom de la feature
│   │   │   └── useAuth.ts
│   │   ├── api/                 # Appels API (React Query mutations/queries)
│   │   │   └── auth.api.ts
│   │   ├── store/               # State local à la feature (Zustand slice)
│   │   │   └── auth.store.ts
│   │   ├── types/               # Types TypeScript de la feature
│   │   │   └── auth.types.ts
│   │   └── index.ts             # Barrel export (API publique de la feature)
│   ├── products/
│   └── orders/
├── shared/                      # Code transversal réutilisable
│   ├── components/              # Composants UI génériques
│   │   ├── ui/                  # Primitives (Button, Input, Modal, Badge)
│   │   └── layout/              # Layout (Navbar, Sidebar, PageWrapper)
│   ├── hooks/                   # Hooks génériques (useDebounce, useLocalStorage)
│   ├── utils/                   # Fonctions pures utilitaires
│   ├── types/                   # Types globaux partagés
│   └── lib/                     # Configurations de librairies (axios, queryClient)
└── pages/                       # Routes top-level (composent les features)
    ├── ProductsPage.tsx
    └── DashboardPage.tsx
```

**Règle d'importation** : une feature peut importer de `shared/`, jamais d'une autre feature directement. Si deux features partagent du code, il monte dans `shared/`.

---

## 2. State Management — Zustand (recommandé)

### 2.1 Store structuré avec slices

```typescript
// features/auth/store/auth.store.ts
import { create } from "zustand";
import { devtools, persist } from "zustand/middleware";
import { immer } from "zustand/middleware/immer";

interface User {
  id: string;
  email: string;
  role: "user" | "admin";
}

interface AuthState {
  user: User | null;
  accessToken: string | null;
  isAuthenticated: boolean;
  // Actions — toujours dans le même store
  setUser: (user: User, token: string) => void;
  logout: () => void;
  updateUser: (partial: Partial<User>) => void;
}

export const useAuthStore = create<AuthState>()(
  devtools(
    persist(
      immer((set) => ({
        user: null,
        accessToken: null,
        isAuthenticated: false,

        setUser: (user, token) =>
          set((state) => {
            state.user = user;
            state.accessToken = token;
            state.isAuthenticated = true;
          }),

        logout: () =>
          set((state) => {
            state.user = null;
            state.accessToken = null;
            state.isAuthenticated = false;
          }),

        updateUser: (partial) =>
          set((state) => {
            if (state.user) Object.assign(state.user, partial);
          }),
      })),
      {
        name: "auth-storage",
        partialize: (state) => ({ accessToken: state.accessToken }), // Persister seulement le token
      }
    ),
    { name: "AuthStore" }
  )
);

// Sélecteurs — éviter les re-renders inutiles
export const useUser = () => useAuthStore((s) => s.user);
export const useIsAdmin = () => useAuthStore((s) => s.user?.role === "admin");
```

### 2.2 Quand utiliser quel state

```
État local (useState) :
  → État UI éphémère : isOpen, activeTab, inputValue
  → Données non partagées avec d'autres composants

État feature (Zustand slice) :
  → Données partagées au sein d'une feature
  → État persistant (token, préférences)
  → État synchronisé entre composants distants

Serveur (React Query) :
  → TOUT ce qui vient d'une API
  → Ne jamais dupliquer les données serveur dans Zustand
```

---

## 3. Data Fetching — React Query

### 3.1 Configuration du QueryClient

```typescript
// shared/lib/queryClient.ts
import { QueryClient } from "@tanstack/react-query";

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000,     // Données fraîches pendant 5 min
      gcTime: 30 * 60 * 1000,        // Garbage collection après 30 min
      retry: (failureCount, error: any) => {
        if (error?.status === 404 || error?.status === 403) return false; // Pas de retry sur 4xx
        return failureCount < 2;
      },
      refetchOnWindowFocus: false,   // Désactiver en prod (agressif)
    },
    mutations: {
      retry: 0,                      // Pas de retry sur les mutations
    },
  },
});
```

### 3.2 Couche API structurée

```typescript
// features/products/api/products.api.ts
import { api } from "@/shared/lib/axios";

export interface Product {
  id: string;
  name: string;
  price: number;
  status: "active" | "draft";
}

export interface ProductFilters {
  status?: string;
  category?: string;
  page?: number;
  page_size?: number;
}

// Query keys — centralisés, évite les typos
export const productKeys = {
  all: ["products"] as const,
  lists: () => [...productKeys.all, "list"] as const,
  list: (filters: ProductFilters) => [...productKeys.lists(), filters] as const,
  details: () => [...productKeys.all, "detail"] as const,
  detail: (id: string) => [...productKeys.details(), id] as const,
};

// API functions — séparées des hooks
export const productApi = {
  getAll: (filters: ProductFilters) =>
    api.get<{ data: Product[]; meta: any }>("/products/", { params: filters }),

  getById: (id: string) =>
    api.get<{ data: Product }>(`/products/${id}/`),

  create: (data: Partial<Product>) =>
    api.post<{ data: Product }>("/products/", data),

  update: (id: string, data: Partial<Product>) =>
    api.patch<{ data: Product }>(`/products/${id}/`, data),

  delete: (id: string) =>
    api.delete(`/products/${id}/`),
};
```

### 3.3 Hooks React Query

```typescript
// features/products/hooks/useProducts.ts
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { productApi, productKeys, ProductFilters } from "../api/products.api";

export function useProducts(filters: ProductFilters = {}) {
  return useQuery({
    queryKey: productKeys.list(filters),
    queryFn: () => productApi.getAll(filters).then((r) => r.data),
    placeholderData: (prev) => prev, // Garde les données pendant le refetch (no flash)
  });
}

export function useProduct(id: string) {
  return useQuery({
    queryKey: productKeys.detail(id),
    queryFn: () => productApi.getById(id).then((r) => r.data.data),
    enabled: !!id, // Ne pas lancer si id est undefined
  });
}

export function useCreateProduct() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: productApi.create,
    onSuccess: (response) => {
      // Invalider la liste pour refetch
      queryClient.invalidateQueries({ queryKey: productKeys.lists() });
      // Pré-remplir le cache détail du nouvel objet
      queryClient.setQueryData(productKeys.detail(response.data.data.id), response.data.data);
    },
    onError: (error: any) => {
      // Erreur gérée globalement via l'intercepteur Axios
    },
  });
}

export function useUpdateProduct(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Product>) => productApi.update(id, data),
    onMutate: async (newData) => {
      // Optimistic update
      await queryClient.cancelQueries({ queryKey: productKeys.detail(id) });
      const previous = queryClient.getQueryData(productKeys.detail(id));
      queryClient.setQueryData(productKeys.detail(id), (old: any) => ({ ...old, ...newData }));
      return { previous };
    },
    onError: (_err, _vars, context) => {
      // Rollback si erreur
      queryClient.setQueryData(productKeys.detail(id), context?.previous);
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: productKeys.detail(id) });
    },
  });
}
```

---

## 4. Formulaires — React Hook Form + Zod

```typescript
// features/products/components/ProductForm.tsx
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

const productSchema = z.object({
  name: z.string().min(3, "Minimum 3 caractères").max(100),
  price: z.number({ invalid_type_error: "Le prix doit être un nombre" })
           .positive("Le prix doit être positif")
           .multipleOf(0.01, "Maximum 2 décimales"),
  description: z.string().min(10).max(1000).optional(),
  status: z.enum(["draft", "active"]),
  category_id: z.string().uuid("Catégorie invalide"),
});

type ProductFormData = z.infer<typeof productSchema>;

export function ProductForm({ onSuccess }: { onSuccess: () => void }) {
  const { mutate: createProduct, isPending } = useCreateProduct();

  const {
    register,
    handleSubmit,
    formState: { errors, isDirty, isValid },
    setError,
    reset,
  } = useForm<ProductFormData>({
    resolver: zodResolver(productSchema),
    defaultValues: { status: "draft" },
    mode: "onBlur", // Validation au blur, pas au keystroke
  });

  const onSubmit = (data: ProductFormData) => {
    createProduct(data, {
      onSuccess: () => {
        reset();
        onSuccess();
      },
      onError: (error: any) => {
        // Mapper les erreurs API sur les champs du formulaire
        const apiErrors = error.response?.data?.errors;
        if (apiErrors) {
          Object.entries(apiErrors).forEach(([field, messages]) => {
            setError(field as keyof ProductFormData, {
              message: (messages as string[])[0],
            });
          });
        }
      },
    });
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input {...register("name")} aria-invalid={!!errors.name} />
      {errors.name && <span role="alert">{errors.name.message}</span>}
      <button type="submit" disabled={isPending || !isDirty || !isValid}>
        {isPending ? "Création..." : "Créer le produit"}
      </button>
    </form>
  );
}
```

---

## 5. Patterns de composants

### 5.1 Composition pattern (éviter le prop drilling)

```typescript
// ❌ Prop drilling profond
<Card title={title} subtitle={subtitle} action={action} icon={icon} loading={loading} />

// ✅ Composition : chaque sous-composant prend ses propres props
<Card>
  <Card.Header>
    <Card.Title>{title}</Card.Title>
    <Card.Action onClick={handleAction}>Éditer</Card.Action>
  </Card.Header>
  <Card.Body>{children}</Card.Body>
  <Card.Footer>{footer}</Card.Footer>
</Card>

// Implémentation
const Card = ({ children }: { children: ReactNode }) => (
  <div className="card">{children}</div>
);
Card.Header = ({ children }: { children: ReactNode }) => <div className="card-header">{children}</div>;
Card.Title = ({ children }: { children: ReactNode }) => <h3 className="card-title">{children}</h3>;
// etc.
```

### 5.2 Render Props / Custom Hooks pour la logique réutilisable

```typescript
// Hook encapsulant une logique complexe
function useTableSelection<T extends { id: string }>(items: T[]) {
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());

  const toggle = (id: string) =>
    setSelectedIds((prev) => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });

  const toggleAll = () =>
    setSelectedIds(
      selectedIds.size === items.length ? new Set() : new Set(items.map((i) => i.id))
    );

  const isSelected = (id: string) => selectedIds.has(id);
  const selectedItems = items.filter((i) => selectedIds.has(i.id));
  const isAllSelected = items.length > 0 && selectedIds.size === items.length;

  return { selectedIds, selectedItems, toggle, toggleAll, isSelected, isAllSelected };
}
```

### 5.3 Éviter les re-renders inutiles

```typescript
// memo : seulement si le composant est coûteux ET ses props changent souvent
const ProductCard = memo(({ product, onEdit }: ProductCardProps) => {
  return <div>{product.name}</div>;
}, (prev, next) => prev.product.id === next.product.id && prev.product.updated_at === next.product.updated_at);

// useCallback : stabiliser les fonctions passées en props
const handleEdit = useCallback((id: string) => {
  navigate(`/products/${id}/edit`);
}, [navigate]); // navigate est stable

// useMemo : calculs coûteux seulement
const sortedProducts = useMemo(
  () => [...products].sort((a, b) => a.price - b.price),
  [products]
);

// ⚠️ Règle : ne pas memo tout par défaut. Profiler d'abord avec React DevTools.
```

---

## 6. Code Splitting et Lazy Loading

```typescript
// app/router.tsx
import { lazy, Suspense } from "react";

// Lazy loading par page (route-based splitting)
const ProductsPage = lazy(() => import("@/pages/ProductsPage"));
const DashboardPage = lazy(() => import("@/pages/DashboardPage"));

// Lazy loading conditionnel (composant lourd chargé seulement si nécessaire)
const HeavyChart = lazy(() => import("@/shared/components/HeavyChart"));

export function AppRouter() {
  return (
    <Suspense fallback={<PageSkeleton />}>
      <Routes>
        <Route path="/products" element={<ProductsPage />} />
        <Route path="/dashboard" element={<DashboardPage />} />
      </Routes>
    </Suspense>
  );
}
```

---

## 7. Configuration Vite

```typescript
// vite.config.ts
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { resolve } from "path";

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@": resolve(__dirname, "./src"), // Import absolu : @/features/auth
    },
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          // Séparer les vendors des chunks applicatifs
          "react-vendor": ["react", "react-dom", "react-router-dom"],
          "query-vendor": ["@tanstack/react-query"],
          "form-vendor": ["react-hook-form", "@hookform/resolvers", "zod"],
        },
      },
    },
    sourcemap: true, // Pour le debugging en prod
  },
  server: {
    proxy: {
      "/api": "http://localhost:8000", // Proxy vers Django en dev
    },
  },
});
```

---

## 8. Axios — Configuration et intercepteurs

```typescript
// shared/lib/axios.ts
import axios from "axios";
import { useAuthStore } from "@/features/auth/store/auth.store";
import { queryClient } from "./queryClient";

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: { "Content-Type": "application/json" },
  timeout: 15000,
});

// Injecter le token JWT automatiquement
api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Gestion globale des erreurs
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().logout();
      queryClient.clear();
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);
```

---

## 9. Checklist d'architecture React

```
Structure :
  [ ] Feature-based : chaque feature est un module autonome
  [ ] Barrel exports (index.ts) pour l'API publique de chaque feature
  [ ] Aucune feature n'importe directement d'une autre feature

State :
  [ ] Données serveur dans React Query uniquement (pas dans Zustand)
  [ ] État UI local dans useState
  [ ] État partagé dans Zustand slices

Performance :
  [ ] React DevTools Profiler utilisé pour identifier les re-renders
  [ ] Code splitting par route (lazy + Suspense)
  [ ] select_related sur les sélecteurs Zustand (sélecteurs granulaires)
  [ ] Aucun useEffect pour dériver des données (useMemo à la place)

Formulaires :
  [ ] React Hook Form sur tous les formulaires
  [ ] Schéma Zod partagé entre front et back si possible
  [ ] Erreurs API mappées sur les champs (setError)
```

---

## Références complémentaires

- `references/testing-patterns.md` — Test des composants, hooks, et stores (voir fullstack-testing skill)
- `references/performance-checklist.md` — Lighthouse, Web Vitals, bundle analysis
