package db.structure;

import java.util.ArrayList;
import java.util.Map;
import db.data.Operator;


/**
 * Classe Index, permettant d'indexer une ou plusieurs colonnes
 * Exemple : Indexer selon le nom ou selon le nom + prénom ou nom + prénom + date de naissance ...
 * 
 */
public abstract class Index {
	
	protected static Operator[] compatibleOperatorsList; // Liste des opérateurs compatibles
	protected Column[] indexedColumnsList; // Liste des colonnes indexées dans cet Index
	
	
	/** Constructeur
	 * @param columnsToIndex la liste des colonnes à indexer
	 */
	public Index(Column[] columnsToIndex) {
		this.indexedColumnsList = columnsToIndex;
	}
	
	/** An index might not has a list on indexed columns at first.
	 *  For example, an IndexTree only knows which column to index when calling IndexTree.indexColumnFromDisk(...)
	 */
	public Index() {
		
	}
	
	/**
	 * @return la liste des colonnes indexées dans cet Index
	 */
	public Column[] getColumnList() {
		return indexedColumnsList;
	}
	
	/* Seems unused ?
	/**
	 * @return la carte de toutes les valeurs indexées dans cet Index
	 * /
	 public Map<Key, ArrayList<Integer>> getIndexedValuesMap() {
		return indexedValuesMap;
	}*/
	
	
}
