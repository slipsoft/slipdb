package system;

import java.util.ArrayList;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import com.dant.utils.Log;
import com.dant.utils.MemUsage;
import com.dant.utils.Timer;

public class MemoryTests {
	
	// Pour ne pas alourdir Tarvis
	private boolean ignoreThoseHeavyTests = true;
	
	@Test
	public void memArrayListTest() {
		if (ignoreThoseHeavyTests) return;
		
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
		if (ignoreThoseHeavyTests) return;
		
		MemUsage.printMemUsage();
		long memUsage = MemUsage.getMemUsage();
		Timer t = new Timer("Temps pris CREATE SimpleArray");
		// Test mémoire
		int arraySize = 10_000_000;
		int[] ar = new int[arraySize];
		
		for (int i = 0; i < arraySize; i++) {
			ar[i] = i;
		}
		t.log();
		//MemUsage.printMemUsage();
		//System.gc();
		//MemUsage.printMemUsage();

		long usedMemoryTotal = MemUsage.getMemUsage() - memUsage;
		String formattedMem = MemUsage.formatMemUsage(usedMemoryTotal);
		Log.info("Mémoire création : " + formattedMem);
		
		
		memUsage = MemUsage.getMemUsage();
		Timer t2 = new Timer("Temps pris GET SimpleArray");
		for (int i = 0; i < arraySize; i++) {
			int v = ar[i];
		}
		t2.log();
		usedMemoryTotal = MemUsage.getMemUsage() - memUsage;
		formattedMem = MemUsage.formatMemUsage(usedMemoryTotal);
		Log.info("Mémoire récupération : " + formattedMem);
		
	}
	
	
	@Test
	public void treeSetVsArrayList() {
		if (ignoreThoseHeavyTests) return;
		
		//MemUsage.printMemUsage();
		long memUsage = MemUsage.getMemUsage();
		Timer t = new Timer("Création TreeSet");
		TreeSet<Integer>  ts = new TreeSet<Integer>();
		
		// Test mémoire
		int arraySize = 10_000_000;
		//ArrayList<Integer> ar = new ArrayList<Integer>(arraySize);
		
		for (int i = 0; i < arraySize; i++) {
			ts.add(i);
		}
		t.log();
		//MemUsage.printMemUsage();
		//System.gc();
		//MemUsage.printMemUsage();

		long usedMemoryTotal = MemUsage.getMemUsage() - memUsage;
		String formattedMem = MemUsage.formatMemUsage(usedMemoryTotal);
		Log.info("Création TreeSet : " + formattedMem);
		
		
		memUsage = MemUsage.getMemUsage();
		Timer t2 = new Timer("GET TreeSet");
		for (Integer i : ts) {
			int v = i;
		}
		t2.log();
		usedMemoryTotal = MemUsage.getMemUsage() - memUsage;
		formattedMem = MemUsage.formatMemUsage(usedMemoryTotal);
		Log.info("Mémoire récupération : " + formattedMem);
		
		
	}
	
	
}
