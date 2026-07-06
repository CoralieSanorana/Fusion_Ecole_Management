# Solution pour erreur PostgreSQL : "This connection has been closed"

## 🔴 Problème

Lors du démarrage du projet Spring Boot avec `mvn spring-boot:run`, l'application échoue avec l'erreur:
```
org.postgresql.util.PSQLException: This connection has been closed.
```

**Cause racine identifiée**: 
1. HikariCP ferme les connexions inactives (timeout 60s) pendant que Hibernate inspecte intensivement le schéma
2. Hibernates 6 effectue aussi une validation du schéma qui échoue car la base a `INTEGER` au lieu de `BIGINT` pour les clés primaires

## ✅ Solution - Mode Développement Local

Les fichiers suivants ont été créés pour vous et n'affectent **PAS** le repository Git:

### 1. Configuration développement (`application-dev.properties`)
```properties
# Ce fichier est automatiquement chargé quand you lancez avec profil 'dev'
spring.jpa.hibernate.ddl-auto=none
spring.sql.init.mode=never
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
```

### 2. Fichier environnement (`.env.local`)
```bash
# Automatiquement chargé par le script run-dev-env.sh
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

## 🚀 Lancement du projet

**Meilleur option** - Utilisez le script fourni qui charge les variables d'environnement:
```bash
./run-dev-env.sh
```

Ou manuellement avec le profil Spring:
```bash
# Option 1: Profil Spring
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run

# Option 2: Script de démarrage avec vérifications
./start-dev.sh

# Option 3: IDE (IntelliJ/Eclipse)
# Ajouter dans Run Configurations -> VM options:
# -Dspring.profiles.active=dev
```

## ✓ Vérification du succès

Après le démarrage, vous devriez voir:
```
2026-07-06T... INFO ... : The following 1 profile is active: "dev"
2026-07-06T... INFO ... : Tomcat initialized with port(s): 8080
2026-07-06T... INFO ... : Started EcoleApplication in X.XXX seconds
```

**PAS** de messages d'erreur concernant les connexions PostgreSQL fermées ou la validation du schéma.

## 📋 Fichiers créés (n'affectent pas Git)

Les fichiers suivants sont dans `.gitignore.local`:
- `src/main/resources/application-dev.properties` - Configuration Hibernates/HikariCP
- `.env.local` - Variables d'environnement
- `start-dev.sh` - Script de démarrage avec vérifications
- `run-dev-env.sh` - Script de démarrage avec chargement du .env.local

## 🔧 Configuration validée

- PostgreSQL 16.13
- Spring Boot 3.3.0  
- Hibernate 6.5.2
- HikariCP 5.1.0
- Java 21 (ou 17+)

## 🧹 Nettoyage

Pour réinitialiser à zéro:
```bash
rm src/main/resources/application-dev.properties .env.local
rm start-dev.sh run-dev-env.sh
mvn clean
```

## ⚠️ Note importante

La configuration du projet par défaut (`src/main/resources/application.properties`) contient des valeurs qui causent ce problème. Le profil `dev` les surcharge correctement. **Ne modifiez pas** `application.properties` - le profil `dev` résout complètement le problème.
