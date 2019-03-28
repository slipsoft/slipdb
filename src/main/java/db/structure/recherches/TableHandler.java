package db.structure.recherches;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.dant.utils.Log;

import db.data.DataType;
import db.disk.dataHandler.DiskDataPosition;
import db.disk.dataHandler.TableDataHandler;
import db.data.DataPositionList;
import db.parsers.CsvParser;
import db.structure.Column;
import db.structure.Table;
import db.structure.indexTree.IndexTreeDic;
import sj.network.tcpAndBuffers.NetBuffer;

public class TableHandler {
	
	protected String tableName;
	protected ArrayList<Column> columnsList = new ArrayList<Column>();
	protected Table associatedTable;
	//protected CsvParser csvParser = null;
	// Possibilité de parser de plusieurs manières différentes (un jour...)
	protected boolean firstTimeParsingData = true;
	
	// Liste des threads faisant actuellement du parsing
	
	protected ArrayList<Thread> parsingThreadList = new ArrayList<Thread>();
	
	protected ArrayList<IndexTreeDic> indexTreeList = new ArrayList<IndexTreeDic>(); // Liste des IndexTree associés à cette table
	
	
	/** @Nicolas : ne pas supprimer ce code, mettre ton code en plus dans une autre fonction.
	 *  Sera fait avec la serialisation plus rapide et fidèle 
	 *  Ecrire l'état actuel de la table :
	 *  Nom, liste de colonnes, arbres
	 * @param writeInStream
	 */
	public void writeTableFull(DataOutputStream writeInStream) {
		NetBuffer tableData = new NetBuffer();
		
		
		
	}
	
	
	public void forceAppendNotFirstParsing() {
		firstTimeParsingData = false; // si à vrai, supprimer tous les fichiers connus
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public TableHandler(String argTableName) {
		tableName = argTableName;
	}
	
	/**
	 *  Ajouter une colonne, suppose qu'il n'y a pas encore de données dans la table
	 *  @param argColumnName
	 *  @param argColumnDataType
	 * @throws Exception 
	 */
	public void addColumn(String argColumnName, DataType argColumnDataType) throws Exception {
		for (Column col : columnsList) {
			if (col.getName().equals(argColumnName)) throw new Exception("Ajout de la colonne impossibe, il en exie déjà une du même nom : " + argColumnName);
		}
		Column newColumn = new Column(argColumnName, argColumnDataType);
		columnsList.add(newColumn);
	}

	public void addColumn(Column column) {
		columnsList.add(column);
	}
	
	public Table createTable() throws IOException {
		associatedTable = new Table(tableName, columnsList, this);
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

	public void parseCsvData (InputStream is, boolean doRuntimeIndexing) throws Exception {
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
		CsvParser csvParser = new CsvParser(associatedTable);
		
		if (doRuntimeIndexing)
			csvParser.setRuntimeIndexing(runtimeIndexingList);
		else
			csvParser.setRuntimeIndexing(null);
		
		csvParser.parse(csvStream, !firstTimeParsingData);
		if (closeStreamAfterUsage)
			csvStream.close();
		
		firstTimeParsingData = false;
		
	}
	
	protected int getColumnIndex(String columnName) throws Exception {
		if (associatedTable == null) throw new Exception("Aucune table crée, indexation impossible.");
		List<Column> columnList = associatedTable.getColumns();
		for (int colIndex = 0; colIndex < columnList.size(); colIndex++) {
			Column currentColumn = columnList.get(colIndex);
			if (currentColumn.getName().equals(columnName)) {
				return colIndex;
			}
		}
		return -1;
	}
	
	public void indexColumnWithTreeFromDisk(String columnName) throws Exception {
		int colIndex = getColumnIndex(columnName);
		if (colIndex == -1) throw new Exception("Colonne introuvable, impossible de l'indexer.");
		indexColumnWithTreeFromDisk(colIndex);
	}
	
	
	
	protected Object indexTreeListLock = new Object();
	
	protected IndexTreeDic findOrCreateAssociatedIndexTree(int columnIndex, boolean createTreeIfDoesNotExists) throws Exception { synchronized(indexTreeListLock) {
		for (IndexTreeDic indexTree : indexTreeList) {
			if (indexTree.getAssociatedTableColumnIndex() == columnIndex) {
				return indexTree;
			}
		}
		if (createTreeIfDoesNotExists == false) return null;
		IndexTreeDic newTree = new IndexTreeDic(associatedTable, columnIndex);
		indexTreeList.add(newTree);
		return newTree;
	} }
	
	/** Pas
	 *  @param columnIndex
	 *  @throws Exception
	 */
	public void indexColumnWithTreeFromDisk(int columnIndex) throws Exception {
		if (associatedTable == null) throw new Exception("Aucune table crée, indexation impossible.");
		List<Column> columnList = associatedTable.getColumns();
		if (columnIndex < 0 || columnIndex >= columnList.size()) throw new Exception("Index de la colonne invalide. (columnIndex=" + columnIndex + " non compris entre 0 et columnList.size()=" + columnList.size());
		
		
		IndexTreeDic alreadyExistingTree = findOrCreateAssociatedIndexTree(columnIndex, true); /*findTreeAssociatedWithColumnIndex(columnIndex);
		Log.info("Arbre existe = " + alreadyExistingTree);
		if (alreadyExistingTree == null) {
			alreadyExistingTree = new IndexTreeDic();
			indexTreeList.add(alreadyExistingTree);
		}*/
		
		//IndexTreeDic indexingObject = new IndexTreeDic();
		alreadyExistingTree.indexColumnFromDisk(associatedTable, columnIndex);
	}
	
	
	// ---- RuntimeIndexing : servant à stocker les champs à indexer ----
	
	// Pour l'instant, il n'y a que le spport des index mono-colonne.
	// Faire une recherche sur une colonne équivaut à trouver l'index qui traîte de la colonne, et à faire la recherche dessus.
	
	
	// indexColumnList est la liste des colonnes à indexer
	public RuntimeIndexingEntryList runtimeIndexingList = new RuntimeIndexingEntryList();//ArrayList<SRuntimeIndexingEntry>();
	
	
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
	
	
	/** Pour une valeur exacte
	 *  @param columnName
	 *  @param equalsExactValue
	 *  @return
	 *  @throws Exception
	 */
	public DataPositionList findIndexedResultsOfColumn(String columnName, Object equalsExactValue) throws Exception {
		return findIndexedResultsOfColumn(columnName, equalsExactValue, null, true);
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
	
	//trip_distance
	public ArrayList<ArrayList<Object>> getFullResultsFromBinIndexes(DataPositionList resultsCollection) throws Exception { // table connue ! , Table fromTable) {
		if (associatedTable == null) throw new Exception("Aucune table crée, indexation impossible.");
		
		ArrayList<ArrayList<Object>> resultArrayList = new ArrayList<ArrayList<Object>>();
		
		TableDataHandler dataHandler = associatedTable.getDataHandler();
		
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
				// TODO
				// TODO
				// TODO Résultats à lire depuis les fichiers binaires
				// TODO
				// TODO
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
	
	public void displayOnLogResults(ArrayList<ArrayList<Object>> resultArrayList) {
		for (ArrayList<Object> objList : resultArrayList) {
			Log.info("  objList = " + objList);
		}
	}
	
	public void flushEveryIndexOnDisk() throws IOException {
		for (IndexTreeDic indexTree : indexTreeList) {
			indexTree.flushOnDisk();
		}
		
	}
	
	private Object multiThreadParsingListLock = new Object();
	
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
