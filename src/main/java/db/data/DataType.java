package db.data;

import java.nio.ByteBuffer;

import db.search.Operable;


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
	
	/**
	 * Convert String input into typed data, write it on the given buffer then returns it.
	 * @param input - raw string input
	 * @param outputBuffer
	 * @return converted data
	 * @throws IllegalArgumentException
	 */
	abstract public Object writeToBuffer(String input, ByteBuffer outputBuffer) throws IllegalArgumentException;
	
	/**
	 * Convert bytes input into typed data
	 * @param bytes
	 * @return converted data
	 */
	abstract public Object readTrueValue(byte[] bytes);
	
	/**
	 * Convert bytes input into typed data optimized for Indexes
	 * TODO integrate Key object in the future
	 * @param bytes
	 * @return
	 */
	abstract public Object readIndexValue(byte[] bytes);

}
