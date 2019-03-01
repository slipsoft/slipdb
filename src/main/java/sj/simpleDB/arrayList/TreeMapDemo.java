package sj.simpleDB.arrayList;

import java.util.SortedMap;
import java.util.TreeMap;

import db.structure.Key;

/**
 * Code issu de https://beginnersbook.com/2014/07/how-to-get-the-sub-map-from-treemap-example-java/
 * Démonstration des TreeMap et de subMap
 * Démonstration de la manière dont les clefs sont ordonnées
 *
 */

class TreeMapDemo {
	
	public static void main(String args[]) {
		mainTreeMapDemo();
	}
	
	  public static void mainTreeMapDemo() {
	 
	    // Create a TreeMap
	    TreeMap<String, String> treemap = 
	            new TreeMap<String, String>();

	    Integer[] objArray = new Integer[] {41, 20};
	    Key key1 = new Key(objArray);
	    objArray = new Integer[] {88, 12};
	    Key key2 = new Key(objArray);
	    objArray = new Integer[] {841, 2220};
	    Key key3 = new Key(objArray);

	    TreeMap<Key, String> treemap2 = 
	            new TreeMap<Key, String>();

	    treemap2.put(key1, "Value 1");
	    System.out.println("TreeMap Contains : " + treemap);
	    
	    
	    if (true) return;
	    
	    // Put elements to the map
	    treemap.put("Key6", "Ram");
	    treemap.put("Key1", "Jack");
	    treemap.put("Key5", "Steve");
	    treemap.put("Key20", "Rick");
	    treemap.put("Key3", "Kate");
	    treemap.put("Key4", "Tom");
	 
	    // Displaying TreeMap elements
	    System.out.println("TreeMap Contains : " + treemap);
	 
	    // Getting the sub map
	    /* public SortedMap<K,V> subMap(K fromKey,K toKey): Returns 
	     * a view of the portion of this map whose keys range from 
	     * fromKey, inclusive, to toKey, exclusive. 
	     * (If fromKey and toKey are equal, the returned map is empty.) 
	     * The returned map is backed by this map, so changes in the 
	     * returned map are reflected in this map, and vice-versa. 
	     * The returned map supports all optional map operations that 
	     * this map supports.
	     */
	    SortedMap<String, String> sortedMap = treemap.subMap("Key2","Key5");
	    System.out.println("SortedMap Contains : " + sortedMap);
	 
	    // Removing an element from Sub Map
	    sortedMap.remove("Key4");
	 
	    /* Displaying elements of original TreeMap after 
	     * removing an element from the Sub Map. Since Sub Map is 
	     * backed up by original Map, the element should be removed
	     * from this TreeMap too.
	     */
	    System.out.println("TreeMap Contains : " + treemap);
	  }
	}