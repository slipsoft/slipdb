package db.data;

import java.nio.ByteBuffer;

import com.dant.utils.Utils;

public class DoubleType extends DataType {
	
	protected final static Operator[] compatibleOperatorsList = {
		Operator.equals,
		Operator.greater,
		Operator.less,
		Operator.greaterOrEquals,
		Operator.lessOrEquals,
	};
	
	public DoubleType(Utils argCurrentUtilsInstance) {
		super(argCurrentUtilsInstance);
		this.sizeInBytes = Double.BYTES;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Class getAssociatedClassType() {
		return Double.class;
	}
	
	@Override
	public void parse(String input, ByteBuffer outputBuffer) {
		outputBuffer.putDouble(Double.parseDouble(input));
	}
	
	@Override
	public Double getValueFromByteArray(byte[] bytes) {
		ByteBuffer wrapped = ByteBuffer.wrap(bytes);
		return wrapped.getDouble();
	}

}
