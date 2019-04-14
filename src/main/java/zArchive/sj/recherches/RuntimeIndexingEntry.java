package zArchive.sj.recherches;

import java.io.IOException;
import java.io.Serializable;

import db.disk.dataHandler.DiskDataPosition;
import db.structure.Column;
import db.structure.Table;
import index.indexTree.IndexTreeDic;

public class RuntimeIndexingEntry implements Comparable<RuntimeIndexingEntry>, Serializable {
	
	// Support des index mono-colonne uniquement, à ce jour
	
	private static final long serialVersionUID = 2604537941424926968L;
	public int columnIndex;
	public IndexTreeDic associatedIndexTree;
	public Table associatedTable;
	public Column associatedColumn;
	
	
	@Override
	public int compareTo(RuntimeIndexingEntry o) {
		if (columnIndex > o.columnIndex) return 1;
		if (columnIndex == o.columnIndex) return 0;
		return -1;
	}
	
	/** Thread-safe si associatedIndexTree.addValue est thread-safe.
	 *  @param argAssociatedValue
	 *  @param dataPosition
	 *  @throws IOException
	 */
	public void addIndexValue(Object argAssociatedValue, DiskDataPosition dataPosition) throws IOException {
		associatedIndexTree.addValue(argAssociatedValue, dataPosition);
	}
	
}
