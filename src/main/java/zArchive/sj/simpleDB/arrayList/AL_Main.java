package zArchive.sj.simpleDB.arrayList;

public class AL_Main {
	
	public static void main(String[] args) {
		
		AL_GlobalTest globalTest = new AL_GlobalTest();
		args = new String[1];
		args[0] = "testdata/SMALL_100_000_yellow_tripdata_2015-04.csv";
		//globalTest.globalTestOffline();
		if (args.length >= 1)
			globalTest.globalTestWithCSV(args[0]);
		else {
			System.out.println("AL_Main.main() : pas assez de paramètres en entrée.");
		}
		
		
	}
	
	
	
}
