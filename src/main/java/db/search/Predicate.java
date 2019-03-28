package db.search;

import db.data.DataPositionList;
import db.structure.Column;
import db.structure.Index;
import db.structure.Table;
import db.structure.indexTree.IndexException;

public class Predicate implements FilterTerm {
	protected Column column;
	protected Operator operator;
	protected Object value;
	protected Index index;

	public Predicate(Column column, Operator operator, Object value) {
		this.column = column;
		this.operator = operator;
		this.value = value;
	}

	public void findBestIndex(Table table) throws Exception {
		this.index = table.findBestIndex(this);
	}

	public Column getColumn() {
		return column;
	}

	public Operator getOperator() {
		return operator;
	}

	public Index getIndex() {
		return this.index;
	}

	public Object getValue() { return this.value; }

	@Override
	public DataPositionList execute() throws SearchException {
		try {
			return this.index.getPositionsFromPredicate(this);
		} catch (IndexException e) {
			throw new SearchException(e.getMessage());
		}
	}
}
