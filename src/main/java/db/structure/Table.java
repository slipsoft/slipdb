package db.structure;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.dant.entity.ColumnEntity;
import com.dant.entity.TableEntity;
import com.dant.utils.EasyFile;
import com.dant.utils.Log;

import db.data.load.CsvParser;
import db.data.load.Loader;
import db.data.load.Parser;
import db.data.types.DataType;
import db.disk.dataHandler.DiskDataPosition;
import db.disk.dataHandler.TableDataHandler;
import db.search.Predicate;
import db.search.ResultSet;
import index.indexTree.IndexException;

/**
 * A simple SQL-like table, consisting of
 */
public class Table implements Serializable {
	private static final long serialVersionUID = 2266328195200925214L;
	private final static short currentNodeID = 1;

	@Deprecated private static String oldSmellyBasePath = "target/tables/";
	final private static String baseAllTablesDirPath = "data_save/tables/";
	final public static String allTablesFileExtension = ".sbin";
	
	private final int tableID;
	private final short nodeID; // TODO à faire plus tard : importation d'une table depuis un autre noeud
	private final String baseTablePath;
	private int lineDataSize;
	
	
	//protected final String dataFilesOnDiskBasePath; devenu baseTablePath
	protected final TableDataHandler dataHandler;
	
	protected final String name; // table name
	
	// obj non serial
	transient private final Object multiThreadParsingListLock = new Object();
	transient protected ArrayList<Thread> parsingThreadList = new ArrayList<>();
	
	@Deprecated protected EasyFile fileLinesOnDisk; // <- le système de fichiers à changé (il est mieux maintenant)
	private List<Column> columnsList = new ArrayList<>(); // liste des colonnes de la table
	/* @CurrentlyUseless */ protected List<Index> indexesList = new ArrayList<>();   // liste des index générés pour cette table
	// -> Dans TableHandler.indexTreeList pour l'instant
	
	/**
	 * Plus tard : Evolution, pour permettre le multi-thread, sauvegarder et indexer plus vite, avoir plusieurs fichiers par colonne, sauvegarde des données en entrée par colonne.
	 * Pour l'instant, je laisse comme ça (Sylvain), et je fais l'index par dichotomie
	 */
	
	public void doBeforeSerialWrite() {
		this.flushAllIndexOnDisk();
	}

	public void debugSerialShowVariables() {
		Log.info("TableDataHandler : ");
		dataHandler.debugSerialShowVariables();
	}

	public Table(String name) throws IOException {
		this(name, new ArrayList<>());
	}
	
	/** 
	 * Create a table with a name and a columns list
	 * @param argName - name of the table
	 * @param argColumnsList - list of columns
	 * @throws IOException - if cannot create file
	 */
	public Table(String argName, List<Column> argColumnsList) throws  IOException {
		this(argName, argColumnsList, currentNodeID, Database.getInstance().getAndIncrementNextTableID());
	}
	
	public Table(String argName, List<Column> argColumnsList, short argNodeID, int argTableID) throws IOException {
		name = argName;
		columnsList.addAll(argColumnsList);
		
		baseTablePath = baseAllTablesDirPath + name + "/";
		dataHandler = new TableDataHandler(this, baseTablePath);
		tableID = argTableID;
		nodeID = argNodeID;
		/* Désormais géré par TableDiskDataHandler*/
		this.fileLinesOnDisk = new EasyFile(oldSmellyBasePath + name + ".bin");
		this.fileLinesOnDisk.createFileIfNotExist();
		computeLineDataSize();
	}
	
	public String getBaseTablePath() {
		return baseTablePath;
	}

	public String getName() {
		return name;
	}

	public List<Column> getColumns() {
		return columnsList;
	}

	public List<Index> getIndexes() {
		return indexesList;
	}

	public TableDataHandler getDataHandler() {
		return dataHandler;
	}

	public int getLineSize() {
		return lineDataSize;
	}

	@Deprecated
	public EasyFile getFileLinesOnDisk() {
		return fileLinesOnDisk;
	}

	public Index createIndex(String columnName, Class<? extends Index> indexClass) throws StructureException {
		try {
			Constructor<? extends Index> ct = indexClass.getConstructor(Table.class, Column.class);
			Column column = this.getColumnByNameNoCheck(columnName).orElseThrow(() -> new StructureException("column " + columnName + " doesn't exist"));
			Index newIndex = ct.newInstance(this, column);
			this.addIndex(newIndex);
			return newIndex;
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new StructureException(e);
		}
	}

	public void addIndex(Index index) {
		this.indexesList.add(index);
		index.getIndexedColumn().addIndex(index);
	}

	private void computeLineDataSize() {
		lineDataSize = columnsList
				.stream()
				.mapToInt(Column::getSize)
				.sum();
	}

	public Optional<Column> getColumnByName(String columnName) {
		if (com.dant.utils.Utils.validateRegex(Database.getInstance().config.columnNamePattern, columnName)) {
			return this.columnsList.stream().filter(c -> c.getName().equals(columnName)).findFirst();
		}
		return Optional.empty();
	}
	public Optional<Column> getColumnByNameNoCheck(String columnName) {
		return this.columnsList.stream().filter(c -> c.getName().equals(columnName)).findFirst();
	}

	private boolean columnExist(String name) {
		return this.columnsList.stream().anyMatch(col -> col.getName().equals(name));
	}

	public void addColumn(String colName, DataType dataType, boolean argKeepDataInMemory, boolean argWriteDataOnDisk) throws StructureException {
		Column newColumn = new Column(colName, dataType, argKeepDataInMemory, argWriteDataOnDisk);
		this.addColumn(newColumn);
	}

	/**
	 * Add a column
	 *
	 * @param colName - name of the column
	 * @param dataType - type of the column
	 * @throws StructureException - if column allready exists
	 */
	public void addColumn(String colName, DataType dataType) throws StructureException {
		Column newColumn = new Column(colName, dataType);
		this.addColumn(newColumn);
	}

	/**
	 * Add a column
	 *
	 * @param column - column to add to the table
	 * @throws StructureException - if column allready exists
	 */
	public void addColumn(Column column) throws StructureException {
		if (columnExist(column.getName())) throw new StructureException("Column already exists, colName = " + column.getName());
		column.setNumber(columnsList.size());
		columnsList.add(column);
		computeLineDataSize();
	}

	/**
	 * Trouver l'index d'une colonne à partir de son nom, dans la liste columns
	 *
	 * @param colName nom de la colonne à rechercher
	 * @return -1 si introuvable, un entier >= 0 si existe
	 */
	public int findColumnNumber(String colName) {
		for (int colIndex = 0; colIndex < columnsList.size(); colIndex++) {
			Column columnAtIndex = columnsList.get(colIndex);
			if (columnAtIndex.name.equals(colName)) {
				return colIndex;
			}
		}
		return -1;
	}

	/**
	 * Give each column a number
	 */
	public void setColumnsNumber() {
		for (int colIndex = 0; colIndex < columnsList.size(); colIndex++) {
			columnsList.get(colIndex).setNumber(colIndex);
		}
	}

	/** Thread-safe, la table est en lecture seule
	 * @param csvStream - stream contenant les données en CSV à parser
	 * @param doRuntimeIndexing
	 * @throws Exception
	 */
	public void loadData(Parser parser, InputStream csvStream, boolean doRuntimeIndexing) {
		//if (csvParser == null)
		// Thread-safe
		Loader csvLoader = new Loader(this, parser, doRuntimeIndexing);
		csvLoader.parse(csvStream, true);

	}

	public void multiThreadParsingAddAndStartCsv(String csvPath, boolean doRuntimeIndexing) {
		synchronized(multiThreadParsingListLock) {
			Thread newParsingThread = new Thread(() -> {
				try (InputStream is = new FileInputStream(csvPath)) {
					this.loadData(new CsvParser(), is, doRuntimeIndexing);
				} catch (Exception e) {
					Log.error(e);
				}
			});
			parsingThreadList.add(newParsingThread);
			newParsingThread.start();
		}
	}

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

	/**
	 * Save all indexes to disk
	 */
	public void flushAllIndexOnDisk() {
		Log.debug("TableHandler.flushAllIndexOnDisk : size = " + indexesList.size());
		for (Index index : indexesList) {
			try {
				index.flushOnDisk();
				Log.error("TableHandler.flushAllIndexOnDisk : flush de l'arbre !" + index);
			} catch (IOException e) {
				Log.error("TableHandler.flushAllIndexOnDisk : l'arbre n'a pas pu être écrit sur le disque, IOException.");
				Log.error(e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Add an entry in every index of the table
	 * @param entry - an array of object representing an entry
	 * @param position - the Disk position of this entry
	 * @throws IndexException - if one of the indexes couldn't index this entry
	 */
	public void indexEntry(Object[] entry, DiskDataPosition position) throws IndexException {
		for (Index index: indexesList) {
			index.indexEntry(entry, position);
		}
	}

	/**
	 * @param lineId - the position of the line, 0 being the first loaded line from the file (CSV for New York taxis)
	 * @return - a list containing every entry associates with the line at position lineId
	 * @throws IOException
	 */
	@Deprecated
	public ArrayList<Object> getValuesOfLineById(long lineId) throws IOException { // or getRowById
		// TODO fonction à refaire avec des DiskDataPosition
		// Get a new disposable FileInputStream with the file where all table rows are stored
		FileInputStream fileAsStream = new FileInputStream(fileLinesOnDisk);

		// List of values stored in the line of id lineId
		ArrayList<Object> lineValues = new ArrayList<>(); // rowValues

		// Seek to the right position in the stream
		fileAsStream.skip(lineId * getLineSize());

		// For each column, reads the associated value
		for (Column column : columnsList) {
			byte[] columnValueAsByteArray = new byte[column.getSize()];
			fileAsStream.read(columnValueAsByteArray); // reads from the stream
			//Log.debug(b); for debug purposes only
			lineValues.add(column.getDataType().readTrueValue(columnValueAsByteArray));
		}
		fileAsStream.close();
		return lineValues;
	}

	public ResultSet getFullResultsFromBinIndexes(Collection<DiskDataPosition> resultsCollection) { // table connue ! , Table fromTable) {
		return getFullResultsFromBinIndexes(resultsCollection, true, -1);
	}

	/**
	 * @param resultsCollection - positions list
	 * @param waitForAllResults -
	 * @param waitTimeLimitMs - time limit
	 * @return a full resultset
	 */
	public ResultSet getFullResultsFromBinIndexes(Collection<DiskDataPosition> resultsCollection, boolean waitForAllResults, int waitTimeLimitMs) { // table connue ! , Table fromTable) {
		return getFullResultsFromBinIndexes(resultsCollection, waitForAllResults, waitTimeLimitMs, null);
	}

	/**
	 * @param resultsCollection - positions list
	 * @param waitForAllResults -
	 * @param waitTimeLimitMs - time limit
	 * @param onlyGetThoseColumnsIndex null si renvoyer tout les champs
	 * @return a full resultset
	 */
	public ResultSet getFullResultsFromBinIndexes(Collection<DiskDataPosition> resultsCollection, boolean waitForAllResults, int waitTimeLimitMs, ArrayList<Integer> onlyGetThoseColumnsIndex) { // table connue ! , Table fromTable) {
		return getDataHandler().getValuesOfLinesListById(resultsCollection, waitForAllResults, waitTimeLimitMs, onlyGetThoseColumnsIndex);
	}

	/**
	 * Returns the best index to use for a given filter
	 * @param predicate a predicate without index
	 * @return Index
	 * @throws StructureException if no index is found
	 */
	public Index findBestIndex(Predicate predicate) throws StructureException {
		for (Index index : indexesList) {
			if (index.canBeUsedWithPredicate(predicate)) {
				return index;
			}
		}
		throw new StructureException("no index can be used with this filter");
	}

	public TableEntity convertToEntity() {
		String name = this.name;
		ArrayList<ColumnEntity> allColumns = this.columnsList.stream().map(Column::convertToEntity).collect(Collectors.toCollection(ArrayList::new));
		// ArrayList<IndexEntity> allIndexes = this.indexesList.stream().map(Index::convertToEntity).collect(Collectors.toCollection(ArrayList::new));
		return new TableEntity(name, allColumns);
	}

	public void clearDataDirectory() throws IOException {
		this.dataHandler.clearDataDirectory();
	}

}
