package zArchive.sj.simpleDB.arrayList;


public enum Al_SOptimDataFromCSV {
	
	// Parser de la donnée simplement
	toByte(AL_SStorageDataType.isByte),
	toChar(AL_SStorageDataType.isChar),
	toInteger(AL_SStorageDataType.isInteger),
	toLong(AL_SStorageDataType.isLong),
	toFloat(AL_SStorageDataType.isFloat),
	toDouble(AL_SStorageDataType.isDouble),
	
	// Parser avec conversion
	floatToByte(AL_SStorageDataType.isByte), // multiplie par 10
	floatToShort(AL_SStorageDataType.isShort), // multiplie par 100
	dateStringToInteger(AL_SStorageDataType.isInteger) // convertit la date en nombre
	;
	
	public final AL_SStorageDataType realDataType;
	
	Al_SOptimDataFromCSV(AL_SStorageDataType argRealDataType) {
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
