package index.memDic;

import java.nio.ByteBuffer;
import java.util.Arrays;

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

	public static boolean enableVerboseDichotomy = false;
	public static boolean enableVerboseSort = true;
	
	public static final boolean useSafeSlowComparaisonsNotDichotomy = false;
	public static final boolean enableDoubleDichotomyVerif = false;
	
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
	
	/** Trie les lignes en fonction des valeurs indexées
	 *  Java gère magnifiquement bien le tri, même pour 100 millions d'éléments ! (c'est assez incroyable ^^)
	 */
	public void sortAllv1() {
		
		MemUsage.printMemUsage();
		Timer t1, t2, t3, t4;
		
		t1 = new Timer("IndexMemDic.sortAll - tout :");
		t2 = new Timer("IndexMemDic.sortAll - création objets :");

		MemUsage.printMemUsage();
		IndexMemDicTemporaryItem[] tempSortArray = new IndexMemDicTemporaryItem[totalLength];
		for (int i = 0; i < totalLength; i++) {
			tempSortArray[i] = new IndexMemDicTemporaryItem(i);
		}
		MemUsage.printMemUsage();
		if (enableVerboseSort) t2.log();
		
		t3 = new Timer("IndexMemDic.sortAll - sort :");
		MemUsage.printMemUsage();
		//Arrays.sort(tempSortArray);
		//SCustomSort.sort(tempSortArray);
		Arrays.parallelSort(tempSortArray); // <- faible gain, environ 30% plus rapide, mais prend beaucoup plus de mémoire
		if (enableVerboseSort) t3.log();
		MemUsage.printMemUsage();
		

		t4 = new Timer("IndexMemDic.sortAll - réagencement positions :");
		for (int i = 0; i < totalLength; i++) {
			sortedPositions[i] = tempSortArray[i].originalLinePosition;
			//String displayValues = table.getLineAsReadableString(sortedPositions[i]);
			//Log.info(displayValues);
		}
		MemUsage.printMemUsage();
		if (enableVerboseSort) t4.log();
		for (int i = 0; i < totalLength; i++) {
			tempSortArray[i] = null;
		}
		MemUsage.printMemUsage();
		tempSortArray = null;
		if (enableVerboseSort) t1.log();
		
	}
	
	//findMatchingIndex
	

	private int[] findMatchingIntervalBounds(ByteBuffer searchQuery) { //findClosestLinePosition
		int startIndex = findMatchingIndex(searchQuery, true);
		if (startIndex == -1) return new int[] {-1, -1};
		int stopIndex = findMatchingIndex(searchQuery, false);
		return new int[] {startIndex, stopIndex};
	}
	
	/**
	 *  Trouver par dichotomie LA valeur exacte (sans support des inférieur et supérieur)
	 *  Cette fonction est bien plus simple qu'une dichotomie supportant les inférieur et supérieur,
	 *  car seule l'égalité est recherchée ici.
	 *  @return
	 */
	private int[] findMatchingIntervalBoundsOldWay(ByteBuffer searchQuery) { //findClosestLinePosition
		
		int dicStartIndex = 0;
		int dicStopIndex = sortedPositions.length - 1;
		int intervalLength = dicStopIndex - dicStartIndex + 1;
		int dicCurrentIndex = (dicStopIndex - dicStartIndex) / 2;
		
		/*
		for (int i = 0; i < sortedPositions.length; i++) {
			String line = table.getLineAsReadableString(sortedPositions[i]);
			Log.info(line);
		}*/
		
		if (useSafeSlowComparaisonsNotDichotomy) {
			for (int i = 0; i < sortedPositions.length; i++) {
				int delta = compareLineValuesAndQuery(sortedPositions[i], searchQuery);
				if (delta == 0) {
					dicCurrentIndex = i;//sortedPositions[i];
					//Log.info("Trouvé !!");
					break;
				}// else if (delta >= 0)
					//Log.info("delta = " + delta);
			}
		} else {
			
			while (intervalLength > 1) {
				//Log.info("BBIGFIX currentIndex = " + dicCurrentIndex + " dicStartIndex=" + dicStartIndex + " dicStopIndex=" + dicStopIndex + " normal new value = " + ((dicStopIndex - dicStartIndex) / 2));
				dicCurrentIndex = dicStartIndex + intervalLength / 2;
				//Log.info("BBIGFIX currentIndex = " + dicCurrentIndex + " dicStartIndex=" + dicStartIndex + " dicStopIndex=" + dicStopIndex + " ");
				//Log.info("nouveau dicCurrentIndex = " + dicCurrentIndex + " et diff = " + (dicStopIndex - dicStartIndex));
				
				int realLinePosition = sortedPositions[dicCurrentIndex];
				
				int delta = compareLineValuesAndQuery(realLinePosition, searchQuery);
				
				if (enableVerboseDichotomy) {
					Log.infoOnlySimple("");
					String line = table.getLineAsReadableString(realLinePosition);
					Log.info(line);
					String humanVerbose = "";
					if (delta < 0) humanVerbose = "-> Trop petit ->";
					if (delta > 0) humanVerbose = "<- Trop grand <-";
					if (delta == 0) humanVerbose = "- Parfait ! -";
					Log.info(humanVerbose + "        delta = " + delta);
				}
				
				if (delta > 0) { // valeur trop grande, je prends l'intervalle de gauche
					dicStopIndex = dicCurrentIndex - 1;
				} else if (delta < 0) { // valeur trop petite, je prends l'intervalle de droite
					dicStartIndex = dicCurrentIndex + 1;
					//Log.info("modif startIndex = " + dicStartIndex);
				} else { // égalité
					//Log.info("EGALITE !! dicCurrentIndex = " + dicCurrentIndex);
					break;
				}
				
				if (dicStopIndex >= sortedPositions.length) Log.error("dicStopIndex trop grand");
				if (dicStopIndex < 0) Log.error("dicStopIndex trop petit");
				if (dicStartIndex >= sortedPositions.length) Log.error("dicStartIndex trop grand");
				if (dicStartIndex < 0) Log.error("dicStartIndex trop petit");
				//Log.info("dic bornes OK currentIndex = " + dicCurrentIndex + " dicStartIndex=" + dicStartIndex + " dicStopIndex=" + dicStopIndex + " ");
				
				intervalLength = dicStopIndex - dicStartIndex + 1;
			}
			
		}
		
		//Log.info("------> dicCurrentIndex = " + dicCurrentIndex + " posRéelle = " + sortedPositions[dicCurrentIndex]);
		int delta = compareLineValuesAndQuery(sortedPositions[dicCurrentIndex], searchQuery);
		
		if (delta != 0) {
			//Log.error("delta != 0   " + delta);
			return new int[] {-1, -1};
		}
		
		// Je regarde où débute l'intervalle et où il se termine
		int exactValueStartIndexDic = dicCurrentIndex;
		int exactValueStopIndexDic = dicCurrentIndex;
		
		Timer growTime = new Timer("Temps pris pour trouver les bornes");
		// Tant que je ne suis pas à l'index 0 et que la valeur précédente est égale à la valeur recherchée
		while ( (exactValueStartIndexDic > 0) && (compareLineValuesAndQuery(sortedPositions[exactValueStartIndexDic - 1], searchQuery) == 0) ) {
			exactValueStartIndexDic--;
		}
		
		// Tant que je ne suis pas à l'index maximal et que la valeur suivante est égale à la valeur recherchée
		while ( (exactValueStartIndexDic <= sortedPositions.length - 2) && (compareLineValuesAndQuery(sortedPositions[exactValueStopIndexDic + 1], searchQuery) == 0) ) {
			exactValueStopIndexDic++;
		}
		growTime.log();
		//Log.info("------> exactValueStartIndexDic = " + exactValueStartIndexDic + " exactValueStopIndexDic = " + exactValueStopIndexDic);
		/*for (int i = exactValueStartIndexDic; i <= exactValueStopIndexDic; i++) {
			String lineAsString = table.getLineAsReadableString(sortedPositions[i]);
			Log.info(lineAsString);
		}*/
		
		return new int[] {exactValueStartIndexDic, exactValueStopIndexDic};
	}
	
	
	
	/** Par dichotomie, trouver le nombre à gauche ou à droite correspondant
	 *  @param searchQuery
	 *  @param findLeftValue
	 *  @return  -1 si aucun élèment ne correspondait
	 */
	private int findMatchingIndex(ByteBuffer searchQuery, boolean findLeftValue) { // <- findLeftValue <-  ou  -> findRightValue ->  toujours en valeur exacte (inclusive)
		
		int dicStartIndex = 0;
		int dicStopIndex = sortedPositions.length - 1;
		int intervalLength = dicStopIndex - dicStartIndex + 1;
		int dicCurrentIndex = dicStartIndex + intervalLength / 2;
		
		while (intervalLength > 1) {
			//Log.info("BBUGFIX currentIndex = " + dicCurrentIndex + " dicStartIndex=" + dicStartIndex + " dicStopIndex=" + dicStopIndex + " normal new value = " + ((dicStopIndex - dicStartIndex) / 2));
			//mis à la fin dicCurrentIndex = dicStartIndex + intervalLength / 2;
			//Log.info("BBIGFIX currentIndex = " + dicCurrentIndex + " dicStartIndex=" + dicStartIndex + " dicStopIndex=" + dicStopIndex + " ");
			//Log.info("nouveau dicCurrentIndex = " + dicCurrentIndex + " et diff = " + (dicStopIndex - dicStartIndex));
			
			if (enableVerboseDichotomy) Log.info("intervalLength = " + intervalLength);
			/*if (intervalLength == 2) {
				Log.info(table.getLineAsReadableString(sortedPositions[dicStartIndex]));
				Log.info(table.getLineAsReadableString(sortedPositions[dicStopIndex]));
				Log.info(table.getLineAsReadableString(sortedPositions[dicCurrentIndex]));
			}*/
			
			int realLinePosition = sortedPositions[dicCurrentIndex];
			
			int delta = compareLineValuesAndQuery(realLinePosition, searchQuery);
			
			if (enableVerboseDichotomy) {
				Log.infoOnlySimple("");
				String line = table.getLineAsReadableString(realLinePosition);
				Log.info(line);
				String humanVerbose = "";
				if (delta < 0) humanVerbose = "-> Trop petit ->";
				if (delta > 0) humanVerbose = "<- Trop grand <-";
				if (delta == 0) humanVerbose = "- Parfait ! -";
				Log.info(humanVerbose + "        delta = " + delta);
			}
			
			if (delta > 0) { // valeur trop grande, je prends l'intervalle de gauche
				dicStopIndex = dicCurrentIndex - 1;
			} else if (delta < 0) { // valeur trop petite, je prends l'intervalle de droite
				dicStartIndex = dicCurrentIndex + 1;
				//Log.info("modif startIndex = " + dicStartIndex);
			} else { // égalité
				
				//Log.info("EGALITE !! " + findLeftValue + " dicCurrentIndex = " + dicCurrentIndex);
				//Log.info(table.getLineAsReadableString(realLinePosition));
				
				
				if (findLeftValue) {
					// Trouver la valeur de gauche
					// Je ne m'arrête que lorsque la valeur de gauche est soit différente (plus petite) soit l'index -1
					if (dicCurrentIndex == 0) break;
					int leftRealLinePosition = sortedPositions[dicCurrentIndex - 1];
					int leftDelta = compareLineValuesAndQuery(leftRealLinePosition, searchQuery);
					
					// Toujours la même valeur à gauche, mon index actuel est donc trop grand
					if (leftDelta == 0) {
						dicStopIndex = dicCurrentIndex - 1;
					} else { // valeur différente à gauche, je suis donc à la position minimale
						break;
					}
				} else {
					// Trouver la valeur de droite
					// Je ne m'arrête que lorsque la valeur de droite est soit différente (plus grande) soit l'index maximal
					
					if (dicCurrentIndex == (sortedPositions.length - 1)) break;
					int rightRealLinePosition = sortedPositions[dicCurrentIndex + 1];
					int rightDelta = compareLineValuesAndQuery(rightRealLinePosition, searchQuery);
					
					// Toujours la même valeur à droite, mon index actuel est donc trop petit
					if (rightDelta == 0) {
						dicStartIndex = dicCurrentIndex + 1;
					} else { // valeur différente à droite, je suis donc à la position maximale
						break;
					}
				}
				//break;
			}
			
			if (dicStopIndex >= sortedPositions.length) Log.error("dicStopIndex trop grand");
			if (dicStopIndex < 0) Log.error("dicStopIndex trop petit");
			if (dicStartIndex >= sortedPositions.length) Log.error("dicStartIndex trop grand");
			if (dicStartIndex < 0) Log.error("dicStartIndex trop petit");
			//Log.info("dic bornes OK currentIndex = " + dicCurrentIndex + " dicStartIndex=" + dicStartIndex + " dicStopIndex=" + dicStopIndex + " ");
			
			intervalLength = dicStopIndex - dicStartIndex + 1;
			dicCurrentIndex = dicStartIndex + intervalLength / 2;
		}
		
		//if (dicStopIndex != dicStartIndex) Log.error("ERREUR IndexMemDic.findMatchingIndex : dicStopIndex != dicStartIndex   --   " + dicStopIndex + " != " + dicStartIndex);
		//dicCurrentIndex = dicStartIndex;
		
		
		//Log.info("------> dicCurrentIndex = " + dicCurrentIndex + " posRéelle = " + sortedPositions[dicCurrentIndex]);
		int delta = compareLineValuesAndQuery(sortedPositions[dicCurrentIndex], searchQuery);
		
		if (delta != 0) {
			Log.error("delta != 0   " + delta + " dicCurrentIndex=" + dicCurrentIndex + " realLinePos=" + sortedPositions[dicCurrentIndex]);
			return -1;
		}
		
		return dicCurrentIndex;
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
		searchQuery.rewind();
		int[] intervalBounds;
		if (enableDoubleDichotomyVerif) {
			Timer t1 = new Timer("Temps pris 2 dichotomies (bornes)");
			intervalBounds = findMatchingIntervalBounds(searchQuery);
			t1.log();
			Timer t2 = new Timer("Temps pris 1 dichotomie + grow");
			int[] intervalBoundsVerif = findMatchingIntervalBoundsOldWay(searchQuery);
			t2.log();
			
			if (Arrays.equals(intervalBounds, intervalBoundsVerif) == false) {
				String debugInfo = "";
				for (int i = 0; i < intervalBounds.length; i++) {
					debugInfo += intervalBounds[i] + " ";
				}
				
				debugInfo += " vs  ";
				for (int i = 0; i < intervalBoundsVerif.length; i++) {
					debugInfo += intervalBoundsVerif[i] + " ";
				}
				
				Log.error("Mauvaises valeurs d'intervalles pour la dichotomie : " + debugInfo);
			}
		} else {
			intervalBounds = findMatchingIntervalBounds(searchQuery);
		}
		
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
	
	/** Faire un group-by à partir des résultats passés en entrée.
	 *  Le order 
	 * 	@param positionsArray la liste des positions pour lesquelles faire un order by
	 * 	@param inTable La table dont sont issues les positions stockées dans positionsArray
	 *  @return 
	 */
	public static void orderByColumnsNoCopy(int[] positionsArray, Table inTable) {
		if (positionsArray == null) return;
		if (positionsArray.length == 0) return;
		/*
		if (positionsArray == null) return positionsArray;
		if (positionsArray.length == 0) return positionsArray;*/
		
		
		MemUsage.printMemUsage();
		Timer t1, t2, t3, t4;
		
		t1 = new Timer("IndexMemDic.sortAll - tout :");
		t2 = new Timer("IndexMemDic.sortAll - création objets :");

		MemUsage.printMemUsage();
		IndexMemDicTemporaryItem[] tempSortArray = new IndexMemDicTemporaryItem[positionsArray.length];
		for (int i = 0; i < positionsArray.length; i++) {
			tempSortArray[i] = new IndexMemDicTemporaryItem(i);
		}
		MemUsage.printMemUsage();
		if (enableVerboseSort) t2.log();
		
		t3 = new Timer("IndexMemDic.sortAll - sort :");
		MemUsage.printMemUsage();
		//Arrays.sort(tempSortArray);
		//SCustomSort.sort(tempSortArray);
		Arrays.parallelSort(tempSortArray); // <- faible gain, environ 30% plus rapide, mais prend beaucoup plus de mémoire
		if (enableVerboseSort) t3.log();
		MemUsage.printMemUsage();
		

		t4 = new Timer("IndexMemDic.sortAll - réagencement positions :");
		for (int i = 0; i < positionsArray.length; i++) {
			positionsArray[i] = tempSortArray[i].originalLinePosition;
			//String displayValues = table.getLineAsReadableString(sortedPositions[i]);
			//Log.info(displayValues);
		}
		MemUsage.printMemUsage();
		if (enableVerboseSort) t4.log();
		for (int i = 0; i < positionsArray.length; i++) {
			tempSortArray[i] = null;
		}
		MemUsage.printMemUsage();
		tempSortArray = null;
		if (enableVerboseSort) t1.log();
		
	}
	
	
	
	
}
