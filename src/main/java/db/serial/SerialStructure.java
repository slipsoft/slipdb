package db.serial;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.dant.utils.Log;

import db.structure.Database;

/**
 * Ce code sera probablement à reprendre avec vous deux, Etienne et Nicolas,
 * Pour l'instant, le but est simple : pouvoir charger les données
 * 
 * ATTENTION :
 * 
 *	
 */
public class SerialStructure {
	
	public static final String serialSavePath = "data_save/serialStructureEverything.bin";
	
	// -> Je ne serialise que la liste des tables

	
	public static void saveStructure() throws IOException {
		writeStructure();
	}
	public static void writeStructure() throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(serialSavePath);
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(fileOutputStream));
		
		Database.getInstance().writeSerialData(objectOutputStream);
		
		objectOutputStream.close();
		
		Log.info("SerialStructure.writeStructure : OK !");
		
	}
	
	public static void loadStructure() throws IOException, ClassNotFoundException {
		FileInputStream fileInputStream = new FileInputStream(serialSavePath);
		ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(fileInputStream));
		Database.getInstance().readSerialData(objectInputStream);
		objectInputStream.close();

		Log.info("SerialStructure.loadStructure : OK !");
		
	}
	
	
}
