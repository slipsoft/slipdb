package db.data;

public class Sort {
	protected Direction direction;
	protected Field field;
	
	public enum Direction {
		ASC,
		DESC;
	}
}
