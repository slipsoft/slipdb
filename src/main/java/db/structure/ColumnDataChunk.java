package db.structure;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.dant.utils.Log;

import db.data.types.ByteType;
import db.data.types.DataType;
import db.data.types.DateType;
import db.data.types.DoubleType;
import db.data.types.FloatType;
import db.data.types.IntegerType;
import db.data.types.LongType;
import db.data.types.StringType;

// Version simplifiée
// -> convertir tout ça en abstraction d'un ByteBuffer

public class ColumnDataChunk implements Serializable {
	private static final long serialVersionUID = -3035163829243851789L;
	
	// Stockage de la donnée à garder en mémoire ici
	// -> Il n'est pas possible d'utiliser l'héritage ici, il faut un truc qui prenne le moins de mémoire possible, donc pas des objets.
	
	// stocker des objets ralentir grandement le GC (garbage collector)
	// Un seul tableau stockant toutes les chaînes de caractères, pour ne pas avoir trop d'objets créés
	// et ne pas surcharger le GC
	private final ByteBuffer dataAsBytes;
	
	private int currentItemPosition = 0;
	//private int currentPositionInByteArray = 0;
	private final int maxNumberOfItems;
	private final int byteArrayLength;
	
	protected final DataType dataType;
	protected final int dataTypeSize; // taille en octets
	
	/** 
	 *  @param argDataType     type de la donnée sauvegardée
	 *  @param maxNumberOfItems  taille de l'allocation (ne sera pas ré-alloué, pour des raisons de performance)
	 */
	public ColumnDataChunk(DataType argDataType, int argMaxNumberOfItems) {
		dataType = argDataType;
		dataTypeSize = dataType.getSize();
		maxNumberOfItems = argMaxNumberOfItems;
		byteArrayLength = maxNumberOfItems * dataTypeSize;
		//valuesByteArray = new byte[byteArrayLength]; // données stockées sous forme d'un tableau d'octets
		dataAsBytes = ByteBuffer.allocate(byteArrayLength); // Il est manifestement plus rentable d'allouer dans la mémoire Heap niveau performances
		// à partir du moment où il n'y a que peu d'objets créés (exit donc les tableau de millions d'objets String)
		currentItemPosition = 0;
		//currentPositionInByteArray = 0;
	}
	
	/** 
	 *  @return true si un nouveau chunk est nécessaire
	 */
	private boolean incPosition() {
		currentItemPosition++;
		//currentPositionInByteArray += dataTypeSize;
		if (currentItemPosition >= maxNumberOfItems) return true;
		return false;
	}
	
	public boolean writeInt(int data) {
		dataAsBytes.putInt(data);
		return incPosition();
	}
	
	public boolean writeDate(int data) {
		return writeInt(data);
	}
	
	public boolean writeByte(byte data) {
		dataAsBytes.put(data);
		return incPosition();
	}
	
	public boolean writeLong(long data) {
		dataAsBytes.putLong(data);
		return incPosition();
	}
	
	public boolean writeDouble(double data) {
		dataAsBytes.putDouble(data);
		return incPosition();
	}
	
	public boolean writeFloat(float data) {
		dataAsBytes.putFloat(data);
		//Log.info("ColumnDataChunk.writeFloat : " + data);
		return incPosition();
	}
	
	/**
	 *  En entrée : le String de taille ajustée
	 * @param data  /!\ /!\ /!\ ici, le String doit avoir la bonne taille /!\ /!\ /!\
	 * @return
	 */
	public boolean writeString(String data) {
		byte[] strAsBytes = data.getBytes();
		dataAsBytes.put(strAsBytes);
		return incPosition();
	}
	
	
	public int getCurrentItemPosition() {
		return currentItemPosition;
	}
	
	/** Sans vérification sur la validité de l'index demandé
	 *  @param indexInChunk
	 */
	public String getString(int indexInChunk) {
		byte[] strAsBytes = new byte[dataTypeSize];
		dataAsBytes.get(strAsBytes);
		return new String(strAsBytes);
	}
	
	public byte[] getDataAsRawBytes(int indexInChunk) {
		byte[] thisDataAsRawBytes = new byte[dataTypeSize];
		dataAsBytes.position(indexInChunk);
		dataAsBytes.get(thisDataAsRawBytes);
		return thisDataAsRawBytes;
	}
	
	public int getInt(int indexInChunk) {
		return dataAsBytes.getInt(indexInChunk * dataTypeSize);
	}
	public int getDate(int indexInChunk) {
		return dataAsBytes.getInt(indexInChunk * dataTypeSize);
	}
	public byte getByte(int indexInChunk) {
		return dataAsBytes.get(indexInChunk * dataTypeSize);
	}
	public long getLong(int indexInChunk) {
		return dataAsBytes.getLong(indexInChunk * dataTypeSize);
	}
	public double getDouble(int indexInChunk) {
		return dataAsBytes.getDouble(indexInChunk * dataTypeSize);
	}
	public float getFloat(int indexInChunk) {
		return dataAsBytes.getFloat(indexInChunk * dataTypeSize);
	}
	
	/* peut-être supporté un jour
	public boolean writeByteArrayData(byte[] data) {
		???
		return incPosition();
	}*/
	
	
	
}
