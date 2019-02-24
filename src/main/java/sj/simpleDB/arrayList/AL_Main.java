package sj.simpleDB.arrayList;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AL_Main {
	
	public static void main(String[] args) {
		
		AL_GlobalTest globalTest = new AL_GlobalTest();
		//globalTest.globalTestOffline();
		globalTest.globalTestWithCSV("/home/etienne/Code/laTarentule.csv");
		
		
		
	}
	
	
	
}
