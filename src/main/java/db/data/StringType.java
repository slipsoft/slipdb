package db.data;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class StringType extends DataType {
	public String name = "String";

	protected final static Operator[] compatibleOperatorsList = {
		Operator.equals
	};
	
	public StringType(int size) {
		super();
		this.sizeInBytes = size;
	}
	
	@Override
	public void writeToBuffer(String input, ByteBuffer outputBuffer) {
		byte[] bytes = Arrays.copyOf(input.getBytes(), this.sizeInBytes);
		outputBuffer.put(bytes);
	}
	
	@Override
	public Object writeToBufferAndReturnValue(String input, ByteBuffer outputBuffer) {
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
	public String readTrueValue(byte[] bytes) {
		return new String(bytes).replaceAll("\0", "");
	}
	
	@Override
	public String readIndexValue(byte[] bytes) {
		return new String(bytes).replaceAll("\0", "");
	}

}
