package db.sTreeIndex;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

public class TreeMapTests {
	
	public TreeMap<String, Integer> theTreeMap = new TreeMap<String, Integer>();
	
	@Test
	public void mainTest() {
		theTreeMap.put("salut", 10);
		theTreeMap.put("savmr", 11);
		theTreeMap.put("samtaupe", 12);
		theTreeMap.put("sartarentule", 13);
		theTreeMap.put("saluutte", 14);
		
		// Les TreeMap, bordel, c'est teeeeeeeeeeeeeellement cool !
		
		SortedMap<String, Integer> subMap = theTreeMap.subMap("sam", "saz");
		
		for (Entry<String, Integer> ent : subMap.entrySet()) {
			System.out.println(ent.getKey() + " -> " + ent.getValue());
		}
		
		
	}
	
	
}
