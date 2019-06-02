package db.search;

import db.structure.Column;
import db.structure.Table;

import java.util.ArrayList;
import java.util.List;

public class Group {
	// should be a class different from Column as it could also be an field created in the view...
	protected List<Column> fields = new ArrayList<>();

	public Group(List<Column> fields) {
		this.fields = fields;
	}
	public Group() {
	}
}
