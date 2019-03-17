package db.structure;

import org.apache.commons.lang3.ArrayUtils;

public class KeyHash extends Key {

	public KeyHash(Object[] aValuesList) {
		super(aValuesList);
		// TODO Auto-generated constructor stub
	}
	
	/** hash des valeurs de cette clef
	 */
	@Override
	public int hashCode() {
		return ArrayUtils.hashCode(value);
	}
	
	/** Comparaison entre deux clefs
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj instanceof Key == false) return false;
		Key k = (Key) obj;
		return ArrayUtils.isEquals(k.value, this.value);
	}

}
