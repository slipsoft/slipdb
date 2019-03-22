package com.dant.utils;

import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class LogTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Log.start("logTest", 2);
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testGetLevel() {
		assertEquals(2, Log.getLevel());
    }

	@Test
	void testSetLevel() {
		Log.setLevel(2);
		assertEquals(2, Log.level);
	}

	@Test
	void testStart() {
		Executable exec = new Executable() {
			
			@Override
			public void execute() throws Throwable {
				Log.start("logTest", 2);
			}
		};
		assertDoesNotThrow(exec);
	}

	@Test
	void testInfoString() {
		Log.info("testInfoString");
	}

	@Test
	void testInfoStringString() {
		Log.info("testInfoString", "YO");
	}

}
