package zArchive.sj.simpleDB.treeIndexing;

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

		theTreeMap.put("2015-04-04 00:00:40", 15);
		theTreeMap.put("2015-04-04 00:01:40", 16);
		theTreeMap.put("2015-04-04 00:02:40", 17);
		theTreeMap.put("2015-04-04 00:03:40", 18);
		theTreeMap.put("2015-04-04 00:04:40", 19);
		theTreeMap.put("2015-04-04 00:10:40", 20);
		theTreeMap.put("2015-04-04 00:11:40", 21);
		theTreeMap.put("2015-04-04 00:12:40", 22);
		theTreeMap.put("2015-04-04 00:13:40", 23);
		theTreeMap.put("2015-04-04 00:14:40", 24);
		theTreeMap.put("2015-04-04 00:21:40", 10);
		
		// Les TreeMap, bordel, c'est teeeeeeeeeeeeeellement cool !
		
		SortedMap<String, Integer> subMap = theTreeMap.subMap("2015-04-04 00:00:40", "2015-04-04 00:10:41");
		
		for (Entry<String, Integer> ent : subMap.entrySet()) {
			System.out.println(ent.getKey() + " -> " + ent.getValue());
		}
		
		
	}
	
	
}
