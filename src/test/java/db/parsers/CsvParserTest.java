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

import db.data.DateTime;
import db.data.Decimal;
import db.data.Int;
import db.data.Text;
import db.structure.Column;
import db.structure.Table;

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
			columns.add(new Column("VendorID", new Int(4)));
			columns.add(new Column("tpep_pickup_datetime", new DateTime()));
			columns.add(new Column("tpep_dropoff_datetime", new DateTime()));
			columns.add(new Column("passenger_count", new Int(4)));
			columns.add(new Column("trip_distance", new Decimal(8)));
			columns.add(new Column("pickup_longitude", new Decimal(8)));
			columns.add(new Column("pickup_latitude", new Decimal(8)));
			columns.add(new Column("RateCodeID", new Int(4)));
			columns.add(new Column("store_and_fwd_flag", new Text(1)));
			columns.add(new Column("dropoff_longitude", new Decimal(8)));
			columns.add(new Column("dropoff_latitude", new Decimal(8)));
			columns.add(new Column("payment_type",  new Int(4)));
			columns.add(new Column("fare_amount", new Decimal(8)));
			columns.add(new Column("extra", new Decimal(8)));
			columns.add(new Column("mta_tax", new Decimal(8)));
			columns.add(new Column("tip_amount", new Decimal(8)));
			columns.add(new Column("tolls_amount", new Decimal(8)));
			columns.add(new Column("improvement_surcharge", new Decimal(8)));
			columns.add(new Column("total_amount", new Decimal(8)));
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
				parser.parse(is);
			}
		};
		assertDoesNotThrow(exec);
		Log.debug(table.get(0), "entry/0");
		Log.debug(table.get(70), "entry/70");
	}

}
