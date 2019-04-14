package noob.fastStructure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.IntBuffer;
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
	
	private final int threadCount = 6;//+8;
	private SCsvLoaderRunnable[] runnableArray;// = new SLoaderThread[threadCount];
	
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
	
	IntBuffer buff;
	
	
	public SCsvLoader(Table argTable, Parser argParser) {
		
		loaderParser = argParser;
		// Création des threads
		runnableArray = new SCsvLoaderRunnable[threadCount];
		for (int iThread = 0; iThread < threadCount; iThread++) {
			runnableArray[iThread] = new SCsvLoaderRunnable(argTable, loaderParser, loaderWriteInMemoryLock);
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
	private SCsvLoaderRunnable getReadyThread() {
		SCsvLoaderRunnable foundReadyThread = null;
		do {
			for (int iThread = 0; iThread < threadCount; iThread++) {
				if (runnableArray[iThread].tryUseForNewDataCollection()) {
					foundReadyThread = runnableArray[iThread];
					Timer t = new Timer("GC temps pris");
					System.gc();
					t.log();
					break;
				}
			}
			if (foundReadyThread == null) {
				//Log.info("Attente d'un thread dispo...");
				try {
					Thread.sleep(20); // attente mi-active
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
		
		SCsvLoaderRunnable currentThreadCollectingData = getReadyThread();
		Timer timeTookTimer = new Timer("Temps écoulé");
		
		try ( 
				BufferedReader bRead = new BufferedReader(new InputStreamReader(input));
				) {
			
			
			
			
			//while ((entryString = processReader(bRead)) != null && totalEntryCount != limit) {
			while (localReadEntryNb != limit) {
				
				
				// Lecture d'une nouvelle ligne / entrée
				entryAsString = bRead.readLine(); // "entrée", ligne lue
				
				if (entryAsString == null) break; // fin de la lecture
				
				needNewThread = currentThreadCollectingData.addNewLine(entryAsString);
				
				if (needNewThread) {
					currentThreadCollectingData = getReadyThread();
				}
				
				localReadEntryNb++;
				
				// Affichage d'une entrée toutes les showInfoEveryParsedLines entrées lues
				if (showInfoEveryParsedLines != -1 && localReadEntryNb % showInfoEveryParsedLines == 0) {
					Log.info("Loader : nombre de résultats (local) lus = " + localReadEntryNb + "   temps écoulé = " + timeTookTimer.pretty() + "activeThreadNb = " + SCsvLoaderRunnable.activeThreadNb.get());
					//MemUsage.printMemUsage();
					if (SCsvLoaderRunnable.activeThreadNb.get() < 3) {
						debugShowRunnablesState();
						
						
					}
					
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
		
		//Log.info("PARSE : FINAL USAGE");
		//System.gc();
		//MemUsage.printMemUsage();
		
		for (int iThread = 0; iThread < threadCount; iThread++) {
			runnableArray[iThread].waitTerminaison();
		}
		Log.info("Parsing terminé !! temps écoulé = " + timeTookTimer.pretty());
		Log.info("PARSE : Nombre de lignes = " + localReadEntryNb);
		
	}
	
	
	private void debugShowRunnablesState() {
		for (int iThread = 0; iThread < threadCount; iThread++) {
			boolean readyForNewTask = runnableArray[iThread].readyForNewTask.get();
			boolean running = runnableArray[iThread].running.get();
			Log.info("Thread " + iThread + " ready=" + readyForNewTask + " running=" + running);
			
		}
		
	}
	
	
}
