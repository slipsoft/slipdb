package db.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.io.IOException;

import db.disk.dataHandler.DiskDataPosition;
import db.search.Operator;

import db.search.Operable;
import db.search.Predicate;
import index.indexTree.IndexException;

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
	protected Column indexedColumn; // colonne indexée dans cet Index

	/**
	 * Constructor, not always used. The simple constructor with no arguments is
	 * used by the TreeMap indexing class.
	 *
	 * @param indexedColumn - la colonne à indexer
	 */
	public Index(Column indexedColumn) {
		this.indexedColumn = indexedColumn;
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
	public Column getIndexedColumn() {
		return indexedColumn;
	}
	
	public boolean canBeUsedWithPredicate(Predicate predicate) {
		boolean containsColumn = indexedColumn.equals(predicate.getColumn());
		boolean isOperatorCompatible = this.isOperatorCompatible(predicate.getOperator());
		return containsColumn && isOperatorCompatible;
	}

	public void indexEntry(Object[] entry, DiskDataPosition position) throws IndexException{
		try {
			this.addValue(entry[this.indexedColumn.getNumber()], position);
		} catch (IOException e) {
			throw new IndexException(e);
		}
	}

	public abstract boolean isOperatorCompatible(Operator op);
	public abstract Collection<DiskDataPosition> getPositionsFromPredicate(Predicate predicate) throws IndexException;
	public abstract void addValue(Object value, DiskDataPosition position) throws IOException;
	public abstract void flushOnDisk() throws IOException;
}
