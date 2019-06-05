package db.data.load;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import db.data.load.ParallelLoader;
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
import db.structure.StructureException;
import db.structure.Table;

public class ParallelLoaderTest {
	
	
	@Test
	public void test() throws IOException, StructureException {
		Log.start("indexingTreeTest", 3);
		
		Table table = new Table("NYtest");
		
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
		ParallelLoader csvLoader = new ParallelLoader(table, new CsvParser());
		
		int mounthFinalCount = 2;
		for (int iCsv = 1; iCsv <= mounthFinalCount; iCsv++) {
			String colNumber = String.format("%02d" , iCsv);
			String csvPath = "testdata/SMALL_100_000_yellow_tripdata_2015-04.csv";
			// String csvPath = "E:/csv/yellow_tripdata_2015-" + colNumber + ".csv"; // E:/L3 DANT disque E
			Log.info("Parsing de csvName = " + csvPath);
			parseThisCsv(table, csvLoader, csvPath);
		}
		
		// Gros test et multi-thread
		// Ne pas supprimer, exemple de multi-fichiers !
		/*
		MemUsage.printMemUsage("Mem usage  début - ");
		ParallelLoader csvLoader = new ParallelLoader(table, new CsvParser());
		ParallelLoader csvLoader2 = new ParallelLoader(table, new CsvParser());
		
		
		
		int mounthFinalCount = 12;
		for (int iCsv = 1; iCsv <= mounthFinalCount; iCsv += 2) {
			String colNumber = String.format("%02d" , iCsv);
			String colNumber2 = String.format("%02d" , iCsv + 1);
			//String csvPath = "testdata/SMALL_100_000_yellow_tripdata_2015-04.csv";
			String csvPath1 = "F:/csv/yellow_tripdata_2015-" + colNumber + ".csv"; // E:/L3 DANT disque E/
			String csvPath2 = "F:/csv/yellow_tripdata_2015-" + colNumber2 + ".csv";
			Log.info("Parsing de csvName = " + csvPath1 + " et " + csvPath2);
			//Log.info("Parsing de csvName = " + csvPath);
			Thread th1 = new Thread(() -> {
				try {
					parseThisCsv(table, csvLoader, csvPath1);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			Thread th2 = new Thread(() -> {
				try {
					parseThisCsv(table, csvLoader2, csvPath2);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			th1.start();
			th2.start();
			try {
				th1.join();
				th2.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
		
		System.gc();
		MemUsage.printMemUsage("Mem usage  fin - ");
		parseTimer.log();
		
		
	}
	
	private void parseThisCsv(Table table, ParallelLoader csvLoader, String csvPath) throws IOException {
		InputStream csvStream = new FileInputStream(csvPath);
		csvLoader.parse(csvStream, true);
		csvStream.close();
	}
	
	
}
