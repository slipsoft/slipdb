package db.search;

public class Field {
	protected String name;
	protected String alias;

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
