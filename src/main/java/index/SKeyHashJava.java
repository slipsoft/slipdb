package index;

import java.util.Arrays;


/** Pour faire un benchmark
 *  
 *  Structure pas vraiment optimisée mais fonctionnelle : une seule HashMap
 *  permier benchmark via les HashMap de Java
 *  
 *  Ne pas utiliser ce type d'index en production.
 *  
 */
/**
 * Soit, un array d'objets et j'en fais le hash, à chaque fois -> c'est vraiment pas opti d'avoir des objets en plus
 * Soit un ByteBuffer avec des types primitifs, ça limite (un peu...) le nombre d'objets en mémoire
 * 
 * */
@Deprecated
public class SKeyHashJava {
	
	public final byte[] values; // Plus générique et optmisé que des objets (c'est un seul objet)
	
	/*public byte[] getValues() {
		return values;
	}*/
	
	/** 
	 *  @param argValues
	 */
	public SKeyHashJava(byte[] argValues) {
		values = argValues;
	}
	
	@Override
	public int hashCode() {
		// tester avec : ArrayUtils.hashCode(values);
		return Arrays.hashCode(values);
	}
	
	@Override
	public boolean equals(Object o) {
		//if (true) return false;
		if (o == null) return false;
		if (o.getClass() != getClass()) return false;
		return Arrays.equals(((SKeyHashJava)o).values, values);
	}
	
	
	
}



