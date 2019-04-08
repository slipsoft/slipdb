package db.structure;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.dant.entity.ColumnEntity;
import com.dant.entity.TableEntity;
import com.dant.utils.EasyFile;
import com.dant.utils.Log;

import db.data.types.DataPositionList;
import db.data.types.DataType;
import db.disk.dataHandler.TableDataHandler;
import db.search.Predicate;
import db.search.ResultSet;
import db.structure.recherches.TableHandler;

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
	@Deprecated protected TableHandler tableHandler;
	
	protected final String name; // table name
	
	@Deprecated protected EasyFile fileLinesOnDisk; // <- le système de fichiers à changé (il est mieux maintenant)
	private List<Column> columnsList = new ArrayList<>(); // liste des colonnes de la table
	/* @CurrentlyUseless */ @Deprecated protected List<Index> indexesList = new ArrayList<>();   // liste des index générés pour cette table
	// -> Dans TableHandler.indexTreeList pour l'instant
	
	/**
	 * Plus tard : Evolution, pour permettre le multi-thread, sauvegarder et indexer plus vite, avoir plusieurs fichiers par colonne, sauvegarde des données en entrée par colonne.
	 * Pour l'instant, je laisse comme ça (Sylvain), et je fais l'index par dichotomie
	 */
	
	public void doBeforeSerialWrite() {
		if (tableHandler != null) {
			tableHandler.flushAllIndexTreesOnDisk();
		}
	}
	
	public void debugSerialShowVariables() {
		Log.info("TableDataHandler : ");
		dataHandler.debugSerialShowVariables();
	}
	
	/** 
	 * Create a table with a name and a columns list
	 * @param argName - name of the table
	 * @param argColumnsList - list of columns
	 * @param argTableHandler - table handler
	 * @throws IOException - if cannot create file
	 */
	public Table(String argName, List<Column> argColumnsList, TableHandler argTableHandler) throws  IOException {
		this(argName, argColumnsList, currentNodeID, Database.getInstance().getAndIncrementNextTableID(), argTableHandler);
	}
	
	public Table(String argName, List<Column> argColumnsList, short argNodeID, int argTableID, TableHandler argTableHandler) throws IOException {
		name = argName;
		columnsList.addAll(argColumnsList);
		tableHandler = argTableHandler;
		if (tableHandler == null) {
			tableHandler = new TableHandler(argName);
		}
		
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

	public TableHandler getTableHandler() {
		return tableHandler;
	}

	public int getLineSize() {
		return lineDataSize;
	}

	@Deprecated
	public EasyFile getFileLinesOnDisk() {
		return fileLinesOnDisk;
	}

	public void addIndex(Index index) {
		this.indexesList.add(index);
		for (Column column : index.getColumnList()) {
			column.addIndex(index);
		}
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

	/**
	 * Ajout d'une colonne
	 *
	 * @param colName - name of the column
	 * @throws StructureException - if column allready exists
	 */
	public void addColumn(String colName, DataType dataType) throws StructureException {
		if (columnExist(colName)) throw new StructureException("Column already exists, colName = " + colName);
		// Ajout de la colonne
		Column newColumn = new Column(colName, dataType).setNumber(columnsList.size());
		columnsList.add(newColumn);
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

	/**
	 * @param lineId is the position of the line, 0 being the first loaded line from the file (CSV for New York taxis)
	 * @return a list containing every entry associates with the line at position lineId
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

	public ResultSet getFullResultsFromBinIndexes(DataPositionList resultsCollection) { // table connue ! , Table fromTable) {
		return getFullResultsFromBinIndexes(resultsCollection, true, -1);
	}

	/**
	 * @param resultsCollection - positions list
	 * @param waitForAllResults -
	 * @param waitTimeLimitMs - time limit
	 * @return a full resultset
	 */
	public ResultSet getFullResultsFromBinIndexes(DataPositionList resultsCollection, boolean waitForAllResults, int waitTimeLimitMs) { // table connue ! , Table fromTable) {
		return getFullResultsFromBinIndexes(resultsCollection, waitForAllResults, waitTimeLimitMs, null);
	}

	/**
	 * @param resultsCollection - positions list
	 * @param waitForAllResults -
	 * @param waitTimeLimitMs - time limit
	 * @param onlyGetThoseColumnsIndex null si renvoyer tout les champs
	 * @return a full resultset
	 */
	public ResultSet getFullResultsFromBinIndexes(DataPositionList resultsCollection, boolean waitForAllResults, int waitTimeLimitMs, ArrayList<Integer> onlyGetThoseColumnsIndex) { // table connue ! , Table fromTable) {
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

}
