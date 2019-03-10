package db.structure.indexTree;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.dant.utils.EasyFile;
import com.dant.utils.Log;

import db.data.DataType;
import db.data.IntegerArrayList;
import db.data.Operator;
import db.structure.Column;
import db.structure.Index;
import db.structure.IndexTreeOnDiskBlock;
import db.structure.Table;
import sj.simpleDB.treeIndexing.SIndexingTreeType;


public class IndexTreeV2 extends Index {
	protected SIndexingTreeType treeType; // servira pour l'utilisation de méthodes génériques, pour utiliser le bon type d'arbre et faire les bons cast
	
	protected Object storedDataType; // Integer, Float, Double, Byte, String ...
	
	// Indexes map, using TreeMap
	// This Map is in memory, fast to access, but possibly takes a lot of memory (not working for huge files)
	protected TreeMap<Object/*clef*/, IntegerArrayList/*valeur*/> inMemoryObjectTreeMap = new TreeMap<Object, IntegerArrayList>(); //IntegerArrayList -> BinIndexArrayList
	
	// If a lot of data is (later : removed or) added, the data on disk will be fragmented, causing performances issues.
	protected TreeMap<Object/*clef, valeur indexée*/, ArrayList<IndexTreeOnDiskBlock>> onDiskDataBlocksTreeMap = new TreeMap<Object, ArrayList<IndexTreeOnDiskBlock>>();
	
	protected EasyFile fileStoringDataBlocks; // link between the disk and onDiskDataBlocks
	
	// Work In Proress ...
	// Max distance between two numerical elements to be in the same block :
	// If 0 is used, no grouping is done (enhances performance for int variables)
	protected double maxDistanceBetweenTwoNumericalElements = 0; // compressionFactor
	// IntegerArrayList : mettre un TreeMap des valeurs fines, plus optimisé qu'un ArrayList, en plus, c'est facile de faire un merge de collections lors d'une recherche
	
	/** Returns true if there is more than one value on an IntegerArrayList */
	public boolean compressionIsEnabled() {
		return (maxDistanceBetweenTwoNumericalElements != 0);
	}
	
	/* When a query is made, the IndexTree return what's in it's memory, and checks the disk for all the data.
	 * Once the inMemoryObjectTreeMap is too big, it's flushed on the disk using onDiskDataBlocks.
	 */
	
	/** Index a column and 
	 *  @param inTable
	 *  @param columnIndex
	 *  @throws FileNotFoundException 
	 */
	public void indexColumnFromDisk(Table inTable, int columnIndex) throws IOException {
		indexedColumnsList = new Column[0];
		List<Column> columnsList = inTable.getColumns();
		int columnsNumber = columnsList.size();
		if (columnsNumber <= columnIndex) { // invalid columnIndex
			return;
		}
		System.out.println("maxDistanceBetweenTwoNumericalElements = " + maxDistanceBetweenTwoNumericalElements);
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
		int dataSizeInBytes = columnDataType.getSize();
		
		indexedColumnsList = new Column[1]; // Currently, an IndexTree only supports one column
		indexedColumnsList[0] = indexThisColumn;
		
		int skipBeforeData = dataOffsetInLine; // skip the first values
		int skipAfterData = totalLineSize - skipBeforeData - dataSizeInBytes; // skip the remaining values
		
		// Now, let's read the whole file and index the rows (=lines)...
		// That pretty non-optimised, but that's only V1
		Log.info("skipBeforeData = " + skipBeforeData + " dataSizeInBytes = " + dataSizeInBytes + "  skipAfterData = " + skipAfterData, "SIndexingTree.indexColumnFromDisk");
		
		// Get a new disposable FileInputStream with the file where all table rows are stored
		FileInputStream fileAsStream = new FileInputStream(inTable.getFileLinesOnDisk());
		int lineIndex = 0;
		
		while (true) {

			// Seeks to the right position in the stream
			long checkSkipBytesAmount;
			checkSkipBytesAmount = fileAsStream.skip(skipBeforeData);
			byte[] columnValueAsByteArray = new byte[dataSizeInBytes];
			int bytesRead = fileAsStream.read(columnValueAsByteArray); // reads from the stream
			if (bytesRead == -1) // end of stream
				break;
			Object readValue = columnDataType.getValueFromByteArray(columnValueAsByteArray);
			this.addValue(readValue, new Integer(lineIndex));
			checkSkipBytesAmount = fileAsStream.skip(skipAfterData);
			// Display some contents, debuging :
			//if (lineIndex % 10000 == 0) Log.info("lineIndex = " + lineIndex + " readValue = " + readValue);
			
			lineIndex++;
		}
		
		
		fileAsStream.close();
	}
	
	// Plus tard, pour optimiser : protected final int divideAndGroupBy; // Grouper les valeurs en lot
	
	
	// IntegerArrayList correpond à la liste des binIndex ayant la même valeur pour cet IndexingTree (donc pour la colonne indexée)
	
	/** Trouver le tableau d'index correspondant à une valeur
	 *  @param associatedValue
	 *  @return
	 */
	public IntegerArrayList findBinIndexArrayInMemoryFromValue(Object associatedValue) {
		return inMemoryObjectTreeMap.get(associatedValue); // fait une comparaison d'objet, et non une comparaison de référence : if (associatedValue.equals(valeurDansArbre)) [...]
	}
	
	
	/** Only gets the matching binIndexes from memory, not from stored data on disk
	 * 
	 * @param minValue
	 * @param maxValue
	 * @param isInclusive
	 * @return la collection contenant tous les binIndex correspondants
	 */
	public Collection<IntegerArrayList> findMatchingBinIndexesInMemory(Object minValue, Object maxValue, boolean isInclusive) { // NavigableMap<Integer, IntegerArrayList> findSubTree
		
		Object searchForMinValue = minValue;
		Object searchForMaxValue = maxValue;
		if (maxDistanceBetweenTwoNumericalElements != 0) { // WIP
			searchForMinValue = getBlockNumericalValue(minValue);
			searchForMaxValue = getBlockNumericalValue(maxValue);
		}
		
		NavigableMap<Object, IntegerArrayList> subTree = inMemoryObjectTreeMap.subMap(searchForMinValue, isInclusive, searchForMaxValue, isInclusive);
		Collection<IntegerArrayList> collectionValues = subTree.values();
		
		// Now, if the current TreeIndex has it's elements grouped, remove all the non-matching elements
		// Careful, this is (currently) quite slow
		if (maxDistanceBetweenTwoNumericalElements != 0) {
			
		}
		
		return collectionValues;
		///for (IntegerArrayList binIndexArray : collectionValues) {
		//}
	}
	
	/** Search for the matching binIndexes (between minValue and maxValue), in memory and on the disk
	 *  @param minValue 
	 *  @param maxValue 
	 *  @param isInclusive 
	 *  @return 
	 */
	public Collection<IntegerArrayList> findMatchingBinIndexes(Object minValue, Object maxValue, boolean isInclusive) {
		Collection<IntegerArrayList> collectionFromMemory = findMatchingBinIndexesInMemory(minValue, maxValue, isInclusive);
		// Collection<IntegerArrayList> collectionFromDisk = findMatchingBinIndexesInMemory(minValue, maxValue, isInclusive);
		Collection<IntegerArrayList> returnCollection = collectionFromMemory;
		//returnCollection.addAll(collectionFromDisk);
		return returnCollection;
	}
	
	/** Ajouter une valeur et un binIndex associé
	 *  @param associatedValue valeur indexée, ATTENTION : doit être du type du SIndexingTree utilisé (Integer pour un SIndexingTreeInt par exemple, Float pour un TreeFloat)
	 *  @param binIndex position (dans le fichier binaire global) de l'objet stocké dans la table
	 */
	public void addValue(Object argAssociatedValue, Integer binIndex) {
		
		Object indexThisValue = argAssociatedValue;
		// If we have to regroup elements together, finds the right tree value
		// This isn't really fast (probably) but will work, if the values are not too large or small
		if (maxDistanceBetweenTwoNumericalElements != 0) { // WIP
			// WIP
			indexThisValue = getBlockNumericalValue(argAssociatedValue);
			/*
			double associatedValueAsDouble = ((Number) argAssociatedValue).doubleValue();
			double indexThisValueAsDouble = (associatedValueAsDouble / maxDistanceBetweenTwoNumericalElements);
			long indexThisValueIntegerPart = (long) indexThisValueAsDouble; // ne fonctionne pas si la valeur est supéieure à un int64 signé
			//float newValueGrouped = (float) ((double)((Float)argAssociatedValue).floatValue() / maxDistanceBetweenTwoNumericalElements);
			
			long indexThisValueIntegerPart = getBlockNumericalValue(argAssociatedValue);
			
			if (argAssociatedValue.getClass() == Float.class) indexThisValue = new Float(indexThisValueIntegerPart);
			if (argAssociatedValue.getClass() == Double.class) indexThisValue = new Double(indexThisValueIntegerPart);
			if (argAssociatedValue.getClass() == Byte.class) indexThisValue = new Byte((byte)indexThisValueIntegerPart);
			if (argAssociatedValue.getClass() == Integer.class) indexThisValue = new Integer((int)indexThisValueIntegerPart); // also DateType compatible
			if (argAssociatedValue.getClass() == Long.class) indexThisValue = new Long(indexThisValueIntegerPart);
			// WIP
			//System.out.println("IndexTree.addValue : " + argAssociatedValue + " -> " + indexThisValue);
		*/
		}
		
		//Integer realAssociatedValue = (Integer)argAssociatedValue; // cast de la valeur : elle DOIT être de type Integer
		IntegerArrayList binIndexList = findBinIndexArrayInMemoryFromValue(indexThisValue);
		if (binIndexList == null) {
			binIndexList = new IntegerArrayList();
			inMemoryObjectTreeMap.put(indexThisValue, binIndexList);
		}
		binIndexList.add(binIndex);
	}
	
	/**
	 * Flushes the memory on the disk, stores 
	 */
	public void flushMemoryOnDisk() {
		
	}
	
	/** Probably not very fast ><"
	 *  @param originalAssociatedValue
	 *  @return
	 */
	protected Object getBlockNumericalValue(Object originalAssociatedValue) {
		double associatedValueAsDouble = ((Number) originalAssociatedValue).doubleValue();
		double indexThisValueAsDouble = (associatedValueAsDouble / maxDistanceBetweenTwoNumericalElements);
		long indexThisValueIntegerPart = (long) indexThisValueAsDouble; // ne fonctionne pas si la valeur est supéieure à un int64 signé
		
		Object indexThisValue = null; // originalAssociatedValue normally, but I want to have a bug here if needed
		
		if (originalAssociatedValue.getClass() == Float.class) indexThisValue = new Float(indexThisValueIntegerPart);
		if (originalAssociatedValue.getClass() == Double.class) indexThisValue = new Double(indexThisValueIntegerPart);
		if (originalAssociatedValue.getClass() == Byte.class) indexThisValue = new Byte((byte)indexThisValueIntegerPart);
		if (originalAssociatedValue.getClass() == Integer.class) indexThisValue = new Integer((int)indexThisValueIntegerPart); // also DateType compatible
		if (originalAssociatedValue.getClass() == Long.class) indexThisValue = new Long(indexThisValueIntegerPart);
		
		return indexThisValue;
		
	}
	
	
	
	
	// -> Is it still useful ? (compatibleOperatorsList)
	protected final static Operator[] compatibleOperatorsList = {
		Operator.equals,
		Operator.greater,
		Operator.less,
		Operator.greaterOrEquals,
		Operator.lessOrEquals,
	};
	
}