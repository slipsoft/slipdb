package db.parsers;

import java.io.InputStream;
import java.io.OutputStream;

import db.structure.Table;

public abstract class Parser {
	protected Table schema;
	protected int entrySize;
	
	public Parser(Table schema) {
		this.schema = schema;
		this.entrySize = schema.getLineSize();
	}

	public void parse(InputStream input, OutputStream output) {
		parse(input, output, -1);
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
	abstract public void parse(InputStream input, OutputStream output, int limit);
	
	abstract protected byte[] processLine(String line);
}
