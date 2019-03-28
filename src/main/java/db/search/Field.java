package db.search;

public class Field {
	protected String name;
	protected String alias;

	Field(String name) {
		this.name = name;
	}

	Field(String name, String alias) {
		this(name);
		this.alias = alias;
	}

	public String getName() {
		return name;
	}
}
