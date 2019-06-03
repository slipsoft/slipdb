package com.dant.entity;

import db.data.types.*;

public enum TypeEnum {
	Byte(ByteType.class),
	Date(DateType.class),
	Double(DoubleType.class),
	Float(FloatType.class),
	Integer(IntegerType.class),
	Long(LongType.class),
	String(StringType.class);

	private final Class value;

	public Class convertToClass() {
		return value;
	}

	TypeEnum(Class param) {
		value = param;
	}
	public static TypeEnum valueOf(Class param) {
		for (TypeEnum e : values()) {
			if (e.value.equals(param)) {
				return e;
			}
		}
		return null;
	}
}
