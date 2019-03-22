package db.data;

import db.structure.Column;
import db.structure.Index;
import db.structure.Table;

public class Filter {
	protected Column column;
	protected Operator operator;
	protected Object value;
	protected Index index;

	public Filter(Column column, Operator operator, Object value) {
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
}
