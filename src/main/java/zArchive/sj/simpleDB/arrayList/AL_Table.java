package zArchive.sj.simpleDB.arrayList;

import java.util.ArrayList;

/**
 * Une table contenant des lignes et des colonnes
 * Implémentée via une HashMap simple (avec key = index)
 * */
public class AL_Table {
	
	public ArrayList<AL_Column> columnList = new ArrayList<AL_Column>();
	Al_SOptimDataFromCSV optimDataType;
	
	int currentRowNumber = 0; // la taille que doit avoir chaque columnList (nombre d'éléments stockés dans la table actuelle)
	
	/** Trouver l'index d'une colonne à partir de son nom, dans la liste columnList
	 *  @param colName  nom de la colonne à rechercher
	 *  @return -1 si introuvable, un entier >= 0 si existe
	 */
	public int findColumnIndex(String colName) {
		for (int colIndex = 0; colIndex < columnList.size(); colIndex++) {
			AL_Column columnAtIndex = columnList.get(colIndex);
			if (columnAtIndex.name.equals(colName)) {
				return colIndex;
			}
		}
		return -1;
	}
	
	
	/*
	public boolean addColumn_byte(String colName, byte defaultFillValue) {
		int columnIndex = addColumn_base(colName);
		if (columnIndex == -1) return false;
		SimpleAL_Column column = columnList.get(columnIndex);
		column.storageType = SimpleAL_StorageType.isInteger;
		column.clearAndFillWith(defaultFillValue, currentRowNumber);
		return true;
	}*/
	
	public boolean addColumn_int(String colName, int defaultFillValue) {
		int columnIndex = addColumn_base(colName);
		if (columnIndex == -1) return false;
		AL_Column column = columnList.get(columnIndex);
		column.storageType = AL_SStorageDataType.isInteger;
		column.clearAndFillWith(defaultFillValue, currentRowNumber);
		return true;
	}

	public boolean addColumn_str(String colName, String defaultFillValue) {
		int columnIndex = addColumn_base(colName);
		if (columnIndex == -1) return false;
		AL_Column column = columnList.get(columnIndex);
		column.storageType = AL_SStorageDataType.isString;
		column.clearAndFillWith(defaultFillValue, currentRowNumber);
		return true;
	}

	/** Ajout d'une colonne
	 *  @param colName
	 *  @param defaultFillValue
	 *  @return
	 */
	public boolean addColumn(String colName, int defaultFillValue) {
		int columnIndex = addColumn_base(colName);
		if (columnIndex == -1) return false;
		AL_Column column = columnList.get(columnIndex);
		column.storageType = AL_SStorageDataType.isInteger;
		column.clearAndFillWith(defaultFillValue, currentRowNumber);
		return true;
	}
	
	/** Ajout d'une colonne
	 *  @param colName
	 *  @param defaultFillValue
	 *  @return
	 */
	public boolean addColumn(String colName, String defaultFillValue) {
		int columnIndex = addColumn_base(colName);
		if (columnIndex == -1) return false;
		AL_Column column = columnList.get(columnIndex);
		column.storageType = AL_SStorageDataType.isString;
		column.clearAndFillWith(defaultFillValue, currentRowNumber);
		return true;
	}
	
	/** Ajouter une colonne, en retourner l'index dans la liste columnList
	 *  @param colName
	 *  @return
	 */
	private int addColumn_base(String colName) {
		// Je vérifie qu'aucune colonne n'a le même nom
		if (findColumnIndex(colName) != -1) return -1;
		// Ajout de la colonne
		AL_Column newColumn = new AL_Column(colName, AL_SStorageDataType.isUnknown);
		columnList.add(newColumn);
		return columnList.size() - 1;
	}
	
	/** Ajouter les données passées en argument
	 *  @param valuesAsArray
	 *  @return
	 */
	public boolean addValues(ArrayList<AL_RowArgument> valuesAsArray) {
		// Vérification du nombre de colonnes
		if (valuesAsArray.size() != columnList.size()) return false;
		
		// Vérification du type de chacune des donnée
		for (int columnIndex = 0; columnIndex < columnList.size(); columnIndex++) {
			AL_Column columnAtIndex = columnList.get(columnIndex);
			AL_RowArgument argumentAtIndex = valuesAsArray.get(columnIndex);
			if (argumentAtIndex.argType != columnAtIndex.storageType) return false;
			//Class<? extends Object> classType = valuesAsArray.get(columnIndex).getClass();
		}
		
		// Ok, les vérifications sont passées, je peux ajouter la donnée

		for (int columnIndex = 0; columnIndex < columnList.size(); columnIndex++) {
			AL_Column columnAtIndex = columnList.get(columnIndex);
			Object addThisData = valuesAsArray.get(columnIndex).value;
			columnAtIndex.addData(addThisData);
		}
		
		currentRowNumber++;
		return true;
	}
	
	public String rowAsReadableString(int rowIndex) {
		if (rowIndex < 0) return "";
		if (rowIndex >= currentRowNumber) return "";
		
		String result = "";
		
		for (int columnIndex = 0; columnIndex < columnList.size(); columnIndex++) {
			AL_Column columnAtIndex = columnList.get(columnIndex);
			result += "[" + columnAtIndex.name + "] : ";
			Object objectAtThisIndex = columnAtIndex.dataList.get(rowIndex);
			if (columnAtIndex.storageType == AL_SStorageDataType.isInteger) {
				result += Integer.toString((Integer) objectAtThisIndex);
			}
			if (columnAtIndex.storageType == AL_SStorageDataType.isString) {
				result += (String) objectAtThisIndex;
			}
			result += "\n";
		}
		
		return result;
		
	}
	
	
	
}
















