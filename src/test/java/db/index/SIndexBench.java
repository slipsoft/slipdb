package db.index;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import com.dant.utils.Log;
import com.dant.utils.MemUsage;
import com.dant.utils.Timer;

import db.data.load.CsvParser;
import db.data.types.ByteType;
import db.data.types.DateType;
import db.data.types.DoubleType;
import db.data.types.FloatType;
import db.data.types.StringType;
import db.structure.Column;
import db.structure.Table;
import noob.fastStructure.SCsvLoader;

public class SIndexBench {

	private int limitResultNb = 20_000;
	Table table;
	
	
	@Test
	public void mainTest() throws Exception {
		
		// Chargement des CSV
		loadFirst();
		
		// Ajout à l'index
		indexColumns(new int[] {3, 4}); // trip_distance et passenger_count
		indexColumns(new int[] {3}); // trip_distance
		
		
	}
	
	
	private void indexColumns(int[] colIndexList) {
		if (colIndexList.length == 0) return;
		
		Column[] choosenColArray = new Column[colIndexList.length];
		for (int i = 0; i < colIndexList.length; i++) {
			choosenColArray[i] = table.getColumns().get(i);
		}
		
		// Nombre de lignes au total
		int linesNumber = choosenColArray[0].getTotalLinesNumber();
		
		for (int iLine = 0; iLine < linesNumber; iLine++) {
			// Pour indexer, je n'ai besoin que d'un tableau d'octets
			
			
		}
		
	}
	
	
	
	public void loadFirst() throws Exception {
		Log.start("indexingTreeTest", 3);
		
		table = new Table("NYtest");
		
		table.addColumn("VendorID", new ByteType());
		table.addColumn("tpep_pickup_datetime", new DateType()); //new StringType(19));//
		table.addColumn("tpep_dropoff_datetime", new DateType());//new StringType(19)); //
		table.addColumn("passenger_count", new ByteType());
		table.addColumn("trip_distance", new FloatType());
		table.addColumn("pickup_longitude", new DoubleType());
		table.addColumn("pickup_latitude", new DoubleType());
		table.addColumn("RateCodeID", new ByteType());
		table.addColumn("store_and_fwd_flag", new StringType(1));
		table.addColumn("dropoff_longitude", new DoubleType());
		table.addColumn("dropoff_latitude", new DoubleType());
		table.addColumn("payment_type",  new ByteType());
		table.addColumn("fare_amount", new FloatType());
		table.addColumn("extra", new FloatType());
		table.addColumn("mta_tax", new FloatType());
		table.addColumn("tip_amount", new FloatType());
		table.addColumn("tolls_amount", new FloatType());
		table.addColumn("improvement_surcharge", new FloatType());
		table.addColumn("total_amount", new FloatType());
		
		Timer parseTimer = new Timer("TEMPS TOTAL PRIS PAR TOUS LES PARSINGS");
		
		System.gc();
		MemUsage.printMemUsage("Mem usage  début - ");
		SCsvLoader csvLoader = new SCsvLoader(table, new CsvParser());
		
		int mounthFinalCount = 2;
		for (int iCsv = 1; iCsv <= mounthFinalCount; iCsv++) {
			String colNumber = String.format("%02d" , iCsv);
			String csvPath = "testdata/SMALL_100_000_yellow_tripdata_2015-04.csv";
			//String csvPath = "F:/csv/yellow_tripdata_2015-" + colNumber + ".csv"; // E:/L3 DANT disque E
			Log.info("Parsing de csvName = " + csvPath);
			parseThisCsv(table, csvLoader, csvPath);
		}
		
		System.gc();
		MemUsage.printMemUsage("Mem usage  fin - ");
		parseTimer.log();
		
		
	}
	
	private void parseThisCsv(Table table, SCsvLoader csvLoader, String csvPath) throws IOException {
		InputStream csvStream = new FileInputStream(csvPath);
		csvLoader.parse(csvStream, true);
		csvStream.close();
	}
}
