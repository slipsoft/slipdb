package db.data;

import java.nio.ByteBuffer;

public class IntegerType extends DataType {
	
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
	public void parse(String input, ByteBuffer outputBuffer) {
		outputBuffer.putInt(Integer.parseInt(input));
	}
	
	@Override
	public Integer parseAndReturnValue(String input, ByteBuffer outputBuffer) {
		Integer asInteger = Integer.parseInt(input);
		outputBuffer.putInt(asInteger);
		return asInteger;
	}
	
	@Override
	public Integer getValueFromByteArray(byte[] bytes) {
		ByteBuffer wrapped = ByteBuffer.wrap(bytes);
		return wrapped.getInt();
	}

}