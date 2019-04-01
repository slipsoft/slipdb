package memory.tests;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.dant.utils.Log;
import com.dant.utils.MemUsage;
import com.dant.utils.Timer;

public class MemoryTests {
	@Test
	public void memArrayListTest() {
		
		MemUsage.printMemUsage();
		long memUsage = MemUsage.getMemUsage();
		Timer t = new Timer("Temps pris ArrayList");
		// Test mémoire
		int arraySize = 10_000_000;
		ArrayList<Integer> ar = new ArrayList<Integer>(arraySize);
		
		for (int i = 0; i < arraySize; i++) {
			ar.add(i);
		}
		t.log();
		MemUsage.printMemUsage();
		//System.gc();
		MemUsage.printMemUsage();

		long usedMemoryTotal = MemUsage.getMemUsage() - memUsage;
		String formattedMem = MemUsage.formatMemUsage(usedMemoryTotal);
		Log.info("Mémoire prise par l'opération : " + formattedMem);
		
	}

	@Test
	public void memSimpleArrayTest() {
		
		MemUsage.printMemUsage();
		long memUsage = MemUsage.getMemUsage();
		Timer t = new Timer("Temps pris SimleArray");
		// Test mémoire
		int arraySize = 10_000_000;
		int[] ar = new int[arraySize];
		
		for (int i = 0; i < arraySize; i++) {
			ar[i] = i;
		}
		t.log();
		MemUsage.printMemUsage();
		//System.gc();
		MemUsage.printMemUsage();

		long usedMemoryTotal = MemUsage.getMemUsage() - memUsage;
		String formattedMem = MemUsage.formatMemUsage(usedMemoryTotal);
		Log.info("Mémoire prise par l'opération : " + formattedMem);
		
	}
}
