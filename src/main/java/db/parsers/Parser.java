package db.parsers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.List;

import com.dant.utils.Log;
import com.dant.utils.MemUsage;
import com.dant.utils.Timer;

import db.disk.dataHandler.TableDataHandler;
import db.disk.dataHandler.TableDataHandlerWriteJob;
import db.disk.dataHandler.DiskDataPosition;
import db.structure.Column;
import db.structure.Table;
import db.structure.recherches.SRuntimeIndexingEntry;
import db.structure.recherches.SRuntimeIndexingEntryList;

public abstract class Parser {
	protected Table schema;
	protected Table currentTable;
	protected int lineByteSize; // number of bytes used to store information
	protected int totalEntryCount = 0;
	
	/** Avant le parsing, définir runtimeIndexingEntries.
	 *  Au moment du parsing, runtimeIndexingEntries doit être en lecture seule, donc thread-safe.
	 */
	protected SRuntimeIndexingEntryList runtimeIndexingEntries = null;
	
	public Parser(Table schema) {
		this.schema = schema; // <- C'est Nicolas qui a voulu appeler ça comme ça, pas moi :p
		currentTable = schema;
		this.lineByteSize = schema.getLineSize();
	}
	
	// Pour indexer au moment du parsing
	public void setRuntimeIndexing(SRuntimeIndexingEntryList argIndexingEntryList) {
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
				String entryAsString = processReader(bRead); // "entrée", ligne lue (d'un fichier CSV par exemple pour CSVParser)
				if (entryAsString == null) break; // fin de la lecture
				
				// Ecriture de l'entrée
				try {
					parseAndWriteEntry(entryAsString, writeJob);
					localReadEntryNb++;
					totalEntryCount++;
				} catch (IncorrectEntryException e) {
					Log.error(e);
					// TODO: handle exception
				} catch (IOException e) {
					Log.error(e);
					// TODO: handle exception
				}
				
				// Affichage d'une entrée toutes les showInfoEveryParsedLines entrées lues
				if (showInfoEveryParsedLines != -1 && localReadEntryNb % showInfoEveryParsedLines == 0) {
					Log.info("Parser : nombre de résultats (local) parsés = " + localReadEntryNb + "   temps écoulé = " + timeTookTimer.pretty());
					MemUsage.printMemUsage();
				}
				
			}
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
		
		String[] valuesAsStringArray = processEntry(entryString);
		List<Column> columnsList = currentTable.getColumns();
		
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
				
				// Converts the string value into an array of bytes representing the same data
				Object currentValue = currentColumn.parseAndWriteToBuffer(valuesAsStringArray[columnIndex], entryBuffer);
				// TEMPORAIREMENT désactivé (rush) currentColumn.evaluateMinMax(currentValue); // <- Indispensable pour le IndexTreeCeption (non utile pour le IndexTreeDic)
				entriesArray[columnIndex] = currentValue;
			}
		} catch (IllegalArgumentException e) {
			throw new IncorrectEntryException(totalEntryCount, "incorrect data");
		}
		
		DiskDataPosition dataPosition = writeJob.writeDataLine(entryBuffer.array());
		
		// Indexer au moment de parser (pour de meilleures performances)
		if (runtimeIndexingEntries != null) {
			for (int columnIndex = 0; columnIndex < columnsList.size(); columnIndex++) {
				SRuntimeIndexingEntry indexingEntry = runtimeIndexingEntries.getEntryAssociatedWithColumnIndex(columnIndex);
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
	
	/*
	protected final void writeEntry(String entryString, /*OutputStream* /DataOutputStream output) throws IncorrectEntryException, IOException {
		String[] valuesAsStringArray = processEntry(entryString);
		
		if (!isCorrectSize(valuesAsStringArray)) {
			throw new IncorrectEntryException(totalEntryCount, "incorrect size");
		}
		
		// the buffer used to store the line data as an array of bytes
		ByteBuffer entryBuffer = ByteBuffer.allocate(lineByteSize);
		Object[] entriesArray = new Object[valuesAsStringArray.length];
		
		
		long entryBinIndex = output.size();
		try {
		// for each column, parse and write data into entryBuffer
			for (int columnIndex = 0; columnIndex < schema.getColumns().size(); columnIndex++) {
				Column currentColumn = schema.getColumns().get(columnIndex);
				
				// Converts the string value into an array of bytes representing the same data
				Object currentValue = currentColumn.writeToBuffer(valuesAsStringArray[columnIndex], entryBuffer);
				currentColumn.evaluateMinMax(currentValue); // <- Indispensable pour le IndexTreeCeption (non utile pour le IndexTreeDic)
				// Indexer au moment de parser (pour de meilleures performances)
				
				if (runtimeIndexingEntries != null) {
					SRuntimeIndexingEntry indexingEntry = runtimeIndexingEntries.getEntryAssociatedWithColumnIndex(columnIndex);
					if (indexingEntry != null) {
						// Indexer cette entrée
						indexingEntry.addIndexValue(currentValue, entryBinIndex);
						//Log.info("Indexer valeur = " + currentValue);
					}
					//Log.info("Indexer2 valeur = " + currentValue);
				}
				
				
				entriesArray[columnIndex] = currentValue;
			}
		} catch (Exception e) {
			throw new IncorrectEntryException(totalEntryCount, "incorrect data");
		}
		// writes the line in the output stream associated with the current file
		output.write(entryBuffer.array());
	}*/
	
	/**
	 * Checks if the number of values in an array is the same as the number of columns in the schema.
	 *
	 * @param valuesArray
	 * @return
	 */
	private final boolean isCorrectSize(String[] valuesArray) {
		return valuesArray.length == schema.getColumns().size();
	}
	
	
	//////////////////////////////// Interface to implement ////////////////////////////////
	
	/**
	 * Reads a BufferedReader and return an entry String.
	 *
	 * @param input - the input as a buffered reader
	 * @return entryString or *null* if there is no more entries
	 */
	abstract protected String processReader(BufferedReader input) throws IOException;
	
	/** 
	 * Converts a string entry (such as a CSV line) to a byte array of raw data.
	 *
	 * @param entryString
	 * @return the data stored by the line as string array
	 */
	abstract protected String[] processEntry(String entryString);
}
