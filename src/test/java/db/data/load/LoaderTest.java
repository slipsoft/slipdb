package db.data.load;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import db.data.types.*;
import db.disk.dataHandler.DiskDataPosition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.dant.utils.Log;
import com.dant.utils.Timer;
import com.dant.utils.Utils;

import db.structure.Table;

class LoaderTest {
	private Loader loader;
	private Table table;
	private Utils utilsInstance;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Log.start("csvParserTest", 3);
	}

	@BeforeEach
	void setUp() throws Exception {
		utilsInstance = new Utils(); // For thread-safety !
		table = new Table("test");
		try {
			table.addColumn("VendorID", new ByteType());
			table.addColumn("tpep_pickup_datetime", new DateType());
			table.addColumn("tpep_dropoff_datetime", new DateType());
			table.addColumn("passenger_count", new ByteType());
			table.addColumn("trip_distance", new FloatType());
			table.addColumn("pickup_longitude", new DoubleType());
			table.addColumn("pickup_latitude", new DoubleType());
			table.addColumn("RateCodeID", new ByteType());
			table.addColumn("store_and_fwd_flag", new StringType(3)); // <- Longueur de 1, test du padding
			table.addColumn("dropoff_longitude", new DoubleType());
			table.addColumn("dropoff_latitude", new DoubleType());
			table.addColumn("payment_type", new ByteType());
			table.addColumn("fare_amount", new FloatType());
			table.addColumn("extra", new FloatType());
			table.addColumn("mta_tax", new FloatType());
			table.addColumn("tip_amount", new FloatType());
			table.addColumn("tolls_amount", new FloatType());
			table.addColumn("improvement_surcharge", new FloatType());
			table.addColumn("total_amount", new FloatType());
		} catch (Exception e) {
			Log.error(e);
		}
		loader = new Loader(table, new CsvParser(), false); // 750 ms
	}

	@Deprecated
	@Test
	void testParseInputStreamInt() {
		Executable exec = () -> {
			FileInputStream is = new FileInputStream("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv");
			Timer parseTimer = new Timer("Temps pris par le parsing");
			loader.parse(is, false); // Supprimer les données déjà écrites
			parseTimer.log();
		};
		assertDoesNotThrow(exec);

		List<Object> expected = new ArrayList<>();
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

		DataPositionList positions = new DataPositionList();
		positions.add(new DiskDataPosition((short) 1, (short)1, 70));
		List<Object> result = table.getFullResultsFromBinIndexes(positions).get(0);
		Log.debug(result, "line/70");
		assertEquals(expected, result);
	}
}
