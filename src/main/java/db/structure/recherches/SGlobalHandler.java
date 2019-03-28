package db.structure.recherches;

import java.util.ArrayList;

import db.structure.Table;

/**
 *  Classe pour la recherche d'information dans les IndexTree
 *  Stocke les IndexTreeDic connus, fais des recherches et retourne les résultats.
 *  
 *  Fonctions :
 *  - Créer de nouveaux index à partir de colonnes déjà parsées
 *  - Charger des index du disque
 *  - 
 *
 *
 */
public class SGlobalHandler {
	
	
	protected static ArrayList<TableHandler> tableHandlersList = new ArrayList<TableHandler>();
	
	public static TableHandler initializeTable(String tableName) {
		if (tableName == null) return null;
		
		for (TableHandler table : tableHandlersList) {
			if (table.getTableName().equals(tableName)) {
				return null;
			}
		}
		
		TableHandler newTableHandler = new TableHandler(tableName);
		
		return newTableHandler;
	}
	
	
}
