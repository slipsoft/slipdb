package db.search;

import db.structure.Table;

import javax.ws.rs.BadRequestException;

public class Sort {
	protected Direction direction;
	protected Field field;
	
	public enum Direction {
		ASC("ASC"),
		DESC("DESC");

		private final String fieldDescription;

		public String getfieldDescription() {
			return fieldDescription;
		}
		Direction(String value) {
			fieldDescription = value;
		}
	}

	public void validate(Table table) {
		if (this.direction == null) {
			throw new BadRequestException("Direction is invalid in sort");
		}
		field.validate(table);
	}
}
