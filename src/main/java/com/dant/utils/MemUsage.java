package com.dant.utils;

import java.text.NumberFormat;

public class MemUsage {
	

	public static void printMemUsage() {
		printMemUsage("", 4, 4);
		
	}

	public static void printMemUsage(String message) {
		printMemUsage(message, 4, 4);
	}
	
	public static long getMemUsage() {
		Runtime runtime = Runtime.getRuntime();
		long allocatedMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		long usedMemory = allocatedMemory - freeMemory;
		return usedMemory;
	}
	
	public static String formatMemUsage(long usedMemory) {
		NumberFormat format = NumberFormat.getInstance();
		return format.format(usedMemory / 1024);
	}


	public static void printMemUsage(String message, int logLevel, int logDepth) {
		/*Runtime runtime = Runtime.getRuntime();
		
		
		//StringBuilder sb = new StringBuilder();
		//long maxMemory = runtime.maxMemory();
		long allocatedMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();*/
		long usedMemory = getMemUsage();//allocatedMemory - freeMemory;
		String formattedMemory = formatMemUsage(usedMemory);
		
		//NumberFormat format = NumberFormat.getInstance();
		
		
		if (message == null) message = "";
		if (message.equals("") == false) message += " ";
		
		//Log.logInfoMessage(message + format.format(usedMemory / 1024), "MEMORY", 3);
		Log.logInfoMessage(message + formattedMemory, "MEMORY", logDepth);
		
		/*
		Log.info("free memory: " + format.format(freeMemory / 1024));
		Log.info("allocated memory: " + format.format(allocatedMemory / 1024));
		Log.info("max memory: " + format.format(maxMemory / 1024));
		Log.info("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024));*/
		
	}
	
	public static void printMemUsage(String message, int logLevel) {
		printMemUsage(message, logLevel, 4);
	}
}
