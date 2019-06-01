package index.indexTree;

import java.io.IOException;
import java.util.*;

import db.data.load.Loader;
import db.disk.dataHandler.DiskDataPosition;
import db.search.*;
import db.serial.SerialStructure;
import db.structure.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.dant.utils.Log;
import com.dant.utils.MemUsage;
import com.dant.utils.Timer;
import com.dant.utils.Utils;

import db.data.types.ByteType;
import db.data.types.DateType;
import db.data.types.DoubleType;
import db.data.types.FloatType;
import db.data.types.IntegerArrayList;
import db.data.types.DataPositionList;
import db.data.types.StringType;
import db.structure.Column;
import db.structure.Database;
import db.structure.Table;
import index.indexTree.IndexTreeCeption;
import index.indexTree.IndexTreeDic;

import static org.junit.jupiter.api.Assertions.*;

class IndexTreeMessyTest {
	
	// Voir constante dans IndexTreeDic : static public int maxResultCountPerIndexInstance = 10;
	// -> limitation du nombre de résultats affichés par arbre
	
	//protected static Loader parser;
	private static Index indexTripDistance;
	private static Index indexPickupDate;
	private static Table table;
	private static Utils currentlyUsedUils = new Utils(); // For thread-safety ! (but, here, it's static so thread unsafe... ^^')
	
	private static boolean parseAgain = true;
	private boolean doItWithTableHandler = true;
	
	public static final boolean enableCsvRelatedTests = true; // <- à true si faie tous les tests, false si c'est juste charger des CSV sans faire de test !)
	
	/** Pour la déserialisation
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	
	private static String serializeFromPath = "data_save/serialStructureEverything.bin";
	
	@BeforeAll
	static void setUpBeforeAll() throws Exception {
		SerialStructure.loadStructureFrom(serializeFromPath);
		table = Database.getInstance().getAllTables().get(0);
		/*Log.info(table.getName());
		//table.debugSerialShowVariables();
		
		//tableHandler.associatedTable = table;
		if (tableHandler.associatedTable == null) {
			Log.error("tableHandler.associatedTable == null");
		}*/
	}
	
	@BeforeAll
	@Deprecated // IndexTreeDic n'est plus utilisé, remplacé par IndexMemDic
	static void setUpBeforeAllDe() throws Exception {
		Log.info("setUpBeforeAll");
		Log.start("indexingTreeTest", 3);
		
		table = new Table("NYtest");
		assertNotNull(table);
		
		//getValuesOfLineByIdForSignleQuery

		table.addColumn("VendorID", new ByteType());
		// -> On a bien les mêmes résultats en castant la date et en la traîtant comme une string
		
		/*
		tableHandler.addColumn("tpep_pickup_datetime", new DateType(), false); //new StringType(19));//
		tableHandler.addColumn("tpep_dropoff_datetime", new DateType(), false);//new StringType(19)); // 
		tableHandler.addColumn("passenger_count", new ByteType(), false);
		tableHandler.addColumn("trip_distance", new FloatType(), true);
		tableHandler.addColumn("pickup_longitude", new DoubleType(), false);
		tableHandler.addColumn("pickup_latitude", new DoubleType(), false);
		tableHandler.addColumn("RateCodeID", new ByteType(), false);
		tableHandler.addColumn("store_and_fwd_flag", new StringType(1), false);
		tableHandler.addColumn("dropoff_longitude", new DoubleType(), false);
		tableHandler.addColumn("dropoff_latitude", new DoubleType(), false);
		tableHandler.addColumn("payment_type",  new ByteType(), false);
		tableHandler.addColumn("fare_amount", new FloatType(), false);
		tableHandler.addColumn("extra", new FloatType(), false);
		tableHandler.addColumn("mta_tax", new FloatType(), false);
		tableHandler.addColumn("tip_amount", new FloatType(), false);
		tableHandler.addColumn("tolls_amount", new FloatType(), false);
		tableHandler.addColumn("improvement_surcharge", new FloatType(), false);
		tableHandler.addColumn("total_amount", new FloatType(), false);
		*/
		/*
		 Sauvegarde : 
		tableHandler.addColumn("VendorID", new ByteType());
		// -> On a bien les mêmes résultats en castant la date et en la traîtant comme une string
		tableHandler.addColumn("tpep_pickup_datetime", new DateType()); //new StringType(19));//
		tableHandler.addColumn("tpep_dropoff_datetime", new DateType());//new StringType(19)); // 
		tableHandler.addColumn("passenger_count", new ByteType());
		tableHandler.addColumn("trip_distance", new FloatType());
		tableHandler.addColumn("pickup_longitude", new DoubleType());
		tableHandler.addColumn("pickup_latitude", new DoubleType());
		tableHandler.addColumn("RateCodeID", new ByteType());
		tableHandler.addColumn("store_and_fwd_flag", new StringType(1));
		tableHandler.addColumn("dropoff_longitude", new DoubleType());
		tableHandler.addColumn("dropoff_latitude", new DoubleType());
		tableHandler.addColumn("payment_type",  new ByteType());
		tableHandler.addColumn("fare_amount", new FloatType());
		tableHandler.addColumn("extra", new FloatType());
		tableHandler.addColumn("mta_tax", new FloatType());
		tableHandler.addColumn("tip_amount", new FloatType());
		tableHandler.addColumn("tolls_amount", new FloatType());
		tableHandler.addColumn("improvement_surcharge", new FloatType());
		tableHandler.addColumn("total_amount", new FloatType());*/
		
		
		//table = tableHandler.createTable();
		//tableHandler.clearDataDirectory();
		
		table.addColumn("tpep_pickup_datetime", new DateType()); //new StringType(19));//
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
		

		
		//table.clearDataDirectory();

		indexPickupDate = table.createIndex("tpep_pickup_datetime", IndexTreeDic.class);
		indexTripDistance = table.createIndex("trip_distance", IndexTreeDic.class);
		table.addIndex(new IndexTreeDic(table, 2));
		table.addIndex(new IndexTreeDic(table, 3));
		table.addIndex(new IndexTreeDic(table, 5));
		table.addIndex(new IndexTreeDic(table, 6));
		table.addIndex(new IndexTreeDic(table, 7));
		table.addIndex(new IndexTreeDic(table, 8));
		table.addIndex(new IndexTreeDic(table, 9));
		table.addIndex(new IndexTreeDic(table, 10));
		table.addIndex(new IndexTreeDic(table, 11));
		table.addIndex(new IndexTreeDic(table, 12));
		table.addIndex(new IndexTreeDic(table, 13));
		table.addIndex(new IndexTreeDic(table, 14));
		table.addIndex(new IndexTreeDic(table, 15));
		//tableHandler.createRuntimeIndexingColumn("trip_distance"); // indexer au moment du parse, et non via indexColumnWithTreeFromDisk("trip_distance");
		//tableHandler.createRuntimeIndexingColumn("tpep_dropoff_datetime"); // indexer au moment du parse, et non via indexColumnWithTreeFromDisk("trip_distance");
		//tableHandler.createRuntimeIndexingColumn("dropoff_longitude"); // indexer au moment du parse, et non via indexColumnWithTreeFromDisk("trip_distance");
		
		//tableHandler.createRuntimeIndexingColumn(1);
		/*tableHandler.createRuntimeIndexingColumn(2);
		tableHandler.createRuntimeIndexingColumn(3);
		tableHandler.createRuntimeIndexingColumn(4);
		tableHandler.createRuntimeIndexingColumn(5);
		tableHandler.createRuntimeIndexingColumn(6);
		tableHandler.createRuntimeIndexingColumn(7);
		tableHandler.createRuntimeIndexingColumn(8);
		tableHandler.createRuntimeIndexingColumn(9);
		tableHandler.createRuntimeIndexingColumn(10);*/
		
		if (parseAgain) {
			Timer parseTimer = new Timer("TEMPS TOTAL pris par le parsing");
			Log.info("Nombre de résultats/entrées parsés INIT : " + Loader.debugNumberOfEntriesWritten.get());
			//tableHandler.forceAppendNotFirstParsing();
			//tableHandler.parseCsvData("E:/L3 DANT disque E/yellow_tripdata_2015-06.csv", true);
			
			//tableHandler.parseCsvData("../SMALL_1_000_000_yellow_tripdata_2015-04.csv", true);
			//tableHandler.parseCsvData("E:/L3 DANT disque E/yellow_tripdata_2015-07.csv", true);
			
			/*
			Thread tPars1 = new Thread(() -> {
				try {
					tableHandler.parseCsvData("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);
					//tableHandler.parseCsvData("E:/L3 DANT disque E/yellow_tripdata_2015-07.csv", true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			Thread tPars2 = new Thread(() -> {
				try {
					tableHandler.parseCsvData("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);
					//tableHandler.parseCsvData("E:/L3 DANT disque E/yellow_tripdata_2015-08.csv", true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			Thread tPars3 = new Thread(() -> {
				try {
					tableHandler.parseCsvData("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);
					//tableHandler.parseCsvData("../SMALL_1_000_000_yellow_tripdata_2015-04.csv", true);
					//tableHandler.parseCsvData("E:/L3 DANT disque E/yellow_tripdata_2015-09.csv", true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			Thread tPars4 = new Thread(() -> {
				try {
					tableHandler.parseCsvData("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);
					//tableHandler.parseCsvData("E:/L3 DANT disque E/yellow_tripdata_2015-10.csv", true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			tPars1.start();
			tPars2.start();
			tPars3.start();
			tPars4.start();
			
			tPars1.join();
			tPars2.join();
			tPars3.join();
			tPars4.join();*/
			
			
			String basePath = "E:/L3 DANT disque E/csv/";
			
			//tableHandler.multiThreadParsingAddAndStartCsv(basePath + "yellow_tripdata_2015-01.csv", true);
			//tableHandler.multiThreadParsingAddAndStartCsv(basePath + "yellow_tripdata_2015-02.csv", true);
			/*tableHandler.multiThreadParsingAddAndStartCsv(basePath + "yellow_tripdata_2015-03.csv", true);
			tableHandler.multiThreadParsingAddAndStartCsv(basePath + "yellow_tripdata_2015-04.csv", true);
			tableHandler.multiThreadParsingAddAndStartCsv(basePath + "yellow_tripdata_2015-05.csv", true);
			tableHandler.multiThreadParsingAddAndStartCsv(basePath + "yellow_tripdata_2015-06.csv", true);
			tableHandler.multiThreadParsingAddAndStartCsv(basePath + "yellow_tripdata_2015-07.csv", true);
			tableHandler.multiThreadParsingAddAndStartCsv(basePath + "yellow_tripdata_2015-08.csv", true);
			tableHandler.multiThreadParsingAddAndStartCsv(basePath + "yellow_tripdata_2015-09.csv", true);
			tableHandler.multiThreadParsingAddAndStartCsv(basePath + "yellow_tripdata_2015-10.csv", true);
			tableHandler.multiThreadParsingAddAndStartCsv(basePath + "yellow_tripdata_2015-11.csv", true);
			tableHandler.multiThreadParsingAddAndStartCsv(basePath + "yellow_tripdata_2015-12.csv", true);
			*/
			
			
			// Loader de la donnée en multi-thread
			//for (int i = 0; i < 20; i++)
			
			//table.multiThreadParsingAddAndStartCsv("E:/L3 DANT disque E/csv/yellow_tripdata_2015-07.csv", true);
			
			for (int i = 0; i < 4; i++)
				table.multiThreadParsingAddAndStartCsv("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);
			
			/*table.multiThreadParsingAddAndStartCsv("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);
			table.multiThreadParsingAddAndStartCsv("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);
			table.multiThreadParsingAddAndStartCsv("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);
			table.multiThreadParsingAddAndStartCsv("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);*/

			/*table.multiThreadParsingAddAndStartCsv("E:/L3 DANT disque E/csv/yellow_tripdata_2015-07.csv", true);
			table.multiThreadParsingAddAndStartCsv("E:/L3 DANT disque E/csv/yellow_tripdata_2015-08.csv", true);
			table.multiThreadParsingAddAndStartCsv("E:/L3 DANT disque E/csv/yellow_tripdata_2015-09.csv", true);*/
			
			// Attendre que toute la donnée soit parsée
			table.multiThreadParsingJoinAllThreads();
			
			/**
			 * Ne supporte pour l'instant pas recherches + indexing en même temps. (Problèmes de concurrence)
			 * -> BIEN penser à faire tableHandler.multiThreadParsingJoinAllThreads();
			 */
			
			
			//for (int i = 0; i < 6; i++)
			//tableHandler.parseCsvData("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);
			/*tableHandler.parseCsvData("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);
			tableHandler.parseCsvData("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);*/

			//tableHandler.parseCsvData("E:/L3 DANT disque E/yellow_tripdata_2015-07.csv", true);
			//tableHandler.parseCsvData("E:/L3 DANT disque E/yellow_tripdata_2015-08.csv", true);
			//tableHandler.parseCsvData("E:/L3 DANT disque E/yellow_tripdata_2015-09.csv", true);
			//tableHandler.parseCsvData("E:/L3 DANT disque E/yellow_tripdata_2015-10.csv", true);
			//Log.info("Nombre de résultats/entrées parsés FINAL : " + Loader.debugNumberOfEntriesWritten.get());
			
			
			/**/
			/*tableHandler.parseCsvData("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);
			tableHandler.parseCsvData("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);
			tableHandler.parseCsvData("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);
			tableHandler.parseCsvData("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);*/
			
			//tableHandler.parseCsvData("../SMALL_1_000_000_yellow_tripdata_2015-04.csv", true);
			/*tableHandler.parseCsvData("../SMALL_1_000_000_yellow_tripdata_2015-04.csv", true);
			tableHandler.parseCsvData("../SMALL_1_000_000_yellow_tripdata_2015-04.csv", true);
			tableHandler.parseCsvData("../SMALL_1_000_000_yellow_tripdata_2015-04.csv", true);*/
			
			
			/*tableHandler.parseCsvData("E:/L3 DANT disque E/yellow_tripdata_2015-07.csv", true);
			tableHandler.parseCsvData("E:/L3 DANT disque E/yellow_tripdata_2015-08.csv", true);
			tableHandler.parseCsvData("E:/L3 DANT disque E/yellow_tripdata_2015-09.csv", true);
			tableHandler.parseCsvData("E:/L3 DANT disque E/yellow_tripdata_2015-10.csv", true);
			tableHandler.parseCsvData("E:/L3 DANT disque E/yellow_tripdata_2015-11.csv", true);
			tableHandler.parseCsvData("E:/L3 DANT disque E/yellow_tripdata_2015-12.csv", true);*/
			//tableHandler.parseCsvData("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv", true);
			//tableHandler.parseCsvData("../SMALL_1_000_000_yellow_tripdata_2015-04.csv", true);
			// tableHandler.parseCsvData("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv"); Fichiers identiques, donc 2 fois plus de résultats !
			parseTimer.log();
		}
		//table.flushEveryIndexOnDisk();


		// -> Go faire le parsing multi-thread maintenant !!
		// Nécessaire d'avoir plusieurs fichiers, à voir plus tard.
		
		Log.info("setUpBeforeAll OK");
		Database.getInstance().getAllTables().add(table);
		SerialStructure.writeStructureTo(serializeFromPath);
		
		/*if (tableHandler.associatedTable == null) {
			Log.error("tableHandler.associatedTable == null");
		} else {
			Log.error("tableHandler.associatedTable != null");
			
		}*/
		
		//setUpBeforeAll();
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
	@Deprecated // IndexTreeDic n'est plus utilisé, remplacé par IndexMemDic
	void testIndexTreeDic() throws Exception {
		//if (true) return;
		/**
		 * Note : c'est super le bordel ici, je vais ranger ça ^^'
		 */
		//SIndexingTreeFloat indexingFoat = new SIndexingTreeFloat();
		Log.info("Lancé");
		
		// Index the column on indexTripDistance 4
		//int indexingColumnIndex = 3; // passanger count
		//int indexingColumnIndex = 4; // trip distance
		//int indexingColumnIndex = 5; // latitude
		int indexingColumnIndex = 1; // date pickup
		
		if (doItWithTableHandler) {

			/*Timer localTimer = new Timer("Temps pris pour indexer tpep_pickup_datetime");
			tableHandler.indexColumnWithTreeFromDisk("tpep_pickup_datetime");
			tableHandler.indexColumnWithTreeFromDisk("tpep_dropoff_datetime");
			tableHandler.indexColumnWithTreeFromDisk("trip_distance");
			tableHandler.indexColumnWithTreeFromDisk("pickup_longitude");
			tableHandler.indexColumnWithTreeFromDisk("pickup_latitude");
			localTimer.log();*/
			//tableHandler.indexColumnWithTreeFromDisk("trip_distance");
			
			
			String stringDateFrom = "2015-04-05 00:00:00";//"2015-04-04 00:01:00";//
			String stringDateTo = "2015-04-05 03:20:00";//"2015-04-04 00:18:57";//
			
			Date dateFrom = currentlyUsedUils.dateFromString(stringDateFrom);
			Date dateTo = currentlyUsedUils.dateFromString(stringDateTo);
			int intDateFrom = Utils.dateToSecInt(dateFrom);
			int intDateTo = Utils.dateToSecInt(dateTo);
			
			/*Object searchFromValue = new Float(12.78641);
			Object searchToValue = new Float(14.748621);*/
			
			
			Object searchFromValue = intDateFrom;//stringDateFrom;//
			Object searchToValue = intDateTo;//stringDateTo;//
			
			Timer searchQueryTimer = new Timer("Temps total recherche"); // "Time took to return the matching elements" : flemme d'écrire en anglais
			
			Column column = table.getColumns().get(1);
			Predicate predicate = new Predicate(table, column, Operator.between, new Object[] {searchFromValue, searchToValue});
			Collection<DiskDataPosition> result = indexPickupDate.getPositionsFromPredicate(predicate);
			
			
			searchQueryTimer.log();
			//trip_distance
			
			Timer searchQueryFullTimer = new Timer("Temps parcours des résultats");
			int numberOfResults = result.size();
			if (enableCsvRelatedTests) assertEquals(3116, numberOfResults);
			//int numberOfResults = tableHandler.evaluateNumberOfResults(result);
			//int numberOfLines = tableHandler.evaluateNumberOfArrayListLines(result);
			searchQueryFullTimer.log();
			Log.info("Nombre de résultats = " + numberOfResults);
			//Log.info("Nombre de lignes = " + numberOfLines);

			
			searchQueryTimer = new Timer("Temps total recherche");
			column = table.getColumns().get(4);
			predicate = new Predicate(table, column, Operator.between, new Float[] {17.78f, 18f});
			result = indexTripDistance.getPositionsFromPredicate(predicate);
			searchQueryTimer.log();
			
			
			searchQueryFullTimer = new Timer("1Temps d'acquisition des résultats (chargement du disque de tous les champs)");
			numberOfResults = result.size();// tableHandler.evaluateNumberOfResults(result);
			if (enableCsvRelatedTests) assertEquals(700, numberOfResults);
			//numberOfLines = tableHandler.evaluateNumberOfArrayListLines(result);
			
			//ArrayList<ArrayList<Object>>
			ResultSet fullResulsVariables = table.getFullResultsFromBinIndexes(result, true, -1, null);
			
			//tableHandler.displayOnLogResults(fullResulsVariables);
			
			
			searchQueryFullTimer.log();

			searchQueryFullTimer = new Timer("2Temps d'acquisition des résultats certains champs seulement");
			ArrayList<Integer> onlyThoseColumns = new ArrayList<Integer>();
			onlyThoseColumns.add(6);
			onlyThoseColumns.add(4);
			fullResulsVariables = table.getFullResultsFromBinIndexes(result, true, -1, onlyThoseColumns);
			
			//tableHandler.displayOnLogResults(fullResulsVariables);
			
			searchQueryFullTimer.log();
			Log.info("Nombre de résultats = " + numberOfResults);

			predicate = new Predicate(table, column, Operator.equals, 18f);
			result = indexTripDistance.getPositionsFromPredicate(predicate);
			numberOfResults = result.size();
			if (enableCsvRelatedTests) assertEquals(116, numberOfResults);
			Log.info("Nombre de résultats (pour 18 exact) = " + numberOfResults);
			
			
			//Log.info("Nombre de lignes = " + numberOfLines);
			
			
			
			
			
		} else {
		
			
			Column indexThisColumn = table.getColumns().get(indexingColumnIndex);
			
			//System.out.println("OUOUOUOU " + indexThisColumn.minValue + "  " +  indexThisColumn.maxValue);
			
			//IndexTreeCeption indexingObject = new IndexTreeCeption(0, null, indexThisColumn.minValue, indexThisColumn.maxValue);
			IndexTreeDic indexingObject = new IndexTreeDic(table, indexingColumnIndex);//Integer.class);
			
			
			//indexingObject.initializeMaxDistanceBetweenElementsArray(indexThisColumn.minValue, indexThisColumn.maxValue);
			
			// Index the column from the disk
			// -> reading fron the disk is quite slow
			// --> a very cool optimization will be to indexTripDistance a bunch of columns at the same time
			Timer loadFromDiskTimer = new Timer("Time took to indexTripDistance this column, from disk");
			MemUsage.printMemUsage();
			indexingObject.indexColumnFromDisk(table, indexingColumnIndex);
			MemUsage.printMemUsage();
			loadFromDiskTimer.log();
			
			// Ecriture sur le disque
			Timer writeIndexToDiskTimer = new Timer("Temps pris pour l'écriture sur disque");
			//indexingObject.flushOnDisk();
			//indexingObject.saveOnDisk(false); // première sauvegarde, écraser ce qui existe déjà
			writeIndexToDiskTimer.log();
			
			
			Log.info("Fini");
			Log.info("OBJECT RESULT :");
			
			// Get the query result
			Collection<DataPositionList> result;
			MemUsage.printMemUsage();
			
			String stringDateFrom = "2015-04-04 00:00:00";//"2015-04-04 00:01:00";//
			String stringDateTo = "2015-04-04 03:20:00";//"2015-04-04 00:18:57";//
			
			Date dateFrom = currentlyUsedUils.dateFromString(stringDateFrom);
			Date dateTo = currentlyUsedUils.dateFromString(stringDateTo);
			int intDateFrom = Utils.dateToSecInt(dateFrom);
			int intDateTo = Utils.dateToSecInt(dateTo);
			
			/*Object searchFromValue = new Float(12.78641);
			Object searchToValue = new Float(14.748621);*/
	
			
			Object searchFromValue = intDateFrom;//stringDateFrom;//
			Object searchToValue = intDateTo;//stringDateTo;//
			
			
			// à faire : supprimer la recherche en mémoire, il n'y a plus rien en mémoire.
			result = indexingObject.findMatchingBinIndexesFromMemory(searchFromValue, searchToValue, true); // new Float(20), new Float(21)
			
			//result = indexingObject.findMatchingBinIndexesInMemory(intDateFrom, intDateTo, true);
			
			//result = indexingObject.findMatchingBinIndexes(new Integer(-1000), new Integer(1000), true);
	
			
			Timer searchQueryTimer = new Timer("Time took to return the matching elements");
			Timer searchQueryFullTimer = new Timer("Time took to return the matching elements + size evaluation");
			
			//result = indexingObject.findMatchingBinIndexes(new Byte((byte)0), new Byte((byte)100), true);
			//result = indexingObject.findMatchingBinIndexes(new Double(0), new Double(0), true);
			
			
			MemUsage.printMemUsage();
			searchQueryTimer.log();
			Column indexingColumn = table.getColumns().get(indexingColumnIndex);
			
			// Iterates over all the results
			int numberOfResults = 0, numberOfLines = 0;
			for (DataPositionList list : result) {
				//Log.info("list size = " + list.size());
				numberOfResults += list.size();
				numberOfLines++;
				/*if (false)
				for (Long indexTripDistance : list) {
					// un-comment those lines if you want to get the full info on lines : List<Object> objList = table.getValuesOfLineById(indexTripDistance);
					//Log.info("  indexTripDistance = " + indexTripDistance);
					List<Object> objList = table.getValuesOfLineById(indexTripDistance);
					Object indexedValue = objList.get(indexingColumnIndex);
					//indexingColumn.getDataType().
					Log.info("  valeur indexée = " + indexedValue);
					//Log.info("  objList = " + objList);
					
				}*/
			}
			searchQueryFullTimer.log();
			Log.info("Number of results = " + numberOfResults);
			Log.info("Number of lines = " + numberOfLines);
			
			
			Log.info("Depuis le disque : ");
			Timer searchFromDiskTimer = new Timer("Temps pris pour la recherche du disque");
	
			result = indexingObject.findMatchingBinIndexesFromDisk(searchFromValue, searchToValue, true, false);
			//result = indexingObject.findMatchingBinIndexesFromDisk(intDateFrom, intDateTo, true);
			searchFromDiskTimer.log();
			
			boolean showAllResults = false;
			
			// Iterates over all the results
			numberOfResults = 0;
			numberOfLines = 0;
			for (DataPositionList list : result) {
				//Log.info("list size = " + list.size());
				numberOfResults += list.size();
				numberOfLines++;
				//Log.info("Line("+numberOfLines+") : nb=" + list.size());
				
				/*if (showAllResults) {
					for (Long indexTripDistance : list) {
						// un-comment those lines if you want to get the full info on lines : List<Object> objList = table.getValuesOfLineById(indexTripDistance);
						List<Object> objList = table.getValuesOfLineById(indexTripDistance);
						Object indexedValue = objList.get(indexingColumnIndex);
						Log.info("  indexTripDistance = " + indexTripDistance + "   val = " + indexedValue);
						
						//Log.info("  valeur indexée = " + indexedValue);
						//Log.info("  objList = " + objList);
						
					}
				}*/
			}
			Log.info("Number of results = " + numberOfResults);
			Log.info("Number of lines = " + numberOfLines);
		}
	}
	
	//@Test
	@Deprecated // IndexTreeCeption n'est plus utilisé, remplacé par IndexTreeDic puis IndexMemDic
	void testIndexTreeCeption() throws IOException {
		//if (true) return;
		/**
		 * Note : c'est super le bordel ici, je vais ranger ça ^^'
		 */
		//SIndexingTreeFloat indexingFoat = new SIndexingTreeFloat();
		Log.info("Lancé");
		
		// Index the column on indexTripDistance 4
		//int indexingColumnIndex = 3; // passanger count
		//int indexingColumnIndex = 4; // trip distance
		//int indexingColumnIndex = 5; // latitude
		int indexingColumnIndex = 1; // date pickup
		
		Column indexThisColumn = table.getColumns().get(indexingColumnIndex);
		
		//System.out.println("OUOUOUOU " + indexThisColumn.minValue + "  " +  indexThisColumn.maxValue);
		
		IndexTreeCeption indexingObject = new IndexTreeCeption(0, null, indexThisColumn.getMinValue(), indexThisColumn.getMaxValue());
		//IndexTreeDic indexingObject = new IndexTreeDic();//Integer.class);
		
		
		//indexingObject.initializeMaxDistanceBetweenElementsArray(indexThisColumn.minValue, indexThisColumn.maxValue);
		
		// Index the column from the disk
		// -> reading fron the disk is quite slow
		// --> a very cool optimization will be to indexTripDistance a bunch of columns at the same time
		Timer loadFromDiskTimer = new Timer("Time took to indexTripDistance this column, from disk");
		MemUsage.printMemUsage();
		indexingObject.indexColumnFromDisk(table, indexingColumnIndex);
		MemUsage.printMemUsage();
		loadFromDiskTimer.log();
		
		// Ecriture sur le disque
		Timer writeIndexToDiskTimer = new Timer("Temps pris pour l'écriture sur disque");
		//indexingObject.flushOnDisk();
		//indexingObject.saveOnDisk(false); // première sauvegarde, écraser ce qui existe déjà
		writeIndexToDiskTimer.log();
		
		
		Log.info("Fini");
		Log.info("OBJECT RESULT :");
		
		// Get the query result
		Collection<IntegerArrayList> result;
		MemUsage.printMemUsage();
		
		
		Date dateFrom = currentlyUsedUils.dateFromString("2015-04-04 00:01:00");
		Date dateTo = currentlyUsedUils.dateFromString("2015-04-04 00:18:57");
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
		searchQueryTimer.log();
		
		// Iterates over all the results
		int numberOfResults = 0, numberOfLines = 0;
		for (IntegerArrayList list : result) {
			//Log.info("list size = " + list.size());
			numberOfResults += list.size();
			numberOfLines++;
			for (Integer index : list) {
				// un-comment those lines if you want to get the full info on lines : List<Object> objList = table.getValuesOfLineById(indexTripDistance);
				/*Log.info("  indexTripDistance = " + indexTripDistance);
				List<Object> objList = table.getValuesOfLineById(indexTripDistance);
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

		result = indexingObject.findMatchingBinIndexesFromDisk(searchFromValue, searchToValue, true);
		//result = indexingObject.findMatchingBinIndexesFromDisk(intDateFrom, intDateTo, true);
		searchFromDiskTimer.log();
		
		boolean showAllResults = false;
		
		// Iterates over all the results
		numberOfResults = 0;
		numberOfLines = 0;
		for (IntegerArrayList list : result) {
			//Log.info("list size = " + list.size());
			numberOfResults += list.size();
			numberOfLines++;

			if (showAllResults) {
				for (Integer index : list) {
					// un-comment those lines if you want to get the full info on lines : List<Object> objList = table.getValuesOfLineById(indexTripDistance);
					List<Object> objList = table.getValuesOfLineById(index);
					Object indexedValue = objList.get(indexingColumnIndex);
					Log.info("  indexTripDistance = " + index + "   val = " + indexedValue);
					
					//Log.info("  valeur indexée = " + indexedValue);
					//Log.info("  objList = " + objList);
					
				}
			}
		}
		Log.info("Number of results = " + numberOfResults);
		Log.info("Number of lines = " + numberOfLines);
		
	}

	@Test
	// à appliquer au nouvel index car IndexTreeDic n'est plus utilisé, remplacé par IndexMemDic
	void executeView() {
		if (enableCsvRelatedTests == false) return;
		Column column1 = table.getColumns().get(1);
		Column column4 = table.getColumns().get(4);
		String stringDateFrom = "2015-04-05 00:00:00";//"2015-04-04 00:01:00";//
		String stringDateTo = "2015-04-06 00:00:00";//"2015-04-04 00:18:57";//
		Date dateFrom = currentlyUsedUils.dateFromString(stringDateFrom);
		Date dateTo = currentlyUsedUils.dateFromString(stringDateTo);
		int intDateFrom = Utils.dateToSecInt(dateFrom);
		int intDateTo = Utils.dateToSecInt(dateTo);

		Predicate predicate1 = new Predicate(table, column4, Operator.equals, new Float("17.80"));
		Predicate predicate2 = new Predicate(table, column4, Operator.between, new Float[] {17.78f, 18f});
		Predicate predicate3 = new Predicate(table, column4, Operator.equals, new Float("17"));
		Predicate predicate4 = new Predicate(table, column1, Operator.between, new Object[] {intDateFrom, intDateTo});

		FilterTerm filter1 = new FilterGroup(JoinMethod.or, new Predicate[] {predicate1, predicate2});
		FilterTerm filter2 = new FilterGroup(JoinMethod.and, new Predicate[] {predicate2, predicate3});
		FilterTerm filter3 = new FilterGroup(JoinMethod.and, new Predicate[] {predicate2, predicate4});
		FilterTerm filter4 = new FilterGroup(JoinMethod.or, new Predicate[] {predicate2, predicate3});
		View view1 = new View(table, predicate1);
		View view2 = new View(table, predicate2);
		View view3 = new View(table, filter1);
		View view4 = new View(table, filter2);
		View view5 = new View(table, filter3);
		View view6 = new View(table, filter4);

		try {
			ResultSet result;
			result = view1.execute();
			assertEquals(140, result.size());
			Log.info("executed view1");
			result = view2.execute();
			assertEquals(700, result.size());
			Log.info("executed view2");
			result = view3.execute();
			assertEquals(700, result.size());
			Log.info("executed view3");
			result = view4.execute();
			assertEquals(0, result.size());
			Log.info("executed view4");
			result = view5.execute();
			assertEquals(24, result.size());
			Log.info("executed view5");
			result = view6.execute();
			assertEquals(816, result.size());
			Log.info("executed view6");
		} catch (SearchException e) {
			Log.error(e);
			throw new AssertionError(e);
		}
	}
	
	@AfterAll
	@Deprecated // IndexTreeDic n'est plus utilisé, remplacé par IndexMemDic
	static void tearDown() {
		
	}
}
