package db.structure;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dant.entity.ColumnEntity;
import db.data.DataType;

public class Column {

	protected String name = "Nom inconnu";
	protected DataType storedDataType;
	protected int number;
	
	protected Object minValue = null;
	protected Object maxValue = null;

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

	public Column setName(String name) {
		this.name = name;
		return this;
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
	
	public Object writeToBuffer(String input, ByteBuffer outputBuffer) {
		return this.getDataType().writeToBuffer(input, outputBuffer);
	}
	
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

	public Object getMinValue() {
		return minValue;
	}
	
	public Object getMaxValue() {
		return maxValue;
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
		int typeClassSize = this.dataType.getSize();
		String typeClassName = this.dataType.getClass().getName();
		String DataTypesClassPathPrefix = Database.getInstance().config.DataTypesClassPathPrefix;

		String type = Database.getInstance().config.DataTypes.entrySet().stream().filter(e -> (DataTypesClassPathPrefix + e.getValue()).equals(typeClassName)).map(Map.Entry::getKey).findFirst().get();
		return new ColumnEntity(this.name, type, typeClassSize);
	}
}
