package db.types;

import java.nio.ByteBuffer;

public class Int extends Type {

	public Int(int size) {
		this.size = size;
	}
	
	@Override
	public void parse(String input, ByteBuffer outputBuffer) {
		outputBuffer.putInt(Integer.parseInt(input));
	}
	
	@Override
	public Int get(Byte[] bytes) {
		// TODO Auto-generated method stub
		return null;
	}

}
