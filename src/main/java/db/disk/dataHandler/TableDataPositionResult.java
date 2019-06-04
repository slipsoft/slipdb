package db.disk.dataHandler;

@Deprecated
public class TableDataPositionResult {
	
	public final DiskDataPosition dataPosition;
	public final boolean canStillUseThisFile;
	
	public TableDataPositionResult(short argFileID, int argLineIndex, boolean argCanStillUseThisFile) { // short argNodeID, 
		dataPosition = new DiskDataPosition(argFileID, argLineIndex);
		canStillUseThisFile = argCanStillUseThisFile;
	}
	
	
	
}