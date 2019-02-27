package sj.db.customTreeIndexing;


/**
 * 
 *
 */
public class TIndexingTree {
	
	public String name;
	// Pour simplifier les choses, dans un premier temps, les valeurs sont des float
	public float valueMin, valueMax; // valeur minimale et maximale gérée
	public int nodeSplitFactor; // nombre de franches filles de chaque noeud
	public int treeDepth; // profondeur de l'arbre
	// à partir de la profondeur, il est possible d'en déduire l'intervalle de valeur géré par chaque feuille : (valueMax - valueMin) / (nodeSplitFactor * treeDepth)
	// Nombre de feuilles de l'arbre : nodeSplitFactor ^ treeDepth
	public TIndexingNode firstNode;
	
	
	/** Initialisation des valeurs par défaut
	 */
	public TIndexingTree() { //(float atgValueMin, float argValueMax) {
		nodeSplitFactor = 10;
		treeDepth = 4;
		// Nombre de feuilles de l'arbre = 10^4 = 10 000
	}
	
	public void generateTree(float argValueMin, float argValueMax, String argName) {
		valueMin = argValueMin;
		valueMax = argValueMax;
		name = argName;
		firstNode = new TIndexingNode(valueMin, valueMax, nodeSplitFactor, treeDepth);
		
	}
	
	/** Insérer un index dans l'arbre
	 * Je recherche la feuille et je l'ajoute à l'arbre
	 * @param value
	 * @param index
	 */
	public void insertIndex(float valueInColumn, int globalIndex) {
		// Je peux directement évaluer le noeud qui contient la liste des feuilles :
		
	}
	
	
	
	
}
