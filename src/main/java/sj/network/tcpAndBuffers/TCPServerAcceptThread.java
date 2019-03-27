package sj.network.tcpAndBuffers;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Accepte de nouvelles connexions au serveur
 * @author admin
 *
 */
public class TCPServerAcceptThread implements Runnable {
	
	private TCPServer myServer;
	AtomicBoolean stillActive = new AtomicBoolean(true); // false lorsque le thread s'arrête de lui-même
	//private boolean allowAcceptNewClients = true;
	
	public TCPServerAcceptThread(TCPServer arg_myServer) {
		myServer = arg_myServer;
	}
	
	public void close() {
		// Fermeture de toutes les connexions client
		// Arrêt du thread
		stillActive.set(false);
		ServerSocket servSock = myServer.getServSock();
		if (servSock != null) {
			try {
				servSock.close();
			} catch (IOException e) {
				
			}
		}
		
	}
	public void stop() {
		close();
	}
	
	public boolean isStillActive() {
		return stillActive.get();
	}
	
	@Override
	public void run() {
		
		while (stillActive.get()) {
			
			if (myServer == null || myServer.getServSock() == null) {
				stillActive.set(false);
				// servSock.isClosed()
				break;
			}
			ServerSocket servSock = myServer.getServSock();
			try {
				Socket clientSock = servSock.accept();
				TCPClient newClient = new TCPClient(clientSock, true);
				myServer.addClientFromAcceptThread(newClient);
			} catch (Exception e) {
				stillActive.set(false);
				//e.printStackTrace();
			}
		}
		//System.out.println("TCPAccept STOPPED");
	}
}
