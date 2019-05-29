package db.structure;

import java.util.ArrayList;

import db.indexHash.KeyHash;

/**
 *  KeyMaker, pour ne pas avoir à créer un tableau lors de la création des clefs, pas super optilisé mais pratique (parfois)
 */
@Deprecated
public class KeyHashMaker {
	
	protected ArrayList<Object> valuesList = new ArrayList<Object>(); // Liste des valeurs de la clef : Integer, String ...
	
	public KeyHashMaker() {
		
	}
	
	public void addValue(Object newValue) {
		valuesList.add(newValue);
	}
	
	public Key makeKey() {
		Object[] objectArray = valuesList.toArray();
		return null;
		//return new KeyHash(objectArray);
	}
	
}
