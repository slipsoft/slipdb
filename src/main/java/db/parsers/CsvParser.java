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
import com.dant.utils.MemUsage;
import com.dant.utils.Timer;
import db.structure.Column;
import db.structure.Table;

public class CsvParser extends Parser {

	public CsvParser(Table schema) {
		super(schema);
	}

	@Override
	public void parse(InputStream input, OutputStream output, int limit) {
		int currentLineCount = 0;
		
		Timer timer = new Timer("load time");
		MemUsage.printMemUsage();
		try (
				BufferedReader bRead = new BufferedReader(new InputStreamReader(input));
				DataOutputStream bWrite = new DataOutputStream(new BufferedOutputStream(output));
		) {
			while (currentLineCount != limit) {
				String line = bRead.readLine();
	
				if (line == null)
					break;
				if (currentLineCount != 0) {
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
		String[] valueList = line.split(",");
		if (valueList.length != schema.getColumns().size()) {
			System.err.println("ERREUR AL_GlobalTest.processCSVLine : valueList.length(" + valueList.length
					+ ") != testTable.columnList.size()(" + schema.getColumns().size() + ")");
			return new byte[0];
		}

		ByteBuffer entryBuffer = ByteBuffer.allocate(entrySize);

		for (int columnIndex = 0; columnIndex < schema.getColumns().size(); columnIndex++) {
			String strValue = valueList[columnIndex];
			Column currentColumn = schema.getColumns().get(columnIndex);

			currentColumn.parse(strValue, entryBuffer);
		}

		return entryBuffer.array();
	}

}
