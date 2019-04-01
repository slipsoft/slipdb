package db.structure;

import java.util.ArrayList;
import java.util.HashMap;

import db.data.types.DataPositionList;
import db.search.Predicate;
import index.indexTree.IndexException;

import org.apache.commons.lang3.ArrayUtils;

import db.search.Operator;

public class IndexHash extends Index {

	public IndexHash(Column[] columns) {
		super(columns);
		this.indexedValuesMap = new HashMap<Key, ArrayList<Integer>>();
	}

	@Override
	public DataPositionList getPositionsFromPredicate(Predicate predicate) throws IndexException {
		// TODO make this func
		return null;
	}

	@Override
	public boolean isOperatorCompatible(Operator op) {
		if(!ArrayUtils.contains(new Operator[] {
				Operator.equals
			}, op)) {
			return false;
		}
		for (Column column : indexedColumnsList) {
			if (!column.getDataType().isOperatorCompatible(op)) {
				return false;
			}
		}
		return true;
	}

}
