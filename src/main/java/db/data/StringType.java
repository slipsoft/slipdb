package db.data;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.dant.utils.Log;

public class StringType extends DataType {
	public static boolean sizeIsRequired = true;
	public static int maxSizeInBytes = 65535; //obligatoire si sizeIsRequired => true:
	
	public StringType(int size) {
		super();
		this.sizeInBytes = size;
	}

	public final String stringPaddingChar = "_";
	
	
	
	@Override
	public Object writeToBuffer(String input, ByteBuffer outputBuffer) {
		
		/* débug OK ! if ("2015-04-02 21:12:28".equals(input)) {//2015-04-04 10:17:26
			Log.error("FOUNd input  input = " + input);
		}*/
		
		if (input.length() < sizeInBytes) {
			input = StringUtils.rightPad(input, sizeInBytes, stringPaddingChar);
			//Log.info("padding  str -> " + input);
			
		}
		
		byte[] bytes;
		if (input.length() > sizeInBytes)
			bytes = Arrays.copyOf(input.getBytes(), this.sizeInBytes);
		else
			bytes = input.getBytes();
		
		//bytes[2] = 0;
		/*if (input.length() != sizeInBytes) {
			Log.info("input.length() != sizeInBytes -> " + input.length() + " != " + sizeInBytes + "  bytes = " + bytes.length + "  str -> " + new String(bytes));
		} débug */
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
		return new String(bytes).replaceAll(stringPaddingChar, ""); // "\0" -> stringPaddingChar
	}
	
	@Override
	public String readIndexValue(byte[] bytes) {
		return new String(bytes).replaceAll(stringPaddingChar, ""); // "\0" -> stringPaddingChar
	}

	@Override
	public boolean isOperatorCompatible(Operator op) {
		return ArrayUtils.contains(new Operator[] {
			Operator.equals
		}, op);
	}

}
