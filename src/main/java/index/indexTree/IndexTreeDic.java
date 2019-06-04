package index.indexTree;

import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.dant.utils.BufferedDataInputStreamCustom;
import com.dant.utils.EasyFile;
import com.dant.utils.Log;
import com.dant.utils.MemUsage;
import com.dant.utils.Timer;

import db.data.types.DataType;
import db.data.types.DataPositionList;
import db.disk.dataHandler.DiskDataPosition;
import db.search.Operator;
import db.search.Predicate;
import db.structure.Column;
import db.structure.Database;
import db.structure.Index;
import db.structure.Table;
import index.IndexException;
import org.apache.commons.lang3.ArrayUtils;
import zArchive.sj.tcpAndBuffers.NetBuffer;

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
 *	  -> Justification de la redondance de l'information pour l'IndexTreeDic : ne pas avoir un aute fichier d'ouvert,
 *       ne pas faire de seek supplémentaire, améliore grandement les performances et ne prend pas beaucoup plus d'espace disque.
 *
 */
@Deprecated
public class IndexTreeDic extends Index implements Serializable {
	private static final long serialVersionUID = 7766967903992652785L;
	
	/**
	 * Il y a donc une seule TreeMap pour ce type d'index (contrairement à IndexTreeCeption)
	 *
	 *
	 */
	
	// Pour la recherche multi-thread, ce nombre est multiplié autant de fois qu'il y a d'arbre !
	static public int maxResultCountPerIndexInstance = 20_000_000_00;
	static public int maxResultCountInTotal = 20_000_000_00; // new AtomicInteger(lent, mais pour peu de résultats, ça passera !
	
	public int flushOnDiskOnceReachedThisTotalEntrySize = 120_000_000; // <- Globalement, la taille des fichiers mis sur le disque     ancien : EntryNumber
	protected int currentTotalEntrySizeInMemory = 0; // nombre actuel de résultats en mémoire vive, multiplié par la taille de chaque résultat (en octets) (utile pour le flush sur le disque)
	public boolean useMultithreadSearch = true;
	public boolean showMemUsageAtEachFlush = true;
	public boolean forceGarbageCollectorAtEachFlush = false;
	
	protected String baseAssociatedTablePath;
	
	// Contient tous les index des données indexées
	transient protected TreeMap<Object/*clef, valeur indexée*/, DataPositionList/*valeur*/> associatedBinIndexes;// = new TreeMap<Object, DataPositionList>();
	//protected EasyFile fileStoringDataBlocks; // link between the disk and onDiskDataBlocks
	//protected EasyFile fileSaveOnDisk = null;
	//protected String currentSaveFilePath = null;
	protected String basePath; // initialisé via initialiseWithTableAndColumn    ancien : = "data_save/IndexTreeDic_DiskMemory/";//"target/IndexTreeDic_DiskMemory/";
	//protected static int rootIndexTreeCount = 0;
	
	
	private void loadSerialAndCreateCommon() {
		associatedBinIndexes = new TreeMap<Object, DataPositionList>();
	}
	
	transient private Object indexingValueLock = new Object();
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		loadSerialAndCreateCommon();
		indexingValueLock = new Object();
		//Log.info("READ READ READ readObject IndexTreeDic");
	}
	
	
	/** Pour la sauvegarde sur disque de cet IndexTree
	 *  -> Les données de l'arbre (pour dichotomie etc.) sont sauvegardées autrement (via saveOnDisk())
	 *  La mémoire vive est supposée avoir été tptalement écrite sur le disque
	 * @throws IOException problème d'I/O
	 */
	public void saveVariablesOnStream(DataOutputStream outStream) throws IOException {
		flushOnDisk(); // écriture des données en mémoire vive, s'il y en a
		NetBuffer writeBuff = new NetBuffer(); // <- fait maison ! :3 (mais pas super ouf ouf parce que fait en rush, en début d'année)
		writeBuff.writeInt(indexTreeDicUniqueId);
		// baseSaveFilePath, suffixSaveFilePath : pas la peine
		writeBuff.writeInt(uniqueFileIdForThisTree);


		// de nombreuses variables sont initialisées via : initialiseWithTableAndColumn
		// Ecrire :
		/*



		*/

	}

	// Thread-safe
	protected int indexTreeDicUniqueId; // id unique de cet index
	protected final String baseSaveFilePath, suffixSaveFilePath;
	protected int uniqueFileIdForThisTree = 1;

	// storedValuesClassType et storedValueDataByteSize sont définis dans indexColumnFromDisk(...)
	// -> c'est à dire au moment d'indexer une colonne
	@SuppressWarnings("rawtypes")
	protected Class storedValuesClassType;
	protected int storedValueSizeInBytes; // nombre d'octets pris par chaque valeur (associée à chaque IntegerArrayList)

	protected final byte binIndexStorageSize = DiskDataPosition.diskDataPositionSizeOnDisk;//8;
	protected long diskEntryTotalSize; // = storedValueSizeInBytes + binIndexStorageSize; // nombre d'octets pris par chaque entrée (valeur + binIndex)
	@Deprecated protected int associatedTableColumnIndex;
	
	/** Ce constructeur : Lors de la création d'un nouvel index uniquement
	 * @throws IndexException erreur lors de la création
	 */
	@Deprecated
	public IndexTreeDic(Table inTable, int columnIndex) throws IndexException {//(Class argStoredValuesClassType) {

		initialiseWithTableAndColumn(inTable, columnIndex);
		basePath = baseAssociatedTablePath + "IndexTreeDic_DiskMemory/";
		indexTreeDicUniqueId = Database.getInstance().getAndIncrementNextIndexTreeDicID();
		baseSaveFilePath = basePath + "IndexTreeDic_" + indexTreeDicUniqueId + "_"; // id arbre _ id(nombre) fichier
		suffixSaveFilePath = ".idc_bin";
		
		/*currentSaveFilePath = basePath + "IndexTreeDic_indexSave_" + indexTreeDicUniqueId + ".bin_tree";
		fileSaveOnDisk = new EasyFile(currentSaveFilePath);
		try {
			fileSaveOnDisk.createFileIfNotExist();
		} catch (IOException e) {
			fileSaveOnDisk = null;
			e.printStackTrace();
		}*/
		//storedValuesClassType = argStoredValuesClassType;
		//rootIndexTreeCount++;
	}
	
	/**
	 * Constructeur : Pour le chargement du disque (index sauvegardé)
	 * @param inTable la table associée à cet index
	 * @param columnIndex le numéro de la colonne associée
	 * @param argUniqueID un id unique ?
	 * @throws IndexException erreur lors de la création
	 */
	@Deprecated
	public IndexTreeDic(Table inTable, int columnIndex, int argUniqueID) throws IndexException {//(Class argStoredValuesClassType) {

		initialiseWithTableAndColumn(inTable, columnIndex);
		basePath = baseAssociatedTablePath + "IndexTreeDic_DiskMemory/";
		indexTreeDicUniqueId = argUniqueID;
		baseSaveFilePath = basePath + "IndexTreeDic_" + indexTreeDicUniqueId + "_"; // id arbre _ id(nombre) fichier
		suffixSaveFilePath = ".idc_bin";
		
		/*currentSaveFilePath = basePath + "IndexTreeDic_indexSave_" + indexTreeDicUniqueId + ".bin_tree";
		fileSaveOnDisk = new EasyFile(currentSaveFilePath);
		try {
			fileSaveOnDisk.createFileIfNotExist();
		} catch (IOException e) {
			fileSaveOnDisk = null;
			e.printStackTrace();
		}*/
		//storedValuesClassType = argStoredValuesClassType;
		// n'incrémte pas : rootIndexTreeCount++;
	}
	
	/**
	 * Nouveau constructeur qui prend direct une column
	 */
	public IndexTreeDic(Table table, Column indexedColumn) throws IndexException{
		super(table, indexedColumn);
		loadSerialAndCreateCommon();
		DataType columnDataType = indexedColumn.getDataType();
		storedValuesClassType = columnDataType.getAssociatedClassType();
		storedValueSizeInBytes = columnDataType.getSize();
		diskEntryTotalSize = storedValueSizeInBytes + binIndexStorageSize; // nombre d'octets pris par chaque entrée (valeur + binIndex)
		baseAssociatedTablePath = table.getBaseTablePath();
		basePath = baseAssociatedTablePath + "IndexTreeDic_DiskMemory/";
		indexTreeDicUniqueId = Database.getInstance().getAndIncrementNextIndexTreeDicID();
		baseSaveFilePath = basePath + "IndexTreeDic_" + indexTreeDicUniqueId + "_"; // id arbre _ id(nombre) fichier
		suffixSaveFilePath = ".idc_bin";
		
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
	
	/* Débugs
	String stringDateFrom = "2015-04-04 00:00:00";//"2015-04-04 00:01:00";//
	String stringDateTo = "2015-04-04 03:20:00";//"2015-04-04 00:18:57";//
	Date dateFrom = Utils.dateFromStringNoThreadSafe(stringDateFrom);
	Date dateTo = Utils.dateFromStringNoThreadSafe(stringDateTo);
	int intDateFrom = Utils.dateToSecInt(dateFrom);
	int intDateTo = Utils.dateToSecInt(dateTo);*/

	/** Utile pour le RuntimeIndexing
	 * @throws IndexException
	 */
	@Deprecated
	public void initialiseWithTableAndColumn(Table inTable, int columnIndex) throws IndexException {
		loadSerialAndCreateCommon();
		if (inTable == null) throw new IndexException("Impossible d'initialiser cet index avec une Table null.");
		List<Column> columnsList = inTable.getColumns();
		int columnsNumber = columnsList.size();
		if (columnsNumber <= columnIndex) throw new IndexException("Impossible d'initialiser cet index avec un index invalide de colonne."); // invalid columnIndex

		baseAssociatedTablePath = inTable.getBaseTablePath();


		associatedTableColumnIndex = columnIndex;
		indexedColumn = columnsList.get(associatedTableColumnIndex);
		DataType columnDataType = indexedColumn.getDataType();

		storedValuesClassType = columnDataType.getAssociatedClassType();
		int dataSizeInBytes = columnDataType.getSize();
		storedValueSizeInBytes = dataSizeInBytes;
		diskEntryTotalSize = storedValueSizeInBytes + binIndexStorageSize; // nombre d'octets pris par chaque entrée (valeur + binIndex)
		
	}
	
	/** Sera bientôt remplacée par une fonction supportant le nouveau système de fichiers
	 *  @param inTable
	 *  @param columnIndex
	 *  @throws IOException
	 *  @throws Exception
	 */
	@Deprecated
	public void indexColumnFromDisk(Table inTable, int columnIndex) throws IOException, Exception {
		//indexedColumnsList = new Column[0];

		debug_AnnoyingTable = inTable;
		debug_AnnoyingColmnIndex = columnIndex;

		initialiseWithTableAndColumn(inTable, columnIndex);
		List<Column> columnsList = inTable.getColumns();

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

		//indexedColumnsList = new Column[1]; // Currently, an IndexTree only supports one column
		//indexedColumnsList[0] = indexThisColumn;

		int skipBeforeData = dataOffsetInLine; // skip the first values
		int skipAfterData = totalLineSize - skipBeforeData - storedValueSizeInBytes; // skip the remaining values

		// Now, let's read the whole file and index the rows (=lines)...
		// That pretty non-optimised, but that's only V1
		//Log.info("skipBeforeData = " + skipBeforeData + " dataSizeInBytes = " + dataSizeInBytes + "  skipAfterData = " + skipAfterData, "SIndexingTree.indexColumnFromDisk");

		// Get a new disposable FileInputStream with the file where all table rows are stored
		BufferedDataInputStreamCustom fileAsStream = new BufferedDataInputStreamCustom(new FileInputStream(inTable.getFileLinesOnDisk()));//new DataInputStream(new BufferedInputStream());
		long lineIndex = 0; // index de
		long currentBinPosition = 0;
		long fileSize = inTable.getFileLinesOnDisk().length();

		//int inMemoryResults = 0;

		// ancien débug 12h-24H à garder au cas où re-bug  String stringDateFrom = "2015-04-04 00:01:00";
		//String stringDateTo = "2015-04-04 00:18:57";

		//Timer benchTime = new Timer("Temps pris par l'indexation");
		byte[] columnValueAsByteArray = new byte[storedValueSizeInBytes];
		byte colListSize = (byte) columnsList.size();

		boolean benchFullRead = false;

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
			 
			fileAsStream.skip(skipBeforeData);
			fileAsStream.skip(dataSizeInBytes);
			fileAsStream.skip(skipAfterData);
				-> Prend 210 ms
			
			fileAsStream.skip(skipBeforeData + dataSizeInBytes + skipAfterData);
				-> Prend 70 ms
			
			
			-> D'où la nécessité de faire des colonnes séparées ! (on réduit de BEAUCOUP le temps !)
			*/

			if (benchFullRead == false) {

				// Seeks to the right position in the stream
				fileAsStream.skipForce(skipBeforeData);


				// Lire la donnée
				Object readValue = columnDataType.readIndexValue(fileAsStream, columnValueAsByteArray);//fileAsStream, columnValueAsByteArray);


				// Ajouter la donnée à l'arbre
				//Log.info("readValue = " + readValue);
				// TODO this.addValue(readValue, lineIndex * totalLineSize); // new Integer() creating a new Integer is quite slow ><" (but the bottle neck really is I/O on disk)
				// TODO
				// TODO
				// TODO
				// TODO
				
				/*débugs int valueAsInt = ((Integer) readValue).intValue();
				if (intDateFrom <= valueAsInt && valueAsInt <= intDateTo) {
					resultCount++;
				}
				ancien débug 12h-24H à garder au cas où re-bug String valueAsString = (String) readValue;
				if ( (valueAsString.compareTo(stringDateFrom) >= 0) && (valueAsString.compareTo(stringDateTo) <= 0) ) {
					Log.info("readValue = " + readValue);
					resultCount++;
				}*/

				//fileAsStream.skipForce(totalLineSize);
				fileAsStream.skipForce(skipAfterData);
				currentBinPosition += totalLineSize; // = skipBeforeData + storedValueDataByteSize + skipAfterData;

			} else {

				// Bench : lecture de tous les champs de l'objet VS lecture d'un seul champ
				for (byte iColumn = 0; iColumn < colListSize; iColumn++) {
					Column col = columnsList.get(iColumn);

					//byte[] dataAsByteArray = new byte[col.getDataSize()];
					//fileAsStream.readFully(dataAsByteArray); // renvoie une exception si les données n'ont pas pu être lues

					Object readValue = col.getDataType().readIndexValue(fileAsStream);





					if (iColumn == columnIndex) {
						// TODO this.addValue(readValue, lineIndex);// TODO
						// TODO
						// TODO
						// TODO
					}
				}

				currentBinPosition += totalLineSize;
			}

			//inMemoryResults++;




			// Display some contents, debuging :
			//if (lineIndex % 10000 == 0) Log.info("lineIndex = " + lineIndex + " readValue = " + readValue);

			lineIndex++;
		}
		//benchTime.printms();

		fileAsStream.close();
		// débug Log.info("MMMMMMMM resultCount = " + resultCount);

		flushOnDisk();
	}

	
	//private Object indexingValueLockOnlyForAddAndDiskAndMemory = new Object(); // pas d'interblocage possible car les fonctions ne s'utilisent pas l'une l'autre
	/** Ajouter une valeur et un binIndex associé
	 *  @param associatedValue valeur indexée, ATTENTION : doit être du type du IndexTree utilisé (Integer, Float, Byte, Double, ...)
	 *  @param binIndex position (dans le fichier binaire global) de la donnée stockée dans la table
	 * @throws IOException problème d'I/O
	 */
	public void addValue(Object argAssociatedValue, DiskDataPosition dataPosition) throws IOException { synchronized (indexingValueLock) {
		
		// Je peux ajouter la donnée fine
		DataPositionList binIndexList = associatedBinIndexes.get(argAssociatedValue);
		if (binIndexList == null) {
			binIndexList = new DataPositionList();
			associatedBinIndexes.put(argAssociatedValue, binIndexList);
		}
		binIndexList.add(dataPosition);
		currentTotalEntrySizeInMemory += storedValueSizeInBytes + binIndexStorageSize;
		//Log.info(" currentTotalEntrySizeInMemory = " + currentTotalEntrySizeInMemory + "  /  " + flushOnDiskOnceReachedThisTotalEntrySize + "  (storedValueDataByteSize = "+storedValueSizeInBytes+")"  + "  (binIndexStorageSize = "+binIndexStorageSize+")");
		
		// S'il y a trop de valeurs mises en mémoire, j'écris l'index sur le disque et je retiens le nom du fichier écrit.
		if (currentTotalEntrySizeInMemory > flushOnDiskOnceReachedThisTotalEntrySize) {
			flushOnDisk();
			if (forceGarbageCollectorAtEachFlush) System.gc();
			//Log.info("indexColumnFromDisk : SAVE ON DISK !!");
		}

	} }

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
	 * @throws Exception
	 */
	public Collection<DataPositionList> findMatchingBinIndexesFromMemory(Object minValueExact, Object maxValueExact, boolean isInclusive) throws IndexException {// synchronized (indexingValueLockOnlyForAddAndDiskAndMemory) { // NavigableMap<Integer, IntegerArrayList> findSubTree
		// arbre terminal : je retourne la liste des binIndex
		// binIndexesFromValue est non null ici, donc; et finerSubTrees est null
		//if (checkIfCompatibleObjectType(minValueExact) == false) return new ArrayList<IntegerArrayList>();
		//if (checkIfCompatibleObjectType(maxValueExact) == false) return new ArrayList<IntegerArrayList>();

		if (maxValueExact == null) { // recherche d'une seule valeur (equals)
			maxValueExact = minValueExact;
			isInclusive = true;
		}
		if (minValueExact.getClass() != storedValuesClassType) throw new IndexException("findMatchingBinIndexesFromMemory : Le type d'objet recherché ne correspond pas au type d'objet indexé.");
		if (minValueExact.getClass() != maxValueExact.getClass()) throw new IndexException("findMatchingBinIndexesFromMemory : Les types d'objets min et max ne correspondant pas.");

		NavigableMap<Object, DataPositionList> subTree = associatedBinIndexes.subMap(minValueExact, isInclusive, maxValueExact, isInclusive);
		Collection<DataPositionList> collectionValues = subTree.values();
		if (collectionValues == null) // pour ne pas renvoyer null
			collectionValues = new ArrayList<DataPositionList>();
		return collectionValues;
		
	}
	
	int debugNumberOfExactArrayListValuesWrittenOnDisk = 0;
	int debugNumberOfExactValuesWrittenOnDisk = 0;
	
	@Override
	public void flushOnDisk() throws IOException { synchronized (indexingValueLock) {
		if (associatedBinIndexes.size() == 0) return;
		
		String saveFileName = baseSaveFilePath + uniqueFileIdForThisTree + suffixSaveFilePath;
		uniqueFileIdForThisTree++;
		// -> TRES IMPORTANT : évaluer la distance moyenne entre deux éléments de l'index, pour savoir quel index donnera le moins de résultats !
		EasyFile fileInstance = new EasyFile(saveFileName);
		fileInstance.createFileIfNotExist();
		saveOnDisk(fileInstance, false);
		indexWrittenOnDiskFilePathsArray.add(saveFileName);
		associatedBinIndexes = new TreeMap<Object, DataPositionList>(); // réinitialisation
		currentTotalEntrySizeInMemory = 0;
		
		if (showMemUsageAtEachFlush) MemUsage.printMemUsage("IndexTreeDic.flushOnDisk");//, baseSaveFilePath = " + baseSaveFilePath);
		//SimpleTCPDemoCreateTable.saveSerialData03(); // TODO
		// TODO
		// TODO
		// TODO
		// TODO
		// TODO
		// TODO
		// TODO
		// TODO
		// TODO
		}  }
	
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

		int[] rememberedBinPosOfIntegerArrayLists = new int[totalNumberOfDistinctValues];
		int currentIntegerArrayListIndex = 0;

		// Ecriture de toutes les IntegerArrayList : nombre de binIndex, et pour chaque binIndex : binIndex (int)
		for (Entry<Object, DataPositionList> currentEntry : associatedBinIndexes.entrySet()) {
			//Object ent.getKey()
			debugNumberOfExactArrayListValuesWrittenOnDisk++;
			// Position sur le disque (binIndex) de cette LongArrayList de binIndex
			rememberedBinPosOfIntegerArrayLists[currentIntegerArrayListIndex] = writeInDataStream.size();

			DataPositionList binIndexesList = currentEntry.getValue(); // liste des binIndex associés à la clef (Object)
			int binIndexTotalCount = binIndexesList.size();
			writeInDataStream.writeInt(binIndexTotalCount); // nombre de binIndex associés à la valeur

			for (int binIndexCount = 0; binIndexCount < binIndexTotalCount; binIndexCount++) {
				DiskDataPosition binIndex = binIndexesList.get(binIndexCount);
				binIndex.writeInStream(writeInDataStream);
				// ancien : writeInDataStream.writeLong(binIndex); // /*writeLong*/ binIndex
				debugNumberOfExactValuesWrittenOnDisk++;
			}
			currentIntegerArrayListIndex++;
		}
		
		// Ecriture de la "table de routage locale" (valeur <-> position dans ce fichier des DataPositionList)
		// Ecriture de toutes les valeurs de l'arbre et des binIndex (dans ce fichier) associés
		long routingTableBinIndex = writeInDataStream.size();
		writeInDataStream.writeInt(totalNumberOfDistinctValues); // taille de la table
		currentIntegerArrayListIndex = 0;
		for (Object associatedValue : associatedBinIndexes.keySet()) { // associatedValue = valeur associée à la IntegerArrayList de binIndex
			long associatedBinIndex = rememberedBinPosOfIntegerArrayLists[currentIntegerArrayListIndex];
			//System.out.println("currentEntry["+currentIntegerArrayListIndex+"] = " + associatedValue);
			writeObjectValueOnDisk(associatedValue, writeInDataStream); // écriture de la valeur
			writeInDataStream./*----writeLong*/writeLong(associatedBinIndex);
			currentIntegerArrayListIndex++;
		}

		// Ecriture de la position de la table d'index (inutile d'utiliser un Long dans cette version, un Int suffit)
		writeInDataStream./*----writeLong*/writeLong(routingTableBinIndex);

	}




	public static int debugDiskNumberOfIntegerArrayList = 0;
	public static int debugDiskNumberOfExactValuesEvaluated = 0;



	protected class IndexTreeDic_localDichotomyResult {
		public final boolean success;// = false;
		public final long keyIndex;// = -1;
		public final long binIndex;// = -1;

		// success == false, juste pour plus de lisibilité
		public IndexTreeDic_localDichotomyResult() {
			success = false;
			keyIndex = -1;
			binIndex = -1;
		}

		public IndexTreeDic_localDichotomyResult(boolean argSuccess, long argKeyIndex, long argBinIndex) {
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
		//Log.info("searchValue = " + searchValue + " routingTableBinIndex = " + routingTableBinIndex);
		
		final byte routingTableLengthIndicationSize = 4; // nombre d'octets nécessaire pour écrire

		randFile.seek(routingTableBinIndex);
		int totalNumberOfDistinctValues = randFile.readInt();
		if (totalNumberOfDistinctValues <= 0) return localResultAsFalse;

		//long diskEntrySize = storedValueSizeInBytes + binIndexStorageSize; // nombre d'octets pris par chaque entrée (valeur + binIndex)

		long intervalStartIndex = 0;
		long intervalStopIndex = totalNumberOfDistinctValues - 1;
		long intervalLength = totalNumberOfDistinctValues; //intervalStopIndex - intervalStartIndex + 1;
		//Log.info("totalNumberOfDistinctValues = " + totalNumberOfDistinctValues);
		
		
		// Je recherche la valeur la plus proche
		Object lastApprochingValue = null;
		while (intervalLength > 1) {

			// Je me mets au milieu de l'intervalle
			long currentEntryIndex = intervalStartIndex + intervalLength / 2;

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

			// binPos de la valeur à lire (
			long currentEntryBinPos = (routingTableBinIndex + routingTableLengthIndicationSize) + currentEntryIndex * diskEntryTotalSize;
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
		long lastApprochingValueBinPos = (routingTableBinIndex  + routingTableLengthIndicationSize) + intervalStartIndex * diskEntryTotalSize;
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

				long checkEntryBinPos = (routingTableBinIndex +  + routingTableLengthIndicationSize) + intervalStartIndex * diskEntryTotalSize;
				randFile.seek(checkEntryBinPos);
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
				long checkEntryBinPos = (routingTableBinIndex + routingTableLengthIndicationSize) + intervalStartIndex * diskEntryTotalSize;
				randFile.seek(checkEntryBinPos);
				Object checkValue = readObjectValueFromDisk(randFile, storedValuesClassType);
				int checkFirstValueIsHigher = firstValueIsHigherThatSecondValue(searchValue, checkValue);
				if (checkFirstValueIsHigher != -1) {
					Log.error("ERREUR dichotomie (2), firstValueIsHigher=" + firstValueIsHigher + " et checkFirstValueIsHigher=" + checkFirstValueIsHigher);
					//Log.error("En théorie, je devrais avoir : checkValue < searchValue < lastApprochingValue");
					Log.error("Mais j'ai : checkValue("+checkValue+") < searchValue("+searchValue+") < lastApprochingValue("+lastApprochingValue+")");

					class showLocalValue {

						public showLocalValue(long indexInArray) throws IOException {
							if (indexInArray < 0) return;
							if (indexInArray >= totalNumberOfDistinctValues) return;
							long debugGoToThisIndex, debugGoToEntryPos;
							Object debugCheckValue;

							debugGoToThisIndex = indexInArray;
							debugGoToEntryPos = (routingTableBinIndex + routingTableLengthIndicationSize) + debugGoToThisIndex * diskEntryTotalSize;
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


		long binIndexOfIntegerArrayList = (routingTableBinIndex + routingTableLengthIndicationSize) + intervalStartIndex * diskEntryTotalSize; //  binIndex de la table de routage + offset dans cette table (binIndex de l'entrée)
		long whereToFindAssociatedBinIndex = binIndexOfIntegerArrayList + storedValueSizeInBytes; // la position où lire le binIndex de la donnée

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
		long keyIndex = intervalStartIndex;
		long binIndex = randFile.readLong();
		//Log.info("IndexTreeDis.findValueIndexByDichotomy : associatedValue = " + associatedValue + "  asDate = " + Utils.dateFromSecInt((Integer)associatedValue));

		return new IndexTreeDic_localDichotomyResult(true, keyIndex, binIndex);

	}


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

	/** Fonction pour évaluer le nombre de résultats (binIndex) trouvés, sans retourner les résultats
	 *  -> PAS SUR : Du coup, pour que ça aille plus vite, écrire le nombre d'éléments de chaque liste avant son binIndex (int) ?
	 *    -> Faire un benchmark pour ça. pour l'instant, je laisse comme c'est
	 * @param minValueExact
	 * @param maxValueExact
	 * @param isInclusive
	 * @return
	 */
	public int evaluateResultNumber(Object minValueExact, Object maxValueExact, boolean isInclusive) {
		return 0;
	}

	/** Pour faire un Equals (demandé par Nicolas)
	 *  @param equalsExactValue
	 *  @param justEvaluateResultNumber
	 *  @return
	 *  @throws Exception
	 */
	public DataPositionList getPositionsFromPredicate(Predicate predicate) throws IndexException {
		try {
			switch (predicate.getOperator()) {
				case equals:
					return findMatchingBinIndexes(predicate.getValue(), null, true, false);
				case between:
					Object[] values;
					try {
						values = (Object[]) predicate.getValue();
					} catch (Exception e) {
						throw new IndexException(e);
					}
					return findMatchingBinIndexes(values[0], values[1], false, false);
				default:
					throw new IndexException("invalid operator");
			}
		} catch (IOException e) {
			throw new IndexException(e);
		}
	}

	/** Trouve les résultats dans la mémoire et sur le disque
	 *  @param minValueExact
	 *  @param maxValueExact
	 *  @param isInclusive
	 *  @param justEvaluateResultNumber
	 *  @return
	 *  @throws Exception
	 */
	public DataPositionList findMatchingBinIndexes(Object minValueExact, Object maxValueExact, boolean isInclusive, boolean justEvaluateResultNumber) throws IndexException, IOException {
		synchronized (indexingValueLock) { // NavigableMap<Integer, IntegerArrayList> findSubTree
			this.flushOnDisk();
			Collection<DataPositionList> fromDisk = findMatchingBinIndexesFromDisk(minValueExact, maxValueExact, isInclusive, justEvaluateResultNumber);
			Collection<DataPositionList> fromMemory = findMatchingBinIndexesFromMemory(minValueExact, maxValueExact, isInclusive); // new ArrayList<DataPositionList>();//

			Collection<DataPositionList> allResults = fromDisk;
			allResults.addAll(fromMemory);

			DataPositionList simpleResultList = new DataPositionList();
			for (DataPositionList subList : allResults) {
				simpleResultList.addAll(subList);
			}

			return simpleResultList;
	} }

	/** Gets the matching results from disk !
	 *
	 *  @param minValue
	 *  @param maxValue
	 *  @param isInclusive
	 *  @return la collection contenant tous les binIndex correspondants
	 * @throws Exception
	 */
	public Collection<DataPositionList> findMatchingBinIndexesFromDisk(Object argMinValueExact, Object argMaxValueExact, boolean isInclusive, boolean justEvaluateResultNumber) throws IndexException, IOException { //synchronized (indexingValueLockOnlyForAddAndDiskAndMemory) { // NavigableMap<Integer, IntegerArrayList> findSubTree
		
		debugDiskNumberOfIntegerArrayList = 0;
		debugDiskNumberOfExactValuesEvaluated = 0;
		if (argMaxValueExact == null) { // recherche d'une seule valeur (equals)
			argMaxValueExact = argMinValueExact;
			isInclusive = true;
		}

		if (argMinValueExact.getClass() != storedValuesClassType) throw new IndexException("findMatchingBinIndexesFromDisk : Le type d'objet recherché ne correspond pas au type d'objet indexé.");
		if (argMinValueExact.getClass() != argMaxValueExact.getClass()) throw new IndexException("findMatchingBinIndexesFromDisk : Les types d'objets min et max ne correspondant pas.");

		final Object minValueExact = argMinValueExact;
		final Object maxValueExact = argMaxValueExact;

		final AtomicInteger totalResultCount = new AtomicInteger(0); // <- lent, mais pour peu de résultats, ça passera !

		// Lecture du dernier float écrit dans le disque, i.e. de la position de la table de routage de l'arbre principal
		/**
			Optimiser : faire le moins de seek/skip possibles
		 */

		class SearchInFile implements Runnable {

			private final String filePath;

			public ArrayList<DataPositionList> listOfLocalMatchingArraysOfBinIndexes = new ArrayList<DataPositionList>();

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

				//randFile.seek(routingTableBinIndex);
				//int totalNumberOfDistinctValues = randFile.readInt();


				// Lecture de la table de routage

				// Pas besoin de récursvité pour l'arbre avec dichotomie
				/** 1) Je cherche le binIndex de la valeur minimale, puis de la valeur maximale
				 *  2) Je vais à tous les index, je mets les valeurs en mémoire.
				 */
				long minBinIndex = -1;
				//long maxBinIndex = -1;
				long keyIndexOfMin = -1;
				long keyIndexOfMax = -1;
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

				long integerArrayListTotalCount = keyIndexOfMax - keyIndexOfMin + 1;
				//Log.info("IndexTreeDic.findMatchingBinIndexesFromDisk : integerArrayListTotalCount = " + integerArrayListTotalCount);

				boolean resultCountExcedeedStop = false;

				if (minBinIndex != -1) {
					randFile.seek(minBinIndex); // premier seek
					listOfLocalMatchingArraysOfBinIndexes.ensureCapacity((int) integerArrayListTotalCount);
					int totalResultCountForThisTree = 0;
					// Lecture de tous les IntegerArrayList de binIndex
					for (int integerArrayListCount = 0; integerArrayListCount < integerArrayListTotalCount; integerArrayListCount++) {
						int binIndexTotalCount = randFile.readInt(); // nombre de binIndex stockés

						DataPositionList binIndexesList = new DataPositionList();
						binIndexesList.ensureCapacity(binIndexTotalCount);
						for (long binIndexCout = 0; binIndexCout < binIndexTotalCount; binIndexCout++) {
							DiskDataPosition dataPos = DiskDataPosition.readFromRandFile(randFile);
							//long binIndex = randFile.readLong();

							if (totalResultCountForThisTree >= IndexTreeDic.maxResultCountPerIndexInstance) {
								resultCountExcedeedStop = true;
								break;
							}

							int localTotalResCount = totalResultCount.addAndGet(1);
							if (localTotalResCount > maxResultCountInTotal) {
								resultCountExcedeedStop = true;
								break;
							}

							binIndexesList.add(dataPos);
							totalResultCountForThisTree++;

						}
						listOfLocalMatchingArraysOfBinIndexes.add(binIndexesList);
						if (resultCountExcedeedStop)
							break;
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
		ArrayList<DataPositionList> listOfMatchingArraysOfBinIndexes = new ArrayList<DataPositionList>();

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

		return listOfMatchingArraysOfBinIndexes; // <- jamais null
	}
	
	
	//public static boolean debugWriteOnce = true;
	/**
	 *
	 * @param originalAssociatedValue
	 * @param writeInDataOutput
	 * @throws IOException
	 */
	protected void writeObjectValueOnDisk(Object originalAssociatedValue, DataOutput writeInDataOutput) throws IOException  {

		//if (originalAssociatedValue.getClass() != String.class)
		// ancien débug	Log.info("originalAssociatedValue.getClass() != String.class - originalAssociatedValue = " + originalAssociatedValue);

		if (originalAssociatedValue.getClass() == Float.class)    { writeInDataOutput.writeFloat(((Float) originalAssociatedValue).floatValue());    return; }
		if (originalAssociatedValue.getClass() == Double.class)   { writeInDataOutput.writeDouble(((Double) originalAssociatedValue).doubleValue()); return; }
		if (originalAssociatedValue.getClass() == Byte.class)     { writeInDataOutput.writeByte(((Byte) originalAssociatedValue).byteValue());       return; }
		if (originalAssociatedValue.getClass() == Integer.class)  { writeInDataOutput.writeInt(((Integer) originalAssociatedValue).intValue());      return; }
		if (originalAssociatedValue.getClass() == Long.class)     { writeInDataOutput.writeFloat(((Long) originalAssociatedValue).longValue());      return; }
		if (originalAssociatedValue.getClass() == String.class)   {
			byte[] stringAsByteAray = ((String)originalAssociatedValue).getBytes();
			//if (stringAsByteAray.length != 19) Log.error("stringAsByteAray.length != 19 ->  "+ stringAsByteAray.length);
			//writeInDataOutput.writeInt(19);//stringAsByteAray.length); // taille du string, sur 2 octets
			writeInDataOutput.write(stringAsByteAray); // string en tant qu'array
			/* débug if (debugWriteOnce) {
				Log.info("stringAsByteAray.length = " + stringAsByteAray.length + " originalAssociatedValue = " + originalAssociatedValue);
				debugWriteOnce = false;
			}*/
			return;
		}
	}

	/**
	 * @param dataInput   randAccessFile ou dataClassType
	 * @param dataClassType
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
			//dataInput.skipBytes(1);
			long stringAsByteArrayLength = storedValueSizeInBytes;//19;//dataInput.readInt(); // 2 octets
			byte[] stringAsByteAray = new byte[(int) stringAsByteArrayLength];
			//int stringAsByteArrayCheckLength =
			dataInput.readFully(stringAsByteAray, 0, stringAsByteAray.length);
			String resultString = new String(stringAsByteAray);

			// débug Log.error("stringAsByteAray.length = " + stringAsByteAray.length + " resultString = " + resultString);

			//if (stringAsByteArrayLength != stringAsByteArrayCheckLength) throw new IOException("readObjectValueFromDisk : stringAsByteArrayLength("+stringAsByteArrayLength+") != stringAsByteArrayCheckLength("+stringAsByteArrayCheckLength+")");
			return resultString;
		}

		return null;
	}

	/** Juste pour avoir un nom plus clair que compareValues,
	 * 	pour me faciliter le débug ^^'
	 *  @return   1 pour "true"   0 pour "égalité"   -1 pour "value1 < value2" (inférieur strict)
	 */
	public static int firstValueIsHigherThatSecondValue(Object value1, Object value2) {
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


	public int getAssociatedTableColumnIndex() {
		return associatedTableColumnIndex;
	}

	@Override
	public boolean isOperatorCompatible(Operator op) {
		return ArrayUtils.contains(new Operator[] {
				Operator.equals,
				Operator.greater,
				Operator.less,
				Operator.greaterOrEquals,
				Operator.lessOrEquals,
				Operator.in,
				Operator.between
		}, op);
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
