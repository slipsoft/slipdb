package db.disk.dataHandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dant.utils.EasyFile;
import com.dant.utils.Log;

public class TableDataHandlerFile {
	
	// Champs mis en public pour gagner du temps
	// Thread-safe (c'est d'ailleurs le but !)

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
	private DataInputStream streamReader = null;
	private boolean isReadOnlyMode;
	
	static public final long maxFileSizeOnDisk = 500_000_000; // Max 500 mo sur disque (grosse marge de sécurité)
	
	public TableDataHandlerFile(short argFileID, String argFilePath) throws IOException {
		fileID = argFileID;
		filePath = argFilePath;
		fileOnDisk = new EasyFile(filePath);
		fileOnDisk.createFileIfNotExist();
		currentlyInUse.set(false);
	}
	
	/**
	 * 
	 * @param readOnly  true pour readOnly, false pour writeOnly
	 * @return
	 * @throws IOException
	 */
	public boolean tryToUseThisFile(boolean readOnly) throws IOException {
		if ((readOnly == false) && (fileIsFull.get())) return false; // accès en écriture et fichier plein
		if (currentlyInUse.getAndSet(true)) return false; // je viens de passer de true -> true, déjà en cours d'utilisation
		
		
		// si je viens de passer de false -> true, c'est bon !
		// Ouverture du bon stream
		isReadOnlyMode = readOnly;
		
		if (readOnly) {
			streamReader = new DataInputStream(new BufferedInputStream(new FileInputStream(fileOnDisk)));
		} else {
			streamWriter = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileOnDisk)));
		}
		
		return true;
	}

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
	
}
