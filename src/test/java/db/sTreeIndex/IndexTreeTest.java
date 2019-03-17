package db.sTreeIndex;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.dant.utils.Log;
import com.dant.utils.MemUsage;
import com.dant.utils.Timer;
import com.dant.utils.Utils;

import db.data.ByteType;
import db.data.DateType;
import db.data.DoubleType;
import db.data.FloatType;
import db.data.IntegerArrayList;
import db.data.StringType;
import db.parsers.CsvParser;
import db.parsers.Parser;
import db.structure.Column;
import db.structure.Table;
import db.structure.indexTree.IndexTreeCeption;
import db.structure.indexTree.IndexTreeDic;

public class IndexTreeTest {

	protected static Parser parser;
	protected static Table table;
	protected static Utils currentlyUsedUils = new Utils(); // For thread-safety ! (but, here, it's static so thread unsafe... ^^')

	@BeforeAll
	static void setUpBeforeAll() throws Exception {
		Log.info("setUpBeforeAll");
		Log.start("target/slipdb_indexingTreeTest.log", 3);
		//if (true) return;
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
		
		//FileInputStream is = new FileInputStream("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv"); // "../SMALL_1_000_000_yellow_tripdata_2015-04.csv"
		FileInputStream is = new FileInputStream("../SMALL_1_000_000_yellow_tripdata_2015-04.csv"); // testdata
		
		Timer parseTimer = new Timer("Temps pris par le parsing");
		parser.parse(is);
		parseTimer.printms();
		
		Log.info("setUpBeforeAll OK");
	}
	
	/*
	@Test
	void testStorageLimit() {
		
		int arraySize = 1_000_000;
		MemUsage.printMemUsage();
		/*float[] floatArray = new float[arraySize];
		for (float i = 0; i < arraySize; i++) {
			floatArray[(int)i] = i;
		}* /
		Timer time = new Timer("Time");
		Float[] floatArray = new Float[arraySize];
		float nb = 0;
		for (float i = 0; i < arraySize; i++) {
			floatArray[(int)i] = nb;
			nb++;
		}
		time.printms();
		MemUsage.printMemUsage();
		
	}*/
	
	@Test
	void testIndexingTreeInt() throws IOException {
		//if (true) return;
		/**
		 * Note : c'est super le bordel ici, je vais ranger ça ^^'
		 */
		//SIndexingTreeFloat indexingFoat = new SIndexingTreeFloat();
		Log.info("Lancé");
		
		// Index the column on index 4
		//int indexingColumnIndex = 3; // passanger count
		//int indexingColumnIndex = 4; // trip distance
		//int indexingColumnIndex = 5; // latitude
		int indexingColumnIndex = 1; // date pickup
		
		Column indexThisColumn = table.getColumns().get(indexingColumnIndex);
		
		System.out.println("OUOUOUOU " + indexThisColumn.minValue + "  " +  indexThisColumn.maxValue);

		//IndexTreeCeption indexingObject = new IndexTreeCeption(0, null, indexThisColumn.minValue, indexThisColumn.maxValue);
		IndexTreeDic indexingObject = new IndexTreeDic();//Integer.class);
		
		
		//indexingObject.initializeMaxDistanceBetweenElementsArray(indexThisColumn.minValue, indexThisColumn.maxValue);
		
		// Index the column from the disk
		// -> reading fron the disk is quite slow
		// --> a very cool optimization will be to index a bunch of columns at the same time
		Timer loadFromDiskTimer = new Timer("Time took to index this column, from disk");
		MemUsage.printMemUsage();
		indexingObject.indexColumnFromDisk(table, indexingColumnIndex);
		MemUsage.printMemUsage();
		loadFromDiskTimer.printms();
		
		// Ecriture sur le disque
		Timer writeIndexToDiskTimer = new Timer("Temps pris pour l'écriture sur disque");
		indexingObject.saveOnDisk(false); // première sauvegarde, écraser ce qui existe déjà
		writeIndexToDiskTimer.printms();
		
		
		Log.info("Fini");
		Log.info("OBJECT RESULT :");
		
		// Get the query result
		Collection<IntegerArrayList> result;
		MemUsage.printMemUsage();
		
		
		Date dateFrom = currentlyUsedUils.dateFromString("2015-04-29 00:05:00");
		Date dateTo = currentlyUsedUils.dateFromString("2015-04-29 00:10:30");
		int intDateFrom = Utils.dateToSecInt(dateFrom);
		int intDateTo = Utils.dateToSecInt(dateTo);
		
		/*Object searchFromValue = new Float(12.78641);
		Object searchToValue = new Float(14.748621);*/

		Object searchFromValue = intDateFrom;
		Object searchToValue = intDateTo;
		
		result = indexingObject.findMatchingBinIndexesInMemory(searchFromValue, searchToValue, true); // new Float(20), new Float(21)
		//result = indexingObject.findMatchingBinIndexesInMemory(intDateFrom, intDateTo, true);
		
		//result = indexingObject.findMatchingBinIndexes(new Integer(-1000), new Integer(1000), true);

		
		Timer searchQueryTimer = new Timer("Time took to return the matching elements");
		Timer searchQueryFullTimer = new Timer("Time took to return the matching elements + size evaluation");
		
		//result = indexingObject.findMatchingBinIndexes(new Byte((byte)0), new Byte((byte)100), true);
		//result = indexingObject.findMatchingBinIndexes(new Double(0), new Double(0), true);
		
		
		MemUsage.printMemUsage();
		searchQueryTimer.printms();
		
		// Iterates over all the results
		int numberOfResults = 0, numberOfLines = 0;
		for (IntegerArrayList list : result) {
			//Log.info("list size = " + list.size());
			numberOfResults += list.size();
			numberOfLines++;
			for (Integer index : list) {
				// un-comment those lines if you want to get the full info on lines : List<Object> objList = table.getValuesOfLineById(index);
				/*Log.info("  index = " + index);
				List<Object> objList = table.getValuesOfLineById(index);
				Object indexedValue = objList.get(indexingColumnIndex);
				
				//Log.info("  valeur indexée = " + indexedValue);
				Log.info("  objList = " + objList);*/
				
			}
		}
		searchQueryFullTimer.printms();
		Log.info("Number of results = " + numberOfResults);
		Log.info("Number of lines = " + numberOfLines);
		
		
		Log.info("Depuis le disque : ");
		Timer searchFromDiskTimer = new Timer("Temps pris pour la recherche du disque");
		
		result = indexingObject.findMatchingBinIndexesFromDisk(searchFromValue, searchToValue, true);
		//result = indexingObject.findMatchingBinIndexesFromDisk(intDateFrom, intDateTo, true);
		
		searchFromDiskTimer.printms();
		
		// Iterates over all the results
		numberOfResults = 0;
		numberOfLines = 0;
		for (IntegerArrayList list : result) {
			//Log.info("list size = " + list.size());
			numberOfResults += list.size();
			numberOfLines++;
			for (Integer index : list) {
				// un-comment those lines if you want to get the full info on lines : List<Object> objList = table.getValuesOfLineById(index);
				/*Log.info("  index = " + index);
				List<Object> objList = table.getValuesOfLineById(index);
				Object indexedValue = objList.get(indexingColumnIndex);
				
				//Log.info("  valeur indexée = " + indexedValue);
				Log.info("  objList = " + objList);*/
				
			}
		}
		if (result.size() > 0) Log.info("from value = ");
		Log.info("Number of results = " + numberOfResults);
		Log.info("Number of lines = " + numberOfLines);

		
		
		
	}
	
	@AfterAll
	static void tearDown() {
		
	}
}
