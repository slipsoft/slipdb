package zArchive.sj.simpleDB.arrayList;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;

import com.dant.utils.Timer;



// C'est du vieux, code, je fais en sorte que le swarnings ne s'affichent plus
@SuppressWarnings("all") // bourrin, mais comme c'est du vieux code
public class AL_GlobalTest {
	
	/** Test de la base de données
	 * 	
	 */
	public void globalTestOffline() {
		
		//SimpleAL_DB dataBase = new SimpleAL_DB();
		AL_Table table = new AL_Table();
		table.addColumn("prix voyage", 0);
		table.addColumn("nom chauffeur", "");
		table.addColumn("code état", "");

		// Ajout d'une entrée
		AL_LineMaker lineMaker = new AL_LineMaker();
		lineMaker.addRowValue_int(10);
		lineMaker.addRowValue_str("Jean-Pierre");
		lineMaker.addRowValue_str("75A13");
		table.addValues(lineMaker.getArgumentList());

		// Ajout d'une entrée
		lineMaker = new AL_LineMaker();
		lineMaker.addRowValue_int(45);
		lineMaker.addRowValue_str("Claude");
		lineMaker.addRowValue_str("75A14");
		table.addValues(lineMaker.getArgumentList());

		// Ajout d'une entrée
		lineMaker = new AL_LineMaker();
		lineMaker.addRowValue_int(66);
		lineMaker.addRowValue_str("Bertrand");
		lineMaker.addRowValue_str("99B54");
		table.addValues(lineMaker.getArgumentList());

		// Ajout d'une entrée
		lineMaker = new AL_LineMaker();
		lineMaker.addRowValue_int(12);
		lineMaker.addRowValue_str("Bernard");
		lineMaker.addRowValue_str("87452Z");
		table.addValues(lineMaker.getArgumentList());

		// Ajout d'une entrée
		lineMaker = new AL_LineMaker();
		lineMaker.addRowValue_int(88);
		lineMaker.addRowValue_str("Bernard");
		lineMaker.addRowValue_str("78562SB");
		table.addValues(lineMaker.getArgumentList());
		
		System.out.println("AL_GlobalTest.globalTest : nbColonnes = " + table.columnList.size());
		
		
		AL_Finder finder = new AL_Finder();
		//finder.addFilter("prix voyage", AL_FinderArgumentOperation.equals, new Integer(12));
		finder.addFilter("nom chauffeur", AL_FinderArgumentOperation.equals, new String("Bernard"));
		
		ArrayList<Integer> indexList = finder.findMatchingIndexList(table, 2);
		
		System.out.println("AL_GlobalTest.globalTest : indexList.size() = " + indexList.size());
		
		
		/*
		table.addColumn_int("VendorID", 0); // A code indicating the TPEP provider that provided the record.
		table.addColumn_int("tpep_pickup_datetime", 0); // The date and time when the meter was engaged.
		table.addColumn_int("tpep_dropoff_datetime", 0); // The date and time when the meter was disengaged.
		table.addColumn_int("Passenger_count", 0);
		
		
		*/
		
	}
	
	public void loadFromCSV(String pathToCSV, int maxLineCount) {
		Timer timer = new Timer("load time");
		//AL_Table table = new AL_Table();
		initCSVTable();
		printMemUsage();
		//printMemUsage();
		
		// Chargement via le CSV
		//int maxLineCount = 100000;
		int currentLineCount = 0;
		
		try {
			FileReader fRead = new FileReader(pathToCSV);
			BufferedReader bRead = new BufferedReader(fRead);
			

			//PrintWriter writer = new PrintWriter("D:\\\\L3 DANT disque D\\\\SMALL_1_000_000_yellow_tripdata_2015-04.csv", "UTF-8");
			
			while (true) { // ce FAMEUX while(true) de la mort qui pue...
				String line = bRead.readLine();
				if (line == null) break;
				//writer.println(line);
				//System.out.println(line);
				if (currentLineCount != 0) {
					processCSVLine(line);
					//String[] valueList = line.split(",");
					//System.out.println(valueList.length);
				}
				currentLineCount++;
				if (currentLineCount >= maxLineCount) break;
			}
			bRead.close();
			//writer.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (Exception e) {
			System.out.println(e);
		}


		printMemUsage();
		timer.log();
	}
	
	public void globalTestWithCSV(String pathToCSV) {
		
		//Chargement à partir du fichier CSV
		loadFromCSV(pathToCSV, 100_000);
		
		// Recherche d'une distance et d'un nombre de passagers
		AL_Finder finder = new AL_Finder();
		finder.addFilter("trip_distance", AL_FinderArgumentOperation.equals, new Integer(12));
		finder.addFilter("passenger_count", AL_FinderArgumentOperation.equals, new Integer(6));
		ArrayList<Integer> indexList = finder.findMatchingIndexList(testTable, 1000);
		
		
		// Affichage des résultats
		System.out.println("AL_GlobalTest.globalTest : indexList.size() = " + indexList.size());
		for (int resultIndex = 0; resultIndex < indexList.size(); resultIndex++) {
			System.out.println("RESULTAT n°" + resultIndex + " :");
			int rowIndex = indexList.get(resultIndex);
			System.out.println(testTable.rowAsReadableString(rowIndex));
		}
		printMemUsage();
		
	}
	
	public AL_Table testTable = new AL_Table();
	
	public void initCSVTable() {
		
		// Tables et type des tables
		testTable.addColumn("VendorID", 0);
		testTable.addColumn("tpep_pickup_datetime", "");
		testTable.addColumn("tpep_dropoff_datetime", "");
		testTable.addColumn("passenger_count", 0);
		testTable.addColumn("trip_distance", 0);
		testTable.addColumn("pickup_longitude", "");
		testTable.addColumn("pickup_latitude", "");
		testTable.addColumn("RateCodeID", 0);
		testTable.addColumn("store_and_fwd_flag", "");
		testTable.addColumn("dropoff_longitude", "");
		testTable.addColumn("dropoff_latitude", "");
		testTable.addColumn("payment_type", 0);
		testTable.addColumn("fare_amount", 0);
		testTable.addColumn("extra", 0);
		testTable.addColumn("mta_tax", 0);
		testTable.addColumn("tip_amount", 0);
		testTable.addColumn("tolls_amount", 0);
		testTable.addColumn("improvement_surcharge", 0);
		testTable.addColumn("total_amount", 0);
		
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
	
	public void processCSVLine(String csvLine) {
		String[] valueList = csvLine.split(",");
		if (valueList.length != testTable.columnList.size()) {
			System.err.println("ERREUR AL_GlobalTest.processCSVLine : valueList.length("+valueList.length+") != testTable.columnList.size()("+testTable.columnList.size()+")");
			return;
		}
		

		AL_LineMaker lineMaker = new AL_LineMaker();
		for (int columnIndex = 0; columnIndex < testTable.columnList.size(); columnIndex++) {
			String strValue = valueList[columnIndex];
			AL_Column correntColumn = testTable.columnList.get(columnIndex);
			
			switch (correntColumn.storageType) {
			case isInteger :
				// Cast de la valeur en entier
				double doubleValue = Double.parseDouble(strValue);
				int intValue = (int) doubleValue;
				lineMaker.addRowValue_int(intValue);
				break;
			case isString :
				lineMaker.addRowValue_str(strValue);
				break;
			}
		}
		testTable.addValues(lineMaker.getArgumentList());
		//System.out.println("AL_GlobalTest.globalTest : indexList.size() = " + indexList.size());
		
		//System.out.println(valueList.length);
	}
	
	public void printMemUsage() {
		Runtime runtime = Runtime.getRuntime();

		NumberFormat format = NumberFormat.getInstance();

		StringBuilder sb = new StringBuilder();
		long maxMemory = runtime.maxMemory();
		long allocatedMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		
		
		long usedMemory = allocatedMemory - freeMemory;
		
		System.out.println("Used Memory = " + format.format(usedMemory / 1024));
		
		/*
		System.out.println("free memory: " + format.format(freeMemory / 1024));
		System.out.println("allocated memory: " + format.format(allocatedMemory / 1024));
		System.out.println("max memory: " + format.format(maxMemory / 1024));
		System.out.println("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024));*/
		
		
		/*
		sb.append("free memory: " + format.format(freeMemory / 1024) + "<br/>");
		sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + "<br/>");
		sb.append("max memory: " + format.format(maxMemory / 1024) + "<br/>");
		sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "<br/>");
		*/
		
		
		
	}
	
}












