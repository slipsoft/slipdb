package db.data.types;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.ArrayUtils;

import db.search.Operator;

public class FloatType extends DataType {
	private static final long serialVersionUID = -8609612884136762449L;
	public static boolean sizeIsRequired = false;
	
	public FloatType() {
		super();
		this.sizeInBytes = Float.BYTES;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Class getAssociatedClassType() {
		return Float.class;
	}

	@Override
	public Object parseAndWriteToBuffer(String input, ByteBuffer outputBuffer) throws IllegalArgumentException { // throws NumberFormatException {
		Float asFloat = Float.parseFloat(input);
		outputBuffer.putFloat(asFloat);
		return asFloat;
	}
	
	@Override
	public Float readTrueValue(byte[] bytes) {
		ByteBuffer wrapped = ByteBuffer.wrap(bytes);
		return wrapped.getFloat();
	}
	
	@Override
	public Float readIndexValue(byte[] bytes) {
		ByteBuffer wrapped = ByteBuffer.wrap(bytes);
		return wrapped.getFloat();
	}

	@Override
	public boolean isOperatorCompatible(Operator op) {
		return ArrayUtils.contains(new Operator[] {
			Operator.equals,
			Operator.greater,
			Operator.less,
			Operator.greaterOrEquals,
			Operator.lessOrEquals,
		}, op);
	}

	public boolean inputCanBeParsed(String input) {
		try {
			Float.parseFloat(input);
			return true;
		} catch (IllegalArgumentException exp) {
			return false;
		}
	}

}
