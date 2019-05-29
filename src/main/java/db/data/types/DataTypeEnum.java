package db.data.types;

public enum DataTypeEnum {
	UNKNOWN,
	BYTE,
	INTEGER,
	LONG,
	DATE,
	FLOAT,
	DOUBLE,
	STRING;

	public static DataTypeEnum instanciate(DataType dataType) {
		Class<? extends DataType> objectClass = dataType.getClass();

		if (objectClass == ByteType.class) return DataTypeEnum.BYTE;
		if (objectClass == IntegerType.class) return DataTypeEnum.INTEGER;
		if (objectClass == LongType.class) return DataTypeEnum.LONG;
		if (objectClass == DateType.class) return DataTypeEnum.DATE;
		if (objectClass == FloatType.class) return DataTypeEnum.FLOAT;
		if (objectClass == DoubleType.class) return DataTypeEnum.DOUBLE;
		if (objectClass == StringType.class) return DataTypeEnum.STRING;
		
		return DataTypeEnum.UNKNOWN;
	}
	
	private DataTypeEnum() {
		
	}
	
}
