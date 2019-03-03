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
		
		System.out.println("Used Memory = " + format.format(usedMemory / 1024));
		
		/*
		System.out.println("free memory: " + format.format(freeMemory / 1024));
		System.out.println("allocated memory: " + format.format(allocatedMemory / 1024));
		System.out.println("max memory: " + format.format(maxMemory / 1024));
		System.out.println("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024));*/
		
		
		/*
		sb.append("free memory: " + format.format(freeMemory / 1024) + "<br/>");
		sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + "<br/>");
		sb.append("max memory: " + format.format(maxMemory / 1024) + "<br/>");
		sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "<br/>");
		*/
		
		
		
	}
}
