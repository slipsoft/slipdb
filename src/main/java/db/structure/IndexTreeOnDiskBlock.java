package db.structure;

/**
 * For our engine to work with huge files, we need to put the indexes on the disk.
 * 
 * 
 */
public class IndexTreeOnDiskBlock {
	
	public long seekPosInFile; // the position where this block is
	public long blockSizeInBytes; // the block size, in bytes (not the number of items on the block)
	
	// To be able to remove data from a table without messing up all the indexes,
	// a boolean value "still present" will be necessary before each field
	// This slows everything down, but is needed to have a fast removing of data
	
}
