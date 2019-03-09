package db.sTreeIndex;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dant.utils.Log;
import com.dant.utils.Timer;

import db.data.ByteType;
import db.data.DateType;
import db.data.DoubleType;
import db.data.FloatType;
import db.data.IntegerArrayList;
import db.data.StringType;
import db.parsers.CsvParser;
import db.structure.Column;
import db.structure.IndexTree;
import db.structure.Table;

public class IndexTreeTest {

	protected static CsvParser parser;
	protected static Table table;

	@BeforeAll
	static void setUpBeforeAll() throws Exception {
		Log.info("setUpBeforeAll");
		Log.start("target/slipdb_indexingTreeTest.log", 3);
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
			columns.add(new Column("store_and_fwd_flag", new StringType(1)));
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
		
		FileInputStream is = new FileInputStream("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv");
		Timer parseTimer = new Timer("Temps pris par le parsing");
		parser.parse(is);
		parseTimer.printms();
		
		Log.info("setUpBeforeAll OK");
	}
	
	@Test
	void testIndexingTreeInt() throws IOException {
		
		/**
		 * Note : c'est super le bordel ici, je vais ranger ça ^^'
		 */
		IndexTree indexingObject = new IndexTree();
		//SIndexingTreeFloat indexingFoat = new SIndexingTreeFloat();
		Log.info("Lancé");
		
		// Index the column on index 4
		int indexingColumnIndex = 4;
		
		// Index the column from the disk
		// -> reading fron the disk is quite slow
		// --> a very cool optimization will be to index a bunch of columns at the same time
		Timer loadFromDiskTimer = new Timer("Time took to index this column, from disk");
		indexingObject.indexColumnFromDisk(table, indexingColumnIndex);
		loadFromDiskTimer.printms();
		
		Timer searchQueryTimer = new Timer("Time took to return the matching elements");
		Timer searchQueryFullTimer = new Timer("Time took to return the matching elements + size evaluation");
		Log.info("Fini");
		Log.info("OBJECT RESULT :");
		
		// Get the query result
		Collection<IntegerArrayList> result;
		result = indexingObject.findMatchingBinIndexes(new Float(3), new Float(100), true); // new Float(20), new Float(21)
		searchQueryTimer.printms();
		
		// Iterates over all the results
		int numberOfResults = 0;
		for (IntegerArrayList list : result) {
			//Log.info("list size = " + list.size());
			numberOfResults += list.size();
			for (Integer index : list) {
				// un-comment this line if you want to get the full info on lines : List<Object> objList = table.getValuesOfLineById(index);
				/*Log.info("  index = " + index);
				Object indexedValue = objList.get(indexingColumnIndex);
				
				Log.info("  valeur indexée = " + indexedValue);*/
				
			}
		}
		searchQueryFullTimer.printms();
		Log.info("Number of results = " + numberOfResults);
		
	}
	
	@AfterAll
	static void tearDown() {
		
	}
}
