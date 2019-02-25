package db.structure;

import java.util.List;

public class Table {
	protected String name;
	protected List<Column> columns;
	protected List<Index> indexes;
	
	public Table(String name, List<Column> columns) {
		this.name = name;
		this.columns = columns;
	}
	
	public String getName() {
		return name;
	}
	
	public List<Column> getColumns() {
		return columns;
	}

	public List<Index> getIndexes() {
		return indexes;
	}
	
	public void addIndex(Index index) {
		this.indexes.add(index);
	}
}
