package db.structure;

import java.util.ArrayList;
import java.util.List;

import db.parseCSV.OptimDataFromCSV;


public class Table {

	protected String name; // nom de la table
	protected List<Column> columnsList = new ArrayList<Column>(); // liste des colonnes de la table
	protected List<Index> indexesList = new ArrayList<Index>();   // liste des index générés pour cette table
	
	public Table(String name, List<Column> columns) {
		this.name = name;
		this.columnsList.addAll(columns);
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
		columnsList.add(newColumn);
		return columnsList.size() - 1;
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
	
}
