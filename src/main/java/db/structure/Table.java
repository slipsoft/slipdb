package db.structure;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.dant.entity.ColumnEntity;
import com.dant.entity.IndexEntity;
import com.dant.entity.TableEntity;
import com.dant.utils.EasyFile;

import com.dant.utils.Log;
import db.data.DataType;

/**
 * A simple SQL-like table, consisting of 
 */
public class Table {

	protected static String basePath = "target/tables/";
	protected String name; // table name
	protected EasyFile fileLinesOnDisk; // <- les fichiers de sauvegarde des colonnes sont désormais indépendants
	protected List<Column> columnsList = new ArrayList<Column>(); // liste des colonnes de la table
	protected List<Index> indicesList = new ArrayList<Index>();   // liste des index générés pour cette table
	
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
		this.fileLinesOnDisk = new EasyFile(basePath + name + ".bin");
		this.fileLinesOnDisk.createFileIfNotExist();
	}
	
	public String getName() {
		return name;
	}
	
	public List<Column> getColumns() {
		return columnsList;
	}

	public List<Index> getIndices() {
		return indicesList;
	}
	
	public void addIndex(Index index) {
		this.indicesList.add(index);
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
	
	/** Ajout d'une colonne
	 *  @param colName
	 *  @param defaultFillValue
	 *  @return
	 * @throws Exception 
	 */
	public boolean addColumn(String colName, DataType dataType) throws Exception {
		if (columnExist(colName)) throw new Exception("Column already exists, colName = " + colName);
		// Ajout de la colonne
		Column newColumn = new Column(colName, dataType);
		columnsList.add(newColumn);
		return true;
	}

	/** Trouver l'index d'une colonne à partir de son nom, dans la liste columns
	 *  @param colName  nom de la colonne à rechercher
	 *  @return -1 si introuvable, un entier >= 0 si existe
	 */
	public int findColumnIndex(String colName) {
		for (int colIndex = 0; colIndex < columnsList.size(); colIndex++) {
			Column columnAtIndex = columnsList.get(colIndex);
			if (columnAtIndex.name.equals(colName)) {
				return colIndex;
			}
		}
		return -1;
	}

	/**
	 * 
	 * @param lineId is the position of the line, 0 being the first loaded line from the file (CSV for New York taxis)
	 * @return a list containing every entry associates with the line at position lineId
	 * @throws IOException
	 */
	public List<Object> getValuesOfLineById(int lineId) throws IOException { // or getRowById
		
		// Get a new disposable FileInputStream with the file where all table rows are stored
		FileInputStream fileAsStream = new FileInputStream(fileLinesOnDisk);
		
		// List of values stored in the line of id lineId
		List<Object> lineValues = new ArrayList<>(); // rowValues
		
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
	
	public OutputStream tableToOutputStream() throws IOException {
		return new FileOutputStream(fileLinesOnDisk);
	}
	
	public EasyFile getFileLinesOnDisk() {
		return fileLinesOnDisk;
	}

	public TableEntity convertToEntity () {
		String name = this.name;
		ArrayList<ColumnEntity> allColumns = this.columnsList.stream().map(c -> c.convertToEntity()).collect(Collectors.toCollection(ArrayList::new));
		ArrayList<IndexEntity> allIndexes = this.indicesList.stream().map(Index::convertToEntity).collect(Collectors.toCollection(ArrayList::new));
		return new TableEntity(name, allColumns, allIndexes);
    }
}
