# API REST Spring Boot - Application de Gestion des Congés

J'ai conçu et développé l'API Backend REST de mon application de gestion des congés en utilisant Java 17 et Spring Boot 3, en respectant les principes d'architecture logicielle SOLID.

---

## 🏛 Architecture & Principes SOLID

- **SRP (Single Responsibility Principle)** :
  - **Controllers** (`apiprojet.apigestiondeconge.controllers`) : Gèrent exclusivement la couche HTTP (requêtes, réponses, statuts HTTP).
  - **Services** (`apiprojet.apigestiondeconge.service` & `.impl`) : Contiennent l'ensemble de la logique métier et des règles de gestion des congés.
  - **Repositories** (`apiprojet.apigestiondeconge.repository`) : Gèrent la persistance des données via Spring Data JPA.
  - **DTOs** (`apiprojet.apigestiondeconge.dto`) : Isolent la couche de présentation de la base de données pour ne jamais exposer les entités JPA directement.

- **DIP (Dependency Inversion Principle)** :
  - J'ai extrait des interfaces Java pour tous mes services (`DemandeCongeService`, `EmployeService`, `DepartementService`, `TypeCongeService`, `SoldeCongeService`, `ValidationService`, `UtilisateurService`).
  - Mes contrôleurs dépendent uniquement des abstractions (interfaces) et non des implémentations concrètes.

- **Injection de dépendances propre** :
  - J'utilise l'injection par constructeur via `@RequiredArgsConstructor` avec des attributs `final` pour garantir l'immutabilité et faciliter les tests unitaires.

---

##  Sécurité & Authentification

- **Spring Security & JWT** : Authentification stateless basée sur des jetons JSON Web Tokens (JWT).
- **Cryptage BCrypt** : Hachage sécurisé des mots de passe en base de données.
- **Gestion des Rôles** : Contrôle d'accès basé sur les rôles `ADMIN` et `EMPLOYE`.

---

##  Principaux Endpoints REST API

- **Authentification** (`/api/auth`) : Connexion et inscription des utilisateurs.
- **Demandes de congé** (`/api/demandes-conge`) :
  - `GET /api/demandes-conge` : Liste globale des demandes.
  - `POST /api/demandes-conge` : Soumission d'une nouvelle demande avec pièce jointe.
  - `GET /api/demandes-conge/{id}/justificatif` : Téléchargement du justificatif médical/attestation au format binaire.
- **Validations** (`/api/validations`) :
  - `POST /api/validations` : Traitement (Approbation / Refus) d'une demande par un manager/admin.
- **Collaborateurs & Soldes** (`/api/employes`, `/api/solde-conge`) : Gestion des fiches d'employés et suivi des compteurs annuels.
- **Organisation** (`/api/departements`, `/api/type-conge`) : Gestion des services et des motifs de congés.

---

##  Stack Technique Backend

- **Java 17** & **Spring Boot 3**
- **Spring Data JPA** & **Hibernate**
- **Spring Security** & **jjwt (JSON Web Token)**
- **Lombok** (Constructeurs, DTOs, Builders)
- **Base de données** : MySQL / PostgreSQL
