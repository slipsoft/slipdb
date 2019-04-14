package db.data.load;

import java.io.BufferedReader;
import java.io.IOException;


/**
 * CsvParser : CsvParser version 2
 *
 * But de ce parser :
 * - Evaluer le minimum et maximum de chaque colonne
 * - Sauvegarder chaque colonne dans un fichier séparé
 * PAS BESOIN : split les résultats pour avoir des colonnes de tailles raisonnables, et pouvoir indexer en multi-thread (exemple : 1_000_000 par colonne)
 * Il est possible de faire du multi-thread sur un seul fichier, c'est même plus simple et plus rapide !!
 *
 */


public class CsvParser extends Parser {

	protected final static String csvSeparator = ","; // the CSV separator used to delimit fields

	/** Lecture du CSV depuis un InputStream (peu importe la provenance)
	 *  Exécution mono-thread, en l'état, faire du multi-thread serait super super compliqué
	 *
	 *  Chaque colonne aura son propre fichier binaire.
	 */
	@Override
	public String processReader(BufferedReader input) throws IOException {
		return input.readLine();
	}

	@Override
	public String[] processEntry(String entryString) {
		// 1) Split the current line, get an array of String
		return entryString.split(csvSeparator); // csvSeparator declared in the unit
	}
}