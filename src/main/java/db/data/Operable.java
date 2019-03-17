package db.data;

import org.apache.commons.lang3.ArrayUtils;

public interface Operable {
	public static Operator[] compatibleOperatorsList = {};
	
	public default boolean isOperatorCompatible(Operator op) {
		return ArrayUtils.contains(compatibleOperatorsList, op);
	}
}
