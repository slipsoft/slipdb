package db.structure;

import java.util.ArrayList;
import java.util.TreeMap;

import db.data.Operator;

public class IndexTree extends Index {
	/** IndexTree index des colonnes en stockant chaque valeur dans un TreeMap.
	 * Il ne peut y avoir plus d'un seul index associé à chaque valeur,
	 * d'où l'emploi d'une ArrayList<Integer> à la place d'un simple Integer.
	 * 
	 * 
	 */
	
	// Liste des opérateurs compatibles :
	protected final static Operator[] compatibleOperatorsList = {
		Operator.equals,
		Operator.greater,
		Operator.less,
		Operator.greaterOrEquals,
		Operator.lessOrEquals,
	};
	
	/** Constructeur
	 *  @param indexThoseColumns colonnes indexées par cet index (ex : nom + prénom)
	 */
	public IndexTree(Column[] indexThoseColumns) {
		super(indexThoseColumns);
		this.indexedValuesMap = new TreeMap<Key, ArrayList<Integer>>();
	}
	
	/** Ajout d'un index (binIndex) associé à une valeur (clef)
	 * @param indexThisValue
	 * @param indexInGlobalBinaryFile
	 */
	public void addIndexInGlobalBinaryFile(Key indexThisValue, Integer indexInGlobalBinaryFile) {
		// Je recherche la valeur dans la carte :
		ArrayList<Integer> foundValue = indexedValuesMap.get(indexThisValue);
		if (foundValue == null) { // Création de la liste s'il n'existe pas encore
			foundValue = new ArrayList<Integer>();
		}
		foundValue.add(indexInGlobalBinaryFile);
		indexedValuesMap.put(indexThisValue, foundValue);
	}
	
	/** 
	 *  @param indexedValue
	 *  @return null si aucun élément ne correspond
	 */
	public ArrayList<Integer> getBinIndexListFromValue(Key indexedValue) {
		ArrayList<Integer> foundValue = indexedValuesMap.get(indexedValue);
		return foundValue;
	}
	
	// Encore beaucoup de taf pour rendre les arbres réellement opérationnels
	
}














