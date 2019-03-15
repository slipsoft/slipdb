package db.structure.indexTree;

/***
 * 
 * ~ Code de Sylvain, idée de Nicolas ~
 * 
 * Bench de IndexTreeDic vs IndexTree pour mesurer les performances des deux méthodes
 * 
 * Indexer via un TreeMap, et une recherche par dichotomie sur le disque.
 * Le TreeMap contenant les valeurs donnera une collection classée par ordre croissant de ses clefs,
 * ce sera donc facile de l'écrire sur le disque.
 * 
 * 1) Ecrire chaque IntegerArrayList sur le disque, et retenir le binIndex
 * 2) Ecrire la table d'index, retenir la position de la table, écrire : nombre d'IntegerArrayList (=nb valeurs différentes),
 *    puis pour chaque IntegerArrayList : valeur (clef) + binIndex (4 ou 8 octets)
 *    
 * -> Cette méthode est significativement plus simple que les IndexTreeCeption, reste à en évaluer les performances sur les données des Taxis de New York.
 * 
 */

public class IndexTreeDicV1 {

}
