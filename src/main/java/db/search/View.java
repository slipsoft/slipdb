package db.search;

import db.data.types.DataPositionList;
import db.structure.Column;
import db.structure.recherches.TableHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class View {

	protected TableHandler tableHandler;
	protected FilterTerm filter;
	protected List<Field>  fields = new ArrayList<>();
	protected List<Sort>   sorts  = new ArrayList<>();
	protected Group groupBy;

	public View(TableHandler tableHandler, FilterTerm filter, List<Field> fields, List<Sort> sorts, Group groupBy){
		this.tableHandler = tableHandler;
		this.filter = filter;
		this.fields = fields;
		this.sorts = sorts;
		this.groupBy = groupBy;
	}

	Column[] getListColumns() throws SearchException {
		int nbFields = fields.size();
		Column[] columns = new Column[nbFields];
		for (int i = 0; i < nbFields; i++) {
			Optional<Column> column = this.tableHandler.getAssociatedTable().getColumnByNameNoCheck(fields.get(i).name);
			if (column.isPresent()) {
				columns[i] = column.get();
			} else {
				throw new SearchException("Field not found");
			}
		}
		return columns;
	}

	public ResultSet execute() throws SearchException {
		Column[] columns = getListColumns();
		DataPositionList positions = this.filter.execute();
		try {
		return tableHandler.getFullResultsFromBinIndexes(positions);
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}
}
