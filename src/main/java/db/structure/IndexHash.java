package db.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import db.data.Operator;

public class IndexHash extends Index {
	
	/** Carte des valeurs indexées par cet objet Index - association clef valeur :
	 *  Key est l'identifiant (ex : nom -ou- nom + prénom + age par exemple),
	 *  Integer est l'index dans le fichier binaire issu des .CSV
	 *  Une seule correspondance est possible, pour que ça fonctionne, il faudrait donc remplacer Integer par ArrayList<Integer>
	 */
	protected HashMap<Key, ArrayList<Integer>> indexedValuesMap; // This is useless for an IndexTree, as of now (may change...)
	
	// Liste des opérateurs compatibles :
	protected final static Operator[] compatibleOperatorsList = {
		Operator.equals
	};
	
	public IndexHash(Column[] columns) {
		super(columns);
		this.indexedValuesMap = new HashMap<Key, ArrayList<Integer>>();
	}

}
