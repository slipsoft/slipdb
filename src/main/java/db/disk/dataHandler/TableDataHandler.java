package db.disk.dataHandler;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.dant.utils.Log;

import db.structure.Column;
import db.structure.Table;

/**
 *  Gestion de la donnée sauvegardée sur le disque,
 *  pour rendre ça compatible multi-thread, multi-noeuds.
 *  
 *  -> Ne sert PAS à stocker la donnée des index (comme IndexTreeDic), ils gèrent leur donnée
 *  
 *  Pour l'instant, je considère qu'il n'y a qu'une seule table, je ne me soucie pas de la répartition de la donnée entre tables.
 *  
 *  
 *  Thread-safe pour les parties qui le nécessitent,
 *  mais globalement non-thread-safe : doit être utilisé par le thread qui gère la table myTable.
 */
public class TableDataHandler {
	
	static protected short currentNodeID = 1;
	static protected String saveFileBaseName = "table_data_n1_";
	static final public String fileExtension = ".bin";
	
	protected final Table myTable; // Table associée
	protected final String baseTablePath;
	// Liste de tous les fichiers (pleins, utilisés ou non utilisés) :
	protected ArrayList<TableDataHandlerFile> allFilesList = new ArrayList<TableDataHandlerFile>();
	// Liste des jobs en cours
	protected ArrayList<TableDataHandlerWriteJob> allRunningJobsList = new ArrayList<TableDataHandlerWriteJob>();
	
	protected AtomicInteger nextFileID = new AtomicInteger(1);
	
	protected Object allFilesListLock = new Object();
	
	static public void setNodeID(short argCurrentNodeID) {
		currentNodeID = argCurrentNodeID;
		saveFileBaseName = "table_data_nid" + currentNodeID + "_fid"; // nodeID, fileID
	}
	
	/*
		Besoin :
			- Ecrire une ligne sur le disque : peu importe où, on renvoie sa position (plus tard, éventuellement, optimisations sur la position)
		
		
	 * Les Jobs doivent être thread-safe
	 * 
	 * */
	
	public TableDataHandler(Table argTable, String argBaseTablePath) {
		myTable = argTable;
		baseTablePath = argBaseTablePath;
		// myTable ne doit pas être null (lever une exception, plus tard, sinon)
		
	}
	
	public String getFileNameFromDataPosition(short nodeID, short fileID) {
		return "tdata_nid" + nodeID + "_fid" + fileID + "_" + fileExtension; // nodeID, fileID
	}
	
	/*public TableDataHandlerJob newJob() {
		
	}*/
	
	
	public static int debugLastFileID = -1;
	
	/** Marche pour une seule demande isolée, mais TRES LENT pour plusieurs résultats.
	 *  Utiliser 
	 *  @param dataPosition
	 *  @return
	 *  @throws IOException
	 */
	public ArrayList<Object> getValuesOfLineByIdForSignleQuery(DiskDataPosition dataPosition) throws IOException { // or getRowById
		
		if (debugLastFileID != dataPosition.fileID) {
			Log.info("getValuesOfLineByIdForSignleQuery depuis fileID = " + dataPosition.fileID);
			debugLastFileID = dataPosition.fileID;
		}
		
		//String fileName = getFileNameFromDataPosition(dataPosition.nodeID, dataPosition.fileID);
		// accès au fichier
		synchronized (allFilesListLock) {
			for (TableDataHandlerFile dataFile : allFilesList) {
				if (dataFile.getFileID() == dataPosition.fileID) {
					if (dataFile.tryToUseThisFile(true)) {
						ArrayList<Object> resultArray = dataFile.getValuesOfLineById(dataPosition, myTable);
						dataFile.stopFileUse();
						return resultArray;
					} else {
						throw new IOException("Impossible de libre du fichier : il est occupé.");
					}
				}
			}
		}
		return new ArrayList<Object>();
	}
	
	
	/** 
	    Thread-safe
	    Pas besoin de fonction spéciale pour libérer le fichier, juste faire
	    un .stopFileUse(); -> géré automatiquement par TableDataHandlerWriteJob s'il est utilisé
	    
	 */
	public TableDataHandlerFile findOrCreateWriteFile() throws IOException { synchronized (allFilesListLock) {
			TableDataHandlerFile foundDataFile = null;
			for (TableDataHandlerFile dataFile : allFilesList) {
				//if (dataFile.fileIsFull.get() == false)
				//if (dataFile.currentlyInUse.get() == false) {
				if (dataFile.tryToUseThisFile(false)) {
					foundDataFile = dataFile;
				}
			}
			// Création d'un nouveau fichier, dans l'état occupé
			if (foundDataFile == null) {
				short fileID = (short)nextFileID.getAndIncrement();
				String fullFilePath = baseTablePath + saveFileBaseName + fileID + fileExtension;// getFileNameFromDataPosition(); //
				foundDataFile = new TableDataHandlerFile(fileID, fullFilePath);
				boolean canUseFile = foundDataFile.tryToUseThisFile(false);
				if (canUseFile == false) {
					Log.error("TableDataHandler.findOrCreateWriteFile : canUseFile == false alors que je viens de créer le fichier.");
				}
			}
			// tryToUseThisFile a bien été fait sur le fichier, il est ajouté dans la liste comme étant occupé
			allFilesList.add(foundDataFile);
			return foundDataFile;
		}
	}
	
	// NON thread-safe, le fichier doit déjà être ouvert et acquis.
	/*public TableDataPositionResult writeDataLine(byte[] dataAsByteArray, TableDataHandlerFile inDataFile) throws Exception {
		return inDataFile.writeDataLine(dataAsByteArray);
	}*/
	
	/**
		Pour écrire une donnée sur le disque :
		-> Faire findOrCreateWriteFile(...) pour trouver un fichier dans lequel écrire
		-> Ecrire dans le fichier
		-> Vérifier que je peux toujours utiliser le fichier via TableDataPositionResult.canStillUseThisFile
			- inutile de fermer le fichier si (canStillUseThisFile == false), il est déjà fermé
		-> Fermer le fichier via TableDataHandlerFile.setInUse(false)
	 */
	
	public TableDataHandlerWriteJob createNewWriteJob() {
		return new TableDataHandlerWriteJob(this);
	}
	// TableDataHandlerWriteJob s'occupe d'écrire les données sur le disque, d'une manière thread-safe
	
	
	/* Recherche : 
	 * Ecrire une ligne sur disque :
	 * Soit écrire les lignes unes par unes, dans un des fichiers dispo,
	 * Soit allouer un fichier à un "job", écrire dans ce fichier jusqu'à ce qu'il soit plein.
	 * -> Liste des jobs en cours, avec un JobHandler par job.
	 *  - Chaque JobHandler 
	 *  
	 * */
	
	
	
	
	
}
