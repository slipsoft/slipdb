package index.memDic;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import db.data.load.ParallelLoader;
import org.junit.jupiter.api.Test;

import com.dant.utils.Log;
import com.dant.utils.Timer;

import db.data.load.CsvParser;
import db.data.types.ByteType;
import db.data.types.DateType;
import db.data.types.DoubleType;
import db.data.types.FloatType;
import db.data.types.StringType;
import db.structure.Table;
import index.memDic.IndexMemDic;
//import index.memDic.IndexMemDicCh;

/** 
 *  Tests pour le stockage des positions dans un index.
 *  Stockage par blocs/chunks.
 *  
 */

public class IndexMemDicStorageTests {
	
	/** IMPORTANT - Info imortante :
	 *  Ce moteur d'index tire parti des performances de la machine sur laquelle il est :
	 *  Le programme peut très bien exploiter totalement un SDD et plusieurs coeurs de processeur.
	 *  Les performances s'adaptent donc au matériel (parsing d'un même fichier en multi-thread par exemple)
	 * @throws Exception 
	 */
	
	//@Test
	public void mainTestFirst() throws Exception {
		// Parsing et mise en mémoire vive des CSV à charger
		// Pas de seuvagerde sur disque, pour l'instant
		loadFirst();
		
		// Création de l'index sur les colonnes (d'index) 3 et 4 de la table "table".
		IndexMemDic indexDic = new IndexMemDic(table, new int[]{3, 4}); // passenger_count et trip_distance
		// Etape nécessaire à la réalisation de la dichotomie : classement des lignes indexées par ordre croissant (en fonction des valeurs indexées)
		Timer tim = new Timer("Temps pris pour classer la 1ère fois");
		indexDic.sortAllv1();
		tim.log();
		tim = new Timer("Temps pris pour classer la 2ème fois");
		indexDic.sortAllv1();
		tim.log();
		
		/*if (indexDic.testSortedPositionsChunks() == false)
			throw new Exception("Erreur lors du test indexMemDic.testSortedPositionsChunks()");
		
		indexDic.benchmarkSortedPositionsChunks();*/
		
	}
	
	private void doSpeedBenchmark() {
		
		
		
	}
	
	@Test
	public void mainTest() throws Exception {
		
		// Parsing et mise en mémoire vive des CSV à charger
		// Pas de sauvagerde sur disque, pour l'instant
		loadFirst();
		
		// Création de l'index sur les colonnes (d'index) 3 et 4 de la table "table".
		IndexMemDic indexDic = new IndexMemDic(table, new int[]{3, 4}); // passenger_count et trip_distance
		// Etape nécessaire à la réalisation de la dichotomie : classement des lignes indexées par ordre croissant (en fonction des valeurs indexées)
		Timer tim = new Timer("Temps pris pour classer la 1ère fois");
		indexDic.sortAllv1();
		
		
		tim.log();
		tim = new Timer("Temps pris pour classer la 2ème fois");
		indexDic.sortAllv1();
		tim.log();
		
		// Faire une requête sur l'index
		// Trois manières possibles :
		
		// A) Soit via un ByteBuffer :
		ByteBuffer searchQuery = ByteBuffer.allocate(100);
		searchQuery.rewind();
		searchQuery.put((byte)1);
		searchQuery.putFloat(4);
		
		// B) Soit via une liste simple d'objets, le cast sera réalisé
		ArrayList<Object> searchQueryAsList_doCast = new ArrayList<Object>();
		searchQueryAsList_doCast.add(new Double(1));
		searchQueryAsList_doCast.add(new Double(4));
		
		// C) Soit via une liste simple d'objets, directement dans le bon type
		ArrayList<Object> searchQueryAsList_noCast = new ArrayList<Object>();
		searchQueryAsList_noCast.add(new Byte((byte) 1));
		searchQueryAsList_noCast.add(new Float(4));
		
		int[] resultsPositionsArrayA = indexDic.findMatchingLinePositions(searchQuery); // les positions des lignes de résultat, réelles
		Log.info("Recherche A OK ! Nb résultats selon A : " + resultsPositionsArrayA.length);
		
		int[] resultsPositionsArrayB = indexDic.findMatchingLinePositions(searchQueryAsList_doCast, false); // les positions des lignes de résultat, réelles
		Log.info("Recherche B OK ! Nb résultats selon B : " + resultsPositionsArrayB.length);
		
		int[] resultsPositionsArrayC = indexDic.findMatchingLinePositions(searchQueryAsList_noCast, true); // les positions des lignes de résultat, réelles
		Log.info("Recherche C OK ! Nb résultats selon C : " + resultsPositionsArrayC.length);
		
		boolean errorOccured = false;
		Log.info("resultsPositionsArray.length = " + resultsPositionsArrayA.length);
		if (resultsPositionsArrayA.length != resultsPositionsArrayB.length) errorOccured = true;
		if (resultsPositionsArrayA.length != resultsPositionsArrayC.length) errorOccured = true;
		
		if (errorOccured) {
			Exception error = new Exception("Résultats différents en fonction de la manière dont la recherche est faite !"
					+ "resultsPositionsArrayA.length = " + resultsPositionsArrayA.length
					+ "resultsPositionsArrayB.length = " + resultsPositionsArrayB.length
					+ "resultsPositionsArrayC.length = " + resultsPositionsArrayC.length);
			Log.error(error);
			throw error;
		}
		
	}
	
	
	
	//private int limitResultNb = 20_000;
	Table table; // Table utilisée par tous les index
	
	private int parsingTimeLimitSec = -1; // S'il faut stopper le parsing tôt (limite de temps, 5 mins à la soutenance par exemple)
	private Timer limitParsingTimeTimer;  // Timer permettant d'évaluer le temps déjà écoulé
	
	/** Chargement et parsing des données à indexer, plus tard
	 *  @throws Exception
	 */
	public void loadFirst() throws Exception {
		Log.start("indexingTreeTest", 3);
		
		table = new Table("NYtest");
		
		table.addColumn("VendorID", new ByteType());
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
		//table.debugInitTheStringColumn();
		
		Timer parseTimer = new Timer("TEMPS TOTAL PRIS PAR TOUS LES PARSINGS");
		
		ParallelLoader csvLoader = new ParallelLoader(table, new CsvParser());
		
		limitParsingTimeTimer = new Timer("");
		
		/* Paring de l'irdi de Sylvain, pour des tests plus complets*/
		//doSylvainParsing(csvLoader);
		
		/* Parsing générique, de gitHub*/
		String csvPath = "testdata/SMALL_100_000_yellow_tripdata_2015-04.csv";
		Log.info("Parsing de csvName = " + csvPath);
		parseThisCsv(table, csvLoader, csvPath);
		
		parseTimer.log();
		
		
	}
	
	/** Racourci pour ne pas avoir à mettre ces trois lignes à) chaque fois dans la boucle de loadFirst()
	 *  @param table  la table courante
	 *  @param csvLoader  le csvLoader à utiliser (un par thread, si parsing de plusieurs fichiers simultanément
	 *                                               -> c'est une mauvaise idée, le disque devient limitant !)
	 *  @param csvPath  chemin vers le CSV à parser (relatif ou absolu)
	 *  @throws IOException  erreur retournée si l'accès au CSV est impossible
	 */
	private void parseThisCsv(Table table, ParallelLoader csvLoader, String csvPath) throws IOException {
		InputStream csvStream = new FileInputStream(csvPath);
		csvLoader.parse(csvStream, true, limitParsingTimeTimer, parsingTimeLimitSec);
		csvStream.close();
	}
	
	
	
	/** Préparation de la requête commune aux index (via un ByteBuffer)
	 *  Il doit y avoir 263 résultats sur le jeu de test de 100_000 lignes (sur testdata/SMALL_100_000_yellow_tripdata_2015-04.csv)
	 *  pour cette requête, avec (1, 4) en paramètre (voir le corps de la fonction)
	 *  @return la requête simple à adresser directement aux index, sous la forme d'un ByteBuffer
	 */
	private ByteBuffer getBufferedQuery() {
		ByteBuffer searchQuery = ByteBuffer.allocate(100);
		searchQuery.rewind();
		searchQuery.put((byte)1);
		searchQuery.putFloat(4);
		return searchQuery;
	}
	
	private void doSylvainParsing(ParallelLoader csvLoader) throws IOException {
		// Manière plus complète de faire, pour charger plusieurs CSV :
		int mounthFinalCount = 1;
		parsingTimeLimitSec = 20;
		for (int iCsv = 1; iCsv <= mounthFinalCount; iCsv++) {
			String colNumber = String.format("%02d" , iCsv);
			//String csvPath = "testdata/SMALL_100_000_yellow_tripdata_2015-04.csv";
			String csvPath = "F:/csv/yellow_tripdata_2015-" + colNumber + ".csv"; // E:/L3 DANT disque E
			//String csvPath = "C:/Users/admin/Desktop/L3 DANT Jussieu/Web_Olivier/yellow_tripdata_2015-" + colNumber + ".csv"; // E:/L3 DANT disque E
			
			//String csvPath = "F:/csv/SMALL_1_000_000_yellow_tripdata_2015-04.csv";
			Log.info("Parsing de csvName = " + csvPath);
			parseThisCsv(table, csvLoader, csvPath);
		}
		
		
		
	}
	
}


