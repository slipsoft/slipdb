package db.structure;

import java.util.ArrayList;
import java.util.List;

import db.parseCSV.OptimDataFromCSV;


public class Table {
	
	
	
	/** ATTENTION : 
	 * DEBUT ce bloc de fonctions n'est pas utilisé par le moteur de Sylvain, mais je l'ai ai laissées pour que ça reste compatible avec les tests de Nicolas*/
	protected String name;
	protected List<Column> columns;
	protected List<Index> indexes;
	
	public Table(String name, List<Column> columns) {
		this.name = name;
		this.columns = columns;
		this.indexes = new ArrayList<>();
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
	/** ATTENTION : 
	 * FIN ce bloc de fonctions n'est pas utilisé par le moteur de Sylvain, mais je l'ai ai laissées pour que ça reste compatible avec les tests de Nicolas*/
	
	
	public Table() {
		
	}

	public ArrayList<Column> columnList = new ArrayList<Column>();
	
	/** Ajout d'une colonne
	 *  @param colName
	 *  @param defaultFillValue
	 *  @return
	 */
	public boolean addColumn(String colName, OptimDataFromCSV optimDataType) {
		int columnIndex = addColumn_base(colName, optimDataType);
		if (columnIndex == -1) return false;
		//Column column = columnList.get(columnIndex);
		//column.storageType = AL_StorageType.isString;
		//column.clearAndFillWith(defaultFillValue, currentRowNumber);
		return true;
	}
	
	/** Ajouter une colonne, en retourner l'index dans la liste columnList
	 *  @param colName
	 *  @return
	 */
	private int addColumn_base(String colName, OptimDataFromCSV optimDataType) { //StorageDataType dataType, StorageDataType dataTypeInCSV) {
		// Je vérifie qu'aucune colonne n'a le même nom
		if (findColumnIndex(colName) != -1) return -1;
		// Ajout de la colonne
		Column newColumn = new Column(colName, optimDataType);
		columnList.add(newColumn);
		return columnList.size() - 1;
	}
	
	/** Trouver l'index d'une colonne à partir de son nom, dans la liste columnList
	 *  @param colName  nom de la colonne à rechercher
	 *  @return -1 si introuvable, un entier >= 0 si existe
	 */
	public int findColumnIndex(String colName) {
		for (int colIndex = 0; colIndex < columnList.size(); colIndex++) {
			Column columnAtIndex = columnList.get(colIndex);
			if (columnAtIndex.name.equals(colName)) {
				return colIndex;
			}
		}
		return -1;
	}
	
}
