package index.memDic;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dant.utils.Log;
import com.dant.utils.MemUsage;
import com.dant.utils.Timer;

import db.data.types.StringType;
import db.search.Predicate;
import db.structure.Column;
import db.structure.Table;
import index.IndexException;

/**
 * 
 * 
 */
public class IndexMemDic extends IndexMemDicAncester {
	
	
	
	public int totalLength;
	private int[] sortedPositions;
	private int sortedPositionsRealLength;
	private boolean hasToCheckResultsFlags = false;
	private boolean notBeenSortedYet = true;
	
	
	/** Création de l'index par dichotomie
	 *  @param argTable
	 *  @param argColIndexArray
	 */
	public IndexMemDic(Table argTable, int[] argColIndexArray) { // int argTotalLength,
		colIndexArray = argColIndexArray;
		table = argTable;
		totalLength = 0; // Initialisation à 0, mise à jour dans refreshIndexWithColumnsData()   //ancienne valeur : table.getTotalLinesCount();
		sortedPositionsRealLength = totalLength;
		sortedPositions = new int[sortedPositionsRealLength];
		notBeenSortedYet = true;
		
		indexOnThisColArray = new Column[colIndexArray.length];
		for (int i = 0; i < colIndexArray.length; i++) {
			indexOnThisColArray[i] = table.getColumns().get(colIndexArray[i]);
			//dataAsBytesTotalLength += choosenColArray[i].dataSizeInBytes;
		}
	}
	
	
	/** Création de l'index par dichotomie, en le rendant directement fonctionnel si (shortThisIndex == true) (classement des valeurs indexées)
	 *  @param argTable
	 *  @param argColIndexArray
	 *  @param shortThisIndex
	 */
	public IndexMemDic(Table argTable, int[] argColIndexArray, boolean shortThisIndex) { // int argTotalLength,
		this(argTable, argColIndexArray);
		if (shortThisIndex)
			sortAllv1();
	}
	
	/** Vérifie si une colonne est indexée par cet index
	 *  @param colIndex
	 *  @return
	 */
	public boolean hasColumnIndex(int colIndex) {
		for (int iCol : colIndexArray) {
			if (iCol == colIndex) return true;
		}
		return false;
	}
	
	/** Suppression d'une colonne de la table : répercussion sur les index
	 *  @param colIndex
	 *  @return
	 */
	public void columnWasDeletedFromTable(int colIndex) {
		for (int i = 0; i < colIndexArray.length; i++) {
			if (colIndexArray[i] > colIndex)
				colIndexArray[i]--;
		}
	}
	
	/** Mise à jour de l'index avec les données des colonnes,
	 *  nécessaire lors de l'ajout de nouvelles lignes.
	 *  
	 *  Ça pourrait être beaucoup plus optimisé avec un système de chunks, mais je n'ai malheureusement plus assez de temps.
	*/
	public void refreshIndexWithColumnsData(boolean beSuperVerbose) {
		
		beSuperVerbose = (beSuperVerbose && enableVerboseSort);
		
		if (beSuperVerbose) MemUsage.printMemUsage();
		Timer t1, t2, t3, t4;
		
		t1 = new Timer("IndexMemDic.sortAll - tout :");
		t2 = new Timer("IndexMemDic.sortAll - création objets :");
		
		if (beSuperVerbose) MemUsage.printMemUsage();
		
		int numberOfStillPersentLines = table.getTotalNumberOfLines();
		int lastLoadedLineIndexLength = table.getLastLoadedLineIndexLength();
		
		if (notBeenSortedYet) totalLength = 0;
		notBeenSortedYet = false; // <- plus vraiment nécessaire mais je laisse quand-même ^^
		// Déjà, j'ajoute les lignes que j'ai en mémoire, si elles sont toujours présentes
		if (sortedPositions.length < totalLength) // correctif
			totalLength = sortedPositions.length;
		int oldTotalLength = totalLength;
		totalLength = numberOfStillPersentLines;
		// Ici, je suppose que la seule différence avec l'état précédent est
		// que des lignes ont été ajoutées et des lignes ont été flag.
		// Pas de restructuration profonde de la base et des données, donc.
		// Il y a donc toujours totalLength >= oldTotalLength.
		
		IndexMemDicTemporaryItem[] tempSortArray = new IndexMemDicTemporaryItem[numberOfStillPersentLines];
		int indexInTempSortArray = 0;
		
		// Pour chaque ligne déjà dans l'index, je regarde si elle existe toujours
		for (int iLine = 0; iLine < oldTotalLength; iLine++) {
			int linePositionInTable = sortedPositions[iLine];
			boolean stillPresent = table.getLineFlag(linePositionInTable);
			if (stillPresent) {
				tempSortArray[indexInTempSortArray] = new IndexMemDicTemporaryItem(linePositionInTable);
				indexInTempSortArray++;
			}
		}
		// Ajout des nouvelles lignes
		for (int iLine = oldTotalLength; iLine < lastLoadedLineIndexLength; iLine++) {
			
			boolean stillPresent = table.getLineFlag(iLine);
			if (stillPresent) {
				tempSortArray[indexInTempSortArray] = new IndexMemDicTemporaryItem(iLine);
				indexInTempSortArray++;
			}
			
		}
		
		if (indexInTempSortArray != numberOfStillPersentLines) {
			Log.error("Erreur lors de la restructuration de l'index : indexInTempSortArray("+indexInTempSortArray+") != numberOfStillPersentLines("+numberOfStillPersentLines+")");
		}
		
		if (beSuperVerbose) MemUsage.printMemUsage();
		if (enableVerboseSort) t2.log();
		
		// ABSOLUMENT nécessaire pour le sort : avoir staticIndexOnThisColArray en static. 
		IndexMemDicAncester.staticIndexOnThisColArray = indexOnThisColArray;
		
		t3 = new Timer("IndexMemDic.refreshIndexWithColumnsData - sort :");
		if (beSuperVerbose) MemUsage.printMemUsage();
		//Arrays.sort(tempSortArray);
		//SCustomSort.sort(tempSortArray);
		Arrays.parallelSort(tempSortArray); // <- faible gain, environ 30% plus rapide, mais prend beaucoup plus de mémoire
		if (enableVerboseSort) t3.log();
		if (beSuperVerbose) MemUsage.printMemUsage();
		
		/*Timer t4_2 = new Timer("IndexMemDic.sortAll - deteteAtPosition 0 :");
		for (int i = 0; i < 20; i++)
			deleteAtPosition(0);
		t4_2.log();*/
		
		
		t4 = new Timer("IndexMemDic.refreshIndexWithColumnsData - réagencement positions :");
		for (int i = 0; i < totalLength; i++) {
			
			sortedPositions[i] = tempSortArray[i].originalLinePosition;
			//String displayValues = table.getLineAsReadableString(sortedPositions[i]);
			//Log.info(displayValues);
		}
		if (beSuperVerbose) MemUsage.printMemUsage();
		if (enableVerboseSort) t4.log();
		for (int i = 0; i < totalLength; i++) {
			tempSortArray[i] = null;
		}
		if (beSuperVerbose) MemUsage.printMemUsage();
		tempSortArray = null;
		if (enableVerboseSort) t1.log();
		
		//hasToCheckResultsFlags = false;
		
		
	}
	
	/*
	public void setPosition(int sortedByValueIndex, int lineOriginalPosition) {
		sortedPositions[sortedByValueIndex] = lineOriginalPosition;
	}*/
	
	public void enableFlagCheck(boolean enable) {
		hasToCheckResultsFlags = enable;
	}
	
	public void sortAllv1() {
		sortAllv1(false);
	}
	
	/** TODO
	 * 	Faire un benchmark de cette solution et de la solution avec l'index découpé par chunks
	 *  @param deletePos
	 */
	public void deleteAtPosition(int deletePos) {
		if (deletePos < 0) return;
		if (deletePos >= sortedPositionsRealLength) return;
		for (int rmPos = deletePos; rmPos <= sortedPositionsRealLength - 2; rmPos++) {
			sortedPositions[rmPos] = sortedPositions[rmPos + 1];
		}
		sortedPositionsRealLength--;
	}
	
	/** Trie les lignes en fonction des valeurs indexées
	 *  Java gère magnifiquement bien le tri, même pour 100 millions d'éléments ! (c'est assez incroyable ^^)
	 */
	public void sortAllv1(boolean beSuperVerbose) {
		if (notBeenSortedYet) totalLength = 0;
		refreshIndexWithColumnsData(beSuperVerbose);
		
		/*
		
		beSuperVerbose = (beSuperVerbose && enableVerboseSort);
		
		if (beSuperVerbose) MemUsage.printMemUsage();
		Timer t1, t2, t3, t4;
		
		t1 = new Timer("IndexMemDic.sortAll - tout :");
		t2 = new Timer("IndexMemDic.sortAll - création objets :");

		if (beSuperVerbose) MemUsage.printMemUsage();
		IndexMemDicTemporaryItem[] tempSortArray = new IndexMemDicTemporaryItem[totalLength];
		for (int i = 0; i < totalLength; i++) {
			tempSortArray[i] = new IndexMemDicTemporaryItem(i);
		}
		if (beSuperVerbose) MemUsage.printMemUsage();
		if (enableVerboseSort) t2.log();
		
		t3 = new Timer("IndexMemDic.sortAll - sort :");
		if (beSuperVerbose) MemUsage.printMemUsage();
		//Arrays.sort(tempSortArray);
		//SCustomSort.sort(tempSortArray);
		Arrays.parallelSort(tempSortArray); // <- faible gain, environ 30% plus rapide, mais prend beaucoup plus de mémoire
		if (enableVerboseSort) t3.log();
		if (beSuperVerbose) MemUsage.printMemUsage();
		
		/*Timer t4_2 = new Timer("IndexMemDic.sortAll - deteteAtPosition 0 :");
		for (int i = 0; i < 20; i++)
			deleteAtPosition(0);
		t4_2.log();* /
		
		
		t4 = new Timer("IndexMemDic.sortAll - réagencement positions :");
		for (int i = 0; i < totalLength; i++) {
			
			sortedPositions[i] = tempSortArray[i].originalLinePosition;
			//String displayValues = table.getLineAsReadableString(sortedPositions[i]);
			//Log.info(displayValues);
		}
		if (beSuperVerbose) MemUsage.printMemUsage();
		//if (enableVerboseSort) 
		t4.log();
		for (int i = 0; i < totalLength; i++) {
			tempSortArray[i] = null;
		}
		if (beSuperVerbose) MemUsage.printMemUsage();
		tempSortArray = null;
		if (enableVerboseSort) t1.log();
		*/
	}
	
	//findMatchingIndex
	
	private int[] findMatchingIntervalBounds(ByteBuffer searchQuery) { //findClosestLinePosition
		int startIndex = findMatchingIndex(searchQuery, true);
		if (startIndex == -1) return new int[] {-1, -1};
		int stopIndex = findMatchingIndex(searchQuery, false);
		if (stopIndex == -1) return new int[] {-1, -1};
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
		int dicStopIndex = sortedPositionsRealLength - 1;
		int intervalLength = dicStopIndex - dicStartIndex + 1;
		int dicCurrentIndex = (dicStopIndex - dicStartIndex) / 2;
		
		/*
		for (int i = 0; i < sortedPositionsRealLength; i++) {
			String line = table.getLineAsReadableString(sortedPositions[i]);
			Log.info(line);
		}*/
		
		if (useSafeSlowComparaisonsNotDichotomy) {
			for (int i = 0; i < sortedPositionsRealLength; i++) {
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
				
				if (dicStopIndex >= sortedPositionsRealLength) Log.error("dicStopIndex trop grand");
				if (dicStopIndex < 0) Log.error("dicStopIndex trop petit");
				if (dicStartIndex >= sortedPositionsRealLength) Log.error("dicStartIndex trop grand");
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
		while ( (exactValueStartIndexDic <= sortedPositionsRealLength - 2) && (compareLineValuesAndQuery(sortedPositions[exactValueStopIndexDic + 1], searchQuery) == 0) ) {
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

		if (sortedPositions == null) return -1;
		if (sortedPositions.length == 0) return -1;
		
		int dicStartIndex = 0;
		int dicStopIndex = sortedPositionsRealLength - 1;
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
					
					if (dicCurrentIndex == (sortedPositionsRealLength - 1)) break;
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
			
			if (dicStopIndex >= sortedPositionsRealLength) {
				Log.error("dicStopIndex trop grand");
				return -1;
			}
			if (dicStopIndex < 0) {
				Log.error("dicStopIndex trop petit");
				return -1;
			}
			if (dicStartIndex >= sortedPositionsRealLength) {
				Log.error("dicStartIndex trop grand");
				return -1;
			}
			if (dicStartIndex < 0) {
				Log.error("dicStartIndex trop petit");
				return -1;
			}
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
	 *  @param searchQuery
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
	
	
	
	/** Pour alléger l'écriture
	 *  @param warnIfWrongArgumentType
	 *  @param value
	 *  @param neededClass
	 *  @return
	 */
	private boolean findMatchingLinePositions_checkValueType(boolean warnIfWrongArgumentType, Object value, @SuppressWarnings("rawtypes") Class neededClass) {
		if (warnIfWrongArgumentType == false) return true; // aucune vérif à faire
		if (value.getClass() != neededClass) { // mauvaise classe : cast refusé
			Log.error("Mauvais type de valeur entré : value.getClass()=[" + value.getClass() + "]  !=  [" + neededClass + "]=neededClass");
			return false;
		}
		return true; // bonne classe, pas de cast à effectuer, c'est OK !
	}
	
	/** Recherche à partir d'une liste d'objets
	 * @param queryList la liste d'objets
	 * @param warnIfWrongArgumentType
	 * @return  null en cas d'erreur, un int[] contenant les position des lignes coïncidant
	 */
	public int[] findMatchingLinePositions(List<Object> queryList, boolean warnIfWrongArgumentType) {
		// Si possible, cast des objets en ByteBuffer pour effectuer la requête
		
		if (queryList == null) {
			Log.error("searchQuery == null");
			return null; // aucune requête adressée
		}
		if (queryList.size() != indexOnThisColArray.length) {
			Log.error("Mauvaise taille de searchQuery : " + queryList.size() + " != " + indexOnThisColArray.length + "  (indexOnThisColArray.length)");
			return null;
		}
		

		// 1) Cast des objets
		ByteBuffer queryBuffer = ByteBuffer.allocate(200);
		
		for (int iColumnValue = 0; iColumnValue < queryList.size(); iColumnValue++) { // 
			Object value = queryList.get(iColumnValue);
			Column column = indexOnThisColArray[iColumnValue];
			
			// manière super bourrine de faire : 
			
			switch (column.dataTypeEnum) {
			case BYTE : // je dois caster en byte la valeur entrée
				if (value instanceof Number) {
					// Vérification du type d'objet
					if (findMatchingLinePositions_checkValueType(warnIfWrongArgumentType, value, Byte.class) == false) return null;
					// Cast de l'objet
					queryBuffer.put(((Number) value).byteValue());
				} else {
					Log.error("L'objet n'est pas un nombre. Cast en BYTE impossible, iColumnValue = " + iColumnValue);
					return null;
				}
				break;
			
			case INTEGER :
				if (value instanceof Number) {
					// Vérification du type d'objet
					if (findMatchingLinePositions_checkValueType(warnIfWrongArgumentType, value, Integer.class) == false) return null;
					// Cast de l'objet
					queryBuffer.putInt(((Number) value).intValue());
				} else {
					Log.error("L'objet n'est pas un nombre. Cast en INTEGER impossible, iColumnValue = " + iColumnValue);
					return null;
				}
				break;
			case LONG :
				if (value instanceof Number) {
					// Vérification du type d'objet
					if (findMatchingLinePositions_checkValueType(warnIfWrongArgumentType, value, Long.class) == false) return null;
					// Cast de l'objet
					queryBuffer.putLong(((Number) value).longValue());
				} else {
					Log.error("L'objet n'est pas un nombre. Cast en LONG impossible, iColumnValue = " + iColumnValue);
					return null;
				}
				break;
			case DATE :
				if (value instanceof Number) {
					// Vérification du type d'objet
					if (findMatchingLinePositions_checkValueType(warnIfWrongArgumentType, value, Integer.class) == false) return null;
					// Cast de l'objet
					queryBuffer.putInt(((Number) value).intValue());
				} else {
					Log.error("L'objet n'est pas un nombre. Cast en INTEGER (DATE) impossible, iColumnValue = " + iColumnValue);
					return null;
				}
				break;
			case FLOAT :
				if (value instanceof Number) {
					// Vérification du type d'objet
					if (findMatchingLinePositions_checkValueType(warnIfWrongArgumentType, value, Float.class) == false) return null;
					// Cast de l'objet
					queryBuffer.putFloat(((Number) value).floatValue());
				} else {
					Log.error("L'objet n'est pas un nombre. Cast en FLOAT impossible, iColumnValue = " + iColumnValue);
					return null;
				}
				break;
			case DOUBLE :
				if (value instanceof Number) {
					// Vérification du type d'objet
					if (findMatchingLinePositions_checkValueType(warnIfWrongArgumentType, value, Double.class) == false) return null;
					// Cast de l'objet
					queryBuffer.putDouble(((Number) value).doubleValue());
				} else {
					Log.error("L'objet n'est pas un nombre. Cast en DOUBLE impossible, iColumnValue = " + iColumnValue);
					return null;
				}
				break;
			case STRING :
				if (value.getClass() == String.class) {
					// -> il est nécessaire d'avoir un String de la bonne taille
					byte[] stringAsAjustedBytes = StringType.stringToAjustedByteArray((String)value, column.dataSizeInBytes);
					queryBuffer.put(stringAsAjustedBytes);
				} else {
					Log.error("L'objet n'est pas un String. iColumnValue = " + iColumnValue);
					return null;
				}
				break;
			case UNKNOWN:
				Log.error("La colonne a un type INCONNU. iColumnValue = " + iColumnValue);
				return null;
			}
		}
		
		
		// Ici, queryBuffer a été créé à partir de queryList
		return findMatchingLinePositions(queryBuffer);
		
	}
	
	/** 
	 *  @param searchQuery
	 *  @return
	 */
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
		
		if (hasToCheckResultsFlags) {
			int[] originalLinePositionArray = new int[intervalLength];
			int realResultCount = 0;
			for (int iRes = 0; iRes < intervalLength; iRes++) {
				int linePositionInTable = sortedPositions[iRes + startIndex];
				boolean stillPresent = table.getLineFlag(linePositionInTable);
				if (stillPresent) {
					originalLinePositionArray[realResultCount] = linePositionInTable;
					realResultCount++;
				}
			}
			
			int[] originalLinePositionArray_realSize = new int[realResultCount];
			for (int iRes = 0; iRes < realResultCount; iRes++) {
				originalLinePositionArray_realSize[iRes] = sortedPositions[iRes + startIndex];
			}
			
			return originalLinePositionArray_realSize;
		} else {
			
			int[] originalLinePositionArray_realSize = new int[intervalLength];
			for (int iRes = 0; iRes < intervalLength; iRes++) {
				originalLinePositionArray_realSize[iRes] = sortedPositions[iRes + startIndex];
			}
			return originalLinePositionArray_realSize;
		}
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

	@Override
	public int[] getIdsFromPredicate(Predicate predicate) throws IndexException {
		List<Object> values = new ArrayList<Object>(Arrays.asList(predicate.getValue()));
		switch (predicate.getOperator()) {
			case equals:
				return this.findMatchingLinePositions(values, false);
			default:
				throw new IndexException("invalid operator");
		}
	}
}
