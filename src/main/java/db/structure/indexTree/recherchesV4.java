package db.structure.indexTree;

/**
	Recherches pour la V4 de TreeIndex
*/

/**
	Reste à faire :
	
	-> Un index doit être composé de plusieurs sous-index mis sur le disque
		Donc gestion du multi-thread en découle sans souci
	
	-> Il faut définir automatiquement les valeurs de arrayMaxDistanceBetweenTwoNumericalElements
		Balayer globalement le fichier, en déduire les valeurs optimales ? (lent, mais s'il faut...)
	
	-> Un bottleneck clair est la lecture du fichier et la fonction split. Optimiser ça ?
*/