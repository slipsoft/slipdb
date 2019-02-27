package db.mainTest;

import java.util.ArrayList;

import db.parseCSV.CSVParser;
import db.structure.Column;
import db.structure.Table;

public class MainForTests {
	
	public static void main(String[] args) {
		Table table = new Table("test", new ArrayList<Column>());
		CSVParser parser = new CSVParser();
		
		// J'ai pas passé en argulent ces valeurs, pour faire plaisir à Nicolas !
		parser.loadFromCSV("testdata/SMALL_100_000_yellow_tripdata_2015-04.csv",
				           "testdata/SMALL_100_000_yellow_tripdata_2015-04.bin", 100_000, table);
		
	}
}
