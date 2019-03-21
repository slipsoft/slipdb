package db.parsers;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
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
			String entryString;
			Timer timeTookTimer = new Timer("Temps écoulé");
			while ((entryString = processReader(bRead)) != null && totalEntryCount != limit) {
				if (showInfoEveryParsedLines != -1 && localReadEntryNb % showInfoEveryParsedLines == 0) {
					Log.info("Parser : nombre de résultats (local) parsés = " + localReadEntryNb + "   temps écoulé = " + timeTookTimer.pretty());
				}
				
				try {
					this.writeEntry(entryString, bWrite);
					localReadEntryNb++;
					totalEntryCount++;
				} catch (IncorrectEntryException e) {
					Log.error(e);
					// TODO: handle exception
				} catch (IOException e) {
					Log.error(e);
					// TODO: handle exception
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
	
	protected final void writeEntry(String entryString, OutputStream output) throws IncorrectEntryException, IOException {
		String[] valuesArray = processEntry(entryString);

		if (!isCorrectSize(valuesArray)) {
			throw new IncorrectEntryException(totalEntryCount, "incorrect size");
			// -> will be handled Nicolas' way ? yes
		}
		// the buffer used to store the line data as an array of bytes
		ByteBuffer entryBuffer = ByteBuffer.allocate(lineByteSize);
		Object[] entry = new Object[valuesArray.length];
		try {
		// for each column, parse and write data into entryBuffer
			for (int i = 0; i < schema.getColumns().size(); i++) {
				Column currentColumn = schema.getColumns().get(i);
				// Converts the string value into an array of bytes representing the same data
				Object currentValue = currentColumn.writeToBuffer(valuesArray[i], entryBuffer);
				currentColumn.evaluateMinMax(currentValue);
				entry[i] = currentValue;
			}
		} catch (Exception e) {
			throw new IncorrectEntryException(totalEntryCount, "incorrect data");
		}
		// writes the line in the output stream associated with the current file
		output.write(entryBuffer.array());
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
