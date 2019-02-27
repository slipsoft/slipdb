package db.structure;

import java.util.TreeMap;

import db.data.Operator;

public class IndexTree extends Index {
	// -> IndexTree devrait
	
	// Liste des opérateurs compatibles :
	protected final static Operator[] operators = {
		Operator.equals,
		Operator.greater,
		Operator.less,
		Operator.greaterOrEquals,
		Operator.lessOrEquals,
	};

	public IndexTree(Column[] columns) {
		super(columns);
		this.values = new TreeMap<Key, Integer>();
	}
	
	
	
}
