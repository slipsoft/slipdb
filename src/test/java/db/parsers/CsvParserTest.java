package db.parsers;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.dant.utils.Log;
import com.dant.utils.Timer;
import com.dant.utils.Utils;

import db.data.ByteType;
import db.data.DateType;
import db.data.DoubleType;
import db.data.FloatType;
import db.data.StringType;
import db.structure.Column;
import db.structure.Table;

class CsvParserTest {
	protected Parser parser;
	protected Table table;
	Utils utilsInstance;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Log.start("csvParserTest", 2);
	}

	@BeforeEach
	void setUp() throws Exception {
		ArrayList<Column> columns = new ArrayList<Column>();
		utilsInstance = new Utils(); // For thread-safety !
		try {
			columns.add(new Column("VendorID", new ByteType()));
			columns.add(new Column("tpep_pickup_datetime", new DateType()));
			columns.add(new Column("tpep_dropoff_datetime", new DateType()));
			columns.add(new Column("passenger_count", new ByteType()));
			columns.add(new Column("trip_distance", new FloatType()));
			columns.add(new Column("pickup_longitude", new DoubleType()));
			columns.add(new Column("pickup_latitude", new DoubleType()));
			columns.add(new Column("RateCodeID", new ByteType()));
			columns.add(new Column("store_and_fwd_flag", new StringType(1))); // <- Longueur de 1
			columns.add(new Column("dropoff_longitude", new DoubleType()));
			columns.add(new Column("dropoff_latitude", new DoubleType()));
			columns.add(new Column("payment_type", new ByteType()));
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
		//parser = new CsvParser2(table); // 790 ms
		parser = new CsvParser(table); // 750 ms
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
				parser.parse(is, false); // Supprimer les données déjà écrites
				parseTimer.log();
			}
		};
		assertDoesNotThrow(exec);
		Log.debug(table.getValuesOfLineById(0), "entry/0");
		Log.debug(table.getValuesOfLineById(69), "entry/69");
		List<Object> expected = new ArrayList<Object>();
		expected.add((byte) 2);
		expected.add(utilsInstance.dateFromString("2015-04-09 19:29:33"));
		expected.add(utilsInstance.dateFromString("2015-04-09 19:37:09"));
		expected.add((byte) 1);
		expected.add((float) 0.83);
		expected.add(-73.98651885986328);
		expected.add(40.76189422607422);
		expected.add((byte) 1);
		expected.add("N");
		expected.add(-73.97399139404297);
		expected.add(40.760414123535156);
		expected.add((byte) 1);
		expected.add((float) 6.5);
		expected.add((float) 1.0);
		expected.add((float) 0.5);
		expected.add((float) 1.66);
		expected.add((float) 0.0);
		expected.add((float) 0.3);
		expected.add((float) 9.96);

		assertEquals(expected, table.getValuesOfLineById(70));
	}
}
