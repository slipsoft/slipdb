package sj.demoSimple;


import java.io.IOException;

import com.dant.utils.Log;
import com.dant.utils.Timer;

import db.data.types.ByteType;
import db.data.types.DataPositionList;
import db.data.types.DateType;
import db.data.types.DoubleType;
import db.data.types.FloatType;
import db.data.types.StringType;
import db.search.ResultSet;
import db.data.load.Loader;
import db.serial.SerialStructure;
import db.structure.Database;
import db.structure.Table;
import db.structure.recherches.TableHandler;

/**
 *  Créer et parser la table, chargement initial
 *  
 *  
 */
public class SimpleTCPDemoCreateTable {

	static protected Table table;
	//protected Utils currentlyUsedUils = new Utils(); // For thread-safety ! (but, here, it's static so thread unsafe... ^^')
	static protected TableHandler tableHandler;
	
	static protected String databaseName = null;// = "Database4";
	
	static protected String serializeFilePath = null;// = "data_save/SimpleTCPDemo_serialData_" + databaseName + ".bin";
	
	static protected String tableName = null;
	
	static public void setDatabaseName00(String argDatabaseName) {
		if ("".equals(argDatabaseName)) {
			databaseName = argDatabaseName;
			serializeFilePath = "data_save/SimpleTCPDemo_serialData.bin";
			tableName = "NYBigTest";
		} else {
			databaseName = argDatabaseName;
			serializeFilePath = "data_save/SimpleTCPDemo_serialData_" + databaseName + ".bin";
			tableName = databaseName + "_NYBigTest";
		}
	}
	
	static public void initTable01() throws Exception {
		Log.info("SimpleTCPDemoCreateTable.initTable01");
		//Log.start("indexingTreeTest", 2);
		
		tableHandler = new TableHandler(tableName);
		
		//getValuesOfLineByIdForSignleQuery
		
		
		// Modèle 2015
		tableHandler.addColumn("VendorID",                 new ByteType(), false, false);
		// -> On a bien les mêmes résultats en castant la date et en la traîtant comme une string
		tableHandler.addColumn("tpep_pickup_datetime",     new DateType(), false, false); //new StringType(19));//
		tableHandler.addColumn("tpep_dropoff_datetime",    new DateType(), false, false);   //new StringType(19)); // 
		tableHandler.addColumn("passenger_count",          new ByteType(), false, false);
		tableHandler.addColumn("trip_distance",            new FloatType(), true, false);
		tableHandler.addColumn("pickup_longitude",       new DoubleType(), false, false);
		tableHandler.addColumn("pickup_latitude",        new DoubleType(), false, false);
		tableHandler.addColumn("RateCodeID",               new ByteType(), false, false);
		tableHandler.addColumn("store_and_fwd_flag",    new StringType(1), false, false);
		tableHandler.addColumn("dropoff_longitude",      new DoubleType(), false, false);
		tableHandler.addColumn("dropoff_latitude",       new DoubleType(), false, false);
		tableHandler.addColumn("payment_type",             new ByteType(), false, false);
		tableHandler.addColumn("fare_amount",             new FloatType(), false, false);
		tableHandler.addColumn("extra",                   new FloatType(), false, false);
		tableHandler.addColumn("mta_tax",                 new FloatType(), false, false);
		tableHandler.addColumn("tip_amount",              new FloatType(), false, false);
		tableHandler.addColumn("tolls_amount",            new FloatType(), false, false);
		tableHandler.addColumn("improvement_surcharge",   new FloatType(), false, false);
		tableHandler.addColumn("total_amount",            new FloatType(), false, false);
		
		// 2014 : 
		
		table = tableHandler.createTable();
		
		tableHandler.clearDataDirectory();

		/*tableHandler.createRuntimeIndexingColumn("VendorID");
		
		tableHandler.createRuntimeIndexingColumn("trip_distance");*/
		Database.getInstance().getAllTables().add(table);
		SerialStructure.writeStructureTo(serializeFilePath);
		
	}
	
	static public void parseCsvData02() throws IOException, Exception { // non  synchronized
		
		tableHandler.createRuntimeIndexingColumn("trip_distance");
		
		/*
		int lastColIndex = table.getColumns().size();
		for (int colIndex = 0; colIndex < lastColIndex; colIndex++) {
			tableHandler.createRuntimeIndexingColumn(colIndex);
		}*/
		
		String basePath = "E:/L3 DANT disque E/csv/"; // "D:/csv/"; //
		
		
		//tableHandler.multiThreadParsingAddAndStartCsv("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);
		//tableHandler.multiThreadParsingAddAndStartCsv("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);
		/*tableHandler.multiThreadParsingAddAndStartCsv("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);
		tableHandler.multiThreadParsingAddAndStartCsv("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);
		*/
		
		//tableHandler.parseCsvData(basePath + "yellow_tripdata_2015-01.csv", true);
		
		//tableHandler.multiThreadParsingAddAndStartCsv(basePath + "yellow_tripdata_2015-01.csv", true);
		//tableHandler.multiThreadParsingAddAndStartCsv(basePath + "yellow_tripdata_2015-02.csv", true);
		//tableHandler.multiThreadParsingAddAndStartCsv(basePath + "yellow_tripdata_2015-03.csv", true);
		//tableHandler.multiThreadParsingAddAndStartCsv(basePath + "yellow_tripdata_2015-04.csv", true);
		//tableHandler.multiThreadParsingAddAndStartCsv(basePath + "yellow_tripdata_2015-05.csv", true);
		//tableHandler.multiThreadParsingAddAndStartCsv(basePath + "yellow_tripdata_2015-06.csv", true);
		/*tableHandler.multiThreadParsingAddAndStartCsv(basePath + "yellow_tripdata_2015-07.csv", true);
		tableHandler.multiThreadParsingAddAndStartCsv(basePath + "yellow_tripdata_2015-08.csv", true);
		tableHandler.multiThreadParsingAddAndStartCsv(basePath + "yellow_tripdata_2015-09.csv", true);
		tableHandler.multiThreadParsingAddAndStartCsv(basePath + "yellow_tripdata_2015-10.csv", true);
		tableHandler.multiThreadParsingAddAndStartCsv(basePath + "yellow_tripdata_2015-11.csv", true);
		tableHandler.multiThreadParsingAddAndStartCsv(basePath + "yellow_tripdata_2015-12.csv", true);
		*/
		
		
		/*String baseDirPath = "F:/csv/";
		
		ArrayList<String> filesName = new ArrayList<String>();
		
		for (int year = 2009; year <= 2015; year ++) {
			for (int month = 1; month <= 12; month++) {
				String monthPadded = String.format("%02d" , month);
				String fileName = baseDirPath + "yellow_tripdata_" + year + "-" + monthPadded + ".csv";
				File f = new File(fileName);
				if (f.exists() == false) {
					Log.error("Le fichier " + fileName + "  est introuvable.");
				} else {
					filesName.add(fileName);
					Log.info(fileName);
				}
				//tableHandler.multiThreadParsingAddAndStartCsv(fileName, true);
			}
		}*/
		
		
		
		

		// Attendre que toute la donnée soit parsée
		tableHandler.multiThreadParsingJoinAllThreads();
		Log.info("Nombre de résultats/entrées parsés FINAL : " + Loader.debugNumberOfEntriesWritten.get());
		tableHandler.flushEveryIndexOnDisk();
	}
	
	
	static public void saveSerialData03() {
		if (Database.getInstance().getAllTables().size() == 0)
			Database.getInstance().getAllTables().add(table);
		
		SerialStructure.writeStructureTo(serializeFilePath);
	}
	
	public static void loadSerialData03() {
		SerialStructure.loadStructureFrom(serializeFilePath);
		table = Database.getInstance().getAllTables().get(0);
		tableHandler = table.getTableHandler();
		Log.info("Taille Database.getInstance().getAllTables() = " + Database.getInstance().getAllTables().size());
	}
	
	static public void search04() throws Exception {
		Timer searchQueryTimer = new Timer("Temps total recherche");
		//DataPositionList result = tableHandler.findIndexedResultsOfColumn("trip_distance", 17.78f, 18f, true);
		DataPositionList result = tableHandler.findIndexedResultsOfColumn("trip_distance", 18f);
		searchQueryTimer.log();
		ResultSet fullResulsVariables = tableHandler.getFullResultsFromBinIndexes(result, true, -1, null);
		
		//tableHandler.displayOnLogResults(fullResulsVariables);
		int numberOfResults = result.size();
		Log.info("Nombre de résultats = " + numberOfResults);
		
		
	}
	
	
	
}
