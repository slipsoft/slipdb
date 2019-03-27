package db.search;

public class FilterGroup implements FilterTerm {
	protected JoinMethod join = JoinMethod.and;
	protected FilterTerm[] terms = new FilterTerm[0];
	
	public FilterGroup() {
		// TODO Auto-generated constructor stub
	}
}
