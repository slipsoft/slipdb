package db.structure;

import java.io.Serializable;

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
	//public byte[] valuesArrayStringAsBytes = null;
	//public int[] valuesArray = null;
	
	public int currentSizePosition = 0;
	public final int allocationSize;
	
	protected DataType dataType;
	
	/** 
	 *  @param argDataType     type de la donnée sauvegardée
	 *  @param allocationSize  taille de l'allocation (ne sera pas ré-alloué, pour des raisons de performance)
	 */
	public ColumnDataChunk(DataType argDataType, int argAllocationSize) {
		dataType = argDataType;
		allocationSize = argAllocationSize;
		
		if (dataType.getClass() == ByteType.class)    valuesArrayByte = new byte[allocationSize];
		if (dataType.getClass() == DateType.class)    valuesArrayInteger = new int[allocationSize];
		if (dataType.getClass() == IntegerType.class) valuesArrayInteger = new int[allocationSize];
		if (dataType.getClass() == LongType.class)    valuesArrayLong = new long[allocationSize];
		if (dataType.getClass() == FloatType.class)   valuesArrayFloat = new float[allocationSize];
		if (dataType.getClass() == DoubleType.class)  valuesArrayDouble = new double[allocationSize];
		if (dataType.getClass() == StringType.class)  valuesArrayString = new String[allocationSize];
		
		currentSizePosition = 0;
		
	}
	
	/** 
	 *  @return true si un nouveau chunk est nécessaire
	 */
	private boolean incPosition() {
		currentSizePosition++;
		if (currentSizePosition >= allocationSize) return true;
		return false;
	}
	
	public boolean writeIntData(int data) {
		valuesArrayInteger[currentSizePosition] = data;
		return incPosition();
	}
	
	public boolean writeDateData(int data) {
		valuesArrayInteger[currentSizePosition] = data;
		return incPosition();
	}
	
	public boolean writeByteData(byte data) {
		valuesArrayByte[currentSizePosition] = data;
		return incPosition();
	}
	
	public boolean writeLongData(long data) {
		valuesArrayLong[currentSizePosition] = data;
		return incPosition();
	}
	
	public boolean writeDoubleData(double data) {
		valuesArrayDouble[currentSizePosition] = data;
		return incPosition();
	}
	
	public boolean writeFloatData(float data) {
		valuesArrayFloat[currentSizePosition] = data;
		return incPosition();
	}
	
	public boolean writeStringData(String data) {
		valuesArrayString[currentSizePosition] = data;
		return incPosition();
	}
	/* devra être supporté (gestion des strings)
	public boolean writeByteArrayData(byte[] data) {
		valuesArrayFloat[currentSizePosition] = data;
		return incPosition();
	}*/
	
	
	
}
