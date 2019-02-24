package sj.simpleDB.arrayList;

import java.util.ArrayList;

/**
 * Trouver les index des données recherchées
 *
 */
public class AL_Finder {
	
	/**
	 * Critères de recherche :
	 * Nom Table + Comparaison + Valeur
	 * 
	 * Exemple :
	 * "Nom chauffeur" + "EQUALS" + "VALUE"
	 */
	
	private ArrayList<AL_FinderArgument> filterList = new ArrayList<AL_FinderArgument>();

	public boolean addFilter(String columnName, AL_FinderArgumentOperation operation, Integer toValue) {
		AL_RowArgument argument = new AL_RowArgument(AL_StorageType.isInteger, toValue);
		return addFilter(columnName, operation, argument);
	}

	public boolean addFilter(String columnName, AL_FinderArgumentOperation operation, String toValue) {
		AL_RowArgument argument = new AL_RowArgument(AL_StorageType.isString, toValue);
		return addFilter(columnName, operation, argument);
	}
	
	public boolean addFilter(String columnName, AL_FinderArgumentOperation operation, AL_RowArgument toValue) {
		AL_FinderArgument argument = new AL_FinderArgument(columnName, operation, toValue);
		return filterList.add(argument);
	}
	
	/** Retourne la liste des index correspondant aux filtres passés via addFilter
	 * 
	 * @param inTable
	 * @param maxResultCount
	 * @return
	 */
	public ArrayList<Integer> findMatchingIndexList(AL_Table inTable, int maxResultCount) {
		ArrayList<Integer> indexList = new ArrayList<Integer>();
		if (filterList.size() == 0) return indexList;
		
		// 1ère étape, ultra-pas-optimisée, mais fonctionnelle :
		
		// Liste des colonnes à comparer : je trouve les colonnes pour les filters
		//ArrayList<AL_Column> columnList = new ArrayList<AL_Column>();
		for (int filterIndex = 0; filterIndex < filterList.size(); filterIndex++) {
			AL_FinderArgument currentFilter = filterList.get(filterIndex);
			String nomColonne = currentFilter.nomColonne;
			int colIndex = inTable.findColumnIndex(nomColonne);
			if (colIndex == -1) {
				System.err.println("ERREUR AL_Finder.findMatchingIndexList : colIndex == -1");
				return indexList;
			}
			AL_Column currentColumn = inTable.columnList.get(colIndex);
			if (currentFilter.valeur.argType != currentColumn.storageType) {
				System.err.println("ERREUR AL_Finder.findMatchingIndexList : currentFilter.valeur.argType != currentColumn.storageType");
				return indexList;
			}
			
			//columnList.add(currentColumn);
			currentFilter.column = currentColumn;
		}

		//System.out.println("AL_Finder.findMatchingIndexList : filterList.size() = " + filterList.size());
		//System.out.println("AL_Finder.findMatchingIndexList : ");
		// -> Pour tout les éléments (lignes) de la table, je check les filtres
		
		// Pour tous les éléments...
		for (int rowIndex = 0; rowIndex < inTable.currentRowNumber; rowIndex++) {
			
			//System.out.println("  AL_Finder.findMatchingIndexList : rowIndex = " + rowIndex);
			boolean keepThisIndex = true;
			// Pour tous les filtres...
			for (int filterIndex = 0; filterIndex < filterList.size(); filterIndex++) {
				AL_FinderArgument currentFilter = filterList.get(filterIndex);
				//System.out.println("    AL_Finder.findMatchingIndexList : filterIndex = " + filterIndex);
				// J'évalue la condition sur la colonne
				//AL_RowArgument compareToFilterValue = currentFilter.valeur;
				Object compareToFilterValue = currentFilter.valeur.value;
				//AL_Column currentColumn = currentFilter.column;
				
				Object currentRowValue = currentFilter.column.dataList.get(rowIndex);
				//System.out.println("    AL_Finder.findMatchingIndexList : compareToFilterValue = " + compareToFilterValue + "  --  " + currentRowValue + " = currentRowValue");
				
				if (compareToFilterValue.equals(currentRowValue) == false) {
					keepThisIndex = false;
					break;
				}
				
				//System.out.println("    AL_Finder.findMatchingIndexList : compare OK !");
				
				
				/*
				if (currentFilter.comparaison == AL_FinderArgumentOperation.equals) {
					
					
					
					
				}*/
				
				
			}
			
			if (keepThisIndex) {
				indexList.add(rowIndex);
				if (indexList.size() >= maxResultCount)
					return indexList;
			}
			
		}
		
		// Check du premier filtre, si ok, passage au deuxième filtre etc.
		//AL_Column currentColumn = inTable.findColumnIndex(colName);
		
		
		
		
		// Je fais la liste des colonnes que je vais avoir à utiliser
		// 
		
		
		return indexList;
	}
	
	//public ArrayList<Integer> getValue
	
	
}












