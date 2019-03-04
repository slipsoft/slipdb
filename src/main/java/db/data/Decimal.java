package db.data;

import java.nio.ByteBuffer;

public class Decimal extends Type {
	
	protected final static Operator[] compatibleOperatorsList = {
		Operator.equals,
		Operator.greater,
		Operator.less,
		Operator.greaterOrEquals,
		Operator.lessOrEquals,
	};

	public Decimal(int size) {
		this.size = size;
	}
	
	@Override
	public void parse(String input, ByteBuffer outputBuffer) {
		outputBuffer.putDouble(Double.parseDouble(input));
	}
	
	@Override
	public Double get(byte[] bytes) {
		ByteBuffer wrapped = ByteBuffer.wrap(bytes);
		return wrapped.getDouble();
	}

}
