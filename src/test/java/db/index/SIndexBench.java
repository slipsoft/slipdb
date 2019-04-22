package db.index;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

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
import noob.fastStructure.SCsvLoader;
import noob.fastStructure.SIndexHashJava;

public class SIndexBench {

	private int limitResultNb = 20_000;
	Table table;
	
	
	@Test
	public void mainTest() throws Exception {
		
		// Chargement des CSV
		loadFirst();
		
		System.gc();
		Log.error("DEBUT");
		MemUsage.printMemUsage();
		// Ajout à l'index
		//SIndexHash ind2 = indexColumns(new int[] {3, 3}); // passenger_count
		SIndexHashJava ind1 = indexColumns(new int[] {3, 4}); // passenger_count et trip_distance
		//ind2.put(new byte[] {1}, 99);
		
		System.gc();
		Log.error("FIN");
		MemUsage.printMemUsage();
		
		
		ByteBuffer seachQuery = ByteBuffer.allocate(100);
		byte[] rightSizedQuery;
		
		seachQuery.rewind();
		seachQuery.put((byte)1);
		seachQuery.putFloat(2);
		rightSizedQuery = new byte[seachQuery.position()];
		System.arraycopy(seachQuery.array(), 0, rightSizedQuery, 0, seachQuery.position());
		int[] resultsPositionsArray = ind1.get(rightSizedQuery);
		
		/*seachQuery.rewind();
		seachQuery.put((byte)2);
		rightSizedQuery = new byte[seachQuery.position()];
		System.arraycopy(seachQuery.array(), 0, rightSizedQuery, 0, seachQuery.position());
		
		
		int[] resultsPositionsArray = ind2.get(rightSizedQuery);*/
		
		Log.info("resultsPositionsArray.length = " + resultsPositionsArray.length + "  seachQuery.position() = " + seachQuery.position());
		//ind2.put(rightSizedQuery, 99);
		
		
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
			
			result.put(wholeLineDataAsBytes, iLine);
			//Log.info("put: " + debugBytes);
			//Log.info("put: " + wholeLineDataAsBytes.hashCode());
			
		}
		
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
		
		Timer parseTimer = new Timer("TEMPS TOTAL PRIS PAR TOUS LES PARSINGS");
		
		System.gc();
		MemUsage.printMemUsage("Mem usage  début - ");
		SCsvLoader csvLoader = new SCsvLoader(table, new CsvParser());
		
		int mounthFinalCount = 1;
		for (int iCsv = 1; iCsv <= mounthFinalCount; iCsv++) {
			String colNumber = String.format("%02d" , iCsv);
			String csvPath = "testdata/SMALL_100_000_yellow_tripdata_2015-04.csv";
			//String csvPath = "F:/csv/yellow_tripdata_2015-" + colNumber + ".csv"; // E:/L3 DANT disque E
			Log.info("Parsing de csvName = " + csvPath);
			parseThisCsv(table, csvLoader, csvPath);
		}
		
		System.gc();
		MemUsage.printMemUsage("Mem usage  fin - ");
		parseTimer.log();
		
		
	}
	
	private void parseThisCsv(Table table, SCsvLoader csvLoader, String csvPath) throws IOException {
		InputStream csvStream = new FileInputStream(csvPath);
		csvLoader.parse(csvStream, true);
		csvStream.close();
	}
}
