package index.memDic;

import db.disk.dataHandler.DiskDataPosition;
import db.search.Operator;
import db.search.Predicate;
import db.structure.Column;
import db.structure.Index;
import db.structure.Table;
import index.IndexException;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.util.Collection;

/**
 *  Pour avoir un objet commun pour le stockage des variables statiques entre IndexMemDic et IndexMemDicCh.
 *  Probablement à passer en abstract @SylvainLune ??
 *
 */
public abstract class IndexMemDicAncester extends Index {
	// Pensé pour être mono-thread, pour l'instant
	public static Table table;
	public static int[] colIndexArray; // dans l'ordre
	public static Column[] indexOnThisColArray;

	public static boolean enableVerboseDichotomy = false;
	public static boolean enableVerboseSort = false;
	
	public static final boolean useSafeSlowComparaisonsNotDichotomy = false;
	public static final boolean enableDoubleDichotomyVerif = false;

	@Override
	public Column[] getIndexedColumns() {
		return indexOnThisColArray;
	}

	@Override
	public void indexEntry(Object[] entry, int id) throws IndexException {
		//TODO
	}

	@Override
	protected Operator[] compatibleOperators() {
		return new Operator[]{
				Operator.equals,
		};
	}

	// Deprecated interface
	@Override
	public Collection<DiskDataPosition> getPositionsFromPredicate(Predicate predicate) throws IndexException {
		return null;
	}

	@Override
	public void addValue(Object value, DiskDataPosition position) throws IOException {

	}

	@Override
	public void flushOnDisk() throws IOException {

	}
}
