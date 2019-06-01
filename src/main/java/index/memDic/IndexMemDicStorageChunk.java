package index.memDic;

/** 
 *  Optimisé pour la recherche (retrouver une position),
 *  significativement plus lent pour les additions et suppressions.
 */
public class IndexMemDicStorageChunk {
	
	
	public int[] sortedPositions;
	// public int length;
	
	public int startPosition; // <- mis à jour à chaque fois qu'une ligne est ajoutée/modifiée
	public int realLength;
	// public int stopIndex;
	// ByteBuffer startValue, stopValue; ?
	
	public IndexMemDicStorageChunk(int thisBlockLength, int argStartIndex) {
		realLength = thisBlockLength;
		sortedPositions = new int[realLength];
		startPosition = argStartIndex;
	}
	
	public int getLastPosition() {
		return startPosition + realLength - 1;
	}
	
	/** Sans vérification sur les bornes du tableau sortedPositions par rapport à localPosition.
	 * @param localPosition
	 * @return
	 */
	public int getValueAtLocalPosition(int localPosition) {
		return sortedPositions[localPosition];
	}
	
	/** Sans vérification sur les bornes du tableau sortedPositions par rapport à localPosition.
	 * @param globalPosition
	 * @return
	 */
	public int getValueAtGlobalPosition(int globalPosition) {
		return sortedPositions[globalPosition - startPosition];
	}

	/** Retourne true si la position est dans ce chunk, false sinon.
	 *  @param globalPositionInIndex
	 *  @return
	 */
	public boolean globalPositionIsInThisChunk(int globalPositionInIndex) {
		if (globalPositionInIndex >= startPosition && (globalPositionInIndex <= getLastPosition()))
			return true;
		return false;
	}

	/** Retourne true si la position est dans ce chunk, false sinon.
	 *  @param globalPositionInIndex
	 *  @return
	 */
	public boolean hasGlobalPosition(int globalPosition) {
		if (globalPosition >= startPosition && (globalPosition <= getLastPosition()))
			return true;
		return false;
	}
	
}
