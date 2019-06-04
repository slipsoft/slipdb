package db.structure;

// Key DOIT implémenter l'interface Comparable pour être compatible avec les TreeMap.
// En l'état actuel, le TreeMap ne peut pas fonctionner, car il est vraiment difficile de comparer deux
// objets du type Key entre eux : soit on lui met des variables pour implémenter son type (au risque de voir les performances chuter grandement)
// 

@Deprecated
public abstract class Key {
	
	protected Object value; // liste des valeurs contenues dans cette clef

	@Deprecated
	public Key(Object value) { // possible de créer une clef via un KeyMaker, pas super optimisé mais pratique !
		this.value = value;
	}
	
	public abstract boolean equals(Object other);
	public abstract int hashCode();
}
