package db.data;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.ArrayUtils;


public abstract class DataType {
	public String name;
	protected int sizeInBytes;
	protected static Operator[] compatibleOperatorsList;
	//c'est en fait inutile (merci Nicolas ;) ) : protected final Utils currentUtilsInstance; // doit être initialisé
	
	protected Object currentValue;
	
	//@SuppressWarnings("rawtypes") protected Class associatedIndexClassType;
	
	public int getSize() {
		return sizeInBytes;
	}
	
	public DataType() {
	}
	
	@SuppressWarnings("rawtypes")
	public abstract Class getAssociatedClassType();
	
	public void setSize(int argSizeInBytes) {
		this.sizeInBytes = argSizeInBytes;
	}
	
	public boolean isOperatorCompatible(Operator op) {
		return ArrayUtils.contains(compatibleOperatorsList, op);
	}
	
	abstract public Object writeToBufferAndReturnValue(String input, ByteBuffer outputBuffer);
	abstract public void writeToBuffer(String input, ByteBuffer outputBuffer);
	
	abstract public Object readTrueValue(byte[] bytes);
	abstract public Object readIndexValue(byte[] bytes); // return Key object in the future

}
