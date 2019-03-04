package com.dant.utils;

import java.io.IOException;

@SuppressWarnings("serial")
public class EasyFile extends java.io.File {

	public EasyFile(String pathname) {
		super(pathname);
	}

	public void createFileIfNotExist() throws IOException {
		if (!exists()) {
			java.io.File dirObject = getParentFile();
			if (!dirObject.exists()) {
				dirObject.mkdirs();
			}
			createNewFile();
		}
	}

}
