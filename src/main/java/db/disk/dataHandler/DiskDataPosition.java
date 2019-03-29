package db.disk.dataHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;

/**
 *  Position exacte d'une donnée (ligne) écrite sur le disque
 *
 */
public class DiskDataPosition implements Comparable<DiskDataPosition>, Serializable {
	
	private static final long serialVersionUID = 8763520237301457678L;
	
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
	
	@Override
	public int compareTo(DiskDataPosition o) {
		DiskDataPosition otherObject = (DiskDataPosition) o;
		if (nodeID > otherObject.nodeID) return 1;
		if (nodeID < otherObject.nodeID) return -1;
		if (fileID > otherObject.fileID) return 1;
		if (fileID < otherObject.fileID) return -1;
		if (lineIndex > otherObject.lineIndex) return 1;
		if (lineIndex < otherObject.lineIndex) return -1;
		return 0;
	}
	
}
