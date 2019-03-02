package db.structure;

import db.parseCSV.OptimDataFromCSV;
import db.parseCSV.StorageDataType;
import db.types.Type;

public class Column {
	
	protected String name = "Nom inconnu";
	protected Type type;

	// Type de donnée stockée
	public final StorageDataType dataType;// = StorageDataType.isUnknown;
	public final OptimDataFromCSV optimDataType; // utile pour le cast d'une date -> int par exemple
	
	public Column(String argName, OptimDataFromCSV argOptimDataType) { //StorageDataType argDataType, StorageDataType argDataTypeInCSV) {
		this.name = argName;
		this.dataType = argOptimDataType.realDataType;
		this.optimDataType = argOptimDataType;
		
	}
	
	/**
	 * Laissé pour rester compatibles avec les tests unitaires de Nicolas, mais non utilisé par Sylvain
	 * @param name
	 * @param type
	 * @param size
	 */
	public Column(String name, Type type) {
		optimDataType = null;
		dataType = null;
		this.name = name;
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public int getSize() {
		return type.getSize();
	}
}
