# Lancer le Projet École Management

## Prérequis

- **Java** (JDK 11 ou supérieur)
- **Maven** (pour le build Spring Boot)
- **PostgreSQL** (port 5432)
- **Python** (pour le serveur HTTP de développement du frontend)

## Configuration de la Base de Données

1. **Créer la base de données PostgreSQL :**
   ```bash
   # Ouvrir PostgreSQL et créer la base
   createdb ecole
   ```

2. **Exécuter le script SQL :**
   ```bash
   psql -d ecole -f BASE-SQL/schema_ecole_v2.sql
   ```

3. **Vérifier la connexion :**
   ```bash
   psql -d ecole -U postgres
   ```

## Lancer le Backend (Spring Boot)

1. **Naviguer vers le dossier backend :**
   ```bash
   cd Back-ecole
   ```

2. **Compiler le projet avec Maven :**
   ```bash
   mvn clean install
   ```

3. **Lancer l'application Spring Boot :**
   ```bash
   mvn spring-boot:run
   ```

   L'application sera accessible sur `http://localhost:8080`

## Lancer le Frontend

1. **Naviguer vers le dossier frontend :**
   ```bash
   cd Front-ecole
   ```

2. **Lancer le serveur HTTP de développement :**
   ```bash
   python -m http.server 8080
   ```

   Le frontend sera accessible sur `http://localhost:8080/pages/layouts/model.html`

## Accéder à l'Application

- **Frontend** : http://localhost:8080/pages/layouts/model.html
- **Backend API** : http://localhost:8080 (Spring Boot)

## Structure des Contrôleurs

Les contrôleurs Spring MVC sont configurés pour la navigation :

- **Directeur** : `/directeur/*`
  - `/directeur/dashboard`
  - `/directeur/finances`
  - `/directeur/professeurs`
  - `/directeur/profil-professeur`
  - `/directeur/ecolages`

- **Secrétaire** : `/secretaire/*`
  - `/secretaire/paiements`
  - `/secretaire/bilan`
  - `/secretaire/eleves`
  - `/secretaire/profil-eleve`

- **Professeur** : `/professeur/*`
  - `/professeur/emploi`
  - `/professeur/notes`
  - `/professeur/devoirs`
  - `/professeur/bulletins`
  - `/professeur/profil`

- **Étudiant** : `/etudiant/*`
  - `/etudiant/emploi`
  - `/etudiant/notes`
  - `/etudiant/bulletin`
  - `/etudiant/devoirs`

## Dépannage

### Erreur de connexion PostgreSQL
- Vérifier que PostgreSQL est en cours d'exécution
- Vérifier les identifiants dans `applicationContext.xml`
- Assurer que la base `ecole` existe

### Port déjà utilisé
- Si le port 8080 est occupé, utiliser un autre port :
  ```bash
  python -m http.server 8081
  ```

### Problèmes de dépendances Maven
- Nettoyer et réinstaller :
  ```bash
  mvn clean install -U
  ```
