package db.disk.dataHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *  Position exacte d'une donnée (ligne) écrite sur le disque
 *
 */
public class DiskDataPosition {
	
	static public final int diskDataPositionSizeOnDisk = 2 + 2 + 4; // 8

	public final short nodeID;
	public final short fileID;
	public final int lineIndex;
	
	public DiskDataPosition(short argNodeID, short argFileID, int argLineIndex) {
		nodeID = argNodeID;
		fileID = argFileID;
		lineIndex = argLineIndex;
	}

	public void writeInStream(DataOutputStream writeInDataStream) throws IOException {
		writeInDataStream.writeShort(nodeID);
		writeInDataStream.writeShort(fileID);
		writeInDataStream.writeInt(lineIndex);
	}
	

	public static DiskDataPosition readFromStream(DataInputStream readFromDataStream) throws IOException {
		short nodeID = readFromDataStream.readShort();
		short fileID = readFromDataStream.readShort();
		int lineIndex = readFromDataStream.readInt();
		return new DiskDataPosition(nodeID, fileID, lineIndex);
	}

	public static DiskDataPosition readFromRandFile(RandomAccessFile readFromRandFile) throws IOException {
		short nodeID = readFromRandFile.readShort();
		short fileID = readFromRandFile.readShort();
		int lineIndex = readFromRandFile.readInt();
		return new DiskDataPosition(nodeID, fileID, lineIndex);
	}
	
}
