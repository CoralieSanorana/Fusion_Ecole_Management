Date : 11 Juillet 2026 à 15h49
Testeur : Funaki Live ETU004169

---

## Page : Directeur/Dashboard.html

**Scénario :**
Dès la connexion, on est redirigé vers la page Dashboard. On voit bien le bilan financier, le nombre de professeurs actifs actuellement et le nombre d'étudiants ayant payé ou non leur école de ce mois.
- Lors d'un ajout de dépenses via le secrétaire, le bilan se met à jour automatiquement
- Filtre pour voir les bénéfices mensuels
- Affichage de graphes sur les dépenses et les revenus et un graphe en camembert sur les paiements des élèves
- Export des bilans et des transactions de l'école en PDF

**Tests effectués :**
Ajout de dépenses via le secrétaire pour l'achat de plusieurs tôles pour la rénovation de l'école :
- Le bilan se met à jour automatiquement
- Export du bilan et des transactions en PDF réussi
- Les dépenses sont visibles par le directeur

**Tables utilisées :**
- `transactions`
- `depenses`
- `annees_scolaires`
- `profils_professeurs`
- `profils_etudiants`
- `paiements`

**Statut :** ✅ Opérationnel

---

## Page : Directeur/initialize.html (Initialisation de l'école)

**Scénario :**
On insère une année scolaire puis on peut ajouter des niveaux, des salles et des matières puis on affecte les professeurs aux matières qu'ils enseignent.
- Rien ne marche sans une année scolaire active
- Les classes devraient avoir des salles et sans salles actifs on ne peut pas créer de classe

**Tests effectués :**
Insertion d'une année scolaire, ajout de niveaux, salles, matières et affectation des professeurs aux matières :
- Tout ceci se fait bien dans la base de données mais ils sont insérés individuellement
- Problème : on peut insérer des classes sans salles de classe, des niveaux sans classes et même sans établissement on peut ajouter des années scolaires

**Solution appliquée :**
L'initialisation se fait maintenant dans un ordre logique et chaque étape est obligatoire avant de passer à l'autre :
1. Création d'un établissement
2. Création d'une année scolaire
3. Création des niveaux
4. Création des salles
5. Création des matières
6. Création des classes
7. Création des professeurs

Modifications effectuées dans `static/js/directeur-js/initialize.js` et `java/service/InitializeService.java`.

**Tables utilisées :**
- `etablissements`
- `annees_scolaires`
- `niveaux`
- `salles`
- `matieres`
- `classes`
- `profils_professeurs`
- `users`
- `roles`
- `user_roles`

**Statut :** ✅ Opérationnel et fonctionnel

---

## Page : Directeur/edt.html (Gestion des Emplois du Temps)

**Scénario :**
- Affichage des emplois du temps de chaque salle sous forme de tableau
- Sélection de plusieurs cases dans le tableau pour insérer une affectation (professeur + matière)
- Configuration des horaires des cours par niveau
- Écrasement automatique si on clique sur une case déjà associée à une affectation
- Filtrage par classe pour observer l'emploi du temps

**Tests effectués :**
Insertion d'emplois du temps pour plusieurs salles et plusieurs jours :
- L'insertion fonctionne bien dans la base de données
- Problème initial : pas d'horaires par défaut dans la base de données
- Erreur lors de la configuration d'horaires spécifiques pour un niveau
- Bug du filtre : si on choisit une salle non associée au même niveau, message d'erreur

**Solution appliquée :**
1. **Filtrage par classe** : Remplacement du filtre "Niveau" par un filtre "Classe"
2. **Auto-détermination de la salle** : La salle est automatiquement déterminée à partir de la classe choisie via la relation `classes.salle_id`
3. **Configuration des horaires par niveau** : Le niveau sert uniquement à configurer les horaires spécifiques en base de données
4. **Création automatique d'horaires par défaut** : Les niveaux sans horaires configurées reçoivent automatiquement les 9 plages horaires par défaut (07h00-17h00)
5. **Correction de l'insertion** : Les horaires spécifiques s'insèrent maintenant correctement dans la base de données

**Modifications effectuées :**
- `templates/directeur/edt.html` : Remplacement du filtre niveau par classe, mise à jour des stats
- `controller/DirecteurController.java` : Gestion du paramètre `classe_id` et auto-détermination de la salle
- `service/EdtService.java` : Création automatique d'horaires par défaut et correction de l'insertion

**Tables utilisées :**
- `emploi_du_temps`
- `horaire_edt`
- `affectations_enseignement`
- `classes`
- `salles`
- `niveaux`
- `annees_scolaires`
- `matieres`
- `profils_professeurs`

**Statut :** ✅ Opérationnel et bien configuré

