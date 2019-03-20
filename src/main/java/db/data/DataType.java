package db.data;

import java.nio.ByteBuffer;


public abstract class DataType implements Operable {

	public static boolean sizeIsRequired; // nécessaire au bon fonctionnement de l'API, si set à true => public static int maxSizeInBytes = [max]; obligatoire

	protected int sizeInBytes;

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
	
	abstract public Object writeToBufferAndReturnValue(String input, ByteBuffer outputBuffer);
	abstract public void writeToBuffer(String input, ByteBuffer outputBuffer);
	
	abstract public Object readTrueValue(byte[] bytes);
	abstract public Object readIndexValue(byte[] bytes); // return Key object in the future

}
