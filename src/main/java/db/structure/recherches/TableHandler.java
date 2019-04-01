package db.structure.recherches;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.dant.utils.Log;

import db.data.load.Loader;
import db.data.types.DataType;
import db.disk.dataHandler.DiskDataPosition;
import db.disk.dataHandler.TableDataHandler;
import db.data.types.DataPositionList;
import db.data.load.CsvParser;
import db.search.ResultSet;
import db.structure.Column;
import db.structure.Database;
import db.structure.StructureException;
import db.structure.Table;
import index.indexTree.IndexException;
import index.indexTree.IndexTreeDic;

public class TableHandler implements Serializable {
	/* Ordre de serialization :
	
	TableHandler -> Table -> Colonnes
						  -> TableDataHandler
				 -> IndexTree
				 
	*/
	private static final long serialVersionUID = -4180065130691064979L;
	protected String tableName;
	protected ArrayList<Column> columnsListForCreatingTableOnly;
	public Table associatedTable;
	//protected CsvParser csvParser = null;
	// Possibilité de parser de plusieurs manières différentes (un jour...)
	protected boolean firstTimeParsingData = true;
	
	// Liste des threads faisant actuellement du parsing
	
	// obj non serial
	transient private Object multiThreadParsingListLock;
	transient protected Object indexTreeListLock;
	transient protected ArrayList<Thread> parsingThreadList;
	
	// indexColumnList est la liste des colonnes à indexer
	transient public RuntimeIndexingEntryList runtimeIndexingList;//ArrayList<SRuntimeIndexingEntry>();
	// TODO
	protected ArrayList<IndexTreeDic> indexTreeList = new ArrayList<IndexTreeDic>(); // Liste des IndexTree associés à cette table
	
	public void flushAllIndexTreesOnDisk() {
		//Log.error("TableHandler.flushAllIndexTreesOnDisk : size = " + indexTreeList.size());
		for (IndexTreeDic indexTree : indexTreeList) {
			try {
				indexTree.flushOnDisk();
				//Log.error("TableHandler.flushAllIndexTreesOnDisk : flush de l'arbre !" + indexTree);
			} catch (IOException e) {
				//Log.error("TableHandler.flushAllIndexTreesOnDisk : l'arbre n'a pas pu être écrit sur le disque, IOException.");
				Log.error(e);
				//e.printStackTrace();
			}
		}
	}
	
	private void loadSerialAndCreateCommon() {
		columnsListForCreatingTableOnly = new ArrayList<Column>(); // juste au cas où
		indexTreeListLock = new Object();
		parsingThreadList = new ArrayList<Thread>();
		multiThreadParsingListLock = new Object();
		runtimeIndexingList = new RuntimeIndexingEntryList();
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		loadSerialAndCreateCommon();
	}
	
	
	
	public TableHandler() {
		loadSerialAndCreateCommon();
	}
	
	
	public void forceAppendNotFirstParsing() {
		firstTimeParsingData = false; // si à vrai, supprimer tous les fichiers connus
	}
	
	public String getTableName() {
		return tableName;
	}

	public Table getAssociatedTable() {
		return this.associatedTable;
	}
	
	public TableHandler(String argTableName) {
		this();
		tableName = argTableName;
	}
	
	/**
	 *  Ajouter une colonne, suppose qu'il n'y a pas encore de données dans la table
	 *  @param argColumnName
	 *  @param argColumnDataType
	 * @throws Exception 
	 */
	public void addColumn(String argColumnName, DataType argColumnDataType) throws Exception {
		if (this.columnExist(argColumnName)) throw new Exception("Ajout de la colonne impossibe, il en exie déjà une du même nom : " + argColumnName);
		Column newColumn = new Column(argColumnName, argColumnDataType, true);
		columnsListForCreatingTableOnly.add(newColumn);
	}

	public boolean columnExist(String name) {
		return this.columnsListForCreatingTableOnly.stream().anyMatch(col -> col.getName() == name);
	}

	public void addColumn(Column column) {
		columnsListForCreatingTableOnly.add(column);
	}
	
	public Table createTable() throws IOException {
		associatedTable = new Table(tableName, columnsListForCreatingTableOnly, this);
		return associatedTable;
	}

	/** Thread-safe, la table est en lecture seule
	 * @param csvPath
	 * @param doRuntimeIndexing
	 * @throws Exception
	 */
	public void parseCsvData(String csvPath, boolean doRuntimeIndexing) throws Exception {
		InputStream is = new FileInputStream(csvPath);
		try {
			parseCsvData(is, doRuntimeIndexing, false);
			is.close();
		} catch (Exception e) {
			is.close();
			throw e;
		}
	}

	public void parseCsvData(InputStream is, boolean doRuntimeIndexing) throws Exception {
		try {
            parseCsvData(is, doRuntimeIndexing, false);
			is.close();
		} catch (Exception e) {
			is.close();
			Log.error(e);
			throw e;
		}
	}

	/** Thread-safe, la table est en lecture seule
	 * @param csvStream stream contenant les données en CSV à parser
	 * @param doRuntimeIndexing
	 * @param closeStreamAfterUsage Fermer le stream après usage (true ou false)
	 * @throws Exception
	 */
	public void parseCsvData(InputStream csvStream, boolean doRuntimeIndexing, boolean closeStreamAfterUsage) throws Exception {
		if (associatedTable == null) throw new Exception("La table associée est null, elle doit être crée via createTable avant tout parsing.");
		//if (csvParser == null)
		// Thread-safe
		Loader csvLoader = new Loader(associatedTable, new CsvParser());
		
		if (doRuntimeIndexing)
			csvLoader.setRuntimeIndexing(runtimeIndexingList);
		else
			csvLoader.setRuntimeIndexing(null);
		
		csvLoader.parse(csvStream, !firstTimeParsingData);
		if (closeStreamAfterUsage)
			csvStream.close();
		
		firstTimeParsingData = false;
		
	}
	
	protected int getColumnIndex(String columnName) throws StructureException {
		if (associatedTable == null) throw new StructureException("Aucune table crée, indexation impossible.");
		List<Column> columnList = associatedTable.getColumns();
		for (int colIndex = 0; colIndex < columnList.size(); colIndex++) {
			Column currentColumn = columnList.get(colIndex);
			if (currentColumn.getName().equals(columnName)) {
				return colIndex;
			}
		}
		throw new StructureException("Colonne introuvable, impossible de l'indexer.");
	}
	
	public void indexColumnWithTreeFromDisk(String columnName) throws StructureException {
		int colIndex = getColumnIndex(columnName);
		indexColumnWithTreeFromDisk(colIndex);
	}
	
	
	
	
	protected IndexTreeDic findOrCreateAssociatedIndexTree(int columnIndex, boolean createTreeIfDoesNotExists) throws IndexException {
		synchronized(indexTreeListLock) {
		
			//Log.info("findOrCreateAssociatedIndexTree : size = " + indexTreeList.size());
			for (IndexTreeDic indexTree : indexTreeList) {
				if (indexTree.getAssociatedTableColumnIndex() == columnIndex) {
					//Log.info("findOrCreateAssociatedIndexTree : TROUVE TROUVE TROUVE TROUVE");
					return indexTree;
				}
			}
			if (createTreeIfDoesNotExists == false) return null;
			IndexTreeDic newTree = new IndexTreeDic(associatedTable, columnIndex);
			indexTreeList.add(newTree);
			associatedTable.addIndex(newTree);
			//Log.info("findOrCreateAssociatedIndexTree : CREE CREE CREE CREE CREE");
			return newTree;
		}
	}
	
	/** Pas
	 *  @param columnIndex
	 *  @throws StructureException
	 */
	public void indexColumnWithTreeFromDisk(int columnIndex) throws StructureException {
		if (associatedTable == null) throw new StructureException("Aucune table crée, indexation impossible.");
		List<Column> columnList = associatedTable.getColumns();
		if (columnIndex < 0 || columnIndex >= columnList.size()) throw new IndexException("Index de la colonne invalide. (columnIndex=" + columnIndex + " non compris entre 0 et columnList.size()=" + columnList.size());
		
		
		IndexTreeDic alreadyExistingTree = findOrCreateAssociatedIndexTree(columnIndex, true); /*findTreeAssociatedWithColumnIndex(columnIndex);
		Log.info("Arbre existe = " + alreadyExistingTree);
		if (alreadyExistingTree == null) {
			alreadyExistingTree = new IndexTreeDic();
			indexTreeList.add(alreadyExistingTree);
		}*/
		
		//IndexTreeDic indexingObject = new IndexTreeDic();
		try {
			alreadyExistingTree.indexColumnFromDisk(associatedTable, columnIndex);
		} catch (Exception e) {
			throw new StructureException(e);
		}
	}
	
	
	// ---- RuntimeIndexing : servant à stocker les champs à indexer ----
	
	// Pour l'instant, il n'y a que le spport des index mono-colonne.
	// Faire une recherche sur une colonne équivaut à trouver l'index qui traîte de la colonne, et à faire la recherche dessus.
	
	
	public void createRuntimeIndexingColumn(int columnIndex) throws Exception { // addInitialColumnAndCreateAssociatedIndex
		if (associatedTable == null) throw new Exception("Aucune table crée, indexation impossible.");
		List<Column> columnList = associatedTable.getColumns();
		if (columnIndex < 0 || columnIndex >= columnList.size()) throw new Exception("Index de la colonne invalide. (columnIndex=" + columnIndex + " non compris entre 0 et columnList.size()=" + columnList.size());
		
		IndexTreeDic alreadyExistingTree = findOrCreateAssociatedIndexTree(columnIndex, true); //findTreeAssociatedWithColumnIndex(columnIndex);
		
		RuntimeIndexingEntry indexEntry = new RuntimeIndexingEntry();
		indexEntry.associatedIndexTree = alreadyExistingTree;
		indexEntry.associatedColumn = columnList.get(columnIndex);
		indexEntry.associatedTable = associatedTable;
		indexEntry.columnIndex = columnIndex;
		runtimeIndexingList.add(indexEntry);
		indexEntry.associatedIndexTree.initialiseWithTableAndColumn(associatedTable, columnIndex); // Pour pouvoir indexer au runtime (lors du parsing)
	}
	
	
	public void createRuntimeIndexingColumn(String columnName) throws Exception {
		if (associatedTable == null) throw new Exception("Aucune table crée, indexation impossible.");
		int colIndex = getColumnIndex(columnName);
		if (colIndex == -1) throw new Exception("Colonne introuvable, impossible de l'indexer.");
		createRuntimeIndexingColumn(colIndex);
	}
	
	
	
	//public boolean makeQuery(STableQuery query) {
		
	//}
	
	/*public IndexTreeDic findTreeAssociatedWithColumnIndex(int columnIndex) {
		for (IndexTreeDic indexTree : indexTreeList) {
			if (indexTree.getAssociatedTableColumnIndex() == columnIndex) {
				return indexTree;
			}
		}
		return null;
	}*/
	
	
	public DataPositionList findIndexedResultsOfColumn(String columnName, Object exactValue) throws Exception {
		return findIndexedResultsOfColumn(columnName, exactValue, null, true);
	}
	
	/**
	 * 
	 * @param columnName
	 * @param minValue
	 * @param maxValue
	 * @param inclusive
	 * @return
	 * @throws Exception
	 */
	@Deprecated // TODO in favor to Index.getPositionsFromPredicate
	public DataPositionList findIndexedResultsOfColumn(String columnName, Object minValue, Object maxValue, boolean inclusive) throws Exception {
		int columnIndex = getColumnIndex(columnName);
		if (columnIndex == -1) throw new Exception("Colonne introuvable, impossible de faire une recherche sur ses index.");
		if (IndexTreeDic.firstValueIsHigherThatSecondValue(minValue, maxValue) > 0) {
			return new DataPositionList(); // aucun résultat
		}
		//return findIndexedResultsOfColumn();
		
		IndexTreeDic makeRequestOnThisTree = findOrCreateAssociatedIndexTree(columnIndex, false); //findTreeAssociatedWithColumnIndex(columnIndex);
		/*for (IndexTreeDic indexTree : indexTreeList) {
			if (indexTree.getAssociatedTableColumnIndex() == columnIndex) {
				makeRequestOnThisTree = indexTree;
				break;
			}
		}*/
		if (makeRequestOnThisTree == null) {
			return new DataPositionList();
		}
		return makeRequestOnThisTree.findMatchingBinIndexes(minValue, maxValue, inclusive, false);
	}
	
	
	public int evaluateNumberOfResults(Collection<DataPositionList> resultsCollection) {
		// Iterates over all the results
		int numberOfResults = 0;
		for (DataPositionList longList : resultsCollection) {
			//Log.info("list size = " + list.size());
			numberOfResults += longList.size();
			//numberOfLines++;
			/*if (false)
			for (Long index : longList) {
				// un-comment those lines if you want to get the full info on lines : List<Object> objList = table.getValuesOfLineById(index);
				//Log.info("  index = " + index);
				List<Object> objList = table.getValuesOfLineById(index);
				Object indexedValue = objList.get(indexingColumnIndex);
				//indexingColumn.getDataType().
				Log.info("  valeur indexée = " + indexedValue);
				//Log.info("  objList = " + objList);
			}*/
		}
		return numberOfResults;
	}

	//trip_distance
	public int evaluateNumberOfArrayListLines(Collection<DataPositionList> resultsCollection) {
		return resultsCollection.size();
	}
	
	private static boolean debugUseOldDeprecatedSearch = false; // bench : la nouvelle manière va environ 80x plus vite ^^
	
	
	public ResultSet getFullResultsFromBinIndexes(DataPositionList resultsCollection) throws StructureException, IOException { // table connue ! , Table fromTable) {
		return getFullResultsFromBinIndexes(resultsCollection, true, -1);
	}
	/**
	 *  @param resultsCollection
	 *  @param waitForAllResults
	 *  @param waitTimeLimitMs
	 *  @return
	 *  @throws Exception
	 */
	public ResultSet getFullResultsFromBinIndexes(DataPositionList resultsCollection, boolean waitForAllResults, int waitTimeLimitMs) throws StructureException, IOException{ // table connue ! , Table fromTable) {
		return getFullResultsFromBinIndexes(resultsCollection, waitForAllResults, waitTimeLimitMs, null);
	}
	//trip_distance
	
	/** 
	 *  @param resultsCollection
	 *  @param waitForAllResults
	 *  @param waitTimeLimitMs
	 *  @param onlyGetThoseColumnsIndex null si renvoyer tout les champs
	 *  @return
	 *  @throws Exception
	 */
	//public ArrayList<ArrayList<Object>> getFullResultsFromBinIndexes(DataPositionList resultsCollection) throws Exception { // table connue ! , Table fromTable) {
	public ResultSet getFullResultsFromBinIndexes(DataPositionList resultsCollection, boolean waitForAllResults, int waitTimeLimitMs, ArrayList<Integer> onlyGetThoseColumnsIndex) throws StructureException, IOException { // table connue ! , Table fromTable) {
		if (associatedTable == null) throw new StructureException("Aucune table crée, indexation impossible.");
		
		ResultSet resultArrayList = new ResultSet();
		
		TableDataHandler dataHandler = associatedTable.getDataHandler();
		
		if (debugUseOldDeprecatedSearch == false) {
			return dataHandler.getValuesOfLinesListById(resultsCollection, waitForAllResults, waitTimeLimitMs, onlyGetThoseColumnsIndex);
		} else {
			
			// Pour toutes les listes de valeurs identiques
			// (il peut y avoir des listes distinctes associés à une même valeur indexée, du fait du multi-fichiers / multi-thread)
			for (DiskDataPosition dataPos : resultsCollection) {
				//Log.info("list size = " + list.size());
				//for (DiskDataPosition dataPos : dataPosList) {
					// un-comment those lines if you want to get the full info on lines : List<Object> objList = table.getValuesOfLineById(index);
					ArrayList<Object> objList = dataHandler.getValuesOfLineByIdForSignleQuery(dataPos);
					resultArrayList.add(objList);
					//Log.info("  objList = " + objList);
					
					//Log.info("  index = " + index);
					// TO DO Résultats à lire depuis les fichiers binaires -> OK !!!
					//ArrayList<Object> objList = associatedTable.getValuesOfLineById(binIndex);
					//resultArrayList.add(objList);
					
					//Object indexedValue = objList.get(indexingColumnIndex);
					//indexingColumn.getDataType().
					//Log.info("  valeur indexée = " + indexedValue);
					//Log.info("  objList = " + objList);
				//}
			}
			return resultArrayList;
		}
	}
	
	public void displayOnLogResults(ResultSet resultArrayList) {
		for (List<Object> objList : resultArrayList) {
			Log.info("  objList = " + objList);
		}
	}
	
	public void flushEveryIndexOnDisk() throws IOException {
		for (IndexTreeDic indexTree : indexTreeList) {
			indexTree.flushOnDisk();
		}
		
	}
	
	public void multiThreadParsingAddAndStartCsv(String csvPath, boolean doRuntimeIndexing) { synchronized(multiThreadParsingListLock) {
		Thread newParsingThread = new Thread(() -> {
			try {
				this.parseCsvData(csvPath, doRuntimeIndexing);
			} catch (Exception e) {
				Log.error(e);
				e.printStackTrace();
			}
		});
		parsingThreadList.add(newParsingThread);
		newParsingThread.start();
	} }
	
	
	public void multiThreadParsingAddAndStartCsv(InputStream csvStream, boolean doRuntimeIndexing, boolean closeStreamAfterUsage) { synchronized(multiThreadParsingListLock) {
		Thread newParsingThread = new Thread(() -> {
			try {
				this.parseCsvData(csvStream, doRuntimeIndexing, closeStreamAfterUsage);
			} catch (Exception e) {
				Log.error(e);
				e.printStackTrace();
			}
		});
		parsingThreadList.add(newParsingThread);
		newParsingThread.start();
	} }
	
	public void multiThreadParsingWaitForAllThreads() {
		multiThreadParsingJoinAllThreads();
	}
	
	/** Attendre que tous les threads soient finis.
	 *  Si un thread a rencontré une erreur et ne peut pas être arrêté, il est gardé dans la liste et je passe au suivant.
	 */
	public void multiThreadParsingJoinAllThreads() { synchronized(multiThreadParsingListLock) {
		
		int invalidThreadNumber = 0;
		while (invalidThreadNumber < parsingThreadList.size()) {
			Thread parsingThread = parsingThreadList.get(invalidThreadNumber);
			try {
				parsingThread.join();
				parsingThreadList.remove(invalidThreadNumber);
				// Je resue au même index, donc
			} catch (InterruptedException e) {
				Log.error(e);
				e.printStackTrace();
				invalidThreadNumber++; // je passe au thread suivant, celi-là est invalide
			}
		}
		
	} }
	
	public void multiThreadParsingInit() {
		
	}
	
	public void multiThreadParsingAddFile() {
		
	}
	
	public void clearDataDirectory() throws IOException {
		associatedTable.getDataHandler().clearDataDirectory();
	}
	
}
