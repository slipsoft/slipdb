package db.data;

import java.nio.ByteBuffer;

import com.dant.utils.Utils;

public class FloatType extends DataType {
	
	protected final static Operator[] compatibleOperatorsList = {
		Operator.equals,
		Operator.greater,
		Operator.less,
		Operator.greaterOrEquals,
		Operator.lessOrEquals,
	};
	
	public FloatType(Utils argCurrentUtilsInstance) {
		super(argCurrentUtilsInstance);
		this.sizeInBytes = Float.BYTES;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Class getAssociatedClassType() {
		return Float.class;
	}
	
	@Override
	public void parse(String input, ByteBuffer outputBuffer) {
		outputBuffer.putFloat(Float.parseFloat(input));
	}
	
	@Override
	public Float getValueFromByteArray(byte[] bytes) {
		ByteBuffer wrapped = ByteBuffer.wrap(bytes);
		return wrapped.getFloat();
	}

}
