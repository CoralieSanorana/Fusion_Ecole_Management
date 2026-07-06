#!/bin/bash
# Lance l'application avec les variables d'environnement du fichier .env.local
# Ces variables surchargent les properties par défaut de Spring Boot

set -a
source .env.local
set +a

export SPRING_PROFILES_ACTIVE=dev

echo "=========================================="
echo "Démarrage Fusion Ecole - Mode DEV"
echo "=========================================="
echo "Profil actif: $SPRING_PROFILES_ACTIVE"
echo ""

# Test PostgreSQL
if ! psql -h localhost -U postgres -d postgres -c "SELECT 1" > /dev/null 2>&1; then
    echo "❌ PostgreSQL non accessible"
    exit 1
fi

mvn -DskipTests spring-boot:run
