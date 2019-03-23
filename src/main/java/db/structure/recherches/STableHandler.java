package db.structure.recherches;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import db.data.DataType;
import db.parsers.CsvParser;
import db.structure.Column;
import db.structure.Table;
import db.structure.indexTree.IndexTreeDic;

public class STableHandler {
	
	protected String tableName;
	protected ArrayList<Column> columnsList = new ArrayList<Column>();
	protected Table associatedTable;
	protected CsvParser csvParser = null;
	// Possibilité de parser de plusieurs manières différentes (un jour...)
	protected boolean firstTimeParsingData = true;
	
	protected ArrayList<IndexTreeDic> indexTreeList = new ArrayList<IndexTreeDic>(); // Liste des IndexTree associés à cette table
	
	public String getTableName() {
		return tableName;
	}
	
	public STableHandler(String argTableName) {
		tableName = argTableName;
	}
	
	/**
	 *  Ajouter une colonne, suppose qu'il n'y a pas encore de données dans la table
	 *  @param argColumnName
	 *  @param argColumnDataType
	 */
	public void addColumn(String argColumnName, DataType argColumnDataType) {
		Column newColumn = new Column("VendorID", argColumnDataType);
		columnsList.add(newColumn);
	}
	
	public Table createTable() throws IOException {
		associatedTable = new Table(tableName, columnsList);
		return associatedTable;
	}
	
	public void parseCsvData(String csvPath) throws Exception {
		if (associatedTable == null) throw new Exception("La table associée est null, elle doit être crée via createTable avant tout parsing.");
		if (csvParser == null)
			csvParser = new CsvParser(associatedTable);
		
		InputStream is = new FileInputStream(csvPath);
		csvParser.parse(is, !firstTimeParsingData);
		is.close();
		
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
	
	public void indexColumnWithTree(String columnName) throws Exception {
		int colIndex = getColumnIndex(columnName);
		if (colIndex == -1) throw new Exception("Colonne introuvable, impossible de l'indexer.");
		indexColumnWithTree(colIndex);
	}

	public void indexColumnWithTree(int columnIndex) throws Exception {
		if (associatedTable == null) throw new Exception("Aucune table crée, indexation impossible.");
		List<Column> columnList = associatedTable.getColumns();
		if (columnIndex < 0 || columnIndex >= columnList.size()) throw new Exception("Index de la colonne invalide. (columnIndex=" + columnIndex + " non compris entre 0 et columnList.size()=" + columnList.size());
		
		IndexTreeDic indexingObject = new IndexTreeDic();
		indexTreeList.add(indexingObject);
		indexingObject.indexColumnFromDisk(associatedTable, columnIndex);
	}
	
	//public boolean makeQuery(STableQuery query) {
		
	//}
	
	/*
	public ArrayList<Integer> findIndexedResultsOfColumn(String comumnName, Object minValue, Object maxValue, boolean inclusive) {
		int colIndex = getColumnIndex(comumnName);
		if (colIndex == -1) throw new Exception("Colonne introuvable, impossible de faire une recherche sur ses index.");
		//return findIndexedResultsOfColumn();
		
		
	}*/
	
	
	
	
}
