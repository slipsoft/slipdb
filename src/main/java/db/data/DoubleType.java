package db.data;

import java.nio.ByteBuffer;


public class DoubleType extends DataType {
	
	protected final static Operator[] compatibleOperatorsList = {
		Operator.equals,
		Operator.greater,
		Operator.less,
		Operator.greaterOrEquals,
		Operator.lessOrEquals,
	};
	
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
	public void writeToBuffer(String input, ByteBuffer outputBuffer) {
		outputBuffer.putDouble(Double.parseDouble(input));
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

}
