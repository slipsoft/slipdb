package db.structure;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.dant.entity.ColumnEntity;
import com.dant.utils.Log;
import db.data.DataType;

public class Column {

	protected String name = "Nom inconnu";
	protected DataType storedDataType;
	
	public Object minValue = null;
	public Object maxValue = null;

	protected DataType dataType;
	protected List<Index> relatedIndicesList = new ArrayList<>();

	/**
	 * Columns contructor
	 *
	 * @param name
	 * @param dataType
	 */
	public Column(String name, DataType dataType) {
		this.name = name;
		this.dataType = dataType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	public int getSize() {
		return dataType.getSize();
	}

	public void parse(String input, ByteBuffer outputBuffer) {
		this.getDataType()/*storedDataType*/.writeToBuffer(input, outputBuffer);
	}
	
	public Object parseAndReturnValue(String input, ByteBuffer outputBuffer) {
		return this.getDataType().writeToBufferAndReturnValue(input, outputBuffer);
	}
	
	
	public int compareValues(Object value1, Object value2) {
		if (value1 == null || value2 == null) return 0;
		if (value1.getClass() != value2.getClass()) return 0;
		
		if (value1 instanceof Number) {
			double asDouble1 = ((Number) value1).doubleValue(); // lent et pas opti ><"
			double asDouble2 = ((Number) value2).doubleValue(); // lent et pas opti ><"

			if (asDouble1 == asDouble2) return 0;
			if (asDouble1 > asDouble2) return 1;
			return -1;
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
	
	
	public void addIndex(Index index) {
		this.relatedIndicesList.add(index);
	}

	public ColumnEntity convertToEntity() {
		return new ColumnEntity(this.name, this.dataType.name);
	}
}
