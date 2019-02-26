package db.mainTest;

import db.parseCSV.CSVParser;
import db.structure.Table;

public class MainForTests {
	
	public static void main(String[] args) {
		Table table = new Table();
		CSVParser parser = new CSVParser();
		
		// J'ai pas passé en argulent ces valeurs, pour faire plaisir à Nicolas !
		parser.loadFromCSV("D:\\L3 DANT disque D\\SMALL_100_000_yellow_tripdata_2015-04.csv",
				           "D:\\L3 DANT disque D\\SMALL_100_000_yellow_tripdata_2015-04.bin", 100_000, table);
		
	}
}
