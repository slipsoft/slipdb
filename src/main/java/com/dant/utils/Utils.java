package com.dant.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
	// Opérations sur les dates
	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
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

	public static int dateToSecInt(Date date) {
		return (int) (date.getTime() / 1_000); // tient sur 4 octets sans problème
	}

	public static Date dateFromSecInt(int secAsInt) {
		Date date = new Date(((long)secAsInt) * 1_000);
		return date;
	}
	
}
