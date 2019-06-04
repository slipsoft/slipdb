package index.indexHash;

import org.apache.commons.lang3.ArrayUtils;

@Deprecated
public class KeyHash { //  extends Key 
	
	private Object[] valuesArray;
	
	public KeyHash(Object[] aValuesArray) {
		valuesArray = aValuesArray;
	}
	
	/** Hash des valeurs de cette clef
	 */
	@Override
	public int hashCode() {
		return ArrayUtils.hashCode(valuesArray);
	}
	
	/** Comparaison entre deux clefs
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		//if (obj instanceof Key == false) return false;
		if (obj.getClass() != getClass()) return false;
		KeyHash k = (KeyHash) obj;
		return ArrayUtils.isEquals(k.valuesArray, this.valuesArray);
	}

}
