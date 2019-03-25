package db.disk.dataHandler;

public class TableDataPositionResult {
	
	public final TableDataPosition dataPosition;
	public final boolean canStillUseThisFile;
	
	public TableDataPositionResult(short argNodeID, short argFileID, int argLineIndex, boolean argCanStillUseThisFile) {
		dataPosition = new TableDataPosition(argNodeID, argFileID, argLineIndex);
		canStillUseThisFile = argCanStillUseThisFile;
	}
	
	
	
}