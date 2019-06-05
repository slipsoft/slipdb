package db.data.load;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.dant.utils.Log;
import com.dant.utils.MemUsage;
import com.dant.utils.Timer;

import db.structure.Database;
import db.structure.Table;

/**
 *  Un seul CSV parsé à la fois avec une instance de ce loader, mais le parsing est réalisé en multi-thread
 *  Loader capable de
 *
 */
public class ParallelLoader {

	private final static int threadCount = Database.getInstance().config.loaderNbThread;
	private ParallelLoaderRunnable[] runnableArray;// = new SLoaderThread[threadCount];
	
	public static AtomicInteger totalParsedLines = new AtomicInteger(0); // Mis à jour des threads
	//public static final int updateTotalParsedLinesEvery = 1000; // à chaque fois que X lignes sont parsées, update de totalParsedLines.
	
	//private Table currentTable;
	//private int lineByteSize; // number of bytes used to store information
	//private int totalEntryCount = 0;
	
	static public AtomicInteger totalReadEntryNb = new AtomicInteger(0); // Mis à jour de cette classe
	static public int totalReadEntryNbNoThreadSafe = 0; // parsing non thread-safe
	static public long updateTotalReadEntryNbEach = 500_000;
	protected long addToTotalReadEntryCountBuffered = 0;
	protected int showInfoEveryParsedLines = 100_000; // mettre -1 pour désactiver l'affichage
	protected boolean enableErrorLog = false;
	
	public static AtomicLong debugNumberOfEntriesWritten = new AtomicLong(0);
	private Parser loaderParser;
	private Object loaderWriteInMemoryLock = new Object();
	
	public ParallelLoader(Table argTable, Parser argParser) {
		
		loaderParser = argParser;
		// Création des threads
		runnableArray = new ParallelLoaderRunnable[threadCount];
		for (int iThread = 0; iThread < threadCount; iThread++) {
			runnableArray[iThread] = new ParallelLoaderRunnable(argTable, loaderParser, loaderWriteInMemoryLock);
		}
		
		//currentTable = argTable;
		//lineByteSize = currentTable.getLineSize();
		
	}
	
	
	public final void parse(InputStream input, boolean appendAtTheEndOfSave) {
		parse(input, -1, appendAtTheEndOfSave, null, -1);
	}
	
	public final void parse(InputStream input, boolean appendAtTheEndOfSave, Timer limitParsingTimeTimer, int maxParsingTimeSec) {
		parse(input, -1, appendAtTheEndOfSave, limitParsingTimeTimer, maxParsingTimeSec);
	}
	
	/** Bloquant jusqu'à ce qu'un nouveau thread prêt soit trouvé
	 *  @return un thread prêt à être utilisé
	 */
	private ParallelLoaderRunnable getReadyThread() {
		ParallelLoaderRunnable foundReadyThread = null;
		do {
			for (int iThread = 0; iThread < threadCount; iThread++) {
				if (runnableArray[iThread].tryUseForNewDataCollection()) {
					foundReadyThread = runnableArray[iThread];
					//Log.error("trouvé - iThread=" + iThread);
					//Timer t = new Timer("GC temps pris");
					//System.gc();
					//t.log();
					//Log.error("Thread trouvé !!");
					break;
				}
			}
			if (foundReadyThread == null) {
				//Log.error("Attente d'un thread dispo...");
				try {
					Thread.sleep(5); // attente mi-active
				} catch (Exception e) {
					Log.error(e);
				}
			}
		} while (foundReadyThread == null);
		return foundReadyThread;
	}

	//private int debugCount = 0;
	
	/**
	 * Parse an input stream into an output stream according to a schema with a
	 * limit of lines (-1 : no limit)
	 * @param input
	 * @param limit
	 */
	public final void parse(InputStream input, int limit, boolean appendAtTheEndOfSave, Timer limitParsingTimeTimer, int maxParsingTimeSec) {
		if (maxParsingTimeSec != -1)
		if (limitParsingTimeTimer.getseconds() >= maxParsingTimeSec) {
			Log.warning("PARSING IMPOSSIBLE : TEMPS ECOULE " + limitParsingTimeTimer.pretty());
			return; // arrêt des lectures, le temps est dépassé, va directement au threads.join();
		}
		
		Log.info("PARSE : memusage init = ");
		//System.gc(); pour ne pas ralentir inutilement
		MemUsage.printMemUsage();
		int localReadEntryNb = 0;
		int localReadEntryNbToAddToTotalCount = 0;
		//int choosenThreadIndex = 0;
		boolean needNewThread = false;
		String entryAsString;
		
		
		
		ParallelLoaderRunnable currentThreadCollectingData = getReadyThread();
		Timer timeTookTimer = new Timer("Temps écoulé");
		
		try (
				BufferedReader bRead = new BufferedReader(new InputStreamReader(input));
				) {
			
			//while ((entryString = processReader(bRead)) != null && totalEntryCount != limit) {
			while (localReadEntryNb != limit) {
				
				// Lecture d'une nouvelle ligne / entrée
				entryAsString = bRead.readLine(); // "entrée", ligne lue
				if (entryAsString == null) break; // fin de la lecture
				/* Pour juste compter rapidement les lignes :
				debugCount++;
				if (debugCount % 1_00_000 == 0) {
					Log.error("debugCount = " + debugCount);
				}
				if (true) continue;*/
				//Log.infoOnly(entryAsString);
				
				localReadEntryNb++;
				localReadEntryNbToAddToTotalCount++;
				totalReadEntryNbNoThreadSafe++;
				// Affichage d'une entrée toutes les showInfoEveryParsedLines entrées lues
				if (showInfoEveryParsedLines != -1 && (totalReadEntryNbNoThreadSafe % showInfoEveryParsedLines == 0)) { // localReadEntryNb % showInfoEveryParsedLines == 0
					// localReadEntryNb
					Log.info("Loader : nombre de résultats (total) lus = " + totalReadEntryNbNoThreadSafe + "   temps écoulé = " + timeTookTimer.pretty() + "activeThreadNb = " + ParallelLoaderRunnable.activeThreadNb.get());
					
					totalReadEntryNb.addAndGet(localReadEntryNbToAddToTotalCount);
					localReadEntryNbToAddToTotalCount = 0;
					
					if (maxParsingTimeSec != -1)
					if (limitParsingTimeTimer.getseconds() >= maxParsingTimeSec) {
						Log.error("PARSING STOPPE - TEMPS ECOULE : " + limitParsingTimeTimer.pretty());
						break; // arrêt des lectures, le temps est dépassé, va directement au threads.join();
					}
					
					//MemUsage.printMemUsage();
					/*if (localReadEntryNb == 10_000_000) {
						Log.error("10_000_000 résultats - GC");
						MemUsage.printMemUsage();
						System.gc();
						MemUsage.printMemUsage();
					}*/
					/// Un débug -> En fait, c'était pas unbug mais le disque qui était limitant ^^'
					if (ParallelLoaderRunnable.activeThreadNb.get() <= 1 /*&& localReadEntryNb > 1_000_000*/) {
						//debugShowRunnablesState();
					}
					
				}
				//if (true) continue;
				
				needNewThread = currentThreadCollectingData.addNewLine(entryAsString);
				
				if (needNewThread) {
					//Log.error("needNewThread...");
					currentThreadCollectingData = getReadyThread();
					//MemUsage.printMemUsage();
					//System.gc();
				}
				
				if (localReadEntryNbToAddToTotalCount >= updateTotalReadEntryNbEach) {
					
					
					
				}
				
				
			}
			totalParsedLines.addAndGet(localReadEntryNbToAddToTotalCount);
			totalReadEntryNb.addAndGet(localReadEntryNbToAddToTotalCount);
			localReadEntryNbToAddToTotalCount = 0; // <- ne sera plus utilisé
			
			debugNumberOfEntriesWritten.addAndGet(localReadEntryNb);
		}/* catch (FileNotFoundException e) {
			Log.error(e);
		}*/ catch (IOException e) {
			Log.error(e);
		}
		//Log.error("debugCount = " + debugCount);
		currentThreadCollectingData.startIfNeeded(); // exécuter le dernier thread si besoin
		
		
		for (int iThread = 0; iThread < threadCount; iThread++) {
			runnableArray[iThread].waitTerminaison();
		}
		
		/*List<Column> columnsList = currentTable.getColumns();
		for (int iColumn = 0; iColumn < columnsList.size(); iColumn++) {
			Column col = columnsList.get(iColumn);
			col.clearAllMemoryData();
		}*/
		
		Log.info("Parsing terminé !! temps écoulé = " + timeTookTimer.pretty());
		Log.info("PARSE : Nombre de lignes (local) = " + localReadEntryNb);
		/*Log.info("PARSE : FINAL USAGE");
		System.gc();
		MemUsage.printMemUsage();*/
		
	}
	
	
	private void debugShowRunnablesState() {
		for (int iThread = 0; iThread < threadCount; iThread++) {
			boolean readyForNewTask = runnableArray[iThread].readyForNewTask.get();
			boolean running = runnableArray[iThread].running.get();
			Log.info("Thread " + iThread + " ready=" + readyForNewTask + " running=" + running);
			
		}
		
	}
	
	
}
