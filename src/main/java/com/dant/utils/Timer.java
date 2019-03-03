package com.dant.utils;


/**
 * 
 * @author Nicolas
 *
 */
public class Timer {
	protected long start;
	protected String label;
	
	public Timer(String label) {
		this.start = System.nanoTime();
		this.label = label;
	}
	
	protected long diff() {
		return System.nanoTime() - start;
	}
	
	public void printns() {
		Log.info(label + ": " + diff() + " ns");
	}
	
	public void printms() {
		Log.info(label + ": " + diff() / 1000000 + " ms");
	}
	
}
