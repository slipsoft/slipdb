package db.data.types;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.ArrayUtils;

import db.search.Operator;


public class DoubleType extends DataType {
	private static final long serialVersionUID = 205941428943957057L;
	public static boolean sizeIsRequired = false;
	
	public DoubleType() {
		super();
		this.sizeInBytes = Double.BYTES;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Class getAssociatedClassType() {
		return Double.class;
	}
	
	@Override
	public Object parseAndWriteToBuffer(String input, ByteBuffer outputBuffer) throws IllegalArgumentException { // throws NumberFormatException {
		Double valueAsDouble = Double.parseDouble(input);
		outputBuffer.putDouble(valueAsDouble);
		return valueAsDouble;
	}
	
	@Override
	public Double readTrueValue(byte[] bytes) {
		ByteBuffer wrapped = ByteBuffer.wrap(bytes);
		return wrapped.getDouble();
	}
	
	@Override
	public Double readIndexValue(byte[] bytes) {
		ByteBuffer wrapped = ByteBuffer.wrap(bytes);
		return wrapped.getDouble();
	}

	@Override
	public boolean isOperatorCompatible(Operator op) {
		return ArrayUtils.contains(new Operator[] {
			Operator.equals,
			Operator.greater,
			Operator.less,
			Operator.greaterOrEquals,
			Operator.lessOrEquals,
			Operator.between,
		}, op);
	}

	public boolean inputCanBeParsed(String input) {
		try {
			Double.parseDouble(input);
			return true;
		} catch (IllegalArgumentException exp) {
			return false;
		}
	}
	

	public Object getDefaultValue() {
		return new Double(0);
	}

}
