package db.types;

import java.nio.ByteBuffer;

public class Decimal extends Type {

	public Decimal(int size) {
		this.size = size;
	}
	
	@Override
	public void parse(String input, ByteBuffer outputBuffer) {
		outputBuffer.putFloat(Float.parseFloat(input));
	}
	
	@Override
	public Decimal get(Byte[] bytes) {
		// TODO Auto-generated method stub
		return null;
	}

}
