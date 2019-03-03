package db.types;

/**
 * 
 * Sylvain : vous êtes sérieusement en train de remplacer
 * un type de base de Java (java.lang.String)
 * par votre propre classe String ? o-O
  --> (ce n'est que mon avis et j'admets que je peux parfaitement me tromper, hein)
 * 
 * Je fais mes tests dans mon coin, dans mes packages sj.db[...]
 * j'essaie de faire des trucs simples mais fonctionnels, faites vos interfaces et expériementations,
 * on fera des benchmarks ;)
 */
public class String extends Type {

	public String(int size) {
		this.size = size;
		// Sylvain : remplace donc java.lang.String str = new java.lang.String(); ><"
	}

}
