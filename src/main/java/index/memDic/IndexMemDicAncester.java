package index.memDic;

import db.structure.Column;
import db.structure.Table;

/**
 *  Pour avoir un objet commun pour le stockage des variables statiques entre IndexMemDic et IndexMemDicCh.
 *
 */
public class IndexMemDicAncester {
	// Pensé pour être mono-thread, pour l'instant
	
	protected Table table;
	protected int[] colIndexArray; // dans l'ordre
	protected Column[] indexOnThisColArray;
	
	//public static Table staticTable; désormais inutile
	//public static int[] staticColIndexArray;
	public static Column[] staticIndexOnThisColArray; // seulement utile pour le sort, utilisé depuis IndexMemDicTemporaryItem
	// et actualisé avant tous les sort (dans IndexMemDic.refreshIndexWithColumnsData())
	
	public static boolean enableVerboseDichotomy = false;
	public static boolean enableVerboseSort = false;
	
	public static final boolean useSafeSlowComparaisonsNotDichotomy = false;
	public static final boolean enableDoubleDichotomyVerif = false;
	
}
