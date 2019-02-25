package db.storage;

import java.io.BufferedReader;
import java.io.FileReader;

import db.structure.Table;

/**
 * Lire d'un fichier CSV 
 * 
 */
public class ReadFromCSV {
	
}
	/*
	// Copie des valeurs de la table
	private int tableColumnCount;
	private ArrayList<>
	
	public void readFromCSV(String filePath, Table inTable) {
		
		try {
			FileReader fRead = new FileReader(filePath);
			BufferedReader bRead = new BufferedReader(fRead);
			

			//PrintWriter writer = new PrintWriter("D:\\\\L3 DANT disque D\\\\SMALL_1_000_000_yellow_tripdata_2015-04.csv", "UTF-8");
			
			while (true) { // ce FAMEUX while(true) de la mort qui pue...
				String line = bRead.readLine();
				if (line == null) break;
				//writer.println(line);
				//System.out.println(line);
				if (currentLineCount != 0) {
					processCSVLine(line);
					//String[] valueList = line.split(",");
					//System.out.println(valueList.length);
				}
				currentLineCount++;
				if (currentLineCount >= maxLineCount) break;
			}
			bRead.close();
			//writer.close();
		} catch (Exception e) {
			
		}
		
		
		
		
	}
	
	public void processCSVLine(String csvLine, Table inTable) {
		String[] valueList = csvLine.split(",");
		if (valueList.length != testTable.columnList.size()) {
			System.err.println("ERREUR AL_GlobalTest.processCSVLine : valueList.length("+valueList.length+") != testTable.columnList.size()("+testTable.columnList.size()+")");
			return;
		}
	}
	
}

class ReadFromCSV_
*/





