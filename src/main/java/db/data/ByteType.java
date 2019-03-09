package db.data;

import java.nio.ByteBuffer;

public class ByteType extends DataType {

	protected final static Operator[] compatibleOperatorsList = {
		Operator.equals
	};

	public ByteType() {
		this.sizeInBytes = Byte.BYTES;
	}
	
	@Override
	public void parse(String input, ByteBuffer outputBuffer) {
		outputBuffer.put(Byte.parseByte(input));
	}
	
	@Override
	public Byte getValueFromByteArray(byte[] bytes) {
		return new Byte(bytes[0]); // get the associates byte
	}

}
