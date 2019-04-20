package db.structure;

import java.io.Serializable;
import java.util.Arrays;

import db.data.types.ByteType;
import db.data.types.DataType;
import db.data.types.DateType;
import db.data.types.DoubleType;
import db.data.types.FloatType;
import db.data.types.IntegerType;
import db.data.types.LongType;
import db.data.types.StringType;

public class ColumnDataChunk implements Serializable {
	private static final long serialVersionUID = -3035163829243851789L;
	
	// Stockage de la donnée à garder en mémoire ici
	// -> Il n'est pas possible d'utiliser l'héritage ici, il faut un truc qui prenne le moins de mémoire possible, donc pas des objets.
	
	public byte[] valuesArrayByte = null;
	public int[] valuesArrayInteger = null; // date, aussi
	public long[] valuesArrayLong = null;
	public float[] valuesArrayFloat = null;
	public double[] valuesArrayDouble = null;
	public String[] valuesArrayString = null; // stocker des objets ralentir grandement le GC (garbage collector)
	// Un seul tableau stockant toutes les chaînes de caractères, pour ne pas avoir trop d'objets créés
	// et ne pas surcharger le GC
	public byte[] valuesArrayStringAsBytes = null;
	//public int[] valuesArray = null;
	
	static public final boolean useByteStringStorage = true; // stocker les strings sous forme d'octets
	// -> Stocker sous forme d'octets est BEAUCOUP plus optimisé (que sous forme d'objets)
	
	private int currentItemPosition = 0;
	private final int allocationSize;
	
	protected final DataType dataType;
	protected final int dataTypeSize; // taille en octets
	
	/** 
	 *  @param argDataType     type de la donnée sauvegardée
	 *  @param allocationSize  taille de l'allocation (ne sera pas ré-alloué, pour des raisons de performance)
	 */
	public ColumnDataChunk(DataType argDataType, int argAllocationSize) {
		dataType = argDataType;
		dataTypeSize = dataType.getSize();
		allocationSize = argAllocationSize;
		
		if (dataType.getClass() == ByteType.class)    valuesArrayByte = new byte[allocationSize];
		if (dataType.getClass() == DateType.class)    valuesArrayInteger = new int[allocationSize];
		if (dataType.getClass() == IntegerType.class) valuesArrayInteger = new int[allocationSize];
		if (dataType.getClass() == LongType.class)    valuesArrayLong = new long[allocationSize];
		if (dataType.getClass() == FloatType.class)   valuesArrayFloat = new float[allocationSize];
		if (dataType.getClass() == DoubleType.class)  valuesArrayDouble = new double[allocationSize];
		if (dataType.getClass() == StringType.class) { // Type String
			if (useByteStringStorage) // ne pas utisier d'objets, juste des octets
				valuesArrayStringAsBytes = new byte[allocationSize * dataType.getSize()];
			else // utiliser des objets (pas opti)
				valuesArrayString = new String[allocationSize];
		}
		//if (dataType.getClass() == StringType.class)  valuesArrayString = new String[allocationSize];
		
		currentItemPosition = 0;
		
	}
	
	/** 
	 *  @return true si un nouveau chunk est nécessaire
	 */
	private boolean incPosition() {
		currentItemPosition++;
		if (currentItemPosition >= allocationSize) return true;
		return false;
	}
	
	public boolean writeIntData(int data) {
		valuesArrayInteger[currentItemPosition] = data;
		return incPosition();
	}
	
	public boolean writeDateData(int data) {
		valuesArrayInteger[currentItemPosition] = data;
		return incPosition();
	}
	
	public boolean writeByteData(byte data) {
		valuesArrayByte[currentItemPosition] = data;
		return incPosition();
	}
	
	public boolean writeLongData(long data) {
		valuesArrayLong[currentItemPosition] = data;
		return incPosition();
	}
	
	public boolean writeDoubleData(double data) {
		valuesArrayDouble[currentItemPosition] = data;
		return incPosition();
	}
	
	public boolean writeFloatData(float data) {
		valuesArrayFloat[currentItemPosition] = data;
		return incPosition();
	}
	
	// En entrée : le String de taille ajustée
	public boolean writeStringData(String data) {
		
		if (useByteStringStorage) { // ne pas utisier d'objets, juste des octets
			byte[] strAsBytes = data.getBytes();
			System.arraycopy(strAsBytes, 0, valuesArrayStringAsBytes, currentItemPosition * dataTypeSize, dataTypeSize);
		} else // utiliser des objets (pas opti)
			valuesArrayString[currentItemPosition] = data;
		return incPosition();
	}
	
	
	public int getCurrentItemPosition() {
		return currentItemPosition;
	}
	
	/** Sans vérification sur la validité de l'index demandé
	 *  @param indexInChunk
	 */
	public String getString(int indexInChunk) {
		if (useByteStringStorage) { // ne pas utisier d'objets, juste des octets
			byte[] strAsBytes = new byte[dataTypeSize];
			System.arraycopy(valuesArrayStringAsBytes, indexInChunk * dataTypeSize, strAsBytes, 0, dataTypeSize);
			return new String(strAsBytes);
		} else {
			return valuesArrayString[indexInChunk];
		}
	}
	
	/* devra être supporté (gestion des strings)
	public boolean writeByteArrayData(byte[] data) {
		valuesArrayFloat[currentSizePosition] = data;
		return incPosition();
	}*/
	
	
	
}
