package noob.fastStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.dant.utils.Log;

import db.structure.Column;

/** 
 * Structure super pas optimisée,
 * permier benchmark via les HashMap de Java
 * 
 * 
 *
 */
public class SIndexHashJava {
	// Une ou plusieurs colonnes indexées ici
	public final Column[] indexedColumns; 
	private HashMap<SKeyHashJava, ArrayList<Integer>> storedLinesPosition;
	/*
	
	*/
	
	/** Préférer ce constructeur, car il permet d'allouer la HashMap avec une talle adéquate
	 *  @param argIndexedColumns Les colonnes indexées
	 *  @param argTotalCapacity  La capacité totale initiale
	 */
	public SIndexHashJava(Column[] argIndexedColumns, int argTotalCapacity) {
		indexedColumns = argIndexedColumns;
		storedLinesPosition = new HashMap<>(argTotalCapacity);
	}
	
	/** 
	 *  @param argIndexedColumns
	 */
	public SIndexHashJava(Column[] argIndexedColumns) {
		indexedColumns = argIndexedColumns;
		storedLinesPosition = new HashMap<>();
	}
	
	/** Ajouter un élément à cette map
	 *  @param argKeyValues
	 *  @param argLinePosition
	 */
	public void put(byte[] argKeyValues, int argLinePosition) {
		SKeyHashJava key = new SKeyHashJava(argKeyValues);
		put(key, argLinePosition);
	}
	
	/** Ajouter un élément à cette map
	 *  @param key
	 *  @param argLinePosition
	 */
	public void put(SKeyHashJava key, int argLinePosition) {
		
		ArrayList<Integer> positions = storedLinesPosition.get(key);
		
		if (positions == null) {
			positions = new ArrayList<Integer>();
			storedLinesPosition.put(key, positions);
			
			/* DEBUG only
			String debugKeys = "";
			for (int iKey = 0; iKey < key.values.length; iKey++) {
				debugKeys += key.values[iKey];
			}
			Log.info(hashCode() + " - Ajout nouvelle clef !! : hash(" + key.hashCode() + ") values(" + debugKeys + ")");
			Log.info("Etat de la hashMap : taille=" + storedLinesPosition.size());
			
			SKeyHash[] keyArray = new SKeyHash[storedLinesPosition.size()];
			int iKey = 0;
			for (Entry<SKeyHash, ArrayList<Integer>> cEntry : storedLinesPosition.entrySet()) {
				Log.info("   hash(" + cEntry.getKey().hashCode() + ")  addrMem(" + cEntry.getKey().toString() + ")");
				keyArray[iKey] = cEntry.getKey();
				iKey++;
			}
			if (storedLinesPosition.size() == 3) {
				Log.info("equals : " + keyArray[0].equals(keyArray[1]) + " et " + keyArray[0].values + "  -  " + keyArray[1].values + "  -  " + keyArray[2]);
			}*/
			
		}
		positions.add(argLinePosition);
		//Log.info("Ajout nouvelle position : " + argLinePosition + "  len = " + positions.size());
	}
	
	
	/** Trouver la liste des positions de lignes correspondant à la valeur entrée
	 *  @param argKeyValues
	 *  @return
	 */
	public int[] get(byte[] argKeyValues) {
		SKeyHashJava key = new SKeyHashJava(argKeyValues);
		//Log.info("get : " + key.values[0]);
		return get(key);
	}
	
	/** Trouver la liste des positions de lignes correspondant à la valeur entrée
	 *  @param argKeyValues
	 *  @return
	 */
	public int[] get(SKeyHashJava key) {
		ArrayList<Integer> positions = storedLinesPosition.get(key);
		//Log.info("get : positions = " + positions);
		if (positions == null) return new int[0];
		int[] result = new int[positions.size()];
		for (int i = 0; i < positions.size(); i++) {
			result[i] = positions.get(i);
		}
		return result;
	}
	
	
	
}
