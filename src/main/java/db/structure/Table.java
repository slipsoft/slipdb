package db.structure;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.dant.entity.ColumnEntity;
import com.dant.entity.TableEntity;
import com.dant.utils.EasyFile;

import db.data.DataType;
import db.disk.dataHandler.TableDataHandler;
import db.search.Predicate;

/**
 * A simple SQL-like table, consisting of 
 */
public class Table {

	protected static String oldSmellyBasePath = "target/tables/";
	final public static String baseAllTablesDirPath = "data_save/tables/";
	final public static String allTablesFileExtension = ".sbin";
	protected static AtomicInteger nextTableID = new AtomicInteger(1);
	
	protected final int tableID;
	protected final String baseTablePath;
	
	
	//protected final String dataFilesOnDiskBasePath; devenu baseTablePath
	protected final TableDataHandler dataHandler;
	
	protected final String name; // table name
	
	protected EasyFile fileLinesOnDisk; // <- les fichiers de sauvegarde des colonnes sont désormais indépendants
	protected List<Column> columnsList = new ArrayList<Column>(); // liste des colonnes de la table
	protected List<Index> indexesList = new ArrayList<Index>();   // liste des index générés pour cette table
	
	/**
	 * Plus tard : Evolution, pour permettre le multi-thread, sauvegarder et indexer plus vite, avoir plusieurs fichiers par colonne, sauvegarde des données en entrée par colonne.
	 * Pour l'instant, je laisse comme ça (Sylvain), et je fais l'index par dichotomie
	 */
	
	/** Create a table with a name and a columns list
	 * @param name
	 * @param columnsList
	 * @throws IOException
	 */
	public Table(String argName, List<Column> argColumnsList) throws IOException {
		this.name = argName;
		this.columnsList.addAll(argColumnsList);
		baseTablePath = baseAllTablesDirPath + name + "/";
		dataHandler = new TableDataHandler(this, baseTablePath);
		tableID = nextTableID.addAndGet(1);
		
		/* Désormais géré par TableDiskDataHandler*/
		this.fileLinesOnDisk = new EasyFile(oldSmellyBasePath + name + ".bin");
		this.fileLinesOnDisk.createFileIfNotExist();
		
	}
	
	// dataHandler
	
	public String getName() {
		return name;
	}
	
	public List<Column> getColumns() {
		return columnsList;
	}
	
	public List<Index> getIndexes() {
		return indexesList;
	}
	
	public void addIndex(Index index) {
		this.indexesList.add(index);
		for (Column column : index.getColumnList()) {
			column.addIndex(index);
		}
	}
	
	public int getLineSize() {
		return columnsList
		    .stream()
		    .mapToInt(Column::getSize)
		    .sum();
		// -> do a performance benchmark with this function (seems very fast)
		/*
		Same as :
		
		int lineSize = 0;
		for (int columnIndex = 0; columnIndex < columnsList.size(); columnIndex++) {
			lineSize += columnsList.get(columnIndex).getSize();
		}
		return lineSize;
		
		*/
	}
	
	public boolean columnExist(String name) {
		return this.columnsList.stream().anyMatch(col -> col.getName() == name);
	}
	
	/**
	 * Ajout d'une colonne
	 * @param colName
	 * @param defaultFillValue
	 * @return
	 * @throws Exception
	 */
	public boolean addColumn(String colName, DataType dataType) throws Exception {
		if (columnExist(colName)) throw new Exception("Column already exists, colName = " + colName);
		// Ajout de la colonne
		Column newColumn = new Column(colName, dataType).setNumber(columnsList.size());
		columnsList.add(newColumn);
		return true;
	}
	
	/**
	 * Trouver l'index d'une colonne à partir de son nom, dans la liste columns
	 * @param colName  nom de la colonne à rechercher
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
	public void setColumnsNumber(String colName) {
		for (int colIndex = 0; colIndex < columnsList.size(); colIndex++) {
			columnsList.get(colIndex).setNumber(colIndex);
		}
	}

	/**
	 * 
	 * @param lineId is the position of the line, 0 being the first loaded line from the file (CSV for New York taxis)
	 * @return a list containing every entry associates with the line at position lineId
	 * @throws IOException
	 */
	public ArrayList<Object> getValuesOfLineById(long lineId) throws IOException { // or getRowById
		
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

	public OutputStream tableToOutputStream(boolean appendAtTheEnd) throws FileNotFoundException {
		return new FileOutputStream(fileLinesOnDisk, appendAtTheEnd);
	}
	
	public EasyFile getFileLinesOnDisk() {
		return fileLinesOnDisk;
	}
	
	/**
	 * Returns the best index to use for a given filter
	 * @param predicate
	 * @return
	 * @throws Exception 
	 */
	public Index findBestIndex(Predicate predicate) throws Exception {
		for (Index index : indexesList) {
			if (index.canBeUsedWithPredicate(predicate)) {
				return index;
			}
		}
		throw new Exception("no index can be used with this filter");
	}

	public TableEntity convertToEntity () {
		String name = this.name;
		ArrayList<ColumnEntity> allColumns = this.columnsList.stream().map(Column::convertToEntity).collect(Collectors.toCollection(ArrayList::new));
		// ArrayList<IndexEntity> allIndexes = this.indexesList.stream().map(Index::convertToEntity).collect(Collectors.toCollection(ArrayList::new));
		return new TableEntity(name, allColumns);
	}
}
