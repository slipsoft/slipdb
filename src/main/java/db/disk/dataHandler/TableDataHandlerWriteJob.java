package db.disk.dataHandler;

import java.io.Closeable;
import java.io.IOException;

import com.dant.utils.Log;

public class TableDataHandlerWriteJob implements Closeable {
	
	protected final TableDataHandler dataHandler;
	protected TableDataHandlerFile currentWriteFile = null;
	
	public TableDataHandlerWriteJob(TableDataHandler argDataHandler) {
		dataHandler = argDataHandler;
		Log.info("TableDataHandlerWriteJob constructeur");
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
		Log.info("TableDataHandlerWriteJob CLOSE job !!");
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
