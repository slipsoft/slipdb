package hashmap.tests;

import java.util.HashMap;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import com.dant.utils.Log;

/**
 *  Test des collisions de hash, c'est OK pour une HashMap, il faut juste avoir le moins de collisons possibles
 *  pour que la HashMap soit optimisée et ait un sens.
 *  
 */

public class HashMapTests {
	
	@Test
	public void test() {
		HashMap<ClumsyHashedObject, Integer> hm = new HashMap<>();

		ClumsyHashedObject c1 = new ClumsyHashedObject("Salut !");
		ClumsyHashedObject c2 = new ClumsyHashedObject("Salut vous !");
		hm.put(c1, 40);
		hm.put(c2, 60);
		//ArrayUtils.hashCode(array)
		Integer res = hm.get(c1);
		Log.info("" + res);
		
		//hm.get(key)
	}
	/*
	public static void main(String[] args) {
	}*/
	
}


class ClumsyHashedObject {
	
	public Object myValue;
	
	public ClumsyHashedObject(Object argMyValue) {
		myValue = argMyValue;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o.getClass() != getClass()) return false;
		if (((ClumsyHashedObject)o).myValue.equals(myValue) == false) return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		return 20; // hash identique pour toutes les instances, mais ça fonctionne quand-même (pour tester les collisions)
		//return myValue.hashCode();
	}
	
}
