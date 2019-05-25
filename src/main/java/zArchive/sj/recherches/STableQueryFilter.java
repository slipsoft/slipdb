package zArchive.sj.recherches;


/**
 *  Filtre de requête sur table
 *
 */
public class STableQueryFilter {
	
	// Pour une valeur exacte sans inverval, ne pas spécifier de maxValue (laisser à null)
	public Object minValue = null, maxValue = null;
	public boolean isInclusive = false;
	public final int comumnIndex;

	public STableQueryFilter(int aColumnIndex, Object aMinValue, Object aMaxValue, boolean aInclusive) {
		comumnIndex = aColumnIndex;
		minValue = aMinValue;
		maxValue = aMaxValue;
		isInclusive = aInclusive;
	}

	public STableQueryFilter(int aColumnIndex, Object aMinValue) { // + Operator
		comumnIndex = aColumnIndex;
		minValue = aMinValue;
		maxValue = null;
		isInclusive = false;
	}
	
	
	
}
