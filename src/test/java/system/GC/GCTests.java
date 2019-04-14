package system.GC;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.dant.utils.Log;
import com.dant.utils.MemUsage;
import com.dant.utils.Timer;

class FinalSave {
	
	public final ArrayList<Integer> myFinalList;
	
	public FinalSave(ArrayList<Integer> argMyFinalList) {
		myFinalList = argMyFinalList;
	}
	
	public int getInt() {
		return 42;
	}
	
}

public class GCTests {
	
	// Pour ne pas alourdir Tarvis
	private boolean ignoreThoseHeavyTests = false;
	
	
	
	/*private final static long INT_SIZE_IN_BYTES = 4;

	  private final long startIndex;

	  public void init(long size) {
	    startIndex = unsafe.allocateMemory(size * INT_SIZE_IN_BYTES);
	    unsafe.setMemory(startIndex, size * INT_SIZE_IN_BYTES, (byte) 0);
	    }
	  }

	  public void setValue(long index, int value) {
	    unsafe.putInt(index(index), value);
	  }

	  public int getValue(long index) {
	    return unsafe.getInt(index(index));
	  }

	  private long index(long offset) {
	    return startIndex + offset * INT_SIZE_IN_BYTES;
	  }

	  public void destroy() {
	    unsafe.freeMemory(startIndex);
	  }
	}*/
	
	
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
		FinalSave finalSave = new FinalSave(ar);
		ar = null;
		
		
		MemUsage.printMemUsage();
		Timer gcTimer = new Timer("Temps pris par le GC");
		System.gc();
		MemUsage.printMemUsage();
		gcTimer.log();

		long usedMemoryTotal = MemUsage.getMemUsage() - memUsage;
		String formattedMem = MemUsage.formatMemUsage(usedMemoryTotal);
		Log.info("Mémoire prise par l'opération : " + formattedMem + "   int=" + finalSave.getInt());
		
		finalSave = null;
	}
	
	//@Test
	public void memSimpleArrayTest() {
		if (ignoreThoseHeavyTests) return;
		
		MemUsage.printMemUsage();
		long memUsage = MemUsage.getMemUsage();
		Timer t = new Timer("Temps pris CREATE SimpleArray");
		// Test mémoire
		int arraySize = 100_000_000;
		int[] ar = new int[arraySize];
		
		for (int i = 0; i < arraySize; i++) {
			ar[i] = i;
		}
		t.log();
		
		MemUsage.printMemUsage();
		Timer gcTimer = new Timer("Temps pris par le GC");
		System.gc();
		MemUsage.printMemUsage();
		gcTimer.log();

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
	
	
	
}
