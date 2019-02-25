package db.structure;

import java.util.List;
import java.util.Map;

public class Index {
	protected List<String> columns;
	protected Map<Key, Integer> values;
	
	public Index(List<String> columns) {
		this.columns = columns;
	}
	
	public List<String> getColumns() {
		return columns;
	}
	
	public Map<Key, Integer> getValues() {
		return values;
	}
	
	
}
