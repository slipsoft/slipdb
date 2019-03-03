package db.data;

import java.nio.ByteBuffer;

public class Text extends Type {

	protected final static Operator[] compatibleOperatorsList = {
		Operator.equals
	};

	public Text(int size) {
		this.size = size;
	}
	
	@Override
	public void parse(String input, ByteBuffer outputBuffer) {
		outputBuffer.put(input.getBytes());
	}
	
	@Override
	public Text get(Byte[] bytes) {
		// TODO Auto-generated method stub
		return null;
	}

}
