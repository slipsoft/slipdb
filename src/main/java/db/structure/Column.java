package db.structure;

import db.parseCSV.OptimDataFromCSV;
import db.parseCSV.StorageDataType;

public class Column {
	
	public String name = "Nom inconnu";
	// Type de donnée stockée
	public final StorageDataType dataType;// = StorageDataType.isUnknown;
	public final OptimDataFromCSV optimDataType; // utile pour le cast d'une date -> int par exemple
	
	//public ArrayList<Object> dataList = new ArrayList<Object>();
	
	public Column(String argName, OptimDataFromCSV argOptimDataType) { //StorageDataType argDataType, StorageDataType argDataTypeInCSV) {
		name = argName;
		dataType = argOptimDataType.realDataType;
		optimDataType = argOptimDataType;
	}
	
	
	
	
	
	protected String type;
	protected int size;
	
	/**
	 * Laissé pour rester compatibles avec les tests unitaires de Nicolas, mais non utilisé par Sylvain
	 * @param name
	 * @param type
	 * @param size
	 */
	public Column(String name, String type, int size) {
		optimDataType = null;
		dataType = null;
		this.name = name;
		this.type = type;
		this.size = size;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	
	
	
	
	
}
