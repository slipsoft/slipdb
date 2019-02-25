package db.structure;

import java.util.List;

public class Table {
	protected String name;
	protected List<Column> columns;
	protected List<Index> indexes;
	
	public Table(String name) {
		setName(name);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Column> getColumns() {
		return columns;
	}
	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}
	public List<Index> getIndexes() {
		return indexes;
	}
	public void setIndexes(List<Index> indexes) {
		this.indexes = indexes;
	}
}
