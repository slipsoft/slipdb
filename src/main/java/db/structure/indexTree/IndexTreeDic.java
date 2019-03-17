package db.structure.indexTree;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
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

import com.dant.utils.EasyFile;
import com.dant.utils.Log;

import db.data.DataType;
import db.data.IntegerArrayList;
import db.structure.Column;
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
 * Bench de IndexTreeDic vs IndexTree pour mesurer les performances des deux méthodes
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
 */

public class IndexTreeDic {
	
	/**
	 * Il y a donc une seule TreeMap pour ce type d'index (contrairement à IndexTreeCeption)
	 * 
	 * 
	 */
	
	
	// Contient tous les index des données indexées
	protected TreeMap<Object/*clef, valeur indexée*/, IntegerArrayList/*valeur*/> associatedBinIndexes = new TreeMap<Object, IntegerArrayList>();
	//protected EasyFile fileStoringDataBlocks; // link between the disk and onDiskDataBlocks
	protected EasyFile fileSaveOnDisk = null;
	protected String currentSaveFilePath = null;
	protected static String basePath = "target/treeIndexDiskMemory/";
	protected static int rootIndexTreeCount = 0;
	

	@SuppressWarnings("rawtypes")
	protected Class storedValuesClassType;
	
	@SuppressWarnings("rawtypes")
	public IndexTreeDic(Class argStoredValuesClassType) {
		currentSaveFilePath = basePath + "IndexTreeDic_indexSave_" + rootIndexTreeCount + ".bin_tree";
		fileSaveOnDisk = new EasyFile(currentSaveFilePath);
		try {
			fileSaveOnDisk.createFileIfNotExist();
		} catch (IOException e) {
			fileSaveOnDisk = null;
			e.printStackTrace();
		}
		argStoredValuesClassType = storedValuesClassType;
		rootIndexTreeCount++;
	}
	
	
	/** Index a column and 
	 *  @param inTable
	 *  @param columnIndex
	 *  @throws FileNotFoundException 
	 */
	public void indexColumnFromDisk(Table inTable, int columnIndex) throws IOException {
		//indexedColumnsList = new Column[0];
		
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
		//long currentBinPosition = 0;
		//long fileSize = inTable.getFileLinesOnDisk().length();
		
		//Timer benchTime = new Timer("Temps pris par l'indexation");
		byte[] columnValueAsByteArray = new byte[dataSizeInBytes];
		//while (currentBinPosition < fileSize)
		while (true) {
			
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
			
			int bytesRead = fileAsStream.read(columnValueAsByteArray); // reads from the stream
			if (bytesRead == -1) // end of stream
				break;
			
			Object readValue = columnDataType.readIndexValue(columnValueAsByteArray);
			
			this.addValue(readValue, new Integer(lineIndex)); // creating a new Integer is quite slow ><" (but the bottle neck really is I/O on disk)
			
			fileAsStream.skip(skipAfterData);
			
			//currentBinPosition += skipBeforeData + dataSizeInBytes + skipAfterData;
			
			// Display some contents, debuging :
			//if (lineIndex % 10000 == 0) Log.info("lineIndex = " + lineIndex + " readValue = " + readValue);
			
			lineIndex++;
		}
		//benchTime.printms();
		
		fileAsStream.close();
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
		NavigableMap<Object, IntegerArrayList> subTree = associatedBinIndexes.subMap(minValueExact, isInclusive, maxValueExact, isInclusive);
		Collection<IntegerArrayList> collectionValues = subTree.values();
		return collectionValues;

	}
	
	int debugNumberOfExactArrayListValuesWrittenOnDisk = 0;
	int debugNumberOfExactValuesWrittenOnDisk = 0;
	
	public void saveOnDisk() throws IOException {
		
		DataOutputStream writeInDataStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileSaveOnDisk)));
		writeAllStoredDataOnDisk(writeInDataStream); // écriture de tous les arbres, du plus fin au plus grossier
		writeInDataStream.close();
		
		System.out.println("IndexTreeV3.saveOnDisk : debugNumberOfExactArrayListValuesWrittenOnDisk=" + debugNumberOfExactArrayListValuesWrittenOnDisk);
		System.out.println("IndexTreeV3.saveOnDisk : debugNumberOfExactValuesWrittenOnDisk=" + debugNumberOfExactValuesWrittenOnDisk);
		
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
	public static int debugDiskNumberOfExactValues = 0;
	
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
		debugDiskNumberOfExactValues = 0;
		
		// Lecture du dernier float écrit dans le disque, i.e. de la position de la table de routage de l'arbre principal
		/**
			Optimiser : faire le moins de seek/skip possibles
		 */
		
		
		//long fileSize = fileSaveOnDisk.length();
		RandomAccessFile randFile = new RandomAccessFile(currentSaveFilePath, "r");
		long fileSize = randFile.length();
		System.out.println("IndexTreeDic.loadFromDisk : fileSize = " + fileSize + "currentSaveFilePath = " + currentSaveFilePath);
		randFile.seek(fileSize - 8);
		
		long routingTableBinIndex = randFile.readLong();
		randFile.seek(routingTableBinIndex);
		
		ArrayList<IntegerArrayList> listOfMatchingArraysOfBinIndexes = new ArrayList<IntegerArrayList>();
		
		// Lecture de la table de routage
		int currentHeight = 0;
		
		// Pas besoin de récursvité pour l'arbre avec dichotomie
		/** 1) Je cherche le binIndex de la valeur minimale, puis de la valeur maximale
		 *  2) Je vais à tous les index, je mets les valeurs en mémoire.
		 */
		int totalNumberOfDistinctValues = randFile.readInt();
		long minBinIndex = -1;
		long maxBinIndex = -1;
		int keyIndexOfMin = -1;
		int keyIndexOfMax = -1;
		int integerArrayListLoadCount = 0; // nombre de listes à charger en mémoire (optimisé de faire ainsi)
		// Je recherche le premier index : la valeur antérieure doit être inférieure à minValue, et la valeur actuelle supérieure ou égale à minValue
		
		// V0 : je parcours tout, sans dichotomie
		// Trouver le minimum
		for (int keyIndex = 0; keyIndex < totalNumberOfDistinctValues; keyIndex++) {
			Object associatedValue = readObjectValueFromDisk(randFile, storedValuesClassType);
			
			int compared = compareValues(minValueExact, associatedValue);
			if (compared <= 0) { // minValue plus petite ou égale à associatedValue
				keyIndexOfMin = keyIndex; //- 1;  if (keyIndexOfMin < 0) keyIndexOfMin = 0;
				minBinIndex = randFile.readInt();
				break;
			}
			randFile.skipBytes(4); // binIndex
		}
		
		/*
		for (int keyIndex = 0; keyIndex < totalNumberOfDistinctValues; keyIndex++) {
			Object associatedValue = readObjectValueFromDisk(randFile, storedValuesClassType);
			System.out.println("associatedValue["+keyIndex+"] = " + associatedValue);
		}*/
		randFile.seek(routingTableBinIndex + 4);
		
		// Trouver le maximum
		for (int keyIndex = 0; keyIndex < totalNumberOfDistinctValues; keyIndex++) {
			Object associatedValue = readObjectValueFromDisk(randFile, storedValuesClassType);
			//System.out.println("currentEntry["+keyIndex+"] = " + associatedValue);
			
			int compared = compareValues(maxValueExact, associatedValue);
			if (compared < 0) { // maxValue plus petite que associatedValue
				keyIndexOfMax = keyIndex - 1;
				maxBinIndex = randFile.readInt();
				break;
			}
			randFile.skipBytes(4); // binIndex
		}
		Log.info("IndexTreeDic.findMatchingBinIndexesFromDisk : keyIndexOfMin = " + keyIndexOfMin + "  keyIndexOfMax = " + keyIndexOfMax);
		Log.info("IndexTreeDic.findMatchingBinIndexesFromDisk : minBinIndex = " + minBinIndex + "  maxBinIndex = " + maxBinIndex);
		Log.info("IndexTreeDic.findMatchingBinIndexesFromDisk : minValueExact = " + minValueExact + "  maxValueExact = " + maxValueExact);
		
		if (keyIndexOfMax == -1 && keyIndexOfMin != -1) {
			keyIndexOfMax = totalNumberOfDistinctValues - 1;
		}
		int integerArrayListTotalCount = keyIndexOfMax - keyIndexOfMin + 1;
		Log.info("IndexTreeDic.findMatchingBinIndexesFromDisk : integerArrayListTotalCount = " + integerArrayListTotalCount);
		
		if (minBinIndex != -1) {
			randFile.seek(minBinIndex); // premier seek
			listOfMatchingArraysOfBinIndexes.ensureCapacity(integerArrayListTotalCount);
			// Lecture de tous les IntegerArrayList de binIndex
			for (int integerArrayListCount = 0; integerArrayListCount < integerArrayListTotalCount; integerArrayListCount++) {
				int binIndexTotalCount = randFile.readInt(); // nombre de binIndex stockés
				IntegerArrayList binIndexesList = new IntegerArrayList();
				binIndexesList.ensureCapacity(binIndexTotalCount);
				for (int binIndexCout = 0; binIndexCout < binIndexTotalCount; binIndexCout++) {
					int binIndex = randFile.readInt();
					binIndexesList.add(binIndex);
				}
				listOfMatchingArraysOfBinIndexes.add(binIndexesList);
			}
		}
		
		//searchInDiskData(randFile, listOfMatchingArraysOfBinIndexes, minValueExact, maxValueExact, isInclusive, currentHeight);
		
		randFile.close();
		
		System.out.println("IndexTreeDic.loadFromDisk : debugDiskNumberOfIntegerArrayList=" + debugDiskNumberOfIntegerArrayList);
		System.out.println("IndexTreeDic.loadFromDisk : debugDiskNumberOfExactValues=" + debugDiskNumberOfExactValues);
		
		return listOfMatchingArraysOfBinIndexes;
	}
	
	/** Probably not very fast ><"
	 *  Même very very lent, indeed :'(
	 *  @param originalAssociatedValue
	 *  @return
	 * @throws IOException 
	 */
	protected void writeObjectValueOnDisk(Object originalAssociatedValue, DataOutputStream writeInDataStream) throws IOException  {
		
		if (originalAssociatedValue.getClass() == Float.class)    writeInDataStream.writeFloat(((Float) originalAssociatedValue).floatValue());
		if (originalAssociatedValue.getClass() == Double.class)   writeInDataStream.writeDouble(((Double) originalAssociatedValue).doubleValue());
		if (originalAssociatedValue.getClass() == Byte.class)     writeInDataStream.writeByte(((Byte) originalAssociatedValue).byteValue());
		if (originalAssociatedValue.getClass() == Integer.class)  writeInDataStream.writeInt(((Integer) originalAssociatedValue).intValue());
		if (originalAssociatedValue.getClass() == Long.class)     writeInDataStream.writeFloat(((Long) originalAssociatedValue).longValue());
		
	}
	
	/** Probably not very fast ><"
	 *  Même very very lent, indeed :'(
	 *  @param originalAssociatedValue
	 *  @return
	 * @throws IOException 
	 */
	@SuppressWarnings("rawtypes") // J'utilise Class pour rendre générique ma fonction
	protected Object readObjectValueFromDisk(DataInputStream readFromDataStream, Class dataClassType) throws IOException  {
		
		Object result = null;
		
		if (dataClassType == Float.class)    result = new Float(readFromDataStream.readFloat()); // new Float() : il le fait tout seul et essaie d'optimiser le truc, je pense !
		if (dataClassType == Double.class)   result = new Double(readFromDataStream.readDouble());
		if (dataClassType == Byte.class)     result = new Byte(readFromDataStream.readByte());
		if (dataClassType == Integer.class)  result = new Integer(readFromDataStream.readInt());
		if (dataClassType == Long.class)     result = new Long(readFromDataStream.readLong());
		
		return result;
	}
	
	/** Probably not very fast ><"
	 *  Même very very lent, indeed :'(
	 *  @param originalAssociatedValue
	 *  @return
	 * @throws IOException 
	 */
	@SuppressWarnings("rawtypes") // J'utilise Class pour rendre générique ma fonction
	protected Object readObjectValueFromDisk(RandomAccessFile randAccessFile, Class dataClassType) throws IOException  {
		
		Object result = null;
		
		if (dataClassType == Float.class)    result = new Float(randAccessFile.readFloat()); // new Float() : il le fait tout seul et essaie d'optimiser le truc, je pense !
		if (dataClassType == Double.class)   result = new Double(randAccessFile.readDouble());
		if (dataClassType == Byte.class)     result = new Byte(randAccessFile.readByte());
		if (dataClassType == Integer.class)  result = new Integer(randAccessFile.readInt());
		if (dataClassType == Long.class)     result = new Long(randAccessFile.readLong());
		
		return result;
	}
	
	public int compareValues(Object value1, Object value2) {
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
		}*/
		
	}
	
	
	
}
