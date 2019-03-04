package db.parseCSV;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.dant.utils.Log;

import db.structure.Column;
import db.structure.Table;

class CSVParserTest {
	protected Table table;
	protected CSVParser parser = new CSVParser();

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Log.start("target/slipdb.log", 3);	
	}

	@BeforeEach
	void setUp() throws Exception {
		table = new Table("test", new ArrayList<Column>());
		try {
			table.addColumn("VendorID", OptimDataFromCSV.toByte);
			table.addColumn("tpep_pickup_datetime", OptimDataFromCSV.dateStringToInteger);
			table.addColumn("tpep_dropoff_datetime", OptimDataFromCSV.dateStringToInteger);
			table.addColumn("passenger_count", OptimDataFromCSV.toByte);
			table.addColumn("trip_distance", OptimDataFromCSV.floatToShort, true);
			table.addColumn("pickup_longitude", OptimDataFromCSV.toDouble);
			table.addColumn("pickup_latitude", OptimDataFromCSV.toDouble);
			table.addColumn("RateCodeID", OptimDataFromCSV.toByte);
			table.addColumn("store_and_fwd_flag", OptimDataFromCSV.toChar); // 1 caractère
			table.addColumn("dropoff_longitude", OptimDataFromCSV.toDouble);
			table.addColumn("dropoff_latitude", OptimDataFromCSV.toDouble);
			table.addColumn("payment_type", OptimDataFromCSV.toByte);
			table.addColumn("fare_amount", OptimDataFromCSV.floatToShort);
			table.addColumn("extra", OptimDataFromCSV.floatToByte);
			table.addColumn("mta_tax", OptimDataFromCSV.floatToByte);
			table.addColumn("tip_amount", OptimDataFromCSV.floatToShort);
			table.addColumn("tolls_amount", OptimDataFromCSV.floatToShort);
			table.addColumn("improvement_surcharge", OptimDataFromCSV.floatToByte);
			table.addColumn("total_amount", OptimDataFromCSV.floatToShort);
		} catch (Exception e) {
			Log.error(e);
		}
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testLoadFromCSV() {
		Executable exec = new Executable() {
			@Override
			public void execute() throws Throwable {
				parser.loadFromCSV("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv",
				           "testdata/SMALL_100_000_yellow_tripdata_2015-04.bin", 100_000, table);
			}
		};
		assertDoesNotThrow(exec);
	}

}
