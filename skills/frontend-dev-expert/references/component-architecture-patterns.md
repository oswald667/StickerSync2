# Patterns d'architecture de composants — indépendants du framework

## Séparation présentation / conteneur

Un composant de présentation reçoit des données et des callbacks en entrée (props) et ne sait rien de leur origine ; un composant conteneur connaît l'état/les données et les transmet. Cette séparation permet de réutiliser un composant de présentation dans un contexte totalement différent (autre page, autre source de données, voire un test avec des données factices) sans rien changer à son code.

Signal qu'elle est violée : un composant censé être un simple bouton ou une simple carte importe directement un client API ou un store global au lieu de recevoir ses données en props.

## Composition plutôt que props conditionnelles multiples

Quand un composant accumule des props qui changent radicalement son comportement (`variant`, `mode`, `type` avec de nombreuses branches conditionnelles internes), c'est souvent le signe que ce sont en réalité plusieurs composants distincts déguisés en un seul par souci (mal placé) de réutilisation. Préférer composer de petits composants spécialisés, éventuellement en partageant les parties réellement communes via un composant de base ou un slot/children, plutôt que de faire porter toute la variation à un seul composant paramétré à l'excès.

Test pratique : si retirer une prop conditionnelle nécessite de comprendre plusieurs autres props pour savoir si le composant reste cohérent, c'est un signal de couplage interne excessif.

## Composants contrôlés vs non contrôlés

Un composant contrôlé reçoit son état depuis l'extérieur (le parent decide de la valeur affichée) ; un composant non contrôlé gère son état en interne et expose seulement des événements. Les composants de formulaire réutilisables gagnent à supporter les deux modes (valeur + callback de changement en props, avec une valeur par défaut interne si non fournie) pour rester utilisables aussi bien dans un formulaire simple que dans un formulaire piloté par une bibliothèque de gestion de formulaire.

## Hiérarchie par intention utilisateur, pas par taille de fichier

Découper un gros composant en plusieurs petits fichiers uniquement parce que le fichier est long, sans que chaque nouveau composant corresponde à une intention utilisateur claire et nommable, déplace le problème sans le résoudre (on se retrouve avec plusieurs petits fichiers fortement couplés entre eux à la place d'un seul gros fichier). Le bon découpage se reconnaît à ce que chaque composant a un nom qui décrit ce qu'un utilisateur reconnaîtrait ("carte produit", "barre de filtres"), pas une description technique ("partie du haut du composant principal").

## Emplacement de l'état — le plus bas possible, remonté seulement si nécessaire

Placer un état au niveau le plus haut de l'arbre de composants "au cas où" crée des re-rendus inutiles pour tous les descendants et rend le composant racine responsable de détails qui ne le concernent pas. Démarrer l'état au plus proche de son usage, et ne le remonter que lorsqu'un deuxième composant, qui n'est pas un descendant du premier, en a réellement besoin.

## Props drilling excessif — vraies solutions vs fausses bonnes solutions

Quand un état doit traverser plusieurs niveaux de composants qui ne l'utilisent pas eux-mêmes, la réaction réflexe "ajouter un store global" n'est pas toujours la meilleure : vérifier d'abord si une restructuration de la composition (utiliser `children`/slots pour insérer directement le composant qui a besoin de la donnée à l'endroit où elle existe, plutôt que de le faire descendre à travers des intermédiaires) résout le problème sans ajouter de dépendance globale. Réserver un mécanisme de partage transverse (contexte, store) aux cas où la donnée est réellement nécessaire à des branches éloignées et non prévisibles à l'avance.

## Frontières d'erreur (error boundaries) et états de chargement traités comme des cas de première classe

Un composant qui n'affiche que le cas "données chargées avec succès" et laisse le cas de chargement ou d'erreur non géré (écran blanc, crash silencieux) traite ces cas comme secondaires alors qu'ils se produisent en pratique aussi souvent que le cas heureux, en particulier sur réseau mobile. Concevoir les trois états (chargement, erreur, succès) dès la conception du composant, pas comme un ajout après coup.
