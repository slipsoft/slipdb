package index.indexHash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import db.data.types.DataPositionList;
import db.disk.dataHandler.DiskDataPosition;
import db.search.Predicate;
import db.structure.Column;
import db.structure.Index;
import db.structure.Table;
import index.IndexException;

import org.apache.commons.lang3.ArrayUtils;

import db.search.Operator;
@Deprecated
public class IndexHash extends Index {
	
	// Hash map associant une clef à une liste de positions (index des lignes parsées des csv)
	protected HashMap<KeyHash, ArrayList<Integer>> indexedValuesMap;
	
	public IndexHash(Table table, Column column) {
		super(table, new Column[] {column});
		this.indexedValuesMap = new HashMap<>();
	}
	
	@Override
	public DataPositionList getPositionsFromPredicate(Predicate predicate) throws IndexException {
		// TODO make this func
		return null;
	}
	
	@Override
	public void addValue(Object value, DiskDataPosition position) throws IOException {
		// TODO
	}
	
	@Override
	public void flushOnDisk() throws IOException {
		// TODO
	}

	@Override
	public void indexEntry(Object[] entry, int id) throws IndexException {

	}

	@Override
	protected Operator[] compatibleOperators() {
		return new Operator[] {
			Operator.equals,
		};
	}

	@Override
	public int[] getIdsFromPredicate(Predicate predicate) throws IndexException {
		return new int[0];
	}

}
