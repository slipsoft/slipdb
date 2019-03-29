package zArchive.sj.simpleDB.treeIndexing;

import java.util.ArrayList;
import java.util.Collection;

import db.data.types.IntegerArrayList;

/** Retourner les valeurs correspondant aux filtres passés en argument
 * 
 * Manière pas super optimisée :
 * 1) Je prends tous les sous-arbres contenant des éléments aux valeurs compatibles avec ce que je recherche
 * 2) Je crée un second arbre contenant tout les index du premier arbre
 * 3) Si une valeur n'est pas présente dans les autres arbres, je la supprime de ma copie d'arbre
 * 4) Quand tout les filtres ont été appliqués, l'arbre qu'il me reste est le résultat.
 *
 */
public class SIndexingTreeFinder {
	// Chaque élément 
	
	//protected TreeMap<Integer, Boolean> validItemsTree;
	//protected TreeSet<SIndexingTreeFinderItem> validItemsBinIndex;
	
	protected ArrayList<SIndexingTreeFinderItem> validBinIndexesArray = new ArrayList<SIndexingTreeFinderItem>();
	
	public void initializeWithFirstCollection(Collection<IntegerArrayList> addFromThisBinIndexCollection) {
		for (IntegerArrayList binIndexArray : addFromThisBinIndexCollection) {
			for (Integer binIndex : binIndexArray) {
				
				//SIndexingTreeFinderItem finderItem = new SIndexingTreeFinderItem(true, binIndex);
				//validItemsBinIndex.add(finderItem);
			}
		}
	}
	
	public void filterResultsWithCollection(Collection<IntegerArrayList> filterWithThisBinIndexCollection) {
		//for (SIndexingTreeFinderItem finderItem : validItemsBinIndex) {
		//	finderItem.passedThisFilter = false;
		//}
		for (IntegerArrayList binIndexArray : filterWithThisBinIndexCollection) {
			for (Integer binIndex : binIndexArray) {
				//validItemsBinIndex.
				SIndexingTreeFinderItem finderItem = new SIndexingTreeFinderItem(true, binIndex);
				//validItemsBinIndex.add(finderItem);
			}
		}
		
	}
	
	
	
}
