package db.structure;

import java.util.HashMap;

import db.data.Operator;

public class IndexHash extends Index {
	protected final static Operator[] operators = {
		Operator.equals
	};

	public IndexHash(Column[] columns) {
		super(columns);
		this.values = new HashMap<Key, Integer>();
	}

}
