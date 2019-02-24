package sj.simpleDB.arrayList;

/**
 * Argument du AL_Finder
 *
 */
public class AL_FinderArgument {
	/**
	 * Critères de recherche :
	 * Nom Table + Comparaison + Valeur
	 * 
	 * Exemple :
	 * "Nom chauffeur" + "EQUALS" + "VALUE"
	 */
	
	public final String nomColonne;
	public final AL_RowArgument valeur;
	public final AL_FinderArgumentOperation comparaison;
	public AL_Column column = null; // trouvée via AL_Finder
	public boolean validFilter = false;
	
	public AL_FinderArgument(String arg_nomColonne, AL_FinderArgumentOperation arg_comparaison, AL_RowArgument arg_valeur) {
		nomColonne = arg_nomColonne;
		valeur = arg_valeur;
		comparaison = arg_comparaison;
	}
	
	
}
