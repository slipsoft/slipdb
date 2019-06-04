package zArchive.sj.demoSimple;


import java.io.IOException;

import com.dant.utils.Log;
import com.dant.utils.Timer;

import db.data.types.ByteType;
import db.data.types.DateType;
import db.data.types.DoubleType;
import db.data.types.FloatType;
import db.data.types.StringType;
import db.search.Operator;
import db.search.Predicate;
import db.search.ResultSet;
import db.data.load.Loader;
import db.search.View;
import db.serial.SerialStructure;
import db.structure.Column;
import db.structure.Database;
import db.structure.Table;
import index.indexTree.IndexTreeDic;

/**
 *  Créer et parser la table, chargement initial
 *  
 *  
 */
public class SimpleTCPDemoCreateTable {

	static protected Table table;
	//protected Utils currentlyUsedUils = new Utils(); // For thread-safety ! (but, here, it's static so thread unsafe... ^^')
	
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
		
		table = new Table(tableName);
		
		//getValuesOfLineByIdForSignleQuery
		
		
		// Modèle 2015
		table.addColumn("VendorID",                 new ByteType(), false, false);
		// -> On a bien les mêmes résultats en castant la date et en la traîtant comme une string
		table.addColumn("tpep_pickup_datetime",     new DateType(), false, false); //new StringType(19));//
		table.addColumn("tpep_dropoff_datetime",    new DateType(), false, false);   //new StringType(19)); //
		table.addColumn("passenger_count",          new ByteType(), false, false);
		table.addColumn("trip_distance",           new FloatType(), true,  false);
		table.addColumn("pickup_longitude",       new DoubleType(), false, false);
		table.addColumn("pickup_latitude",        new DoubleType(), false, false);
		table.addColumn("RateCodeID",               new ByteType(), false, false);
		table.addColumn("store_and_fwd_flag",    new StringType(1), false, false);
		table.addColumn("dropoff_longitude",      new DoubleType(), false, false);
		table.addColumn("dropoff_latitude",       new DoubleType(), false, false);
		table.addColumn("payment_type",             new ByteType(), false, false);
		table.addColumn("fare_amount",             new FloatType(), false, false);
		table.addColumn("extra",                   new FloatType(), false, false);
		table.addColumn("mta_tax",                 new FloatType(), false, false);
		table.addColumn("tip_amount",              new FloatType(), false, false);
		table.addColumn("tolls_amount",            new FloatType(), false, false);
		table.addColumn("improvement_surcharge",   new FloatType(), false, false);
		table.addColumn("total_amount",            new FloatType(), false, false);
		
		// 2014 :
		
		table.clearDataDirectory();

		/*tableHandler.createRuntimeIndexingColumn("VendorID");
		
		tableHandler.createRuntimeIndexingColumn("trip_distance");*/
		Database.getInstance().getAllTables().add(table);
		SerialStructure.writeStructureTo(serializeFilePath);
		
	}
	
	static public void parseCsvData02() throws IOException, Exception { // non  synchronized
		
		table.createIndex("trip_distance", IndexTreeDic.class);
		
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
		table.multiThreadParsingJoinAllThreads();
		Log.info("Nombre de résultats/entrées parsés FINAL : " + Loader.debugNumberOfEntriesWritten.get());
		table.flushAllIndexOnDisk();
	}
	
	
	static public void saveSerialData03() {
		if (Database.getInstance().getAllTables().size() == 0)
			Database.getInstance().getAllTables().add(table);
		
		SerialStructure.writeStructureTo(serializeFilePath);
	}
	
	public static void loadSerialData03() {
		SerialStructure.loadStructureFrom(serializeFilePath);
		table = Database.getInstance().getAllTables().get(0);
		Log.info("Taille Database.getInstance().getAllTables() = " + Database.getInstance().getAllTables().size());
	}
	
	static public void search04() throws Exception {
		Timer searchQueryTimer = new Timer("Temps total recherche");
		Column column = table.getColumnByName("trip_distance").orElseThrow(Exception::new);
		Predicate predicate = new Predicate(table, column, Operator.equals, 18f);
		View view = new View(table, predicate);
		//DataPositionList result = tableHandler.findIndexedResultsOfColumn("trip_distance", 17.78f, 18f, true);
		//DataPositionList result = table.findIndexedResultsOfColumn("trip_distance", 18f);
		searchQueryTimer.log();
		ResultSet fullResulsVariables = view.execute();
		
		//tableHandler.displayOnLogResults(fullResulsVariables);
		int numberOfResults = fullResulsVariables.size();
		Log.info("Nombre de résultats = " + numberOfResults);
		
		
	}
	
	
	
}
