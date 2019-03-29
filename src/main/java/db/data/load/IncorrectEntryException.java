package db.data.load;

public class IncorrectEntryException extends Exception {

	/**
	 * Generated UID
	 */
	private static final long serialVersionUID = -2559700888526192724L;

	public IncorrectEntryException(int entryId, String reason) {
		super("Fail to save entry number " + entryId + ": " + reason);
	}
}
