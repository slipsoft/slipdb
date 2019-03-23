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
	
	
	protected static ArrayList<STableHandler> tableHandlersList = new ArrayList<STableHandler>();
	
	public static STableHandler initializeTable(String tableName) {
		if (tableName == null) return null;
		
		for (STableHandler table : tableHandlersList) {
			if (table.getTableName().equals(tableName)) {
				return null;
			}
		}
		
		STableHandler newTableHandler = new STableHandler(tableName);
		
		return newTableHandler;
	}
	
	
}
