package db.structure;

import org.apache.commons.lang3.ArrayUtils;

public class Key {
	protected Object[] values;
	
	public Key(Object[] values) {
		this.values = values;
	}
	
	@Override
	public int hashCode() {
		return ArrayUtils.hashCode(values);
	}
	
	@Override
	public boolean equals(Object obj) {
		Key k = (Key) obj;
		return ArrayUtils.isEquals(k.values, this.values);
	}
}
