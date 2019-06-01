package index.memDic;

import com.dant.utils.Log;

/** 
 *  Permet la gestion du tableau de positions classé de IndexMemDic
 *  
 */
public class IndexMemDicStorage {
	
	public static final int baseBlockSize = 10_000; // Taille initiale de chaque block.
	// Si un bloc dépasse le double de cette taille, il est split en deux (et les positions des autres blocs sont actalisées)
	// -> Pas trop compliqué de décaler tous les autres blocs et de les mettre à jour,
	//    le nombre de blocs à bouger est de l'ordre de nombre_de_lignes / baseBlockSize
	//    beaucoup beaucoup moins gourmand en ressources que d'avoir une seule liste de positions (sans blocks)
	
	/**
	 *  Il faut une liste de blocs contenant :
	 *  
	 *  
	 *  Il faut aussi pouvoir retrouver très rapidement le bloc contenant un index en particulier :
	 *  -> Analyser tous les blocs est trop lent. Recherche par dichotomie pour déterminer le bloc à prendre.
	 */
	
	public IndexMemDicStorageChunk[] a1Chunks;
	public int totalNumberOfPositions; // nombre total de positions stockées (et donc classées)
	
	
	public IndexMemDicStorage(int totalLength) {
		totalNumberOfPositions = totalLength;
		int chunkNb = (int) Math.ceil(((double)totalNumberOfPositions / (double)baseBlockSize)); // arrondi au supérieur
		
		a1Chunks = new IndexMemDicStorageChunk[chunkNb];
		
		if (chunkNb != 0) {
			// Création des ckunks de taille baseBlockSize
			for (int iChunk = 0; iChunk <= chunkNb - 2; iChunk++) {
				a1Chunks[iChunk] = new IndexMemDicStorageChunk(baseBlockSize, iChunk * baseBlockSize);
			}
			// Création du dernier chunk, de taille variable
			int lastChunkLength = totalNumberOfPositions - (chunkNb - 1) * baseBlockSize;
			a1Chunks[chunkNb - 1] = new IndexMemDicStorageChunk(lastChunkLength, (chunkNb - 1) * baseBlockSize);
		} else {
			// pas de données indexées, pas de chunk à créer
		}
		
	}
	
	/** Il est nécessaire d'appeler cette fonction pour actualiser les positions des chunks.
	 *  
	 */
	public void refreshChunkList() {
		int nbChunks = a1Chunks.length;
		// 1) Suppression des chunks vides
		
		// 2) Calcul des positions de début des chunks
		int currentPosition = 0;
		for (int iChunk = 0; iChunk < nbChunks; iChunk++) {
			IndexMemDicStorageChunk chunk = a1Chunks[iChunk];
			chunk.startPosition = currentPosition;
			currentPosition += chunk.realLength;
		}
	}
	
	/** Il n'est simple de trouver directement la valeur associée à une position
	 *  à part en balayant les positions de fin et de début de chaque chunk, ce qui n'est pas optimisé.
	 *  Pour trouver la valeur à une position donnée, il est donc judicieux de faire une recherche par dichotomie.
	 *  
	 *  Trouver la "position de la ligne chargée" associée à une position dans cet index
	 *  @param globalPosition
	 *  @return
	 */
	public int findValueAtPosition(int globalPositionInIndex, boolean useDichotomy) {
		
		if (useDichotomy == false) {
			// 1) Méthode bourrine et lente : tout scanner
			int nbChunks = a1Chunks.length;
			for (int iChunk = 0; iChunk < nbChunks; iChunk++) {
				IndexMemDicStorageChunk chunk = a1Chunks[iChunk];
				if (chunk.realLength == 0) continue;
				if (globalPositionInIndex >= chunk.startPosition && (globalPositionInIndex <= chunk.getLastPosition())) {
					// C'est dans ce chunk là !
					return chunk.getValueAtGlobalPosition(globalPositionInIndex);
				}
			}
		} else {
			// recherche par dichotomie
			int startChunkPosition = 0;
			int stopChunkPosition = a1Chunks.length - 1;
			
			int currentChunkPosition;// = a1Chunks.length / 2;
			
			while (stopChunkPosition - startChunkPosition >= 1) {
				int chunkInvervalLength = stopChunkPosition - startChunkPosition;
				// Je me place au centre de l'intervalle
				currentChunkPosition = startChunkPosition + chunkInvervalLength / 2;
				// Je regarde où je dois aller (ok, droite, gauche)
				IndexMemDicStorageChunk chunk = a1Chunks[currentChunkPosition];
				if (chunk.hasGlobalPosition(globalPositionInIndex)) {
					startChunkPosition = currentChunkPosition;
					stopChunkPosition = currentChunkPosition;
					return (chunk.getValueAtGlobalPosition(globalPositionInIndex));
				}
				// Si le chunk n'a pas la position, je regarde si la position est à droite ou à gauche
				if (chunk.startPosition < globalPositionInIndex) {
					// Si la position de départ du chunk est plus petite et qu'il ne contient pas la position,
					// je drois aller à droite
					startChunkPosition = currentChunkPosition + 1;
					continue;
				} else { // (else non nécessaire, mais ajouté pour plus de clareté)
					// Si la position de départ du chunk est plus grande et qu'il ne contient pas la position,
					// je drois aller à gauche
					stopChunkPosition = currentChunkPosition - 1;
					continue;
				}
			}
			if (stopChunkPosition - startChunkPosition >= 1) {
				Log.error("Bornes de fin de dichotomie invalides : " + (stopChunkPosition - startChunkPosition));
				return -1;
			}
			
			IndexMemDicStorageChunk chunk = a1Chunks[startChunkPosition];
			if (chunk.hasGlobalPosition(globalPositionInIndex) == false) {
				Log.error("Un seul chunk restant, mais la position n'y est pas."
						+ "globalPositionInIndex("+globalPositionInIndex+") et intervalChunk=[" + chunk.startPosition + ",  " + chunk.getLastPosition() + "]");
				return -1;
				
			} else {
				return (chunk.getValueAtGlobalPosition(globalPositionInIndex));
				//Log.info("Trouvay !");
			}
			
			
		}
		
		Log.error("Position invalide, n'est dans aucun chunk. useDichotomy = " + Boolean.toString(useDichotomy));
		return -1;
	}
	
	/** 
	 *  Ajouter pleins de lignes : 
	 *  
	 *  Supprimer pleins de lignes : c'est simple, pour chaque ligne, trouver sa position dans l'index via ses valeurs,
	 *  et supprimer les lignes unes à unes.
	 */
	
	
	/** Cas simple : supprimer une seule ligne
	 *  @param globalDeletePosition
	 */
	public void deleteAtPositionSingle(int globalDeletePosition) {
		// 1) Il faut trouver le bloc dans lequel est cette position
		
		
		
	}
	
	public void setChunkLineValue() {
		
	}
	
	/**
	 *  Ajout de pleins de lignes à la fois : ajout des lignes aux bonnes positions,
	 *  puis 
	 */
	
	
	
}
