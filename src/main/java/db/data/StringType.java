package db.data;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class StringType extends DataType {

	protected final static Operator[] compatibleOperatorsList = {
		Operator.equals
	};
	
	public StringType(int size) {
		super();
		this.sizeInBytes = size;
	}
	
	@Override
	public void parse(String input, ByteBuffer outputBuffer) {
		byte[] bytes = Arrays.copyOf(input.getBytes(), this.sizeInBytes);
		outputBuffer.put(bytes);
	}
	
	@Override
	public Object parseAndReturnValue(String input, ByteBuffer outputBuffer) {
		byte[] bytes = Arrays.copyOf(input.getBytes(), this.sizeInBytes);
		outputBuffer.put(bytes);
		return input;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Class getAssociatedClassType() {
		return String.class;
	}
	
	@Override
	public String getValueFromByteArray(byte[] bytes) {
		return new String(bytes).replaceAll("\0", "");
	}

}