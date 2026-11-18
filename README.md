# DiaGo — Location de voitures à Madagascar (version web Java)

Projet « Technologie Java » : application web Spring Boot, adaptée de la maquette Figma mobile.

**Slogan :** « Safidio ny lalanao, DiaGo no mitondra anao » (Choisissez votre route, DiaGo vous accompagne.)

## Technologies
- Java 17 ou plus, Maven
- Spring Boot 3.5 : Spring MVC, Spring Data JPA, Spring Security (BCrypt), Validation
- Thymeleaf (pages HTML côté serveur), CSS/JS natifs
- H2 (base de données fichier, aucune installation)

## Lancer dans IntelliJ IDEA
1. **File → Open** → choisir le dossier `diago-location` (celui qui contient `pom.xml`) → *Open as Project*.
2. Attendre l'import Maven (barre de progression en bas).
3. **File → Project Structure → Project → SDK** : choisir un JDK 17 ou plus.
4. Ouvrir `DiagoApplication.java` et cliquer sur le triangle vert ▶ à côté de `main`.
5. Aller sur http://localhost:8080

Compte client de démonstration : **demo@diago.mg** / **demo1234** (ou créer un compte).
Compte administrateur : **admin@diago.mg** / **admin1234** → lien « ⚙ Admin » dans le menu, ou http://localhost:8080/admin

## Si Maven ne télécharge pas les dépendances
- *Settings → Build Tools → Maven* : décocher **Work offline**.
- Fermer IntelliJ, supprimer `C:\Users\<vous>\.m2\repository\org\apache\maven\plugins`, rouvrir, puis *Maven → Reload All Maven Projects*.

## Les 15 écrans de la maquette → pages web
| Écran Figma | URL | Contrôleur |
|---|---|---|
| 1 Splash | `/` | AuthController |
| 2 Connexion | `/login` | Spring Security |
| 3 Inscription | `/register` | AuthController |
| 4 Accueil | `/home` | HomeController |
| 5 Recherche | `/search` | CarController |
| 6 Résultats + filtres | `/cars` | CarController |
| 7 Détails voiture | `/cars/{id}` | CarController |
| 8 Options | `/booking/options` | BookingFlowController |
| 9 Récapitulatif | `/booking/summary` | BookingFlowController |
| 10 Paiement | `/booking/payment` | BookingFlowController |
| 11 Confirmation | `/booking/confirmation/{ref}` | BookingFlowController |
| 12 Mes réservations | `/bookings` | MyBookingsController |
| 13 Détails réservation (+ annuler / modifier) | `/bookings/{id}` | MyBookingsController |
| 14 Favoris | `/favorites` | FavoriteController |
| 15 Profil (+ infos, adresses, aide…) | `/profile` | ProfileController |

## Espace administrateur (`/admin`, rôle ADMIN uniquement)
- Tableau de bord : chiffre d'affaires, nombre de réservations / voitures / clients
- Voitures : ajouter, modifier, activer/désactiver, supprimer (CRUD avec validation)
- Chauffeurs : liste, ajout, suppression
- Réservations : liste complète, annuler / rétablir
- Clients : liste des comptes
Protégé par Spring Security : `.requestMatchers("/admin/**").hasRole("ADMIN")`.

## API REST (architecture client / serveur)
Le même back-end sert **le site web** (pages Thymeleaf) **et une API REST JSON** (`/api/**`) utilisable par n'importe quel client
(application Java, mobile, Postman…). Le projet frère `diago-client` est un client console Java qui n'utilise que cette API.

- **Documentation interactive (Swagger)** : http://localhost:8080/swagger-ui.html
- Authentification : HTTP Basic (email + mot de passe), sans session. Compte test : `demo@diago.mg` / `demo1234`
- Erreurs en JSON : 400 données invalides · 401 non connecté · 403 interdit · 404 introuvable · 409 conflit (voiture indisponible…)

| Méthode | URL | Accès | Rôle |
|---|---|---|---|
| GET | `/api/cars?category=&type=&maxPrice=` | public | Lister / filtrer les voitures |
| GET | `/api/cars/{id}` | public | Détails d'une voiture |
| GET | `/api/cars/{id}/availability?startDate=&endDate=` | public | Disponibilité sur une période |
| POST / PUT / DELETE | `/api/cars` · `/api/cars/{id}` | ADMIN | Gérer les voitures |
| POST | `/api/auth/register` | public | Créer un compte |
| GET | `/api/me` | connecté | Mon profil |
| GET | `/api/bookings` · `/api/bookings/{id}` | connecté | Mes réservations |
| POST | `/api/bookings/quote` | connecté | Calculer le prix |
| POST | `/api/bookings` | connecté | Réserver (201 Created) |
| PUT | `/api/bookings/{id}` | connecté | Modifier une réservation à venir |
| DELETE | `/api/bookings/{id}` | connecté | Annuler une réservation à venir |
| GET / PUT / DELETE | `/api/favorites` · `/api/favorites/{carId}` | connecté | Favoris |

Exemples avec curl :
```
curl http://localhost:8080/api/cars?category=VIP
curl -u demo@diago.mg:demo1234 http://localhost:8080/api/bookings
curl -u demo@diago.mg:demo1234 -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"carId":1,"pickupLocation":"Ivato","startDate":"2027-01-10","startTime":"08:00","endDate":"2027-01-12","endTime":"18:00","mode":"WITHOUT_DRIVER","insurance":true}'
```

## Architecture (MVC en couches)
```
model/       Entités JPA : User, Car, Driver, Booking, SavedAddress + enums
repository/  Interfaces Spring Data JPA
service/     Logique métier : PricingService (prix), BookingService, CarService, UserService
web/         Contrôleurs Spring MVC (pages HTML)
api/         Contrôleurs REST (JSON), DTO, gestion des erreurs
config/      Sécurité, données de démo (DataSeeder), formatage
templates/   Pages Thymeleaf (layout + fragments réutilisables)
```

## Règles de prix (identiques à la maquette)
Location = prix/jour × jours (minimum 1) · chauffeur +50 000 Ar/jour · assurance +15 000 Ar/jour ·
GPS +5 000 · siège bébé +8 000 · livraison +20 000 · retour à domicile +20 000.

## Tests
`mvn test` lance `PricingServiceTest` (calcul des prix).

## Utiliser MySQL au lieu de H2 (optionnel)
Ajouter la dépendance `com.mysql:mysql-connector-j` et remplacer dans `application.properties` :
`spring.datasource.url=jdbc:mysql://localhost:3306/diago`, `username`, `password`.
