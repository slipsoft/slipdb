package db.data.types;

import java.io.DataInput;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

import db.search.Operable;


public abstract class DataType implements Operable, Serializable {
	private static final long serialVersionUID = 4426175636422784230L;

	public static boolean sizeIsRequired; // nécessaire au bon fonctionnement de l'API, si set à true => public static int maxSizeInBytes = [max]; obligatoire
	
	
	
	protected int sizeInBytes;
	
	//c'est en fait inutile (merci Nicolas ;) ) : protected final Utils currentUtilsInstance; // doit être initialisé
	//protected Object currentValue;
	
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
	abstract public Object parseAndWriteToBuffer(String input, ByteBuffer outputBuffer) throws IllegalArgumentException;

	/**
	 * check if input can be parsed into Typed Data.
	 * @param input - raw string input
	 * @return boolean
	 */
	abstract public boolean inputCanBeParsed(String input);
	
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

	/** Lire directement d'un DataInput la valeur, et non plus à partir d'un tableau d'octets comme readIndexValue(byte[])
	 *  @param dataInput
	 *  @return
	 *  @throws IOException
	 */
	public Object readIndexValue(DataInput dataInput) throws IOException {
		byte[] dataAsByteArray = new byte[sizeInBytes];
		dataInput.readFully(dataAsByteArray); // renvoie une exception si les données n'ont pas pu être lues
		return readIndexValue(dataAsByteArray);		
	}

	public Object readIndexValue(DataInput dataInput, byte[] emptyExactSizeByteArray) throws IOException {
		dataInput.readFully(emptyExactSizeByteArray); // renvoie une exception si les données n'ont pas pu être lues
		return readIndexValue(emptyExactSizeByteArray);
	}
	
	protected byte[] readIndexValueAsByteArray(DataInput dataInput) throws IOException {

		/*if (dataInput instanceof InputStream) {
		    byte[] dataAsByteArray = new byte[sizeInBytes];
			int bytesRead = ((InputStream)dataInput).read(dataAsByteArray); // reads from the stream
			if (bytesRead == -1) // end of stream
				break;
		}*/
		
		byte[] dataAsByteArray = new byte[sizeInBytes];
		dataInput.readFully(dataAsByteArray); // renvoie une exception si les données n'ont pas pu être lues
		return dataAsByteArray;
	}

}
