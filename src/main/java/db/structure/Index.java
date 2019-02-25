package db.structure;

import java.util.List;
import java.util.Map;

public class Index {
	protected List<String> columns;
	protected Map<Object[], Integer> values;
	
	public Index(List<String> columns) {
		setColumns(columns);
	}
	
	public List<String> getColumns() {
		return columns;
	}
	public void setColumns(List<String> columns) {
		this.columns = columns;
	}
	public Map<Object[], Integer> getValues() {
		return values;
	}
	public void setValues(Map<Object[], Integer> values) {
		this.values = values;
	}
	
	
}
