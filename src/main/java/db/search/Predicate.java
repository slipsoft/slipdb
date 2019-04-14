package db.search;

import db.disk.dataHandler.DiskDataPosition;
import db.structure.Column;
import db.structure.Index;
import db.structure.StructureException;
import db.structure.Table;

import java.util.HashSet;
import java.util.Set;

public class Predicate implements FilterTerm {
	protected Table table;
	protected Column column;
	protected Operator operator;
	protected Object value;
	protected Index index;

	public Predicate(Table table, Column column, Operator operator, Object value) {
		this.table = table;
		this.column = column;
		this.operator = operator;
		this.value = value;
		try {
			findBestIndex();
		} catch (Exception exp) {
			this.index = null;
		}
	}

	private Index findBestIndex() throws StructureException {
		index = table.findBestIndex(this);
		return index;
	}

	public Column getColumn() {
		return column;
	}

	public Operator getOperator() {
		return operator;
	}

	public Index getIndex() throws StructureException {
		if (this.index == null){
			findBestIndex();
		}
		return index;
	}

	public Object getValue() { return this.value; }

	@Override
	public Set<DiskDataPosition> execute() throws SearchException {
		try {
			return new HashSet<>(getIndex().getPositionsFromPredicate(this));
		} catch (StructureException e) {
			throw new SearchException(e);
		}
	}
}
