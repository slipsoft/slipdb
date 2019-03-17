package db.structure;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import db.data.DataType;

public class Column {

	protected String name = "Nom inconnu";
	protected DataType dataType;
	protected List<Index> relatedIndicesList = new ArrayList<>();

	/**
	 * Columns contructor
	 *
	 * @param name
	 * @param dataType
	 * @param size
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
		this.getDataType().writeToBuffer(input, outputBuffer);
	}

	public void addIndex(Index index) {
		this.relatedIndicesList.add(index);
	}
}
