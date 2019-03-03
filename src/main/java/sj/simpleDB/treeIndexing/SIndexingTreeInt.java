package sj.simpleDB.treeIndexing;

import java.util.Collection;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Gestion des index de type int
 */
public class SIndexingTreeInt extends SIndexingTree {
	
	// Plus tard, pour optimiser : protected final int divideAndGroupBy; // Grouper les valeurs en lot
	
	// Carte des index sous forme de TreeMap
	protected TreeMap<Integer/*clef*/, IntegerArrayList/*valeur*/> intTreeMap = new TreeMap<Integer, IntegerArrayList>();
	
	// IntegerArrayList correpond à la liste des binIndex ayant la même valeur pour cet IndexingTree (donc pour la colonne indexée)
	
	/** Trouver le tableau d'index correspondant à une valeur
	 * @param associatedValue
	 * @return
	 */
	public IntegerArrayList findBinIndexArrayFromValue(Integer associatedValue) {
		return intTreeMap.get(associatedValue);
	}
	
	/**
	 * @param minValue
	 * @param maxValue
	 * @param isInclusive
	 * @return la collection contenant tous les binIndex correspondants
	 */
	@Override
	public Collection<IntegerArrayList> findMatchingBinIndexes(Integer minValue, Integer maxValue, boolean isInclusive) { // NavigableMap<Integer, IntegerArrayList> findSubTree
		NavigableMap<Integer, IntegerArrayList> subTree = intTreeMap.subMap(minValue, isInclusive, maxValue, isInclusive);
		Collection<IntegerArrayList> collectionValues = subTree.values();
		return collectionValues;
		///for (IntegerArrayList binIndexArray : collectionValues) {
		//}
	}
	
	/** Ajouter une valeur et un binIndex associé
	 * @param associatedValue valeur indexée
	 * @param binIndex position (dans le fichier binaire global) de l'objet stocké dans la table
	 */
	public void addValue(Integer associatedValue, Integer binIndex) {
		IntegerArrayList binIndexList = findBinIndexArrayFromValue(associatedValue);
		if (binIndexList == null) {
			binIndexList = new IntegerArrayList();
			intTreeMap.put(associatedValue, binIndexList);
		}
		binIndexList.add(binIndex);
	}
	
}
