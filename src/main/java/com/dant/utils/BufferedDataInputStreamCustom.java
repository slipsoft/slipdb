package com.dant.utils;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;


public class BufferedDataInputStreamCustom extends DataInputStream {

	public BufferedDataInputStreamCustom(InputStream in) {
		super(new BufferedInputStream(in));
	}
	
	public void skipForce(int skipBytesNumber) throws IOException {
		int bytesRemaining = skipBytesNumber;
		
		while (bytesRemaining > 0) {
			bytesRemaining = bytesRemaining - skipBytes(bytesRemaining); //skip(bytesRemaining);
		}
		
	}
	
	
}
