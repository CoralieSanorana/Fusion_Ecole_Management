#!/bin/bash
# Script de lancement local  
# Utilisation: ./run-local.sh

cd "$(dirname "$0")"

# Backup du application.properties original
if [ ! -f src/main/resources/application.properties.bak ]; then
    cp src/main/resources/application.properties src/main/resources/application.properties.bak
fi

# Créer un fichier application.properties temporaire pour le profil local
cat > src/main/resources/application-temp.properties << 'EOF'
# Configuration temporaire pour développement local
spring.profiles.active=local

# Annule les paramètres problématiques du profil par défaut
spring.sql.init.mode=never
spring.jpa.hibernate.ddl-auto=none
spring.jpa.defer-datasource-initialization=false
EOF

# Fusionner avec la config locale
cat src/main/resources/application.properties >> src/main/resources/application-temp.properties

# Copier en tant que application.properties pour ce lancement
cp src/main/resources/application-temp.properties src/main/resources/application.properties

echo "Configuration locale activée. Lancement..."
export SPRING_PROFILES_ACTIVE=local
mvn -DskipTests spring-boot:run "$@"

# Restaurer après
cp src/main/resources/application.properties.bak src/main/resources/application.properties
rm -f src/main/resources/application-temp.properties
echo "Configuration originale restaurée."
