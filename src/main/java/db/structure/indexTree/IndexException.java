package db.structure.indexTree;

import db.structure.StructureException;

public class IndexException extends StructureException {
	private static final long serialVersionUID = -8200666968152312521L;

	public IndexException(String reason) {
		super("Index exception: " + reason);
	}

}
