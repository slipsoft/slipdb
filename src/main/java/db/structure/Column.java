package db.structure;

import java.nio.ByteBuffer;

import db.data.Type;
import zArchive.sj.simpleBD.parseCSV.SOptimDataFromCSV;
import zArchive.sj.simpleBD.parseCSV.SStorageDataType;

public class Column {
	
	protected String name = "Nom inconnu";
	protected Type type;

	// Type de donnée stockée
	public final SStorageDataType dataType;// = StorageDataType.isUnknown;
	public final SOptimDataFromCSV optimDataType; // utile pour le cast d'une date -> int par exemple
	public final boolean hasToIndexThisColumn; // Indexer cette colonne au chargement
	
	public Column(String argName, SOptimDataFromCSV argOptimDataType, boolean aHasToIndexThisColumn) { //StorageDataType argDataType, StorageDataType argDataTypeInCSV) {
		this.name = argName;
		this.dataType = argOptimDataType.realDataType;
		this.optimDataType = argOptimDataType;
		hasToIndexThisColumn = aHasToIndexThisColumn;
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
		hasToIndexThisColumn = false;
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
	
	public void parse(String input, ByteBuffer outputBuffer) {
		this.getType().parse(input, outputBuffer);
	}
}
