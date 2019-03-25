package db.disk.dataHandler;

import java.io.Closeable;
import java.io.IOException;

public class TableDataHandlerWriteJob implements Closeable {
	
	protected final TableDataHandler dataHandler;
	protected TableDataHandlerFile currentWriteFile = null;
	
	public TableDataHandlerWriteJob(TableDataHandler argDataHandler) {
		dataHandler = argDataHandler;
	}
	
	public DiskDataPosition writeDataLine(byte[] dataAsByteArray) throws IOException, Exception {
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
	
	public void closeJob() throws IOException, Exception {
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
