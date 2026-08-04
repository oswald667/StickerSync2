---
name: data-viz-dashboard
description: >
  Création de dashboards et visualisations de données dans React. Utiliser ce skill pour :
  choisir la bonne bibliothèque de charts (Recharts vs Chart.js vs D3), concevoir des
  dashboards de KPIs, créer des graphiques interactifs (ligne, barre, camembert, aires),
  agréger des données côté Django avec des annotations PostgreSQL, construire des tableaux
  de données triables/filtrables/exportables, implémenter des filtres de période (date range),
  et optimiser les performances des visualisations sur grands volumes.
  Utiliser ce skill dès qu'une feature implique des graphiques, tableaux de bord, ou rapports.
---

# Data Viz Dashboard

Dashboards et visualisations de données production-grade avec React et Django.

---

## 1. Choisir la bonne bibliothèque

| Bibliothèque | Forces | Faiblesses | Quand utiliser |
|---|---|---|---|
| **Recharts** | Simple, React natif, composable, déclaratif | Moins flexible pour du custom avancé | 80% des cas — dashboards standards |
| **Chart.js** | Performant, plugin riche, animations fluides | API impérative, pas React natif | Graphiques simples, contraintes de performance |
| **D3.js** | Contrôle total, n'importe quelle visualisation | Courbe d'apprentissage très élevée | Visualisations custom impossibles avec Recharts |
| **Plotly** | Graphiques scientifiques, 3D, interactifs | Bundle lourd (1MB+) | Data science, graphiques techniques |
| **Tremor** | UI complète, composants prêts à l'emploi | Moins flexible, opinions fortes | Dashboards admin rapides avec peu de custom |

**Recommandation** : Recharts pour les dashboards standards. D3 uniquement si Recharts ne peut pas couvrir le besoin.

---

## 2. Agrégations côté Django — Calculer en SQL, pas en Python

### Principe fondamental

```
Ne jamais envoyer des données brutes au front pour qu'il calcule.
Le front reçoit des données déjà agrégées, prêtes à afficher.
Django fait les GROUP BY, COUNT, SUM, AVG en SQL — c'est son rôle.
```

### Vue dashboard complète

```python
# apps/analytics/views.py
from django.db.models import Count, Sum, Avg, F, Value, FloatField
from django.db.models.functions import (
    TruncDay, Coalesce, ExtractHour, ExtractWeekDay
)
from django.utils import timezone
from datetime import timedelta
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated, IsAdminUser

class SalesDashboardView(APIView):
    permission_classes = [IsAuthenticated, IsAdminUser]

    def get(self, request):
        period = request.query_params.get('period', '30d')
        start_date = self._parse_period(period)

        return Response({
            'success': True,
            'data': {
                'kpis': self._get_kpis(start_date),
                'sales_over_time': self._get_sales_trend(start_date),
                'top_products': self._get_top_products(start_date),
                'sales_by_category': self._get_by_category(start_date),
                'hourly_heatmap': self._get_hourly_distribution(start_date),
            }
        })

    def _parse_period(self, period: str):
        mapping = {
            '7d': timedelta(days=7),
            '30d': timedelta(days=30),
            '90d': timedelta(days=90),
            '1y': timedelta(days=365),
        }
        return timezone.now() - mapping.get(period, timedelta(days=30))

    def _get_kpis(self, start_date):
        """KPIs globaux sur la période."""
        qs = Order.objects.filter(created_at__gte=start_date, status='completed')
        return qs.aggregate(
            total_revenue=Coalesce(
                Sum('total_amount'), Value(0), output_field=FloatField()
            ),
            order_count=Count('id'),
            avg_order_value=Coalesce(
                Avg('total_amount'), Value(0), output_field=FloatField()
            ),
            unique_customers=Count('customer', distinct=True),
        )

    def _get_kpis_with_trend(self, start_date):
        """KPIs avec variation % vs période précédente."""
        delta = timezone.now() - start_date
        prev_start = start_date - delta
        prev_end = start_date

        current = Order.objects.filter(
            created_at__gte=start_date, status='completed'
        ).aggregate(
            revenue=Coalesce(Sum('total_amount'), Value(0), output_field=FloatField()),
            count=Count('id'),
        )
        previous = Order.objects.filter(
            created_at__range=(prev_start, prev_end), status='completed'
        ).aggregate(
            revenue=Coalesce(Sum('total_amount'), Value(0), output_field=FloatField()),
            count=Count('id'),
        )

        def pct(cur, prev):
            if prev == 0:
                return None
            return round((cur - prev) / prev * 100, 1)

        return {
            'revenue': current['revenue'],
            'revenue_trend': pct(current['revenue'], previous['revenue']),
            'order_count': current['count'],
            'order_count_trend': pct(current['count'], previous['count']),
        }

    def _get_sales_trend(self, start_date):
        """Ventes agrégées par jour."""
        return list(
            Order.objects.filter(created_at__gte=start_date, status='completed')
            .annotate(date=TruncDay('created_at'))
            .values('date')
            .annotate(
                revenue=Coalesce(
                    Sum('total_amount'), Value(0), output_field=FloatField()
                ),
                count=Count('id'),
            )
            .order_by('date')
            .values('date', 'revenue', 'count')
        )

    def _get_top_products(self, start_date, limit=10):
        """Top produits par revenus générés."""
        return list(
            OrderItem.objects.filter(
                order__created_at__gte=start_date,
                order__status='completed'
            )
            .values('product__id', 'product__name')
            .annotate(
                revenue=Sum(
                    F('quantity') * F('unit_price'),
                    output_field=FloatField()
                ),
                units_sold=Sum('quantity'),
            )
            .order_by('-revenue')[:limit]
        )

    def _get_by_category(self, start_date):
        """Répartition des ventes par catégorie."""
        return list(
            OrderItem.objects.filter(
                order__created_at__gte=start_date,
                order__status='completed'
            )
            .values('product__category__name')
            .annotate(
                revenue=Sum(
                    F('quantity') * F('unit_price'),
                    output_field=FloatField()
                ),
                count=Count('id'),
            )
            .order_by('-revenue')
        )

    def _get_hourly_distribution(self, start_date):
        """Distribution par heure × jour de semaine — pour heatmap."""
        return list(
            Order.objects.filter(created_at__gte=start_date, status='completed')
            .annotate(
                hour=ExtractHour('created_at'),
                weekday=ExtractWeekDay('created_at'),
            )
            .values('hour', 'weekday')
            .annotate(count=Count('id'))
            .order_by('weekday', 'hour')
        )
```

---

## 3. React Query — Hook dashboard

```typescript
// features/analytics/hooks/useDashboard.ts
import { useQuery } from '@tanstack/react-query'
import { analyticsApi } from '../api/analyticsApi'

type Period = '7d' | '30d' | '90d' | '1y'

export function useDashboard(period: Period = '30d') {
  return useQuery({
    queryKey: ['dashboard', period],
    queryFn: () => analyticsApi.getDashboard(period),
    staleTime: 5 * 60 * 1000,
    refetchInterval: 10 * 60 * 1000,
    placeholderData: (prev) => prev,  // Garde les données pendant changement de période
  })
}
```

---

## 4. Composants Recharts

### KPI Card avec tendance

```tsx
// features/analytics/components/KPICard.tsx
import { useMemo } from 'react'

interface KPICardProps {
  label: string
  value: number
  trend?: number | null
  format?: 'currency' | 'number' | 'percent'
  isLoading?: boolean
}

export function KPICard({ label, value, trend, format = 'number', isLoading }: KPICardProps) {
  const formatted = useMemo(() => {
    if (isLoading) return '—'
    switch (format) {
      case 'currency': return `${value.toLocaleString('fr-FR')} FCFA`
      case 'percent':  return `${value.toFixed(1)} %`
      default:         return value.toLocaleString('fr-FR')
    }
  }, [value, format, isLoading])

  return (
    <article className="kpi-card" aria-busy={isLoading}>
      <span className="kpi-label">{label}</span>
      <div className="kpi-value" aria-label={`${label} : ${formatted}`}>
        {isLoading ? <span className="skeleton" /> : formatted}
      </div>
      {trend != null && (
        <div
          className={`kpi-trend ${trend >= 0 ? 'positive' : 'negative'}`}
          aria-label={`Variation : ${trend >= 0 ? '+' : ''}${trend.toFixed(1)} %`}
        >
          <span aria-hidden="true">{trend >= 0 ? '↑' : '↓'}</span>
          {' '}{Math.abs(trend).toFixed(1)} % vs période précédente
        </div>
      )}
    </article>
  )
}
```

### Graphique de tendance (LineChart)

```tsx
// features/analytics/components/SalesTrend.tsx
import {
  ResponsiveContainer, LineChart, Line,
  XAxis, YAxis, CartesianGrid, Tooltip, Legend, ReferenceLine
} from 'recharts'
import { format, parseISO } from 'date-fns'
import { fr } from 'date-fns/locale'

const CustomTooltip = ({ active, payload, label }: any) => {
  if (!active || !payload?.length) return null
  return (
    <div className="chart-tooltip" role="status">
      <p>{format(parseISO(label), 'EEEE dd MMMM yyyy', { locale: fr })}</p>
      <p>{payload[0]?.value?.toLocaleString('fr-FR')} FCFA</p>
      <p>{payload[1]?.value} commande{payload[1]?.value !== 1 ? 's' : ''}</p>
    </div>
  )
}

export function SalesTrend({ data }: { data: any[] }) {
  const avg = data.length > 0
    ? data.reduce((sum, d) => sum + d.revenue, 0) / data.length
    : 0

  return (
    <section className="chart-container">
      <h3>Évolution des ventes</h3>
      <div style={{ height: 300 }}>
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={data} margin={{ top: 5, right: 20, left: 10, bottom: 5 }}>
            <CartesianGrid
              strokeDasharray="3 3"
              stroke="var(--color-border-tertiary)"
              vertical={false}
            />
            <XAxis
              dataKey="date"
              tickFormatter={(v) => format(parseISO(v), 'dd/MM')}
              tick={{ fontSize: 12 }}
              tickLine={false}
            />
            <YAxis
              yAxisId="left"
              tickFormatter={(v) => `${(v / 1000).toFixed(0)}k`}
              tick={{ fontSize: 12 }}
              tickLine={false}
              axisLine={false}
            />
            <YAxis
              yAxisId="right"
              orientation="right"
              tick={{ fontSize: 12 }}
              tickLine={false}
              axisLine={false}
            />
            <Tooltip content={<CustomTooltip />} />
            <Legend />
            <ReferenceLine
              yAxisId="left"
              y={avg}
              stroke="var(--color-text-tertiary)"
              strokeDasharray="5 5"
              label={{ value: 'Moy.', fontSize: 11 }}
            />
            <Line
              yAxisId="left"
              type="monotone"
              dataKey="revenue"
              name="Revenus (FCFA)"
              stroke="var(--color-primary)"
              strokeWidth={2}
              dot={false}
              activeDot={{ r: 4, strokeWidth: 0 }}
            />
            <Line
              yAxisId="right"
              type="monotone"
              dataKey="count"
              name="Commandes"
              stroke="#10B981"
              strokeWidth={2}
              dot={false}
              activeDot={{ r: 4, strokeWidth: 0 }}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </section>
  )
}
```

### Barres horizontales — Top produits

```tsx
// features/analytics/components/TopProducts.tsx
import {
  ResponsiveContainer, BarChart, Bar,
  XAxis, YAxis, Tooltip, Cell
} from 'recharts'

export function TopProducts({ data }: { data: any[] }) {
  return (
    <section className="chart-container">
      <h3>Top 10 produits</h3>
      <div style={{ height: 320 }}>
        <ResponsiveContainer width="100%" height="100%">
          <BarChart
            data={data}
            layout="vertical"
            margin={{ top: 0, right: 20, left: 120, bottom: 0 }}
          >
            <XAxis
              type="number"
              tickFormatter={(v) => `${(v / 1000).toFixed(0)}k`}
              tick={{ fontSize: 11 }}
              tickLine={false}
              axisLine={false}
            />
            <YAxis
              type="category"
              dataKey="product__name"
              tick={{ fontSize: 12 }}
              tickLine={false}
              axisLine={false}
              width={115}
              tickFormatter={(v) => v.length > 18 ? v.substring(0, 18) + '…' : v}
            />
            <Tooltip
              formatter={(v: number) => [
                `${v.toLocaleString('fr-FR')} FCFA`, 'Revenus'
              ]}
              cursor={{ fill: 'var(--color-background-secondary)' }}
            />
            <Bar dataKey="revenue" radius={[0, 4, 4, 0]}>
              {data.map((_, i) => (
                <Cell key={i} fill={`hsl(221, 83%, ${65 - i * 4}%)`} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>
    </section>
  )
}
```

### Camembert — Répartition par catégorie

```tsx
// features/analytics/components/SalesByCategory.tsx
import {
  ResponsiveContainer, PieChart, Pie,
  Cell, Tooltip, Legend
} from 'recharts'

const COLORS = [
  '#3B82F6', '#10B981', '#F59E0B', '#EF4444',
  '#8B5CF6', '#06B6D4', '#84CC16', '#F97316',
]
const RADIAN = Math.PI / 180

const renderLabel = ({ cx, cy, midAngle, innerRadius, outerRadius, percent }: any) => {
  if (percent < 0.05) return null
  const r = innerRadius + (outerRadius - innerRadius) * 0.5
  const x = cx + r * Math.cos(-midAngle * RADIAN)
  const y = cy + r * Math.sin(-midAngle * RADIAN)
  return (
    <text x={x} y={y} fill="white" textAnchor="middle"
      dominantBaseline="central" fontSize={12} fontWeight={500}>
      {`${(percent * 100).toFixed(0)}%`}
    </text>
  )
}

export function SalesByCategory({ data }: { data: any[] }) {
  return (
    <section className="chart-container">
      <h3>Ventes par catégorie</h3>
      <div style={{ height: 300 }}>
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={data}
              dataKey="revenue"
              nameKey="product__category__name"
              cx="50%" cy="50%"
              outerRadius={100}
              labelLine={false}
              label={renderLabel}
            >
              {data.map((_, i) => (
                <Cell key={i} fill={COLORS[i % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip
              formatter={(v: number) => [
                `${v.toLocaleString('fr-FR')} FCFA`, 'Revenus'
              ]}
            />
            <Legend
              formatter={(v) => v.length > 22 ? v.substring(0, 22) + '…' : v}
            />
          </PieChart>
        </ResponsiveContainer>
      </div>
    </section>
  )
}
```

---

## 5. Tableau de données — Triable, filtrable, exportable

```tsx
// shared/components/DataTable.tsx
import { useState, useMemo, useCallback } from 'react'

type SortDir = 'asc' | 'desc'

export interface TableColumn<T> {
  key: keyof T
  label: string
  sortable?: boolean
  align?: 'left' | 'right' | 'center'
  format?: (value: T[keyof T], row: T) => string
}

interface DataTableProps<T extends Record<string, unknown>> {
  data: T[]
  columns: TableColumn<T>[]
  pageSize?: number
  onExportCSV?: () => void
  isLoading?: boolean
  emptyMessage?: string
}

export function DataTable<T extends Record<string, unknown>>({
  data, columns, pageSize = 20, onExportCSV,
  isLoading, emptyMessage = 'Aucune donnée disponible',
}: DataTableProps<T>) {
  const [sortKey, setSortKey] = useState<keyof T | null>(null)
  const [sortDir, setSortDir] = useState<SortDir>('desc')
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(1)

  const filtered = useMemo(() => {
    if (!search.trim()) return data
    const q = search.toLowerCase()
    return data.filter(row =>
      Object.values(row).some(v =>
        String(v ?? '').toLowerCase().includes(q)
      )
    )
  }, [data, search])

  const sorted = useMemo(() => {
    if (!sortKey) return filtered
    return [...filtered].sort((a, b) => {
      const va = a[sortKey] ?? ''
      const vb = b[sortKey] ?? ''
      const cmp = String(va).localeCompare(String(vb), 'fr', { numeric: true })
      return sortDir === 'asc' ? cmp : -cmp
    })
  }, [filtered, sortKey, sortDir])

  const totalPages = Math.ceil(sorted.length / pageSize)
  const paginated = sorted.slice((page - 1) * pageSize, page * pageSize)

  const handleSort = useCallback((key: keyof T) => {
    setSortDir(prev =>
      sortKey === key ? (prev === 'asc' ? 'desc' : 'asc') : 'desc'
    )
    setSortKey(key)
    setPage(1)
  }, [sortKey])

  return (
    <div className="data-table-wrapper">
      <div className="table-toolbar">
        <input
          id="table-search"
          type="search"
          value={search}
          onChange={e => { setSearch(e.target.value); setPage(1) }}
          placeholder="Rechercher dans le tableau…"
          aria-label="Filtrer les données"
        />
        <span>{sorted.length.toLocaleString('fr-FR')} résultat{sorted.length !== 1 ? 's' : ''}</span>
        {onExportCSV && (
          <button onClick={onExportCSV} className="btn-secondary">
            Exporter CSV
          </button>
        )}
      </div>

      <div className="table-scroll" role="region" aria-label="Données" tabIndex={0}>
        <table aria-rowcount={sorted.length} aria-busy={isLoading}>
          <thead>
            <tr>
              {columns.map(col => (
                <th
                  key={String(col.key)}
                  style={{ textAlign: col.align ?? 'left', cursor: col.sortable ? 'pointer' : 'default' }}
                  aria-sort={
                    sortKey === col.key
                      ? sortDir === 'asc' ? 'ascending' : 'descending'
                      : col.sortable ? 'none' : undefined
                  }
                  onClick={col.sortable ? () => handleSort(col.key) : undefined}
                >
                  {col.label}
                  {col.sortable && (
                    <span aria-hidden="true">
                      {sortKey === col.key
                        ? sortDir === 'asc' ? ' ↑' : ' ↓'
                        : ' ↕'}
                    </span>
                  )}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {isLoading
              ? Array.from({ length: 5 }).map((_, i) => (
                  <tr key={i}>
                    {columns.map(col => (
                      <td key={String(col.key)}>
                        <span className="skeleton" style={{ display: 'block', width: '70%', height: 16 }} />
                      </td>
                    ))}
                  </tr>
                ))
              : paginated.length === 0
              ? (
                <tr>
                  <td colSpan={columns.length} style={{ textAlign: 'center', padding: '2rem' }}>
                    {emptyMessage}
                  </td>
                </tr>
              )
              : paginated.map((row, i) => (
                  <tr key={i}>
                    {columns.map(col => (
                      <td key={String(col.key)} style={{ textAlign: col.align ?? 'left' }}>
                        {col.format
                          ? col.format(row[col.key], row)
                          : String(row[col.key] ?? '—')}
                      </td>
                    ))}
                  </tr>
                ))
            }
          </tbody>
        </table>
      </div>

      {totalPages > 1 && (
        <nav className="pagination" aria-label="Pagination du tableau">
          <button onClick={() => setPage(1)} disabled={page === 1} aria-label="Première page">«</button>
          <button onClick={() => setPage(p => Math.max(1, p - 1))} disabled={page === 1} aria-label="Page précédente">‹</button>
          <span aria-current="page">Page {page} / {totalPages}</span>
          <button onClick={() => setPage(p => Math.min(totalPages, p + 1))} disabled={page === totalPages} aria-label="Page suivante">›</button>
          <button onClick={() => setPage(totalPages)} disabled={page === totalPages} aria-label="Dernière page">»</button>
        </nav>
      )}
    </div>
  )
}
```

---

## 6. Export CSV

```typescript
// shared/utils/exportCsv.ts
interface ExportColumn<T> {
  key: keyof T
  label: string
  format?: (value: T[keyof T]) => string
}

export function exportToCSV<T extends Record<string, unknown>>(
  data: T[],
  columns: ExportColumn<T>[],
  filename: string
): void {
  const headers = columns.map(c => `"${c.label}"`).join(',')
  const rows = data.map(row =>
    columns.map(col => {
      const raw = row[col.key]
      const value = col.format ? col.format(raw) : String(raw ?? '')
      return `"${value.replace(/"/g, '""')}"`
    }).join(',')
  )

  // BOM UTF-8 obligatoire pour que Excel ouvre correctement les accents
  const csv = '\ufeff' + [headers, ...rows].join('\r\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${filename}_${new Date().toISOString().split('T')[0]}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}
```

---

## 7. Performance — Grands volumes (> 10 000 lignes)

```typescript
// Virtualisation avec @tanstack/react-virtual
import { useVirtualizer } from '@tanstack/react-virtual'
import { useRef } from 'react'

function VirtualTable({ rows, columns, rowHeight = 48 }) {
  const parentRef = useRef<HTMLDivElement>(null)

  const virtualizer = useVirtualizer({
    count: rows.length,
    getScrollElement: () => parentRef.current,
    estimateSize: () => rowHeight,
    overscan: 10,
  })

  return (
    <div ref={parentRef} style={{ height: 600, overflow: 'auto' }}>
      <table>
        <thead>...</thead>
        <tbody style={{ height: virtualizer.getTotalSize(), position: 'relative' }}>
          {virtualizer.getVirtualItems().map(vRow => (
            <tr
              key={vRow.index}
              style={{
                position: 'absolute',
                top: 0,
                transform: `translateY(${vRow.start}px)`,
                height: vRow.size,
              }}
            >
              {columns.map(col => (
                <td key={String(col.key)}>{String(rows[vRow.index][col.key] ?? '')}</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
```

---

## 8. Sélecteur de période

```tsx
// features/analytics/components/PeriodSelector.tsx
const PERIODS = [
  { value: '7d',  label: '7 jours' },
  { value: '30d', label: '30 jours' },
  { value: '90d', label: '90 jours' },
  { value: '1y',  label: '1 an' },
] as const

export function PeriodSelector({ value, onChange }: {
  value: string
  onChange: (period: string) => void
}) {
  return (
    <div role="group" aria-label="Sélectionner la période d'analyse">
      {PERIODS.map(p => (
        <button
          key={p.value}
          onClick={() => onChange(p.value)}
          aria-pressed={value === p.value}
          className={`period-btn ${value === p.value ? 'active' : ''}`}
        >
          {p.label}
        </button>
      ))}
    </div>
  )
}
```

---

## 9. Anti-patterns critiques

| Anti-pattern | Problème | Solution |
|---|---|---|
| Calculs d'agrégation en Python/JS | Lent sur 100k lignes | `annotate()` Django + SQL `GROUP BY` |
| Tout le dataset brut envoyé au front | Mémoire saturée, chargement lent | Données pré-agrégées côté API uniquement |
| Graphiques sans `ResponsiveContainer` | Cassé sur mobile ou redimensionnement | Toujours wrapper avec `ResponsiveContainer` |
| Pas de skeleton pendant le chargement | Flash de contenu vide | Skeletons sur chaque KPI et graphique |
| Export CSV sans BOM (`\ufeff`) | Excel corrompt les accents (é, è, à…) | Toujours préfixer avec `\ufeff` |
| Labels de graphique sans tooltip | Valeurs illisibles sur mobile | Tooltip custom systématique |
| Couleurs sans légende | Utilisateur incapable d'interpréter | `<Legend />` obligatoire sur tout graphique |
| Tableau sans pagination sur grands volumes | Render de 10 000 lignes = blocage UI | Pagination ou virtualisation au-delà de 100 lignes |
