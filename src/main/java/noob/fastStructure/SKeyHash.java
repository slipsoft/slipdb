package noob.fastStructure;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

// Structure super pas optimisée
/*
 * Soit, un array d'objets et j'en fais le hash, à chaque fois -> c'est vraiment pas opti d'avoir des objets en plus
 * Soit un ByteBuffer avec des types primitifs, ça limite (un peu...) le nombre d'objets en mémoire
 * 
 * */
public class SKeyHash {
	
	public final byte[] values; // Plus générique et optmisé que des objets (c'est un seul objet)
	
	/*public byte[] getValues() {
		return values;
	}*/
	
	/** 
	 *  @param argValues
	 */
	public SKeyHash(byte[] argValues) {
		//ByteBuffer buff = ByteBuffer.allocateDirect(10);
		//buff.hashCode()
		values = argValues;
		
	}
	
	@Override
	public int hashCode() {
		// tester avec : ArrayUtils.hashCode(values);
		return Arrays.hashCode(values);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o.getClass() != getClass()) return false;
		if (Arrays.equals(((SKeyHash)o).values, values) == false) return false;
		return true;
	}
	
	
	
}



