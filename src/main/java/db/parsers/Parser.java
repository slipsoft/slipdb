package db.parsers;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.dant.utils.Log;
import com.dant.utils.Timer;

import db.structure.Column;
import db.structure.Table;

public abstract class Parser {
	protected Table schema;
	protected int lineByteSize; // number of bytes used to store information
	protected int totalEntryCount = 0;
	
	public Parser(Table schema) {
		this.schema = schema;
		this.lineByteSize = schema.getLineSize();
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
		try (
				BufferedReader bRead = new BufferedReader(new InputStreamReader(input));
				
				DataOutputStream bWrite = new DataOutputStream(new BufferedOutputStream(schema.tableToOutputStream(appendAtTheEndOfSave)));
		) {
			Timer timeTookTimer = new Timer("Temps écoulé");
			while (totalEntryCount != limit) {
				if (localReadEntryNb % showInfoEveryParsedLines == 0 && showInfoEveryParsedLines != -1) {
					Log.info("Parser : nombre de résultats (local) parsés = " + localReadEntryNb + "   temps écoulé = " + timeTookTimer.getseconds() + " s");
				}
				
				String entryString = this.processReader(bRead);
	
				if (entryString == null) // no more data
					break;
				
				if (localReadEntryNb != 0) { // we don't want to process the first line (informations on fields and columns)
					try {
						this.writeEntry(entryString, bWrite);
					} catch (IncorrectEntryException e) {
						Log.error(e);
					}
				}
				localReadEntryNb++;
				totalEntryCount++;
			}
			bWrite.close();
		} catch (Exception e) {
			Log.error(e);
		}
	}
	
	protected final void writeEntry(String entryString, OutputStream output) throws IncorrectEntryException {
		String[] valuesArray = processEntry(entryString);

		if (!isCorrectSize(valuesArray)) {
			throw new IncorrectEntryException(totalEntryCount, "incorrect size");
			// -> will be handled Nicolas' way ? yes
		}
		// the buffer used to store the line data as an array of bytes
		ByteBuffer entryBuffer = ByteBuffer.allocate(lineByteSize);
		
		// for each column, parse and write data into entryBuffer
		for (int columnIndex = 0; columnIndex < schema.getColumns().size(); columnIndex++) {
			String valueString = valuesArray[columnIndex];
			Column currentColumn = schema.getColumns().get(columnIndex);
			// Converts the string value into an array of bytes representing the same data
			Object currentValue = currentColumn.parseAndReturnValue(valueString, entryBuffer);
			if (currentColumn.minValue == null) currentColumn.minValue = currentValue;
			if (currentColumn.maxValue == null) currentColumn.maxValue = currentValue;
			if (currentColumn.compareValues(currentValue, currentColumn.maxValue) == 1) {
				currentColumn.maxValue = currentValue;
			}
			if (currentColumn.compareValues(currentValue, currentColumn.minValue) == -1) {
				currentColumn.minValue = currentValue;
			}
		}
		// returns the CSV line as an array of rightly typed data, as bytes
		// return entryBuffer.array(); deso je vois pas à quoi ça sert
		try {
			output.write(entryBuffer.array()); // writes the line in the output stream associated with the current file
		} catch (IOException e) {
			Log.error(e);
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
