package db.data;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.ArrayUtils;

import db.search.Operator;

public class IntegerType extends DataType {
	private static final long serialVersionUID = 433681923805061790L;
	public static boolean sizeIsRequired = false;
	
	public IntegerType() {
		super();
		this.sizeInBytes = Integer.BYTES;
	}
	

	@SuppressWarnings("rawtypes")
	@Override
	public Class getAssociatedClassType() {
		return Integer.class;
	}
	
	@Override
	public Object parseAndWriteToBuffer(String input, ByteBuffer outputBuffer) throws IllegalArgumentException { // throws NumberFormatException {
		Integer asInteger = Integer.parseInt(input);
		outputBuffer.putInt(asInteger);
		return asInteger;
	}
	
	@Override
	public Integer readTrueValue(byte[] bytes) {
		ByteBuffer wrapped = ByteBuffer.wrap(bytes);
		return wrapped.getInt();
	}
	
	@Override
	public Integer readIndexValue(byte[] bytes) {
		ByteBuffer wrapped = ByteBuffer.wrap(bytes);
		return wrapped.getInt();
	}

	@Override
	public boolean isOperatorCompatible(Operator op) {
		return ArrayUtils.contains(new Operator[] {
			Operator.equals,
			Operator.greater,
			Operator.less,
			Operator.greaterOrEquals,
			Operator.lessOrEquals,
		}, op);
	}

	public boolean inputCanBeParsed(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (IllegalArgumentException exp) {
			return false;
		}
	}
}
