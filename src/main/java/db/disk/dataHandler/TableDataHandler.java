package db.disk.dataHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;

import com.dant.utils.Log;

import db.data.DataPositionList;
import db.structure.Table;

/**
 *  Gestion de la donnée sauvegardée sur le disque,
 *  pour rendre ça compatible multi-thread, multi-noeuds.
 *  
 *  -> Ne sert PAS à stocker la donnée des index (comme IndexTreeDic), ils gèrent leur donnée
 *  
 *  Pour l'instant, je considère qu'il n'y a qu'une seule table, je ne me soucie pas de la répartition de la donnée entre tables.
 *  
 *  Thread-safe pour les parties qui le nécessitent,
 *  mais globalement non-thread-safe : doit être utilisé par le thread qui gère la table myTable.
 */
public class TableDataHandler {
	
	static {
		setNodeID((short) 1); // TODO parametrer le VRAI NodeID
	}
	
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
	
	public void clearDataDirectory() throws IOException {
		FileUtils.deleteDirectory(new File(myTable.getBaseTablePath()));
	}
	
	
	public static int debugLastFileID = -1;
	
	/** Marche pour une seule demande isolée, mais TRES LENT pour plusieurs résultats.
	 *  Utiliser 
	 *  @param dataPosition
	 *  @return
	 *  @throws IOException
	 */
	@Deprecated
	public ArrayList<Object> getValuesOfLineByIdForSignleQuery(DiskDataPosition dataPosition) throws IOException { // or getRowById
		
		if (debugLastFileID != dataPosition.fileID) { // Afficher un message à chaque changement de fichier
			//Log.info("getValuesOfLineByIdForSignleQuery depuis fileID = " + dataPosition.fileID);
			debugLastFileID = dataPosition.fileID;
		}
		
		//String fileName = getFileNameFromDataPosition(dataPosition.nodeID, dataPosition.fileID);
		// accès au fichier
		synchronized (allFilesListLock) {
			for (TableDataHandlerFile dataFile : allFilesList) {
				if (dataFile.getFileID() == dataPosition.fileID) {
					if (dataFile.tryToUseThisFile(true, true)) {
						ArrayList<Object> resultArray = dataFile.getValuesOfLineById(dataPosition, myTable);
						dataFile.stopFileUse();
						return resultArray;
					} else {
						throw new IOException("Impossible de lire du fichier : il est occupé.");
					}
				}
			}
		}
		return new ArrayList<Object>();
	}
	
	/** Rechercher de la donnée sur le disque.
	 *  Cette fonction est optimisée pour faire le moins d'appels disques possibles,
	 *  et garantit que tous les résultats seront bien lus, si waitForAllResults == true
	 *  
	 *  Une optimisation de plus serait de rendre la lecture sur fichiers multi-thread (à faire, plus tard)
	 * @param dataPosition
	 * @return
	 * @throws IOException
	 */
	public ArrayList<ArrayList<Object>> getValuesOfLinesListById(DataPositionList argDataPositionList, boolean waitForAllResults, int waitTimeLimitMs) { // or getRowById
		
		ArrayList<ArrayList<Object>> resultsArray = new ArrayList<ArrayList<Object>>();
		
		DataPositionList dataPositionList = (DataPositionList) argDataPositionList.clone(); // copie de la liste
		Collections.sort(dataPositionList); // pas super opti pour un très grand nombre de résultats (100 000+)
		// Regrouper par fichier
		short oldNodeID = -1;
		short oldFileID = -1;
		//int oldSingleID = (oldNodeID << 16) + (oldFileID << 0);
		
		// Je mets toutes les demandes portant sur le même fichier dans la même liste
		ArrayList<ArrayList<DiskDataPosition>> a2OrderedByFileList = new ArrayList<ArrayList<DiskDataPosition>>();
		ArrayList<DiskDataPosition> a1CurrentDataPosArray = null;
		
		
		for (DiskDataPosition dataPosition : dataPositionList) {
			if (dataPosition.nodeID != oldNodeID || dataPosition.fileID != oldFileID) {
				a1CurrentDataPosArray = new ArrayList<DiskDataPosition>();
				a2OrderedByFileList.add(a1CurrentDataPosArray);
				oldNodeID = dataPosition.nodeID;
				oldFileID = dataPosition.fileID;
			}
			a1CurrentDataPosArray.add(dataPosition); // pas opti de les ajouter un par un mais bon... C'est déjà opti de faire fichier par fichier !
		}
		
		synchronized (allFilesListLock) {
			int checkIndex = 0;
			while (a2OrderedByFileList.size() > 0) {
				if (checkIndex >= a1CurrentDataPosArray.size()) {
					checkIndex = 0; // boucle, revenir au premier fichier
				}
				a1CurrentDataPosArray = a2OrderedByFileList.get(checkIndex);
				if (a1CurrentDataPosArray.size() == 0) { // (improbable) liste vide, je passe à la suivante et je la supprime
					a2OrderedByFileList.remove(checkIndex);
					continue;
				}
				
				DiskDataPosition firstDataPos = a1CurrentDataPosArray.get(0); // existe bien
				//short nodeID = firstDataPos.nodeID;
				short fileID = firstDataPos.fileID;
				
				TableDataHandlerFile fondUsableDataFile = null;
				boolean foundFileInList = false;
				// Je trouve le fichier associé
				for (TableDataHandlerFile dataFile : allFilesList) {
					if (dataFile.getFileID() == fileID) {
						foundFileInList = true; // trouvé, mais pas forcément libre
						try {
							if (dataFile.tryToUseThisFile(true, true)) {
								fondUsableDataFile = dataFile;
							} else {
								break; // puis checkIndex++ si waitForAllResults
								//throw new IOException("Impossible de lire du fichier : il est occupé.");
							}
						} catch (IOException e) {
							Log.error(e);
							e.printStackTrace(); // ne devrait jamais arriver
						}
					}
				}
				
				if (foundFileInList == false) { // fichier introuvable donc, je l'ignore
					a2OrderedByFileList.remove(checkIndex);
					continue; // sans checkIndex++
				}
				
				// Fichier trouvé et donc à lire
				if (fondUsableDataFile != null) {
					// Pour chaque donnée à lire, je la lis
					for (DiskDataPosition dataPos : a1CurrentDataPosArray) {
						ArrayList<Object> entryAsArray;
						try {
							entryAsArray = fondUsableDataFile.orderedReadGetValuesOfLineById(dataPos, myTable);
							resultsArray.add(entryAsArray);
						} catch (IOException e) {
							Log.error(e);
							e.printStackTrace();
						}
					}
					try {
						fondUsableDataFile.stopFileUse();
					} catch (IOException e) {
						Log.error(e);
						e.printStackTrace();
					}
					a2OrderedByFileList.remove(checkIndex);
					continue; // sans checkIndex++
				} else { // si fondUsableDataFile == null
					if (waitForAllResults == false) {
						a2OrderedByFileList.remove(checkIndex);
						continue;
					}
					
				}
				checkIndex++;
			}
		}
		
		//argDataPositionList.toArray()
		//dataPositionList.set(index, element)
		
		
		
		// Je classe par fichiers
		// Je classe par position dans le fichier
		/*TODO
		 * Collections.sort(Database.arrayList, new Comparator<MyObject>() {
		    @Override
		    public int compare(MyObject o1, MyObject o2) {
		        return o1.getStartDate().compareTo(o2.getStartDate());
		    }
		});*/
		return resultsArray;
		
		
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
				if (dataFile.tryToUseThisFile(false, true)) {
					foundDataFile = dataFile;
					//Log.info("TableDataHandler.findOrCreateWriteFile : trouvé   dataFile == " + dataFile);
				}
			}
			// Création d'un nouveau fichier, dans l'état occupé
			if (foundDataFile == null) {
				short fileID = (short)nextFileID.getAndIncrement();
				String fullFilePath = baseTablePath + saveFileBaseName + fileID + fileExtension;// getFileNameFromDataPosition(); //
				foundDataFile = new TableDataHandlerFile(fileID, fullFilePath);
				boolean canUseFile = foundDataFile.tryToUseThisFile(false, true);
				//Log.info("TableDataHandler.findOrCreateWriteFile : créé   foundDataFile == " + foundDataFile);
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
