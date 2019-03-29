package db.data;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.ArrayUtils;

import db.search.Operator;

public class LongType extends DataType {
	private static final long serialVersionUID = -7928604950572059766L;
	public static boolean sizeIsRequired = false;
	
	public LongType() {
		super();
		this.sizeInBytes = Long.BYTES;
	}
	

	@SuppressWarnings("rawtypes")
	@Override
	public Class getAssociatedClassType() {
		return Long.class;
	}
	
	@Override
	public Object parseAndWriteToBuffer(String input, ByteBuffer outputBuffer) throws IllegalArgumentException { // throws NumberFormatException {
		Long asLong = Long.parseLong(input);
		outputBuffer.putLong(asLong);
		return asLong;
	}
	
	@Override
	public Long readTrueValue(byte[] bytes) {
		ByteBuffer wrapped = ByteBuffer.wrap(bytes);
		return wrapped.getLong();
	}
	
	@Override
	public Long readIndexValue(byte[] bytes) {
		ByteBuffer wrapped = ByteBuffer.wrap(bytes);
		return wrapped.getLong();
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
			Long.parseLong(input);
			return true;
		} catch (IllegalArgumentException exp) {
			return false;
		}
	}

}
