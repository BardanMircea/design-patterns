# Design-patterns-app

Application Spring Boot 3 (Java 21) avec JWT, rôles (ROLE_USER / ROLE_ADMIN), MySQL, architecture Controller → Service → Repository, validation des entrées, et protections anti-brute force + rate limiting/throttling.

Authentification JWT : JwtTokenStrategy, JwtService, JwtAuthenticationFilter émettent et valident les jetons.

## Fonctionnalités implémentées:
- Autorisation par rôles : règles de chemins dans SecurityConfig + rôles (ROLE_USER, ROLE_ADMIN).

- MySQL : application.yml, entités/répositories JPA pointent vers MySQL.

- C-S-R : contrôleurs légers ; toute la logique dans AuthService, UserServiceImpl.

- Endpoints sécurisés : chaîne de filtres Spring Security sans état avec filtre JWT.

- Validation : annotations sur les DTO + GlobalExceptionHandler → réponses 400 propres.

- Brute force & DDoS : LoginAttemptService (verrouillages) + RateLimitFilter (limitation par IP) + journalisation dans les décorateurs/filtres.

- Patrons de conception :
  1. Singleton : les beans Spring sont des singletons (p. ex. JwtService, UserService).

  2. Decorator : LoggingUserServiceDecorator enveloppe/décore UserService.

  3. Strategy : TokenGenerationStrategy avec l’implémentation JwtTokenStrategy.

### Sommaire

1) Créer la base de données MySQL

2) Configuration de l’application

3) Importer, builder et lancer dans IntelliJ

4) Routes d’API & réponses

5) Sécurité : JWT, Rate Limiting, Throttling, Brute Force

6) Patterns de conception implémentés

7) Architecture & responsabilités

8) Tests rapides (cURL)

9) Notes


###  Pré-requis :

Java 17+  | 
Maven 3.9+ (ou le wrapper ./mvnw) | 
MySQL 8+  | 
IntelliJ IDEA (Community ou Ultimate)


## 1) Créer la base de données MySQL

Connectez-vous à MySQL et exécutez :

`
create database design_patterns;
use design_patterns;
create user 'design_patterns_user'@'localhost' identified by 'design_patterns_password';
GRANT ALL PRIVILEGES ON design_patterns.* TO 'design_patterns_user'@'localhost';
FLUSH PRIVILEGES;
`


## 2) Configuration de l’application

Trouvé dans le fichier src/main/resources/application.properties.
L'appli s'execute sur le port: 8080 par default.


## 3) Importer, builder et lancer dans IntelliJ

Ouvrir le projet

IntelliJ → New Project from Existing Sources… → sélectionnez le pom.xml.

Choisissez le SDK 21.

Synchroniser Maven

Laissez IntelliJ télécharger les dépendances.

Configurer la base

Assurez-vous que MySQL tourne et que la base 'design_patterns' existe.

Vérifiez/éditez `application.properties`, et le fichier `.env`  fourni (URL, user, mot de passe, secret JWT).

Lancer l’application

Ouvrez la classe SecureAppApplication et cliquez sur Run ▶.

L’appli démarre sur http://localhost:8080.

(Optionnel) Lancer en ligne de commande

`mvn spring-boot:run`


## 4) Routes d’API & réponses

Les endpoints exposent des statuts cohérents et des messages clairs. L’authentification se fait via JWT passé dans l’en-tête :
(Authorization: Bearer < token > )

#### Authentification / Enregistrement

###### POST /api/auth/register

Body (JSON) :

{ "email": "user@example.com", "password": "Password123!" }


200 OK : utilisateur créé (retourne UserDTO { id, email })

400 Bad Request :

erreurs de validation (email invalide, password trop court)

email déjà enregistré

429 Too Many Requests : si la limite par IP est atteinte (rate limiting)

###### POST /api/auth/login

Body (JSON) :

{ "email": "user@example.com", "password": "Password123!" }


200 OK : { "token": "< JWT >" }

400 Bad Request : erreurs de validation d’entrée

401 Unauthorized : identifiants invalides

429 Too Many Requests :

Rate limiting sur /api/auth/** (bucket plus strict)

Brute force lockout après 5 échecs (verrouillage 15 min)

Utilisateur

###### GET /api/users/me (rôles : USER ou ADMIN)

Headers : Authorization: Bearer <token>

200 OK : UserDTO { id, email }

401 Unauthorized : token manquant/expiré/invalide

429 Too Many Requests : si la limite par IP est atteinte

Admin

###### GET /api/admin/panel (rôle : ADMIN uniquement)

200 OK : message de bienvenue admin

401 Unauthorized : token manquant/expiré/invalide

403 Forbidden : authentifié mais sans rôle ADMIN

429 Too Many Requests : si la limite par IP est atteinte

###### Gestion globale des erreurs

400 : validations (@Valid) ou erreurs métier (e.g., email déjà pris)

401 : credentials invalides (login) / token JWT invalide

403 : accès refusé (rôle insuffisant)

429 : dépassement de la limite de débit / verrouillage anti-brute force


## 5) Sécurité : JWT, Rate Limiting, Throttling, Brute Force

#### JWT

Génération/validation via JwtService + stratégie JwtTokenStrategy

Injection dans le contexte sécurité par JwtAuthenticationFilter (avant UsernamePasswordAuthenticationFilter)

#### Rate limiting & Throttling (anti-DDoS léger)

Filtre global RateLimitFilter (ordre le plus haut), basé sur un token bucket par IP :

/api/auth/** : plus strict (ex. burst 10, ~5 req/s)

autres endpoints : plus permissif (ex. burst 100, ~50 req/s)

Réponse 429 Too Many Requests lorsqu’on dépasse la limite.

#### Anti-brute force (login)

LoginAttemptService : verrouillage de l’email+IP pendant 15 minutes après 5 échecs.

Pendant le verrouillage : 429 avec message explicite.

Succès de login ⇒ remise à zéro du compteur.


## 6) Patterns de conception implémentés

### Strategy

Interface : security.TokenGenerationStrategy

Implémentation : security.JwtTokenStrategy

Usage : la génération/validation du JWT est interchangeable par stratégie (ex. autre algo, autre fournisseur).

### Decorator

Décorateur : service.LoggingUserServiceDecorator

Composant décoré : service.UserServiceImpl (qualifié baseUserService)

Usage : ajoute du logging sans modifier la logique métier de base (enveloppe transparente).

### Singleton (via Spring par défaut)

Les beans Spring (e.g. JwtService, SecurityConfig, RateLimitFilter, LoginAttemptService, UserService…) sont singleton par défaut, ce qui illustre le pattern Singleton sans code additionnel.

### Builder

Présent via Lombok @Builder sur plusieurs classes (e.g. domain.User, dto.AuthResponse, dto.UserDTO, dto.RegisterRequest), montrant le pattern Builder directement dans le code.

## 7) Architecture & responsabilités

#### Controller : endpoints minces, validation @Valid, délègue tout au Service.

web.controller.AuthController, web.controller.UserController, web.controller.AdminController

#### Service : logique métier exclusivement ici.

service.AuthService, service.UserService (+ UserServiceImpl), service.LoggingUserServiceDecorator

#### Repository : accès base de données.

repo.UserRepository

#### Security : configuration et filtres.

config.SecurityConfig, security.JwtAuthenticationFilter, security.JwtService,
security.TokenGenerationStrategy / security.JwtTokenStrategy,
security.RateLimitFilter, security.LoginAttemptService

#### Validation & erreurs :

DTO @Valid + web.GlobalExceptionHandler

#### Données :

model.User, model.Role, seed admin via config.DataSeeder

## 8) Tests rapides 
### L'appli n'a pas d'interface visuélle, donc à tester via POSTMAN ou cURL:

### Enregistrement

`
curl -X POST http://localhost:8080/api/auth/register \
-H Content-Type: application/json" \
-d '{"email":"user1@example.com","password":"Password123!"}'
`

### Login

`TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
-H "Content-Type: application/json" \
-d '{"email":"user1@example.com","password":"Password123!"}' | jq -r .token)
echo $TOKEN
`

### Profil

`curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/users/me
`

### Admin 
(avec admin seedé admin@email.com / AdminPassword1!)

`ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
-H "Content-Type: application/json" \
-d '{"email":"admin@email.com","password":"AdminPassword1!"}' | jq -r .token)
curl -H "Authorization: Bearer $ADMIN_TOKEN" http://localhost:8080/api/admin/panel
`

## 9) Notes

Les identifiants de connexion à la bdd et les secrets JWT se trouvent dans le fichier .env.

!!  Ce fichier n'est pas publié sur GitHub !

!! N'oubliez pas de mettre le fichier .env à la racine du projet avant l'execution !
