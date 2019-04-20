package noob.fastStructure;

import java.util.ArrayList;
import java.util.HashMap;

import db.structure.Column;

/** 
 * Structure super pas optimisée,
 * permier benchmark via les HashMap de Java
 * 
 * 
 *
 */
public class SIndexHash {
	// Une ou plusieurs colonnes indexées ici
	public final Column[] indexedColumns; 
	private HashMap<SKeyHash, ArrayList<Integer>> storedLinesPosition;
	/*
	
	*/
	
	/** Préférer ce constructeur, car il permet d'allouer la HashMap avec une talle adéquate
	 *  @param argIndexedColumns Les colonnes indexées
	 *  @param argTotalCapacity  La capacité totale initiale
	 */
	public SIndexHash(Column[] argIndexedColumns, int argTotalCapacity) {
		indexedColumns = argIndexedColumns;
		storedLinesPosition = new HashMap<>(argTotalCapacity);
	}
	
	/** 
	 *  @param argIndexedColumns
	 */
	public SIndexHash(Column[] argIndexedColumns) {
		indexedColumns = argIndexedColumns;
		storedLinesPosition = new HashMap<>();
	}
	
	/** Ajouter un élément à cette map
	 *  @param argKeyValues
	 *  @param argLinePosition
	 */
	public void put(byte[] argKeyValues, int argLinePosition) {
		SKeyHash key = new SKeyHash(argKeyValues);
		put(key, argLinePosition);
	}
	
	/** Ajouter un élément à cette map
	 *  @param key
	 *  @param argLinePosition
	 */
	public void put(SKeyHash key, int argLinePosition) {
		ArrayList<Integer> positions = storedLinesPosition.get(key);
		if (positions == null) {
			positions = new ArrayList<Integer>();
			storedLinesPosition.put(key, positions);
		}
		positions.add(argLinePosition);
	}
	
	
	/** Trouver la liste des positions de lignes correspondant à la valeur entrée
	 *  @param argKeyValues
	 *  @return
	 */
	public int[] get(byte[] argKeyValues) {
		SKeyHash key = new SKeyHash(argKeyValues);
		return get(key);
	}
	
	/** Trouver la liste des positions de lignes correspondant à la valeur entrée
	 *  @param argKeyValues
	 *  @return
	 */
	public int[] get(SKeyHash key) {
		ArrayList<Integer> positions = storedLinesPosition.get(key);
		if (positions == null) return new int[0];
		int[] result = new int[positions.size()];
		for (int i = 0; i < positions.size(); i++) {
			result[i] = positions.get(i);
		}
		return result;
	}
	
	
	
}
