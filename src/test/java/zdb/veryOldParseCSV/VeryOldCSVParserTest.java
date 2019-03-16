package zdb.veryOldParseCSV;

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
import zArchive.sj.simpleBD.parseCSV.SCSVParser;
import zArchive.sj.simpleBD.parseCSV.SOptimDataFromCSV;

class VeryOldCSVParserTest {
	protected Table table;
	protected SCSVParser parser = new SCSVParser();

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Log.start("target/slipdb.log", 3);	
	}

	@BeforeEach
	void setUp() throws Exception {
		table = new Table("test", new ArrayList<Column>());
		try {
			table.addColumn("VendorID", SOptimDataFromCSV.toByte);
			table.addColumn("tpep_pickup_datetime", SOptimDataFromCSV.dateStringToInteger);
			table.addColumn("tpep_dropoff_datetime", SOptimDataFromCSV.dateStringToInteger);
			table.addColumn("passenger_count", SOptimDataFromCSV.toByte);
			table.addColumn("trip_distance", SOptimDataFromCSV.floatToShort);
			table.addColumn("pickup_longitude", SOptimDataFromCSV.toDouble);
			table.addColumn("pickup_latitude", SOptimDataFromCSV.toDouble);
			table.addColumn("RateCodeID", SOptimDataFromCSV.toByte);
			table.addColumn("store_and_fwd_flag", SOptimDataFromCSV.toChar); // 1 caractère
			table.addColumn("dropoff_longitude", SOptimDataFromCSV.toDouble);
			table.addColumn("dropoff_latitude", SOptimDataFromCSV.toDouble);
			table.addColumn("payment_type", SOptimDataFromCSV.toByte);
			table.addColumn("fare_amount", SOptimDataFromCSV.floatToShort);
			table.addColumn("extra", SOptimDataFromCSV.floatToByte);
			table.addColumn("mta_tax", SOptimDataFromCSV.floatToByte);
			table.addColumn("tip_amount", SOptimDataFromCSV.floatToShort);
			table.addColumn("tolls_amount", SOptimDataFromCSV.floatToShort);
			table.addColumn("improvement_surcharge", SOptimDataFromCSV.floatToByte);
			table.addColumn("total_amount", SOptimDataFromCSV.floatToShort);
			
			int totalEntrySize = 0;
			for (Column col : table.getColumns()) {
				totalEntrySize += col.dataType.getSize();
			}
			Log.debug("Sylvain CSVParserTest : totalEntrySize = " + totalEntrySize);
			
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
