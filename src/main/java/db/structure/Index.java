package db.structure;

import java.util.ArrayList;
import java.util.Map;

import db.data.Operable;

/**
 * Classe Index, permettant d'indexer une ou plusieurs colonnes Exemple :
 * Indexer selon le nom ou selon le nom + prénom ou nom + prénom + date de
 * naissance ...
 *
 */
public abstract class Index implements Operable {

	/**
	 * Carte des valeurs indexées par cet objet Index - association clef valeur :
	 * Key est l'identifiant (ex : nom -ou- nom + prénom + age par exemple), Integer
	 * est l'index dans le fichier binaire issu des .CSV Une seule correspondance
	 * est possible, pour que ça fonctionne, il faudrait donc remplacer Integer par
	 * ArrayList<Integer>
	 */
	protected Map<Key, ArrayList<Integer>> indexedValuesMap;
	protected Column[] indexedColumnsList; // Liste des colonnes indexées dans cet Index

	/**
	 * Constructor, not always used. The simple constructor with no arguments is
	 * used by the TreeMap indexing class.
	 *
	 * @param columnsToIndex la liste des colonnes à indexer
	 */
	public Index(Column[] columnsToIndex) {
		this.indexedColumnsList = columnsToIndex;
	}

	/**
	 * An index might not has a list on indexed columns at first. For example, an
	 * IndexTree only knows which column to index when calling
	 * IndexTree.indexColumnFromDisk(...)
	 */
	public Index() {

	}

	/**
	 * @return la liste des colonnes indexées dans cet Index
	 */
	public Column[] getColumnList() {
		return indexedColumnsList;
	}
}