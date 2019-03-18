package com.dant.utils;

import java.text.DecimalFormat;

/**
 * 
 * @author Nicolas
 *
 */
public class Timer {
	public final static int ONE_MILLION  =     1_000_000;
	public final static int TEN_MILLIONS =    10_000_000;
	public final static int ONE_BILLION  = 1_000_000_000;
	
	protected long start;
	protected String label;
	protected String prefix = "TIMER";
	
	public Timer(String label) {
		this.start = System.nanoTime();
		this.label = label;
	}
	
	public Timer(String label, String prefix) {
		this(label);
		this.prefix = prefix;
	}
	
	/**
	 * Calculates the duration of the timer.
	 * @return
	 */
	protected long duration() {
		return System.nanoTime() - start;
	}
	
	/**
	 * Duration of the timer in nanoseconds.
	 * @return
	 */
	public long getns() {
		return duration();
	}
	
	/**
	 * Duration of the timer in milliseconds.
	 * @return
	 */
	public long getms() {
		return duration() / ONE_MILLION;
	}

	public long getseconds() {
		return duration() / ONE_BILLION;
	}
	
	/**
	 * Duration String using the appropriate format.
	 * @return
	 */
	public String pretty() {
		long duration = duration();
		if (duration - TEN_MILLIONS < 0) {
			return new DecimalFormat("##.00 millis").format((double) duration / ONE_MILLION);
		} else if (duration - ONE_BILLION < 0) {
			return new DecimalFormat("##0.000 s").format((double) duration / ONE_BILLION);
		} else {
			return new DecimalFormat("### ##0.00s").format((double) duration / ONE_BILLION);
		}
	}
	
	/**
	 * Log duration in nanoseconds.
	 */
	public void logns() {
		Log.logInfoMessage(label + ": " + getns() + " nanos", prefix, 3);
	}
	
	/**
	 * Log duration in milliseconds.
	 */
	public void logms() {
		Log.logInfoMessage(label + ": " + getms() + " millis", prefix, 3);
	}
	
	/**
	 * Log duration using the appropriate format.
	 */
	public void log() {
		Log.logInfoMessage(label + ": " + pretty(), prefix, 3);
	}
	
}
