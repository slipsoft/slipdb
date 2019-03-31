package db.search;

import db.data.types.DataPositionList;
import org.apache.commons.collections4.ListUtils;

public class FilterGroup implements FilterTerm {
	protected JoinMethod join = JoinMethod.and;
	protected FilterTerm[] terms = new FilterTerm[0];
	
	public FilterGroup(JoinMethod join, FilterTerm[] term) {
		this.join = join;
		this.terms = term;

	}

	@Override
	public DataPositionList execute() throws SearchException {
		DataPositionList positions = null;
		for (FilterTerm filterTerm : terms) {
			if (positions == null) {
				positions = filterTerm.execute();
			} else {
				switch (join) {
					case and:
						positions = (DataPositionList) ListUtils.intersection(positions, filterTerm.execute());
					case or:
						positions = (DataPositionList) ListUtils.union(positions, filterTerm.execute());
				}
			}
		}
		if (positions == null) {
			throw new SearchException("Failed to execute Filter");
		}
		return positions;
	}
}
