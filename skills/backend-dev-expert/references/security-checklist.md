# Checklist sécurité backend

Cette liste couvre les points à vérifier systématiquement lors d'une conception ou d'une revue. Elle ne remplace pas un audit de sécurité formel sur un système critique, mais couvre les erreurs les plus fréquentes et les plus coûteuses.

## Validation des entrées

- Valider à la frontière du système (dès la réception de la requête), pas seulement au moment de l'usage — une validation tardive laisse des chemins de code qui traitent des données non vérifiées.
- Valider le type, le format ET les bornes métier (une quantité négative peut être un entier syntaxiquement valide mais métier-invalide).
- Ne jamais construire de requête (SQL, shell, LDAP...) par concaténation de chaînes avec une entrée utilisateur — utiliser systématiquement des requêtes paramétrées ou l'ORM. Ce point reste la cause numéro un d'injection malgré des décennies de sensibilisation.

## Authentification

- Stocker les mots de passe avec un algorithme de hachage lent et salé conçu pour ça (bcrypt, argon2, scrypt) — jamais un hash rapide (MD5, SHA-256 seul) même avec sel.
- Sur les tokens (JWT ou équivalent) : vérifier la signature ET l'expiration à chaque requête, ne jamais faire confiance au contenu du token sans validation cryptographique.
- Prévoir un mécanisme de révocation pour les sessions/tokens (déconnexion forcée, compromission) — un JWT purement stateless sans aucune liste de révocation rend impossible la déconnexion immédiate d'un utilisateur compromis.

## Autorisation

- Appliquer le principe du moindre privilège par défaut (deny by default), pas l'inverse.
- Revérifier l'autorisation à chaque opération sensible, même si l'utilisateur a été authentifié plus tôt dans la requête — ne pas supposer qu'un contrôle en amont (middleware générique) couvre les cas fins (ownership d'une ressource précise).
- Se méfier des identifiants séquentiels ou devinables exposés dans les URLs sans contrôle d'accès associé (IDOR).

## Gestion des secrets

- Jamais de secret (clé API, mot de passe DB, clé de signature) en dur dans le code ou committé dans le contrôle de version — utiliser un gestionnaire de secrets ou des variables d'environnement injectées à l'exécution.
- Faire une rotation possible des secrets sans redéploiement complet du service si l'architecture le permet.
- Distinguer les secrets par environnement (jamais le même secret en dev/staging/prod) — une fuite en environnement de test ne doit pas compromettre la prod.

## Rate limiting et protection contre l'abus

- Limiter le débit sur les endpoints sensibles (login, réinitialisation de mot de passe, envoi d'email) — sans ça, un endpoint de connexion est une invitation au brute-force.
- Prévoir une réponse cohérente en cas de dépassement (429 avec un header indiquant le délai avant réessai) plutôt qu'une erreur générique.

## Données sensibles

- Chiffrer les données sensibles au repos (PII, données de santé, données financières) selon la sensibilité réelle du champ, pas la base entière par réflexe si ce n'est pas nécessaire.
- Chiffrer en transit systématiquement (TLS) — sans exception, y compris en communication interne entre services si le réseau n'est pas une frontière de confiance forte.
- Ne jamais logger de données sensibles en clair (mots de passe, tokens, numéros de carte) — un log est souvent moins protégé que la base de données elle-même.

## Dépendances

- Garder un œil sur les vulnérabilités connues des dépendances (audit régulier des paquets) — une bonne architecture peut être compromise par une bibliothèque tierce vulnérable.

## Ce qui n'est PAS de la sécurité mais qu'on confond souvent avec

- Un ID d'utilisateur difficile à deviner (UUID) n'est pas un contrôle d'accès — c'est de l'obscurité, pas de la sécurité. Le contrôle d'accès explicite reste nécessaire même avec des identifiants non séquentiels.
- HTTPS protège le transport, pas les données une fois stockées ou une fois affichées à un utilisateur non autorisé qui a par ailleurs un accès légitime au système.
