package sj.demoSimple;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dant.utils.Log;

import sj.network.tcpAndBuffers.NetBuffer;
import sj.network.tcpAndBuffers.TCPClient;
import sj.network.tcpAndBuffers.TCPServer;

public class SimpleTCPDemoServRunnable implements Runnable {
	

	public int serverPort = 443;
	public AtomicBoolean hasToStop = new AtomicBoolean(false);
	private boolean serverSuccessStart = false;
	private TCPServer tcpServ = new TCPServer(serverPort);
	private ArrayList<TCPClient> clientsList = new ArrayList<TCPClient>();
	
	private boolean loadFromDisk = true;
	private String sessionDatabaseName = "Database2_2015";//"Database2015";//"";//
	
	
	public boolean startServer() {
		class LocalLog {
			public void log(String message) {
				System.out.println("testTCP_withServer : " + message);
			}
		}
		//SimpleTCPDemoCreateTable demo = new SimpleTCPDemoCreateTable();
		Log.info("Serveur : CSV LOAD");
		try {
			SimpleTCPDemoCreateTable.setDatabaseName00(sessionDatabaseName);
			if (loadFromDisk) {
				SimpleTCPDemoCreateTable.loadSerialData03();
				SimpleTCPDemoCreateTable.parseCsvData02();
				SimpleTCPDemoCreateTable.saveSerialData03();
			} else {
				SimpleTCPDemoCreateTable.initTable01();
				SimpleTCPDemoCreateTable.parseCsvData02();
				SimpleTCPDemoCreateTable.saveSerialData03();
			}
			// Faire la recherche
			SimpleTCPDemoCreateTable.search04();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.info("Serveur : CSV LOAD OKAY !");
		
		LocalLog localLog = new LocalLog();
		localLog.log("Serveur : Initialisation...");
		if (tcpServ.isListening() == false) {
			Log.error("Serveur : Le serveur n'a pas pu ouvrir le port demandé.");
			tcpServ.close(); // <- super important !
			return false;
		}
		Log.info("Serveur : Démarré sur le port " + serverPort);
		serverSuccessStart = true;
		
		return serverSuccessStart;
		
	}
	
	
	@Override
	public void run() {
		if (startServer() == false) return;
		
		while (hasToStop.get() == false) {
			
			TCPClient newClient = tcpServ.accept();
			if (newClient != null) {
				clientsList.add(newClient);
			}
			
			for(int clientIndex = 0; clientIndex < clientsList.size(); clientIndex++) {
				TCPClient client = clientsList.get(clientIndex);
				if (client.isConnected() == false) {
					clientsList.remove(clientIndex);
					clientIndex--;
					continue;
				}
				
				NetBuffer message = client.getNewMessage();
				
				if (message != null) {
					handleClientMessage(message);
				}
				
			}
			
			try {
				Thread.sleep(1);
			} catch (Exception e) {
				Log.error(e);
				e.printStackTrace();
				return;
			}
		}
		
		tcpServ.close(); // <- super important !
		
		
	}
	
	

	private void handleClientMessage(NetBuffer message) {
		if (message != null) return;
		
		/**
		 *  Structure du message :
		 *  int commande type
		 *  
		 *  si commande type == 1
		 *  	
		 */
		
		
	}
	
	
	

}
