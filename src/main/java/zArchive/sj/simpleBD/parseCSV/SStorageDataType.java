package zArchive.sj.simpleBD.parseCSV;

public enum SStorageDataType {
	isUnknown(-1),
	isByte(1),
	isChar(2),
	isInteger(4),
	isLong(8),
	isFloat(4),
	isDouble(8),
	isShort(2),
	isString(-1),
	
	isStringDate(-1)
	;
	
	public final byte dataSize;
	
	private SStorageDataType(int arg_dataSize) {
		dataSize = (byte) arg_dataSize;
		
	}
	
	public byte getSize() {
		return dataSize;
	}
}
