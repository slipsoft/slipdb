package index.memDic;

import com.dant.utils.Log;

import db.data.types.DataTypeEnum;
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
	public boolean equals(Object other) {
		if (other == null) return false;
		if (other.getClass() != getClass()) return false;
		return (originalLinePosition == ((IndexMemDicTemporaryItem)other).originalLinePosition);
	}
	
	@Override
	public int hashCode() {
		return originalLinePosition;
	}
	
	@Override
	public int compareTo(IndexMemDicTemporaryItem other) {
		// Ne pas comparer octet par octet ><'
		int c1 = reallyCompareTo(other);
		int c2 = other.reallyCompareTo(this);
		
		if (c1 != -c2) {
			Log.error("ERROR - IndexMemDicTemporaryItem.compareTo  c1!=-c2 : " + c1 + " - " + c2);
		} else {
			//Log.info("OK -  c1!=-c2 : " + c1 + " - " + c2);
		}
		//Log.info("" + c1);
		return c1;
	}
	
	private int reallyCompareTo(IndexMemDicTemporaryItem other) {
		// Ne pas comparer octet par octet ><'
		
		
		Column[] indexOnThisColArray = IndexMemDic.indexOnThisColArray;
		byte[] myValues, otherValues;
		//if (false)
		for (int iCol = 0; iCol < indexOnThisColArray.length; iCol++) {
			// Comparaison des valeurs à cette position
			Column col = indexOnThisColArray[iCol];
			
			int comparaison = col.compareLineValues(originalLinePosition, other.originalLinePosition);
			
			
			
			if (comparaison != 0) {
				//Log.info("IndexMemDicTempItem.reallyCompareTo comparaison = " + comparaison);
				return comparaison;
			} else {
				//String s1 = col.getDataAsReadableString(originalLinePosition);
				//String s2 = col.getDataAsReadableString(other.originalLinePosition);
				//Log.info("IDENTIQUES : " + s1 + " == " + s2 + " iCol");
			}
			
			/*
			myValues = col.getDataAsRawBytes(originalLinePosition);
			otherValues = col.getDataAsRawBytes(other.originalLinePosition);

			//String myVal = ""; for (int i = 0; i < myValues.length; i++) myVal += Byte.toString(myValues[i]) + " ";
			//String othVal = ""; for (int i = 0; i < otherValues.length; i++) othVal += Byte.toString(otherValues[i]) + " ";
			//Log.info("Compare at " + iCol + "  : " + myVal + " <-> " + othVal);
			
			// Comparaison des valeurs byte par byte
			for (int iByte = 0; iByte < myValues.length; iByte++) {
				byte myByte = myValues[iByte];
				byte otherByte = otherValues[iByte];
				if (myByte != otherByte) {
					//Log.info("RET(" + (myByte - otherByte) + ") Compare at " + iCol + "  : " + myVal + " <-> " + othVal);
					return myByte - otherByte;
				}
			}*/
			
		}
		
		/*Log.info("IndexMemDicTempItem.reallyCompareTo comparaison == 0 - Valeurs identiques");
		Log.info(IndexMemDic.table.getLineAsReadableString(originalLinePosition));
		Log.info(IndexMemDic.table.getLineAsReadableString(other.originalLinePosition));*/
		return 0;
	}
	
	
}
