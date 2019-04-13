package zArchive.sj.simpleDB.treeIndexing;

import java.io.IOException;

import db.structure.StructureException;

public class TestIndexTreeMT1Runnable implements Runnable {
	
	//public AtomicBoolean : juste un thread.join()
	
	@Override
	public void run() {
		try {
			
			int cId = TestIndexTreeMT1.globalTableId;
			//TestIndexTreeMT1.loadNewTableFromDisk("table" + cId, "../SMALL_1_000_000_yellow_tripdata_2015-04_"+cId+".csv");
			TestIndexTreeMT1.loadNewTableFromDisk("table" + cId, "testdata/SMALL_100_000_yellow_tripdata_2015-04.csv");
			// Toujours le même fichier parsé, ici (benchmark, pas fait pour charger de la vraie donnée différente)
			// Fait pour démontrer l'intérêt de faire du multi-thread plutôt que du mono-thread
		} catch (IOException | StructureException e) {
			e.printStackTrace();
		}
	}
}
