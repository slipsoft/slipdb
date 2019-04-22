package index.memDic;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.lang3.ArrayUtils;

import com.dant.utils.Log;
import com.dant.utils.MemUsage;
import com.dant.utils.Timer;

import db.structure.Column;
import db.structure.Table;

/**
 * 
 *
 */
public class IndexMemDic {
	
	public final int totalLength;
	private int[] sortedPositions;
	// Pensé pour être mono-thread, pour l'instant
	public static Table table;
	public static int[] colIndexArray; // dans l'ordre
	public static Column[] indexOnThisColArray;
	
	public IndexMemDic(Table argTable, int[] argColIndexArray) { // int argTotalLength,
		colIndexArray = argColIndexArray;
		table = argTable;
		totalLength = table.getTotalLinesCount();
		sortedPositions = new int[totalLength];
		indexOnThisColArray = new Column[colIndexArray.length];
		for (int i = 0; i < colIndexArray.length; i++) {
			indexOnThisColArray[i] = table.getColumns().get(colIndexArray[i]);
			//dataAsBytesTotalLength += choosenColArray[i].dataSizeInBytes;
		}
	}
	
	/*
	public void setPosition(int sortedByValueIndex, int lineOriginalPosition) {
		sortedPositions[sortedByValueIndex] = lineOriginalPosition;
	}*/
	
	public void sortAllv1() {
		
		Timer t = new Timer("IndexMemDic.sortAll pris :");
		MemUsage.printMemUsage();
		IndexMemDicTemporaryItem[] tempSortArray = new IndexMemDicTemporaryItem[totalLength];
		for (int i = 0; i < totalLength; i++) {
			tempSortArray[i] = new IndexMemDicTemporaryItem(i);
		}
		
		Arrays.sort(tempSortArray);
		
		for (int i = 0; i < totalLength; i++) {
			sortedPositions[i] = tempSortArray[i].originalLinePosition;
			//String displayValues = table.getLineAsReadableString(sortedPositions[i]);
			//Log.info(displayValues);
		}
		
		t.log();
		MemUsage.printMemUsage();
		
	}
	
	/**
	 *  Trouver par dichotomie LA valeur exacte (sans support des inférieur et supérieur)
	 *  Cette fonction est bien plus simple qu'une dichotomie supportant les inférieur et supérieur,
	 *  car seule l'égalité est recherchée ici.
	 *  @return
	 */
	private int[] findMatchingIntervalBounds(ByteBuffer searchQuery) { //findClosestLinePosition
		
		int dicStartIndex = 0;
		int dicStopIndex = sortedPositions.length - 1;
		int intervalLength = dicStopIndex - dicStartIndex + 1;
		int dicCurrentIndex = (dicStopIndex - dicStartIndex) / 2;
		
		for (int i = 0; i < sortedPositions.length; i++) {
			int delta = compareLineValuesAndQuery(sortedPositions[i], searchQuery);
			if (delta == 0) {
				dicCurrentIndex = i;//sortedPositions[i];
				Log.info("Trouvé !!");
				break;
			} else if (delta >= 0)
				Log.info("delta = " + delta);
		}
		/*
		while (intervalLength > 1) {
			dicCurrentIndex = (dicStopIndex - dicStartIndex) / 2;
			
			int delta = compareLineValuesAndQuery(dicCurrentIndex, searchQuery);
			
			if (delta > 0) { // valeur trop grande, je prends l'intervalle de gauche
				dicStopIndex = dicCurrentIndex - 1;
			} else if (delta < 0) { // valeur trop petite, je prends l'intervalle de droite
				dicStartIndex = dicCurrentIndex + 1;
			} else { // égalité
				Log.info("EGALITE !! dicCurrentIndex = " + dicCurrentIndex);
				break;
			}

			if (dicStopIndex >= sortedPositions.length) Log.error("dicStopIndex trop grand");
			if (dicStopIndex < 0) Log.error("dicStopIndex trop petit");
			if (dicStartIndex >= sortedPositions.length) Log.error("dicStartIndex trop grand");
			if (dicStartIndex < 0) Log.error("dicStartIndex trop petit");
			Log.info("dic boenes OK currentIndex = " + dicCurrentIndex);
			
			intervalLength = dicStopIndex - dicStartIndex + 1;
		}*/
		
		Log.info("------> dicCurrentIndex = " + dicCurrentIndex + " posRéelle = " + sortedPositions[dicCurrentIndex]);
		int delta = compareLineValuesAndQuery(sortedPositions[dicCurrentIndex], searchQuery);
		
		if (delta != 0) {
			//Log.error("delta != 0   " + delta);
			return new int[] {-1, -1};
		}
		
		// Je regarde où débute l'intervalle et où il se termine
		int exactValueStartIndexDic = dicCurrentIndex;
		int exactValueStopIndexDic = dicCurrentIndex;
		
		// Tant que je ne suis pas à l'index 0 et que la valeur précédente est égale à la valeur recherchée
		while ( (exactValueStartIndexDic > 0) && (compareLineValuesAndQuery(sortedPositions[exactValueStartIndexDic - 1], searchQuery) == 0) ) {
			exactValueStartIndexDic--;
		}
		
		// Tant que je ne suis pas à l'index maximal et que la valeur suivante est égale à la valeur recherchée
		while ( (exactValueStartIndexDic <= sortedPositions.length - 2) && (compareLineValuesAndQuery(sortedPositions[exactValueStopIndexDic + 1], searchQuery) == 0) ) {
			exactValueStopIndexDic++;
		}

		Log.info("------> exactValueStartIndexDic = " + exactValueStartIndexDic + " exactValueStopIndexDic = " + exactValueStopIndexDic);
		/*for (int i = exactValueStartIndexDic; i <= exactValueStopIndexDic; i++) {
			String lineAsString = table.getLineAsReadableString(sortedPositions[i]);
			Log.info(lineAsString);
		}*/
		
		return new int[] {exactValueStartIndexDic, exactValueStopIndexDic};
	}
	
	
	/** 
	 *  @param linePosition
	 *  @param seachQuery
	 *  @return
	 */
	public int compareLineValuesAndQuery(int linePosition, ByteBuffer searchQuery) {
		int delta = 0;
		searchQuery.rewind();
		// Comparaison de chaque colonne, l'une après l'autre
		for (int iCol = 0; iCol < indexOnThisColArray.length; iCol++) {
			// Comparaison des valeurs à cette position
			Column col = indexOnThisColArray[iCol];
			int comparaison = col.compareLineValues(linePosition, searchQuery);
			if (comparaison != 0) {
				delta = comparaison;
				break;
			}
		}
		return delta;
	}
	
	public int[] findMatchingLinePositions(ByteBuffer searchQuery) {
		int[] intervalBounds = findMatchingIntervalBounds(searchQuery);
		int startIndex = intervalBounds[0];
		int stopIndex = intervalBounds[1];
		if (startIndex == -1 || (stopIndex - startIndex <= 0))
			return new int[0];
		
		int intervalLength = stopIndex - startIndex + 1;
		int[] originalLinePositionArray = new int[intervalLength];
		
		for (int iRes = 0; iRes < intervalLength; iRes++) {
			originalLinePositionArray[iRes] = sortedPositions[iRes + startIndex];
		}
		
		return originalLinePositionArray;
	}
	
}
