package sj.simpleDB.treeIndexing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.TreeMap;

public class SIndexingTreeObject extends SIndexingTree {
	
	// Plus tard, pour optimiser : protected final int divideAndGroupBy; // Grouper les valeurs en lot
	
	// Carte des index sous forme de TreeMap
	protected TreeMap<Object/*clef*/, IntegerArrayList/*valeur*/> floatTreeMap = new TreeMap<Object, IntegerArrayList>();
	
	// IntegerArrayList correpond à la liste des binIndex ayant la même valeur pour cet IndexingTree (donc pour la colonne indexée)
	
	/** Trouver le tableau d'index correspondant à une valeur
	 * @param associatedValue
	 * @return
	 */
	@Override
	public IntegerArrayList findBinIndexArrayFromValue(Object associatedValue) {
		return floatTreeMap.get(associatedValue); // fait une comparaison d'objet, et non une comparaison de référence : if (associatedValue.equals(valeurDansArbre)) [...]
	}
	
	
	@Override
	public Collection<IntegerArrayList> findMatchingBinIndexes(Object minValue, Object maxValue, boolean isInclusive) { // NavigableMap<Integer, IntegerArrayList> findSubTree
		NavigableMap<Object, IntegerArrayList> subTree = floatTreeMap.subMap(minValue, isInclusive, maxValue, isInclusive);
		Collection<IntegerArrayList> collectionValues = subTree.values();
		return collectionValues;
		///for (IntegerArrayList binIndexArray : collectionValues) {
		//}
	}
	
	@Override
	public void addValue(Object argAssociatedValue, Integer binIndex) {
		//Integer realAssociatedValue = (Integer)argAssociatedValue; // cast de la valeur : elle DOIT être de type Integer
		IntegerArrayList binIndexList = findBinIndexArrayFromValue(argAssociatedValue);
		if (binIndexList == null) {
			binIndexList = new IntegerArrayList();
			floatTreeMap.put(argAssociatedValue, binIndexList);
		}
		binIndexList.add(binIndex);
	}
	
	
	
}
