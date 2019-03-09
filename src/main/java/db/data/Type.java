package db.data;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.ArrayUtils;

public abstract class Type {
	protected int size;
	protected static Operator[] compatibleOperatorsList;
	
	public int getSize() {
		return size;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public boolean isOperatorCompatible(Operator op) {
		return ArrayUtils.contains(compatibleOperatorsList, op);
	}
	
	abstract public void parse(String input, ByteBuffer outputBuffer);
	abstract public Object getValueFromByteArray(byte[] bytes);

}
