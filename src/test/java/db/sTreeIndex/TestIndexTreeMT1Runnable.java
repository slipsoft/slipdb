package db.sTreeIndex;

import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class TestIndexTreeMT1Runnable implements Runnable {
	
	//public AtomicBoolean : juste un thread.join()
	
	@Override
	public void run() {
		try {
			
			int cId = TestIndexTreeMT1.globalTableId;
			TestIndexTreeMT1.loadNewTableFromDisk("table" + cId, "../SMALL_1_000_000_yellow_tripdata_2015-04_"+cId+".csv");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
