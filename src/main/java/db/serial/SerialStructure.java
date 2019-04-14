package db.serial;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.dant.utils.EasyFile;
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


	public static void saveStructure() {
		writeStructure();
	}
	public static void saveStructureTo(String filePath) {
		writeStructureTo(filePath);
	}
	public static void writeStructure() {
		writeStructureTo(serialSavePath);
	}
	
	public static void writeStructureTo(String filePath) {
		
		try {
			EasyFile createDirFile = new EasyFile(filePath);
			createDirFile.createFileIfNotExist();
			
			FileOutputStream fileOutputStream = new FileOutputStream(filePath);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(fileOutputStream));
			
			Database.getInstance().writeSerialData(objectOutputStream);
			
			objectOutputStream.close();
			
			Log.info("SerialStructure.writeStructure : OK !");
		} catch (Exception e) {
			Log.error("SerialStructure.writeStructure :  impossible de sauvegarder la structure du disque.");
			Log.error(e);
		}
		
	}
	
	
	
	public static void loadStructureFrom(String filePath) {
		try {
			FileInputStream fileInputStream = new FileInputStream(filePath);
			ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(fileInputStream));
			Database.getInstance().readSerialData(objectInputStream);
			objectInputStream.close();
	
			Log.info("SerialStructure.loadStructure : OK !");
		} catch (Exception e) {
			Log.error("SerialStructure.loadStructure :  impossible de charger la structure du disque.");
			Log.error(e);
		}
		
	}
	
	public static void loadStructure() {
		loadStructureFrom(serialSavePath);
	}
	
	
}
