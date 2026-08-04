# Anti-patterns backend fréquents — à vérifier en revue

Pour chaque anti-pattern : le symptôme à repérer, pourquoi c'est un problème, et la piste de correction. Structure ta remarque en revue selon ce même triptyque (constat → risque → suggestion).

## Contrôleur/handler obèse

**Symptôme** : le point d'entrée HTTP contient directement la logique métier, les accès base de données, la validation et la construction de la réponse.
**Risque** : logique intestable sans monter tout le framework HTTP, duplication si la même logique doit être appelée depuis un autre contexte (job asynchrone, CLI).
**Piste** : extraire la logique métier dans une couche service/use-case indépendante du framework web, que le controller ne fait qu'appeler.

## Requêtes N+1

**Symptôme** : une boucle qui déclenche une requête base de données par itération au lieu d'une requête groupée.
**Risque** : performance qui se dégrade linéairement (ou pire) avec le volume de données, souvent invisible en dev avec peu de données et catastrophique en prod.
**Piste** : chargement anticipé (eager loading / jointure) ou batching explicite des requêtes.

## Secrets ou configuration en dur

**Symptôme** : clé API, mot de passe, URL d'environnement écrits littéralement dans le code source.
**Risque** : fuite si le code est partagé ou committé publiquement, impossibilité de changer sans redéploiement.
**Piste** : externaliser en variables d'environnement ou gestionnaire de secrets.

## Absence de validation ou validation incomplète

**Symptôme** : les données reçues sont utilisées directement sans vérification de type, format, ou bornes métier.
**Risque** : comportements indéfinis, crashs sur entrée inattendue, surface d'attaque pour injection.
**Piste** : valider explicitement à la frontière du système, rejeter tôt avec un message clair.

## Opérations non idempotentes sur des actions sensibles

**Symptôme** : un endpoint de paiement, d'envoi d'email ou de création de ressource critique n'a aucun mécanisme pour détecter un doublon en cas de retry réseau.
**Risque** : double facturation, emails dupliqués, ressources créées en double lors d'un simple problème réseau côté client.
**Piste** : clé d'idempotence fournie par le client et vérifiée côté serveur avant de ré-exécuter l'action.

## Gestion d'erreur par capture silencieuse

**Symptôme** : `try/catch` (ou équivalent) qui avale l'exception sans la logger ni la propager de façon exploitable.
**Risque** : échecs invisibles en production, données dans un état incohérent sans qu'aucune alerte ne se déclenche.
**Piste** : logger l'erreur avec contexte suffisant pour la diagnostiquer, et décider explicitement si l'appelant doit être informé de l'échec.

## Transactions mal délimitées

**Symptôme** : plusieurs écritures liées (débiter un compte, créditer un autre) exécutées sans transaction englobante, ou une transaction qui englobe des appels réseau externes lents.
**Risque** : incohérence de données si une écriture échoue après une autre (premier cas), ou verrous tenus trop longtemps qui bloquent la concurrence (second cas).
**Piste** : englober exactement les écritures qui doivent être atomiques, jamais plus, jamais moins ; sortir tout appel externe (réseau, email) de la transaction.

## Couplage direct entre modules via la base de données

**Symptôme** : un module lit ou écrit directement dans les tables d'un autre module au lieu de passer par son interface.
**Risque** : impossible de faire évoluer le schéma d'un module sans casser silencieusement un autre, dette qui rend une future extraction en service séparé très coûteuse.
**Piste** : exposer une interface explicite (fonction, événement) entre modules, jamais un accès table à table.

## Pagination absente sur une collection qui va grossir

**Symptôme** : un endpoint qui renvoie "tous les éléments" sans limite, fonctionnel en dev avec 10 lignes.
**Risque** : dégradation puis timeout en production quand le volume augmente, potentiel déni de service involontaire.
**Piste** : pagination systématique dès la conception sur toute collection dont la taille n'est pas bornée par nature.

## Logique métier dupliquée entre validation et traitement

**Symptôme** : une règle métier (ex: "un stock ne peut pas être négatif") vérifiée dans la couche de validation d'entrée ET réimplémentée différemment dans la couche de traitement.
**Risque** : les deux implémentations divergent avec le temps, créant des incohérences difficiles à diagnostiquer.
**Piste** : centraliser la règle métier à un seul endroit (le domaine), la validation d'entrée ne fait qu'appeler cette même règle.
