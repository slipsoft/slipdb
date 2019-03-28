package db.search;

public class SearchException extends Exception {

	/**
	 * Generated UID
	 */
	private static final long serialVersionUID = -255911111526192724L;

	public SearchException(Exception e) {
		super(e);
	}

	public SearchException(String reason) {
		super("Search exception: " + reason);
	}
}
