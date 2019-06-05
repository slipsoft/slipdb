package db.search;

import db.disk.dataHandler.DiskDataPosition;

import java.util.Set;

public class FilterGroup implements FilterTerm {
	protected JoinMethod join = JoinMethod.and;
	protected FilterTerm[] terms = new FilterTerm[0];
	
	public FilterGroup(JoinMethod join, FilterTerm[] term) {
		this.join = join;
		this.terms = term;

	}

	@Override
	public Set<Integer> execute() throws SearchException {
		Set<Integer> positions = null;
		for (FilterTerm filterTerm : terms) {
			if (positions == null) {
				positions = filterTerm.execute();
			} else {
				switch (join) {
					case and:
						positions.retainAll(filterTerm.execute());
						break;
					case or:
						positions.addAll(filterTerm.execute());
						break;
				}
			}
		}
		if (positions == null) {
			throw new SearchException("Failed to execute Filter");
		}
		return positions;
	}
}
