package db.data.types;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import db.search.Operator;

public class StringType extends DataType {
	private static final long serialVersionUID = -3628502984256891019L;
	public static boolean sizeIsRequired = true;
	public static int maxSizeInBytes = 65535; //obligatoire si sizeIsRequired => true:
	
	public StringType(int size) {
		super();
		this.sizeInBytes = size;
	}

	public static final String stringPaddingChar = "\0";
	
	// -> Je laisse comme ça si ça te vas, Nicolas, pour qu'on puisse vérifier s'il n'y a bien pas de caractère 0 dans les string ?
	// /!\ stringPaddingChar ne DOIT PAS se trouver dans les String des données lues en entrée, sous peine de corrompre les données
	@Override
	public Object parseAndWriteToBuffer(String input, ByteBuffer outputBuffer) throws IllegalArgumentException { // throws NumberFormatException {		
		
		/* inutile si (stringPaddingChar == "\0")
		if (input.length() < sizeInBytes) {
			input = StringUtils.rightPad(input, sizeInBytes, stringPaddingChar);
			//Log.info("padding  str -> " + input);
		}*/
		
		byte[] bytes;
		if (input.length() != sizeInBytes)
			bytes = Arrays.copyOf(input.getBytes(), this.sizeInBytes);
		else
			bytes = input.getBytes();
		
		outputBuffer.put(bytes);
		return input;
	}

	public static byte[] stringToRightSizeByteArray(String input, int sizeInBytes) {
		byte[] bytes;
		if (input.length() != sizeInBytes)
			bytes = Arrays.copyOf(input.getBytes(), sizeInBytes);
		else
			bytes = input.getBytes();
		return bytes;
	}
	
	public static String getOriginalString(byte[] bytes) { // FromRightSizeByteArray
		return new String(bytes).replaceAll(stringPaddingChar, ""); // "\0" -> stringPaddingChar
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

	public boolean inputCanBeParsed(String input) {
		return true;
	}
	
	public Object getDefaultValue() { // padding avec des zéros
		return new String(Arrays.copyOf("".getBytes(), this.sizeInBytes));
	}

}
