package com.dant.utils;

import java.io.IOException;
import java.io.Serializable;

/**
 * This EasyFile class extends java.io.File,
 * it's purpose is to make the use of files easier, providing with useful functions.
 *
 */
public class EasyFile extends java.io.File implements Serializable {
	
	private static final long serialVersionUID = 7805241382356378021L;


	/** File creation
	 *  @param pathname
	 */
	public EasyFile(String pathname) {
		super(pathname);
	}
	
	
	public void createFileIfNotExist() throws IOException {
		if (!exists()) {
			java.io.File dirObject = getParentFile();
			if (!dirObject.exists()) {
				dirObject.mkdirs(); // create directories
			}
			/*super.*/createNewFile();
		}
	}

}
