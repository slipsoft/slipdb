package db.search;

import db.disk.dataHandler.DiskDataPosition;
import db.structure.Column;
import db.structure.Table;

import java.util.*;
import java.util.stream.Collectors;

public class View {

	protected Table table;
	protected FilterTerm filter;
	protected List<Field> fields;
	protected List<Sort> sorts;
	protected Group groupBy;

	public View(Table table, FilterTerm filter, List<Field> fields, List<Sort> sorts, Group groupBy) {
		this.table = table;
		this.filter = filter;
		this.fields = fields;
		this.sorts = sorts;
		this.groupBy = groupBy;
	}

	public View(Table table, FilterTerm filter) {
		this(table, filter, new ArrayList<>(), new ArrayList<>(), new Group());
	}

	/**
	 * Get the list of columns to return based on the fields of the view
	 * @return - an array of Columns
	 * @throws SearchException - in case of Field not found
	 */
	Column[] getListColumns() throws SearchException {
		int nbFields = fields.size();
		Column[] columns = new Column[nbFields];
		for (int i = 0; i < nbFields; i++) {
			Optional<Column> column = this.table.getColumnByNameNoCheck(fields.get(i).name);
			if (column.isPresent()) {
				columns[i] = column.get();
			} else {
				throw new SearchException("Field not found");
			}
		}
		return columns;
	}

	/**
	 * Executes the view on the table to get a resut set
	 * @return - a result set
	 * @throws SearchException - in case of search failure
	 */
	public Set<Object> execute() throws SearchException {
		Column[] columns = getListColumns();
		Set<Integer> positions = this.filter.execute();
		try {
			return Arrays.stream(columns).map((Column col) -> col.getDataAsRawBytes(2)).collect(Collectors.toSet());
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}
}
