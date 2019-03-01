package db.structure;

import java.util.HashMap;

import db.data.Operator;

public class IndexHash extends Index {
	
	// Liste des opérateurs compatibles :
	protected final static Operator[] compatibleOperatorsList = {
		Operator.equals
	};
	
	public IndexHash(Column[] columns) {
		super(columns);
		this.indexedValuesMap = new HashMap<Key, Integer>();
	}

}
