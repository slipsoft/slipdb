package zArchive.sj.tcpAndBuffers;

//import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Classe NetBuffer permettant de constituer des messages
 * Pas totalement commenté, par manque de temps
 */

public class NetBuffer { // fonctionnement synchrone, non thread-safe
	
	public ArrayList<NetBufferData> dataList = new ArrayList<NetBufferData>();
	private int currentReadPos = 0;
	
	private byte[] receivedRawData = null;

	public void writeInteger(int intData) {
		NetBufferData newData = new NetBufferData(intData);
		dataList.add(newData);
	}
	public void writeInt(int intData) { writeInteger(intData); }

	public void writeLong(long longData) {
		NetBufferData newData = new NetBufferData(longData);
		dataList.add(newData);
	}
	public void writeInt64(long longData) {
		writeLong(longData);
	}
	
	public void writeDouble(double doubleData) {
		NetBufferData newData = new NetBufferData(doubleData);
		dataList.add(newData);
	}
	
	public void writeString(String stringData) {
		NetBufferData newData = new NetBufferData(stringData);
		dataList.add(newData);
	}
	
	public void writeByteArray(byte[] byteArrayData) {
		NetBufferData newData = new NetBufferData(byteArrayData);
		dataList.add(newData);
	}
	public void writeBoolean(boolean boolData) {
		NetBufferData newData = new NetBufferData(boolData);
		dataList.add(newData);
	}
	public void writeBool(boolean boolData) { writeBoolean(boolData); }
	
	public void writeByte(byte byteData) {
		NetBufferData newData = new NetBufferData(byteData);
		dataList.add(newData);
	}
	

	public void setPosition(int setOffset) {
		currentReadPos = setOffset;
	}
	
	public void insertAtPos(int position, NetBufferData dataToInsert) {
		if( position > 0 && position < this.dataList.size()) this.dataList.add(position, dataToInsert);
	}
	public void rewind() {
		setPosition(0);
	}
	
	/** Dernière position valide dans le buffer
	 * @return -1 si aucune donnée dans le buffer, le dernier index valide sinon
	 */
	public int getLastIndex() {
		return dataList.size() - 1;
	}
	
	/** Fonction de débug UNIQUEMENT, ne sert qu'à avoir un aperçu de ce qu'un y a dans le NetBuffer
	 * Pour convertir un NetBuffer de manière exhaustive, il faut utiliser convertToByteArray
	 */
	public String toString() {
		StringBuffer buff = new StringBuffer();
		for (NetBufferData data : this.dataList) {
			buff.append(data.toString() + " ");
		}
		if (buff.length() > 0) return new String(buff);
		else
			return "";
	}
	// Les Read retournent une exception si on tente de lire un 
	
	
	public int readInteger() { // ne retourne pas d'exception IndexOutOfBoundsException.
		if (currentReadPos >= dataList.size()) return 0;
		NetBufferData data = dataList.get(currentReadPos);
		currentReadPos++;
		if (data == null) return 0;
		if (data.integerData == null) return 0; // ne devrait pas arriver si le message est lu dans le bon ordre
		return data.integerData;
	}
	public int readInt() { return readInteger(); }

	public long readLong() {
		if (currentReadPos >= dataList.size()) return 0;
		NetBufferData data = dataList.get(currentReadPos);
		currentReadPos++;
		if (data == null) return 0;
		if (data.longData == null) return 0; // ne devrait pas arriver si le message est lu dans le bon ordre
		return data.longData;
	}
	public long readInt64() {
		return readLong();
	}
	
	public double readDouble() {
		if (currentReadPos >= dataList.size()) return 0;
		NetBufferData data = dataList.get(currentReadPos);
		currentReadPos++;
		if (data == null) return 0;
		if (data.doubleData == null) return 0; // ne devrait pas arriver si le message est lu dans le bon ordre
		return data.doubleData;
	}
	
	public String readString() {
		if (currentReadPos >= dataList.size()) return "";
		NetBufferData data = dataList.get(currentReadPos);
		currentReadPos++;
		if (data == null) return "";
		if (data.stringData == null) return ""; // ne devrait pas arriver si le message est lu dans le bon ordre
		return data.stringData;
	}
	public String readStr() { return readString(); }
	
	public byte[] readByteArray() {
		if (currentReadPos >= dataList.size()) return new byte[0];
		NetBufferData data = dataList.get(currentReadPos);
		currentReadPos++;
		if (data == null) return new byte[0];
		if (data.byteArrayData == null) return new byte[0]; // ne devrait pas arriver si le message est lu dans le bon ordre
		return data.byteArrayData;
	}

	public boolean readBoolean() {
		if (currentReadPos >= dataList.size()) return false;
		NetBufferData data = dataList.get(currentReadPos);
		currentReadPos++;
		if (data == null) return false;
		if (data.booleanData == null) return false; // ne devrait pas arriver si le message est lu dans le bon ordre
		return data.booleanData;
	}
	public boolean readBool() { return readBoolean(); }
	

	public byte readByte() {
		if (currentReadPos >= dataList.size()) return 0;
		NetBufferData data = dataList.get(currentReadPos);
		currentReadPos++;
		if (data == null) return 0;
		if (data.byteData == null) return 0; // ne devrait pas arriver si le message est lu dans le bon ordre
		return data.byteData;
	}
	
	
	
	// Vérification des types de données
	public boolean currentData_isInteger() {
		//if (currentReadPos >= dataList.size()) return false;
		NetBufferData data = dataList.get(currentReadPos);
        return data.dataType.equals(NetBufferDataType.INTEGER);
    }
	public boolean currentData_isDouble()    { NetBufferData data = dataList.get(currentReadPos); return data.dataType.equals(NetBufferDataType.DOUBLE); }
	public boolean currentData_isString()    { NetBufferData data = dataList.get(currentReadPos); return data.dataType.equals(NetBufferDataType.STRING); }
	public boolean currentData_isByteArray() { NetBufferData data = dataList.get(currentReadPos); return data.dataType.equals(NetBufferDataType.BYTE_ARRAY); }
	public boolean currentData_isBoolean()   { NetBufferData data = dataList.get(currentReadPos); return data.dataType.equals(NetBufferDataType.BOOLEAN); }
	public boolean currentData_isByte()      { NetBufferData data = dataList.get(currentReadPos); return data.dataType.equals(NetBufferDataType.BYTE); }
	public boolean currentData_isLong()      { NetBufferData data = dataList.get(currentReadPos); return data.dataType.equals(NetBufferDataType.LONG); }
	public boolean currentData_isInt()       { return currentData_isInteger(); } // écriture abrégée
	public boolean currentData_isStr()       { return currentData_isString(); } // écriture abrégée
	public boolean currentData_isBool()      { return currentData_isBoolean(); } // écriture abrégée
	
	
	//public boolean currentData_isInteger()    throws IndexOutOfBoundsException { NetBufferData data = dataList.get(currentReadPos); if (data.dataType.equals(NetBufferDataType.INTEGER))    return true; return false; }
	//public boolean currentData_isInt()        throws IndexOutOfBoundsException { return currentData_isInteger(); }
	
	
	public void resetReadPosition() {
		currentReadPos = 0;
	}
	
	public NetBuffer() {
		
	}
	
	
	/** Récurérer les données brutes
	 * @param arg_receivedRawData
	 */
	public NetBuffer(byte[] arg_receivedRawData) {
		receivedRawData = arg_receivedRawData;
		readFromReceivedRawData();
	}
	
	public NetBufferData getDataAtPosition(int index) {
		if (index >= dataList.size()) return null;
		return dataList.get(index);
	}
	
	/** Créer un buffer à partir d'un array de byte
	 *  Pareil que new NetBuffer(byteArray) mais en statique.
	 *  @param arg_receivedRawData
	 *  @return
	 */
	public static NetBuffer getBufferFromByteArray(byte[] arg_receivedRawData) {
		NetBuffer result = new NetBuffer(arg_receivedRawData);
		return result;
	}
	
	public static boolean compareNetBuffers(NetBuffer buff1, NetBuffer buff2) {
		/*if (buff1 == null && buff2 != null) return false;
		if (buff1 != null && buff2 == null) return false;*/
		//System.out.println("----> NetBuffer.compareNetBuffers : 1");
		if ((buff1 == null) ^ (buff2 == null)) return false; // xor, l'un des deux est null mais pas l'autre
		if (buff1 == null && buff2 == null) return true;
		//System.out.println("----> NetBuffer.compareNetBuffers : 2");
		int dataListSize = buff1.dataList.size();
		if (dataListSize != buff2.dataList.size()) return false;
		//System.out.println("----> NetBuffer.compareNetBuffers : 3");
		
		for (int dataIndex = 0; dataIndex < dataListSize; dataIndex++) {
			NetBufferData data1 = buff1.getDataAtPosition(dataIndex);
			NetBufferData data2 = buff2.getDataAtPosition(dataIndex);
			//System.out.println("----> NetBuffer.compareNetBuffers : data " + dataIndex + "...");
			if (NetBufferData.checkEquals(data1,  data2) == false) return false;
			//System.out.println("----> NetBuffer.compareNetBuffers : data " + dataIndex + " OK !");
		}
		return true;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o.getClass() != getClass()) return false;
		return NetBuffer.compareNetBuffers(this, (NetBuffer) o);
	}
	
	/** Copier les donnes brutes du buffer de réception
	 * @param arg_threadReceivedBytesFULL
	 * @param copyStartIndex
	 * @param copyLength
	 */
	public NetBuffer(byte[] arg_threadReceivedBytesFULL, int copyStartIndex, int copyLength) {
		receivedRawData = new byte[copyLength];
		System.arraycopy(arg_threadReceivedBytesFULL, copyStartIndex, receivedRawData, 0, copyLength);
		readFromReceivedRawData();
		//System.out.println("NetBuffer create copyLength = " + copyLength);
	}
	
	
	/** Si c'est un buffer reçu, il y a des données brutes reçues
	 * 
	 * @return
	 */
	public byte[] getReceivedRawData() {
		return receivedRawData;
	}
	
	
	/** Transforme le NetBuffer en tableau d'octets
	 * @return
	 */
	public byte[] convertToByteArray() {
		// Ecriture de toutes les données dans un buffer
		// Je constitue la liste des données à envoyer
		int totalDataBufferSize = 0;
		ArrayList<byte[]> a2DataAsByteArray = new ArrayList<byte[]>();
		
		for (int iData = 0; iData < dataList.size(); iData++) {
			NetBufferData data = dataList.get(iData);
			byte[] dataToByteArray = data.toByteArray();
			a2DataAsByteArray.add(dataToByteArray);
			totalDataBufferSize += dataToByteArray.length;
		}
		//System.out.println("----> NetBuffer.convertToByteArray : totalDataBufferSize = " + totalDataBufferSize);
		
		if (totalDataBufferSize >= 10_000_000) { // Message beaucoup trop grand
			return null;
		}
		byte[] packetBuffer = new byte[4 + totalDataBufferSize];
		byte[] packetSizeBuffer = NetBufferData.intToByteArray(totalDataBufferSize);
		System.arraycopy(packetSizeBuffer, 0, packetBuffer, 0, packetSizeBuffer.length); // Taille du buffer de ce NetBuffer. packetSizeBuffer.length = 4
		int currentPosInPacketBuffer = 0 + 4; // les 4 octets indiquant la taille du message
		// Ajout des données
		for (int iData = 0; iData < a2DataAsByteArray.size(); iData++) {
			byte[] dataAsByteArray = a2DataAsByteArray.get(iData);
			System.arraycopy(dataAsByteArray, 0, packetBuffer, currentPosInPacketBuffer, dataAsByteArray.length);
			currentPosInPacketBuffer += dataAsByteArray.length;
		}
		return packetBuffer;
	}
	
	/** Sécurité : vérifier que le buffer de réception est assez gros pour lire les octets restants
	 * @param posInBuffer
	 * @param needToReadSize
	 * @return
	 */
	private boolean bufferIsTooSmall(int posInBuffer, int needToReadSize) {
		int totalBufferSize = receivedRawData.length;
		if (posInBuffer + needToReadSize > totalBufferSize) { // plus assez d'espace
			System.err.println("ERREUR GRAVE NetBuffer.bufferIsTooSmall : receivedRawData.length("+receivedRawData.length+") < posInBuffer + needToReadSize ("+(posInBuffer + needToReadSize)+")");
			return true;
		}
		return false;
	}
	/** Erreur critique : supprimer les données du NetBuffer
	 */
	private void clearRawDataOnCriticalError() {
		dataList.clear();
		currentReadPos = 0;
		receivedRawData = null;
	}
	
	/** Sécurité : vérifier que le buffer de réception est assez gros pour lire les octets restants
	 * @param posInBuffer
	 * @param needToReadSize
	 * @return
	 */
	private boolean checkCriticalReadSizeError(int posInBuffer, int needToReadSize) {
		if (bufferIsTooSmall(posInBuffer, needToReadSize)) {
			clearRawDataOnCriticalError();
			//System.out.println("----> NetBuffer.checkCriticalReadSizeError : retuen TRUE");
			return true;
		}
		//System.out.println("----> NetBuffer.checkCriticalReadSizeError : retuen FALSE");
		return false;
	}
	
	/** Lire le buffer depuis un tableau d'octets, buffer précédemment encodé via NetBuffer.convertToByteArray()
	 * @param skipBufferSizeBytes  mettre l'offset à 4 si vrai.
	 */
	public void readFromReceivedRawData() { //(boolean skipBufferSizeBytes) {
		dataList.clear();
		currentReadPos = 0;
		//System.out.println("----> NetBuffer.readFromReceivedRawData : receivedRawData.length = " + receivedRawData.length);
		
		if (receivedRawData == null) return;
		if (checkCriticalReadSizeError(0, 4)) return; // <- erreur critique, taille du buffer insuffisante
		
		byte[] bufferSizeAsByteArray = new byte[4];
		System.arraycopy(receivedRawData, 0, bufferSizeAsByteArray, 0, 4);
		int totalDataBufferSize = NetBufferData.byteArrayToInt(bufferSizeAsByteArray);
		//System.out.println("----> NetBuffer.readFromReceivedRawData : totalDataBufferSize = " + totalDataBufferSize);
		
		// Erreur de taille du buffer :
		if (receivedRawData.length != totalDataBufferSize + 4) {
			System.err.println("ERREUR GRAVE NetBuffer.readFromReceivedRawData : receivedRawData.length("+receivedRawData.length+") != totalDataBufferSize + 4 ("+(totalDataBufferSize+4)+")");
			return;
		}
		
		int currentPosInRawDataBuffer = 0 + 4; // 4 octets pour indiquer la taille totale du buffer contenant les données
		int receivedDataCount = 0;
		
		while (currentPosInRawDataBuffer < receivedRawData.length) {
			
			byte dataTypeAsByte = receivedRawData[currentPosInRawDataBuffer];
			currentPosInRawDataBuffer++;
			NetBufferDataType dataType = NetBufferDataType.getType(dataTypeAsByte);
			//NetBufferData data = null;
			switch (dataType) {
			
			case INTEGER :
				if (checkCriticalReadSizeError(currentPosInRawDataBuffer, 4)) return; // <- erreur critique, taille du buffer insuffisante
				byte[] intByteArray = new byte[4];
				System.arraycopy(receivedRawData, currentPosInRawDataBuffer, intByteArray, 0, 4);
				currentPosInRawDataBuffer += 4;
				int intValue = NetBufferData.byteArrayToInt(intByteArray);
				NetBufferData intData = new NetBufferData(intValue);
				dataList.add(intData);
				break;
				
			case DOUBLE :
				if (checkCriticalReadSizeError(currentPosInRawDataBuffer, 8)) return; // <- erreur critique, taille du buffer insuffisante
				byte[] doubleByteArray = new byte[8];
				System.arraycopy(receivedRawData, currentPosInRawDataBuffer, doubleByteArray, 0, 8);
				currentPosInRawDataBuffer += 8;
				double doubleValue = NetBufferData.byteArrayToDouble(doubleByteArray);
				NetBufferData doubleData = new NetBufferData(doubleValue);
				dataList.add(doubleData);
				break;
			
			case STRING :
				if (checkCriticalReadSizeError(currentPosInRawDataBuffer, 4)) return; // <- erreur critique, taille du buffer insuffisante
				byte[] stringSizeByteArray = new byte[4];
				System.arraycopy(receivedRawData, currentPosInRawDataBuffer, stringSizeByteArray, 0, 4);
				currentPosInRawDataBuffer += 4;
				int stringSize = NetBufferData.byteArrayToInt(stringSizeByteArray);
				if (checkCriticalReadSizeError(currentPosInRawDataBuffer, stringSize)) return; // <- erreur critique, taille du buffer insuffisante
				byte[] stringAsByteArray = new byte[stringSize];
				System.arraycopy(receivedRawData, currentPosInRawDataBuffer, stringAsByteArray, 0, stringSize);
				currentPosInRawDataBuffer += stringSize;
				String stringValue = new String(stringAsByteArray, StandardCharsets.UTF_8);
				NetBufferData stringData = new NetBufferData(stringValue);
				dataList.add(stringData);
				break;
			
			case BYTE_ARRAY :
				if (checkCriticalReadSizeError(currentPosInRawDataBuffer, 4)) return; // <- erreur critique, taille du buffer insuffisante
				byte[] byteArraySizeByteArray = new byte[4];
				System.arraycopy(receivedRawData, currentPosInRawDataBuffer, byteArraySizeByteArray, 0, 4);
				currentPosInRawDataBuffer += 4;
				int byteArraySize = NetBufferData.byteArrayToInt(byteArraySizeByteArray);
				if (checkCriticalReadSizeError(currentPosInRawDataBuffer, byteArraySize)) return; // <- erreur critique, taille du buffer insuffisante
				byte[] byteArrayValue = new byte[byteArraySize];
				System.arraycopy(receivedRawData, currentPosInRawDataBuffer, byteArrayValue, 0, byteArraySize);
				currentPosInRawDataBuffer += byteArraySize;
				NetBufferData byteArrayData = new NetBufferData(byteArrayValue);
				dataList.add(byteArrayData);
				break;

			case BOOLEAN :
				if (checkCriticalReadSizeError(currentPosInRawDataBuffer, 1)) return; // <- erreur critique, taille du buffer insuffisante
				byte booleanAsByteValue = receivedRawData[currentPosInRawDataBuffer];
				currentPosInRawDataBuffer++;
				boolean booleanValue = (booleanAsByteValue == 1);
				NetBufferData boolData = new NetBufferData(booleanValue);
				dataList.add(boolData);
				break;
				
			case LONG :
				if (checkCriticalReadSizeError(currentPosInRawDataBuffer, 8)) return; // <- erreur critique, taille du buffer insuffisante
				byte[] longByteArray = new byte[8];
				System.arraycopy(receivedRawData, currentPosInRawDataBuffer, longByteArray, 0, 8);
				currentPosInRawDataBuffer += 8;
				long longValue = NetBufferData.byteArrayToLong(longByteArray);
				NetBufferData longData = new NetBufferData(longValue);
				dataList.add(longData);
				break;

			case BYTE :
				if (checkCriticalReadSizeError(currentPosInRawDataBuffer, 1)) return; // <- erreur critique, taille du buffer insuffisante
				byte byteValue = receivedRawData[currentPosInRawDataBuffer];
				currentPosInRawDataBuffer++;
				NetBufferData byteData = new NetBufferData(byteValue);
				dataList.add(byteData);
				break;
				
			default : break;
			}
			receivedDataCount++;
		}
		//System.out.println("----> NetBuffer.readFromReceivedRawData : receivedDataCount = " + receivedDataCount);
		
		
	}
	
	/** Lent et pas optimisé, mais fonctionnel. Fait par manque de temps.
	 * @param otherBuffer
	 * @return
	 */
	public boolean isEqualTo(NetBuffer otherBuffer) {
		return equals(otherBuffer);
		/*
		if (otherBuffer == null) return false;
		// méthode bourrine, pas optimisée du tout : PAR MANQUE DE TEMPS !
		byte[] thisBufferAsByteArray = this.convertToByteArray();
		byte[] otherBufferAsByteArray = otherBuffer.convertToByteArray();
		boolean areEqual = Arrays.equals(thisBufferAsByteArray, otherBufferAsByteArray);
		return areEqual; */
	}
	
	
}
