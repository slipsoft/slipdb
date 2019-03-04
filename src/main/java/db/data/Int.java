package db.data;

import java.nio.ByteBuffer;

public class Int extends Type {
	
	protected final static Operator[] compatibleOperatorsList = {
		Operator.equals,
		Operator.greater,
		Operator.less,
		Operator.greaterOrEquals,
		Operator.lessOrEquals,
	};

	public Int(int size) {
		this.size = size;
	}
	
	@Override
	public void parse(String input, ByteBuffer outputBuffer) {
		outputBuffer.putInt(Integer.parseInt(input));
	}
	
	@Override
	public Integer get(Byte[] bytes) {
		// TODO Auto-generated method stub
		return null;
	}

}
