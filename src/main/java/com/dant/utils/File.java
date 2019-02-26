package com.dant.utils;

import java.io.IOException;

@SuppressWarnings("serial")
public class File extends java.io.File {

	public File(String pathname) {
		super(pathname);
	}

	protected void createFileIfNotExist() throws IOException {
		if (!exists()) {
			java.io.File dirObject = getParentFile();
			if (!dirObject.exists()) {
				dirObject.mkdirs();
			}
			createNewFile();
		}
	}

}
