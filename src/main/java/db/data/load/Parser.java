package db.data.load;

import java.io.BufferedReader;
import java.io.IOException;

public abstract class Parser {

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
