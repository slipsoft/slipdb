package com.dant.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
	protected static volatile int level = 0;
	protected static String prefix = "LOG";
	protected static EasyFile file;
	
	public static int getLevel() {
		return level;
	}

	public static void setLevel(int level) {
		Log.level = level;
	}

	public static void start(String file, int level) {
		Log.level = level;
		Log.file = new EasyFile(file);
	}

	/**
	 * Print a debug message in the standard output if verbose
	 * @param msg
	 */
	public static void debug(Object msg) {
		debug(msg, prefix);
	}

	/**
	 * Print a debug message in the standard output if verbose
	 * @param msg
	 * @param prefix
	 */
	public static void debug(Object msg, String prefix) {
		if (Log.level > 1) {
			info(msg.toString(), prefix);
		}
	}
	
	/**
	 * Print an info message
	 * @param msg
	 */
	public static void info(String msg) {
		info(msg, prefix);
	}
	
	/**
	 * Print an info message
	 * @param msg
	 * @param prefix
	 */
	public static void info(String msg, String prefix) {
		msg = "INFO " + prefixString(prefix) + msg;
		if (Log.level > 0) {
			System.out.println(msg);
		}
		append(msg);
	}
	
	/**
	 * Print an error message in the standard output
	 * @param msg
	 */
	public static void error(String msg) {
		error(msg, prefix);
	}
	
	/**
	 * Print an error message in the standard output
	 * @param msg
	 * @param prefix
	 */
	public static void error(String msg, String prefix) {
		msg = "ERROR " + prefixString(prefix) + msg;
		System.err.println(msg);
		append(msg);
	}
	
	/**
	 * Print an error message in the standard output
	 * @param msg
	 */
	public static void error(Exception e) {
		error(e, prefix);
	}
	
	/**
	 * Print an error message in the standard output
	 * @param msg
	 * @param prefix
	 */
	public static void error(Exception e, String prefix) {
		error(e.getMessage(), prefix);
		if (Log.level > 2) {
			e.printStackTrace();
		}
	}
	
	private static String prefixString(String prefix) {
		String[] parts = prefix.split("/");
		return "["+ String.join("][", parts) + "] ";
	}
	
	private static void append(String msg) {
		if (file != null) {
			try (FileWriter fw = new FileWriter(file, true)) {
				file.createFileIfNotExist();
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
				String date = df.format(new Date());
				fw.write(date + " " + msg + "\n");
			} catch (IOException e) {
				msg = "ERROR " + prefixString(prefix) + e.getMessage();
				System.err.println(msg);
			}
		}
	}

}
