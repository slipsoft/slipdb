package db.parsers;

import java.io.InputStream;

import db.structure.Table;

public abstract class Parser {
	protected Table schema;
	protected int lineByteSize; // number of bytes used to store information
	
	public Parser(Table schema) {
		this.schema = schema;
		this.lineByteSize = schema.getLineSize();
	}

	public void parse(InputStream input) {
		parse(input, -1);
	}
	
	/**
	 * Parse an input stream into an output stream according to a schema with a
	 * limit of lines (-1 : no limit)
	 * 
	 * @param input
	 * @param output
	 * @param schema
	 * @param limit
	 */
	abstract public void parse(InputStream input, int limit);
	
	/** Converts a line (such as a CSV line) to a byte array of raw data
	 *  @param line
	 *  @return the data stored by the line as byte array
	 */
	abstract protected byte[] processLine(String line);
	
}
