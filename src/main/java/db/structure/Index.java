package db.structure;

import java.util.Map;

import db.data.Operator;

public abstract class Index {
	protected static Operator[] operators;
	protected Column[] columns;
	protected Map<Key, Integer> values;
	
	public Index(Column[] columns) {
		this.columns = columns;
	}
	
	public Column[] getColumns() {
		return columns;
	}
	
	public Map<Key, Integer> getValues() {
		return values;
	}
	
	
}
