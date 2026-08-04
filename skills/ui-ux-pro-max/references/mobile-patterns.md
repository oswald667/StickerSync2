# Mobile UI Patterns — iOS & Android

## Conventions de navigation

### iOS (Human Interface Guidelines)
- **Tab bar** en bas : max 5 items, toujours visible sur les écrans principaux
- **Navigation bar** en haut : titre + actions max à droite (bouton retour auto à gauche)
- **Large title** : collapse lors du scroll (depuis iOS 11)
- **Action sheets** : menu d'actions depuis le bas (jamais de menu contextuel flottant)
- **Safe areas** : toujours respecter les insets (encoche, home indicator)

### Android (Material Design 3)
- **Navigation bar** en bas (3 items) OU **Navigation drawer** latéral
- **Top app bar** : peut être collapsible (Medium/Large top app bar)
- **FAB** (Floating Action Button) : action principale de l'écran, en bas à droite
- **Bottom sheets** : modal ou persistant selon l'usage
- **Edge-to-edge** : contenu sous la navigation system bar (gérer les insets)

## Gestes standards

| Geste | iOS | Android |
|-------|-----|---------|
| Retour | Swipe from left edge | Swipe from left edge (gesture nav) |
| Dismiss modal | Swipe down | Swipe down |
| Pull-to-refresh | Pull down | Pull down |
| Swipe to delete | Swipe left on cell | Swipe left/right (selon app) |
| Long press | Context menu | Context menu / drag |

## Touch targets — Règles absolues

- **iOS** : minimum 44×44 pt (même si l'icône est plus petite visuellement)
- **Android** : minimum 48×48 dp
- **Espacement entre cibles** : minimum 8dp pour éviter les erreurs de tap
- Si la cible visuelle est petite : agrandir la zone de touch invisible

## Typographie mobile

- **Taille minimum lisible** : 11pt/sp (labels), 13pt/sp (corps de texte)
- **Line height recommandé** : 1.4–1.6× la taille de police
- **Longueur de ligne optimale** : 45–75 caractères (éviter les lignes trop longues sur tablette)

## Formulaires mobile — Best practices

- Afficher le **bon clavier** : `email`, `number`, `tel`, `url` selon le type de champ
- **Autofill** : supporter les suggestions du système (nom, email, mot de passe)
- **Champ password** : icône œil pour afficher/masquer, toujours
- **Bouton de soumission** : au-dessus du clavier (pas derrière), ou sticky en bas

## Performances perçues

- **Skeleton screens** > spinners pour les listes et cards
- **Optimistic UI** : appliquer l'action localement avant la réponse serveur (avec rollback si erreur)
- **Prefetch** : charger la page suivante avant que l'utilisateur ne la demande
- **Animations** : 200–300ms pour les transitions d'écran, 150ms pour les micro-interactions
