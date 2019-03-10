package db.structure;

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
import sj.simpleDB.treeIndexing.SIndexingTreeType;

/**
 * -> Pour l'instant, c'est dans mon package, mais je vais faire moi-même un peu plus tard le refactoring quand tout marchera !
 * 
 * Modèle d'arbre (générique) pour l'indexation.
 * SIndexingTree est la super-classe dont héritent les arbres spécialisés comme SIndexingTreeInt, String, Float...
 * Le code des classes filles est spécialisé, pour optimiser un maximum le code en fonction du type de la donnée traîtée.
 * -> Le but est de charger le plus de données, le plus rapidement possible, et de pouvoir faire des recherches rapides.
 * 
 * Indexation d'une seule colonne par arbre.
 * Tous les index sont stockés en mémoire vive dans cette version.
 * 
 * Optimisation à réaliser : grouper les valeurs semblables, selon ce que j'ai écrit sur feuille volante.
 * Exemple : diviser la valeur d'un int par 2 pour avoir moins de ramifications, et plus d'éléments dans les listes (feuilles)
 
	Pour pouvoir stocker (beaucoup) plus de données, il faudra que les listes
	des binIndex (feuilles) associées à chaque valeur (de feuille) soient écrites sur le disque.
	  -> Un fichier par arbre.
	  -> les données doivent être aussi simples à lire que possible, et aussi rapides à charger du disque que possible.
	  
	  Fractionner les listes de chaque feuille semble être une mauvaise idée : il faudrait que chaque feuille ait sa liste complète écrite
	  quelque part sur le disque. Mais utiliser un fichier par feuille serait horrible (beaucoup trop long à charger, trop de place perdue sur le disque...)
	  
	  S'il y a suffisamment de mémoire pour indexer toute une colonne, j'indexe toute la colonne, puis j'écris les données sur le disque.
	  Les données seraient de la forme, pour chaque feuille : nombre d'éléments + binIndex pour chaque élément.
	  Une fois que tout a été écrit sur le disque, je libère la mémoire vive et je ne garde que la position (dans le fichier) de chaque liste (feuille).
	  Ainsi, en mémoire, il n'y a plus que la position sur le disque de la liste, et non plus la liste de tous les binIndex.
	  
	  S'il n'y a pas suffisamment de mémoire, alors, à chaque fois que l'arbre est trop plein, je vais le mettre sur le disque.
	  L'arbre sera ainsi fractionné, mais ça ne devrait pas trop nuire aux performances, à condition que les fractions (les bouts d'arbre)
	  soient quand-même suffisament grandes.
	  -> Au lieu d'avoir en mémoire la (seule) position et taille ou commence le bloc associé à une valeur, il faudra donc avoir une liste.
	 	
	  
	
	V1 : indexer une colonne à partir du disque
		Chargement :
		à partir d'un objet Table, lire du disque tous les champs concernés,
		les ajouter à l'index.
		
		Recherche :
		via findMatchingBinIndexes
	
 * 
 */
public class IndexTree extends Index {
	protected SIndexingTreeType treeType; // servira pour l'utilisation de méthodes génériques, pour utiliser le bon type d'arbre et faire les bons cast
	
	protected Object storedDataType; // Integer, Float, Double, Byte, String ...
	
	// Indexes map, using TreeMap
	// This Map is in memory, fast to access, but possibly takes a lot of memory (not working for huge files)
	protected TreeMap<Object/*clef*/, IntegerArrayList/*valeur*/> inMemoryObjectTreeMap = new TreeMap<Object, IntegerArrayList>();
	
	// If a lot of data is (later : removed or) added, the data on disk will be fragmented, causing performances issues.
	protected TreeMap<Object/*clef, valeur indexée*/, ArrayList<IndexTreeOnDiskBlock>> onDiskDataBlocksTreeMap = new TreeMap<Object, ArrayList<IndexTreeOnDiskBlock>>();

	protected EasyFile fileStoringDataBlocks; // link between the disk and onDiskDataBlocks
	
	// Max distance between two numerical elements to be in the same block :
	// If 0 is used, no grouping is done (enhances performance for int variables)
	protected double maxDistanceBetweenTwoNumericalElements; // compressionFactor
	
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
	 * @param associatedValue
	 * @return
	 */
	public IntegerArrayList findBinIndexArrayFromValue(Object associatedValue) {
		return inMemoryObjectTreeMap.get(associatedValue); // fait une comparaison d'objet, et non une comparaison de référence : if (associatedValue.equals(valeurDansArbre)) [...]
	}
	
	
	/**
	 * 
	 * @param minValue
	 * @param maxValue
	 * @param isInclusive
	 * @return la collection contenant tous les binIndex correspondants
	 */
	public Collection<IntegerArrayList> findMatchingBinIndexes(Object minValue, Object maxValue, boolean isInclusive) { // NavigableMap<Integer, IntegerArrayList> findSubTree
		NavigableMap<Object, IntegerArrayList> subTree = inMemoryObjectTreeMap.subMap(minValue, isInclusive, maxValue, isInclusive);
		Collection<IntegerArrayList> collectionValues = subTree.values();
		return collectionValues;
		///for (IntegerArrayList binIndexArray : collectionValues) {
		//}
	}
	
	/** Ajouter une valeur et un binIndex associé
	 *  @param associatedValue valeur indexée, ATTENTION : doit être du type du SIndexingTree utilisé (Integer pour un SIndexingTreeInt par exemple, Float pour un TreeFloat)
	 *  @param binIndex position (dans le fichier binaire global) de l'objet stocké dans la table
	 */
	public void addValue(Object argAssociatedValue, Integer binIndex) {
		
		Object indexThisValue;
		// If we have to regroup elements together, finds the right tree value
		// This isn't really fast (probably) but will work, if the values are not too large or small
		if (maxDistanceBetweenTwoNumericalElements != 0) {
			
			
			
			double associatedValueAsDouble = ((Number) argAssociatedValue).doubleValue();
			double indexThisValueAsDouble = (associatedValueAsDouble / maxDistanceBetweenTwoNumericalElements);
			long indexThisValueIntegerPart = (long) indexThisValueAsDouble; // ne fonctionne pas si la valeur est supéieure à un int64 signé
			//float newValueGrouped = (float) ((double)((Float)argAssociatedValue).floatValue() / maxDistanceBetweenTwoNumericalElements);
			
			
			if (argAssociatedValue.getClass() == Float.class) indexThisValue = new Float(indexThisValueIntegerPart);
			if (argAssociatedValue.getClass() == Double.class) indexThisValue = new Double(indexThisValueIntegerPart);
			if (argAssociatedValue.getClass() == Byte.class) indexThisValue = new Byte((byte)indexThisValueIntegerPart);
			if (argAssociatedValue.getClass() == Integer.class) indexThisValue = new Integer((int)indexThisValueIntegerPart);
			if (argAssociatedValue.getClass() == Long.class) indexThisValue = new Long(indexThisValueIntegerPart);
			
			// Reste à faire : DateType
			
		}
		
		//Integer realAssociatedValue = (Integer)argAssociatedValue; // cast de la valeur : elle DOIT être de type Integer
		IntegerArrayList binIndexList = findBinIndexArrayFromValue(argAssociatedValue);
		if (binIndexList == null) {
			binIndexList = new IntegerArrayList();
			inMemoryObjectTreeMap.put(argAssociatedValue, binIndexList);
		}
		binIndexList.add(binIndex);
	}
	
	/**
	 * Flushes the memory on the disk, stores 
	 */
	public void flushMemoryOnDisk() {
		
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
