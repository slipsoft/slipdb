/**
 * 
 */
/**
 * J'écris cette partie de Français, parce que si je dois traduire en plus, c'est galère xD
 * 
 * Indexer des colonnes via un IndexTree (TreeMap),
 * Je garde ici un historique des versions pour pouvoir faire des tests et comparatifs de performance.
 * v1 : Indexer une colonne de type Number (Integer, Float, Byte ...) à partir du disque et retrouver
 *      les valeurs rapidement, tous les index sont stockés en mémoire, il n'y a pas de regroupement de valeurs
 *      proches : pour chaque valeur, il y a une ArrayList stockant les binIndex correspondants.
 *      
 * v2 : Stocker le plus de données possible sur le disque, tout en pouvant les retrouver aussi facilement que possible.
 *      Création d'arbres imbriqués, pour avoir le plus de données possibles écrites sur le disque, mais n'avoir à charger que très
 *      peu de données pour retrouver les bonnes listes.
 * v3, et IndexTreeDic (= v4)
 */


/**
	2019 - 03 - 22
	
	L'IndexTreeDic est fonctionnel, et il est possible de charger autant de donnée qu'on veut.
	-> Parser et indexer se fait en mono-thread
		-> Pour prendre en charge me multi-thread, parser plusieurs fichiers à la fois, et écrire dans plusieurs fichiers, idem pour l'indexing (pas prioritaire)
	-> Rechercher des valeurs se fait en multi-thread (stable et fiable)
		-> Recherche sur chaque fichier d'index flush sur le disque simultanément
	
	Je pense que parser et charger l'ensemble des CSV des taxis de NY ne devrait pas prendre plus de 2h, sur une seule machine (en mono-thread)
	La recherche sur disque sera sans doute assez rapide, également (même sur 200Go+), probablement pas plus de 10 secondes pour de la donnée fine
	
	- Au-delà du multi-thread (perf x4 si 4 coeurs), il faut que le multi-noeud soit fonctionnel.
	- Il faut également prendre totalement en charge les fonctions SELECT … WHERE … GROUP BY  (FROM la table principale)
	-> J'implémente ça, maintenant (2019-03-22, 21h)
	
	Pour un IndexTree de base :
	SELECT colonneSelect WHERE (valeur minimale) (valeur maximale) GROUP BY colonneGoup
	
	-> En interne, une recherche (sur colonneSelect) doit être faite, et les résultats devont être ajoutés dans un arbre,
	   en ayant en clef la valeur dans colonneGoup.
	   -> Limiter le nombre de résultats pour ne pas en avoir trop.
	   
	Recherche personnalisée :
	- Retourner, en résultat : select : seulement les colonnes demandées
	                           résultats correspondant aux conditions (colonneId, valeurMin, valeurMax) x (nombre de colonnes)
	                           group by : grouper les résultats en fonction de la bonne colonne ou des bonnes colonnes
	                           order by : ordonner (via un TreeMap) en fonction d'une clef (colonne)
	                           SUM, AVG, MIN, MAX, COUNT : à effectuer sur les résultats
	                             -> Exemple : obtenir le min, max, sum et avg des distances parcourues entre date1 et date2
	                           
	                           
	2019-03-23
	-> Faire le chargement/sauvegarde d'un index complet sur le disque
	-> Charger les données parsées sans avoir à les re-parser (csv)
	-> Faire la recherche sur une colonne indexée
	
	2019-03-24
	-> Avoir la liste des colonnes à indexer, les indexer pendant le parsing ou plus tard
	
	
	SELECT … WHERE … GROUP BY
	
	SELECT : sélectionner une colonne
	
	2019-03-24 - 23h
		Les fichiers de sauvegarde semblent être limités à 4Go.
		-> Faire un système de sauvegarde propre au moteur :
			- Plusieurs fichiers sur le disque, un path (String) et un AtomicBoolean pour dire si le fichier est utilisé (en écriture ou non et un AtomicBoolean pour la lecture, plus tard)
			- Parsing multi-thread : écriture dans un des fichiers libres (non utilisé) et dont la taille est valide (inférieure à une certaine taile, 1go par exemple)
			- Les binIndex des données devront être stockées sous la forme (id fichier)(position dans fichier), (int)(int) ou (short)(int), je garde double au début, au moins
			  ça pourrait aussi être (short)(short)(int) : id du noeud, id du fichier, index (de la ligne) dans le fichier
			- Important : écriture de l'état d'un IndexTreeDic sur le disque, pour pouvoir le charger du disque et le réutiliser, sans avoir à le re-créer à chaque fois
		
		-> Serveur avec les 200Go de CSV chargés, requêtes simples à faire dessus, pour montrer qu'on peut parser beaucoup de donnée
	
	-> Pour cette version, je ne me soucie pas des problèmes de suppression de lignes, ni d'ajout de lignes.
		- Il serait nécessaire d'avoir pus de fichiers, et cela complexifie les choses. Déjà, avoir quelque chose de vraiment bien qui marche, et ensuite l'améliorer.
	
	-> Sauvegarde complet des index sur le disque, pour ne pas avoir à les re-charger à chaque fois (pour le parsing et indexation énorme, faire après le parsing multi-thread)
	
	Pour le parsing multi-thread : écrire de la donnée
	
	2019-03-26 - 20h15
	-> OK Sauvegarde multi-fichiers
	-> OK Chargement de la donnée depuis le disque pour affichage
	-> OK parsing multi-thread
	
	-> à faire : chargement de liste de donnée depuis le disque (doit être rapide, classer par fichier et position croissante dans le fichier puis charger en multi-thread)
	-> à faire : indexer depuis la sauvegarde sur disque
	-> à faire : sauvegarder le noeud sur le disque : Table, STableHandler, IndexTreeDic ...
	-> à faire : charger le noeud du disque (sans re-parser ni re-indexer), pouvoir indexer et parser à la suite sans problème
	-> à faire : multi-noeud très simple, requêtes en ligne de commande depuis le client
	-> à faire : mettre tout ça en place et que ça fonctionne bien pour le vendredi 29 matin !
	-> à faire : 
	-> ATTENTION : attention aux exceptions qui pourraient arriver lors d'une requête et tout casser, à distance...
	-> à faire : 
	
	
	
*/



/**
	-------- Recherches pour la V4 de TreeIndex --------
*/

/**
	Reste à faire :
	
	-> Un index doit être composé de plusieurs sous-index mis sur le disque
		Donc gestion du multi-thread en découle sans souci
	
	-> Il faut définir automatiquement les valeurs de arrayMaxDistanceBetweenTwoNumericalElements
		Balayer globalement le fichier, en déduire les valeurs optimales ? (lent, mais s'il faut...)
	
	-> Un bottleneck clair est la lecture du fichier et la fonction split. Optimiser ça ?
*/




/**
	-------- Recherches pour la V3 de IndexTree --------
*/
//IMPORTANT : les binIndex doivent être des long et non plus des int, comme les fichiers vont être énormes.
//IntegerArrayList : mettre un TreeMap des valeurs fines, plus optimisé qu'un ArrayList, en plus, c'est facile de faire un merge de collections lors d'une recherche
/*****
Sauvegarde d'un indexTreeV3 sur le disque (et mise en lecture seule, donc) :
	-> Il faut avoir une organisation très structurée, savoir très rapidement où retrouver les arbres contenant la donnée fine.
	1) Ecriture de tous les arbres contenant la donnée fine, sur le disque
	2) La donnée fine des arbres (terminaux) est remplacée par une position et une taille : position de la donnée dans le fichier binaire,
	   et nombre d'éléments dans chaque arbre.
	3) Ensuite, tous les arbres juste au-dessus des arbres terminaux écrivent les valeurs qu'il contiennent : valeur, nombre d'arbres terminaux,
	   et pour chaque arbre terminal : valeur et position dans le fichier binaire (donnera la taille quand consulté)
	4) Traîtement des arbres encore au-dessus : identique.
	...
	dernier) identique, sauf que je garde en mémoire vive le dernier arbre (appartenant à l'arbre root, donc).

Recherche d'une valeur, depuis le disque :
	1) Je divise la valeur à rechercher par le diviseur de l'arbre root, je recherche le sous-arbre intermédiaire associé.
	2) Je lis la BinPos du sous-arbre et ne Nombre, je vais alors chercher dans le fichier binaire (de l'index) la sauvegarde de ce sous-arbre.
	3) Je lis chaque valeur stockée dans ce sous-arbre, (chaque champ est composé de (Valeur, PositionBin, Nombre)),
	   je vais à la position bin qui correspond à la valeur que je cherche.
	   i.e. : Lecture de chaque valeur, quand j'ai trouvé la bonne valeur, je vais à la position, et ainsi de suite jusqu'à trouver l'arbre terminal.
	
	-> En cas de valeur dans un interval, je retiens tous les arbres terminaux qui satisfont aux conditions.


*/



/** Plus ancien :
Il serait possible de faire cet arbre d'une manière statique, mais cela nécessiterait une énorme allocation mémoire ?
-> ArrayList des premières valeurs
Idée : diviser la valeur par 1000, trouver la liste qui correspond (au max, 10 vérif à faire)
                     par 100,
                     par 10,
                     enfin, trouver la liste contenant les binIndex associés à la valeur recherchée
 -> Lent avec une ArrayList car nécessite de parcourir toutes les valeurs
 Mais au lieu de parcourir et de charger toutes les valeurs, je ne charge que les valeurs pour les listes intermédiaires, et les feuilles de la liste finale.
 
 

Il faut un coefficient pour regrouper les valeurs : maxDistanceBetweenTwoNumericalElements

Pour les AJOUTS :
Plusieurs types d'arbres possibles :
	le premier arbre :
		regarde si 
*/

/**
 *  --- V1 ---
 * Infos complémentaires :
 * 
 * -> Pour l'instant, c'est dans mon package, mais je vais faire moi-même un peu plus tard le refactoring quand tout marchera !
 * 
 * Modèle d'arbre (générique) pour l'indexation.
 * SIndexingTree est la super-classe dont héritent les arbres spécialisés comme SIndexingTreeInt, String, Float...
 * Le code des classes filles est spécialisé, pour optimiser un maximum le code en fonction du type de la donnée traîtée.
 * -> Le but est de charger le plus de données, le plus rapidement possible, et de pouvoir faire des recherches rapides.
 * 
 * Indexation d'une seule colonne par arbre.
 * Tous les index sont stockés en mémoire vive dans cette version.
 * 
 * Optimisation à réaliser : grouper les valeurs semblables, selon ce que j'ai écrit sur feuille volante.
 * Exemple : diviser la valeur d'un int par 2 pour avoir moins de ramifications, et plus d'éléments dans les listes (feuilles)
 
	Pour pouvoir stocker (beaucoup) plus de données, il faudra que les listes
	des binIndex (feuilles) associées à chaque valeur (de feuille) soient écrites sur le disque.
	  -> Un fichier par arbre.
	  -> les données doivent être aussi simples à lire que possible, et aussi rapides à charger du disque que possible.
	  
	  Fractionner les listes de chaque feuille semble être une mauvaise idée : il faudrait que chaque feuille ait sa liste complète écrite
	  quelque part sur le disque. Mais utiliser un fichier par feuille serait horrible (beaucoup trop long à charger, trop de place perdue sur le disque...)
	  
	  S'il y a suffisamment de mémoire pour indexer toute une colonne, j'indexe toute la colonne, puis j'écris les données sur le disque.
	  Les données seraient de la forme, pour chaque feuille : nombre d'éléments + binIndex pour chaque élément.
	  Une fois que tout a été écrit sur le disque, je libère la mémoire vive et je ne garde que la position (dans le fichier) de chaque liste (feuille).
	  Ainsi, en mémoire, il n'y a plus que la position sur le disque de la liste, et non plus la liste de tous les binIndex.
	  
	  S'il n'y a pas suffisamment de mémoire, alors, à chaque fois que l'arbre est trop plein, je vais le mettre sur le disque.
	  L'arbre sera ainsi fractionné, mais ça ne devrait pas trop nuire aux performances, à condition que les fractions (les bouts d'arbre)
	  soient quand-même suffisament grandes.
	  -> Au lieu d'avoir en mémoire la (seule) position et taille ou commence le bloc associé à une valeur, il faudra donc avoir une liste.
	 	
	  
	
	V1 : indexer une colonne à partir du disque
		Chargement :
		à partir d'un objet Table, lire du disque tous les champs concernés,
		les ajouter à l'index.
		
		Recherche :
		via findMatchingBinIndexes
	
	ok, V1 fonctionnelle, 
	
	V2 : réduire l'espace mémoire pris, n'avoir que très peu de variables en mémoire, charger le moins de données possibles du disque
		
	
	
 * 
 */



package db.structure.indexTree;