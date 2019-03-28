package db.structure.recherches;

import java.util.ArrayList;
import java.util.Collections;

public class RuntimeIndexingEntryList extends ArrayList<RuntimeIndexingEntry> {
	private static final long serialVersionUID = 6016049346172625358L;
	
	
	public void sort() { // très important lors de la lecture des colonnes (parsing) - sortRuntimeIndexingList
		Collections.sort(this); // (List<SInitialIndexingIndex>)
	}
	
	public RuntimeIndexingEntry getEntryAssociatedWithColumnIndex(int columnIndex) {
		for (int colIndex = 0; colIndex < this.size(); colIndex++) {
			RuntimeIndexingEntry currentEntry = this.get(colIndex);
			if (currentEntry.columnIndex == columnIndex) {
				return currentEntry;
			}
		}
		return null;
	}
	
	
}
