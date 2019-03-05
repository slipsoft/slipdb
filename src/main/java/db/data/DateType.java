package db.data;

import java.nio.ByteBuffer;
import java.util.Date;

import com.dant.utils.Utils;

public class DateType extends Type {
	
	protected final static Operator[] compatibleOperatorsList = {
		Operator.equals,
		Operator.greater,
		Operator.less,
		Operator.greaterOrEquals,
		Operator.lessOrEquals,
	};

	public DateType() {
		this.size = 4;
	}

	@Override
	public void parse(String input, ByteBuffer outputBuffer) {
		int dateAsInt = Utils.dateToSecInt(Utils.dateFromString(input));
		outputBuffer.putInt(dateAsInt);
	}
	
	@Override
	public Date get(byte[] bytes) {
		ByteBuffer wrapped = ByteBuffer.wrap(bytes);
		return Utils.dateFromSecInt(wrapped.getInt());
	}
}
