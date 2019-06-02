package db.search;

public enum DirectionEnum {
	ASC("ASC"),
	DESC("DESC");

	private final String fieldDescription;

	public String getfieldDescription() {
		return fieldDescription;
	}
	DirectionEnum(String value) {
		fieldDescription = value;
	}
}
