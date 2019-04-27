package db.index;

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
import db.structure.Column;
import db.structure.Table;
import index.memDic.IndexMemDic;
import noob.fastStructure.SCsvLoader;
import noob.fastStructure.SIndexHashJava;

public class SIndexBench {

	//private int limitResultNb = 20_000;
	Table table;
	
	private int parsingTimeLimitSec = -1;
	private Timer limitParsingTimeTimer;
	
	
	/** Préparation de la requête commune aux index (via un ByteBuffer)
	 *  @return
	 */
	private ByteBuffer getBufferedQuery() {
		ByteBuffer searchQuery = ByteBuffer.allocate(100);
		searchQuery.rewind();
		searchQuery.put((byte)1);
		searchQuery.putFloat(4);
		return searchQuery;
	}
	
	SIndexHashJava theIndexHash;
	
	private void testIndexHash() {
		System.gc();
		
		// ----------- Avec IndexHash ----------- 
		long memInit, memFinal;
		
		memInit = MemUsage.getMemUsage();
		
		Timer timer = new Timer("Temps pris testIndexHash");
		SIndexHashJava indHash = indexColumns(new int[] {3, 4}); // passenger_count et trip_distance
		theIndexHash = indHash;
		
		// Il doit y avoir 54 résultats
		
		byte[] rightSizedQuery;
		ByteBuffer searchQuery = getBufferedQuery();
		rightSizedQuery = new byte[searchQuery.position()];
		System.arraycopy(searchQuery.array(), 0, rightSizedQuery, 0, searchQuery.position());
		Timer timerQuery = new Timer("Temps pris testIndexHash QUERY ONLY");
		int[] resultsPositionsArray = indHash.get(rightSizedQuery);
		timer.log();
		timerQuery.log();
		
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
	
	IndexMemDic theIndexMemDic;
	
	private void testIndexMemDic() {
		System.gc();
		
		// ----------- Avec IndexMemDic ----------- 
		long memInit, memFinal;
		
		memInit = MemUsage.getMemUsage();

		Timer timer = new Timer("Temps pris testIndexMemDic");
		
		IndexMemDic ind3 = new IndexMemDic(table, new int[]{3, 4}); // passenger_count et trip_distance
		ind3.sortAllv1();
		theIndexMemDic = ind3;
		
		// Il doit y avoir 54 résultats
		ByteBuffer searchQuery = getBufferedQuery();
		Timer timerQuery = new Timer("Temps pris testIndexMemDic QUERY ONLY");
		int[] resultsPositionsArray = ind3.findMatchingLinePositions(searchQuery);
		timerQuery.log();
		timer.log();
		
		Log.info("resultsPositionsArray.length = " + resultsPositionsArray.length);
		
		memFinal = MemUsage.getMemUsage();
		String memUsedStr = MemUsage.formatMemUsage(memFinal - memInit);
		System.gc();
		Log.info("Mémoire utilisée : " + memUsedStr);
		
		//showResults(resultsPositionsArray);
		
		if (ind3.totalLength == 18) {
			Log.info("Inutile, mais force le GC à garder la réf en mémoire");
		}
		
	}
	
		// ----------- Avec IndexMemDic, générique ----------- 
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
		
		Log.error(info1);
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
		
		Log.error(info2);

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
		
		Log.error(info3);
		
		if (theIndexHash.myNumberToKeepRef == 7865241) { // get(new byte[] {0}) == null
			Log.info("Houyy !");
		}
		
		
		ArrayList<String> infoMessagesList = new ArrayList<String>();
		
		// Bench de divers index
		/*
		infoMessagesList.add(doBenchmarkIndexMemDic(new int[] {}));
		
		
		ByteBuffer searchQuery;
		searchQuery = ByteBuffer.allocate(100); searchQuery.rewind();
		searchQuery.put((byte)1);
		searchQuery.putFloat(4);
		
		
		System.gc();
		memInit = MemUsage.getMemUsage();
		tim2 = new Timer("Temps pris testIndexMemDic");
		
		testIndexMemDic();
		System.gc();
		
		memFinal = MemUsage.getMemUsage();
		memUsedStr2 = MemUsage.formatMemUsage(memFinal - memInit);
		info2 = "MEM MEM MEM Mémoire utilisée IndexMemDic : " + memUsedStr2 + "  en  " + tim2.pretty();
		
		Log.error(info2);

		if (theIndexMemDic.totalLength == 78) // garder la réf
			Log.info("Hey !");
		*/
		
		
		
		
		
		
		
		Log.info("");
		Log.warning(info1);
		Log.warning(info2);
		Log.warning(info3);
		
		for (int iMsg = 0; iMsg < infoMessagesList.size(); iMsg++) {
			Log.warning(infoMessagesList.get(iMsg));
		}
		
		
		
		
		
	}
	
	private void showResults(int[] resultsPositionsArray) {
		
		for (int resultIndex = 0; resultIndex < resultsPositionsArray.length; resultIndex++) {
			int linePosition = resultsPositionsArray[resultIndex];
			
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
	
	private SIndexHashJava indexColumns(int[] colIndexList) {
		if (colIndexList.length == 0) return null;
		
		int colCount = colIndexList.length;
		Column[] choosenColArray = new Column[colCount];
		int dataAsBytesTotalLength = 0;
		
		for (int i = 0; i < colCount; i++) {
			choosenColArray[i] = table.getColumns().get(colIndexList[i]);
			dataAsBytesTotalLength += choosenColArray[i].dataSizeInBytes;
		}
		
		// Nombre de lignes au total
		int linesNumber = choosenColArray[0].getTotalLinesNumber();
		
		SIndexHashJava result = new SIndexHashJava(choosenColArray, linesNumber);
		//IndexMemDic resultMemDic = new IndexMemDic(table, colIndexList);
		
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
			//String csvPath = "F:/csv/SMALL_1_000_000_yellow_tripdata_2015-04.csv";
			Log.info("Parsing de csvName = " + csvPath);
			parseThisCsv(table, csvLoader, csvPath);
		}
		
		//System.gc();
		//MemUsage.printMemUsage("Mem usage  fin - ");
		parseTimer.log();
		
		
		
	}
	
	private void parseThisCsv(Table table, SCsvLoader csvLoader, String csvPath) throws IOException {
		InputStream csvStream = new FileInputStream(csvPath);
		csvLoader.parse(csvStream, true, limitParsingTimeTimer, parsingTimeLimitSec);
		csvStream.close();
	}
}
