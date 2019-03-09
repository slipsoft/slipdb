package zArchive.sj.simpleDB.arrayList;

import java.util.ArrayList;

/** Permet de créer un objet et de l'ajouter aux colonnes
 * @author admin
 *
 */
public class AL_LineMaker {
	
	private ArrayList<AL_RowArgument> rowValueList = new ArrayList<AL_RowArgument>();
	
	public void addRowValue_int(int value) {
		AL_RowArgument newArgument = new AL_RowArgument(AL_StorageType.isInteger, new Integer(value));
		rowValueList.add(newArgument);
	}
	
	public void addRowValue_str(String value) {
		AL_RowArgument newArgument = new AL_RowArgument(AL_StorageType.isString, new String(value));
		rowValueList.add(newArgument);
	}
	
	public ArrayList<AL_RowArgument> getArgumentList() {
		return rowValueList;
	}
	
}
