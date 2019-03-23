package sj.network.tcpAndBuffers;

import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/** Principe du TCPClient :
 * Un thread est créé pour recevoir les messages.
 * Les messages sont toujours de la forme suivante : 4 octets pour la taille + données
 * Si les messages sont trop gros et fragmentés par le réseau, aucun problème, le message sera bien reçu entier par les TCPClient.
 * TCPClient retrourne un NetBuffer lors de l'appel à la fonction TCPClient.getNewMessage()
 * Le message intégralement reçu le plus ancien est alors récupéré du buffer contenant les messages "reçus par le thread mais en attente".
 */

public class TCPClient {
	
	//private Socket mySocket; géré par le TCPClientThread
	private TCPClientThread myThread = null; // Réception des nouveaux octets à lire
	private ArrayList<NetBuffer> readyBufferList = new ArrayList<NetBuffer>(); // Liste des buffers accessibles 
	private Object bufferListLock = new Object();
	
	// Utilisation d'un AtomicBoolean pour éviter de faire attendre via synchronized(...),
	// et ainsi ne pas risquer de bloquer (légerement) le thread principal de l'application lors
	// de la vérification des nouveaux messages : l'AtomicBoolean permet de ne pas attendre d'obtenir le lock via synchronized(...)
	private AtomicBoolean canAccessReadyBufferList = new AtomicBoolean(true); // Un booléen atomique donc thread-safe
	private String remoteIP = "";
	private int remotePort = 0;
	
	
	/** Constructeur par défaut, sans tenter de se connecter ni créer de thread de réception */
	public TCPClient() { }
	/** Constructeur permettrant de tenter de se connecter à l'adresse passée en argument
	 * @param hostIp    ip de l'hôte distant
	 * @param hostPort  port de l'hôte distant
	 */
	public TCPClient(String hostIp, int hostPort) {
		connect(hostIp, hostPort);
	}

	/** Résoudre l'IP distante du client
	 *  @return IP distante du client
	 */
	public String getRemoteIP() {
		return remoteIP;
	}
	
	/** Résoudre le port  distant du client
	 *  @return port distant du client
	 */
	public int getRemotePort() {
		return remotePort;
	}
	
	/** Constructeur à partir d'un socket fonctionnel
	 * -> surtout utilisé lors de l'acceptation d'un nouveau client par un TCPServer
	 * @param workingSocket socket connecté à un hôte distant
	 */
	public TCPClient(Socket workingSocket, boolean createdFromServer) {
		if (workingSocket != null) { // résolution de l'adresse
			InetAddress sockAddr = workingSocket.getInetAddress();
			remoteIP = sockAddr.getHostAddress();
			if (createdFromServer)
				remotePort = workingSocket.getLocalPort(); // probablement accepté d'un serveur, donc il s'agit ici du port local et non du port distant du client
			else
				remotePort = workingSocket.getPort(); // port distant (moins utilie)
			//System.out.println("TCPClient constructeur : workingSocket.getPort()="+workingSocket.getPort()+" workingSocket.getLocalPort()="+workingSocket.getLocalPort());
			if (remoteIP == null) remoteIP = "";
		}
		myThread = new TCPClientThread(this, workingSocket, false); // thread de réception
		new Thread(myThread).start();
	}
	
	
	/** Se connecter à une adresse (asynchrone, utiliser isConnected() pour vérifier si le client est connecté ou non)
	 * @param hostIp  IP de l'hôte distant
	 * @param hostPort  port à joindre
	 */
	public void connect(String hostIp, int hostPort) {
		if (myThread != null) return; // impossible de lancer 2+ fois connect()
		remoteIP = hostIp;
		remotePort = hostPort;
		myThread = new TCPClientThread(this, hostIp, hostPort, false);
		new Thread(myThread).start();
	}
	
	/** Vérifie si le client est connecté
	 * (toujours asynchrone, comme toutes les autres fonctions à l'exception du constructeur de TCPServer)
	 * Si le client n'est pas encore connecté : isConnected() == false et isStillActive() == true
	 * Si le client est déconnecté (échec de la résolution de l'adresse, hôte inacessible,
	 * perte de la connexion...) : isConnected() == false et isStillActive() == false
	 * 
	 * @return vrai si le client est connecté, false si pas connecté ou déconnecté.
	 */
	public boolean isConnected() {
		if (myThread == null) return false;
		if (!isStillActive()) return false;
		return myThread.isConnectedToHost();
	}
	/** Vérifie que le client est toujours actif (et non "mort" ou inactif)
	 * Si le client n'est pas encore connecté : isStillActive() == true
	 * Si le client est déconnecté (échec de la résolution de l'adresse,
	 * hôte inacessible, perte de connexion...) : isConnected() == false et isStillActive() == false
	 * 
	 * @return vrai si le client est toujours en activité (attente de connexion ou connecté), false si la connexion n'a pu être établie ou qu'elle a été perdue
	 */
	public boolean isStillActive() {
		if (myThread == null) return false;
		return myThread.isStillActive();
	}
	
	/** Envoyer un message : un NetBuffer contenant des informations, dans l'ordre (voir la documentation du package slip.network.buffers pour de plus amples informations)  <br>
	 * Attention : Si le client n'est pas connecté, le message ne sera pas envoyé. <br>
	 * Cette fonction peut être bloquante si le buffer d'envoi est saturé,
	 * ou renvoyer false (message non envoyé) si le client n'est pas encore connecté à l'hôte.
	 * Cette fonction sera reprise pour la rendre totalement asynchrone et faire une liste d'attente
	 * des messages à envoyer, dès que possible une fois la connexion établie à l'hôte distant.
	 * @param messageToSend  message à envoyer
	 * @return true si le message a correctement été envoyé, false sinon
	 */
	public boolean sendMessage(NetBuffer messageToSend) {
		if (myThread != null) {
			return myThread.addMessageToSendList(messageToSend);
		}
		return false;
	}
	
	/** Vrai si une erreur critique s'est produite.
	 * Un message non envoyé parce que le client n'est pas (encore/plus) connecté n'est pas une erreur critique.
	 * @return vrai si une erreur critique s'est produite dans le TCPClient.
	 */
	public boolean criticalErrorOccured() {
		if (myThread != null) {
			return myThread.criticalErrorOccured();
		}
		return false;
	}
	/** Récupérer le message d'erreur critique.
	 * @return
	 */
	public String getCriticalErrorMessage() {
		if (! criticalErrorOccured()) return ""; // aucun message d'erreur si aucune erreur critique n'est survenue
		if (myThread != null) {
			return myThread.getCriticalErrorMessage();
		}
		return "";
	}
	
	/** Ajout d'un nouveau buffer prêt à être lu
	 * @param newBuffer
	 */
	protected void addReadyBufferFromThread(NetBuffer newBuffer) {
		synchronized (bufferListLock) {
			canAccessReadyBufferList.set(false);
			readyBufferList.add(newBuffer);
			canAccessReadyBufferList.set(true);
		}
	}
	
	/** Récupérer un nouveau message (mis en file d'attente par le thread de réception du client, le TCPClientThread)
	 * @return null si aucun message n'est dispo en file d'attente, un NetBuffer sinon (message reçu et valide)
	 */
	public NetBuffer getNewMessage() {
		// Le booléen atomique canAccessReadyBufferList est là pour ne pas faire attendre le thread principal avec un synchronized
		// si la liste des buffers est en cours de modification par le thread de réception.
		if (canAccessReadyBufferList.get() == false)
			return null;
		
		synchronized(bufferListLock) {
			if (readyBufferList.size() == 0) return null;
			NetBuffer result = readyBufferList.get(0);
			//System.out.println("TCPClient.getNewMessage() : result.dataList.size() = " + result.dataList.size()); 
			readyBufferList.remove(0);
			return result;
		}
	}
	
	/** Comme getNewMessage() mais ne supprime pas le message de la liste des messages reçus
	 * @return null si aucun message n'est dispo, une COPIE du plus vieux NetBuffer en attente
	 */
	public NetBuffer peekNewMessage() {
		// Le booléen atomique canAccessReadyBufferList est là pour ne pas faire attendre le thread principal avec un synchronized
		// si la liste des buffers est en cours de modification par le thread de réception.
		if (canAccessReadyBufferList.get() == false)
			return null;
		
		synchronized(bufferListLock) {
			if (readyBufferList.size() == 0) return null;
			NetBuffer hasToCopy = readyBufferList.get(0);
			// Copie du buffer (pour ne rien modifier, au cas où le NetBuffer result serait modifié à l'extérieur de cette fonction (i.e. pas en lecture seule))
			byte[] buffToByteArray = hasToCopy.convertToByteArray();
			NetBuffer result = new NetBuffer(buffToByteArray);
			return result;
		}
	}
	
	/** Même résultat que (peekNewMessage() != null), en moins gourmand en ressources (cpu et mémoire)
	 * @return true si un nouveau message est en attente
	 */
	public boolean hasNewMessage() {
		// Le booléen atomique canAccessReadyBufferList est là pour ne pas faire attendre le thread principal avec un synchronized
		// si la liste des buffers est en cours de modification par le thread de réception.
		if (canAccessReadyBufferList.get() == false)
			return false;
		
		synchronized(bufferListLock) {
			return (readyBufferList.size() != 0);
		}
	}
	
	/** Arrêter ce TCPClient et son thread.
	 *  à noter : Le thread est automatiquement arrêté si la connexion à l'hôte est perdue.
	 */
	public void stop() {
		if (myThread != null)
			myThread.stop();
		myThread = null;
	}
	
	
}
