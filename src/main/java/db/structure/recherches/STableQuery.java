package db.structure.recherches;

import java.util.ArrayList;

/**
 *  Faire une requête sur une table
 *  
 *  Retourne les résultats sous forme de colonnes ayant un nom, et des objets
 *  
 */
public class STableQuery {
	
	// Résultats renvoyés (une colonne par SELECT)
	public ArrayList<STableQueryResultColumn> resultColumnList = new ArrayList<STableQueryResultColumn>();
	
	public ArrayList<STableQueryFilter> filtersList = new ArrayList<STableQueryFilter>();
	
	
	/*
	Filtres possibles :
	Trouve les entrées (binIndex au début) qui correspondent à tous les filtres passés en entrée
	
	V0 :
	- Exécute les filters dans l'ordre : premier filtre, recherche dans l'index,
	- filtres suivants : lit directement du disque (ou, pour chaque résultat, vérifie qu'il match bien dans le fichier de sauvegarde)
	- Pour chaque valeur associé à une clef (binIndex), stocker en byte le nombre de filtres passés.
	-> Seuls les résultats ayant le nombre de filtres max seront retournés.
	
	V1 :
	- Exécute les filtres dans l'ordre, stocke les résultats, les compare (TreeMap avec en clef les binIndex)
	
	
	Plus tard :
	- Des filtres (valeur min, valeur max, sur un TreeMap)
	- De grouper les résultats via une fonction (COUNT, AVG, MIN ...)
	
	-> Trouver tous les résultats entre valMin et valMax : OK
	-> Regrouper les résultats par colonne, et faire AVG - MIN - COUNT : ne pas stocker tous les résultats, pas besoin de tous !
	
	*/
	
	public void addFilter(int columnIndex, Object minValue, Object maxValue, boolean inclusive) {
		STableQueryFilter newFilter = new STableQueryFilter(columnIndex, minValue, maxValue, inclusive);
		filtersList.add(newFilter);
	}
	
	
	
	
	
}
