package sj.simpleDB.treeIndexing;

import java.util.Collection;

/**
 * Indexation d'une seule colonne par un arbre.
 * Tous les index sont stockés en mémoire vive dans cette version.
 */
public abstract class SIndexingTree {
	protected SIndexingTreeType treeType; // servira pour l'utilisation de méthodes génériques, pour utiliser le bon type d'arbre et faire les bons cast

	/**
	 * 
	 * @param associatedValue
	 * @return
	 */
	public abstract IntegerArrayList findBinIndexArrayFromValue(Integer associatedValue);
	/**
	 * 
	 * @param minValue
	 * @param maxValue
	 * @param isInclusive
	 * @return
	 */
	public abstract Collection<IntegerArrayList> findMatchingBinIndexes(Integer minValue, Integer maxValue, boolean isInclusive);
	
	
	
}
