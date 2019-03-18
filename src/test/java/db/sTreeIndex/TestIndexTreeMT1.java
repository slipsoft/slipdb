package db.sTreeIndex;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.io.FileUtils;
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
import db.structure.Column;
import db.structure.Table;
import db.structure.indexTree.IndexTreeCeption;

/***
 * Premier test de multi-thread simple
 *
 */


public class TestIndexTreeMT1 {

	protected static CsvParser parser;
	protected static Table table;
	
	
	static ArrayList<Column> initializeColumnsForNYData() {
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
		return columns;
	}
	
	static Table loadNewTableFromDisk(String tableName, String filePathOnDisk) throws IOException {
		ArrayList<Column> columns = initializeColumnsForNYData();
		Table newTable = new Table(tableName, columns);
		CsvParser newParser = new CsvParser(newTable);
		FileInputStream is = new FileInputStream(filePathOnDisk);
		//Timer parseTimer = new Timer("Temps pris par le parsing de " + tableName);
		newParser.parse(is, false); // Supprimer les données déjà écrites
		is.close();
		//parseTimer.log();
		return newTable;
	}
	
	static int globalTableId = 0;
	
	// Copier des fichiers (arborescence de Sylvain)
	static void createFileCopies(int maxFileCount) throws IOException {
		File sourceFile = new File("../SMALL_1_000_000_yellow_tripdata_2015-04.csv");
		
		for (int fileCount = 0; fileCount < 10; fileCount++) {
			File destFile = new File("../SMALL_1_000_000_yellow_tripdata_2015-04_"+fileCount+".csv");
			FileUtils.copyFile(sourceFile, destFile);
		}
	}
	
	@BeforeAll
	static void setUpBeforeAll() throws Exception {
		Log.info("setUpBeforeAll");
		Log.start("target/slipdb_indexingTreeTest.log", 3);

		
		//createFileCopies(10);
		
		// Runnable ArrayList : créer les runnable et les thread
		// Démarrer tout
		// Attendre que tout se termine
		// Comparer chargement linaire et multi-trhead (même fichier vs fichiers différents)
		
		ArrayList<TestIndexTreeMT1Runnable> runnableList = new ArrayList<TestIndexTreeMT1Runnable>();
		ArrayList<Thread> threadList = new ArrayList<Thread>();
		
		int maxRunCount = 10;
		
		for (int runCount = 0; runCount < maxRunCount; runCount++) {
			TestIndexTreeMT1Runnable runnableLoad = new TestIndexTreeMT1Runnable();
			Thread runThread = new Thread(runnableLoad);
			threadList.add(runThread);
			runnableList.add(runnableLoad);
		}
		
		/**
		 * Bilan : le parsing demande beaucoup plus de calculs que de disque.
		 * -> Voir quelles sont les fonctions qui prennent le plus de temps à parser
		 * -> Possible de parser le même fichier, en partant à des index différents (mais pas une bonne idée, car je ne sais pas d'où partir, de peur de couper une ligne)
		 */
		Timer parseTimer = new Timer("Temps pris par le parsing de " + maxRunCount + " fichiers");
		
		
		
		for (int runCount = 0; runCount < maxRunCount; runCount++) {
			threadList.get(runCount).start();   // 12,3 s (i3, Sylvain)
			//runnableList.get(runCount).run(); // 25,7 s (i3, Sylvain)
		}
		for (int runCount = 0; runCount < maxRunCount; runCount++) {
			threadList.get(runCount).join();
		}
		
		parseTimer.log();
		
		/**
		 * En première idée, pour parser :
		 * 1) Sauvegarder chaque colonne sur disque, parsée ou non : éviter, autant que possible, de parser les dates par exemple
		 * */
		
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
		time.log();
		MemUsage.printMemUsage();
		
	}*/
	
	@Test
	void testIndexingTreeInt() throws IOException {
		if (true) return;
		/**
		 * Note : c'est super le bordel ici, je vais ranger ça ^^'
		 */
		IndexTreeCeption indexingObject = new IndexTreeCeption();
		//SIndexingTreeFloat indexingFoat = new SIndexingTreeFloat();
		Log.info("Lancé");
		
		// Index the column on index 4
		//int indexingColumnIndex = 3; // passanger count
		//int indexingColumnIndex = 4; // trip distance
		//int indexingColumnIndex = 5; // latitude
		int indexingColumnIndex = 1; // date pickup
		
		// Index the column from the disk
		// -> reading fron the disk is quite slow
		// --> a very cool optimization will be to index a bunch of columns at the same time
		Timer loadFromDiskTimer = new Timer("Time took to index this column, from disk");
		MemUsage.printMemUsage();
		indexingObject.indexColumnFromDisk(table, indexingColumnIndex);
		MemUsage.printMemUsage();
		loadFromDiskTimer.log();
		
		// Ecriture sur le disque
		Timer writeIndexToDiskTimer = new Timer("Temps pris pour l'écriture sur disque");
		indexingObject.saveOnDisk();
		writeIndexToDiskTimer.log();
		
		
		Log.info("Fini");
		Log.info("OBJECT RESULT :");
		
		// Get the query result
		Collection<IntegerArrayList> result;
		MemUsage.printMemUsage();
		//result = indexingObject.findMatchingBinIndexes(new Float(0), new Float(10000), true); // new Float(20), new Float(21)
		//result = indexingObject.findMatchingBinIndexes(new Integer(-1000), new Integer(1000), true);
		
		Utils currentlyUsedUils = new Utils(); // For thread-safety !
		
		Date dateFrom = currentlyUsedUils.dateFromString("2015-04-16 00:05:00");
		Date dateTo = currentlyUsedUils.dateFromString("2015-04-16 00:06:30");
		int intDateFrom = currentlyUsedUils.dateToSecInt(dateFrom);
		int intDateTo = currentlyUsedUils.dateToSecInt(dateTo);
		
		Timer searchQueryTimer = new Timer("Time took to return the matching elements");
		Timer searchQueryFullTimer = new Timer("Time took to return the matching elements + size evaluation");
		result = indexingObject.findMatchingBinIndexes(intDateFrom, intDateTo, true);
		//result = indexingObject.findMatchingBinIndexes(new Byte((byte)0), new Byte((byte)100), true);
		
		
		MemUsage.printMemUsage();
		searchQueryTimer.log();
		
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
		searchQueryFullTimer.log();
		Log.info("Number of results = " + numberOfResults);
		Log.info("Number of lines = " + numberOfLines);
		
		
		Log.info("Depuis le disque : ");
		Timer searchFromDiskTimer = new Timer("Temps pris pour la recherche du disque");
		result = indexingObject.findMatchingBinIndexesFromDisk(intDateFrom, intDateTo, true);
		searchFromDiskTimer.log();
		
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
		Log.info("Number of results = " + numberOfResults);
		Log.info("Number of lines = " + numberOfLines);

		
		
		
	}
	
	@AfterAll
	static void tearDown() {
		
	}
}
