package db.disk.dataHandler;


/**
 *  Position exacte d'une donnée (ligne) écrite sur le disque
 *
 */
public class TableDataPosition {
	

	public final short nodeID;
	public final short fileID;
	public final int lineIndex;
	
	public TableDataPosition(short argNodeID, short argFileID, int argLineIndex) {
		nodeID = argNodeID;
		fileID = argFileID;
		lineIndex = argLineIndex;
	}
	
	
	
}
