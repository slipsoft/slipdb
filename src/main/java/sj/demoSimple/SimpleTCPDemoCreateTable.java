package sj.demoSimple;


import java.io.IOException;

import com.dant.utils.Log;

import db.data.ByteType;
import db.data.DateType;
import db.data.DoubleType;
import db.data.FloatType;
import db.data.StringType;
import db.parsers.Parser;
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

	protected Table table;
	//protected Utils currentlyUsedUils = new Utils(); // For thread-safety ! (but, here, it's static so thread unsafe... ^^')
	protected TableHandler tableHandler;
	
	protected String serializeFilePath = "data_save/SimpleTCPDemo_serialData.bin";
	
	public void initTable01() throws Exception {
		Log.info("initTable01");
		//Log.start("indexingTreeTest", 2);
		
		tableHandler = new TableHandler("NYBigTest");
		
		//getValuesOfLineByIdForSignleQuery
	
		tableHandler.addColumn("VendorID", new ByteType());
		// -> On a bien les mêmes résultats en castant la date et en la traîtant comme une string
		tableHandler.addColumn("tpep_pickup_datetime", new DateType()); //new StringType(19));//
		tableHandler.addColumn("tpep_dropoff_datetime", new DateType());//new StringType(19)); // 
		tableHandler.addColumn("passenger_count", new ByteType());
		tableHandler.addColumn("trip_distance", new FloatType());
		tableHandler.addColumn("pickup_longitude", new DoubleType());
		tableHandler.addColumn("pickup_latitude", new DoubleType());
		tableHandler.addColumn("RateCodeID", new ByteType());
		tableHandler.addColumn("store_and_fwd_flag", new StringType(1));
		tableHandler.addColumn("dropoff_longitude", new DoubleType());
		tableHandler.addColumn("dropoff_latitude", new DoubleType());
		tableHandler.addColumn("payment_type",  new ByteType());
		tableHandler.addColumn("fare_amount", new FloatType());
		tableHandler.addColumn("extra", new FloatType());
		tableHandler.addColumn("mta_tax", new FloatType());
		tableHandler.addColumn("tip_amount", new FloatType());
		tableHandler.addColumn("tolls_amount", new FloatType());
		tableHandler.addColumn("improvement_surcharge", new FloatType());
		tableHandler.addColumn("total_amount", new FloatType());
		
		table = tableHandler.createTable();
		
		tableHandler.clearDataDirectory();

		tableHandler.createRuntimeIndexingColumn("VendorID");
		
		for (int colIndex = 0; colIndex < table.getColumns().size(); colIndex++) {
			tableHandler.createRuntimeIndexingColumn(colIndex);
		}
		
		Database.getInstance().getAllTables().add(table);
		SerialStructure.writeStructureTo(serializeFilePath);
		
	}
	
	public void parseCsvData02() throws IOException {
		tableHandler.multiThreadParsingAddAndStartCsv("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);
		tableHandler.multiThreadParsingAddAndStartCsv("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);
		tableHandler.multiThreadParsingAddAndStartCsv("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);
		tableHandler.multiThreadParsingAddAndStartCsv("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);
		// Attendre que toute la donnée soit parsée
		tableHandler.multiThreadParsingJoinAllThreads();
		Log.info("Nombre de résultats/entrées parsés FINAL : " + Parser.debugNumberOfEntriesWritten.get());
		tableHandler.flushEveryIndexOnDisk();
	}
	
	public void saveSerialData03() {
		Database.getInstance().getAllTables().add(table);
		SerialStructure.writeStructureTo(serializeFilePath);
	}
	
	
	
}
