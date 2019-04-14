package zArchive.sj.recherches;

/**
	(But selon Sylvain)
	Moteur d'index distribué – but pour le 2019 – 03 – 29 :
	
	Application fonctionnelle, tout les fichiers CSV chargés (170Go), possible de faire une requête de la salle machine (port 80, ou tél portable) pour récupérer les données (sur serveurs chez moi ou mis chez Nicolas + Etienne + moi). Donc serveur central et noeuds (ayant des données différentes).
	
	- Un serveur central (aussi un noeud)
	- Chargement des données par les noeuds, mise sur le disque pour pouvoir indexer beaucoup de données : les arbres sont mis sur disque lorsqu'il n'y a bientôt plus de mémoire dispo. Un index complet est composé d'un arbre en mémoire vive (arbre en cours) et d'une liste d'arbres sur disque (non modifiables, pour l'instant, plus tard modifiables), recherche sur un index complet : (point suivant)
	- Lors d'une recherche, attribuer un thread par arbre sur disque (et le thread principal pour la recherche sur l'arbre en mémoire, éventuellement, ou un autre thread)
	- Architecture multi-noeuds en réseau : HTTP (comme Olivier le veut)  mais éventuellement, pourquoi pas une démo avec les (NetBuffers, et TCPClient/Server) ? (si j'ai le temps / du temps à perdre, Sylvain)
	- Pour un index statique, on peut envisager d'avoir tout sur le disque, et de ne pas s'embêter avec un arbre en mémoire, également.
*/