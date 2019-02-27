package db.structure;

import java.util.HashMap;

import db.data.Operator;

public class IndexHash extends Index {
	
	// Liste des opérateurs compatibles :
	protected final static Operator[] operators = {
		Operator.equals
	};

	public IndexHash(Column[] columns) {
		super(columns);
		this.values = new HashMap<Key, Integer>();
	}

}
