package db.data;

import java.nio.ByteBuffer;

public class FloatType extends DataType {

	public static boolean sizeIsRequired = false;
	
	protected final static Operator[] compatibleOperatorsList = {
		Operator.equals,
		Operator.greater,
		Operator.less,
		Operator.greaterOrEquals,
		Operator.lessOrEquals,
	};
	
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
	public Float writeToBuffer(String input, ByteBuffer outputBuffer) {
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

}
