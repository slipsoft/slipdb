package zArchive.sj.tcpAndBuffers;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;


/** TCPClientThread
 * Pas bien commenté, par manque de temps !
 */

public class TCPClientThread implements Runnable {

	private static final int MAX_MESSAGE_SIZE = 1_000_000; // 1mo en taille de message maximale (au-delà, je considère qu'il y a erreur)
	private static final int MAX_RCV_BUFFER_SIZE = 10_000_000;
	//private static final int MAX_MESSAGE_LIST_TOTAL_SIZE = 10_000_000;
	private TCPClient myClient;
	private Socket mySocket;
	private final int connectToPort;
	private final String connectToHost;
	private byte[] dataNotYetInBuffer = new byte[0];
	//private int dataNotYetInBuffer_currentLength = 0;
	private AtomicBoolean stillActive = new AtomicBoolean(true);
	private AtomicBoolean criticalErrorOccured = new AtomicBoolean(false);
	private AtomicBoolean isConnected = new AtomicBoolean(false);
	private String criticalErrorMessage = "";
	private final boolean hasWorkingSocketOnCreate;
	
	// Partie assurant la sécurité
	private byte[] a1SecurityVersion = { 0, 0, 1 };
	private final boolean forceSecurity;
	private boolean securityEnabled = false;
	private int securityCurrentStep = 0; // 0 Négociation, 1 
	private AtomicBoolean criticalSecurityIssue = new AtomicBoolean(false); // Vrai si la sécurité n'a pas pu être garantie
	// ----
	
	// pas encore implémenté private ArrayList<NetBuffer> sendMesageList = new ArrayList<NetBuffer>(); // liste des messages à envoyer
	
	private Object sendMessage_lock = new Object();
	private Object dataNotYetInBuffer_lock = new Object(); // protection de dataNotYetInBuffer
	//private Object bufferAccessLock = new Object(); // protection de la liste des mesages reçus (NetBuffer)
	
	/** Constructeur utilisé dans une application cliente, pour se connecter à un serveur de manière non bloquante.
	 * Durant l'attente de connexion, isConnected est à false, idem pour la fonction isConnectedToHost()
	 * @param arg_myClient
	 * @param host
	 * @param port
	 */
	public TCPClientThread(TCPClient arg_myClient, String host, int port, boolean arg_forceSecurity) {
		myClient = arg_myClient;
		connectToHost = host;
		connectToPort = port;
		hasWorkingSocketOnCreate = false;
		forceSecurity = arg_forceSecurity;
		// isConnected.set(false); valeur par défaut, le client n'est pas encore connecté
	}
	/** Constructeur majoritairement utilisé par un application serveur, faisant suite à un ServerSocket.accept()
	 * @param arg_myClient 
	 * @param arg_workingSocket
	 */
	public TCPClientThread(TCPClient arg_myClient, Socket arg_workingSocket, boolean arg_forceSecurity) {
		myClient = arg_myClient;
		mySocket = arg_workingSocket;
		connectToHost = "";
		connectToPort = 0;
		hasWorkingSocketOnCreate = true;
		forceSecurity = arg_forceSecurity;
		isConnected.set(true); // je pars du principe que le socket est bien connecté
	}
	
	/** /!\ Un  synchronized (dataNotYetInBuffer_lock) est supposé réalisé à l'appel de cette fonction /!\
	 * 
	 */
	private void removeFirstBytesOfRCVBuffer(int deleteLen) {
		int newGlobalBufferSize = dataNotYetInBuffer.length - deleteLen;
		if (newGlobalBufferSize > 0) {
			byte[] newDataNotYetInBuffer = new byte[newGlobalBufferSize];
			System.arraycopy(dataNotYetInBuffer, deleteLen, newDataNotYetInBuffer, 0, newGlobalBufferSize);
			dataNotYetInBuffer = newDataNotYetInBuffer;
		} else
			dataNotYetInBuffer = new byte[0];
	}
	
	
	// Pas encore implémenté, mais bientôt !
	private boolean initialiseSecurity_receiveMessages() {
		// Première étape de gestion de la sécurité : réception de la version du protocole utilisé
		if (securityCurrentStep == 0) {
			if (dataNotYetInBuffer.length < 4) return false;
			byte receivedEnableSecurityAsByte = dataNotYetInBuffer[0];
			boolean receivedEnableSecurity = (receivedEnableSecurityAsByte == 1);
			securityEnabled = (receivedEnableSecurity || forceSecurity); // l'autre socket demande l'établissement d'une liaison sécurisée, ou je demande une connexion sécurisée
			// Réception de la version (toujours, même si la sécurité n'est pas activée)
			byte[] a1ReceivedSecurityVersion = new byte[3];
			System.arraycopy(dataNotYetInBuffer, 1, a1ReceivedSecurityVersion, 0, 3);
			removeFirstBytesOfRCVBuffer(4);
			// Vérification de la version
			if (securityEnabled) {
				if (Arrays.equals(a1SecurityVersion, a1ReceivedSecurityVersion)) {
					securityCurrentStep = 1;
				} else {
					// Erreur critique : la sécurité n'a pas pu être établie, les deux versions ne coïncident pas
					criticalSecurityIssue.set(true);
				}
			} else
				securityCurrentStep = 100; // passage de toutes les étapes de sécurité si la connexion n'est pas sécurisée
			
			return true;
		}
		
		
		return false;
	}
	
	private boolean checkForCompleteMessage() {
		if (criticalSecurityIssue.get()) return false;
		// regarde si un nouveau message est reçu
		// un message commence toujours par 4 octets indiquant sa longueur
		synchronized (dataNotYetInBuffer_lock) { // modification des données de dataNotYetInBuffer
			
			// sécurité non encore implémentée - initialiseSecurity_receiveMessages();
			// Première étape de gestion de la sécurité : réception de la version du protocole utilisé
			/* initialiseSecurity_receiveMessages(); */
			
			
			if ( dataNotYetInBuffer.length < 4 )
				return false; // il faut au moins 4 octets pour indiquer la taille du message à recevoir
			
			byte[] messageSizeByteArray = new byte[4];
			System.arraycopy(dataNotYetInBuffer, 0, messageSizeByteArray, 0, 4);
			int messageSize = NetBufferData.byteArrayToInt(messageSizeByteArray);
			if (messageSize >= MAX_MESSAGE_SIZE) { // erreur : message beaucoup trop gros
				stillActive.set(false);
				criticalErrorMessage = "Message de taille trop grande détécté : messageSize(" + messageSize + ") > MAX_MESSAGE_SIZE(" + MAX_MESSAGE_SIZE + ")";
				criticalErrorOccured.set(true);
				dataNotYetInBuffer = null; // superflu, mais pour bien comprendre que je supprime l'ancien buffer
				dataNotYetInBuffer = new byte[0];
				return false;
			}
			
			if (dataNotYetInBuffer.length < messageSize + 4)
				return false; // message non entièrement reçu
			// Je reçois le message
			NetBuffer messageBuffer = new NetBuffer(dataNotYetInBuffer, 0, messageSize + 4); // le NetBuffer a sa taille en début de buffer
			// Je supprime du tableau dataNotYetInBuffer le message que je viens de recevoir
			removeFirstBytesOfRCVBuffer(messageSize + 4);
			/*
			int newGlobalBufferSize = dataNotYetInBuffer.length - 4 - messageSize;
			byte[] newDataNotYetInBuffer = new byte[newGlobalBufferSize];
			System.arraycopy(dataNotYetInBuffer, messageSize + 4, newDataNotYetInBuffer, 0, newGlobalBufferSize);
			dataNotYetInBuffer = newDataNotYetInBuffer;*/
			
			//System.out.println("TCPClientThread.checkForCompleteMessage() : messageSize = " + messageSize);
			//System.out.println("TCPClientThread.checkForCompleteMessage() : firstInt = " + messageBuffer.readInteger());
			//messageBuffer.resetReadPosition();
			
			myClient.addReadyBufferFromThread(messageBuffer);
			return true;
		}
	}
	
	private synchronized void tryCloseSocket() {
		isConnected.set(false);
		stillActive.set(false);
		try {
			if (mySocket == null) return;
			if (mySocket.isClosed()) { mySocket = null; return; }
			mySocket.close();
			mySocket = null;
		} catch (Exception e) {
			
		}
	}
	
	@Override
	public void run() {
		// réception des octets du buffer, assemblage des buffers reçus
		
		if ( ! hasWorkingSocketOnCreate) {
			InetAddress hostAddress = null;
			try {
				hostAddress = InetAddress.getByName(connectToHost);
			} catch (Exception hostExcept) { // UnknownHostException
				stillActive.set(false);
				criticalErrorMessage = "Impossible de résoudre le nom de domaine. connectToHost(" + connectToHost + ")" + hostExcept.getMessage();
				criticalErrorOccured.set(true);
				tryCloseSocket();
				return;
			}
			
			try {
				mySocket = new Socket(hostAddress, connectToPort);
			} catch (Exception sockIOException) {
				stillActive.set(false);
				criticalErrorMessage = "Impossible d'ouvrir le socket à l'adresse connectToHost(" + connectToHost + ") " + "connectToPort(" + connectToPort + ")" + sockIOException.getMessage();
				criticalErrorOccured.set(true);
				tryCloseSocket();
				return;
			}
		} // else : mySocket déjà défini dans le constructeur
		
		if (mySocket == null) {
			stillActive.set(false);
			criticalErrorMessage = "Socket == null dès le début de TCPClientThread.run() alors qu'il ne devrait pas l'être.";
			criticalErrorOccured.set(true);
			tryCloseSocket();
			return;
		}
		
		
		if (mySocket.isConnected() == false) {
			stillActive.set(false);
			criticalErrorMessage = "Socket non connecté (mySocket.isConnected() == false) connectToHost(" + connectToHost + ") " + "connectToPort(" + connectToPort + ")";
			criticalErrorOccured.set(true);
			tryCloseSocket();
			return;
		}
		isConnected.set(true); // bien connecté
		//System.out
		
		/*
		System.out.println("TCPClientThread.run : au dodo !");
		try {
			Thread.sleep(4000);
		} catch (Exception e) {
			
		}
		System.out.println("TCPClientThread.run : dodo terminé !");*/
		
		
		//long iBoucle = 0;
		while (stillActive.get()) {
			
			if (myClient == null) {
				stillActive.set(false);
				criticalErrorMessage = "Le composant TCPClient n'est plus référencé sur le thread TCPClientThread.";
				criticalErrorOccured.set(true);
				tryCloseSocket();
				return;
			}
			
			if (criticalSecurityIssue.get()) {
				stillActive.set(false);
				criticalErrorMessage = "Erreur de sécurité critique (criticalSecurityIssue).";
				criticalErrorOccured.set(true);
				tryCloseSocket();
				return;
			}
			
			try {
				if (mySocket == null) {
					stillActive.set(false);
					criticalErrorMessage = "mySocket nis à null.";
					criticalErrorOccured.set(true);
					tryCloseSocket();
					return;
				}
				InputStream input = mySocket.getInputStream();
				if (input == null) {
					stillActive.set(false);
					criticalErrorMessage = "mySocket non null mais son getInputStream l'est.";
					criticalErrorOccured.set(true);
					tryCloseSocket();
					return;
				}
				byte[] newBytesArray = new byte[1024];
				int bytesReceived = input.read(newBytesArray);
				// réception des nouveaux octets
				if (bytesReceived > 0) {
					synchronized (dataNotYetInBuffer_lock) { // modification des données de dataNotYetInBuffer
						int totalBufferLen = dataNotYetInBuffer.length + bytesReceived;
						if (totalBufferLen >= MAX_RCV_BUFFER_SIZE) {
							
							stillActive.set(false);
							criticalErrorMessage = "totalBufferLen dépasse la taille maximale autorisée (trop d'octets en attente sans message reçu) totalBufferLen = " + totalBufferLen + " MAX_RCV_BUFFER_SIZE = " + MAX_RCV_BUFFER_SIZE;
							criticalErrorOccured.set(true);
							tryCloseSocket();
							return;
						}
						byte[] newBuffer = new byte[totalBufferLen];
						System.arraycopy(dataNotYetInBuffer, 0, newBuffer, 0, dataNotYetInBuffer.length);
						System.arraycopy(newBytesArray, 0, newBuffer, dataNotYetInBuffer.length, bytesReceived);
						dataNotYetInBuffer = newBuffer;
					}
					while (checkForCompleteMessage()); // Ajout de tous les messages reçus au TCPClient
				} else /*if (bytesReceived <= 0) */ {
					try { Thread.sleep(2); } catch (Exception e) { }
				}
				if (bytesReceived == -1) {
					stillActive.set(false);
					//System.out.println("TCPClientThread.run : bytesReceived =  " + bytesReceived);
					criticalErrorMessage = "déconnexion, bytesReceived == -1"; // fin de la connexion (suite à un Socket.close()) /!\ Socket.close() laisse Socket.isConnected() à true s'il y a déjà et connexion. D'où l'utilisation de -1 pour signifier "end of stream"
					criticalErrorOccured.set(true);
					tryCloseSocket();
					return;
				}
				//System.out.println("TCPClientThread.run : bytesReceived =  " + bytesReceived);
			} catch (Exception e) { // IOException
				stillActive.set(false);
				criticalErrorMessage = "Exception " + e.getMessage();
				criticalErrorOccured.set(true);
				tryCloseSocket();
				//e.printStackTrace();
			}
			//System.out.println("TCPClientThread.run : still active, boucle " + iBoucle + " mySocket.isClosed=" + mySocket.isClosed() + " mySocket.isConnected=" + mySocket.isConnected());
			//iBoucle++;
		}
		
		tryCloseSocket();
	}
	
	
	public boolean isStillActive() {
		return stillActive.get();
	}
	public boolean criticalErrorOccured() {
		return criticalErrorOccured.get();
	}
	public String getCriticalErrorMessage() {
		if (criticalErrorOccured.get() == false) return "";
		return criticalErrorMessage;
	}
	public boolean isConnectedToHost() {
		return isConnected.get();
	}
	
	
	public boolean addMessageToSendList(NetBuffer message) {
		if (message == null) return false;
		if (mySocket == null) return false;
		synchronized (sendMessage_lock) {
			try {
				//System.out.println("TCPClientThread.addMessageToSendList()");
				byte[] buffToSend = message.convertToByteArray();
				
				OutputStream mySocketOutStream = mySocket.getOutputStream();
				mySocketOutStream.write(buffToSend);
				//System.out.println("TCPClientThread.addMessageToSendList() : OK ! Message bien envoyé.");
			} catch (Exception e) { // IOException
				return false; // si le socket n'est pas encore connecté
				//e.printStackTrace();
			}
		}
		return true;
	}
	
	public synchronized void stop() { // TODO faire un meilleur synchronized
		synchronized (dataNotYetInBuffer_lock) {
			if (mySocket != null) {
				try {
					mySocket.close();
				} catch (Exception e) {	} // réessayer de fermer le socket sur exception, fait plus tard quand j'aurai le temps !
				mySocket = null;
			}
		}
	}
	public synchronized void close() { // TODO faire un meilleur synchronized
		stop();
	}
	
}
/* inutile, augmenter la taille du buffer d'envoi
class TCPClientThread_sendThread implements Runnable {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
}*/
