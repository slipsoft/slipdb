package db.types;

import java.nio.ByteBuffer;

import com.dant.utils.Utils;

public class DateTime extends Type {

	public DateTime() {
	}

	@Override
	public void parse(String input, ByteBuffer outputBuffer) {
		int dateAsInt = Utils.dateToSecInt(Utils.dateFromString(input));
		outputBuffer.putInt(dateAsInt);
	}
	
	@Override
	public DateTime get(Byte[] bytes) {
		// TODO Auto-generated method stub
		return null;
	}
}
