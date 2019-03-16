package db.data;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.ArrayUtils;

public abstract class DataType {
	protected int sizeInBytes;
	protected static Operator[] compatibleOperatorsList;
	//c'est en fait inutile (merci Nicolas ;) ) : protected final Utils currentUtilsInstance; // doit être initialisé
	
	protected Object currentValue;
	
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
	
	abstract public void parse(String input, ByteBuffer outputBuffer);
	abstract public Object parseAndReturnValue(String input, ByteBuffer outputBuffer);
	abstract public Object getValueFromByteArray(byte[] bytes);

}
