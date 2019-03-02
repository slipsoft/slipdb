package sj.db.treeIndexing;

import java.util.TreeMap;

/**
 * Gestion des index de type int
 */
public class SIntIndexingTree extends SNumericalIndexingTree {
	
	// Carte des index sous forme de TreeMap
	protected TreeMap<Integer/*clef*/, IntegerArrayList/*valeur*/> intMapIndex = new TreeMap<Integer, IntegerArrayList>();
	
	public IntegerArrayList getArrayFromValue(Integer associatedValue) {
		return intMapIndex.get(associatedValue);
	}
	
	public void addValue(Integer associatedValue) {
		IntegerArrayList indexList = getArrayFromValue(associatedValue);
		if (indexList == null) {
			indexList = new IntegerArrayList();
		}
		indexList.add(associatedValue);
	}
	
	
}
