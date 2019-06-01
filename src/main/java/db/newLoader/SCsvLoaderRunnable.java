package db.newLoader;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.dant.utils.Log;
import com.dant.utils.MemUsage;
import com.dant.utils.Utils;

import db.data.load.IncorrectEntryException;
import db.data.load.Parser;
import db.data.types.ByteType;
import db.data.types.DataType;
import db.data.types.DateType;
import db.data.types.StringType;
import db.structure.Column;
import db.structure.Table;

public class SCsvLoaderRunnable implements Runnable {
	
	private boolean hasToBeExecuted = false; // vrai si doit être exécuté
	public AtomicBoolean readyForNewTask = new AtomicBoolean(true);
	//private AtomicBoolean collectingData = new AtomicBoolean(false);
	public AtomicBoolean running = new AtomicBoolean(false);
	private final int linesBufferCount = 200_000; // mise en buffer de 200_000 lignes avant la mise en route d'un thread de parsing
	
	private String[] linesBuffer = new String[linesBufferCount];
	private int linesBufferPosition = 0;
	private Table currentTable;
	private int lineByteSize;
	
	private DateType localDateTypeThreadSafe;
	private ByteBuffer localEntryBuffer;
	private Object[] localEntriesArray;
	private final Parser loaderParser;
	private int fieldsNumberInLine;
	private Object loaderWriteInMemoryLock;
	private Object criticalLock = new Object();
	private int parsedLinesCount = 0;
	
	private Thread runningWithThread = null;
	
	public static AtomicInteger activeThreadNb = new AtomicInteger(0);
	
	/*
	public Thread getRunningThread() {
		return runningWithThread;
	}*/
	public void waitTerminaison() {
		if (runningWithThread != null) try {
			runningWithThread.join();
		} catch (InterruptedException e) { e.printStackTrace(); }
	}
	
	public SCsvLoaderRunnable(Table argTable, Parser argParser, Object argLoaderWriteInMemoryLock) {

		localDateTypeThreadSafe = new DateType();
		currentTable = argTable;
		lineByteSize = currentTable.getLineSize();
		loaderParser = argParser;
		fieldsNumberInLine = currentTable.getColumns().size();
		loaderWriteInMemoryLock = argLoaderWriteInMemoryLock;
		localEntriesArray = new Object[fieldsNumberInLine];
		localEntryBuffer = ByteBuffer.allocate(lineByteSize);
		
	}
	
	// Non thread safe, toujours appelé depuis SCsvLoader
	public boolean tryUseForNewDataCollection() { //synchronized (criticalLock) {
		//Log.info("tryUseThisThreadForNewDataCollection - readyForNewTask = " + readyForNewTask.get());
		if (readyForNewTask.getAndSet(false) == false) return false;
		hasToBeExecuted = true;
		return true;
	}
	
	
	/** 
	 *  @param argLine
	 *  @return true si le thread est saturé
	 */
	public boolean addNewLine(String argLine) {
		linesBuffer[linesBufferPosition] = argLine;
		linesBufferPosition++;
		if (linesBufferPosition == linesBufferCount) {
			setBusyRunningState();
			return true;
		}
		return false;
	}
	
	private void setBusyRunningState() {
		readyForNewTask.set(false);
		running.set(true);
		runningWithThread = new Thread(this);
		runningWithThread.start();
		activeThreadNb.addAndGet(1);
	}
	
	
	public void startIfNeeded() {
		if (hasToBeExecuted) {
			setBusyRunningState();
		}
	}
	
	private void executionTerminated() {//  synchronized (criticalLock) {
		linesBufferPosition = 0;
		running.set(false);
		hasToBeExecuted = false;
		readyForNewTask.set(true);
		activeThreadNb.addAndGet(-1);
		//Log.info("RUNNABLE executionTerminated - activeThreadNb = " + activeThreadNb.get());
	}
	
	@Override
	public void run() {
		processData();
		executionTerminated();
	}
	
	private int localReadEntryNb = 0;
	private boolean enableErrorLog = true;

	/** Parser et intégrer les données dans les colonnes
	 */
	public void processData() {
		
		List<Column> columnsList = currentTable.getColumns();
		String entryString;
		String[] valuesAsStringArray;
		Object currentValue = null;
		//Column currentColumn;
		int entCount = 0;
		ByteBuffer lineAsByteBuffer = ByteBuffer.allocateDirect(lineByteSize);
		Utils utilsInstance = new Utils();
		
		Column[] localColumnArray = new Column[columnsList.size()];
		boolean[] ignoreThisDataArray = new boolean[columnsList.size()];
		for (int iColumn = 0; iColumn < columnsList.size(); iColumn++) {
			Column col = columnsList.get(iColumn);
			localColumnArray[iColumn] = col;
			ignoreThisDataArray[iColumn] = ((col.keepDataInMemory == false) && (col.writeDataOnDisk == false));
		}
		
		// -> Voir si c'est plus opti de parser et de mettre dans les colonnes
		
		boolean parseWithNativeFormat = true;
		//boolean stupidBenchmark = true;
		//byte[] strAsByteArray;
		int dateAsInt;
		
		
		
		
		for (int iLine = 0; iLine < linesBufferPosition; iLine++) {
			entryString = linesBuffer[iLine];
			lineAsByteBuffer.rewind();

			//boolean skipAllData = true;
			//if (skipAllData) continue;
			
			valuesAsStringArray = entryString.split(","); //loaderParser.processEntry(entryString);
			
			//if (true == true) continue;
			
			if (valuesAsStringArray.length != fieldsNumberInLine) {
				Log.warning(new IncorrectEntryException(0, "incorrect size")); // TODO mettre le bon numéro de l'entrée
				continue;
			}
			
			
			
			localEntryBuffer.rewind();
			
			//localEntryBuffer.rewind();
			try {
				// for each column, parse and write data into entryBuffer
				for (int columnIndex = 0; columnIndex < fieldsNumberInLine; columnIndex++) {
					Column currentColumn = localColumnArray[columnIndex];
					//boolean totallyIgnoreThisData = true;
					
					// Benchmark du parsing pas fait par Nicolas
					if (parseWithNativeFormat) {
						String valueAsString = valuesAsStringArray[columnIndex];
						
						/*if (stupidBenchmark) {
							switch (currentColumn.dataTypeEnum) {
							case UNKNOWN :
								Log.error("Colonne au type inconnu");
								break;
							case BYTE :
								Byte.parseByte(valueAsString);
								break;
							case INTEGER :
								Integer.parseInt(valueAsString);
								break;
							case LONG :
								Long.parseLong(valueAsString);
								break;
							case DATE :
								//int dateAsInt = utilsInstance.intDateFromString(valueAsString);
								break;
							case FLOAT :
								Float.parseFloat(valueAsString);
								break;
							case DOUBLE :
								Double.parseDouble(valueAsString);
								break;
							case STRING :
								byte[] strAsByteArray = StringType.stringToAjustedByteArray(valueAsString, currentColumn.getDataSize());
								break;
							default : break;
							}
							continue;
						}*/
						
						//if (skipAllData) continue;
						switch (currentColumn.dataTypeEnum) {
						case UNKNOWN :
							Log.error("Colonne au type inconnu");
							break;
						case BYTE :
							lineAsByteBuffer.put(Byte.parseByte(valueAsString));
							break;
						case INTEGER :
							lineAsByteBuffer.putInt(Integer.parseInt(valueAsString));
							break;
						case LONG :
							lineAsByteBuffer.putLong(Long.parseLong(valueAsString));
							break;
						case DATE :
							dateAsInt = utilsInstance.intDateFromString(valueAsString);
							lineAsByteBuffer.putInt(dateAsInt);
							break;
						case FLOAT :
							lineAsByteBuffer.putFloat(Float.parseFloat(valueAsString));
							break;
						case DOUBLE :
							lineAsByteBuffer.putDouble(Double.parseDouble(valueAsString));
							break;
						case STRING :
							byte[] strAsByteArray = StringType.stringToAjustedByteArray(valueAsString, currentColumn.getDataSize());
							lineAsByteBuffer.put(strAsByteArray);
							break;
						default : break;
						}
						
						/*Class<? extends DataType> objectClass = currentColumn.getDataType().getClass();
						
						try {
							if (objectClass == DateType.class) {
								// Date
								int dateAsInt = utilsInstance.intDateFromString(valueAsString);
								lineAsByteBuffer.putInt(dateAsInt);
							} else if (objectClass == ByteType.class) {
								// Byte
								lineAsByteBuffer.put(Byte.parseByte(valueAsString));
							} else if (objectClass == IntegerType.class) {
								// Byte
								lineAsByteBuffer.put(Byte.parseByte(valueAsString));
							}
						} catch (Exception e) {
							Log.warning(e);
						}*/
					} else {
						
						
						if (ignoreThisDataArray[columnIndex] == false) {
							
							//Log.info("parseAndWriteEntry : valuesAsStringArray["+columnIndex+"] = " + valuesAsStringArray[columnIndex]);
							// Converts the string value into an array of bytes representing the same data
							if (currentColumn.getDataType().getClass() == DateType.class) {
								currentValue = localDateTypeThreadSafe.parseAndWriteToBuffer(valuesAsStringArray[columnIndex], localEntryBuffer);
							} else {
								currentValue = currentColumn.parseAndWriteToBuffer(valuesAsStringArray[columnIndex], localEntryBuffer);
							}
						} else {
							currentValue = currentColumn.getDataType().getDefaultValue();
							//Log.error("IGNORE DATA !");
						}
						
						// TEMPORAIREMENT désactivé (rush) currentColumn.evaluateMinMax(currentValue); // <- Indispensable pour le IndexTreeCeption (non utile pour le IndexTreeDic)
						localEntriesArray[columnIndex] = currentValue;
					}
					
					
				}
			} catch (IllegalArgumentException e) {
				//e.printStackTrace();
				Log.warning(new IncorrectEntryException(0, "incorrect data")); // TODO mettre le bon numéro de l'entrée
				continue;
			}
			
			
			
			int newLineIndex = -1;
			
			// Ecriture de la donnée en mémoire, en un seul bloc atomique, pour garantir la cohérence de la donnée (pas un lock par colonne donc !)
			//if (false)
			synchronized(loaderWriteInMemoryLock) {
				
				currentTable.addLineFlag();
				
				lineAsByteBuffer.rewind();
				for (int columnIndex = 0; columnIndex < fieldsNumberInLine; columnIndex++) {
					Column currentColumn = localColumnArray[columnIndex];
					if (currentColumn.keepDataInMemory == false) continue; // Si je dois garder la donnée en mémoire, je la stocke dans la colonne
					
					if (newLineIndex == -1) newLineIndex = currentColumn.getTotalLinesNumber();
					
					/*if (currentColumn == currentTable.debugTheStringColumn) {
						Log.info("Write once.");
					}*/
					
					if (parseWithNativeFormat) {
						
						switch (currentColumn.dataTypeEnum) {
						case UNKNOWN :
							Log.error("Colonne au type inconnu");
							break;
						case BYTE :
							currentColumn.writeByteInMemory(lineAsByteBuffer.get());
							break;
						case INTEGER :
							currentColumn.writeIntegerInMemory(lineAsByteBuffer.getInt());
							break;
						case LONG :
							currentColumn.writeLongInMemory(lineAsByteBuffer.getLong());
							break;
						case DATE :
							currentColumn.writeDateInMemory(lineAsByteBuffer.getInt());
							break;
						case FLOAT :
							currentColumn.writeFloatInMemory(lineAsByteBuffer.getFloat());
							break;
						case DOUBLE :
							currentColumn.writeDoubleInMemory(lineAsByteBuffer.getDouble());
							break;
						case STRING :
							//Log.info("DEBUT ");
							//if (currentColumn == currentTable.debugTheStringColumn)
							//Log.info(currentColumn.getChunkPositionsVariables());
							//Log.info("" + currentTable.debugTheStringColumn);
							byte[] strAsByteArray = new byte[currentColumn.getDataSize()];
							lineAsByteBuffer.get(strAsByteArray);
							currentColumn.writeStringInMemory(new String(strAsByteArray));
							//Log.info(currentColumn.getChunkPositionsVariables());
							//Log.info("FIN ");
							break;
						default : break;
						}
						
						
					} else {
						currentValue = localEntriesArray[columnIndex];
						currentColumn.writeDataInMemory(currentValue); // <- C'est vraiment pas super opti de faire data -> cast en objet -> cast en data mais rush et c'est la "Structure" de Nico
					}
				}
			}
			if (newLineIndex == -1) {
				Log.error("X------------X *dead*");
			}
			/*try {
				String lineAsStr = currentTable.getLineAsReadableString(newLineIndex);
			} catch (Exception e) {
				Log.error("ERREUR à l'index " + newLineIndex);
				e.printStackTrace();
				throw e;
				
			}*/
			//Log.info(lineAsStr);
			
			localReadEntryNb++;
			/*if (localReadEntryNb %  SCsvLoader.updateTotalParsedLinesEvery == 0 && localReadEntryNb != 0) {
				
			}*/
			
			/*
			if (doRuntimeIndexing) {
				try {
					currentTable.indexEntry(localEntriesArray, dataPosition);
				} catch (IndexException e) {
					Log.error(e);
				}
			}
			
			DiskDataPosition dataPosition = writeJob.writeDataLine(localEntryBuffer.array());
			*/
			// Indexer au moment de parser (pour de meilleures performances)
			/*if (runtimeIndexingEntries != null) {
				for (int columnIndex = 0; columnIndex < columnsList.size(); columnIndex++) {
					RuntimeIndexingEntry indexingEntry = runtimeIndexingEntries.getEntryAssociatedWithColumnIndex(columnIndex);
					if (indexingEntry != null) {
						Object currentValue = localEntriesArray[columnIndex];
						// Indexer cette entrée
						indexingEntry.addIndexValue(currentValue, dataPosition);
						//Log.info("Indexer valeur = " + currentValue);
					}
					//Log.info("Indexer2 valeur = " + currentValue);
			*/
			
			//}// catch (IOException e)             { if (enableErrorLog) Log.error(e); }
		}
		
		SCsvLoader.totalParsedLines.addAndGet(localReadEntryNb);
		
		//bBuff.clear();
		//System.gc();
	}
	
	
	
	
}
