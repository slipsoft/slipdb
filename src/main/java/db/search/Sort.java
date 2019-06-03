package db.search;

import db.structure.Column;

public class Sort {
	protected DirectionEnum direction;
	// should be a class different from Column as it could also be an field created in the view...
	protected Column fields;

	public Sort(DirectionEnum direction, Column fields) {
		this.direction = direction;
		this.fields = fields;
	}
}
