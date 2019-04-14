package newLoader.tests;


import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import com.dant.utils.Log;
import com.dant.utils.Timer;

import db.data.load.CsvParser;
import db.data.load.Loader;
import db.data.types.ByteType;
import db.data.types.DateType;
import db.data.types.DoubleType;
import db.data.types.FloatType;
import db.data.types.StringType;
import db.structure.StructureException;
import db.structure.Table;
import noob.fastStructure.SCsvLoader;

public class SLoaderTests {
	
	
	@Test
	public void test() throws IOException, StructureException {
		Log.info("setUpBeforeAll");
		Log.start("indexingTreeTest", 3);
		
		Table table = new Table("NYtest");
		
		table.addColumn("VendorID", new ByteType());table.addColumn("tpep_pickup_datetime", new DateType()); //new StringType(19));//
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
		
		Timer parseTimer = new Timer("TEMPS TOTAL pris par le parsing");
		
		//String csvPath = "testdata/SMALL_100_000_yellow_tripdata_2015-04.csv";
		
		String csvPath = "E:/L3 DANT disque E/csv/yellow_tripdata_2015-07.csv";
		InputStream csvStream = new FileInputStream(csvPath);
		SCsvLoader csvLoader = new SCsvLoader(table, new CsvParser());
		csvLoader.parse(csvStream, true);
		
		
	}
	
	
}
