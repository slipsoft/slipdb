package db.search;

public class Sort {
	protected Direction direction;
	protected Field field;
	
	public enum Direction {
		ASC,
		DESC;
	}
}
