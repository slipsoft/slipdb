package db.structure;

import java.util.Arrays;
import java.util.Collection;
import java.io.IOException;

import db.disk.dataHandler.DiskDataPosition;
import db.search.Operator;

import db.search.Operable;
import db.search.Predicate;
import index.IndexException;
import org.apache.commons.lang3.ArrayUtils;

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
	// KAKA -> protected Map<Key, ArrayList<Integer>> indexedValuesMap;
	protected Column[] indexedColumns; // colonne indexée dans cet Index
	protected Table table;

	/**
	 * Constructor, not always used. The simple constructor with no arguments is
	 * used by the TreeMap indexing class.
	 *
	 * @param table - table of the index
	 * @param indexedColumns - columns to index
	 */
	public Index(Table table, Column[] indexedColumns) {
		this.table = table;
		this.indexedColumns = indexedColumns;
	}

	/**
	 * An index might not has a list on indexed columns at first. For example, an
	 * IndexTree only knows which column to index when calling
	 * IndexTree.indexColumnFromDisk(...)
	 */
	@Deprecated
	public Index() {

	}

	/**
	 * @return la liste des colonnes indexées dans cet Index
	 */
	public Column[] getIndexedColumns() {
		return indexedColumns;
	}
	
	public boolean canBeUsedWithPredicate(Predicate predicate) {
		boolean containsColumn = ArrayUtils.contains(getIndexedColumns(), predicate.getColumn());
		boolean isOperatorCompatible = this.isOperatorCompatible(predicate.getOperator());
		return containsColumn && isOperatorCompatible;
	}

	/**
	 * Check if an operator is compatible with this index.
	 * @param op the operator to check
	 * @return true if the operator is compatible
	 */
	public boolean isOperatorCompatible(Operator op) {
		return ArrayUtils.contains(this.compatibleOperators(), op);
	}

	@Deprecated
	public void indexEntry(Object[] entry, DiskDataPosition position) throws IndexException{
		try {
			this.addValue(entry[this.indexedColumns[0].getNumber()], position);
		} catch (IOException e) {
			throw new IndexException(e);
		}
	}

	@Deprecated
	public abstract void indexEntry(Object[] entry, int id) throws IndexException;

	/**
	 * Function to implement. Return the list of Operators compatible with the index.
	 * @return list of compatible Operators
	 */
	protected abstract Operator[] compatibleOperators();
	public abstract int[] getIdsFromPredicate(Predicate predicate) throws IndexException;
	public abstract void refreshIndexWithColumnsData(boolean beSuperVerbose);

	// Deprecated interface
	@Deprecated //remplace DiskDataPosition by the new int position
	public abstract Collection<DiskDataPosition> getPositionsFromPredicate(Predicate predicate) throws IndexException;
	@Deprecated //remplace DiskDataPosition by the new int position
	public abstract void addValue(Object value, DiskDataPosition position) throws IOException;
	@Deprecated
	public abstract void flushOnDisk() throws IOException;
}
