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
		System.out.println(label + ": " + diff() + " ns");
	}
	
	public void printµs() {
		System.out.println(label + ": " + diff() / 1000 + " µs");
	}
	
	public void printms() {
		
		System.out.println(label + ": " + diff() / 1000000 + " ms");
	}
	
}
