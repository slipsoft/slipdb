package db.data;

import java.nio.ByteBuffer;
import java.util.Date;

import com.dant.utils.Utils;

public class DateType extends DataType {
	
	protected final static Operator[] compatibleOperatorsList = {
		Operator.equals,
		Operator.greater,
		Operator.less,
		Operator.greaterOrEquals,
		Operator.lessOrEquals,
	};

	public DateType() {
		this.sizeInBytes = 4;
	}

	@Override
	public void parse(String input, ByteBuffer outputBuffer) {
		int dateAsInt = Utils.dateToSecInt(Utils.dateFromString(input));
		outputBuffer.putInt(dateAsInt);
	}
	
	@Override
	// Date -> Integer, for it to be indexed faster, and the same way Integers are (that's really convenient, see IndexTree for more)
	public Integer getValueFromByteArray(byte[] bytes) {
		ByteBuffer wrapped = ByteBuffer.wrap(bytes);
		return wrapped.getInt();
		//int dateAsInt = wrapped.getInt(); // converts the byte array into an int
		//return Utils.dateFromSecInt(dateAsInt);
	}
}
