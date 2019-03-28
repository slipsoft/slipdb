package db.structure;

public class StructureException extends Exception {
	private static final long serialVersionUID = -8205544458152312521L;

	public StructureException(Exception e) {
		super(e);
	}

	public StructureException(String reason) {
		super("Structure exception: " + reason);
	}

}
