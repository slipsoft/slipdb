package sj.simpleDB.treeIndexing;


public class SIndexingTreeFinderItem implements Comparable<SIndexingTreeFinderItem> {
	
	// Nombre de filtres passés avec succès
	//public int numberOfPassedFilters;
	public boolean passedThisFilter;
	public int binIndex;
	
	public SIndexingTreeFinderItem(boolean aPassedThisFilter, int aBinIndex) { // int aNumberOfPassedFilters
		passedThisFilter = aPassedThisFilter;
		//numberOfPassedFilters = aNumberOfPassedFilters;
		binIndex = aBinIndex;
	}
	
	@Override
	public int compareTo(SIndexingTreeFinderItem other) {
		return binIndex - other.binIndex;
	}
	
}
