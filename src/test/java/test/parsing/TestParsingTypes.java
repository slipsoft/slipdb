package test.parsing;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.dant.utils.Log;
import com.dant.utils.Timer;
import com.dant.utils.Utils;

import db.data.ByteType;
import db.data.DateType;
import db.data.DoubleType;
import db.data.FloatType;
import db.data.IntegerType;


public class TestParsingTypes {
	
	@BeforeAll
	public static void startTest() {
		//Log.info("setUpBeforeAll");
		Log.start("target/slipdb_TestParsingTypes.log", 3);
		
	}
	
	@Test
	public void testParsingFunctions() {
		
		int maxCountOperation = 5_000_000;
		String maxCountOperationStr = "5_000_000";
		Timer localTimer;
		ByteBuffer bBuff = ByteBuffer.allocate(20);
		
		ByteType   byteType = new ByteType();
		DoubleType doubleType = new DoubleType();
		DateType   dateType = new DateType();
		IntegerType integerType = new IntegerType();
		FloatType floatType = new FloatType();
		
		Log.info("Cout en millisecondes, pour " + maxCountOperationStr + " itérations :");
		
		localTimer = new Timer("Cout de fonctionnement de la boule pour " + maxCountOperationStr + " opérations ");
		for (int count = 0; count < maxCountOperation; count++) {
			bBuff.rewind();
		}
		localTimer.printms();
		
		localTimer = new Timer("Parsing de ByteType ");
		for (int count = 0; count < maxCountOperation; count++) {
			bBuff.rewind();
			byteType.parse("78", bBuff);
		}
		localTimer.printms();
		
		localTimer = new Timer("Parsing de IntegerType ");
		for (int count = 0; count < maxCountOperation; count++) {
			bBuff.rewind();
			integerType.parse("454689586", bBuff);
		}
		localTimer.printms();
		
		localTimer = new Timer("Parsing de FloatType ");
		for (int count = 0; count < maxCountOperation; count++) {
			bBuff.rewind();
			floatType.parse("454689586.2132152", bBuff);
		}
		localTimer.printms();
		
		localTimer = new Timer("Parsing de DoubleType ");
		for (int count = 0; count < maxCountOperation; count++) {
			bBuff.rewind();
			doubleType.parse("454689586.2132152", bBuff);
		}
		localTimer.printms();
		
		localTimer = new Timer("Parsing de DateType ");
		for (int count = 0; count < maxCountOperation; count++) {
			bBuff.rewind();
			dateType.parse("2015-04-27 15:45:38", bBuff);
		}
		localTimer.printms();
		
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
	
	@AfterAll
	public static void terminateTest() {
		
	}
}
