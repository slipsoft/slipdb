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
	 * @throws Exception 
	 */
	public boolean addColumn(String colName, OptimDataFromCSV optimDataType) throws Exception {
		if (columnExist(colName)) throw new Exception("Column exist");
		// Ajout de la colonne
		Column newColumn = new Column(colName, optimDataType);
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
	
}
