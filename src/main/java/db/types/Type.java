package db.types;

import java.nio.ByteBuffer;

public abstract class Type {
	protected int size;
	
	public int getSize() {
		return size;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	abstract public void parse(String input, ByteBuffer outputBuffer);
	abstract public Object get(Byte[] bytes);

}
