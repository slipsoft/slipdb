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
 *
 */


/**
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