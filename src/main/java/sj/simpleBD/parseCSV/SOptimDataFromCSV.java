package sj.simpleBD.parseCSV;


public enum SOptimDataFromCSV {
	
	// Parser de la donnée simplement
	toByte(SStorageDataType.isByte),
	toChar(SStorageDataType.isChar),
	toInteger(SStorageDataType.isInteger),
	toLong(SStorageDataType.isLong),
	toFloat(SStorageDataType.isFloat),
	toDouble(SStorageDataType.isDouble),
	
	// Parser avec conversion
	floatToByte(SStorageDataType.isByte), // multiplie par 10
	floatToShort(SStorageDataType.isShort), // multiplie par 100
	dateStringToInteger(SStorageDataType.isInteger) // convertit la date en nombre
	;
	
	public final SStorageDataType realDataType;
	
	private SOptimDataFromCSV(SStorageDataType argRealDataType) {
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
