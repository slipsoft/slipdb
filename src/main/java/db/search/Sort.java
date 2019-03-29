package db.search;

import com.dant.entity.Location;
import com.dant.entity.ResponseError;
import com.dant.entity.Type;
import db.structure.Table;

import java.util.ArrayList;

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

	public void validate(Table table, ArrayList<ResponseError> allErrors) {
		if (this.direction == null) {
			allErrors.add(new ResponseError(Location.search, Type.invalidData, "Direction is invalid in sort"));
		}
		field.validate(table, allErrors);
	}
}
