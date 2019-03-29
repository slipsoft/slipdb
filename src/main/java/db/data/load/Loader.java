package db.data.load;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.dant.utils.Log;
import com.dant.utils.MemUsage;
import com.dant.utils.Timer;

import db.disk.dataHandler.TableDataHandler;
import db.disk.dataHandler.TableDataHandlerWriteJob;
import db.data.types.DateType;
import db.disk.dataHandler.DiskDataPosition;
import db.structure.Column;
import db.structure.Table;
import db.structure.recherches.RuntimeIndexingEntry;
import db.structure.recherches.RuntimeIndexingEntryList;

public class Loader {
	private Table schema;
	private Table currentTable;
	private Parser parser;
	private int lineByteSize; // number of bytes used to store information
	private int totalEntryCount = 0;
	
	static public AtomicLong totalReadEntryNb = new AtomicLong(0);
	static public long updateTotalReadEntryNbEach = 100_000;
	protected long addToTotalReadEntryCountBuffered = 0;
	
	public static AtomicLong debugNumberOfEntriesWritten = new AtomicLong(0);
	
	/* Thread-safe
	protected void updateTotalResultCount(long addToTotal) {
	}*/
	
	
	/** Avant le parsing, définir runtimeIndexingEntries.
	 *  Au moment du parsing, runtimeIndexingEntries doit être en lecture seule, donc thread-safe.
	 */
	private RuntimeIndexingEntryList runtimeIndexingEntries = null;

	public Loader(Table schema, Parser parser) {
		this.schema = schema; // <- C'est Nicolas qui a voulu appeler ça comme ça, pas moi :p
		currentTable = schema;
		this.parser = parser;
		this.lineByteSize = schema.getLineSize();
	}
	
	// Pour indexer au moment du parsing
	public void setRuntimeIndexing(RuntimeIndexingEntryList argIndexingEntryList) {
		runtimeIndexingEntries = argIndexingEntryList;
	}
	
	public final void parse(InputStream input, boolean appendAtTheEndOfSave) {
		parse(input, -1, appendAtTheEndOfSave);
	}
	
	protected int showInfoEveryParsedLines = 100_000; // mettre -1 pour désactiver l'affichage
	
	/**
	 * Parse an input stream into an output stream according to a schema with a
	 * limit of lines (-1 : no limit)
	 * 
	 * @param input
	 * @param limit
	 */
	public final void parse(InputStream input, int limit, boolean appendAtTheEndOfSave) {
		int localReadEntryNb = 0;
		int localReadEntryNbToAddToTotalCount = 0;
		TableDataHandler dataHandler = currentTable.getDataHandler();
		try (
				BufferedReader bRead = new BufferedReader(new InputStreamReader(input));
				TableDataHandlerWriteJob writeJob = dataHandler.createNewWriteJob();
				//DataOutputStream bWrite = new DataOutputStream(new BufferedOutputStream(schema.tableToOutputStream(appendAtTheEndOfSave)));
		) {
			
			
			Timer timeTookTimer = new Timer("Temps écoulé");
			
			//while ((entryString = processReader(bRead)) != null && totalEntryCount != limit) {
			while (localReadEntryNb != limit) {
				
				// Lecture d'une nouvelle ligne / entrée
				String entryAsString = parser.processReader(bRead); // "entrée", ligne lue (d'un fichier CSV par exemple pour CSVParser)
				if (entryAsString == null) break; // fin de la lecture
				
				// Ecriture de l'entrée
				try {
					parseAndWriteEntry(entryAsString, writeJob);
					localReadEntryNb++;
					totalEntryCount++;
				} catch (IncorrectEntryException e) {
					Log.warning(e);
					// TODO: handle exception
				} catch (IOException e) {
					Log.error(e);
					// TODO: handle exception
				}
				
				// Affichage d'une entrée toutes les showInfoEveryParsedLines entrées lues
				if (showInfoEveryParsedLines != -1 && localReadEntryNb % showInfoEveryParsedLines == 0) {
					Log.info("Loader : nombre de résultats (local) parsés = " + localReadEntryNb + "   temps écoulé = " + timeTookTimer.pretty());
					MemUsage.printMemUsage();
				}
				
				if (localReadEntryNbToAddToTotalCount >= updateTotalReadEntryNbEach) {
					totalReadEntryNb.addAndGet(localReadEntryNbToAddToTotalCount);
					localReadEntryNbToAddToTotalCount = 0;
				}
				
				
			}
			debugNumberOfEntriesWritten.addAndGet(localReadEntryNb);
		} catch (FileNotFoundException e) {
			Log.error(e);
			// TODO: handle exception
		}catch (IOException e) {
			Log.error(e);
			// TODO: handle exception
		}
	}
	
	

	/**
	 * 
	/** Ecriture d'une entrée (ligne, donnée complète) sur un DataOutputStream (nécessaire pour avoir le fonction .size())
	 * @param entryString
	 * @param writeJob
	 * @throws IncorrectEntryException
	 * @throws IOException 
	 */
	protected final void parseAndWriteEntry(String entryString, TableDataHandlerWriteJob writeJob) throws IncorrectEntryException, IOException {
		
		String[] valuesAsStringArray = parser.processEntry(entryString);
		List<Column> columnsList = currentTable.getColumns();
		
		DateType localDateTypeThreadSafe = new DateType();
		
		if (!isCorrectSize(valuesAsStringArray)) {
			throw new IncorrectEntryException(totalEntryCount, "incorrect size");
		}
		// the buffer used to store the line data as an array of bytes
		ByteBuffer entryBuffer = ByteBuffer.allocate(lineByteSize);
		Object[] entriesArray = new Object[valuesAsStringArray.length];
		
		try {
			// for each column, parse and write data into entryBuffer
			for (int columnIndex = 0; columnIndex < columnsList.size(); columnIndex++) {
				Column currentColumn = columnsList.get(columnIndex);
				Object currentValue;
				
				//Log.info("parseAndWriteEntry : valuesAsStringArray["+columnIndex+"] = " + valuesAsStringArray[columnIndex]);
				// Converts the string value into an array of bytes representing the same data
				if (currentColumn.getDataType().getClass() == DateType.class) {
					currentValue = localDateTypeThreadSafe.parseAndWriteToBuffer(valuesAsStringArray[columnIndex], entryBuffer);
				} else {
					currentValue = currentColumn.parseAndWriteToBuffer(valuesAsStringArray[columnIndex], entryBuffer);
				}
				// TEMPORAIREMENT désactivé (rush) currentColumn.evaluateMinMax(currentValue); // <- Indispensable pour le IndexTreeCeption (non utile pour le IndexTreeDic)
				entriesArray[columnIndex] = currentValue;
			}
		} catch (IllegalArgumentException e) {
			//e.printStackTrace();
			throw new IncorrectEntryException(totalEntryCount, "incorrect data");
		}
		
		DiskDataPosition dataPosition = writeJob.writeDataLine(entryBuffer.array());
		
		// Indexer au moment de parser (pour de meilleures performances)
		if (runtimeIndexingEntries != null) {
			for (int columnIndex = 0; columnIndex < columnsList.size(); columnIndex++) {
				RuntimeIndexingEntry indexingEntry = runtimeIndexingEntries.getEntryAssociatedWithColumnIndex(columnIndex);
				if (indexingEntry != null) {
					Object currentValue = entriesArray[columnIndex];
					// Indexer cette entrée
					indexingEntry.addIndexValue(currentValue, dataPosition);
					//Log.info("Indexer valeur = " + currentValue);
				}
				//Log.info("Indexer2 valeur = " + currentValue);
			}
		}
		
		
	}
	
	/**
	 * Checks if the number of values in an array is the same as the number of columns in the schema.
	 *
	 * @param valuesArray
	 * @return
	 */
	private final boolean isCorrectSize(String[] valuesArray) {
		return valuesArray.length == schema.getColumns().size();
	}
}
