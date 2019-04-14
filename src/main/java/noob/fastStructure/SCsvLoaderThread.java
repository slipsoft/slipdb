package noob.fastStructure;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dant.utils.Log;

import db.data.load.IncorrectEntryException;
import db.data.load.Parser;
import db.data.types.DateType;
import db.disk.dataHandler.DiskDataPosition;
import db.disk.dataHandler.TableDataHandlerWriteJob;
import db.structure.Column;
import db.structure.Table;
import index.indexTree.IndexException;

public class SCsvLoaderThread extends Thread {
	
	private AtomicBoolean hasToBeExecuted = new AtomicBoolean(false); // vrai si doit être exécuté
	private AtomicBoolean readyForNewTask = new AtomicBoolean(true);
	private AtomicBoolean collectingData = new AtomicBoolean(false);
	private AtomicBoolean running = new AtomicBoolean(false);
	private final int linesBufferCount = 300_000; // mise en buffer de 300_000 lignes avant la création d'un nouveau thread
	
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
	
	public SCsvLoaderThread(Table argTable, Parser argParser, Object argLoaderWriteInMemoryLock) {

		localDateTypeThreadSafe = new DateType();
		localEntryBuffer = ByteBuffer.allocate(lineByteSize);
		localEntriesArray = new Object[currentTable.getColumns().size()];
		currentTable = argTable;
		lineByteSize = currentTable.getLineSize();
		loaderParser = argParser;
		fieldsNumberInLine = currentTable.getColumns().size();
		loaderWriteInMemoryLock = argLoaderWriteInMemoryLock;
		
	}
	
	// Non thread safe, toujours appelé depuis SCsvLoader
	public boolean tryUseThisThreadForNewDataCollection() {
		if (readyForNewTask.get() == false) return false;
		if (collectingData.get() == true) return false;
		collectingData.set(true);
		hasToBeExecuted.set(true);
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
		collectingData.set(false);
		running.set(true);
		this.start();
	}
	
	
	public void startIfNeeded() {
		if (hasToBeExecuted.get() == true) {
			setBusyRunningState();
		}
	}
	
	private void executionTerminated() {
		linesBufferPosition = 0;
		running.set(false);
		hasToBeExecuted.set(false);
		readyForNewTask.set(true);
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
		Column currentColumn;
		
		// -> Voir si c'est plus opti de parser et de mettre dans les colonnes
		
		
		for (int iLine = 0; iLine < linesBufferPosition; iLine++) {
			try {
				entryString = linesBuffer[iLine];
				valuesAsStringArray = entryString.split(","); //loaderParser.processEntry(entryString);
				if (valuesAsStringArray.length != fieldsNumberInLine)
					throw new IncorrectEntryException(0, "incorrect size"); // TODO mettre le bon numéro de l'entrée
				
				
				//localEntryBuffer.rewind();
				try {
					// for each column, parse and write data into entryBuffer
					for (int columnIndex = 0; columnIndex < fieldsNumberInLine; columnIndex++) {
						currentColumn = columnsList.get(columnIndex);
						boolean ignoreThisData = ((currentColumn.keepDataInMemory == false) && (currentColumn.writeDataOnDisk == false));
						
						if (ignoreThisData == false) {
							
							//Log.info("parseAndWriteEntry : valuesAsStringArray["+columnIndex+"] = " + valuesAsStringArray[columnIndex]);
							// Converts the string value into an array of bytes representing the same data
							if (currentColumn.getDataType().getClass() == DateType.class) {
								currentValue = localDateTypeThreadSafe.parseAndWriteToBuffer(valuesAsStringArray[columnIndex], localEntryBuffer);
							} else {
								currentValue = currentColumn.parseAndWriteToBuffer(valuesAsStringArray[columnIndex], localEntryBuffer);
							}
						} else {
							currentValue = currentColumn.getDataType().getDefaultValue();
						}
						
						// TEMPORAIREMENT désactivé (rush) currentColumn.evaluateMinMax(currentValue); // <- Indispensable pour le IndexTreeCeption (non utile pour le IndexTreeDic)
						localEntriesArray[columnIndex] = currentValue;
					}
				} catch (IllegalArgumentException e) {
					//e.printStackTrace();
					throw new IncorrectEntryException(0, "incorrect data"); // TODO mettre le bon numéro de l'entrée
				}
				
				
				// Ecriture de la donnée en mémoire, en un seul bloc atomique, pour garantir la cohérence de la donnée (pas un lock par colonne donc !)
				synchronized(loaderWriteInMemoryLock) {
					for (int columnIndex = 0; columnIndex < fieldsNumberInLine; columnIndex++) {
						currentColumn = columnsList.get(columnIndex);
						currentValue = localEntriesArray[columnIndex];
						
						// Si je dois garder la donnée en mémoire, je la stocke dans la colonne
						if (currentColumn.keepDataInMemory) {
							currentColumn.writeDataInMemory(currentValue); // <- C'est vraiment pas super opti de faire data -> cast en objet -> cast en data mais rush et c'est la "Structure" de Nico
						}
					}
				}
				
				
				
				
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
				
				
				localReadEntryNb++;
			} catch (IncorrectEntryException e) { if (enableErrorLog) Log.warning(e);
			}// catch (IOException e)             { if (enableErrorLog) Log.error(e); }
		}
		
	}
	
}
