package zArchive.sj.tcpAndBuffers;
/**
 * Test du coverage pour mieux comprendre "comment ça fonctionne ce truc"
 */

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class NetBufferTest {
	
	
	@BeforeAll
	public static void initEverything() {
		
	}
	
	@AfterAll
	public static void tearEverythingDown() {
		
	}
	
	
	
	
	@Test
	public void testNetBuffer() {
		NetBuffer buff = new NetBuffer();
		buff.writeBool(true);
		buff.writeByte((byte) 2);
		buff.writeString("3");
		buff.writeInt(4);
		buff.writeLong(5);
		buff.writeDouble(6.6);
		buff.writeLong((long) 7.7);
		buff.writeInt64(8);
		byte[] writtenByteArray = new byte[] {9, 10, 11};
		buff.writeByteArray(writtenByteArray);
		
		
		assertEquals(true, buff.equals(buff), "Problème dans la fonction NetBuffer.equals().");
		assertEquals(true, buff.isEqualTo(buff), "Problème dans la fonction NetBuffer.isEqualTo(...)");
		
		byte[] asByteArray = buff.convertToByteArray();
		
		NetBuffer checkBuffer = NetBuffer.getBufferFromByteArray(asByteArray);
		
		assertEquals(true, buff.equals(checkBuffer), "Problème dans la fonction NetBuffer.getBufferFromByteArray(...)");
		
		// Test vite fait (les fonctions sont très simples, les bugs sont rares et visibles)
		
		assertEquals(false, checkBuffer.currentData_isDouble());
		assertEquals(false, checkBuffer.currentData_isString());
		assertEquals(false, checkBuffer.currentData_isByteArray());
		assertEquals(false, checkBuffer.currentData_isByte());
		assertEquals(false, checkBuffer.currentData_isLong());
		assertEquals(false, checkBuffer.currentData_isInt());
		assertEquals(false, checkBuffer.currentData_isStr());
		assertEquals(true, checkBuffer.currentData_isBool());
		assertEquals(true, checkBuffer.currentData_isBoolean());
		checkBuffer.readBool();
		
		assertEquals(false, checkBuffer.currentData_isDouble());
		assertEquals(false, checkBuffer.currentData_isString());
		assertEquals(false, checkBuffer.currentData_isByteArray());
		assertEquals(true, checkBuffer.currentData_isByte());
		assertEquals(false, checkBuffer.currentData_isLong());
		assertEquals(false, checkBuffer.currentData_isInt());
		assertEquals(false, checkBuffer.currentData_isStr());
		assertEquals(false, checkBuffer.currentData_isBool());
		assertEquals(false, checkBuffer.currentData_isBoolean());
		checkBuffer.readByte();
		
		assertEquals(false, checkBuffer.currentData_isDouble());
		assertEquals(true, checkBuffer.currentData_isString());
		assertEquals(false, checkBuffer.currentData_isByteArray());
		assertEquals(false, checkBuffer.currentData_isByte());
		assertEquals(false, checkBuffer.currentData_isLong());
		assertEquals(false, checkBuffer.currentData_isInt());
		assertEquals(true, checkBuffer.currentData_isStr());
		assertEquals(false, checkBuffer.currentData_isBool());
		assertEquals(false, checkBuffer.currentData_isBoolean());
		checkBuffer.readString();
		
		assertEquals(false, checkBuffer.currentData_isDouble());
		assertEquals(false, checkBuffer.currentData_isString());
		assertEquals(false, checkBuffer.currentData_isByteArray());
		assertEquals(false, checkBuffer.currentData_isByte());
		assertEquals(false, checkBuffer.currentData_isLong());
		assertEquals(true, checkBuffer.currentData_isInt());
		assertEquals(false, checkBuffer.currentData_isStr());
		assertEquals(false, checkBuffer.currentData_isBool());
		assertEquals(false, checkBuffer.currentData_isBoolean());
		checkBuffer.readInt();
		
		assertEquals(false, checkBuffer.currentData_isDouble());
		assertEquals(false, checkBuffer.currentData_isString());
		assertEquals(false, checkBuffer.currentData_isByteArray());
		assertEquals(false, checkBuffer.currentData_isByte());
		assertEquals(true, checkBuffer.currentData_isLong());
		assertEquals(false, checkBuffer.currentData_isInt());
		assertEquals(false, checkBuffer.currentData_isStr());
		assertEquals(false, checkBuffer.currentData_isBool());
		assertEquals(false, checkBuffer.currentData_isBoolean());
		checkBuffer.readLong();
		
		assertEquals(true, checkBuffer.currentData_isDouble());
		assertEquals(false, checkBuffer.currentData_isString());
		assertEquals(false, checkBuffer.currentData_isByteArray());
		assertEquals(false, checkBuffer.currentData_isByte());
		assertEquals(false, checkBuffer.currentData_isLong());
		assertEquals(false, checkBuffer.currentData_isInt());
		assertEquals(false, checkBuffer.currentData_isStr());
		assertEquals(false, checkBuffer.currentData_isBool());
		assertEquals(false, checkBuffer.currentData_isBoolean());
		checkBuffer.readDouble();

		assertEquals(false, checkBuffer.currentData_isDouble());
		assertEquals(false, checkBuffer.currentData_isString());
		assertEquals(false, checkBuffer.currentData_isByteArray());
		assertEquals(false, checkBuffer.currentData_isByte());
		assertEquals(true, checkBuffer.currentData_isLong());
		assertEquals(false, checkBuffer.currentData_isInt());
		assertEquals(false, checkBuffer.currentData_isStr());
		assertEquals(false, checkBuffer.currentData_isBool());
		assertEquals(false, checkBuffer.currentData_isBoolean());
		checkBuffer.readLong();

		assertEquals(false, checkBuffer.currentData_isDouble());
		assertEquals(false, checkBuffer.currentData_isString());
		assertEquals(false, checkBuffer.currentData_isByteArray());
		assertEquals(false, checkBuffer.currentData_isByte());
		assertEquals(true, checkBuffer.currentData_isLong());
		assertEquals(false, checkBuffer.currentData_isInt());
		assertEquals(false, checkBuffer.currentData_isStr());
		assertEquals(false, checkBuffer.currentData_isBool());
		assertEquals(false, checkBuffer.currentData_isBoolean());
		checkBuffer.readInt64();

		assertEquals(false, checkBuffer.currentData_isDouble());
		assertEquals(false, checkBuffer.currentData_isString());
		assertEquals(true, checkBuffer.currentData_isByteArray());
		assertEquals(false, checkBuffer.currentData_isByte());
		assertEquals(false, checkBuffer.currentData_isLong());
		assertEquals(false, checkBuffer.currentData_isInt());
		assertEquals(false, checkBuffer.currentData_isStr());
		assertEquals(false, checkBuffer.currentData_isBool());
		assertEquals(false, checkBuffer.currentData_isBoolean());
		
		byte[] receivedByteArray = checkBuffer.readByteArray();
		
		assertEquals(true, Arrays.equals(writtenByteArray, receivedByteArray));
		
	}
	
	
	@Test
	public void testTcp() {
		
		int choosenPort = 28756;
		TCPServer tcpServ = new TCPServer(choosenPort);
		try {
			testTCP_withServer(tcpServ, choosenPort);
		} catch (Exception e) {
			e.printStackTrace();
		}
		tcpServ.close(); // <- super important !
		
	}
	
	
	public boolean testTCP_withServer(TCPServer tcpServ, int choosenPort) throws InterruptedException {
		
		class LocalLog {
			public void log(String message) {
				System.out.println("testTCP_withServer : " + message);
			}
		}
		
		LocalLog localLog = new LocalLog();
		localLog.log("Serveur : Initialisation...");
		//localLog.log("");
		
		assertEquals(true, tcpServ.isListening(), "Serveur : Le serveur n'a pas pu ouvrir le port demandé.");
		assertEquals(null, tcpServ.accept(), "Serveur : Aucun client n'était attendu.");
		
		localLog.log("Client : Création & connexion du client");
		
		TCPClient tcpClient = new TCPClient();
		tcpClient.connect("localhost", choosenPort);
		
		int maxTryNb = 100;
		int sleepTimeMsTry = 10;
		boolean connectionSuccess = false;
		for (int iTry = 0; iTry < maxTryNb; iTry++) {
			if (tcpClient.isConnected()) {
				connectionSuccess = true;
				break;
			}
			Thread.sleep(sleepTimeMsTry);
		}
		
		localLog.log("Client : Client connecté !");
		
		assertEquals(true, connectionSuccess, "Client : Echec de la connexion au serveur local.");
		
		// Accepter le client
		TCPClient accepedClient = null;
		for (int iTry = 0; iTry < maxTryNb; iTry++) {
			accepedClient = tcpServ.accept();
			if (accepedClient != null) {
				break;
			}
			Thread.sleep(sleepTimeMsTry);
		}
		
		localLog.log("Serveur : Client accepté par le serveur !");
		
		assertEquals(true, accepedClient != null, "Serveur : Le serveur n'a pas accepté le client, alors que le client est bien connecté.");
		
		
		String checkString = "Je suis une chaîne de caractères avec des c@rac1ër€s spéciaux #\"ç^^$^¤*µ &&& ! ";
		NetBuffer sentMessage = new NetBuffer();
		sentMessage.writeBool(true);
		sentMessage.writeString(checkString);

		localLog.log("Client : Envoi d'un message au serveur");
		
		boolean bienEnvoye = tcpClient.sendMessage(sentMessage);
		assertEquals(true, bienEnvoye, "Client : Message du TCPClient non envoyé.");

		NetBuffer receivedMessage = null;
		for (int iTry = 0; iTry < maxTryNb; iTry++) {
			receivedMessage = accepedClient.getNewMessage();
			if (receivedMessage != null) {
				break;
			}
			Thread.sleep(sleepTimeMsTry);
		}
		
		localLog.log("Serveur : Réception du message par le serveur");
		
		assertEquals(true, receivedMessage != null, "Serveur : message du client non reçu.");
		
		assertEquals(true, receivedMessage.equals(sentMessage), "Serveur : le message envoyé du client ne correspond pas au message reçu du serveur.");
		
		
		tcpClient.stop();
		
		boolean stopSuccess = false;
		for (int iTry = 0; iTry < maxTryNb; iTry++) {
			if (tcpClient.isStillActive() == false) {
				stopSuccess = true;
				break;
			}
			Thread.sleep(sleepTimeMsTry);
		}
		
		localLog.log("Client : stopper le client");
		
		assertEquals(true, stopSuccess, "Echec de stop sur le TCPClient.");
		
		localLog.log("Tout s'est bien passé !!!");
		return true;
		
	}
	
	
	
}
