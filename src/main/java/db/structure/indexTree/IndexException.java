package db.structure.indexTree;

public class IndexException extends Exception {
	private static final long serialVersionUID = -8200666968152312521L;
	
	public final IndexExceptionCode exceptionCode;
	
	public IndexException(IndexExceptionCode argExceptionCode) {
		exceptionCode = argExceptionCode;
	}

}
