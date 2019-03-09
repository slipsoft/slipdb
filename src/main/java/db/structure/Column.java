package db.structure;

import java.nio.ByteBuffer;

import db.data.DataType;
import zArchive.sj.simpleBD.parseCSV.SOptimDataFromCSV;
import zArchive.sj.simpleBD.parseCSV.SStorageDataType;

public class Column {
	
	protected String name = "Nom inconnu";
	protected DataType storedDataType;

	// Type de donnée stockée
	public final SStorageDataType dataType;// = StorageDataType.isUnknown;
	public final SOptimDataFromCSV optimDataType; // utile pour le cast d'une date -> int par exemple
	//public final boolean hasToIndexThisColumn; // Indexer cette colonne au chargement
	
	public Column(String argName, SOptimDataFromCSV argOptimDataType/*, boolean aHasToIndexThisColumn*/) { //StorageDataType argDataType, StorageDataType argDataTypeInCSV) {
		this.name = argName;
		this.dataType = argOptimDataType.realDataType;
		this.optimDataType = argOptimDataType;
		//hasToIndexThisColumn = aHasToIndexThisColumn; not useful anymore
	}
	
	
	/**
	 * Laissé pour rester compatibles avec les tests unitaires de Nicolas, mais non utilisé par Sylvain
	 * @param name
	 * @param type
	 * @param size
	 */
	public Column(String name, DataType type) {
		optimDataType = null;
		dataType = null;
		this.name = name;
		this.storedDataType = type;
		//hasToIndexThisColumn = false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DataType getDataType() {
		return storedDataType;
	}

	public void setDataType(DataType type) {
		this.storedDataType = type;
	}

	public int getSize() {
		return storedDataType.getSize();
	}
	
	public void parse(String input, ByteBuffer outputBuffer) {
		this.getDataType().parse(input, outputBuffer);
	}
}
