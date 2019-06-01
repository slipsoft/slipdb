package db.structure;

import java.nio.ByteBuffer;

import db.data.types.DataType;

public class TableFlagChunk {
	
	
	public final static int chunkAllocationSize = 1_000_000;
	private final int maxNumberOfItems;
	private boolean[] stillPresentFlag;
	// stillPresentFlag pourrait être un byte[] pour permettre le stockage de plusieurs bits,
	// et donc par exemple la protection en leture/écriture pour le multi-noeud et multi-thread
	// i.e. du row-level locking.
	private int currentItemPosition = 0;
	
	public TableFlagChunk() {
		maxNumberOfItems = chunkAllocationSize;
		currentItemPosition = 0;
		stillPresentFlag = new boolean[maxNumberOfItems];
		for (int iItem = 0; iItem < maxNumberOfItems; iItem++) {
			stillPresentFlag[iItem] = true; // par défaut, le flag "présent" est mis à true
		}
	}
	
	/** 
	 * 	@param localIndex  index dans le tableau local de ce chunk
	 *  @param isPresent  flag indiquant si la ligne est présente (true) ou absente (false)
	 */
	public void setItem(int localIndex, boolean isPresent) {
		stillPresentFlag[localIndex] = isPresent;
	}
	
	/** Ajout d'une ligne
	 *  @return  true s'il faut créer un nouveau chunk, false sinon.
	 */
	public boolean setNewItem() {
		stillPresentFlag[currentItemPosition] = true;
		currentItemPosition++;
		if (currentItemPosition >= maxNumberOfItems) {
			return true;
		}
		return false;
	}
	
	/** Ajout d'une ligne
	 *  @return  true s'il faut créer un nouveau chunk, false sinon.
	 */
	public boolean addLine() {
		return setNewItem();
	}
	
	public boolean getItemFlag(int localIndex) {
		return stillPresentFlag[localIndex];
	}
	
	
	
}
