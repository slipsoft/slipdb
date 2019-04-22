package index.memDic;

import db.structure.Column;

/**
 * Pour avoir des objets temporaires permettant d'ordonner la liste des positions de lignes
 * en fonction des valeurs indexées.
 *	
 *	Idée, pour ne pas avoir à créer trop d'objets temporaires :
 *	Créer un tableau à deux dimensions de taille fixe, et classer les valeurs de ce tableau -> 
 *	
 *	v1, pas opti, premier test du sort
 *	
 */
public class IndexMemDicTemporaryItem implements Comparable<IndexMemDicTemporaryItem> {
	
	public final int originalLinePosition;
	
	public IndexMemDicTemporaryItem(int argOriginalLinePosition) {
		originalLinePosition = argOriginalLinePosition;
	}
	
	@Override
	public int compareTo(IndexMemDicTemporaryItem other) {
		
		Column[] indexOnThisColArray = IndexMemDic.indexOnThisColArray;
		byte[] myValues, otherValues;
		for (int iCol = 0; iCol < indexOnThisColArray.length; iCol++) {
			// Comparaison des valeurs à cette position
			Column col = indexOnThisColArray[iCol];
			myValues = col.getDataAsRawBytes(originalLinePosition);
			otherValues = col.getDataAsRawBytes(other.originalLinePosition);
			
			// Comparaison des valeurs byte par byte
			for (int iByte = 0; iByte < myValues.length; iByte++) {
				byte myByte = myValues[iByte];
				byte otherByte = otherValues[iByte];
				if (myByte != otherByte) return myByte - otherByte;
			}
			
		}
		
		return 0;
	}
	
	
	
}
