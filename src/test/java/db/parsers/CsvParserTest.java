package db.parsers;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.dant.utils.Log;
import com.dant.utils.Timer;

import db.data.ByteType;
import db.data.DateType;
import db.data.DoubleType;
import db.data.FloatType;
import db.data.StringType;
import db.structure.Column;
import db.structure.Table;
import zArchive.sj.simpleBD.parseCSV.SCSVParser;

class CsvParserTest {
	protected CsvParser parser;
	protected Table table;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Log.start("target/slipdb.log", 3);
	}

	@BeforeEach
	void setUp() throws Exception {
		ArrayList<Column> columns = new ArrayList<Column>();
		try {
			columns.add(new Column("VendorID", new ByteType()));
			columns.add(new Column("tpep_pickup_datetime", new DateType()));
			columns.add(new Column("tpep_dropoff_datetime", new DateType()));
			columns.add(new Column("passenger_count", new ByteType()));
			columns.add(new Column("trip_distance", new FloatType()));
			columns.add(new Column("pickup_longitude", new DoubleType()));
			columns.add(new Column("pickup_latitude", new DoubleType()));
			columns.add(new Column("RateCodeID", new ByteType()));
			columns.add(new Column("store_and_fwd_flag", new StringType(3)));
			columns.add(new Column("dropoff_longitude", new DoubleType()));
			columns.add(new Column("dropoff_latitude", new DoubleType()));
			columns.add(new Column("payment_type",  new ByteType()));
			columns.add(new Column("fare_amount", new FloatType()));
			columns.add(new Column("extra", new FloatType()));
			columns.add(new Column("mta_tax", new FloatType()));
			columns.add(new Column("tip_amount", new FloatType()));
			columns.add(new Column("tolls_amount", new FloatType()));
			columns.add(new Column("improvement_surcharge", new FloatType()));
			columns.add(new Column("total_amount", new FloatType()));
		} catch (Exception e) {
			Log.error(e);
		}
		table = new Table("test", columns);
		parser = new CsvParser(table);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testParseInputStreamInt() throws IOException {
		Executable exec = new Executable() {
			@Override
			public void execute() throws Throwable {
				FileInputStream is = new FileInputStream("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv");
				Timer parseTimer = new Timer("Temps pris par le parsing");
				parser.parse(is);
				parseTimer.printms();
			}
		};
		assertDoesNotThrow(exec);
		Log.debug(table.getValuesOfLineById(0), "entry/0");
		Log.debug(table.getValuesOfLineById(70), "entry/70");
	}
}

