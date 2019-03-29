package db.data.load;

public class IncorrectEntryException extends Exception {

	/**
	 * Generated UID
	 */
	private static final long serialVersionUID = -2559700888526192724L;

	public IncorrectEntryException(Exception e) {
		super(e);
	}

	public IncorrectEntryException(int entryId, String reason) {
		super("Fail to save entry number " + entryId + ": " + reason);
	}
}
