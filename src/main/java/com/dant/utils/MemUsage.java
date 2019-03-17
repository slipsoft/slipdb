package com.dant.utils;

import java.text.NumberFormat;

public class MemUsage {
	
	public static void printMemUsage() {
		Runtime runtime = Runtime.getRuntime();

		NumberFormat format = NumberFormat.getInstance();

		//StringBuilder sb = new StringBuilder();
		//long maxMemory = runtime.maxMemory();
		long allocatedMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		
		
		long usedMemory = allocatedMemory - freeMemory;
		
		Log.logInfoMessage("Usage: " + format.format(usedMemory / 1024), "MEMORY", 3);
		
		/*
		Log.info("free memory: " + format.format(freeMemory / 1024));
		Log.info("allocated memory: " + format.format(allocatedMemory / 1024));
		Log.info("max memory: " + format.format(maxMemory / 1024));
		Log.info("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024));*/
		
		
		/*
		sb.append("free memory: " + format.format(freeMemory / 1024) + "<br/>");
		sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + "<br/>");
		sb.append("max memory: " + format.format(maxMemory / 1024) + "<br/>");
		sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "<br/>");
		*/
		
		
		
	}
}
