#!/bin/bash
# Script de démarrage pour développement local
# Lance Spring Boot avec le profil 'dev' qui charge application-dev.properties

set -e

cd "$(dirname "$0")"

echo "=========================================="
echo "Fusion_Ecole Management - Démarrage LOCAL"
echo "=========================================="
echo ""

# Vérifier que PostgreSQL est accessible
echo "[1/3] Vérification PostgreSQL..."
if ! psql -h localhost -U postgres -d postgres -c "SELECT 1" > /dev/null 2>&1; then
    echo "❌ ERREUR: PostgreSQL non accessible sur localhost:5432"
    echo "   Commandes de vérification:"
    echo "   - sudo systemctl status postgresql"
    echo "   - psql -h localhost -U postgres"
    exit 1
fi
echo "✓ PostgreSQL accessible"

# Vérifier que Maven est disponible
echo "[2/3] Vérification Maven..."
if ! command -v mvn &> /dev/null; then
    echo "❌ ERREUR: Maven non trouvé"
    echo "   Installer Maven: sudo apt-get install maven"
    exit 1
fi
echo "✓ Maven disponible: $(mvn --version | head -1)"

# Vérifier que application-dev.properties existe
echo "[3/3] Vérification configuration..."
if [ ! -f "src/main/resources/application-dev.properties" ]; then
    echo "❌ ERREUR: application-dev.properties non trouvé"
    echo "   Voir README_LOCAL_SETUP.md pour créer cette configuration"
    exit 1
fi
echo "✓ Configuration locale détectée"

echo ""
echo "Démarrage de l'application avec profil 'dev'..."
echo ""

# Lancer avec le profil dev
export SPRING_PROFILES_ACTIVE=dev
mvn -DskipTests spring-boot:run
