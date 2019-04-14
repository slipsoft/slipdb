package noob.fastStructure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicLong;

import com.dant.utils.Log;
import com.dant.utils.MemUsage;
import com.dant.utils.Timer;

import db.data.load.Parser;
import db.structure.Table;

/**
 *  Un seul CSV parsé à la fois avec une instance de ce loader, mais le parsing est réalisé en multi-thread
 *  Loader capable de
 *
 */
public class SCsvLoader {
	
	private final int threadCount = 4 + 1;
	private SCsvLoaderThread[] threadArray;// = new SLoaderThread[threadCount];
	
	private Table currentTable;
	//private int lineByteSize; // number of bytes used to store information
	private int totalEntryCount = 0;
	
	static public AtomicLong totalReadEntryNb = new AtomicLong(0);
	static public long updateTotalReadEntryNbEach = 100_000;
	protected long addToTotalReadEntryCountBuffered = 0;
	protected int showInfoEveryParsedLines = 100_000; // mettre -1 pour désactiver l'affichage
	protected boolean enableErrorLog = false;
	
	public static AtomicLong debugNumberOfEntriesWritten = new AtomicLong(0);
	private Parser loaderParser;
	private Object loaderWriteInMemoryLock = new Object();
	
	
	
	
	public SCsvLoader(Table argTable, Parser argParser) {
		loaderParser = argParser;
		// Création des threads
		threadArray = new SCsvLoaderThread[threadCount];
		for (int iThread = 0; iThread < threadCount; iThread++) {
			threadArray[iThread] = new SCsvLoaderThread(argTable, loaderParser, loaderWriteInMemoryLock);
		}
		
		currentTable = argTable;
		//lineByteSize = currentTable.getLineSize();
		
	}
	
	
	public final void parse(InputStream input, boolean appendAtTheEndOfSave) {
		parse(input, -1, appendAtTheEndOfSave);
	}
	
	/** Bloquant jusqu'à ce qu'un nouveau thread prêt soit trouvé
	 *  @return un thread prêt à être utilisé
	 */
	private SCsvLoaderThread getReadyThread() {
		SCsvLoaderThread foundReadyThread = null;
		do {
			for (int iThread = 0; iThread < threadCount; iThread++) {
				if (threadArray[iThread].tryUseThisThreadForNewDataCollection()) {
					foundReadyThread = threadArray[iThread];
					break;
				}
			}
			if (foundReadyThread == null) {
				try {
					Thread.sleep(1); // attente mi-active
				} catch (Exception e) {
					Log.error(e);
				}
			}
		} while (foundReadyThread == null);
		return foundReadyThread;
	}
	
	/**
	 * Parse an input stream into an output stream according to a schema with a
	 * limit of lines (-1 : no limit)
	 * @param input
	 * @param limit
	 */
	public final void parse(InputStream input, int limit, boolean appendAtTheEndOfSave) {
		//Log.info("PARSE : memusage init = ");
		//System.gc();
		//MemUsage.printMemUsage();
		int localReadEntryNb = 0;
		int localReadEntryNbToAddToTotalCount = 0;
		//int choosenThreadIndex = 0;
		boolean needNewThread = false;
		String entryAsString;
		
		SCsvLoaderThread currentThreadCollectingData = getReadyThread();
		
		try ( 
				BufferedReader bRead = new BufferedReader(new InputStreamReader(input));
				) {
			
			Timer timeTookTimer = new Timer("Temps écoulé");
			
			
			
			//while ((entryString = processReader(bRead)) != null && totalEntryCount != limit) {
			while (localReadEntryNb != limit) {
				
				
				// Lecture d'une nouvelle ligne / entrée
				entryAsString = bRead.readLine(); // "entrée", ligne lue
				if (entryAsString == null) break; // fin de la lecture
				
				needNewThread = currentThreadCollectingData.addNewLine(entryAsString);
				
				if (needNewThread) {
					currentThreadCollectingData = getReadyThread();
				}
				
				// Affichage d'une entrée toutes les showInfoEveryParsedLines entrées lues
				if (showInfoEveryParsedLines != -1 && localReadEntryNb % showInfoEveryParsedLines == 0) {
					Log.info("Loader : nombre de résultats (local) lus = " + localReadEntryNb + "   temps écoulé = " + timeTookTimer.pretty());
					MemUsage.printMemUsage();
				}
				
				if (localReadEntryNbToAddToTotalCount >= updateTotalReadEntryNbEach) {
					totalReadEntryNb.addAndGet(localReadEntryNbToAddToTotalCount);
					localReadEntryNbToAddToTotalCount = 0;
				}
				
				
			}
			debugNumberOfEntriesWritten.addAndGet(localReadEntryNb);
		}/* catch (FileNotFoundException e) {
			Log.error(e);
		}*/ catch (IOException e) {
			Log.error(e);
		}
		
		currentThreadCollectingData.startIfNeeded(); // exécuter le dernier thread si besoin

		Log.info("PARSE : FINAL USAGE");
		System.gc();
		MemUsage.printMemUsage();
		

		for (int iThread = 0; iThread < threadCount; iThread++) {
			try {
				threadArray[iThread].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Log.info("Parsing terminé !!");
		
	}
	
	
	
}
