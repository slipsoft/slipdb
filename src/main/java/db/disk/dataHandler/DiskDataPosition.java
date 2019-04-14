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
	
	 // A DEBUGER TODO (6 octets au lieu de 8) virer vraiment nodeID
	static public final int diskDataPositionSizeOnDisk = 2 + 2 + 4; // 8
	
	/* MAJ 01-04-2019
	 * Suppression de nodeID, il faudra explicitement importer les données d'un autre noeud pour pouvoir les utiliser.
	 * */
	//public final short nodeID;
	public final short fileID;
	public final int lineIndex;

	@Deprecated
	// Pour la rétro-compatibilité
	public DiskDataPosition(short argFileID, short argNodeID, int argLineIndex) { // short argNodeID, 
		this(argFileID, argLineIndex);
	}
	
	public DiskDataPosition(short argFileID, int argLineIndex) { // short argNodeID, 
		//nodeID = argNodeID;
		fileID = argFileID;
		lineIndex = argLineIndex;
	}

	public void writeInStream(DataOutputStream writeInDataStream) throws IOException {
		//writeInDataStream.writeShort(nodeID);
		writeInDataStream.writeShort(0);// A DEBUGER TODO (6 octets au lieu de 8) virer vraiment nodeID
		writeInDataStream.writeShort(fileID);
		writeInDataStream.writeInt(lineIndex);
	}
	

	public static DiskDataPosition readFromStream(DataInputStream readFromDataStream) throws IOException {
		//short nodeID = readFromDataStream.readShort();
		readFromDataStream.readShort();// A DEBUGER TODO (6 octets au lieu de 8) virer vraiment nodeID
		short fileID = readFromDataStream.readShort();
		int lineIndex = readFromDataStream.readInt();
		return new DiskDataPosition(fileID, lineIndex);
	}

	public static DiskDataPosition readFromRandFile(RandomAccessFile readFromRandFile) throws IOException {
		//short nodeID = readFromRandFile.readShort();
		readFromRandFile.readShort();// A DEBUGER TODO (6 octets au lieu de 8) virer vraiment nodeID
		short fileID = readFromRandFile.readShort();
		int lineIndex = readFromRandFile.readInt();
		return new DiskDataPosition(fileID, lineIndex);
	}
	
	@Override
	public int compareTo(DiskDataPosition o) {
		DiskDataPosition otherObject = (DiskDataPosition) o;
		//if (nodeID > otherObject.nodeID) return 1;
		//if (nodeID < otherObject.nodeID) return -1;
		if (fileID > otherObject.fileID) return 1;
		if (fileID < otherObject.fileID) return -1;
		if (lineIndex > otherObject.lineIndex) return 1;
		if (lineIndex < otherObject.lineIndex) return -1;
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		DiskDataPosition pos = (DiskDataPosition) o;
		return pos.fileID == this.fileID && pos.lineIndex == this.lineIndex; // pos.nodeID == this.nodeID && 
	}

	@Override
	public int hashCode() {
		return (int) 31 * this.fileID + this.lineIndex;
	}
	
}
