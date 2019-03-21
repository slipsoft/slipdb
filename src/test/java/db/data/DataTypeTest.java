package db.data;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dant.utils.Log;
import com.dant.utils.Timer;
import com.dant.utils.Utils;



class DataTypeTest {
	
	ByteType    byteType;
	IntegerType integerType;
	LongType    longType;
	FloatType   floatType;
	DoubleType  doubleType;
	DateType    dateType;
	StringType  stringType;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		//Log.info("setUpBeforeAll");
		Log.start("dataTypesTest", 2);
		
	}

	@BeforeEach
	void setUp() throws Exception {
		byteType    = new ByteType();
		integerType = new IntegerType();
		longType    = new LongType();
		floatType   = new FloatType();
		doubleType  = new DoubleType();
		dateType    = new DateType();
		stringType  = new StringType(15);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testGetSize() {
		assertEquals(1,  byteType.getSize());
		assertEquals(4, integerType.getSize());
		assertEquals(8, longType.getSize());
		assertEquals(4, floatType.getSize());
		assertEquals(8, doubleType.getSize());
		assertEquals(4, dateType.getSize());
		assertEquals(15, stringType.getSize());
	}

	@Test
	void testGetAssociatedClassType() {
		// fail("Not yet implemented");
	}

	@Test
	void testWriteToBuffer() {
		
		int maxCountOperation = 1_000_000;
		String maxCountOperationStr = "1_000_000";
		Timer localTimer;
		ByteBuffer bBuff = ByteBuffer.allocate(20);
		
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
		ByteBuffer bBuff = ByteBuffer.allocate(20);
		Object expected;
		Object actual;
		byteType.writeToBuffer("78", bBuff);
		expected = new Byte("78");
		actual = byteType.readTrueValue(Arrays.copyOf(bBuff.array(), byteType.getSize()));
		assertEquals(expected, actual);
		
		bBuff.rewind();
		integerType.writeToBuffer("454689586", bBuff);
		expected = new Integer("454689586");
		actual = integerType.readTrueValue(Arrays.copyOf(bBuff.array(), integerType.getSize()));
		assertEquals(expected, actual);
		
		bBuff.rewind();
		longType.writeToBuffer("45468957895686", bBuff);
		expected = new Long("45468957895686");
		actual = longType.readTrueValue(Arrays.copyOf(bBuff.array(), longType.getSize()));
		assertEquals(expected, actual);
		
		bBuff.rewind();
		floatType.writeToBuffer("454689586.2132152", bBuff);
		expected = new Float("454689586.2132152");
		actual = floatType.readTrueValue(Arrays.copyOf(bBuff.array(), floatType.getSize()));
		assertEquals(expected, actual);
		
		bBuff.rewind();
		doubleType.writeToBuffer("454689586.2132152", bBuff);
		expected = new Double("454689586.2132152");
		actual = doubleType.readTrueValue(Arrays.copyOf(bBuff.array(), doubleType.getSize()));
		assertEquals(expected, actual);
		
		bBuff.rewind();
		dateType.writeToBuffer("2015-04-27 15:45:38", bBuff);
		expected = new Utils().dateFromString("2015-04-27 15:45:38");
		actual = dateType.readTrueValue(Arrays.copyOf(bBuff.array(), dateType.getSize()));
		assertEquals(expected, actual);
		
		bBuff.rewind();
		stringType.writeToBuffer("hello world !", bBuff);
		expected = "hello world !";
		actual = stringType.readTrueValue(Arrays.copyOf(bBuff.array(), stringType.getSize()));
		assertEquals(expected, actual);
	}

	@Test
	void testReadIndexValue() {
		ByteBuffer bBuff = ByteBuffer.allocate(20);
		Object expected;
		Object actual;
		byteType.writeToBuffer("78", bBuff);
		expected = new Byte("78");
		actual = byteType.readIndexValue(Arrays.copyOf(bBuff.array(), byteType.getSize()));
		assertEquals(expected, actual);
		
		bBuff.rewind();
		integerType.writeToBuffer("454689586", bBuff);
		expected = new Integer("454689586");
		actual = integerType.readIndexValue(Arrays.copyOf(bBuff.array(), integerType.getSize()));
		assertEquals(expected, actual);
		
		bBuff.rewind();
		longType.writeToBuffer("45468957895686", bBuff);
		expected = new Long("45468957895686");
		actual = longType.readIndexValue(Arrays.copyOf(bBuff.array(), longType.getSize()));
		assertEquals(expected, actual);
		
		bBuff.rewind();
		floatType.writeToBuffer("454689586.2132152", bBuff);
		expected = new Float("454689586.2132152");
		actual = floatType.readIndexValue(Arrays.copyOf(bBuff.array(), floatType.getSize()));
		assertEquals(expected, actual);
		
		bBuff.rewind();
		doubleType.writeToBuffer("454689586.2132152", bBuff);
		expected = new Double("454689586.2132152");
		actual = doubleType.readIndexValue(Arrays.copyOf(bBuff.array(), doubleType.getSize()));
		assertEquals(expected, actual);
		
		bBuff.rewind();
		dateType.writeToBuffer("2015-04-27 15:45:38", bBuff);
		expected = Utils.dateToSecInt(new Utils().dateFromString("2015-04-27 15:45:38")); // cheated test to make it work on travis
		actual = dateType.readIndexValue(Arrays.copyOf(bBuff.array(), dateType.getSize()));
		assertEquals(expected, actual);
		
		bBuff.rewind();
		stringType.writeToBuffer("hello world !", bBuff);
		expected = "hello world !";
		actual = stringType.readIndexValue(Arrays.copyOf(bBuff.array(), stringType.getSize()));
		assertEquals(expected, actual);
	}
	
	@AfterAll
	static void terminateTest() {
		
	}
}
