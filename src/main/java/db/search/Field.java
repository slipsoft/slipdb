package db.search;

import com.dant.entity.Location;
import com.dant.entity.ResponseError;
import com.dant.entity.Type;
import db.structure.Database;
import db.structure.Table;

import java.util.ArrayList;

public class Field {
	protected String name;
	protected String alias;

	public void validate(Table table, ArrayList<ResponseError> allErrors) {
		if (this.name != null) {
			if (!com.dant.utils.Utils.validateRegex(Database.getInstance().config.tableNamePattern, this.name) || !table.getColumnByName(name).isPresent()) {
				allErrors.add(new ResponseError(Location.search, Type.invalidData, name + "column name is invalid in field"));
			}
		} else {
			allErrors.add(new ResponseError(Location.search, Type.invalidData, name + "field name is invalid in field"));
		}

		if (this.alias != null){
			if (!com.dant.utils.Utils.validateRegex(Database.getInstance().config.tableNamePattern, this.alias)) {
				allErrors.add(new ResponseError(Location.search, Type.invalidData, name + "alias name is invalid in field"));
			}
		}
	}

	public Field(String name) {
		this.name = name;
	}

	public Field(String name, String alias) {
		this(name);
		this.alias = alias;
	}

	public String getName() {
		return name;
	}
}


