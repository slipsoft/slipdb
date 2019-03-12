package db.structure.indexTree;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import db.data.Operator;
import db.structure.Column;
import db.structure.Index;
import db.structure.IndexTreeOnDiskBlock;
import db.structure.Table;
import sj.simpleDB.treeIndexing.SIndexingTreeType;

/**
 * IndexTree, v3 : sauvegarde du plus d'index possible sur le disque.
 * 
 * Pour de très gros fichiers à indexer, il n'y a pas assez de mémoire vive dispo,
 * il faut donc mettre une partie des index sur le disque.
 * Se pose le problème de pouvoir retrouver les données aussi vite que possible.
 * 
 * Sur le principe des zooms successifs, il serait possible de faire des arbres imbriqués : des regroupements de valeurs proches.
 * Pour une seule valeur : valeur réelle -> valeur groupée -> arbre contenant les arbres contenant les valeurs réelles -> bon arbre
 * Pour un interval : min - max réel -> minGroupé, maxGroupé -> arbres contenant les arbres des valeurs fines -> bons arbres, collections à fusionner pour avoir le résultat
 * 
 * Il me semblerait bon d'avoir plus de deux arbres imbriqués, pour pouvoir mettre BEAUCOUP de choses sur le disque.
 * Le principe serait le suivant : Arbre -> Sous-arbres ayant des valeurs plus fines -> ... -> valeurs recherchées
 * 
 * Un arbre (qui peut aussi être un sous-arbre) doit avoir une valeur approchée associée
 * 
 * 
 * Idées pour l'insertion/suppression une fois le fichier chargé :
 * Ajouter un élément au fichier de sauvegarde : simple, ça va à la fin.
 * 
 * -> Il est difficile de modifier un fichier d'index une fois mis sur le disque.
 * Je pense que ce serait une bonne idée d'avoir les insersions en mémoire, et quand il y en a trop, de les écrire sur le disque.
 * Plusieurs arbres d'index, et pour avoir le résultat d'une recherche, fusionner les résultats (vu que ces résultats sont indépendants)
 * 
 * Pour les suppressions, c'est plus compliqué.
 * 
 * 
 * Pour un IndexTreeV3, il y a trois types de structures :
 * -> L'arbre parent qui stocke les arbres intermédiaires
 * -> Les arbres intermédiaires qui redirigent vers les arbres contenant la donnée fine
 * -> Les arbres contenant la donnée fine
 * 
 * L'arbre parent est en fait un arbre intermédiaire.
 * 
 * Pour faire une recherche :
 *   Chaque arbre connait la valeur minimale et maximale recherchée, et la divise par son "maxDistanceBetweenTwoNumericalElements".
 *   Chaque arbre connait sa "hauteur" par rapport à la donnée fine
 *   
 * 
 * 
 */



/**
 * Cette version utilise des ArrayList, c'est un premier test, après, il sera possible de comparer les performances avec un TreeMap !
 * */
public class IndexTreeV3 extends Index {
	protected SIndexingTreeType treeType; // servira pour l'utilisation de méthodes génériques, pour utiliser le bon type d'arbre et faire les bons cast
	
	// First tree has sub-trees storing divided values
	// Il s'agit de retrouver au plus vite les valeurs fines
	// Le premier arbre sait où sont rangées les valeurs /1000
	// ...
	// Le dernier arbre contient le TreeMap de tous les binIndex associés à une valeur donnée (non approximée)
	// -> Ce tableau devra être à chaque fois adapté au type de valeur stockée, par exmemple, pour indexer une colonne de double, les longitudes/latitudes,
	//    il faudrait qu'il y ait les valeurs 0.1, 0.01, 0.001 : multiplier la valeur par 10, 100, 1000 et en prendre la partie entière.
	public static double[] arrayMaxDistanceBetweenTwoNumericalElements = {
		10000,
		100, // marche bien pour des int, mal pour des double où seules les décomales changent (et non la partie entière)
		/*10, 8, 5, 4, 3, 2, 1,*/
		0 // arbre terminal contenant la donnée fine
	};
	
	// Seulement alloué si l'arbre est un arbre intermédiaire
	protected TreeMap<Object/*clef, valeur indexée*/, IndexTreeV3> finerSubTrees = null; //new TreeMap<Object, IndexTreeV3>();
	protected TreeMap<Object/*clef, valeur indexée*/, IntegerArrayList/*valeur*/> binIndexesFromValue = null;
	protected EasyFile fileSaveOnDisk = null;
	protected static String basePath = "target/treeIndexDiskMemory/";
	protected static int rootIndexTreeCount = 0;
	
	
	protected boolean isTerminalDataTree;
	protected int heightIndex = 0; // par défaut, le permier arbre
	protected Object associatedRoundValue; // Valeur divisée par arrayMaxDistanceBetweenTwoNumericalElements[heightIndex]
	
	public IndexTreeV3() {
		this(0, null);
	}
	
	/** Création de l'arbre, à hauteur définie
	 *  @param argHeightIndex
	 */
	public IndexTreeV3(int argHeightIndex, Object argAssociatedRoundValue) {
		heightIndex = argHeightIndex;
		associatedRoundValue = argAssociatedRoundValue;
		maxDistanceBetweenTwoNumericalElements = arrayMaxDistanceBetweenTwoNumericalElements[heightIndex];
		isTerminalDataTree = (argHeightIndex == (arrayMaxDistanceBetweenTwoNumericalElements.length - 1));
		// arbre contenant la donnée fine si sa hauteur est la taille de arrayMaxDistanceBetweenTwoNumericalElements
		if (isTerminalDataTree == false) {
			finerSubTrees = new TreeMap<Object, IndexTreeV3>(); // TreeMap des sous-arbres
		} else {
			binIndexesFromValue = new TreeMap<Object, IntegerArrayList>(); // TreeMap des valeurs fines
			//IntegerArrayList -> BinIndexArrayList
		}
		if (isRootTree()) {
			fileSaveOnDisk = new EasyFile(basePath + "IndexTreeV3_indexSave_" + rootIndexTreeCount + ".bin_tree");
			try {
				fileSaveOnDisk.createFileIfNotExist();
			} catch (IOException e) {
				fileSaveOnDisk = null;
				e.printStackTrace();
			}
			rootIndexTreeCount++;
			
		}
	}
	
	public boolean isRootTree() {
		return (heightIndex == 0);
	}
	
	// Work In Proress ...
	// Max distance between two numerical elements to be in the same block :
	// If 0 is used, no grouping is done (enhances performance for int variables)
	// Only for this tree, 
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
		//System.out.println("maxDistanceBetweenTwoNumericalElements = " + maxDistanceBetweenTwoNumericalElements);
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
		//Log.info("skipBeforeData = " + skipBeforeData + " dataSizeInBytes = " + dataSizeInBytes + "  skipAfterData = " + skipAfterData, "SIndexingTree.indexColumnFromDisk");
		
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
			this.addValue(readValue, new Integer(lineIndex)); // creating a new Integer is quite slow ><" (but the bottle neck really is I/O on disk)
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
	 
	public IntegerArrayList findBinIndexArrayInMemoryFromValue(Object associatedValue) {
		return inMemoryObjectTreeMap.get(associatedValue); // fait une comparaison d'objet, et non une comparaison de référence : if (associatedValue.equals(valeurDansArbre)) [...]
	}*/
	
	
	/** Only gets the matching binIndexes from memory, not from stored data on disk
	 * 
	 * @param minValue
	 * @param maxValue
	 * @param isInclusive
	 * @return la collection contenant tous les binIndex correspondants
	 */
	public Collection<IntegerArrayList> findMatchingBinIndexesInMemory(Object minValueExact, Object maxValueExact, boolean isInclusive) { // NavigableMap<Integer, IntegerArrayList> findSubTree
		
		
		// Si ce n'est pas un arbre terminal, je recherche dans les sous-arbres
		if (maxDistanceBetweenTwoNumericalElements != 0) {
			// binIndexesFromValue est null ici; mais finerSubTrees n'est pas null
			Object minValueRounded = getBlockNumericalValue(minValueExact);
			Object maxValueRounded = getBlockNumericalValue(maxValueExact);
			//Log.info("findMatchingBinIndexesInMemory : maxDistanceBetweenTwoNumericalElements = " + maxDistanceBetweenTwoNumericalElements + "  minValueRounded = " + minValueRounded + "  maxValueRounded = " + maxValueRounded);
			// Recherche de tous les sous-arbres qui correspondent
			NavigableMap<Object, IndexTreeV3> finnerSubTrees = finerSubTrees.subMap(minValueRounded, isInclusive, maxValueRounded, isInclusive);
			
			// Pour tous les sous-arbres, j'appelle cette même fonction, et j'ajoute la valeur de retour à ma collection sumOfBinIndexesCollections
			//Collection<IntegerArrayList>
			ArrayList<IntegerArrayList> sumOfBinIndexesCollections = new ArrayList<IntegerArrayList>();
			Collection<IndexTreeV3> subTreesCollection = finnerSubTrees.values(); // tous les sous-arbres compatibles
			
			
			// Pour tous les sous-arbres compatibles, je prends les éléments qui coïncident
			for (IndexTreeV3 subTree : subTreesCollection) {
				Collection<IntegerArrayList> subBinIndexesCollection = subTree.findMatchingBinIndexesInMemory(minValueExact, maxValueExact, isInclusive);
				if (subBinIndexesCollection != null) {
					sumOfBinIndexesCollections.addAll(subBinIndexesCollection);
				}
				/*if (sumOfBinIndexesCollections == null)
					sumOfBinIndexesCollections = subBinIndexesCollection;
				else {
					if (subBinIndexesCollection != null)
						sumOfBinIndexesCollections.addAll(subBinIndexesCollection);
				}*/
			}
			return sumOfBinIndexesCollections;
			
		} else { // arbre terminal : je retourne la liste des binIndex
			// binIndexesFromValue est non null ici, donc; et finerSubTrees est null
			NavigableMap<Object, IntegerArrayList> subTree = binIndexesFromValue.subMap(minValueExact, isInclusive, maxValueExact, isInclusive);
			Collection<IntegerArrayList> collectionValues = subTree.values();
			return collectionValues;
		}
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

		//Object minValueRound = getBlockNumericalValue(minValue);
		//Object maxValueRound = getBlockNumericalValue(maxValue);
		
		Collection<IntegerArrayList> collectionFromMemory = findMatchingBinIndexesInMemory(minValue, maxValue, isInclusive);
		
		// Collection<IntegerArrayList> collectionFromDisk = findMatchingBinIndexesInMemory(minValue, maxValue, isInclusive);
		Collection<IntegerArrayList> returnCollection = collectionFromMemory;
		//returnCollection.addAll(collectionFromDisk);
		return returnCollection;
	}
	
	/** Ajouter une valeur et un binIndex associé
	 *  @param associatedValue valeur indexée, ATTENTION : doit être du type du IndexTree utilisé (Integer, Float, Byte, Double, ...)
	 *  @param binIndex position (dans le fichier binaire global) de la donnée stockée dans la table
	 */
	public void addValue(Object argAssociatedValue, Integer binIndex) {
		
		if (maxDistanceBetweenTwoNumericalElements != 0) {
			// Ce n'est pas un arbre terminal, je recherche l'arbre en dessous et je lui demande d'ajouter cette valeur.
			// Je crée l'arbre du dessous s'il n'existe pas encore
			Object indexThisRoundValue = getBlockNumericalValue(argAssociatedValue);
			
			IndexTreeV3 subTree = finerSubTrees.get(indexThisRoundValue);
			if (subTree == null) {
				subTree = new IndexTreeV3(heightIndex + 1, indexThisRoundValue);
				finerSubTrees.put(indexThisRoundValue, subTree); // ajout du sous-arbre à la liste des arbres connus
			}
			subTree.addValue(argAssociatedValue, binIndex); // se charge de l'ajout effectif (ou de la création de nouveaux sous-arbres puis de l'ajout effectif)
			return;
		}
		
		// Si je suis toujours là, c'est qu'il s'agit d'un arbre terminal : je peux ajouter la donnée fine
		
		IntegerArrayList binIndexList = binIndexesFromValue.get(argAssociatedValue);//findBinIndexArrayInMemoryFromValue(argAssociatedValue);
		if (binIndexList == null) {
			binIndexList = new IntegerArrayList();
			binIndexesFromValue.put(argAssociatedValue, binIndexList);
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
		
		if (originalAssociatedValue.getClass() == Float.class)   indexThisValue = new Float(indexThisValueIntegerPart);
		if (originalAssociatedValue.getClass() == Double.class)  indexThisValue = new Double(indexThisValueIntegerPart);
		if (originalAssociatedValue.getClass() == Byte.class)    indexThisValue = new Byte((byte)indexThisValueIntegerPart);
		if (originalAssociatedValue.getClass() == Integer.class) indexThisValue = new Integer((int)indexThisValueIntegerPart); // also DateType compatible
		if (originalAssociatedValue.getClass() == Long.class)    indexThisValue = new Long(indexThisValueIntegerPart);
		
		return indexThisValue;
		
	}
	
	public boolean wasWrittenOnDisk = false;
	//public long thisTreeBinIndexPos; // index sur le disque des données de l'arbre (valeurs fines, ou (valeur, binPosition) des sous-arbres)
	
	//public long thisTreeBinIndexPosAssociationTable;
	//public int numberOfBinIndexArrayOnDisk;
	
	public long routingTableBinIndex; // Position (dans le fichier de sauvegarde de l'index) de l'association valeur <-> position de la IntegerArrayList sur le disque

	public static long debugNumberOfTreesWrittenOnDisk = 0;
	public static long debugNumberOfExactArrayListValuesWrittenOnDisk = 0;
	
	
	
	/**
	Sauvegarde d'un indexTreeV3 sur le disque (et mise en lecture seule, donc) :
		-> Il faut avoir une organisation très structurée, savoir très rapidement où retrouver les arbres contenant la donnée fine.
		1) Ecriture de tous les arbres contenant la donnée fine, sur le disque
		2) La donnée fine des arbres (terminaux) est remplacée par une position et une taille : position de la donnée dans le fichier binaire,
		   et nombre d'éléments dans chaque arbre.
		3) Ensuite, tous les arbres juste au-dessus des arbres terminaux écrivent les valeurs qu'il contiennent : valeur, nombre d'arbres terminaux,
		   et pour chaque arbre terminal : valeur et position dans le fichier binaire (donnera la taille quand consulté)
		4) Traîtement des arbres encore au-dessus : identique.
		...
		dernier) identique, sauf que je garde en mémoire vive le dernier arbre (appartenant à l'arbre root, donc).
	 * @throws IOException 
	*/
	public void saveOnDisk() throws IOException {
		if (heightIndex != 0) return; // doit être l'arbre root
		// if (fileSaveOnDisk == null) return;
		// 1) Pour tout arbre donnant sur un arbre de donnée fine, j'écris l'arbre de donnée fine et je garde sa binPos et value
		if (finerSubTrees == null) return;
		
		DataOutputStream writeInDataStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileSaveOnDisk)));
		
		// Ecriture de chaque niveau, l'un après l'autre :
		// Je commence par les arbres de donnée fine, je stocke leur valeurs, 
		// Puis je remonte jusqu'à l'arbre root (je peux écrire l'arbre root sur le disque, aussi, sous forme de (liste des sous-arbres, nombre de sous arbres), i.e. commencer par la fin)
		int lastTreeIndex = (arrayMaxDistanceBetweenTwoNumericalElements.length) - 1;
		for (int writeTreeIndex = lastTreeIndex; writeTreeIndex >= 0; writeTreeIndex--) {
			writeAllStoredDataOnDisk(writeTreeIndex, writeInDataStream); // écriture de tous les arbres, du plus fin au plus grossier
		}
		
		writeInDataStream.close();
		
		System.out.println("IndexTreeV3.saveOnDisk : debugNumberOfTreesWrittenOnDisk=" + debugNumberOfTreesWrittenOnDisk + "  debugNumberOfExactArrayListValuesWrittenOnDisk=" + debugNumberOfExactArrayListValuesWrittenOnDisk);
		
		
	}
	
	/** Ecrire les sous-arbres sur le disque
	 * @param neededHeight
	 * @throws IOException 
	 */
	public void writeAllStoredDataOnDisk(int neededHeight, DataOutputStream writeInDataStream) throws IOException { // SubTrees
		
		if (this.heightIndex == neededHeight) { // je dois écrire cet arbre sur le disque
			
			debugNumberOfTreesWrittenOnDisk++;
			// Si c'est un arbre terminal, j'écris les valeurs fines
			
			if (isTerminalDataTree) {
				//Collection<IntegerArrayList> collectionOfBinIndexesArrays = binIndexesFromValue.values();
				ArrayList<IntegerArrayList> allBinIndexArrays = new ArrayList<IntegerArrayList>();
				ArrayList<Object> allBinIndexAssociatedValues = new ArrayList<Object>();
				//ArrayList<Long> binPositionOfIntegerArrayList = new ArrayList<Long>();
				//binPositionOfIntegerArrayList.ensureCapacity(allBinIndexArrays.size());
				
				// Une optimisation pourrait être faite ici : il y a copie, je peux ne faire que parcourir, à faire dans V4 ou +
				// -> voir ce que ça fait si je mets une binPosition pour chaque IntegerArrayList
				
				allBinIndexAssociatedValues.addAll(binIndexesFromValue.keySet());
				allBinIndexArrays.addAll(binIndexesFromValue.values()); // dans le même ordre
				
				int numberOfIntegerArrayLists = allBinIndexAssociatedValues.size(); // = binIndexesFromValue.size(); mais plus rapide, je pense
				
				// Pour chaque IntegerArrayList de valeur fine écrite sur le disque, une fois écrite,
				// j'ai une position dans la sauvegarde sur disque de l'index, je la mets ici
				long[] binPositionOfIntegerArrayListValues = new long[numberOfIntegerArrayLists];
				
				
				//for (Entry<Object, IntegerArrayList> ent : binIndexesFromValue.entrySet()) {}
				
				//thisTreeBinIndexPos = writeInDataStream.size();
				//numberOfBinIndexArrayOnDisk = numberOfIntegerArrayLists;
				
				// 1) Faire la sauvegarde des listes de valeurs fines (IntegerArrayList) en mettant en mémoire la liste des positions d'écriture
				// 2) Ecrire la liste des correspondances binIndex <-> valeur fine (et se souvenir de là où elle est écrite via thisTreeBinIndexPosAssociationTable)
				// 3) L'arbre parent qui a appelé subTree.writeAllSubTreesOnDisk (cf plus bas), stocke à peu près de la même manière les sous-arbres qu'il a fait ..
				//    écrire (valeur arrondie <-> binIndex de l'arbre (arbre terminal ou intermédiaire) 
				
				// A) Nombre de listes contenant des BinIndex : int
				// -> pas besoin d'écrire la valeur associée à l'arbre, l'arbre parent a le couple (valeur associée, binPosition)
				// pas besoin !! -> writeInDataStream.writeInt(numberOfIntegerArrayLists);
				//int arraySize = allBinIndexArrays.size();
				
				// -> Le type de la valeur est connue, c'est le type de la colonne
				// B) Pour chaque liste, j'écris : valeur, nombre d'éléments, et pour chaque élément : le binIndex correspondant
				// - Optim future possible : fractionner le fichier contenant tous les binIndex, pour pouvoir faire du multi-thread sur la recherche d'informations (bien mais compliqué !)
				for (int indexIntegerArrayList = 0; indexIntegerArrayList < numberOfIntegerArrayLists; indexIntegerArrayList++) {
					debugNumberOfExactArrayListValuesWrittenOnDisk++;
					// Valeur associée (qui est une clef dans le TreeMap binIndexesFromValue)
					Object exactValue = allBinIndexAssociatedValues.get(indexIntegerArrayList);
					// Liste des binIndex correspondants à la valeur
					IntegerArrayList binIndexesIntegerArrayList = allBinIndexArrays.get(indexIntegerArrayList); // 

					// Je sauvegarde en mémoire où j'ai écrit les valeurs sur le disque
					binPositionOfIntegerArrayListValues[indexIntegerArrayList] = writeInDataStream.size();
					// Nombre de binIndex associés à la valeur actuelle (clef dans le TreeMap)
					int numberOfAssociatedBinIndexes = binIndexesIntegerArrayList.size();
					// Ecriture du nombre de binIndex associés
					writeInDataStream.writeInt(numberOfAssociatedBinIndexes);
					// Ecriture de tous les binIndex
					for (int indexInIntegerArrayList = 0; indexInIntegerArrayList < numberOfAssociatedBinIndexes; indexInIntegerArrayList++) {
						int binIndex = binIndexesIntegerArrayList.get(indexInIntegerArrayList);
						writeInDataStream.writeInt(binIndex);
					}
					
					// plus tard writeObjectValueOnDisk(exactValue, writeInDataStream);
					// Ecriture de la valeur associée
					
					// 
					// Ecriture de la liste
					
				}
				
				// Sauvegarde en mémoire de la position sur le disque de ma table de routage
				routingTableBinIndex = writeInDataStream.size(); // utile pour l'arbre du dessus !
				
				// Toutes les listes ont été écrites, et j'ai la position à laquelle elles ont été écrites.
				// Je sauvegarde donc mon "index" : nombre de valeurs, et pour chaque valeur : valeur, position sur le disque
				
				// Nombre de valeurs
				writeInDataStream.writeBoolean(isTerminalDataTree);
				writeInDataStream.writeInt(numberOfIntegerArrayLists);
				// Pour chaque liste/valeur, j'écris la valeur et la bin position associée
				for (int indexIntegerArrayList = 0; indexIntegerArrayList < numberOfIntegerArrayLists; indexIntegerArrayList++) {
					Object associatedValue = allBinIndexAssociatedValues.get(indexIntegerArrayList); // valeur associée à la IntegerArrayList de binIndex
					long associatedBinIndex = binPositionOfIntegerArrayListValues[indexIntegerArrayList];
					writeObjectValueOnDisk(associatedValue, writeInDataStream); // écriture de la valeur
					writeInDataStream.writeInt((int)associatedBinIndex); // écriture du binIndex où trouver la liste des binIndex
				}
				
				// J'ai écrit ma table de routage, l'arbre au-dessus va récupérer la routingTableBinIndex et l'associer à la valeur (clef) de cet arbre
				
				wasWrittenOnDisk = true; // vient d'être écrit sur le disque
			} else {
				// Si c'est un arbre intermédiaire, j'écris la liste de mes sous-arbres (qui DOIVENT déjà être écrits sur le disque)
				
				routingTableBinIndex = writeInDataStream.size(); // utile pour l'arbre du dessus !

				writeInDataStream.writeBoolean(isTerminalDataTree);
				// J'écris donc ma "table de routage"
				int numberOfSubTrees = finerSubTrees.size();
				// J'écris le nombre d'arbres dont j'ai la référence
				writeInDataStream.writeInt(numberOfSubTrees);
				for (Entry<Object, IndexTreeV3> treeAsEntry : finerSubTrees.entrySet()) {
					Object associatedValue = treeAsEntry.getKey();
					IndexTreeV3 subTree = treeAsEntry.getValue();
					// Pour chaque arbre, j'écris la valeur associée (clef)...
					writeObjectValueOnDisk(associatedValue, writeInDataStream); // écriture de la valeur
					// ...et le binIndex où le trouver (table de routage des valaurs fines ou des sous-arbres)
					writeInDataStream.writeFloat(subTree.routingTableBinIndex);
				}
				
				wasWrittenOnDisk = true; // vient d'être écrit sur le disque
				
			}
			
			
			
			
		} else if (heightIndex < neededHeight) { // si je suis trop bas (proche du root), j'avance aux sous-arbres suivants
			
			for (IndexTreeV3 subTree : finerSubTrees.values()) { // pour tous les sous-arbres, écrire sur le disque
				subTree.wasWrittenOnDisk = false;
				subTree.writeAllStoredDataOnDisk(neededHeight, writeInDataStream);
				if (subTree.wasWrittenOnDisk) {
					// rien à faire, l'arbre a bien été écrit
				}
			}
			
		} // si heightIndex > neededHeight, rien à faire, je suis trop proche des données fines, je ne connais pas les arbres plus hauts
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
	protected Object readObjectValueOnDisk(DataInputStream readFromDataStream, Class dataClassType) throws IOException  {
		
		Object result = null;
		
		if (dataClassType == Float.class)    result = new Float(readFromDataStream.readFloat()); // new Float() : il le fait tout seul et essaie d'optimiser le truc, je pense !
		if (dataClassType == Double.class)   result = new Double(readFromDataStream.readDouble());
		if (dataClassType == Byte.class)     result = new Byte(readFromDataStream.readByte());
		if (dataClassType == Integer.class)  result = new Integer(readFromDataStream.readInt());
		if (dataClassType == Long.class)     result = new Long(readFromDataStream.readLong());
		
		return result;
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