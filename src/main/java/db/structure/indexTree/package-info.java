/**
 * 
 */
/**
 * J'écris cette partie de Français, parce que si je dois traduire en plus, c'est galère xD
 * 
 * Indexer des colonnes via un IndexTree (TreeMap),
 * Je garde ici un historique des versions pour pouvoir faire des tests et comparatifs de performance.
 * v1 : Indexer une colonne de type Number (Integer, Float, Byte ...) à partir du disque et retrouver
 *      les valeurs rapidement, tous les index sont stockés en mémoire, il n'y a pas de regroupement de valeurs
 *      proches : pour chaque valeur, il y a une ArrayList stockant les binIndex correspondants.
 *      
 * v2 : Stocker le plus de données possible sur le disque, tout en pouvant les retrouver aussi facilement que possible.
 *      Création d'arbres imbriqués, pour avoir le plus de données possibles écrites sur le disque, mais n'avoir à charger que très
 *      peu de données pour retrouver les bonnes listes.
 *
 */
package db.structure.indexTree;