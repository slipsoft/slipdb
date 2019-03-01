package db.structure;

import org.apache.commons.lang3.ArrayUtils;

// Key DOIT implémenter l'interface Comparable pour être compatible avec les TreeMap.
// En l'état actuel, le TreeMap ne peut pas fonctionner, car il est vraiment difficile de comparer deux
// objets du type Key entre eux : soit on lui met des variables pour implémenter son type (au risque de voir les performances chuter grandement)
// 

public class Key /*implements Comparable NESESSAIRE POUR FONCTIONNER AVEC UN TREEMAP*/ {
	
	protected Object[] valuesList; // liste des valeurs contenues dans cette clef
	
	public Key(Object[] aValuesList) { // possible de créer une clef via un KeyMaker, pas super optimisé mais pratique !
		this.valuesList = aValuesList;
	}
	
	/** hash des valeurs de cette clef
	 */
	@Override
	public int hashCode() {
		return ArrayUtils.hashCode(valuesList);
	}
	
	/** Comparaison entre deux clefs
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj instanceof Key == false) return false;
		Key k = (Key) obj;
		return ArrayUtils.isEquals(k.valuesList, this.valuesList);
	}
	
}
