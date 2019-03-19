package com.dant.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.ArrayUtils;

public class Log {
	protected static volatile int level = 0;
	protected static EasyFile file;
	
	public static int getLevel() {
		return level;
	}

	/**
	 * Set log level.
	 * It determines how much messages are displayed in the console.
	 * 
	 * @param level - 0: quiet, 1: info, 2: debug, 3: stacktrace
	 */
	public static void setLevel(int level) {
		Log.level = level;
	}

	/**
	 * Start a log session by setting its file and level.
	 * The level determines how much messages are displayed in the console.
	 * 
	 * @param file
	 * @param level - 0: quiet, 1: info, 2: debug, 3: stacktrace
	 * @throws IOException 
	 */
	public static void start(String file, int level) throws IOException {
		Log.level = level;
		Log.file = new EasyFile("target/logs/" + file + ".log");
		Log.file.createFileIfNotExist();
	}
	
	/**
	 * Log an info message.
	 * Print in the standard output if log level >= 1.
	 * 
	 * @param msg
	 */
	public static void info(String msg) {
		logInfoMessage(msg, "", 3);
	}
	
	/**
	 * Log an info message.
	 * Print in the standard output if log level >= 1.
	 * 
	 * @param msg
	 * @param prefix
	 */
	public static void info(String msg, String prefix) {
		logInfoMessage(msg, prefix, 3);
	}
	
	protected static void logInfoMessage(String msg, String prefix, int depth) {
		msg = prefixString("INFO/" + prefix) + msg + getCaller(depth);
		if (Log.level >= 1) {
			System.out.println(msg);
		}
		append(msg);
	}

	/**
	 * Log & Print a debug message if log level >= 2.
	 * 
	 * @param obj
	 */
	public static void debug(Object obj) {
		logDebugMessage(obj, "", 3);
	}

	/**
	 * Log & Print a debug message if log level >= 2.
	 * 
	 * @param obj
	 * @param prefix
	 */
	public static void debug(Object obj, String prefix) {
		logDebugMessage(obj, prefix, 3);
	}
	
	protected static void logDebugMessage(Object obj, String prefix, int depth) {
		if (Log.level >= 2) {
			String msg = obj.toString();
			if (obj.getClass().isArray()) {
				msg = ArrayUtils.toString(obj);
			}
			msg = prefixString("DEBUG/" + prefix) + msg + getCaller(depth);
			System.out.println(msg);
			append(msg);
		}
	}
	
	/**
	 * Log & Print an error message.
	 * Print StackTrace in the standard output if log level >= 3.
	 * 
	 * @param msg
	 */
	public static void error(Exception e) {
		logErrorMessage(e.getMessage(), "", 3);
		if (Log.level >= 3) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Log & Print an error message.
	 * Print StackTrace in the standard output if log level >= 3.
	 * 
	 * @param msg
	 * @param prefix
	 */
	public static void error(Exception e, String prefix) {
		logErrorMessage(e.getMessage(), prefix, 3);
		if (Log.level >= 3) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Log & Print an error message.
	 * Print StackTrace in the standard output if log level >= 3.
	 * 
	 * @param msg
	 */
	public static void error(String msg) {
		logErrorMessage(msg, "", 3);
	}
	
	/**
	 * Log & Print an error message.
	 * Print StackTrace in the standard output if log level >= 3.
	 * 
	 * @param msg
	 * @param prefix
	 */
	public static void error(String msg, String prefix) {
		logErrorMessage(msg, prefix, 3);
	}
	
	protected static void logErrorMessage(String msg, String prefix, int depth) {
		msg = prefixString("ERROR/" + prefix) + msg + getCaller(depth);
		System.err.println(msg);
		append(msg);
	}
	
	public static String getCaller(int depth) {
		if (level >= 2) {
			StackTraceElement elem =  new Throwable().getStackTrace()[depth];
			return " (" + elem.getFileName() + ":" + elem.getLineNumber() + ")";
		} else {
			return "";
		}
	}
	
	private static String prefixString(String prefix) {
		String[] parts = prefix.split("/");
		return "["+ String.join("][", parts) + "] ";
	}
	
	private static void append(String msg) {
		if (file != null) {
			try (FileWriter fw = new FileWriter(file, true)) {
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
				String date = df.format(new Date());
				fw.write(date + " " + msg + "\n");
			} catch (IOException e) {
				msg = prefixString("ERROR") + e.getMessage();
				System.err.println(msg);
			}
		}
	}
}
