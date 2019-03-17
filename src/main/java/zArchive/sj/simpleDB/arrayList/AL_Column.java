package zArchive.sj.simpleDB.arrayList;

import java.util.ArrayList;

/**
 * 2019-02-24
 * Une colonne simple, stockée dans une ArrayList
 *
 */
public class AL_Column {
	public String name; // nom de la colonne
	
	// Type de donnée stockée
	public AL_SStorageDataType storageType = AL_SStorageDataType.isUnknown;
	
	public ArrayList<Object> dataList = new ArrayList<Object>();
	
	public Al_SOptimDataFromCSV optimDataType;
	
	//public ArrayList<Integer> intDataList = new ArrayList<Integer>(); // utilisé s'il s'agit d'une recherche sur des strings
	//public ArrayList<String>  strDataList = new ArrayList<String>();  // utilisé s'il s'agit d'une recherche sur des entiers
	
	public AL_Column(String arg_name, AL_SStorageDataType dataType) {
		name = arg_name;
		storageType = dataType;
	}
	
	
	/** Rechercher tous les index associés à une valeur
	 * @param searchValue
	 * @param maxResultSize
	 * @return une ArrayList<Integer> avec les index correspondants, de taille vide si aucune valeur n'est trouvée
	 */
	public ArrayList<Integer> findAllIndexesOf(Object searchValue, int maxResultSize) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		if (maxResultSize <= 0) return result; // ArrayList de taille 0
		// recherche sur toute la liste
		int dataListSize = dataList.size();
		for (int index = 0; index < dataListSize; index++) {
			Object valueAtIndex = dataList.get(index);
			if (valueAtIndex.equals(searchValue)) {
				result.add(index);
				if (result.size() >= maxResultSize)
					break;
			}
		}
		return result;
	}
	
	/** Ajouter une valeur à une position
	 *  @param data
	 *  @param rowIndex
	 */
	public void insertDataAtPosition(Object data, int rowIndex) {
		// Rectification de rowIndex, si besoin
		if (rowIndex <= 0 || rowIndex > dataList.size()) rowIndex = dataList.size();
		dataList.add(rowIndex, data);
	}
	
	/** Ajout d'une valeur à la fin
	 *  @param data
	 */
	public void addData(Object data) {
		dataList.add(data);
	}
	
	/** Remplir la table
	 *  @param fillValue
	 *  @param countSize
	 *  @return
	 */
	public boolean clearAndFillWith(int fillValue, int countSize) {
		if (storageType != AL_SStorageDataType.isInteger) return false;
		dataList.clear();
		dataList.ensureCapacity(countSize);
		for (int count = 1; count <= countSize; count++)
			dataList.add(new Integer(fillValue));
		return true;
	}

	/** Remplir la table
	 *  @param fillValue
	 *  @param countSize
	 *  @return
	 */
	public boolean clearAndFillWith(String fillValue, int countSize) {
		if (storageType != AL_SStorageDataType.isInteger) return false;
		dataList.clear();
		dataList.ensureCapacity(countSize);
		for (int count = 1; count <= countSize; count++)
			dataList.add(new String(fillValue));
		return true;
	}
	
	
	/*
	HashMap<Integer, StoredDataType> rowMap = new HashMap<Integer, StoredDataType>(); // contenu de la colonne
	
	/**
	 * Trouver l'index de la ligne correspondant à la donnée
	 * @param data
	 * @return
	 * /
	public int findKey(StoredDataType data) {
		rowMap.con
		data.equals(obj)
		return -1;
	}
	*/
	
	
}
