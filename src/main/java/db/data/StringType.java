package db.data;

import java.nio.ByteBuffer;

import com.dant.utils.Utils;

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
		outputBuffer.put(input.getBytes());
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Class getAssociatedClassType() {
		return String.class;
	}
	
	@Override
	public String getValueFromByteArray(byte[] bytes) {
		return new String(bytes);
	}

}
