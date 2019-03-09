package db.structure;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.dant.utils.EasyFile;
import com.dant.utils.Log;

import zArchive.sj.simpleBD.parseCSV.SOptimDataFromCSV;


public class Table {

	protected static String basePath = "target/tables/";
	protected String name; // nom de la table
	protected EasyFile file;
	protected List<Column> columnsList = new ArrayList<Column>(); // liste des colonnes de la table
	protected List<Index> indexesList = new ArrayList<Index>();   // liste des index générés pour cette table
	
	public Table(String name, List<Column> columns) throws IOException {
		this.name = name;
		this.columnsList.addAll(columns);
		this.file = new EasyFile(basePath + name + ".bin");
		this.file.createFileIfNotExist();
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
	
	public void addIndex(Index index) {
		this.indexesList.add(index);
	}
	
	public int getLineSize() {
		return columnsList
		    .stream()
		    .mapToInt(Column::getSize)
		    .sum();
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
	public boolean addColumn(String colName, SOptimDataFromCSV optimDataType, boolean hasToIndexThisColumn) throws Exception {
		if (columnExist(colName)) throw new Exception("Column already exists, colName = " + colName);
		// Ajout de la colonne
		Column newColumn = new Column(colName, optimDataType, hasToIndexThisColumn);
		columnsList.add(newColumn);
		return true;
	}

	public boolean addColumn(String colName, SOptimDataFromCSV optimDataType) throws Exception {
		return addColumn(colName, optimDataType, false);
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
	
	public List<Object> get(int id) throws IOException {
		FileInputStream is = new FileInputStream(file);
		List<Object> entry = new ArrayList<>();
		is.skip(id * getLineSize());
		for (Column column : columnsList) {
			byte[] b = new byte[column.getSize()];
			is.read(b);
			Log.debug(b);
			entry.add(column.getType().get(b));
		}
		is.close();
		return entry;
	}
	
	public OutputStream write() throws IOException {
		return new FileOutputStream(file);
	}
	
}
