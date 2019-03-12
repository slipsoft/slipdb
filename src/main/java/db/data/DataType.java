package db.data;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.ArrayUtils;

public abstract class DataType {
	protected int sizeInBytes;
	protected static Operator[] compatibleOperatorsList;
	
	public int getSize() {
		return sizeInBytes;
	}
	
	@SuppressWarnings("rawtypes")
	public abstract Class getAssociatedClassType();
	
	public void setSize(int argSizeInBytes) {
		this.sizeInBytes = argSizeInBytes;
	}
	
	public boolean isOperatorCompatible(Operator op) {
		return ArrayUtils.contains(compatibleOperatorsList, op);
	}
	
	abstract public void parse(String input, ByteBuffer outputBuffer);
	abstract public Object getValueFromByteArray(byte[] bytes);

}
