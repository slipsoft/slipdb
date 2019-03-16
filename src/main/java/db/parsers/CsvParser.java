package db.parsers;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

import com.dant.utils.Log;
import com.dant.utils.MemUsage;
import com.dant.utils.Timer;
import db.structure.Column;
import db.structure.Table;


/**
 * CsvParser : CsvParser version 2
 * 
 * But de ce parser :
 * - Evaluer le minimum et maximum de chaque colonne
 * - Sauvegarder chaque colonne dans un fichier séparé
 * - Split les résultats pour avoir des colonnes de tailles raisonnables, et pouvoir indexer en multi-thread (exemple : 1_000_000 par colonne)
 *
 */


public class CsvParser extends Parser {
	
	protected static String csvSeparator = ","; // the CSV separator used to delimit fields
	
	public CsvParser(Table schema) {
		super(schema);
	}
	
	@Override
	public void parse(InputStream input, int limit) {
		int currentLineCount = 0;
		
		//Timer timer = new Timer("load time");
		//MemUsage.printMemUsage();
		try (
				BufferedReader bRead = new BufferedReader(new InputStreamReader(input));
				DataOutputStream bWrite = new DataOutputStream(new BufferedOutputStream(schema.tableToOutputStream()));
		) {
			while (currentLineCount != limit) {
				String readCSVLine = bRead.readLine();
	
				if (readCSVLine == null) // no more data
					break;
				
				if (currentLineCount != 0) { // we don't want to process the first line (informations on fields and columns)
					byte[] lineAsByteArray = processLine(readCSVLine);
					bWrite.write(lineAsByteArray); // writes the line in the output stream associated with the current file
				}
				currentLineCount++;
			}
		} catch (IOException e) {
			Log.error(e);
		}
		
		
		/*
		for (int columnIndex = 0; columnIndex < schema.getColumns().size(); columnIndex++) {
			Column currentCol = schema.getColumns().get(columnIndex);
			//if (currentCol.minValue != null && currentCol.maxValue != null)
				System.out.println("Col " + columnIndex + " min = " + currentCol.minValue + "  max = " + currentCol.maxValue);
			
		}*/
		
		//MemUsage.printMemUsage();
		//timer.printms();
	}
	
	@Override
	/**
	 * No indexing is done here
	 */
	protected byte[] processLine(String line) {
		// 1) Split the current line, get an array of String
		String[] valueList = line.split(csvSeparator); // csvSeparator declared in the unit
		// Checks if there is a problem in the number of fields
		// Nicolas will probably make it throw an exception (au error has to be thrown, and handled the right way)
		// -> On a huge data amount, a few wrong entries won't really matter
		if (valueList.length != schema.getColumns().size()) {
			System.err.println("ERREUR AL_GlobalTest.processCSVLine : valueList.length(" + valueList.length
					+ ") != testTable.columnList.size()(" + schema.getColumns().size() + ")");
			// -> will be handled Nicolas' way ?
			return new byte[0];
		}
		
		// the buffer used to store the line data as an array of bytes
		ByteBuffer entryBuffer = ByteBuffer.allocate(lineByteSize);
		
		// for each column, parse and write data into entryBuffer
		for (int columnIndex = 0; columnIndex < schema.getColumns().size(); columnIndex++) {
			String strValue = valueList[columnIndex];
			Column currentColumn = schema.getColumns().get(columnIndex);
			// Converts the string value into an array of bytes representing the same data
			Object currentValue = currentColumn.parseAndReturnValue(strValue, entryBuffer);
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
		return entryBuffer.array();
	}
	
	
	
	

}