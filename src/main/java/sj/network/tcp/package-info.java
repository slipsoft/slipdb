/**
 * 
 */
/**
 * Gestion TCP asynchrone.
 * TOUTES les fonctions proposées à l'utilisateur de TCPClient/TCPServer sont asynchrones à l'exception d'une seule :
 * Une seule exception : le constructeur de TCPServer qui tente d'ouvrir le port du serveur. (L'acceptation de nouveaux clients via TCPServer.accept est asynchrone)
 * 
 * L'immense avantage de ce système est de permettre à un client ou un serveur de fonctionner sur un seul
 * thread, sans se soucier de conflits d'accès entre threads.
 * Le système de réception des messages s'assure que les messages sont toujours entièrement reçus.
 * 
 * Tout est thread-safe, via l'utilisation de synchronized(Object lock) et d'AtomicBoolean
 * J'utilise aussi des AtomicBoolean avant d'effectuer des synchronized() sur appels externe, pour bloquer le moins
 * possible le thread principal de l'application qui va utiliser les TCPClient et TCPServer.
 * 
 * Principe du TCPClient :
 * Un thread est créé pour recevoir les messages.
 * Les messages sont toujours de la forme suivante : 4 octets pour la taille + données
 * Si les messages sont trop gros et fragmentés par le réseau, aucun problème, le message sera bien reçu entier par les TCPClient.
 * TCPClient retrourne un NetBuffer lors de l'appel à la fonction TCPClient.getNewMessage()
 * Le message intégralement reçu le plus ancien est alors récupéré du buffer contenant les messages "reçus par le thread mais en attente".
 * 
 * Principe du TCPServer :
 * Un thread est créé pour accepter de nouveaux clients.
 * S'il y a un nouveau client (tcp de java) accepté par un TCPServer, il est mis en attente dans le buffer du serveur.
 * Un TCPClient est retourné lors de l'appel à TCPServer.accept(), ou null si aucun client n'est en attente d'acceptation.
 * 
 * -> Amélioration future : (non encore réalisée par manque de temps)
 * TCP sécurisé via RSA (1024 ou 2048) + AES
 * Une poignée de main RSA sera réalisée pour l'échange de clefs AES, puis l'échange sera crypté via AES.
 * Il serait possible de toit faire via RSA, mais les fonctions symétriques comme AES sont beaucoup plus rapides
 * pour crypter les messages.
 * 
 * Informations complémentaires :
 * Le TCPClient.sendMessage peut être bloquant si le buffer d'envoi est saturé,
 * ou renvoyer false (message non envoyé) si le client n'est pas encore connecté à l'hôte.
 * Cette fonction sera reprise pour la rendre totalement asynchrone et faire une liste d'attente
 * des messages à envoyer, dès que possible une fois la connexion établie à l'hôte distant.
 * 
 * un bug ou une suggestion d'amélioration : sylvain.joube@gmail.com
 * 
 */
package sj.network.tcp;