package db.data;

import java.nio.ByteBuffer;

public class LongType extends DataType {
	
	protected final static Operator[] compatibleOperatorsList = {
		Operator.equals,
		Operator.greater,
		Operator.less,
		Operator.greaterOrEquals,
		Operator.lessOrEquals,
	};
	
	public LongType() {
		super();
		this.sizeInBytes = Long.BYTES;
	}
	

	@SuppressWarnings("rawtypes")
	@Override
	public Class getAssociatedClassType() {
		return Long.class;
	}
	
	@Override
	public void writeToBuffer(String input, ByteBuffer outputBuffer) {
		outputBuffer.putLong(Long.parseLong(input));
	}
	
	@Override
	public Long readTrueValue(byte[] bytes) {
		ByteBuffer wrapped = ByteBuffer.wrap(bytes);
		return wrapped.getLong();
	}
	
	@Override
	public Long readIndexValue(byte[] bytes) {
		ByteBuffer wrapped = ByteBuffer.wrap(bytes);
		return wrapped.getLong();
	}

}
