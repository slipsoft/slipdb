package db.parseCSV;


public enum OptimDataFromCSV {
	
	// Parser de la donnée simplement
	toByte(StorageDataType.isByte),
	toChar(StorageDataType.isChar),
	toInteger(StorageDataType.isInteger),
	toLong(StorageDataType.isLong),
	toFloat(StorageDataType.isFloat),
	toDouble(StorageDataType.isDouble),
	
	// Parser avec conversion
	floatToByte(StorageDataType.isByte), // multiplie par 10
	floatToShort(StorageDataType.isShort), // multiplie par 100
	dateStringToInteger(StorageDataType.isInteger) // convertit la date en nombre
	;
	
	public final StorageDataType realDataType;
	
	private OptimDataFromCSV(StorageDataType argRealDataType) {
		realDataType = argRealDataType;
	}

	public static byte convertFloatToByte(float data) {
		return (byte) (data * 10);
	}
	
	public static short convertFloatToShort(float data) {
		return (short) (data * 100);
	}

	public static float getInitialFloatFromByte(byte convertedData) {
		return convertedData / 10;
	}
	
	public static float getInitialFloatFromShort(short convertedData) {
		return convertedData / 100;
	}
	
}
