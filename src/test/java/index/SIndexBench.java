package index;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.dant.utils.Log;
import com.dant.utils.MemUsage;
import com.dant.utils.Timer;

import db.data.load.CsvParser;
import db.data.types.ByteType;
import db.data.types.DateType;
import db.data.types.DoubleType;
import db.data.types.FloatType;
import db.data.types.StringType;
import db.newLoader.SCsvLoader;
import db.newLoader.SIndexHashJava;
import db.structure.Column;
import db.structure.Table;
import index.memDic.IndexMemDic;

public class SIndexBench {
	
	
	/** IMPORTANT - Info imortante :
	 *  Ce moteur d'index tire parti des performances de la machine sur laquelle il est :
	 *  Le programme peut très bien exploiter totalement un SDD et plusieurs coeurs de processeur.
	 *  Les performances s'adaptent donc au matériel (parsing d'un même fichier en multi-thread par exemple)
	 */
	
	//private int limitResultNb = 20_000;
	Table table; // Table utilisée par tous les index
	
	private int parsingTimeLimitSec = -1; // S'il faut stopper le parsing tôt (limite de temps, 5 mins à la soutenance par exemple)
	private Timer limitParsingTimeTimer;  // Timer permettant d'évaluer le temps déjà écoulé
	
	
	/** Test de load et d'index sur une HashMap classique et IndexMemDic, index en mémoire par dichotomie.
	 *  Toutes les colonnes indexées par un index doivent être présentes en mémoire pour qu'il soit assez rapide
	 *  (i.e. pas que sur disque)
	 *  Attention, pour le bench, il peut y avoir des nombres négatifs si la volumétrie (nombre de lignes parsées) est faible,
	 *  c'est parce que les tests de mémoire deviennent inexacts quand il y a trop peu de mémoire allouée.
	 *  
	 *  @throws Exception  si une exception survient (rédigé ainsi pour aller plus vite)
	 */
	@Test
	public void mainTest() throws Exception {
		
		long memInit, memFinal;
		String memUsedStr1, memUsedStr2, memUsedStr3;
		String info1, info2, info3;
		Timer tim1, tim2, tim3;
		
		// Chargement des CSV
		System.gc();
		memInit = MemUsage.getMemUsage();
		tim1 = new Timer("Temps pris loadFirst");
		
		loadFirst();
		System.gc();
		
		memFinal = MemUsage.getMemUsage();
		memUsedStr1 = MemUsage.formatMemUsage(memFinal - memInit);
		info1 = "MEM MEM MEM Mémoire utilisée LoadFirst : " + memUsedStr1 + "  en  " + tim1.pretty();
		
		Log.warning(info1);
		System.gc();
		
		// ----------- Avec IndexMemDic ----------- 
		System.gc();
		memInit = MemUsage.getMemUsage();
		tim2 = new Timer("Temps pris testIndexMemDic");
		
		testIndexMemDic();
		System.gc();
		
		memFinal = MemUsage.getMemUsage();
		memUsedStr2 = MemUsage.formatMemUsage(memFinal - memInit);
		info2 = "MEM MEM MEM Mémoire utilisée IndexMemDic : " + memUsedStr2 + "  en  " + tim2.pretty();
		
		Log.warning(info2);

		if (theIndexMemDic.totalLength == 78) // garder la réf
			Log.info("Hey !");
		
		
		
		
		// ----------- Avec IndexHash ----------- 
		System.gc();
		memInit = MemUsage.getMemUsage();
		tim3 = new Timer("Temps pris testIndexHash");
		
		testIndexHash();
		System.gc();
		
		memFinal = MemUsage.getMemUsage();
		memUsedStr3 = MemUsage.formatMemUsage(memFinal - memInit);
		info3 = "MEM MEM MEM Mémoire utilisée IndexHash : " + memUsedStr3 + "  en  " + tim3.pretty();
		
		Log.warning(info3);
		
		if (theIndexHash.myNumberToKeepRef == 7865241) { // get(new byte[] {0}) == null
			Log.info("Houyy !");
		}
		
		ArrayList<String> infoMessagesList = new ArrayList<String>();
		
		// Affichage des résultats du benchmark
		// -> Attention, pour de faibles volumétries (moins d'un CSV
		Log.infoOnlySimple("");
		Log.infoOnlySimple("");
		Log.infoOnlySimple("");
		Log.warning(info1);
		Log.warning(info2);
		Log.warning(info3);
		
		for (int iMsg = 0; iMsg < infoMessagesList.size(); iMsg++) {
			Log.warning(infoMessagesList.get(iMsg));
		}
		
	}
	
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
		
		System.gc();
		MemUsage.printMemUsage("Mem usage  début - ");
		SCsvLoader csvLoader = new SCsvLoader(table, new CsvParser());
		
		limitParsingTimeTimer = new Timer("");
		
		int mounthFinalCount = 1;
		for (int iCsv = 1; iCsv <= mounthFinalCount; iCsv++) {
			String colNumber = String.format("%02d" , iCsv);
			String csvPath = "testdata/SMALL_100_000_yellow_tripdata_2015-04.csv";
			//String csvPath = "F:/csv/yellow_tripdata_2015-" + colNumber + ".csv"; // E:/L3 DANT disque E
			//String csvPath = "C:/Users/admin/Desktop/L3 DANT Jussieu/Web_Olivier/yellow_tripdata_2015-" + colNumber + ".csv"; // E:/L3 DANT disque E
			
			//String csvPath = "F:/csv/SMALL_1_000_000_yellow_tripdata_2015-04.csv";
			Log.info("Parsing de csvName = " + csvPath);
			parseThisCsv(table, csvLoader, csvPath);
		}
		
		//System.gc();
		//MemUsage.printMemUsage("Mem usage  fin - ");
		parseTimer.log();
		
		
		
	}
	
	/** Racourci pour ne pas avoir à mettre ces trois lignes à) chaque fois dans la boucle de loadFirst()
	 *  @param table  la table courante
	 *  @param csvLoader  le csvLoader à utiliser (un par thread, si parsing de plusieurs fichiers simultanément
	 *                                               -> c'est une mauvaise idée, le disque devient limitant !)
	 *  @param csvPath  chemin vers le CSV à parser (relatif ou absolu)
	 *  @throws IOException  erreur retournée si l'accès au CSV est impossible
	 */
	private void parseThisCsv(Table table, SCsvLoader csvLoader, String csvPath) throws IOException {
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
	
	private SIndexHashJava theIndexHash; // permet de garder la référence et donc de ne pas être collecté par le GC
	
	/** Fonction pour tester l'indexation via une HashMap classique de Java
	 */
	private void testIndexHash() {
		System.gc();
		
		// ----------- Avec IndexHash ----------- 
		long memInit, memFinal;
		
		memInit = MemUsage.getMemUsage();
		
		Timer timer = new Timer("Temps pris testIndexHash");
		// Indexer deux colonnes dans l'IndexHash
		SIndexHashJava indHash = indexColumns(new int[] {3, 4}); // passenger_count et trip_distance
		theIndexHash = indHash;
		
		// Il doit y avoir 263 résultats
		
		byte[] rightSizedQuery;
		ByteBuffer searchQuery = getBufferedQuery();
		rightSizedQuery = new byte[searchQuery.position()];
		System.arraycopy(searchQuery.array(), 0, rightSizedQuery, 0, searchQuery.position());
		int[] resultsPositionsArray;
		Timer timerQuery;
		
		timerQuery = new Timer("Temps pris testIndexHash QUERY ONLY");
		resultsPositionsArray = indHash.get(rightSizedQuery);
		timer.log();
		timerQuery.log();
		
		double sumTotal = 0;
		int iteNumber = 40;
		for (int i = 1; i <= iteNumber; i++) {
			timerQuery = new Timer("Temps pris testIndexHash QUERY ONLY");
			resultsPositionsArray = indHash.get(rightSizedQuery);
			timerQuery.log();
			sumTotal += timerQuery.getns();
		}
		double medValue = sumTotal / (iteNumber * 1_000_000) ;
		Log.warning("Temps moyen utilisé (ms) : " + medValue + "ms");
		
		
		Log.info("resultsPositionsArray.length = " + resultsPositionsArray.length);
		
		memFinal = MemUsage.getMemUsage();
		String memUsedStr = MemUsage.formatMemUsage(memFinal - memInit);
		System.gc();
		Log.info("Mémoire utilisée : " + memUsedStr);
		

		//showResults(resultsPositionsArray);
		
		if (indHash.indexedColumns == null) {
			Log.info("Inutile, mais force le GC à garder la réf en mémoire");
		}
		
	}
	
	private IndexMemDic theIndexMemDic; // permet de garder la référence et donc de ne pas être collecté par le GC
	
	/** Fonction de test d'IndexMemDic, index mémoire par dichotomie, rapide et "memory-efficient"
	 */
	private void testIndexMemDic() {
		System.gc();
		
		// ----------- Avec IndexMemDic ----------- 
		//long memInit, memFinal; <- calculé en dehors, maintenant
		//memInit = MemUsage.getMemUsage();

		Timer timer = new Timer("Temps pris testIndexMemDic");
		
		IndexMemDic ind3 = new IndexMemDic(table, new int[]{3, 4}); // passenger_count et trip_distance
		ind3.sortAllv1();
		theIndexMemDic = ind3; // pour en garder la référence et qu'il ne soit pas collecté par le GC hors de la fonction
		
		// Il doit y avoir 54 résultats
		ByteBuffer searchQuery = getBufferedQuery();
		/*ArrayList<Object> queryAsList = new ArrayList<Object>();
		queryAsList.add(new Byte((byte) 1)); // (queryAsList, false);
		queryAsList.add(new Double(4));*/
		
		
		Timer timerQuery = new Timer("Temps pris testIndexMemDic QUERY ONLY");
		int[] resultsPositionsArray = ind3.findMatchingLinePositions(searchQuery); // les positions des lignes de résultat, réelles
		timerQuery.log();
		timer.log();
		
		ind3.enableFlagCheck(false);
		for (int i = 1; i < 40; i++) {
			timerQuery = new Timer("Temps pris testIndexMemDic QUERY ONLY");
			resultsPositionsArray = ind3.findMatchingLinePositions(searchQuery); // les positions des lignes de résultat, réelles
			timerQuery.log();
		}
		
		Log.info("resultsPositionsArray.length = " + resultsPositionsArray.length);
		
		//memFinal = MemUsage.getMemUsage();    <- calculé en dehors, maintenant
		//String memUsedStr = MemUsage.formatMemUsage(memFinal - memInit);
		//System.gc();
		//Log.info("Mémoire utilisée : " + memUsedStr);
		
		//showResults(resultsPositionsArray); /// <- Afficher tous kes résultats
		
		if (ind3.totalLength == 18) {
			Log.info("Inutile, mais force le GC à garder la réf en mémoire");
		}
		
	}
	
	
	/** Afficher tous les résultats dans la console, écrire toutes les valeurs associées à chaque ligne
	 * (non utilisé actuellement, d'où le warning, pour ne pas surcharger le console)
	 * @param resultsPositionsArray  un tableau de positions de lignes dont toutes les valaurs sont à afficher
	 */
	private void showResults(int[] resultsPositionsArray) {
		
		for (int resultIndex = 0; resultIndex < resultsPositionsArray.length; resultIndex++) {
			int linePosition = resultsPositionsArray[resultIndex];
			
			// Débug : 
			/*int delta = ind3.compareLineValuesAndQuery(linePosition, searchQuery);
			if (delta == 0) {
				String lineAsReadableString = table.getLineAsReadableString(linePosition); // i);//
				//Log.info(lineAsReadableString);
				Log.info("" + linePosition);
				resCount++;
			}*/
			String lineAsReadableString = table.getLineAsReadableString(linePosition); // i);//
			Log.info(lineAsReadableString);
		}
		
		
	}
	
	/** Indexer une liste de colonnes via un SIndexHash nouvellement créé
	 *  @param colIndexList   la liste des "index dans Table.columnsList" des colonnes à indexer
	 *  @return   un SIndexHash ayant indexé les colones passées en paramètre (via colIndexList)
	 */
	private SIndexHashJava indexColumns(int[] colIndexList) {
		if (colIndexList.length == 0) return null;
		
		int colCount = colIndexList.length;
		Column[] choosenColArray = new Column[colCount];
		int dataAsBytesTotalLength = 0;
		
		// Création d'un tableau et calcul de la taille totale nécessaire pour stocker les valeurs à indexer de chaque ligne
		for (int i = 0; i < colCount; i++) {
			choosenColArray[i] = table.getColumns().get(colIndexList[i]);
			dataAsBytesTotalLength += choosenColArray[i].dataSizeInBytes;
		}
		
		// Nombre de lignes au total (choosenColArray[0] existe forcément, ici)
		int linesNumber = choosenColArray[0].getTotalLinesNumber();
		
		// result sera retourné à la fin de cette fonction
		SIndexHashJava result = new SIndexHashJava(choosenColArray, linesNumber);
		
		for (int iLine = 0; iLine < linesNumber; iLine++) {
			
			// ATTENTION : bien faire new byte[] ici et non en dehors de la boucle iLine, perte de la cohérence des données de la hashMap !
			byte[] wholeLineDataAsBytes = new byte[dataAsBytesTotalLength];
			String debugBytes = "";
			int destPos = 0;
			for (int iColumn = 0; iColumn < colCount; iColumn++) {
				Column currentColumn = choosenColArray[iColumn];
				// Pour indexer, je n'ai besoin que d'un tableau d'octets
				byte[] columnDataAsBytes = currentColumn.getDataAsRawBytes(iLine);
				for (int iByte = 0; iByte < columnDataAsBytes.length; iByte++) {
					debugBytes += columnDataAsBytes[iByte] + " ";
				}
				System.arraycopy(columnDataAsBytes, 0, wholeLineDataAsBytes, destPos, currentColumn.dataSizeInBytes);
				destPos += currentColumn.dataSizeInBytes;
			}
			
			//resultMemDic.setPosition(iLine, iLine);
			result.put(wholeLineDataAsBytes, iLine);
			//Log.info("put: " + debugBytes);
			//Log.info("put: " + wholeLineDataAsBytes.hashCode());
			
		}
		
		System.gc();
		MemUsage.printMemUsage();
		Timer t = new Timer("tc");
		//resultMemDic.sortAllv1();
		
		t.log();
		MemUsage.printMemUsage();
		
		return result;
	}
	
	
	
	
	
	
	
	
	// TODO ----------- Avec IndexMemDic, générique ----------- 
	private IndexMemDic createIndexAndMakeQuery(int[] indexedColumns, ByteBuffer searchQuery) {
		
		IndexMemDic localIndexMemDic = new IndexMemDic(table, indexedColumns);
		localIndexMemDic.sortAllv1();
		
		String indexedColumnsStr = "";
		for (int iCol = 0; iCol < indexedColumns.length; iCol++) {
			indexedColumnsStr += Integer.toString(indexedColumns[iCol]);
			if (iCol != indexedColumns.length - 1) {
				indexedColumnsStr += " ";
			}
		}
		
		// Il doit y avoir 54 résultats
		Timer timerQuery = new Timer("IndexMemDic[" + indexedColumnsStr + "] QUERY ONLY");
		int[] resultsPositionsArray = localIndexMemDic.findMatchingLinePositions(searchQuery);
		timerQuery.log();
		
		Log.info("resultsPositionsArray.length = " + resultsPositionsArray.length);
		
		return localIndexMemDic;
	}
	
	// TODO  Benchmark générique pour IndexMemDic
	private String doBenchmarkIndexMemDic(int[] indexedColumns, ByteBuffer query) {
		// ----------- Avec IndexMemDic ----------- 
		
		String indexedColumnsStr = "";
		for (int iCol = 0; iCol < indexedColumns.length; iCol++) {
			indexedColumnsStr += Integer.toString(indexedColumns[iCol]);
			if (iCol != indexedColumns.length - 1) {
				indexedColumnsStr += " ";
			}
		}
		
		System.gc();
		long memInit, memFinal;
		memInit = MemUsage.getMemUsage();
		Timer timer = new Timer("IndexMemDic[" + indexedColumnsStr + "] TOUT ");
		
		IndexMemDic indexDic = createIndexAndMakeQuery(indexedColumns, query);
		System.gc();
		
		memFinal = MemUsage.getMemUsage();
		String memUsedStr = MemUsage.formatMemUsage(memFinal - memInit);
		String result ="IndexMemDic[" + indexedColumnsStr + "] MEM " + memUsedStr + "  en  " + timer.pretty();
		
		
		if (indexDic.totalLength == 78) // garder la réf
			Log.info("Hey !");
		
		return result;
		
	}
	
	
	
}
