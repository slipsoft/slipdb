package db.data;

import java.nio.ByteBuffer;

public class IntegerType extends DataType {

	public static boolean sizeIsRequired = false;
	
	protected final static Operator[] compatibleOperatorsList = {
		Operator.equals,
		Operator.greater,
		Operator.less,
		Operator.greaterOrEquals,
		Operator.lessOrEquals,
	};
	
	public IntegerType() {
		super();
		this.sizeInBytes = Integer.BYTES;
	}
	

	@SuppressWarnings("rawtypes")
	@Override
	public Class getAssociatedClassType() {
		return Integer.class;
	}
	
	@Override
	public Integer writeToBuffer(String input, ByteBuffer outputBuffer) {
		Integer asInteger = Integer.parseInt(input);
		outputBuffer.putInt(asInteger);
		return asInteger;
	}
	
	@Override
	public Integer readTrueValue(byte[] bytes) {
		ByteBuffer wrapped = ByteBuffer.wrap(bytes);
		return wrapped.getInt();
	}
	
	@Override
	public Integer readIndexValue(byte[] bytes) {
		ByteBuffer wrapped = ByteBuffer.wrap(bytes);
		return wrapped.getInt();
	}

}
