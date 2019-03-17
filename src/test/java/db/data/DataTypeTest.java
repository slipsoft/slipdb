package db.data;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dant.utils.Log;
import com.dant.utils.Timer;



class DataTypeTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		//Log.info("setUpBeforeAll");
		Log.start("target/slipdb_DataTypesTest.log", 3);
		
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testGetSize() {
		assertEquals(1,  new ByteType().getSize());
		assertEquals(32, new IntegerType().getSize());
		assertEquals(64, new LongType().getSize());
		assertEquals(32, new FloatType().getSize());
		assertEquals(64, new DoubleType().getSize());
		assertEquals(32, new DateType().getSize());
		assertEquals(10, new StringType(10).getSize());
	}

	@Test
	void testGetAssociatedClassType() {
		fail("Not yet implemented");
	}

	@Test
	void testWriteToBuffer() {
		
		int maxCountOperation = 5_000_000;
		String maxCountOperationStr = "5_000_000";
		Timer localTimer;
		ByteBuffer bBuff = ByteBuffer.allocate(20);
		
		ByteType   byteType = new ByteType();
		DoubleType doubleType = new DoubleType();
		DateType   dateType = new DateType();
		IntegerType integerType = new IntegerType();
		FloatType floatType = new FloatType();
		LongType longType = new LongType();
		
		Log.info("Cout en millisecondes, pour " + maxCountOperationStr + " itérations :");
		
		localTimer = new Timer("Cout de fonctionnement de la boule pour " + maxCountOperationStr + " opérations ");
		for (int count = 0; count < maxCountOperation; count++) {
			bBuff.rewind();
		}
		localTimer.log();
		
		localTimer = new Timer("Parsing de ByteType ");
		for (int count = 0; count < maxCountOperation; count++) {
			bBuff.rewind();
			byteType.writeToBuffer("78", bBuff);
		}
		localTimer.log();

		localTimer = new Timer("Parsing de IntegerType ");
		for (int count = 0; count < maxCountOperation; count++) {
			bBuff.rewind();
			integerType.writeToBuffer("454689586", bBuff);
		}
		localTimer.log();

		localTimer = new Timer("Parsing de LongType ");
		for (int count = 0; count < maxCountOperation; count++) {
			bBuff.rewind();
			longType.writeToBuffer("45468957895686", bBuff);
		}
		localTimer.log();
		
		localTimer = new Timer("Parsing de FloatType ");
		for (int count = 0; count < maxCountOperation; count++) {
			bBuff.rewind();
			floatType.writeToBuffer("454689586.2132152", bBuff);
		}
		localTimer.log();
		
		localTimer = new Timer("Parsing de DoubleType ");
		for (int count = 0; count < maxCountOperation; count++) {
			bBuff.rewind();
			doubleType.writeToBuffer("454689586.2132152", bBuff);
		}
		localTimer.log();
		
		localTimer = new Timer("Parsing de DateType ");
		for (int count = 0; count < maxCountOperation; count++) {
			bBuff.rewind();
			dateType.writeToBuffer("2015-04-27 15:45:38", bBuff);
		}
		localTimer.log();
		
		/** Valeurs mise à jour (Sylvain)
		 	Pour 5_000_000 opérations : 
		    Cout fonctionnement de la boucle pour 5_000_000 opérations : 8 ms
		    Parsing de ByteType    : 27 ms
			Parsing de IntegerType : 134 ms
			Parsing de FloatType   : 847 ms
			Parsing de DoubleType  : 928 ms
			Parsing de DateType    : 4791 ms
		 */
		
		
		// Pour 10_000_000 sur le PC de Sylvain
		// Date   : 9 700 ms
		// Double : 1 654 ms
		// Byte   :    35 ms
		// Boucle :    10 ms
		
		
	}

	@Test
	void testReadTrueValue() {
		fail("Not yet implemented");
	}

	@Test
	void testReadIndexValue() {
		fail("Not yet implemented");
	}
	
	@AfterAll
	static void terminateTest() {
		
	}
}
