package db.disk.dataHandler;

import java.io.IOException;

public class TableDataHandlerWriteJob {
	
	protected final TableDataHandler dataHandler;
	protected TableDataHandlerFile currentWriteFile = null;
	
	public TableDataHandlerWriteJob(TableDataHandler argDataHandler) {
		dataHandler = argDataHandler;
	}
	
	public TableDataPosition writeDataLine(byte[] dataAsByteArray) throws IOException, Exception {
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
	
	
}
