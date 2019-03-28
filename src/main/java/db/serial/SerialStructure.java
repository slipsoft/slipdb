package db.serial;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import db.structure.Database;
import db.structure.Table;

/**
 * Ce code sera probablement à reprendre avec vous deux, Etienne et Nicolas,
 * Pour l'instant, le but est simple : pouvoir charger toutes les données du noeud 
 *	
 */
public class SerialStructure {
	
	public static final String serialSavePath = "data_save/serialStructureEverything.bin";
	
	// -> Je ne serialise que la liste des tables
	
	public static void writeStructure() throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(serialSavePath);
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(fileOutputStream));
		
		
		ArrayList<Table> tableList = Database.getInstance().getAllTables();
		for (Table cTable : tableList) {
			cTable.doBeforeSerialWrite(); // flush des arbres sur le disque pour ne pas perdre de donnée
		}
		objectOutputStream.writeObject(tableList);
		Database.getInstance().writeAdditionalSerialData(objectOutputStream);
		
		
		objectOutputStream.close();
		
	}
	
	public static void loadStructure() {
		
	}
	
	
}
