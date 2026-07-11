Date : 11 Juillet 2026 à 15h49
Testeur : Funaki Live ETU004169
Page : Directeur/Dashboard.html
Scenariot : Des la connction , on est redirige vers la page Dashboard . on voit bien le bilan financier
            Nombres de Professeur  Actifs Actuellement et le nombre d'Etudiants ayant paye ou pas paye leur ecoles de ce mois
            -> Lors d'un ajout de Depences via le secretaire , le bilan se met a jour automatiquement , filtre pour voir les benefice mensuel 
            -> Affichage de Gaphe sur les depenses et les revenus et un graphe en fromage sur les paiement des eleves 
            -> Export dees bilans et des transactions issue de l'ecole en pdf 

Teste fait : Ajouter des depenses via le secretaire sur l'achat de plusieuers tole pour la renovation de l'ecole 
            -> Le bilan se met a jour automatiquement 
            -> j 'ai exporter le bilan et les transactions en pdf et ca liste bien tout 
            -> Le depenses est visibles par le directeur

Les tables utiliser :


Page : Directeur/initialize.html (Initialisation de l'ecole)
Scenariot : On inserere une annee scolaire puis on peut ajouter des niveaux et des salles et des matieres puis on affectes les professeurs au matieres qu'il enseigne 
        -> Rien ne marche sans une anne scolaire actif , Les classes devraient avoir des salles et sans salles actifs on ne peut pas cree de classe  

Teste fait : J'ai inserer une annee scolaire puis j'ai ajouter des niveaux et des salles et des matieres puis j'ai affectes les professeurs au matieres qu'il enseigne 
             -> Tout ceci se fait bien dans la base de donnees mais il sont inserer individuellement 
             exemple : on peut inserer des classes sans salles de classe et on peut inserer des niveaux sans classes et meme sans etablissement on peut ajouter des 
             annes scolaire 

Solution : Il faut que l'initialisation se fasse dans un ordre logique et que chaque etape soit obligatoire avant de passer a l'autre
           Par exemple : on doit d'abord creer un etablissement puis une annee scolaire puis des niveaux puis des salles puis des matieres puis des classes puis des professeurs
           et chaque etape doit etre obligatoire avant de passer a l'autre
                En modifiant : static/js/directeur-js/initialize.js et java/service/InitializeService.java 
                le probleme est bien regler et fonctionnelles maintenant 
                Tout est interdependant et chaque etape doit etre obligatoire avant de passer a l'autre

Les tables utiliser : 
 


Page : Directeur/edt.html (Insertion des emplois du Temps)
Scenariot : -> On affiche les emplois du temps de chaque salle a avec une forme de tables 
            -> On cliques sur une cases dans le tables et on peut inserer cliquer sur plusieurs cases et puis 
             ajouter a cette heure et jour qui correspont a la case selectionner un professeur qui ensaigne la matiere
            -> On peut aussi changer les horaires des cours selon les salles 
            -> Si on clique sur une case deja associe a une affectation elle ecrase directement celle de cette colonne
            -> on pourra choir quelle salle on veut observer l'emplois du temps etc 

Teste fait : J'ai inserer des emplois du temps pour plusieurs salles et plusieurs jours et ca s'insere bien dans la base mais il y a un probleme 
             car il n'y a pas d'horaire par defaut dans la base de donnees 
             -> Erreurs quand je configure une horaire specifique pour un niveau ca ne s'inserere pas dans la base de donnees
             -> Le filtre beug car si on choisit une salle qui n'est pas associe au meme niveau ca affiche un message d'erreur qu'il n'y a 
             pas d'horaire en base 

 
Solution : Mettre classe au lieu de niveau en filtrage et niveau servira a configurer son horaire speciales en database 

Les tables utiliser :
            

