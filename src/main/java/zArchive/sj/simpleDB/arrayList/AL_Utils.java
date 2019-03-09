package zArchive.sj.simpleDB.arrayList;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AL_Utils {
	
	
	
	// Opérations sur les dates
	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
	
	public static Date dateFromString(String dateAsString) {
		try {
			return dateFormat.parse(dateAsString);
		} catch (ParseException e) {
			return null; //new Date();
		}
	}
	
	public static String dateToString(Date date) {
	    return dateFormat.format(date);
	}
}
