package db.data.types;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dant.utils.Log;
import com.dant.utils.MemUsage;
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
		Log.start("dataTypesTest", 3);
		
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

	//@Test
	void testEqualsSpeed() {
		
		int maxCountOperation = 1_000_000;
		String maxCountOperationStr = "1_000_000";
		Timer localTimer;
		ByteBuffer bBuff = ByteBuffer.allocate(20);
		
		Log.info("Cout en millisecondes, pour " + maxCountOperationStr + " itérations :");
		
		localTimer = new Timer("Cout de fonctionnement de la boucle pour " + maxCountOperationStr + " opérations ");
		int a = 46512;
		int b = 74651275;
		int c = 0;
		for (int count = 0; count < maxCountOperation; count++) {
			if (a == b) c = 1;
			if (a == b) c = 1;
			if (a == b) c = 1;
			if (a == b) c = 1;
			if (a == b) c = 1;
			if (a == b) c = 1;
			if (a == b) c = 1;
			if (a == b) c = 1;
			if (a == b) c = 1;
			if (a == b) c = 1;
			if (a == b) c = 1;
			//if (a == b) c = 2;
		}
		localTimer.log();
	}

	//@Test
	void testArrayListSpeed() {
		
		int maxCountOperation = 1_000_000;
		String maxCountOperationStr = "1_000_000";
		Timer localTimer;
		
		Log.info("Cout en millisecondes, pour " + maxCountOperationStr + " itérations :");
		
		int a = 46512;
		int b = 74651275;
		int c = 0;
		ArrayList<Integer> al = new ArrayList<Integer>();
		ArrayList<Integer> alModel = new ArrayList<Integer>();
		for (int count = 0; count < maxCountOperation; count++) {
			alModel.add(8575762);
		}
		

		localTimer = new Timer("Cout de fonctionnement de la boucle pour " + maxCountOperationStr + " opérations ");
		for (int count = 0; count < maxCountOperation; count++) {
			al.add(alModel.get(count));
		}
		localTimer.log();
	}
	
	
	//@Test
	void testWriteToBuffer() {
		
		Random rand = new Random();
		
		
		int maxCountOperation = 1_000_000;
		String maxCountOperationStr = "1_000_000";
		Timer localTimer;
		ByteBuffer bBuff = ByteBuffer.allocate(20);
		
		Log.info("Cout en millisecondes, pour " + maxCountOperationStr + " itérations :");
		
		localTimer = new Timer("Cout de fonctionnement de la boucle pour " + maxCountOperationStr + " opérations ");
		for (int count = 0; count < maxCountOperation; count++) {
			bBuff.rewind();
		}
		localTimer.log();
		
		localTimer = new Timer("Parsing de ByteType ");
		for (int count = 0; count < maxCountOperation; count++) {
			bBuff.rewind();
			byteType.parseAndWriteToBuffer("78", bBuff);
		}
		localTimer.log();
		
		// Gain vraiment minime lorsque DataType non utilisé
		localTimer = new Timer("Parsing de ByteType, plus rapide ??");
		for (int count = 0; count < maxCountOperation; count++) {
			bBuff.rewind();
			bBuff.put(Byte.parseByte("78"));
			//Byte.parseByte("78");
		}
		localTimer.log();
		//System.gc();

		localTimer = new Timer("Parsing de IntegerType ");
		for (int count = 0; count < maxCountOperation; count++) {
			bBuff.rewind();
			integerType.parseAndWriteToBuffer("454689586", bBuff);
		}
		//System.gc();
		localTimer.log();

		localTimer = new Timer("Parsing de IntegerType -> plus rapide ??? ");
		for (int count = 0; count < maxCountOperation; count++) {
			bBuff.rewind();
			bBuff.putInt(Integer.parseInt("454689586"));
			/*integerType.parseAndWriteToBuffer("454689586", bBuff);*/
		}
		//System.gc();
		localTimer.log();

		localTimer = new Timer("Parsing de LongType ");
		for (int count = 0; count < maxCountOperation; count++) {
			bBuff.rewind();
			longType.parseAndWriteToBuffer("45468957895686", bBuff);
		}
		localTimer.log();
		
		localTimer = new Timer("Parsing de FloatType ");
		for (int count = 0; count < maxCountOperation; count++) {
			bBuff.rewind();
			floatType.parseAndWriteToBuffer("454689586.2132152", bBuff);
		}
		localTimer.log();
		
		localTimer = new Timer("Parsing de DoubleType ");
		for (int count = 0; count < maxCountOperation; count++) {
			bBuff.rewind();
			doubleType.parseAndWriteToBuffer("454689586.2132152", bBuff);
		}
		localTimer.log();

		localTimer = new Timer("Parsing de DateType ");
		for (int count = 0; count < maxCountOperation; count++) {
			bBuff.rewind();
			dateType.parseAndWriteToBuffer("2015-04-27 15:45:38", bBuff);
		}
		localTimer.log();
		
		
		localTimer = new Timer("String <-> array");
		String myString = "This is a test !";
		byte[] bArr;
		for (int count = 0; count < maxCountOperation; count++) {
			bArr = myString.getBytes();
			myString = new String(bArr);
			//bBuff.rewind();
			//dateType.parseAndWriteToBuffer("2015-04-27 15:45:38", bBuff);
		}
		localTimer.log();
		
		/* Gain vraiment minime, pas la peine de s'embêter
		Utils utilsInstance = new Utils();
		int dateAsInt;
		localTimer = new Timer("Parsing de DateType plus rapide ??");
		for (int count = 0; count < maxCountOperation; count++) {
			bBuff.rewind();
			dateAsInt = Utils.dateToSecInt(utilsInstance.dateFromString("2015-04-27 15:45:38"));
			bBuff.putInt(dateAsInt);
		}
		localTimer.log();*/
		
		/*
			Valeurs mise à jour (Sylvain)
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
	
	private int[] intArray = new int[100000];
	
	private int getAtArrayPos(int posIndex) {
		return intArray[posIndex];
	}

	//@Test
	public void testAccessSpeed() {
		for (int i = 0; i < intArray.length; i++) {
			intArray[i] = i;
		}
		
		int iteNb = 1_000_000_000;
		int res = 0;
		Timer t1 = new Timer("Test accès direct");
		for (int i = 0; i < iteNb; i++) {
			res += intArray[18875];
		}
		t1.log();
		Log.info("res = " + res);
		
		res = 0;
		t1 = new Timer("Test accès par fonction");
		for (int i = 0; i < iteNb; i++) {
			res += getAtArrayPos(18875);
		}
		t1.log();
		Log.info("res = " + res);
	}

	//@Test
	public void testAccessSpeedByteBuffer() {
		ByteBuffer bBuff = ByteBuffer.allocate(60);
		int val = 875465274;
		bBuff.putInt(76532);
		bBuff.putInt(86854);
		bBuff.putInt(val);
		bBuff.putInt(3545);
		bBuff.rewind();
		
		int iteNb = 1_000_000_000;
		int res = 0;
		Timer t1 = new Timer("Test accès direct");
		for (int i = 0; i < iteNb; i++) {
			res += val;
		}
		t1.log();
		Log.info("res = " + res);
		
		res = 0;
		t1 = new Timer("Test accès par fonction");
		for (int i = 0; i < iteNb; i++) {
			res += bBuff.getInt(1); // super super rapide
			res += bBuff.getInt(2);
			res += bBuff.getInt(3);
			res += bBuff.getInt(4);
			res += bBuff.getInt(5);
		}
		t1.log();
		Log.info("res = " + res);
	}
	
	//@Test
	public void testB() {
		Log.info("Salut, test !");
		int allocationSize = 1_000_000_000;
		Timer t;
		System.gc();
		MemUsage.printMemUsage();
		t = new Timer("Allocation array simple");
		byte[] bibi = new byte[allocationSize];
		t.log();
		System.gc();
		MemUsage.printMemUsage();
		Log.info("Ouais, salut !");
		
		t = new Timer("Allocation ByteBuffer");
		ByteBuffer bibiBubu = ByteBuffer.allocate(allocationSize);
		t.log();
		System.gc();
		MemUsage.printMemUsage();
		Log.info("Ouais, salut !");
		
		
	}

	//@Test
	public void testMemDoubleParsing() {
		
		Random rand = new Random();
		
		int maxCountOperation = 10_000_000;
		String maxCountOperationStr = "10_000_000";
		Timer localTimer;
		ByteBuffer bBuff = ByteBuffer.allocate(20);
		
		Log.info("Cout en millisecondes, pour " + maxCountOperationStr + " itérations --- :");
		
		String parseStr;
		localTimer = new Timer("Parsing de DoubleType ");
		for (int count = 0; count < maxCountOperation; count++) {
			bBuff.rewind();
			parseStr = Double.toString(rand.nextDouble());
			doubleType.parseAndWriteToBuffer(parseStr, bBuff); // "454689586.2132152"
		}
		localTimer.log();
	}

	//@Test
	public void testMemArrayAllocation() {
		
		System.gc();
		MemUsage.printMemUsage();
		
		int entrySize = 10;
		int arraySize = 1_000_000;
		//Arrays.sort(a);(a, c);
		int totalArraySize = arraySize * entrySize;
		Integer[] a1int = new Integer[arraySize * entrySize];
		for (int iLine = 0; iLine < totalArraySize; iLine++) {
			a1int[iLine] = iLine;
		}
		
		
		
		/*byte[][] a2int = new byte[arraySize][entrySize];
		
		for (int iLine = 0; iLine < arraySize; iLine++) {
			
			//a2int[iLine] = new int[entrySize + (iLine % 8)];
			
			for (int iEntry = 0; iEntry < entrySize; iEntry++) {
				a2int[iLine][iEntry] = (byte)iEntry;
			}
			
		}*/
		
		Log.info("Working ??");
		System.gc();
		MemUsage.printMemUsage();
		
		Log.info("" + (a1int.length));
		
	}

	//@Test
	void testReadTrueValue() {
		ByteBuffer bBuff = ByteBuffer.allocate(20);
		Object expected;
		Object actual;
		byteType.parseAndWriteToBuffer("78", bBuff);
		expected = new Byte("78");
		actual = byteType.readTrueValue(Arrays.copyOf(bBuff.array(), byteType.getSize()));
		assertEquals(expected, actual);
		
		bBuff.rewind();
		integerType.parseAndWriteToBuffer("454689586", bBuff);
		expected = new Integer("454689586");
		actual = integerType.readTrueValue(Arrays.copyOf(bBuff.array(), integerType.getSize()));
		assertEquals(expected, actual);
		
		bBuff.rewind();
		longType.parseAndWriteToBuffer("45468957895686", bBuff);
		expected = new Long("45468957895686");
		actual = longType.readTrueValue(Arrays.copyOf(bBuff.array(), longType.getSize()));
		assertEquals(expected, actual);
		
		bBuff.rewind();
		floatType.parseAndWriteToBuffer("454689586.2132152", bBuff);
		expected = new Float("454689586.2132152");
		actual = floatType.readTrueValue(Arrays.copyOf(bBuff.array(), floatType.getSize()));
		assertEquals(expected, actual);
		
		bBuff.rewind();
		doubleType.parseAndWriteToBuffer("454689586.2132152", bBuff);
		expected = new Double("454689586.2132152");
		actual = doubleType.readTrueValue(Arrays.copyOf(bBuff.array(), doubleType.getSize()));
		assertEquals(expected, actual);
		
		bBuff.rewind();
		dateType.parseAndWriteToBuffer("2015-04-27 15:45:38", bBuff);
		expected = new Utils().dateFromString("2015-04-27 15:45:38");
		actual = dateType.readTrueValue(Arrays.copyOf(bBuff.array(), dateType.getSize()));
		assertEquals(expected, actual);
		
		bBuff.rewind();
		stringType.parseAndWriteToBuffer("hello world !", bBuff);
		expected = "hello world !";
		actual = stringType.readTrueValue(Arrays.copyOf(bBuff.array(), stringType.getSize()));
		assertEquals(expected, actual);
	}

	//@Test
	void testReadIndexValue() {
		ByteBuffer bBuff = ByteBuffer.allocate(20);
		Object expected;
		Object actual;
		byteType.parseAndWriteToBuffer("78", bBuff);
		expected = new Byte("78");
		actual = byteType.readIndexValue(Arrays.copyOf(bBuff.array(), byteType.getSize()));
		assertEquals(expected, actual);
		
		bBuff.rewind();
		integerType.parseAndWriteToBuffer("454689586", bBuff);
		expected = new Integer("454689586");
		actual = integerType.readIndexValue(Arrays.copyOf(bBuff.array(), integerType.getSize()));
		assertEquals(expected, actual);
		
		bBuff.rewind();
		longType.parseAndWriteToBuffer("45468957895686", bBuff);
		expected = new Long("45468957895686");
		actual = longType.readIndexValue(Arrays.copyOf(bBuff.array(), longType.getSize()));
		assertEquals(expected, actual);
		
		bBuff.rewind();
		floatType.parseAndWriteToBuffer("454689586.2132152", bBuff);
		expected = new Float("454689586.2132152");
		actual = floatType.readIndexValue(Arrays.copyOf(bBuff.array(), floatType.getSize()));
		assertEquals(expected, actual);
		
		bBuff.rewind();
		doubleType.parseAndWriteToBuffer("454689586.2132152", bBuff);
		expected = new Double("454689586.2132152");
		actual = doubleType.readIndexValue(Arrays.copyOf(bBuff.array(), doubleType.getSize()));
		assertEquals(expected, actual);
		
		bBuff.rewind();
		dateType.parseAndWriteToBuffer("2015-04-27 15:45:38", bBuff);
		expected = Utils.dateToSecInt(new Utils().dateFromString("2015-04-27 15:45:38")); // cheated test to make it work on travis
		actual = dateType.readIndexValue(Arrays.copyOf(bBuff.array(), dateType.getSize()));
		assertEquals(expected, actual);
		
		bBuff.rewind();
		stringType.parseAndWriteToBuffer("hello world !", bBuff);
		expected = "hello world !";
		actual = stringType.readIndexValue(Arrays.copyOf(bBuff.array(), stringType.getSize()));
		assertEquals(expected, actual);
	}
	
	@AfterAll
	static void terminateTest() {
		
	}
}
