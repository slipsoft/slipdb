package noob.fastStructure;


/**
 *  Un seul CSV parsé à la fois avec une instance de ce loader, mais le parsing est réalisé en multi-thread
 *  Loader capable de
 *
 */
public class SLoader {
	
	private final int threadCount = 4;
	private final int threadLineBufferCount = 300_000; // mise en buffer de 300_000 lignes avant la création d'un nouveau thread
	private SLoaderThread[] threadArray;// = new SLoaderThread[threadCount];
	
	public SLoader() {
		// Création des threads
		threadArray = new SLoaderThread[threadCount];
		for (int iThread = 0; iThread < threadCount; iThread++) {
			threadArray[iThread] = new SLoaderThread();
		}
	}
	
	
	
	
	
}
