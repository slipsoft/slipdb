package db.disk.dataHandler;

public class TableDataPositionResult {
	
	public final DiskDataPosition dataPosition;
	public final boolean canStillUseThisFile;
	
	public TableDataPositionResult(short argNodeID, short argFileID, int argLineIndex, boolean argCanStillUseThisFile) {
		dataPosition = new DiskDataPosition(argNodeID, argFileID, argLineIndex);
		canStillUseThisFile = argCanStillUseThisFile;
	}
	
	
	
}