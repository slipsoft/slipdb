package db.data;

import java.nio.ByteBuffer;

public class StringType extends DataType {

	protected final static Operator[] compatibleOperatorsList = {
		Operator.equals
	};

	public StringType(int size) {
		this.sizeInBytes = size;
	}
	
	@Override
	public void parse(String input, ByteBuffer outputBuffer) {
		outputBuffer.put(input.getBytes());
	}
	
	@Override
	public String getValueFromByteArray(byte[] bytes) {
		return new String(bytes);
	}

}
