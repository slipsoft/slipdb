package db.data;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.ArrayUtils;


public class ByteType extends DataType {
	public static boolean sizeIsRequired = false;
	
	public ByteType() {
		super();
		this.sizeInBytes = Byte.BYTES;
	}
	
	
	@SuppressWarnings("rawtypes")
	@Override
	public Class getAssociatedClassType() {
		return Byte.class;
	}

	@Override
	public Byte writeToBuffer(String input, ByteBuffer outputBuffer) {
		Byte asByte = Byte.parseByte(input);
		outputBuffer.put(asByte);
		return asByte;
	}
	
	@Override
	public Byte readTrueValue(byte[] bytes) {
		return new Byte(bytes[0]); // get the associates byte
	}
	
	@Override
	public Byte readIndexValue(byte[] bytes) {
		return new Byte(bytes[0]); // get the associates byte
	}

	@Override
	public boolean isOperatorCompatible(Operator op) {
		return ArrayUtils.contains(new Operator[] {
			Operator.equals
		}, op);
	}
}
