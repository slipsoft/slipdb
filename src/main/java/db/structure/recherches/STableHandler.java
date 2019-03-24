package db.structure.recherches;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.dant.utils.Log;

import db.data.DataType;
import db.data.LongArrayList;
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
	 * @throws Exception 
	 */
	public void addColumn(String argColumnName, DataType argColumnDataType) throws Exception {
		for (Column col : columnsList) {
			if (col.getName().equals(argColumnName)) throw new Exception("Ajout de la colonne impossibe, il en exie déjà une du même nom : " + argColumnName);
		}
		Column newColumn = new Column(argColumnName, argColumnDataType);
		columnsList.add(newColumn);
	}
	
	public Table createTable() throws IOException {
		associatedTable = new Table(tableName, columnsList);
		return associatedTable;
	}
	
	public void parseCsvData(String csvPath, boolean doRuntimeIndexing) throws Exception {
		if (associatedTable == null) throw new Exception("La table associée est null, elle doit être crée via createTable avant tout parsing.");
		if (csvParser == null)
			csvParser = new CsvParser(associatedTable);
		
		if (doRuntimeIndexing)
			csvParser.setRuntimeIndexing(runtimeIndexingList);
		else
			csvParser.setRuntimeIndexing(null);
		
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
	
	public void indexColumnWithTreeFromDisk(String columnName) throws Exception {
		int colIndex = getColumnIndex(columnName);
		if (colIndex == -1) throw new Exception("Colonne introuvable, impossible de l'indexer.");
		indexColumnWithTreeFromDisk(colIndex);
	}

	public void indexColumnWithTreeFromDisk(int columnIndex) throws Exception {
		if (associatedTable == null) throw new Exception("Aucune table crée, indexation impossible.");
		List<Column> columnList = associatedTable.getColumns();
		if (columnIndex < 0 || columnIndex >= columnList.size()) throw new Exception("Index de la colonne invalide. (columnIndex=" + columnIndex + " non compris entre 0 et columnList.size()=" + columnList.size());
		
		
		IndexTreeDic alreadyExistingTree = findTreeAssociatedWithColumnIndex(columnIndex);
		Log.info("Arbre existe = " + alreadyExistingTree);
		if (alreadyExistingTree == null) {
			alreadyExistingTree = new IndexTreeDic();
			indexTreeList.add(alreadyExistingTree);
		}
		
		//IndexTreeDic indexingObject = new IndexTreeDic();
		alreadyExistingTree.indexColumnFromDisk(associatedTable, columnIndex);
	}
	
	
	// ---- RuntimeIndexing : servant à stocker les champs à indexer ----
	
	// Pour l'instant, il n'y a que le spport des index mono-colonne.
	// Faire une recherche sur une colonne équivaut à trouver l'index qui traîte de la colonne, et à faire la recherche dessus.
	
	
	// indexColumnList est la liste des colonnes à indexer
	public SRuntimeIndexingEntryList runtimeIndexingList = new SRuntimeIndexingEntryList();//ArrayList<SRuntimeIndexingEntry>();
	
	
	public void createRuntimeIndexingColumn(int columnIndex) throws Exception { // addInitialColumnAndCreateAssociatedIndex
		if (associatedTable == null) throw new Exception("Aucune table crée, indexation impossible.");
		List<Column> columnList = associatedTable.getColumns();
		if (columnIndex < 0 || columnIndex >= columnList.size()) throw new Exception("Index de la colonne invalide. (columnIndex=" + columnIndex + " non compris entre 0 et columnList.size()=" + columnList.size());
		
		IndexTreeDic alreadyExistingTree = findTreeAssociatedWithColumnIndex(columnIndex);
		if (alreadyExistingTree == null) {
			alreadyExistingTree = new IndexTreeDic();
			indexTreeList.add(alreadyExistingTree);
		}
		
		SRuntimeIndexingEntry indexEntry = new SRuntimeIndexingEntry();
		indexEntry.associatedIndexTree = alreadyExistingTree;
		indexEntry.associatedColumn = columnList.get(columnIndex);
		indexEntry.associatedTable = associatedTable;
		indexEntry.columnIndex = columnIndex;
		runtimeIndexingList.add(indexEntry);
		indexEntry.associatedIndexTree.initialiseWithTableAndColumn(associatedTable, columnIndex); // Pour pouvoir indexer au runtime (lors du parsing)
	}
	

	public void createRuntimeIndexingColumn(String columnName) throws Exception {
		if (associatedTable == null) throw new Exception("Aucune table crée, indexation impossible.");
		int colIndex = getColumnIndex(columnName);
		if (colIndex == -1) throw new Exception("Colonne introuvable, impossible de l'indexer.");
		createRuntimeIndexingColumn(colIndex);
	}
	
	
	
	//public boolean makeQuery(STableQuery query) {
		
	//}
	
	public IndexTreeDic findTreeAssociatedWithColumnIndex(int columnIndex) {
		for (IndexTreeDic indexTree : indexTreeList) {
			if (indexTree.getAssociatedTableColumnIndex() == columnIndex) {
				return indexTree;
			}
		}
		return null;
	}
	
	
	public Collection<LongArrayList> findIndexedResultsOfColumn(String columnName, Object minValue, Object maxValue, boolean inclusive) throws Exception {
		int columnIndex = getColumnIndex(columnName);
		if (columnIndex == -1) throw new Exception("Colonne introuvable, impossible de faire une recherche sur ses index.");
		if (IndexTreeDic.firstValueIsHigherThatSecondValue(minValue, maxValue) > 0) {
			return new ArrayList<LongArrayList>(); // aucun résultat
		}
		//return findIndexedResultsOfColumn();
		
		IndexTreeDic makeRequestOnThisTree = findTreeAssociatedWithColumnIndex(columnIndex);
		/*for (IndexTreeDic indexTree : indexTreeList) {
			if (indexTree.getAssociatedTableColumnIndex() == columnIndex) {
				makeRequestOnThisTree = indexTree;
				break;
			}
		}*/
		
		
		if (makeRequestOnThisTree == null) {
			return new ArrayList<LongArrayList>();
		}
		return makeRequestOnThisTree.findMatchingBinIndexes(minValue, maxValue, inclusive, false);
	}

	
	public int evaluateNumberOfResults(Collection<LongArrayList> resultsCollection) {
		// Iterates over all the results
		int numberOfResults = 0;
		for (LongArrayList longList : resultsCollection) {
			//Log.info("list size = " + list.size());
			numberOfResults += longList.size();
			//numberOfLines++;
			/*if (false)
			for (Long index : longList) {
				// un-comment those lines if you want to get the full info on lines : List<Object> objList = table.getValuesOfLineById(index);
				//Log.info("  index = " + index);
				List<Object> objList = table.getValuesOfLineById(index);
				Object indexedValue = objList.get(indexingColumnIndex);
				//indexingColumn.getDataType().
				Log.info("  valeur indexée = " + indexedValue);
				//Log.info("  objList = " + objList);
			}*/
		}
		return numberOfResults;
	}

	//trip_distance
	public int evaluateNumberOfArrayListLines(Collection<LongArrayList> resultsCollection) {
		return resultsCollection.size();
	}
	
	//trip_distance
	public ArrayList<ArrayList<Object>> getFullResultsFromBinIndexes(Collection<LongArrayList> resultsCollection) throws Exception { // table connue ! , Table fromTable) {
		if (associatedTable == null) throw new Exception("Aucune table crée, indexation impossible.");
		
		ArrayList<ArrayList<Object>> resultArrayList = new ArrayList<ArrayList<Object>>();
		
		// Pour toutes les listes de valeurs identiques
		// (il peut y avoir des listes distinctes associés à une même valeur indexée, du fait du multi-fichiers / multi-thread)
		for (LongArrayList longList : resultsCollection) {
			//Log.info("list size = " + list.size());
			for (Long binIndex : longList) {
				// un-comment those lines if you want to get the full info on lines : List<Object> objList = table.getValuesOfLineById(index);
				//Log.info("  index = " + index);
				ArrayList<Object> objList = associatedTable.getValuesOfLineById(binIndex);
				resultArrayList.add(objList);
				
				//Object indexedValue = objList.get(indexingColumnIndex);
				//indexingColumn.getDataType().
				//Log.info("  valeur indexée = " + indexedValue);
				//Log.info("  objList = " + objList);
			}
		}
		return resultArrayList;
	}
	
	public void flushEveryIndexOnDisk() throws IOException {
		for (IndexTreeDic indexTree : indexTreeList) {
			indexTree.flushOnDisk();
		}
		
	}
	
	
}
