package index.memDic;

import db.disk.dataHandler.DiskDataPosition;
import db.search.Operator;
import db.search.Predicate;
import db.structure.Column;
import db.structure.Index;
import db.structure.Table;
import index.IndexException;

import java.io.IOException;
import java.util.Collection;

/**
 *  Pour avoir un objet commun pour le stockage des variables statiques entre IndexMemDic et IndexMemDicCh.
 *
 */
public class IndexMemDicAncester extends Index {
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
	public boolean isOperatorCompatible(Operator op) {
		//TODO
		return false;
	}

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
