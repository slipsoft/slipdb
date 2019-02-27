package db.structure;

import java.util.ArrayList;
import java.util.List;

import db.parseCSV.OptimDataFromCSV;


public class Table {

	protected String name;
	protected List<Column> columns = new ArrayList<>();
	protected List<Index> indexes = new ArrayList<>();
	
	public Table(String name, List<Column> columns) {
		this.name = name;
		this.columns.addAll(columns);
	}
	
	public String getName() {
		return name;
	}
	
	public List<Column> getColumns() {
		return columns;
	}

	public List<Index> getIndexes() {
		return indexes;
	}
	
	public void addIndex(Index index) {
		this.indexes.add(index);
	}
	
	public int getLineSize() {
		return columns
		    .stream()
		    .mapToInt(Column::getSize)
		    .sum();
	}
	
	public boolean columnExist(String name) {
		return this.columns.stream().anyMatch(col -> col.getName() == name);
	}
	
	/** Ajout d'une colonne
	 *  @param colName
	 *  @param defaultFillValue
	 *  @return
	 */
	public boolean addColumn(String colName, OptimDataFromCSV optimDataType) {
		int columnIndex = addColumn_base(colName, optimDataType);
		if (columnIndex == -1) return false;
		//Column column = columns.get(columnIndex);
		//column.storageType = AL_StorageType.isString;
		//column.clearAndFillWith(defaultFillValue, currentRowNumber);
		return true;
	}
	
	/** Ajouter une colonne, en retourner l'index dans la liste columns
	 *  @param colName
	 *  @return
	 */
	private int addColumn_base(String colName, OptimDataFromCSV optimDataType) { //StorageDataType dataType, StorageDataType dataTypeInCSV) {
		if (columnExist(colName)) return -1;
		// Ajout de la colonne
		Column newColumn = new Column(colName, optimDataType);
		columns.add(newColumn);
		return columns.size() - 1;
	}
	
	/** Trouver l'index d'une colonne à partir de son nom, dans la liste columns
	 *  @param colName  nom de la colonne à rechercher
	 *  @return -1 si introuvable, un entier >= 0 si existe
	 */
	public int findColumnNumber(String colName) {
		for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
			Column columnAtIndex = columns.get(colIndex);
			if (columnAtIndex.name.equals(colName)) {
				return colIndex;
			}
		}
		return -1;
	}
	
}
