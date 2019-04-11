package com.dant.utils;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;


public class BufferedDataInputStreamCustom extends DataInputStream {

	public BufferedDataInputStreamCustom(InputStream in) {
		super(new BufferedInputStream(in));
	}
	
	public void skipForce(int skipBytesNumber) throws IOException {
		int bytesRemaining = skipBytesNumber;// débug + 500000000;
		/*Log.infoOnly("skipForce int skipBytesNumber = " + skipBytesNumber);
		if (skipBytesNumber <= 0) {
			Log.error("skipForce ERREUR : skipBytesNumber = " + skipBytesNumber);
		}*/
		
		while (bytesRemaining > 0) {
			bytesRemaining = bytesRemaining - skipBytes(bytesRemaining); //skip(bytesRemaining);
			//if (bytesRemaining < 0) 
			//Log.infoOnly("bytesRemaining = " + bytesRemaining);
			if (bytesRemaining > 0) {
				if (7931 == bytesRemaining) try { Thread.sleep(1000); } catch (Exception e) { }
				
				Log.error("bytesRemaining = " + bytesRemaining);
			}
		}
		
	}
	
	/*
	public void negativeSkipForce(int skipBytesNumber) throws IOException {
		int bytesRemaining = skipBytesNumber;
		
		while (bytesRemaining != 0) {
			bytesRemaining = bytesRemaining - skip(bytesRemaining); //skip(bytesRemaining);
		}
		
	}*/
	
	
}
