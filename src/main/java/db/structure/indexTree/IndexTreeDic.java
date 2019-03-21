package db.structure.indexTree;

import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.dant.utils.EasyFile;
import com.dant.utils.Log;
import com.dant.utils.MemUsage;
import com.dant.utils.Timer;
import com.dant.utils.Utils;

import db.data.DataType;
import db.data.IntegerArrayList;
import db.structure.Column;
import db.structure.Index;
import db.structure.Table;

/**
 * Exploite une extraordinaire propritété des TreeMap (arbre rouge-noir) :
 * Les résultats sont classés par ordre croissants.
 * Il est donc possible de faire des recherches rapides par dichotomie,
 * et se baser sur une implémentation simple via un TreeMap.
 */

/***
 * 
 * ~ Code de Sylvain, idée (dichotomie) de Nicolas ~
 * 
 * Bench de IndexTreeDic vs IndexTreeCeption pour mesurer les performances des deux méthodes
 * 
 * Indexer via un TreeMap, et une recherche par dichotomie sur le disque.
 * Le TreeMap contenant les valeurs donnera une collection classée par ordre croissant de ses clefs,
 * ce sera donc facile de l'écrire sur le disque.
 * 
 * 1) Ecrire chaque IntegerArrayList sur le disque, et retenir le binIndex
 * 2) Ecrire la table d'index, retenir la position de la table, écrire : nombre d'IntegerArrayList (=nb valeurs différentes),
 *    puis pour chaque IntegerArrayList : valeur (clef) + binIndex (4 ou 8 octets)
 * 
 * -> Cette méthode est significativement plus simple à développer que les IndexTreeCeption,
 *    reste à en évaluer les performances sur les données des Taxis de New York.
 * 
 * Important, développement futur :
 * -> Améliorer le CsvParser pour qu'il stocke les colonnes indépendament, ou qu'il "split" les fichiers tous les 1_000_000 de données
 * -> Pour chaque colonne, sauvegarder 
 * 
 * Il faut un score pour classer les colonnes/index, prendre, pour la première requete, l'Index associé ayant le score le plus élevé :
 * -> nombre de valeurs différentes dans l'arbre, pour tenter de limiter un maximum les résultats.
 * 
 */

public class IndexTreeDic extends Index {
	
	/**
	 * Il y a donc une seule TreeMap pour ce type d'index (contrairement à IndexTreeCeption)
	 * 
	 * 
	 */
	public int flushOnDiskOnceReachedThisFileNumber = 2_000_000;
	public boolean useMultithreadSearch = true;
	public boolean showMemUsageAtEachFlush = true;
	public boolean forceGarbageCollectorAtEachFlush = false;
	
	// Contient tous les index des données indexées
	protected TreeMap<Object/*clef, valeur indexée*/, IntegerArrayList/*valeur*/> associatedBinIndexes = new TreeMap<Object, IntegerArrayList>();
	//protected EasyFile fileStoringDataBlocks; // link between the disk and onDiskDataBlocks
	protected EasyFile fileSaveOnDisk = null;
	protected String currentSaveFilePath = null;
	protected static String basePath = "target/IndexTreeDic_DiskMemory/";
	protected static int rootIndexTreeCount = 0;
	
	// Thread-safe
	protected static AtomicInteger nextIndexTreeDicUniqueId = new AtomicInteger(1);
	protected int indexTreeDicUniqueId; // id unique de cet index
	protected final String baseSaveFilePath, suffixSaveFilePath;
	protected int uniqueFileIdForThisTree = 1;

	// storedValuesClassType et storedValueDataByteSize sont définis dans indexColumnFromDisk(...)
	// -> c'est à dire au moment d'indexer une colonne
	@SuppressWarnings("rawtypes")
	protected Class storedValuesClassType;
	protected int storedValueDataByteSize; // nombre d'octets pris par chaque valeur (associée à chaque IntegerArrayList)
	
	
	public IndexTreeDic() {//(Class argStoredValuesClassType) {
		
		indexTreeDicUniqueId = nextIndexTreeDicUniqueId.addAndGet(1);
		baseSaveFilePath = basePath + "IndexTreeDic_" + indexTreeDicUniqueId + "_"; // id arbre _ id(nombre) fichier
		suffixSaveFilePath = ".idc_bin";
		
		currentSaveFilePath = basePath + "IndexTreeDic_indexSave_" + indexTreeDicUniqueId + ".bin_tree";
		fileSaveOnDisk = new EasyFile(currentSaveFilePath);
		try {
			fileSaveOnDisk.createFileIfNotExist();
		} catch (IOException e) {
			fileSaveOnDisk = null;
			e.printStackTrace();
		}
		//storedValuesClassType = argStoredValuesClassType;
		rootIndexTreeCount++;
	}
	
	/*
	@SuppressWarnings("rawtypes")
	protected void setStoredValuesClassType(DataType argStoredValuesClassType) {
		//storedValuesClassType = argStoredValuesClassType;
		//storedValueDataByteSize
	}*/
	
	// Il ne peut pas y avoir de Append sur un fichier
	// Liste des noms de fichiers sauvegardés sur le disque
	protected ArrayList<String> indexWrittenOnDiskFilePathsArray = new ArrayList<String>();
	protected int indexWrittenOnDiskFileCount = 0;
	//protected String indexWrittenOnDiskBasePath = "target/treeIndexDiskMemory/";
	
	/** Index a column and 
	 *  @param inTable
	 *  @param columnIndex
	 *  @throws FileNotFoundException 
	 */
	public void indexColumnFromDisk(Table inTable, int columnIndex) throws IOException {
		//indexedColumnsList = new Column[0];
		
		debug_AnnoyingTable = inTable;
		debug_AnnoyingColmnIndex = columnIndex;
		
		List<Column> columnsList = inTable.getColumns();
		int columnsNumber = columnsList.size();
		if (columnsNumber <= columnIndex) { // invalid columnIndex
			return;
		}
		
		// We need to find where the data is, on the disk.
		// Compute where to read from, on a line, and how many bytes to skip
		int dataOffsetInLine = 0;
		for (int tempColIndex = 0; tempColIndex < columnIndex; tempColIndex++) {
			Column tempColumn = columnsList.get(tempColIndex);
			dataOffsetInLine += tempColumn.getSize();
		}
		int totalLineSize = inTable.getLineSize(); // total size of a line
		
		// Information on the current column that we will index
		Column indexThisColumn = columnsList.get(columnIndex);
		DataType columnDataType = indexThisColumn.getDataType();
		
		storedValuesClassType = columnDataType.getAssociatedClassType();
		int dataSizeInBytes = columnDataType.getSize();
		storedValueDataByteSize = dataSizeInBytes;
		
		//indexedColumnsList = new Column[1]; // Currently, an IndexTree only supports one column
		//indexedColumnsList[0] = indexThisColumn;
		
		int skipBeforeData = dataOffsetInLine; // skip the first values
		int skipAfterData = totalLineSize - skipBeforeData - dataSizeInBytes; // skip the remaining values
		
		// Now, let's read the whole file and index the rows (=lines)...
		// That pretty non-optimised, but that's only V1
		//Log.info("skipBeforeData = " + skipBeforeData + " dataSizeInBytes = " + dataSizeInBytes + "  skipAfterData = " + skipAfterData, "SIndexingTree.indexColumnFromDisk");
		
		// Get a new disposable FileInputStream with the file where all table rows are stored
		FileInputStream fileAsStream = new FileInputStream(inTable.getFileLinesOnDisk());
		int lineIndex = 0;
		long currentBinPosition = 0;
		long fileSize = inTable.getFileLinesOnDisk().length();
		
		int inMemoryResults = 0;
		
		//Timer benchTime = new Timer("Temps pris par l'indexation");
		byte[] columnValueAsByteArray = new byte[dataSizeInBytes];
		while (currentBinPosition < fileSize) {
			
		//while (true) {
			
			/*
			 	Bench - performances (découpage) :
			 		Temps total avec tout :
			 		La gestion de la boucle : 2 ms 
			 		+ Les skip : 210 ms (un seul skip -> 70 ms, 3 skip -> 210 ms)
			 		+ Le read  : 340 ms
			 		+ Le cast  : 350 ms (columnDataType.getValueFromByteArray(columnValueAsByteArray);)
			 		+ addValue : 380 ms
				
			*/
			/*
			fileAsStream.skip(skipBeforeData);
			fileAsStream.skip(dataSizeInBytes);
			fileAsStream.skip(skipAfterData);
				-> Prend 210 ms
			
			fileAsStream.skip(skipBeforeData + dataSizeInBytes + skipAfterData);
				-> Prend 70 ms
			
			-> D'où la nécessité de faire des colonnes séparées ! (on réduit de BEAUCOUP le temps !)
			*/
			
			// Seeks to the right position in the stream
			//long checkSkipBytesAmount;
			fileAsStream.skip(skipBeforeData);
			//checkSkipBytesAmount = fileAsStream.skip(dataSizeInBytes);
			//int bytesRead = fileAsStream.read(columnValueAsByteArray); // reads from the stream
			

			//fileAsStream.skip(skipBeforeData + storedValueDataByteSize + skipAfterData);
			
			int bytesRead = fileAsStream.read(columnValueAsByteArray); // reads from the stream
			if (bytesRead == -1) // end of stream
				break;
			
			Object readValue = columnDataType.readIndexValue(columnValueAsByteArray);
			this.addValue(readValue, lineIndex); // new Integer() creating a new Integer is quite slow ><" (but the bottle neck really is I/O on disk)
			inMemoryResults++;
			
			if (inMemoryResults > flushOnDiskOnceReachedThisFileNumber) {
				flushOnDisk();
				if (forceGarbageCollectorAtEachFlush) System.gc();
				//Log.info("indexColumnFromDisk : SAVE ON DISK !!");
				inMemoryResults = 0;
			}
			
			/*
			 S'il y a trop de valeurs mises en mémoire, j'écris l'index sur le disque et je retiens le nom du fichier écrit.
			 
			 
			 
			 */
			
			fileAsStream.skip(skipAfterData);
			
			currentBinPosition += skipBeforeData + storedValueDataByteSize + skipAfterData;
			
			// Display some contents, debuging :
			//if (lineIndex % 10000 == 0) Log.info("lineIndex = " + lineIndex + " readValue = " + readValue);
			
			lineIndex++;
		}
		//benchTime.printms();
		
		fileAsStream.close();
		
		flushOnDisk();
	}
	
	
	
	/** Ajouter une valeur et un binIndex associé
	 *  @param associatedValue valeur indexée, ATTENTION : doit être du type du IndexTree utilisé (Integer, Float, Byte, Double, ...)
	 *  @param binIndex position (dans le fichier binaire global) de la donnée stockée dans la table
	 */
	public void addValue(Object argAssociatedValue, Integer binIndex) {
		
		// Je peux ajouter la donnée fine
		IntegerArrayList binIndexList = associatedBinIndexes.get(argAssociatedValue);
		if (binIndexList == null) {
			binIndexList = new IntegerArrayList();
			associatedBinIndexes.put(argAssociatedValue, binIndexList);
		}
		binIndexList.add(binIndex);
	}
	
	public boolean checkIfCompatibleObjectType(Object inputObject) {
		if (inputObject == null) return false;
		//return inputObject.getClass() == storedValuesClassType; <- moins clair, je trouve
		if (inputObject.getClass() != storedValuesClassType) return false;
		return true;
	}
	
	/** Only gets the matching binIndexes from memory, not from stored data on disk
	 * 
	 * @param minValue
	 * @param maxValue
	 * @param isInclusive
	 * @return la collection contenant tous les binIndex correspondants
	 */
	public Collection<IntegerArrayList> findMatchingBinIndexesInMemory(Object minValueExact, Object maxValueExact, boolean isInclusive) { // NavigableMap<Integer, IntegerArrayList> findSubTree
		// arbre terminal : je retourne la liste des binIndex
		// binIndexesFromValue est non null ici, donc; et finerSubTrees est null
		//if (checkIfCompatibleObjectType(minValueExact) == false) return new ArrayList<IntegerArrayList>();
		//if (checkIfCompatibleObjectType(maxValueExact) == false) return new ArrayList<IntegerArrayList>();
		
		NavigableMap<Object, IntegerArrayList> subTree = associatedBinIndexes.subMap(minValueExact, isInclusive, maxValueExact, isInclusive);
		Collection<IntegerArrayList> collectionValues = subTree.values();
		return collectionValues;

	}
	
	int debugNumberOfExactArrayListValuesWrittenOnDisk = 0;
	int debugNumberOfExactValuesWrittenOnDisk = 0;
	
	
	public void flushOnDisk() throws IOException {
		if (associatedBinIndexes.size() == 0) return;
		
		String saveFileName = baseSaveFilePath + uniqueFileIdForThisTree + suffixSaveFilePath;
		uniqueFileIdForThisTree++;
		EasyFile fileInstance = new EasyFile(saveFileName);
		fileInstance.createFileIfNotExist();
		saveOnDisk(fileInstance, false);
		indexWrittenOnDiskFilePathsArray.add(saveFileName);
		//associatedBinIndexes = new TreeMap<Object, IntegerArrayList>(); // réinitialisation
		
		if (showMemUsageAtEachFlush) MemUsage.printMemUsage();
	}
	
	/** Ecrire l'index sur le disque
	 *  @param appendAtTheEnd   mettre à true pour écrire les données sur le disque
	 *  @throws IOException
	 */
	protected void saveOnDisk(EasyFile writeOnFile, boolean appendAtTheEnd) throws IOException {
		//fileSaveOnDisk
		DataOutputStream writeInDataStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(writeOnFile, appendAtTheEnd)));
		writeAllStoredDataOnDisk(writeInDataStream);
		writeInDataStream.close();
		//System.out.println("IndexTreeCeption.saveOnDisk : debugNumberOfExactArrayListValuesWrittenOnDisk=" + debugNumberOfExactArrayListValuesWrittenOnDisk);
		//System.out.println("IndexTreeCeption.saveOnDisk : debugNumberOfExactValuesWrittenOnDisk=" + debugNumberOfExactValuesWrittenOnDisk);
		
	}
	
	
	/** Ecrire les données sur le disque, d'une manière facile à retrouver
	 * 1) Ecrire toutes les IntegerArrayList sur le disque, en sauvegarder le binIndex
	 * 2) Ecrire le tableau des associations valeur - binIndex, retenir le binIndex de ce tableau (table de routage, si on veut)
	 * 3) Ecrire le binIndex de la table de routage à la fin du fichier 
	 * @param writeInDataStream
	 * @throws IOException
	 */
	public void writeAllStoredDataOnDisk(DataOutputStream writeInDataStream) throws IOException { // SubTrees
		
		// 1) Ecriture des données
		int totalNumberOfDistinctValues = associatedBinIndexes.size();
		
		long[] rememberedBinPosOfIntegerArrayLists = new long[totalNumberOfDistinctValues];
		int currentIntegerArrayListIndex = 0;
		
		// Ecriture de toutes les IntegerArrayList : nombre de binIndex, et pour chaque binIndex : binIndex (int)
		for (Entry<Object, IntegerArrayList> currentEntry : associatedBinIndexes.entrySet()) {
			//Object ent.getKey()
			debugNumberOfExactArrayListValuesWrittenOnDisk++;
			rememberedBinPosOfIntegerArrayLists[currentIntegerArrayListIndex] = writeInDataStream.size();
			
			IntegerArrayList binIndexesList = currentEntry.getValue(); // liste des binIndex associés à la clef (Object)
			int binIndexTotalCount = binIndexesList.size();
			writeInDataStream.writeInt(binIndexTotalCount); // nombre de binIndex associés à la valeur
			
			for (int binIndexCount = 0; binIndexCount < binIndexTotalCount; binIndexCount++) {
				int binIndex = binIndexesList.get(binIndexCount);
				writeInDataStream.writeInt(binIndex); // binIndex
				debugNumberOfExactValuesWrittenOnDisk++;
			}
			currentIntegerArrayListIndex++;
		}
		
		// Ecriture de toutes les valeurs de l'arbre et des binIndex (dans ce fichier) associés
		long routingTableBinIndex = writeInDataStream.size();
		writeInDataStream.writeInt(totalNumberOfDistinctValues); // taille de la table
		currentIntegerArrayListIndex = 0;
		for (Object associatedValue : associatedBinIndexes.keySet()) { // associatedValue = valeur associée à la IntegerArrayList de binIndex
			long associatedBinIndex = rememberedBinPosOfIntegerArrayLists[currentIntegerArrayListIndex];
			//System.out.println("currentEntry["+currentIntegerArrayListIndex+"] = " + associatedValue);
			writeObjectValueOnDisk(associatedValue, writeInDataStream); // écriture de la valeur
			writeInDataStream.writeInt((int) associatedBinIndex);
			currentIntegerArrayListIndex++;
		}
		
		// Ecriture de la position de la table d'index
		writeInDataStream.writeLong(routingTableBinIndex);
		
	}
	
	
	public static int debugDiskNumberOfIntegerArrayList = 0;
	public static int debugDiskNumberOfExactValuesEvaluated = 0;
	
	
	
	protected class IndexTreeDic_localDichotomyResult {
		public final boolean success;// = false;
		public final int keyIndex;// = -1;
		public final int binIndex;// = -1;
		
		// success == false, juste pour plus de lisibilité
		public IndexTreeDic_localDichotomyResult() {
			success = false;
			keyIndex = -1;
			binIndex = -1;
		}
		
		public IndexTreeDic_localDichotomyResult(boolean argSuccess, int argKeyIndex, int argBinIndex) {
			success = argSuccess;
			keyIndex = argKeyIndex;
			binIndex = argBinIndex;
			
		}
		
	}
	
	
	/** Trouve la position de la valeur immédiatement supérieure ou égale à la valeur recherchée : searchValue
	 *  @param searchValue
	 *  @param randFile
	 *  @param routingTableBinIndex
	 *  @param findLeftValue   true si prendre la valeur approchée la plus petite, false si prendre la plus grande (s'il n'y a pas égalité avec les valeurs trouvées)
	 *  @return true si la valeur recherchée est valide, (utilise getTheSmallestApprochingValue)
	 *  @throws IOException
	 */
	protected IndexTreeDic_localDichotomyResult findValueIndexByDichotomy(Object searchValue, RandomAccessFile randFile, long routingTableBinIndex, boolean findLeftValue) throws IOException {
		IndexTreeDic_localDichotomyResult localResultAsFalse = new IndexTreeDic_localDichotomyResult();
		
		randFile.seek(routingTableBinIndex);
		int totalNumberOfDistinctValues = randFile.readInt();
		if (totalNumberOfDistinctValues <= 0) return localResultAsFalse;
		
		int diskEntrySize = storedValueDataByteSize + 4; // nombre d'octets pris par chaque entrée (valeur + binIndex)
		
		int intervalStartIndex = 0;
		int intervalStopIndex = totalNumberOfDistinctValues - 1;
		int intervalLength = totalNumberOfDistinctValues; //intervalStopIndex - intervalStartIndex + 1;
		
		// Je recherche la valeur la plus proche
		Object lastApprochingValue = null;
		while (intervalLength > 1) {

			// Je me mets au milieu de l'intervalle
			int currentEntryIndex = intervalStartIndex + intervalLength / 2;
			
			// Cas de la valeur recherchée inférieure à la première valeur connue
			if (currentEntryIndex < 0) {
				currentEntryIndex = 0;
				intervalStartIndex = currentEntryIndex;
				break;
			}
			
			// Cas de la valeur recherchée supérieure à la dernière valeur connue
			if (currentEntryIndex >= totalNumberOfDistinctValues) {
				currentEntryIndex = totalNumberOfDistinctValues - 1;
				intervalStartIndex = currentEntryIndex;
				break;
			}
			
			// binPos de la valeur à lire
			int currentEntryBinPos = ((int)routingTableBinIndex + 4) + currentEntryIndex * diskEntrySize;
			// seek à la bonne position
			randFile.seek(currentEntryBinPos);
			// Lecture de la valeur
			Object associatedValue = readObjectValueFromDisk(randFile, storedValuesClassType);
			
			// comparaison de la valeur lue avec la valeur recherchée
			int firstValueIsHigher = firstValueIsHigherThatSecondValue(searchValue, associatedValue);
			
			if (firstValueIsHigher < 0) { // searchValue plus petite que associatedValue (et toutes les valeurs précédentes ont été plus petites que searchValue)
				
				intervalStopIndex = currentEntryIndex - 1;
				
			} else if (firstValueIsHigher > 0) { // searchValue plus grande que associatedValue
				
				intervalStartIndex = currentEntryIndex + 1;
				
			} else { // if (firstValueIsHigher == 0)
				
				// Valeur exacte trouvée !
				intervalStartIndex = currentEntryIndex;
				intervalStopIndex = currentEntryIndex;
				break;
				
			}
			intervalLength = intervalStopIndex - intervalStartIndex + 1;
			/*if (intervalLength <= 1) {
				lastApprochingValue = associatedValue;
			}*/
			//Log.info("intervalLength = " + intervalLength + " associatedValue = " + associatedValue);
		}
		int maxIntervalValue = totalNumberOfDistinctValues - 1;
		int minIntervalValue = 0;
		// // Ne devrait JAMAIS arriver ->
		if (intervalStartIndex > maxIntervalValue) { Log.error("TARENTULE - erreur dichotomie 1 @IndexTreeDic.findValueIndexByDichotomy"); return localResultAsFalse; } // Ne devrait JAMAIS arriver
		if (intervalStartIndex < minIntervalValue) { Log.error("TARENTULE - erreur dichotomie 2 @IndexTreeDic.findValueIndexByDichotomy"); return localResultAsFalse; } // Ne devrait JAMAIS arriver
		
		// binPos de la valeur à lire
		int lastApprochingValueBinPos = ((int)routingTableBinIndex + 4) + intervalStartIndex * diskEntrySize;
		// seek à la bonne position
		randFile.seek(lastApprochingValueBinPos);
		// Lecture de la valeur
		lastApprochingValue = readObjectValueFromDisk(randFile, storedValuesClassType);
		
		//if (lastApprochingValue == null) return false;
		
		// Si l'index trouvé ne correspond pas exactement à la valeur recherchée,
		// je peux prendre la valeur inférieure la plus proche (si findLeftValue),
		// ou la valeur supérieure la plus proche (si findLeftValue == false).
		
		
		// ça maaaaarche !! Enmulti-thread aussi :)
		int firstValueIsHigher = firstValueIsHigherThatSecondValue(searchValue, lastApprochingValue);
		if (firstValueIsHigher != 0) { // pas égalité
			// Si la valeur recherchée est plus petite que la valeur trouvée, 
			// if (firstValueIsHigher == -1 && findLeftValue) : ok, la valeur recherchée est plus grande et je recherche la valeur plus petite
			
			// Si la valeur recherchée est plus petite que la valeur trouvée, et que je cherche la valeur de gauche
			if (firstValueIsHigher == -1 && findLeftValue) {
				intervalStartIndex--; // je bouge à gauche
				if (intervalStartIndex < 0)
						return localResultAsFalse;
				
				//int checkEntryBinPos = ((int)routingTableBinIndex + 4) + intervalStartIndex * diskEntrySize;
				Object checkValue = readObjectValueFromDisk(randFile, storedValuesClassType);
				int checkFirstValueIsHigher = firstValueIsHigherThatSecondValue(searchValue, checkValue);
				if (checkFirstValueIsHigher != 1) {
					Log.error("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNERREUR dichotomie (1), firstValueIsHigher=" + firstValueIsHigher + " et checkFirstValueIsHigher=" + checkFirstValueIsHigher);
					//Log.error("En théorie, je devrais avoir : checkValue < searchValue < lastApprochingValue");
					Log.error("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNMais j'ai : checkValue("+checkValue+") < searchValue("+searchValue+") < lastApprochingValue("+lastApprochingValue+")");
				} else {
					//Log.info("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAVérification dichotomie OK 1 !");
				}
			}
			
			// Si la valeur recherchée est plus grande que la valeur trouvée, et que je cherche la valeur de droite
			if (firstValueIsHigher == 1 && (findLeftValue == false)) {
				//int debugOriginalIndex = intervalStartIndex;
				intervalStartIndex++; // je bouge à droite
				if (intervalStartIndex >= totalNumberOfDistinctValues) {
					return localResultAsFalse; // impossible de trouver la valeur, elle est hors des valeurs connues
				}
				if (intervalStartIndex < 0) {
					return localResultAsFalse; // impossible de trouver la valeur, elle est hors des valeurs connues
				}
				
				// DEBUT C'est du débug, ça partira au prochain commit DEBUT
				int checkEntryBinPos = ((int)routingTableBinIndex + 4) + intervalStartIndex * diskEntrySize;
				randFile.seek(checkEntryBinPos);
				Object checkValue = readObjectValueFromDisk(randFile, storedValuesClassType);
				int checkFirstValueIsHigher = firstValueIsHigherThatSecondValue(searchValue, checkValue);
				if (checkFirstValueIsHigher != -1) {
					Log.error("ERREUR dichotomie (2), firstValueIsHigher=" + firstValueIsHigher + " et checkFirstValueIsHigher=" + checkFirstValueIsHigher);
					//Log.error("En théorie, je devrais avoir : checkValue < searchValue < lastApprochingValue");
					Log.error("Mais j'ai : checkValue("+checkValue+") < searchValue("+searchValue+") < lastApprochingValue("+lastApprochingValue+")");
					
					class showLocalValue {
						
						public showLocalValue(int indexInArray) throws IOException {
							if (indexInArray < 0) return;
							if (indexInArray >= totalNumberOfDistinctValues) return;
							int debugGoToThisIndex, debugGoToEntryPos;
							Object debugCheckValue;
							
							debugGoToThisIndex = indexInArray;
							debugGoToEntryPos = ((int)routingTableBinIndex + 4) + debugGoToThisIndex * diskEntrySize;
							randFile.seek(debugGoToEntryPos);
							debugCheckValue = readObjectValueFromDisk(randFile, storedValuesClassType);
							Log.error("@At position ["+(debugGoToThisIndex)+"] : checkValue("+debugCheckValue+")");
						}
					}
					
					// C'est du débug, ça partira au prochain commit
					//new showLocalValue(debugOriginalIndex);
					new showLocalValue(intervalStartIndex - 2);
					new showLocalValue(intervalStartIndex - 1);
					new showLocalValue(intervalStartIndex - 0);
					new showLocalValue(intervalStartIndex + 1);
					new showLocalValue(intervalStartIndex + 2);
					
					
					/*
					debugGoToThisIndex = intervalStartIndex;
					debugGoToEntryPos = ((int)routingTableBinIndex + 4) + debugGoToThisIndex * diskEntrySize;
					randFile.seek(debugGoToEntryPos);
					debugCheckValue = readObjectValueFromDisk(randFile, storedValuesClassType);
					Log.error("@At position ["+(debugGoToThisIndex)+"] : checkValue("+debugCheckValue+")");
					*/
					//Log.error("ERREUR dichotomie (2), firstValueIsHigher=" + firstValueIsHigher + " et checkFirstValueIsHigher=" + checkFirstValueIsHigher);
				} else {
					//Log.info("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEVérification dichotomie OK 2 !");
				}
				// FIN C'est du débug, ça partira au prochain commit FIN
			}
		}
		
		
		int binIndexOfIntegerArrayList = ((int)routingTableBinIndex + 4) + intervalStartIndex * diskEntrySize; //  binIndex de la table de routage + offset dans cette table (binIndex de l'entrée)
		int whereToFindAssociatedBinIndex = binIndexOfIntegerArrayList + storedValueDataByteSize; // la position où lire le binIndex de la donnée
		
		//randFile.seek(binIndexOfIntegerArrayList);
		//Object associatedValue = readObjectValueFromDisk(randFile, storedValuesClassType); // (débug) valeur associée
		
		// Je regarde si cette entrée est valide :
		/**
			Cas non valide :
			le min est après la valeur maximale
			le max est avant la valeur minimale
			
			Cas plus complexe :
			le min et le max sont compris dans l'intervalle des valeurs, mais aucun élement ne correspond
			-> 
		 */
		
		randFile.seek(whereToFindAssociatedBinIndex);
		//Log.info("IndexTreeDis.findValueIndexByDichotomy : intervalStartIndex = " + intervalStartIndex);
		int keyIndex = intervalStartIndex;
		int binIndex = randFile.readInt();
		//Log.info("IndexTreeDis.findValueIndexByDichotomy : associatedValue = " + associatedValue + "  asDate = " + Utils.dateFromSecInt((Integer)associatedValue));
		
		return new IndexTreeDic_localDichotomyResult(true, keyIndex, binIndex);
		
	}
	
	/*
	public boolean findValueIndexByDichotomy_saved(Object searchValue, RandomAccessFile randFile, long routingTableBinIndex, boolean getTheSmallestApprochingValue) throws IOException {
		randFile.seek(routingTableBinIndex);
		int totalNumberOfDistinctValues = randFile.readInt();
		if (totalNumberOfDistinctValues <= 0) return false;
		
		int diskEntrySize = storedValueDataByteSize + 4; // nombre d'octets pris par chaque entrée (valeur + binIndex)
		
		int intervalStartIndex = 0;
		int intervalStopIndex = totalNumberOfDistinctValues - 1;
		int intervalLength = totalNumberOfDistinctValues; //intervalStopIndex - intervalStartIndex + 1;
		
		Object lastApprochingValue = null;
		while (intervalLength > 1) {

			// Je me mets au milieu de l'intervalle
			int currentEntryIndex = intervalStartIndex + intervalLength / 2;
			
			// Cas de la valeur recherchée inférieure à la première valeur connue
			if (currentEntryIndex < 0) {
				currentEntryIndex = 0;
				intervalStartIndex = currentEntryIndex;
				break;
			}
			
			// Cas de la valeur recherchée supérieure à la dernière valeur connue
			if (currentEntryIndex >= totalNumberOfDistinctValues) {
				currentEntryIndex = totalNumberOfDistinctValues - 1;
				intervalStartIndex = currentEntryIndex;
				break;
			}
			
			// binPos de la valeur à lire
			int currentEntryBinPos = ((int)routingTableBinIndex + 4) + currentEntryIndex * diskEntrySize;
			// seek à la bonne position
			randFile.seek(currentEntryBinPos);
			// Lecture de la valeur
			Object associatedValue = readObjectValueFromDisk(randFile, storedValuesClassType);
			
			// comparaison de la valeur lue avec la valeur recherchée
			int firstValueIsHigher = firstValueIsHigherThatSecondValue(searchValue, associatedValue);
			
			if (firstValueIsHigher < 0) { // searchValue plus petite que associatedValue (et toutes les valeurs précédentes ont été plus petites que searchValue)
				
				intervalStopIndex = currentEntryIndex - 1;
				
			} else if (firstValueIsHigher > 0) { // searchValue plus grande que associatedValue
				
				intervalStartIndex = currentEntryIndex + 1;
				
			} else { // if (firstValueIsHigher == 0)
				
				// Valeur exacte trouvée !
				intervalStartIndex = currentEntryIndex;
				intervalStopIndex = currentEntryIndex;
				
			}
			intervalLength = intervalStopIndex - intervalStartIndex + 1;
			if (intervalLength <= 1) {
				lastApprochingValue = associatedValue;
			}
			//Log.info("intervalLength = " + intervalLength + " associatedValue = " + associatedValue);
		}
		if (lastApprochingValue == null) return false;
		
		// Si l'index trouvé ne correspond pas exactement à la valeur recherchée,
		// je peux prendre la valeur inférieure la plus proche,
		// ou la valeur supérieure la plus proche.
		
		
		// ça fait n'importe quoi. ><" 
		int compared = compareValues(searchValue, lastApprochingValue);
		if (compared != 0) {
			// Cas où je dois prendre la valeur inférieure la plus proche :
			if (getTheSmallestApprochingValue) {
				if (compared == -1) { // searchValue plus petite que lastApprochingValue, je veux searchValue plus grande que lastApprochingValue
					intervalStartIndex--;
					Log.info("getTheSmallestApprochingValue && compared == -1 =>  intervalStartIndex--");
				}
				// si compared == 1, i.e. searchValue est plus grande, c'est bon, je suis bien à la "plus petite valeur approchée"
			} else {
				// Cas où je dois prendre la valeur supérieure la plus proche :
				if (compared == 1) { // searchValue plus grande que lastApprochingValue, je veux searchValue plus petite que lastApprochingValue
					intervalStartIndex++;
					Log.info("not getTheSmallestApprochingValue && compared == 1 =>  intervalStartIndex--");
				}
			}
		}
		
		int maxIntervalValue = totalNumberOfDistinctValues - 1;
		int minIntervalValue = 0;
		if (intervalStartIndex > maxIntervalValue) { intervalStartIndex = maxIntervalValue; Log.info("CUCUHKJ TARENTULE"); }
		if (intervalStartIndex < minIntervalValue) { intervalStartIndex = minIntervalValue; Log.info("222 CUCUHKJ TARENTULE"); }
		
		int binIndexOfIntegerArrayList = ((int)routingTableBinIndex + 4) + intervalStartIndex * diskEntrySize; //  binIndex de la table de routage + offset dans cette table (binIndex de l'entrée)
		int whereToFindAssociatedBinIndex = binIndexOfIntegerArrayList + storedValueDataByteSize; // la position où lire le binIndex de la donnée
		
		randFile.seek(binIndexOfIntegerArrayList);
		Object associatedValue = readObjectValueFromDisk(randFile, storedValuesClassType); // (débug) valeur associée
		
		// Je regarde si cette entrée est valide :
		/**
			Cas non valide :
			le min est après la valeur maximale
			le max est avant la valeur minimale
			
			Cas plus complexe :
			le min et le max sont compris dans l'intervalle des valeurs, mais aucun élement ne correspond
			-> 
		 * /
		
		
		boolean findTheMinimum = ! getTheSmallestApprochingValue;
		// si je dois trouver la valeur approchée la plus faible (!= le plus grande)
		// la valeur n'est valide que si 
		// Si je dois trouver le minimum et que je suis à la dernière valeur connue
		if (findTheMinimum && intervalStartIndex == maxIntervalValue) { 
			int comp = compareValues(searchValue, associatedValue);
			// le minimum (valeur recherchée) est plus grand que la dernière valeur, c'est invalide.
			if (comp == 1) return false; 
		}

		// Si je dois trouver le maximum et que je suis à la première valeur connue
		if ((findTheMinimum == false) && intervalStartIndex == minIntervalValue) { 
			int comp = compareValues(searchValue, associatedValue);
			// le maximum (valeur recherchée) est plus petit que la première valeur, c'est invalide.
			if (comp == -1) return false; 
		}
		
		//randFile.seek(whereToFindAssociatedBinIndex);
		Log.info("IndexTreeDis.findValueIndexByDichotomy : intervalStartIndex = " + intervalStartIndex);
		findValueIndexBy_keyIndex = intervalStartIndex;
		findValueIndexBy_binIndex = randFile.readInt();
		//Log.info("IndexTreeDis.findValueIndexByDichotomy : associatedValue = " + associatedValue + "  asDate = " + Utils.dateFromSecInt((Integer)associatedValue));
		
		return true;
		
	}*/
	
	
	
	

	//protected int findValueIndexBy_keyIndex = -1;
	//protected int findValueIndexBy_binIndex = -1;
	public static Table debug_AnnoyingTable;
	public static int debug_AnnoyingColmnIndex;
	
	// V0 : je parcours tout, sans dichotomie
	/*public int findValueIndexBySimpleReading(Object searchValue, RandomAccessFile randFile, long routingTableBinIndex) throws IOException {
		randFile.seek(routingTableBinIndex);
		int totalNumberOfDistinctValues = randFile.readInt();
		
		for (int keyIndex = 0; keyIndex < totalNumberOfDistinctValues; keyIndex++) {
			Object associatedValue = readObjectValueFromDisk(randFile, storedValuesClassType); // lit et avance de storedValueDataByteSize octets
			debugDiskNumberOfExactValuesEvaluated++;
			int compared = compareValues(searchValue, associatedValue);
			if (compared <= 0) { // searchValue plus petite ou égale à associatedValue
				findValueIndexBy_keyIndex = keyIndex;
				findValueIndexBy_binIndex = randFile.readInt();
				return findValueIndexBy_keyIndex;
				//keyIndexOfMin = keyIndex; //- 1;  if (keyIndexOfMin < 0) keyIndexOfMin = 0;
				//minBinIndex = randFile.readInt();
				
			}
			randFile.skipBytes(4); // binIndex
		}
		if (totalNumberOfDistinctValues == 0) return 0;
		
		randFile.skipBytes(-4); // go à la position du dernier binIndex, pour le lire
		findValueIndexBy_keyIndex = totalNumberOfDistinctValues - 1;
		findValueIndexBy_binIndex = randFile.readInt();
		return findValueIndexBy_keyIndex;
	}*/
	
	/** Gets the matching results from disk !
	 *  
	 *  @param minValue
	 *  @param maxValue
	 *  @param isInclusive
	 *  @return la collection contenant tous les binIndex correspondants
	 * @throws IOException 
	 */
	public Collection<IntegerArrayList> findMatchingBinIndexesFromDisk(Object minValueExact, Object maxValueExact, boolean isInclusive) throws IOException { // NavigableMap<Integer, IntegerArrayList> findSubTree
		debugDiskNumberOfIntegerArrayList = 0;
		debugDiskNumberOfExactValuesEvaluated = 0;
		
		// Lecture du dernier float écrit dans le disque, i.e. de la position de la table de routage de l'arbre principal
		/**
			Optimiser : faire le moins de seek/skip possibles
		 */
		
		class SearchInFile implements Runnable {
			
			private final String filePath;
			
			public ArrayList<IntegerArrayList> listOfLocalMatchingArraysOfBinIndexes = new ArrayList<IntegerArrayList>();
			
			public SearchInFile(String argFilePath) throws IOException {
				filePath = argFilePath;
			}
			
			// IKKI ICI
			
			public void doSearch() throws IOException {
				
				//long fileSize = fileSaveOnDisk.length();
				RandomAccessFile randFile = new RandomAccessFile(filePath, "r");
				long fileSize = randFile.length();
				//System.out.println("IndexTreeDic.loadFromDisk : fileSize = " + fileSize + "currentSaveFilePath = " + currentSaveFilePath);
				randFile.seek(fileSize - 8);
				
				long routingTableBinIndex = randFile.readLong();
				
				randFile.seek(routingTableBinIndex);
				int totalNumberOfDistinctValues = randFile.readInt();
				
				
				// Lecture de la table de routage
				
				// Pas besoin de récursvité pour l'arbre avec dichotomie
				/** 1) Je cherche le binIndex de la valeur minimale, puis de la valeur maximale
				 *  2) Je vais à tous les index, je mets les valeurs en mémoire.
				 */
				long minBinIndex = -1;
				//long maxBinIndex = -1;
				int keyIndexOfMin = -1;
				int keyIndexOfMax = -1;
				//inutile int integerArrayListLoadCount = 0; // nombre de listes à charger en mémoire (optimisé de faire ainsi)
				// Je recherche le premier index : la valeur antérieure doit être inférieure à minValue, et la valeur actuelle supérieure ou égale à minValue
				
				
				
				
				// Trouver le minimum
				//findValueIndexBySimpleReading(minValueExact, randFile, routingTableBinIndex);
				IndexTreeDic_localDichotomyResult dichotomyResult;
				dichotomyResult = findValueIndexByDichotomy(minValueExact, randFile, routingTableBinIndex, false); // false : valeur approchée la plus grande
				if (dichotomyResult.success == false) {
					randFile.close();
					return;
				}
				keyIndexOfMin = dichotomyResult.keyIndex;
				minBinIndex   = dichotomyResult.binIndex;
				
				//Log.info("IndexTreeDec.findMatchingBinIndexesFromDisk : ");
				//if (findValueIndexBy_keyIndex != keyIndexOfMin) Log.info("ERRRRRRRRRRRRRRRRRRRR findValueIndexBy_keyIndex("+findValueIndexBy_keyIndex+") != keyIndexOfMin("+keyIndexOfMin+")");
				//if (findValueIndexBy_binIndex != minBinIndex) Log.info("ERRRRRRRRRRRRRRRRRRRR findValueIndexBy_binIndex("+findValueIndexBy_binIndex+") != minBinIndex("+minBinIndex+")");
				
				// Trouver le maximum
				dichotomyResult = findValueIndexByDichotomy(maxValueExact, randFile, routingTableBinIndex, true); // true : valeur approchée la plus petite
				if (dichotomyResult.success == false) {
					randFile.close();
					return;
				}
				keyIndexOfMax = dichotomyResult.keyIndex;
				// inutile maxBinIndex   = findValueIndexBy_binIndex;
				
				// DEBUG à garder !
				//Log.info("IndexTreeDic.findMatchingBinIndexesFromDisk : keyIndexOfMin = " + keyIndexOfMin + "  keyIndexOfMax = " + keyIndexOfMax);
				//Log.info("IndexTreeDic.findMatchingBinIndexesFromDisk : minBinIndex = " + minBinIndex + "  maxBinIndex = " + maxBinIndex);
				//Log.info("IndexTreeDic.findMatchingBinIndexesFromDisk : minValueExact = " + minValueExact + "  maxValueExact = " + maxValueExact);
				//Log.info("IndexTreeDic.findMatchingBinIndexesFromDisk : AsDate :");
				//Log.info("IndexTreeDic.findMatchingBinIndexesFromDisk : begin = " + Utils.dateFromSecInt((Integer)minValueExact) + "  end = " + Utils.dateFromSecInt((Integer)maxValueExact));
				
				
				/*
				if (keyIndexOfMax == -1 && keyIndexOfMin != -1) {
					keyIndexOfMax = totalNumberOfDistinctValues - 1;
				}*/
				
				int integerArrayListTotalCount = keyIndexOfMax - keyIndexOfMin + 1;
				//Log.info("IndexTreeDic.findMatchingBinIndexesFromDisk : integerArrayListTotalCount = " + integerArrayListTotalCount);
				
				if (minBinIndex != -1) {
					randFile.seek(minBinIndex); // premier seek
					listOfLocalMatchingArraysOfBinIndexes.ensureCapacity(integerArrayListTotalCount);
					// Lecture de tous les IntegerArrayList de binIndex
					for (int integerArrayListCount = 0; integerArrayListCount < integerArrayListTotalCount; integerArrayListCount++) {
						int binIndexTotalCount = randFile.readInt(); // nombre de binIndex stockés
						IntegerArrayList binIndexesList = new IntegerArrayList();
						binIndexesList.ensureCapacity(binIndexTotalCount);
						for (int binIndexCout = 0; binIndexCout < binIndexTotalCount; binIndexCout++) {
							int binIndex = randFile.readInt();
							binIndexesList.add(binIndex);
						}
						listOfLocalMatchingArraysOfBinIndexes.add(binIndexesList);
					}
				}
				
				//searchInDiskData(randFile, listOfMatchingArraysOfBinIndexes, minValueExact, maxValueExact, isInclusive, currentHeight);
				
				randFile.close();
			}
			
			@Override
			public void run() {
				try {
					doSearch();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		// Ouvre tous les fichiers où les index sont sauvegardés (threads séparés), 
		
		Timer tempsPrisPourRecherchesSurFichiers = new Timer("tempsPrisPourRecherchesSurFichiers");
		ArrayList<IntegerArrayList> listOfMatchingArraysOfBinIndexes = new ArrayList<IntegerArrayList>();
		
		//Runnable searchInF
		ArrayList<SearchInFile> runnableSearchesList = new ArrayList<SearchInFile>();
		ArrayList<Thread> threadsRunningSearchesList = new ArrayList<Thread>();
		
		//int filesNb = indexWrittenOnDiskFilePathsArray.size();
		for (String filePath : indexWrittenOnDiskFilePathsArray) {
			SearchInFile searchInFile = new SearchInFile(filePath);
			runnableSearchesList.add(searchInFile);
		}
		
		for (SearchInFile searchInFile : runnableSearchesList) {
			
			if (useMultithreadSearch) {
				// Si Multi-thread :
				Thread newSearchThread = new Thread(searchInFile);
				threadsRunningSearchesList.add(newSearchThread);
				newSearchThread.start();
			} else {
				// Si Mono-thread :
				searchInFile.doSearch();
			}
			
		}
		
		// Version multi-thread (s'il y a des trhreads de lancés) : attent de la fin de toutes les tâches
		for (Thread searchThread : threadsRunningSearchesList) {
			try {
				searchThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		for (SearchInFile searchInFile : runnableSearchesList) {
			listOfMatchingArraysOfBinIndexes.addAll(searchInFile.listOfLocalMatchingArraysOfBinIndexes);
			
			/*
			if (searchInFile.listOfLocalMatchingArraysOfBinIndexes.size() != 0)
				Log.info("Recherche OK pour " + filePath + " dedans, j'ai trouvé : ");
			
			int numberOfResults = 0, numberOfLines = 0;
			for (IntegerArrayList list : searchInFile.listOfLocalMatchingArraysOfBinIndexes) {
				//Log.info("list size = " + list.size());
				numberOfResults += list.size();
				numberOfLines++;
				for (Integer index : list) {
					// un-comment those lines if you want to get the full info on lines : List<Object> objList = table.getValuesOfLineById(index);
					List<Object> objList = debug_AnnoyingTable.getValuesOfLineById(index);
					Object indexedValue = objList.get(debug_AnnoyingColmnIndex);
					Log.info("  index = " + index + "   val = " + indexedValue);
					
					//Log.info("  valeur indexée = " + indexedValue);
					//Log.info("  objList = " + objList);
					
				}
			}*/
			
		}
		tempsPrisPourRecherchesSurFichiers.logms();
		
		
		//saveFileName


		//listOfMatchingArraysOfBinIndexes.addAll(listOfLocalMatchingArraysOfBinIndexes);
		
		
		//System.out.println("IndexTreeDic.loadFromDisk : debugDiskNumberOfIntegerArrayList=" + debugDiskNumberOfIntegerArrayList);
		//System.out.println("IndexTreeDic.loadFromDisk : debugDiskNumberOfExactValuesEvaluated=" + debugDiskNumberOfExactValuesEvaluated);
		
		return listOfMatchingArraysOfBinIndexes;
	}
	
	/**
	 * 
	 * @param originalAssociatedValue
	 * @param writeInDataOutput 
	 * @throws IOException
	 */
	protected void writeObjectValueOnDisk(Object originalAssociatedValue, DataOutput writeInDataOutput) throws IOException  {
		
		if (originalAssociatedValue.getClass() == Float.class)    { writeInDataOutput.writeFloat(((Float) originalAssociatedValue).floatValue());    return; }
		if (originalAssociatedValue.getClass() == Double.class)   { writeInDataOutput.writeDouble(((Double) originalAssociatedValue).doubleValue()); return; }
		if (originalAssociatedValue.getClass() == Byte.class)     { writeInDataOutput.writeByte(((Byte) originalAssociatedValue).byteValue());       return; }
		if (originalAssociatedValue.getClass() == Integer.class)  { writeInDataOutput.writeInt(((Integer) originalAssociatedValue).intValue());      return; }
		if (originalAssociatedValue.getClass() == Long.class)     { writeInDataOutput.writeFloat(((Long) originalAssociatedValue).longValue());      return; }
		if (originalAssociatedValue.getClass() == String.class)   {
			byte[] stringAsByteAray = ((String)originalAssociatedValue).getBytes();
			writeInDataOutput.writeShort(stringAsByteAray.length); // taille du string, sur 2 octets
			writeInDataOutput.write(stringAsByteAray); // string en tant qu'array
			//Log.info("stringAsByteAray.length = " + stringAsByteAray.length + " originalAssociatedValue = " + originalAssociatedValue);
			return;
		}
	}
	
	/** Probably not very fast ><"
	 *  Même very very lent, indeed :'(
	 *  @param originalAssociatedValue
	 *  @return
	 * @throws IOException 
	 */
	/*@SuppressWarnings("rawtypes") // J'utilise Class pour rendre générique ma fonction
	protected Object readObjectValueFromDisk(DataInputStream dataClassType, Class dataClassType) throws IOException  {
		
		if (dataClassType == Float.class)    { return new Float(readFromDataStream.readFloat()); }// new Float() : il le fait tout seul et essaie d'optimiser le truc, je pense !
		if (dataClassType == Double.class)   { return new Double(readFromDataStream.readDouble()); }
		if (dataClassType == Byte.class)     { return new Byte(readFromDataStream.readByte()); }
		if (dataClassType == Integer.class)  { return new Integer(readFromDataStream.readInt()); }
		if (dataClassType == Long.class)     { return new Long(readFromDataStream.readLong()); }
		if (dataClassType == String.class)   {
			short stringAsByteArrayLength = readFromDataStream.readShort(); // 2 octets
			byte[] stringAsByteAray = new byte[stringAsByteArrayLength];
			int stringAsByteArrayCheckLength = readFromDataStream.read(stringAsByteAray, 0, stringAsByteAray.length);
			if (stringAsByteArrayLength != stringAsByteArrayCheckLength) throw new IOException("readObjectValueFromDisk : stringAsByteArrayLength("+stringAsByteArrayLength+") != stringAsByteArrayCheckLength("+stringAsByteArrayCheckLength+")");
			return new String(stringAsByteAray);
		}
		
		return null;
	}*/
	
	/**
	 * @param dataInput   randAccessFile ou dataClassType
	 * @param dataClassType   
	 *  
	 *  readFromDataStream
	 *  @return
	 * @throws IOException 
	 */
	@SuppressWarnings("rawtypes") // J'utilise Class pour rendre générique ma fonction
	protected Object readObjectValueFromDisk(DataInput dataInput, Class dataClassType) throws IOException  {
		
		if (dataClassType == Float.class)    { return new Float(dataInput.readFloat()); }// new Float() : il le fait tout seul et essaie d'optimiser le truc, je pense !
		if (dataClassType == Double.class)   { return new Double(dataInput.readDouble()); }
		if (dataClassType == Byte.class)     { return new Byte(dataInput.readByte()); }
		if (dataClassType == Integer.class)  { return new Integer(dataInput.readInt()); }
		if (dataClassType == Long.class)     { return new Long(dataInput.readLong()); }
		if (dataClassType == String.class)   {
			short stringAsByteArrayLength = dataInput.readShort(); // 2 octets
			byte[] stringAsByteAray = new byte[stringAsByteArrayLength];
			//int stringAsByteArrayCheckLength = 
			dataInput.readFully(stringAsByteAray, 0, stringAsByteAray.length);
			//if (stringAsByteArrayLength != stringAsByteArrayCheckLength) throw new IOException("readObjectValueFromDisk : stringAsByteArrayLength("+stringAsByteArrayLength+") != stringAsByteArrayCheckLength("+stringAsByteArrayCheckLength+")");
			return new String(stringAsByteAray);
		}
		
		return null;
	}
	
	/** Juste pour avoir un nom plus clair que compareValues,
	 * 	pour me faciliter le débug ^^'
	 *  @return   1 pour "true"   0 pour "égalité"   -1 pour "value1 < value2" (inférieur strict)
	 */
	public int firstValueIsHigherThatSecondValue(Object value1, Object value2) {
		if (value1 == null || value2 == null) return 0;
		if (value1.getClass() != value2.getClass()) return 0;
		
		if (value1 instanceof Number) {
			double asDouble1 = ((Number) value1).doubleValue(); // lent et pas opti ><"
			double asDouble2 = ((Number) value2).doubleValue(); // lent et pas opti ><"

			if (asDouble1 == asDouble2)  return 0;
			if (asDouble1 > asDouble2)   return 1;
	      /*if (asDouble1 < asDouble2)*/ return -1;
		}
		
		if (value1 instanceof String) {
			///String value1AsString
			// Faire une super-classe à IndexTreeDic et IndexTreeCeption, pour ne pas mettre de côté IndexTreeCeption
			String string1 = (String) value1;
			String string2 = (String) value2;
			int comparedValues = string1.compareTo(string2);
			if (comparedValues > 0) return 1;
			if (comparedValues < 0) return -1;
			return 0;
		}
		
		return 0;
	}
	
	// compareValues : nom pas clair !
	/*public int compareValues(Object value1, Object value2) {
		if (value1 == null || value2 == null) return 0;
		if (value1.getClass() != value2.getClass()) return 0;
		
		if (value1 instanceof Number) {
			double asDouble1 = ((Number) value1).doubleValue(); // lent et pas opti ><"
			double asDouble2 = ((Number) value2).doubleValue(); // lent et pas opti ><"

			if (asDouble1 == asDouble2) return 0;
			if (asDouble1 > asDouble2) return 1;
			return -1;
		}
		
		return 0;
		
		/*
		if (value1.getClass() == Float.class)   {
			if ((Float) value1 == (Float) value2) return 0;
			if ((Float) value1 > (Float) value2) return 1;
			return -1;
		}
		
		if (value1.getClass() == Double.class)   {
			if (((Double) value1) == (Double) value2) return 0;
			if ((Double) value1 > (Double) value2) return 1;
			return -1;
		}
		
		if (value1.getClass() == Double.class)   {
			if ((Double) value1 == (Double) value2) return 0;
			if ((Double) value1 > (Double) value2) return 1;
			return -1;
		}* /
		
	}*/
	
	
	
}
