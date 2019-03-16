package com.dant.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

	public static DateFormat staticDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
	// Opérations sur les dates
	// Instance thread-safe de DateFormat
	public DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
	// Les fonctions static utilisant ne instance donnée (DateFormat) ne sont pas thread-safe.
	// Cette fonction NE DOIT PAS être static
	// Ne PAS utiliser synchronized : très lent
	public /*synchronized static*/ Date dateFromString(String dateAsString) {
		try {
			return dateFormat.parse(dateAsString);
			// Ne pas faire :  new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(...)  est BEAUCOUP plus lent
		} catch (ParseException e) {
			return null; //new Date();
		}
	}
	
	// Les fonctions static utilisant ne instance donnée (DateFormat) ne sont pas thread-safe.
	// Cette fonction NE DOIT PAS être static
	public /*static*/ String dateToString(Date date) {
	    return dateFormat.format(date);
	}
	
	// Méthodes statiques
	
	// static mais thread-safe
	public static int dateToSecInt(Date date) {
		return (int) (date.getTime() / 1_000); // tient sur 4 octets sans problème
	}
	
	// static mais thread-safe
	public static Date dateFromSecInt(int secAsInt) {
		Date date = new Date(((long)secAsInt) * 1_000);
		return date;
	}
	
	public static String dateToStringNoThreadSafe(Date date) {
	    return staticDateFormat.format(date);
	}
	
	public static Date dateFromStringNoThreadSafe(String dateAsString) {
		try {
			return staticDateFormat.parse(dateAsString);
			// Ne pas faire :  new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(...)  est BEAUCOUP plus lent
		} catch (ParseException e) {
			return null; //new Date();
		}
	}
}
