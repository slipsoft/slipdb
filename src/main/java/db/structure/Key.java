package db.structure;

import org.apache.commons.lang3.ArrayUtils;

public class Key {
	
	protected Object[] valuesList; // liste des valeurs contenues dans cette clef
	
	public Key(Object[] aValuesList) {
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
