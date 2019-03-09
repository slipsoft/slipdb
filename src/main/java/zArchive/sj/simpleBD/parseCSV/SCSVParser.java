package zArchive.sj.simpleBD.parseCSV;

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



public class SCSVParser {
	
	
	/** Pour le test de l'indexation, ici, faire l'indexation
	 *  des colonnes qui doivent être indexées via SIndexingTree
	 */
	
	public Table table;
	public int entrySize;
	
	public void initCSVTable() {
		
		// Calcul de la taille d'une entrée
		entrySize = 0;
		for (int columnIndex = 0; columnIndex < table.getColumns().size(); columnIndex++) {
			Column currentColumn = table.getColumns().get(columnIndex);
			entrySize += currentColumn.dataType.dataSize;
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
		if (valueList.length != table.getColumns().size()) {
			System.err.println("ERREUR AL_GlobalTest.processCSVLine : valueList.length("+valueList.length+") != testTable.columnList.size()("+table.getColumns().size()+")");
			return new byte[0];
		}
		
		// Conversion du trajet en tableau d'octets
		ByteBuffer tripAsByteBuffer = ByteBuffer.allocate(entrySize);
		//System.out.println("CSVParser.processCSVLine llocate = " + totalTripSize);
		
		//AL_LineMaker lineMaker = new AL_LineMaker();
		for (int columnIndex = 0; columnIndex < table.getColumns().size(); columnIndex++) {
			String strValue = valueList[columnIndex];
			Column currentColumn = table.getColumns().get(columnIndex);
			
			
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
				tripAsByteBuffer.put(SOptimDataFromCSV.convertFloatToByte(floatValue));
				break;
			case floatToShort :
				floatValue = Float.parseFloat(strValue);
				tripAsByteBuffer.putShort(SOptimDataFromCSV.convertFloatToShort(floatValue));
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
		this.table = table;
		initCSVTable(); // à remplacer par this.entrySize = table.getLineSize();
		
		Timer timer = new Timer("load time");
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
