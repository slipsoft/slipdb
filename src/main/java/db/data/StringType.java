package db.data;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

public class StringType extends DataType {
	public static boolean sizeIsRequired = true;
	public static int maxSizeInBytes = 65535; //obligatoire si sizeIsRequired => true:
	
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

	@Override
	public boolean isOperatorCompatible(Operator op) {
		return ArrayUtils.contains(new Operator[] {
			Operator.equals
		}, op);
	}

}
