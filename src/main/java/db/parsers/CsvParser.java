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

public class CsvParser extends Parser {
	
	protected static String csvSeparator = ","; // the CSV separator used to delimit fields
	
	public CsvParser(Table schema) {
		super(schema);
	}

	@Override
	public void parse(InputStream input, int limit) {
		int currentLineCount = 0;
		
		Timer timer = new Timer("load time");
		MemUsage.printMemUsage();
		try (
				BufferedReader bRead = new BufferedReader(new InputStreamReader(input));
				DataOutputStream bWrite = new DataOutputStream(new BufferedOutputStream(schema.tableToOutputStream()));
		) {
			while (currentLineCount != limit) {
				String line = bRead.readLine();
	
				if (line == null) // no more data
					break;
				
				if (currentLineCount != 0) { // we don't want to process the first line (informations on fields and columns)
					byte[] lineAsByteArray = processLine(line);
					bWrite.write(lineAsByteArray);
				}
				currentLineCount++;
			}
		} catch (IOException e) {
			Log.error(e);
		}

		MemUsage.printMemUsage();
		timer.printms();
	}

	@Override
	protected byte[] processLine(String line) {
		// 1) Split the current line, get an array of String
		String[] valueList = line.split(csvSeparator); // csvSeparator declared in the unit
		// Checks if there is a problem in the number of fields
		// Nicolas will probably make it throw an exception (au error has to be thrown, and handled the right way)
		// -> On a huge data amount, a few wrong entries won't really matter
		if (valueList.length != schema.getColumns().size()) {
			System.err.println("ERREUR AL_GlobalTest.processCSVLine : valueList.length(" + valueList.length
					+ ") != testTable.columnList.size()(" + schema.getColumns().size() + ")");
			// will be handled Nicolas' way ?
			return new byte[0];
		}
		
		// the buffer 
		ByteBuffer entryBuffer = ByteBuffer.allocate(lineByteSize);

		for (int columnIndex = 0; columnIndex < schema.getColumns().size(); columnIndex++) {
			String strValue = valueList[columnIndex];
			Column currentColumn = schema.getColumns().get(columnIndex);
			currentColumn.parse(strValue, entryBuffer);
		}

		return entryBuffer.array();
	}

}
