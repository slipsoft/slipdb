package com.dant.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.ArrayUtils;

public class Log {
	protected static volatile int level = 3;
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
	 * @param file - name of the file to use for this log
	 * @param level - 0: quiet, 1: info, 2: debug, 3: stacktrace
	 * @throws IOException if can't create file
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
	 * @param msg - the message to log
	 */
	public static void info(String msg) {
		logInfoMessage(msg, "", 3);
	}
	
	/**
	 * Log an info message.
	 * Print in the standard output if log level >= 1.
	 * 
	 * @param msg - the message to log
	 * @param prefix - prefix of the message
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
	 * @param obj - the object to debug
	 */
	public static void debug(Object obj) {
		logDebugMessage(obj, "", 3);
	}

	/**
	 * Log & Print a debug message if log level >= 2.
	 * 
	 * @param obj - the object to debug
	 * @param prefix - prefix of the debug
	 */
	public static void debug(Object obj, String prefix) {
		logDebugMessage(obj, prefix, 3);
	}
	
	protected static void logDebugMessage(Object obj, String prefix, int depth) {
		if (Log.level >= 2) {
			String msg;
			if (obj == null) {
				msg = "null";
			} else if (obj.getClass().isArray()) {
				msg = ArrayUtils.toString(obj);
			} else {
				msg = obj.toString();
			}
			msg = prefixString("DEBUG/" + prefix) + msg + getCaller(depth);
			System.out.println(msg);
			append(msg);
		}
	}

	/**
	 * Log & Print a warning message from an exception.
	 *
	 * @param e - the error to log
	 */
	public static void warning(Exception e) {
		logWarningMessage(e.getMessage(), "", 3);
	}

	/**
	 * Log & Print a warning message from an exception.
	 *
	 * @param e - the error to log
	 * @param prefix - prefix of the error
	 */
	public static void warning(Exception e, String prefix) {
		logWarningMessage(e.getMessage(), prefix, 3);
	}

	protected static void logWarningMessage(String msg, String prefix, int depth) {
		msg = prefixString("WARNING/" + prefix) + msg + getCaller(depth);
		System.err.println(msg);
		append(msg);
	}
	
	/**
	 * Log & Print an error message.
	 * Print StackTrace in the standard output if log level >= 3.
	 * 
	 * @param e - the error to log
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
	 * @param e - the error to log
	 * @param prefix - prefix of the error
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
	 * @param msg - the message to log as an error
	 */
	public static void error(String msg) {
		logErrorMessage(msg, "", 3);
	}
	
	/**
	 * Log & Print an error message.
	 * Print StackTrace in the standard output if log level >= 3.
	 * 
	 * @param msg - the message to log as an error
	 * @param prefix - prefix of the error
	 */
	public static void error(String msg, String prefix) {
		logErrorMessage(msg, prefix, 3);
	}
	
	protected static void logErrorMessage(String msg, String prefix, int depth) {
		msg = prefixString("ERROR/" + prefix) + msg + getCaller(depth);
		System.err.println(msg);
		append(msg);
	}
	
	private static String getCaller(int depth) {
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
