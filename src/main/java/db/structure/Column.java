package db.structure;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.dant.entity.ColumnEntity;
import com.dant.utils.EasyFile;
import com.dant.utils.Log;

import db.data.types.ByteType;
import db.data.types.DataType;
import db.data.types.DateType;
import db.data.types.DoubleType;
import db.data.types.FloatType;
import db.data.types.IntegerType;
import db.data.types.LongType;
import db.data.types.StringType;

public class Column implements Serializable {
	private static final long serialVersionUID = -3714630692284399540L;
	
	protected String name = "Nom inconnu";
	protected int number;
	
	
	protected Object minValue = null;
	protected Object maxValue = null;
	protected transient Object minMaxLock = new Object();
	protected DataType dataType;
	@Deprecated /* @CurrentlyUnused */ transient protected List<Index> relatedIndexesList = new ArrayList<>();
	//inutile désormais -> private transient Object writeInMemoryLock = new Object();

	protected DataType storedDataType; // <- type de la donnée stockée
	// Stockage de la donnée à garder en mémoire ici
	// -> Il n'est pas possible d'utiliser l'héritage ici, il faut un truc qui prenne le moins de mémoire possible, donc pas des objets.
	protected ArrayList<ColumnDataChunk> a2DataChunk = new ArrayList<ColumnDataChunk>();
	// Les données seront écrites dans la mémoire et/ou sur le disque. Version simple : un seul fichier par colonne
	transient protected EasyFile dataOnDiskFile;// = new EasyFile(); TODO
	public final boolean writeDataOnDisk;
	public final boolean keepDataInMemory;
	
	
	public final static int chunkDataTypeAllocationSize = 1_000_000;
	
	public int getTotalDataLength() {
		int totalSize = 0;
		for (ColumnDataChunk dataChunk : a2DataChunk) {
			totalSize += dataChunk.currentSizePosition;
		}
		return totalSize;
	}
	
	
	/** Pour la déserialisation
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		minMaxLock = new Object();
		relatedIndexesList = new ArrayList<>();
	}
	
	/*public NetBuffer columnAsNetBuffer() {
		
	}*/
	
	
	/**
	 * Columns contructor
	 *
	 * @param name
	 * @param dataType
	 */
	public Column(String name, DataType dataType) {
		this(name, dataType, true, true);
	}
	
	public Column(String name, DataType dataType, boolean argKeepDataInMemory) {
		this(name, dataType, argKeepDataInMemory, false);
	}
	/** 
	 *  @param name
	 *  @param dataType
	 *  @param argKeepDataInMemory  garder en mémoire les données ou juste les écrire sur le disque
	 *  @param argWriteDataOnDisk   écrire la donnée sur le disque
	 */
	public Column(String name, DataType dataType, boolean argKeepDataInMemory, boolean argWriteDataOnDisk) {
		this.name = name;
		this.dataType = dataType;
		this.keepDataInMemory = argKeepDataInMemory;//true;//
		this.writeDataOnDisk = argWriteDataOnDisk;//true;//
		
	}
	
	
	public String getName() {
		return name;
	}

	public Column setName(String name) {
		this.name = name;
		return this;
	}

	public int getNumber() {
		return number;
	}

	public Column setNumber(int number) {
		this.number = number;
		return this;
	}

	public DataType getDataType() {
		return dataType;
	}
	

	public Column setDataType(DataType dataType) {
		this.dataType = dataType;
		return this;
	}

	public int getSize() {
		return dataType.getSize();
	}
	
	public int getDataSize() {
		return dataType.getSize();
	}
	
	@Override
	public boolean equals(Object o) {
		Column column = (Column) o;
		return column.getName().equals(this.name);
	}
	
	/** Au passage, @Nicolas, le parseAndWriteToBuffer est spécifique au format string -> objet parsé et n'est pas si générique que ça... (cas de l'hexa par exemple)
	 * @param input
	 * @param outputBuffer
	 * @return
	 * @throws IllegalArgumentException
	 */
	public Object parseAndWriteToBuffer(String input, ByteBuffer outputBuffer) throws IllegalArgumentException {
		return this.getDataType().parseAndWriteToBuffer(input, outputBuffer);
	}
	
	/** NON thread-safe
	 *  @param value
	 */
	public void evaluateMinMax(Object value) {
		if (minValue == null) minValue = value;
		if (maxValue == null) maxValue = value;
		if (compareValues(value, maxValue) == 1) {
			maxValue = value;
		}
		if (compareValues(value, minValue) == -1) {
			minValue = value;
		}
	}

	public Object getMin() { synchronized(minMaxLock) {
		return minValue;
	} }
	
	public synchronized Object getMax() { synchronized(minMaxLock) {
		return maxValue;
	} }

	// Thread-safe (une fois le min-mas trouvé)
	public void pushMinMaxThreadSafe(Object argMinValue, Object argMaxValue) { synchronized(minMaxLock) {
		minValue = argMinValue;
		maxValue = argMaxValue;
	} }

	public Object getMinValue() {
		return minValue;
	}
	
	public Object getMaxValue() {
		return maxValue;
	}
	
	public static int compareValues(Object value1, Object value2) {
		if (value1 == null || value2 == null) return 0;
		if (value1.getClass() != value2.getClass()) return 0;
		
		if (value1 instanceof Number) {
			double asDouble1 = ((Number) value1).doubleValue(); // lent et pas opti ><"
			double asDouble2 = ((Number) value2).doubleValue(); // lent et pas opti ><"

			if (asDouble1 == asDouble2) return 0;
			if (asDouble1 > asDouble2) return 1;
			return -1;
		}
		
		if (value1 instanceof String) {
			String string1 = (String) value1;
			String string2 = (String) value2;
			int comparedValues = string1.compareTo(string2);
			if (comparedValues > 0) return 1;
			if (comparedValues < 0) return -1;
			return 0;
		}
		
		return 0;
		
		/*
		if (value1.getClass() == Float.class)   {
			if ((Float) value1 == (Float) value2) return 0;
			if ((Float) value1 > (Float) value2) return 1;
			return -1;
		}
		
		if (value1.getClass() == Double.class)   {
			if (((Double) value1) == (Double) value2) return 0;
			if ((Double) value1 > (Double) value2) return 1;
			return -1;
		}
		
		if (value1.getClass() == Double.class)   {
			if ((Double) value1 == (Double) value2) return 0;
			if ((Double) value1 > (Double) value2) return 1;
			return -1;
		}*/
		
	}
	
	
	@Deprecated public void addIndex(Index index) { // TODO
		this.relatedIndexesList.add(index);
	}

	public ColumnEntity convertToEntity() {
		int typeClassSize = this.dataType.getSize();
		String typeClassName = this.dataType.getClass().getName();
		String DataTypesClassPathPrefix = Database.getInstance().config.DataTypesClassPathPrefix;

		String type = Database.getInstance().config.DataTypes.entrySet().stream().filter(e -> (DataTypesClassPathPrefix + e.getValue()).equals(typeClassName)).map(Map.Entry::getKey).findFirst().get();
		return new ColumnEntity(this.name, type, typeClassSize);
	}
	
	
	public void writeDataInMemory(Object dataAsPrimitiveObject)  {
		//synchronized(writeInMemoryLock) <- RISQUE de perte de la cohrérence des données, le lock est mis dans Loader.writeInMemoryLock
		//Log.info("Write in memory !");
		if (a2DataChunk.size() == 0) {
			ColumnDataChunk newDataChunk = new ColumnDataChunk(dataType, chunkDataTypeAllocationSize);
			Log.info("Initialisation chunk");
			a2DataChunk.add(newDataChunk);
		}
		int dataChunkListSize = a2DataChunk.size();
		ColumnDataChunk dataChunk = a2DataChunk.get(dataChunkListSize - 1);
		
		/** TODO
		 * TODO
		 * TODO
		 * TODO
		 * TODO
		 * TODO
		 * Finir l'écriture de la donnée en mémoire vive
		 * Faire l'IndexMemDic (indexer une Column)
		 * Faire les recherches dans l'IndexMemDic
		 */
		boolean needAnotherChunk = false;
		if (dataAsPrimitiveObject.getClass() == Byte.class)
			needAnotherChunk = dataChunk.writeByteData( ((Byte)dataAsPrimitiveObject).byteValue() );
		
		if ((dataAsPrimitiveObject.getClass() == Integer.class))
			needAnotherChunk = dataChunk.writeIntData( ((Integer)dataAsPrimitiveObject).intValue() );
		
		if (dataAsPrimitiveObject.getClass() == Long.class)
			needAnotherChunk = dataChunk.writeLongData( ((Long)dataAsPrimitiveObject).longValue() );
		
		if (dataAsPrimitiveObject.getClass() == Float.class)
			needAnotherChunk = dataChunk.writeFloatData( ((Float)dataAsPrimitiveObject).floatValue() );
		
		if (dataAsPrimitiveObject.getClass() == Double.class)
			needAnotherChunk = dataChunk.writeDoubleData( ((Double)dataAsPrimitiveObject).doubleValue() );
		
		if (dataAsPrimitiveObject.getClass() == String.class)
			needAnotherChunk = dataChunk.writeStringData( ((String)dataAsPrimitiveObject) );
		
		if (needAnotherChunk) {
			Log.info("Nouveau chunk");
			ColumnDataChunk newDataChunk = new ColumnDataChunk(dataType, chunkDataTypeAllocationSize);
			a2DataChunk.add(newDataChunk);
		}
		
	}
	
}
