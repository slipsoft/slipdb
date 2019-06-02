package db.search;

import db.structure.Database;
import db.structure.Table;

import javax.ws.rs.BadRequestException;

public class Field {
	protected String name;
	protected String alias;

	public void validate(Table table) {
		if (this.name != null) {
			if (!com.dant.utils.Utils.validateRegex(Database.getInstance().config.tableNamePattern, this.name) || !table.getColumnByName(name).isPresent()) {
				throw new BadRequestException("column name is invalid in field");
			}
		} else {
			throw new BadRequestException("field name is invalid in field");
		}

		if (this.alias != null){
			if (!com.dant.utils.Utils.validateRegex(Database.getInstance().config.tableNamePattern, this.alias)) {
				throw new BadRequestException("alias name is invalid in field");
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


