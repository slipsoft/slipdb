/**
 * 
 */
/**
 * 
 * Index full mémoire (comme demandé par Olivier)
 * utilisant les types primitifs (int, double, byte...) pour le stockage de la donnée,
 * et le type primitif int pour le stockage de la position dans le fichier.
 * 
 * Problème : le moteur de stockage actuel de la donnée n'a pas une seule position (int) mais plusieurs (nodeID, fileID, lineIndex)
 * 
 * Le NodeID n'est pas absolument nécessaire, mais le fileID l'est, si on veut parser en multi-thread.
 * Idées :
 * -> Soit faire un autre système de stockage propre à IndexMemDic non multi-thread avec un seul int par positon de fichier
 * -> Soit modifier le système de fichier actuel pour supprimer nodeID qui n'est pas nécessaire
 *   -> La position de chaque fichier prendra alors plus de place (6 octets contre 4 avec un int simple)
 *      mais ça autorise le multi-thread au moment du parsing (INPORTANT !!) et ça reste compatible avec ce que j'ai déjà fait.
 * 
 * Fonctionnement : 
 * 1) Parser les CSV, stocker la donnée sur le disque et garder en mémoire la donnée dont on a besoin :
 *    Un tableau (array) par type primitif à garder en mémoire, objet MemColumn ayant le type de la donnée stockée,
 *    l'index de la colonne (0 pour VendorID etc.) et l'array des données.
 * 2) Indexer les colonnes à indexer (une colonne à indexer DOIT être en mémoire, mais une colonne en mémoire peut ne pas être indexée)
 *    a) Mettre toutes les valeurs de la colonne dans un objet avec la lineIndex et la valeur
 *    b) Classer la liste en fonction des valeurs
 *    c) Ecrire l'array des lineIndex, donc ici classée par valeur associée croissante
 * 3) Chaque recherche sur un index (une colonne indexée) sera faite par dichotomie, en mémoire :
 *    A chaque lineIndex, il est facile de retrouver l valeur associée car elle est en mémoire.
 *    
 *    -> Justification de la redondance de l'information pour l'IndexTreeDic : ne pas avoir un aute fichier d'ouvert,
 *       ne pas faire de seek supplémentaire, améliore grandement les performances et ne prend pas beaucoup plus d'espace disque.
 *
 */
package index.memDic;