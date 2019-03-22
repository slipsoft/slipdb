package db.data;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.ArrayUtils;


public class DoubleType extends DataType {

	public static boolean sizeIsRequired = false;
	
	public DoubleType() {
		super();
		this.sizeInBytes = Double.BYTES;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Class getAssociatedClassType() {
		return Double.class;
	}
	
	@Override
	public Object writeToBuffer(String input, ByteBuffer outputBuffer) {
		Double valueAsDouble = Double.parseDouble(input);
		outputBuffer.putDouble(valueAsDouble);
		return valueAsDouble;
	}
	
	@Override
	public Double readTrueValue(byte[] bytes) {
		ByteBuffer wrapped = ByteBuffer.wrap(bytes);
		return wrapped.getDouble();
	}
	
	@Override
	public Double readIndexValue(byte[] bytes) {
		ByteBuffer wrapped = ByteBuffer.wrap(bytes);
		return wrapped.getDouble();
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

}
