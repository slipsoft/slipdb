package db.disk.dataHandler;

import java.io.Closeable;
import java.io.IOException;

/**
 *  -> Un WriteJob par thread, le WriteJob NE PEUT PAS être partagé entre plusieurs threads.
 *  -> Il peut (et c'est le but) y avoir plusieurs WriteJobs qui s'exécutent simultanément de plusieurs threads
 *     différents.
 *  
 *  Le WriteJob permet d'écrire de la donnée sur le disque sans avoir à se soucier du fichier
 *  dans laquelle la donnée va, et sans avoir à gérer les accès concurrents des autres threads.
 *  
 *  Pour écrire de la donnée (d'un Loader par exemple), il suffit de créer un nouveau TableDataHandlerWriteJob
 *  et de faire des writeDataLine(..). Ne pas oublier de closeJob (ou close) quand l'écriture du thread est terminée.
 *
 */
public class TableDataHandlerWriteJob implements Closeable { // AutoClosable nécessaire pour des blocs try simples
	
	protected final TableDataHandler dataHandler;
	protected TableDataHandlerFile currentWriteFile = null;
	
	public TableDataHandlerWriteJob(TableDataHandler argDataHandler) {
		dataHandler = argDataHandler;
		//Log.info("TableDataHandlerWriteJob constructeur");
	}
	
	public DiskDataPosition writeDataLine(byte[] dataAsByteArray) throws IOException {
		//Log.info("TableDataHandlerWriteJob WRITE DATA ON DISK ! size = " + dataAsByteArray.length);
		if (currentWriteFile == null) {
			currentWriteFile = dataHandler.findOrCreateWriteFile();
		}
		TableDataPositionResult dataPositionResult = currentWriteFile.writeDataLine(dataAsByteArray);
		// Rechercher un nouveau fichier si besoin
		if (dataPositionResult.canStillUseThisFile == false) {
			currentWriteFile = dataHandler.findOrCreateWriteFile();
		}
		return dataPositionResult.dataPosition;
	}
	
	public void closeJob() throws IOException {
		//Log.info("TableDataHandlerWriteJob CLOSE job !!");
		if (currentWriteFile != null) {
			currentWriteFile.stopFileUse();
		}
	}

	@Override
	public void close() throws IOException {
		try {
			closeJob();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
