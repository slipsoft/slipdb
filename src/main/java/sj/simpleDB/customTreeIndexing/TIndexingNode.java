package sj.simpleDB.customTreeIndexing;

import java.util.ArrayList;

public class TIndexingNode { //implements SIndexingNodeInterface {
	

	public float valueMin, valueMax; // valeur minimale et maximale gérée
	
	public TIndexingNode[] childNodeArray = null;
	public ArrayList<Integer> indexList = null;
	
	public TIndexingNode(float aValueMin, float aValueMax, int aNodeSplitFactor, int aTreeDepthRemaining) {
		valueMin = aValueMin;
		valueMax = aValueMax;
		
		if (aTreeDepthRemaining == 0) {
			// Création de la liste des feuilles
			indexList = new ArrayList<Integer>();
		} else {
			childNodeArray = new TIndexingNode[aNodeSplitFactor];
			// Création de la liste des noeufs enfants
			float decimalSplitValue = (aValueMax - aValueMin) / aNodeSplitFactor;
			float currentNodeValue = aValueMin;
			for (int iChildNode = 0; iChildNode < aNodeSplitFactor; iChildNode++) {
				childNodeArray[iChildNode] = new TIndexingNode(currentNodeValue, currentNodeValue + decimalSplitValue, aNodeSplitFactor, aTreeDepthRemaining - 1);
				currentNodeValue += decimalSplitValue;
			}
		}
	}
	
	//@Override
	/*public boolean isLeaf() {
		return false;
	}*/
	
}
