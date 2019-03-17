package db.structure.indexTree;

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