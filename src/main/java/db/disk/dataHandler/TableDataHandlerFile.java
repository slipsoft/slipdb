package db.disk.dataHandler;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dant.utils.BufferedDataInputStreamCustom;
import com.dant.utils.EasyFile;
import com.dant.utils.Log;

import db.structure.Column;
import db.structure.Table;

public class TableDataHandlerFile {
	
	// Champs mis en public pour gagner du temps
	// Thread-safe (c'est d'ailleurs le but !)
	
	//private Object forceWaitToUseThisFileLock = new Object();

	// Utilisé de l'extérieur (thread différent)
		private AtomicBoolean currentlyInUse = new AtomicBoolean(true); // si le fichier est actuellement utilisé (lecture ou écriture)
		private AtomicBoolean fileIsFull = new AtomicBoolean(false); // impossible de rajouter de la donnée si fileIsFull
	
	
	// Utilisé de l'intérieur (même thread)
	private EasyFile fileOnDisk;
	private String filePath;
	private int currentFileSize = 0;
	private int currentFileEntriesNumber = 0;
	private final short fileID;
	
	// Nonthread-safe, seulement utilisé par quelques fonctions spécifiques, et dans des cas bien précis
	private DataOutputStream streamWriter = null;
	// RandomAccessFile pour la lecture   OU :
	private BufferedDataInputStreamCustom streamReader = null;
	private boolean isReadOnlyMode;
	private int positionInReadOnlyFile = 0;
	
	static public final int maxFileSizeOnDisk = 150_000_000; // 500_000_000 Max 500 mo sur disque (grosse marge de sécurité)
	
	public TableDataHandlerFile(short argFileID, String argFilePath) throws IOException {
		fileID = argFileID;
		filePath = argFilePath;
		fileOnDisk = new EasyFile(filePath);
		fileOnDisk.createFileIfNotExist();
		currentlyInUse.set(false);
	}
	
	/** Thread-safe, car lecture seule (et la ressource ne change pas de position mémoire)
	 *  @return
	 */
	public short getFileID() {
		return fileID;
	}
	
	/**
	 * 
	 * @param readOnly  true pour readOnly, false pour writeOnly
	 * @return
	 * @throws IOException
	 */
	public boolean tryToUseThisFile(boolean readOnly, boolean appendAtTheEndIfWrite) throws IOException {
		if ((readOnly == false) && (fileIsFull.get())) return false; // accès en écriture et fichier plein
		if (currentlyInUse.getAndSet(true)) return false; // je viens de passer de true -> true, déjà en cours d'utilisation
		
		
		// si je viens de passer de false -> true, c'est bon !
		// Ouverture du bon stream
		isReadOnlyMode = readOnly;
		
		if (readOnly) {
			streamReader = new BufferedDataInputStreamCustom(new FileInputStream(fileOnDisk));//new DataInputStream(new BufferedInputStream(
			streamReader.mark((int) maxFileSizeOnDisk + 100); // pour revenir à la position initiale du stream (pas sur que ça marche bien, ça...)
			positionInReadOnlyFile = 0;
		} else {
			streamWriter = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileOnDisk, appendAtTheEndIfWrite)));
		}
		
		return true;
	}
	
	
	/*public boolean waitToUseThisFile(boolean readOnly) throws IOException { synchronized(forceWaitToUseThisFileLock) {
		
		
		
	} }*/
	
	
	public void stopFileUse() throws IOException {
		if (currentlyInUse.get() == false) { //throw new Exception("stopFileUse : le fichier n'était pas utilisé.");
			Log.error("TableDataHandlerFile.stopFileUse : currentlyInUse.get() = false alors que devrait être à true.");
		}
		
		if (currentFileSize >= maxFileSizeOnDisk)
			fileIsFull.set(true);
		else
			fileIsFull.set(false);
		
		// + fermetrure des streams si besoin
		if (streamReader != null) {
			streamReader.close();
			streamReader = null;
		}
		
		if (streamWriter != null) {
			streamWriter.close();
			streamWriter = null;
		}
		currentlyInUse.set(false); // plus utilisé
	}
	
	
	/**
	 * 
	 * @param inUse
	 * @param readOnly
	 * @throws IOException 
	 */
	//public void setInUse(boolean inUse, boolean readOnly) throws IOException {
	//}
	
	public boolean getInUse() {
		return currentlyInUse.get();
	}
	
	public boolean getIsFull() { // depuis l'extérieur
		return fileIsFull.get();
	}
	
	/** NON THREAD-SAFE
	 *  DOIT être bien utilisé.
	 *  
	 *  @param dataAsByteArray
	 *  @return
	 * @throws IOException 
	 */
	public TableDataPositionResult writeDataLine(byte[] dataAsByteArray) throws IOException {
		currentFileEntriesNumber++;
		currentFileSize += dataAsByteArray.length;
		streamWriter.write(dataAsByteArray);
		boolean canStillUseThisFile = (currentFileSize < maxFileSizeOnDisk);
		if (canStillUseThisFile == false) {
			fileIsFull.set(true);
			stopFileUse();
		}
		TableDataPositionResult dataPositionResult = new TableDataPositionResult(TableDataHandler.currentNodeID, fileID, currentFileEntriesNumber - 1, canStillUseThisFile);
		return dataPositionResult;
	}
	
	/** Suppose que 
	 *  @param dataPosition
	 *  @param inTable
	 *  @return
	 * @throws IOException 
	 */
	public ArrayList<Object> getValuesOfLineById(DiskDataPosition dataPosition, Table inTable) throws IOException {
		if (isReadOnlyMode == false) throw new IOException("Le fichier doit être ouvert en lecture."); // erreur, le fichier DOIT être ouvert en lecture
		int binPosInFile = dataPosition.lineIndex * inTable.getLineSize();
		// manifestement incompatible avec les streams, le skip négatif ><"  int hasToSkipBytes = binPosInFile - positionInReadOnlyFile; // positif ou négatif
		// Alternative : utiliser RandomAccessFile
		streamReader.reset(); // <- ça fera peut-être exploser la mémoire...
		// Seek to the right position in the stream
		streamReader.skipForce(binPosInFile);
		positionInReadOnlyFile = binPosInFile;

		ArrayList<Object> lineValues = new ArrayList<>(); // rowValues
		// For each column, reads the associated value
		for (Column column : inTable.getColumns()) {
			byte[] columnValueAsByteArray = new byte[column.getSize()];
			streamReader.read(columnValueAsByteArray); // reads from the stream
			//Log.debug(b); for debug purposes only
			lineValues.add(column.getDataType().readTrueValue(columnValueAsByteArray));
		}
		return lineValues;
		
	}
	
}
