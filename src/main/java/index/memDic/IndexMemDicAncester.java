package index.memDic;

import db.structure.Column;
import db.structure.Table;

/**
 *  Pour avoir un objet commun pour le stockage des variables statiques entre IndexMemDic et IndexMemDicCh.
 *
 */
public class IndexMemDicAncester {
	// Pensé pour être mono-thread, pour l'instant
	public static Table table;
	public static int[] colIndexArray; // dans l'ordre
	public static Column[] indexOnThisColArray;

	public static boolean enableVerboseDichotomy = false;
	public static boolean enableVerboseSort = false;
	
	public static final boolean useSafeSlowComparaisonsNotDichotomy = false;
	public static final boolean enableDoubleDichotomyVerif = false;
}
