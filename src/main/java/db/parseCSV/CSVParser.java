package db.parseCSV;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.ByteBuffer;

import com.dant.utils.MemUsage;
import com.dant.utils.Timer;
import com.dant.utils.Utils;

import db.structure.Column;
import db.structure.Table;



public class CSVParser {
	
	
	public Table testTable;
	public int totalTripSize;
	
	public void initCSVTable() {
		
		// Tables et type des tables
		/*
		testTable.addColumn("VendorID", StorageDataType.isByte);
		testTable.addColumn("tpep_pickup_datetime", StorageDataType.isInteger, StorageDataType.isStringDate);
		testTable.addColumn("tpep_dropoff_datetime", StorageDataType.isInteger, StorageDataType.isStringDate);
		testTable.addColumn("passenger_count", StorageDataType.isByte);
		testTable.addColumn("trip_distance", StorageDataType.isFloat);
		testTable.addColumn("pickup_longitude", StorageDataType.isDouble);
		testTable.addColumn("pickup_latitude", StorageDataType.isDouble);
		testTable.addColumn("RateCodeID", StorageDataType.isByte);
		testTable.addColumn("store_and_fwd_flag", StorageDataType.isChar); // 1 caractère
		testTable.addColumn("dropoff_longitude", StorageDataType.isDouble);
		testTable.addColumn("dropoff_latitude", StorageDataType.isDouble);
		testTable.addColumn("payment_type", StorageDataType.isByte);
		testTable.addColumn("fare_amount", StorageDataType.isFloat);
		testTable.addColumn("extra", StorageDataType.isFloat);
		testTable.addColumn("mta_tax", StorageDataType.isFloat);
		testTable.addColumn("tip_amount", StorageDataType.isFloat);
		testTable.addColumn("tolls_amount", StorageDataType.isFloat);
		testTable.addColumn("improvement_surcharge", StorageDataType.isFloat);
		testTable.addColumn("total_amount", StorageDataType.isFloat);
		*/
		
		// Optimisation spéciale pour ce type de base de donnée (trajets de NY)
		
		testTable.addColumn("VendorID", OptimDataFromCSV.toByte);
		testTable.addColumn("tpep_pickup_datetime", OptimDataFromCSV.dateStringToInteger);
		testTable.addColumn("tpep_dropoff_datetime", OptimDataFromCSV.dateStringToInteger);
		testTable.addColumn("passenger_count", OptimDataFromCSV.toByte);
		testTable.addColumn("trip_distance", OptimDataFromCSV.floatToShort);
		testTable.addColumn("pickup_longitude", OptimDataFromCSV.toDouble);
		testTable.addColumn("pickup_latitude", OptimDataFromCSV.toDouble);
		testTable.addColumn("RateCodeID", OptimDataFromCSV.toByte);
		testTable.addColumn("store_and_fwd_flag", OptimDataFromCSV.toChar); // 1 caractère
		testTable.addColumn("dropoff_longitude", OptimDataFromCSV.toDouble);
		testTable.addColumn("dropoff_latitude", OptimDataFromCSV.toDouble);
		testTable.addColumn("payment_type", OptimDataFromCSV.toByte);
		testTable.addColumn("fare_amount", OptimDataFromCSV.floatToShort);
		testTable.addColumn("extra", OptimDataFromCSV.floatToByte);
		testTable.addColumn("mta_tax", OptimDataFromCSV.floatToByte);
		testTable.addColumn("tip_amount", OptimDataFromCSV.floatToShort);
		testTable.addColumn("tolls_amount", OptimDataFromCSV.floatToShort);
		testTable.addColumn("improvement_surcharge", OptimDataFromCSV.floatToByte);
		testTable.addColumn("total_amount", OptimDataFromCSV.floatToShort);
		
		
		// Calcul de la taille d'un trajet
		totalTripSize = 0;
		for (int columnIndex = 0; columnIndex < testTable.getColumns().size(); columnIndex++) {
			Column currentColumn = testTable.getColumns().get(columnIndex);
			totalTripSize += currentColumn.dataType.dataSize;
		}
		
		/*VendorID,
		tpep_pickup_datetime,
		tpep_dropoff_datetime,
		passenger_count,
		trip_distance,
		pickup_longitude,
		pickup_latitude,
		RateCodeID,
		store_and_fwd_flag,
		dropoff_longitude,
		dropoff_latitude,
		payment_type,
		fare_amount,
		extra,
		mta_tax,
		tip_amount,
		tolls_amount,
		improvement_surcharge,
		total_amount*/
	}
	
	public byte[] processCSVLine(String csvLine) {
		String[] valueList = csvLine.split(",");
		if (valueList.length != testTable.getColumns().size()) {
			System.err.println("ERREUR AL_GlobalTest.processCSVLine : valueList.length("+valueList.length+") != testTable.columnList.size()("+testTable.getColumns().size()+")");
			return new byte[0];
		}
		
		// Conversion du trajet en tableau d'octets
		ByteBuffer tripAsByteBuffer = ByteBuffer.allocate(totalTripSize);
		//System.out.println("CSVParser.processCSVLine llocate = " + totalTripSize);
		
		//AL_LineMaker lineMaker = new AL_LineMaker();
		for (int columnIndex = 0; columnIndex < testTable.getColumns().size(); columnIndex++) {
			String strValue = valueList[columnIndex];
			Column currentColumn = testTable.getColumns().get(columnIndex);
			
			
			float floatValue;
			// Cast de la valeur en entier
			switch (currentColumn.optimDataType) {
			case toByte :
				tripAsByteBuffer.put(Byte.parseByte(strValue));
				break;
			case toChar :
				tripAsByteBuffer.putChar(strValue.charAt(0));
				break;
			case toInteger :
				/*if (currentColumn.dataTypeInCSV == StorageDataType.isStringDate) {
					int dateAsInt = Utils.dateToSecInt(Utils.dateFromString(strValue));
					tripAsByteBuffer.putInt(dateAsInt);
				} else*/
				tripAsByteBuffer.putInt(Integer.parseInt(strValue));
				break;

			case dateStringToInteger :
				int dateAsInt = Utils.dateToSecInt(Utils.dateFromString(strValue));
				tripAsByteBuffer.putInt(dateAsInt);
				break;
			case toLong :
				tripAsByteBuffer.putLong(Long.parseLong(strValue));
				break;
			case toFloat :
				tripAsByteBuffer.putFloat(Float.parseFloat(strValue));
				break;
			case toDouble :
				tripAsByteBuffer.putDouble(Double.parseDouble(strValue));
				break;
			case floatToByte :
				floatValue = Float.parseFloat(strValue);
				tripAsByteBuffer.put(OptimDataFromCSV.convertFloatToByte(floatValue));
				break;
			case floatToShort :
				floatValue = Float.parseFloat(strValue);
				tripAsByteBuffer.putShort(OptimDataFromCSV.convertFloatToShort(floatValue));
				break;
			//case isString : // pas de prise en charge des strings pour le moment
			//	break;
			//case isUnknown :
			//	break;
			//case isStringDate :
			//	break;
			//default :
			//	break;
			}
		}
		//System.out.println("CSVParser.processCSVLine OK !!");
		
		return tripAsByteBuffer.array();
		//System.out.println("AL_GlobalTest.globalTest : indexList.size() = " + indexList.size());
		
		//System.out.println(valueList.length);
	}
	
	// Charger la donnée depuis le fichier CSV
	public void loadFromCSV(String pathToCSV, String savePath, int maxLineCount, Table table) {
		testTable = table;
		
		Timer timer = new Timer("load time");
		//AL_Table table = new AL_Table();
		initCSVTable();
		MemUsage.printMemUsage();
		
		//printMemUsage();
		
		// Chargement via le CSV
		//int maxLineCount = 100000;
		int currentLineCount = 0;
		
		try {
			FileReader fRead = new FileReader(pathToCSV);
			BufferedReader bRead = new BufferedReader(fRead);
			
			DataOutputStream bWrite = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(savePath)));
			
			
			//PrintWriter writer = new PrintWriter("D:\\\\L3 DANT disque D\\\\SMALL_1_000_000_yellow_tripdata_2015-04.csv", "UTF-8");
			
			while (true) { // ce FAMEUX while(true) de la mort qui pue...
				String line = bRead.readLine();
				
				if (line == null) break;
				//writer.println(line);
				//System.out.println(line);
				if (currentLineCount != 0) {
					byte[] lineAsByteArray = processCSVLine(line);
					bWrite.write(lineAsByteArray);
					//String[] valueList = line.split(",");
					//System.out.println(valueList.length);
				}
				currentLineCount++;
				if (currentLineCount >= maxLineCount) break;
			}
			bRead.close();
			bWrite.close();
			//writer.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (Exception e) {
			System.out.println(e);
		}
		
		
		MemUsage.printMemUsage();
		timer.printms();
	}
}
