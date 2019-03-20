package sj.network.tcpAndBuffers;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Décrit une donnée utilisée dans le NetBuffer
 * Pas totalement commenté, par manque de temps (mais les noms des méthodes sont explicites)
 * 
 */

public class NetBufferData {
	public final NetBufferDataType dataType;
	public Integer integerData = null;
	public Double doubleData = null;
	public Long longData = null;
	public String stringData = null;
	public Boolean booleanData = null;
	public Byte byteData = null;
	public byte[] byteArrayData = null;
	// C'était fait en rush, je coredai mieu maintenant (plus dans la philosophie objet)

	
	public NetBufferData(byte arg_byteData) {
		dataType = NetBufferDataType.BYTE;
		byteData = new Byte(arg_byteData);
	}
	public NetBufferData(int arg_intData) {
		dataType = NetBufferDataType.INTEGER;
		integerData = new Integer(arg_intData);
	}
	public NetBufferData(long arg_longData) {
		dataType = NetBufferDataType.LONG;
		longData = new Long(arg_longData);
	}
	public NetBufferData(double arg_doubleData) {
		dataType = NetBufferDataType.DOUBLE;
		doubleData = new Double(arg_doubleData);
	}
	public NetBufferData(String arg_stringData) {
		dataType = NetBufferDataType.STRING;
		stringData = arg_stringData;
	}
	public NetBufferData(byte[] arg_byteArrayData) {
		dataType = NetBufferDataType.BYTE_ARRAY;
		byteArrayData = arg_byteArrayData;
	}
	public NetBufferData(boolean arg_booleanData) {
		dataType = NetBufferDataType.BOOLEAN;
		booleanData = new Boolean(arg_booleanData);
	}
	
	public byte[] toByteArray() {
		switch (dataType) {
		
		case INTEGER :
			int writeInt = 0;
			if (integerData != null) writeInt = integerData.intValue();
			ByteBuffer intByteBuff = ByteBuffer.allocate(1 + 4);
			intByteBuff.put((byte) dataType.toInteger()); // type de la donnée
			intByteBuff.putInt(writeInt);
			return intByteBuff.array();
			
		case DOUBLE :
			double writeDouble = 0;
			if (doubleData != null) writeDouble = doubleData.doubleValue();
			ByteBuffer doubleByteBuff = ByteBuffer.allocate(1 + 8);
			doubleByteBuff.put((byte) dataType.toInteger()); // type de la donnée
			doubleByteBuff.putDouble(writeDouble);
			return doubleByteBuff.array();
			
		case STRING :
			String writeString = "";
			if (stringData != null) writeString = stringData;
			byte[] strAsBytes = writeString.getBytes(StandardCharsets.UTF_8);
			int totalBuffSize = 1 + 4 + strAsBytes.length;
			
			ByteBuffer strByteBuff = ByteBuffer.allocate(totalBuffSize);
			strByteBuff.put((byte) dataType.toInteger());
			strByteBuff.putInt(strAsBytes.length);
			strByteBuff.put(strAsBytes);
			return strByteBuff.array();
			/*
			int totalBuffSize = 1  + writeString.length() * 2; // chaque char est codé sur 2 octets
			ByteBuffer byteBuff = ByteBuffer.allocate(totalBuffSize);
			byteBuff.put((byte) dataType.toInteger()); // type de la donnée
			byteBuff.putInt(totalBuffSize);
			for (int iChar = 0; iChar < writeString.length(); iChar++) {
				char currentChar = writeString.charAt(iChar);
				byteBuff.putChar(currentChar);
			}*/
			
		case BYTE_ARRAY :
			if (byteArrayData == null)
				return new byte[0];
			ByteBuffer byteDataBuff = ByteBuffer.allocate(1 + 4 + byteArrayData.length);
			byteDataBuff.put((byte) dataType.toInteger()); // type de la donnée
			byteDataBuff.putInt(byteArrayData.length);
			byteDataBuff.put(byteArrayData);
			return byteDataBuff.array();

		case BOOLEAN :
			boolean writeBool = false;
			if (booleanData != null) writeBool = booleanData.booleanValue();
			byte[] resultBooleanByteArray = new byte[2];
			resultBooleanByteArray[0] = (byte) dataType.toInteger(); // type de la donnée
			if (writeBool)
				  resultBooleanByteArray[1] = (byte) 1;
			else  resultBooleanByteArray[1] = (byte) 0;
			return resultBooleanByteArray;
			
		case LONG :
			long writeLong = 0;
			if (longData != null) writeLong = longData.longValue();
			ByteBuffer longByteBuff = ByteBuffer.allocate(1 + 8);
			longByteBuff.put((byte) dataType.toInteger()); // type de la donnée
			longByteBuff.putLong(writeLong);
			return longByteBuff.array();

		case BYTE :
			byte writeByte = 0;
			if (byteData != null) writeByte = byteData.byteValue();
			byte[] resultByteByteArray = new byte[2];
			resultByteByteArray[0] = (byte) dataType.toInteger(); // type de la donnée
			resultByteByteArray[1] = writeByte;
			return resultByteByteArray;
			
		default : return null;
		}
	}
	
	
	// int <-> byteArray
	public static byte[] intToByteArray(int intValue) {
		return ByteBuffer.allocate(4).putInt(intValue).array();
	}
	
	public static int byteArrayToInt(byte[] byteArray) {
		if (byteArray.length != 4) {
			System.out.println("GRAVE NetBufferData.byteArrayToInt : byteArray.length(" + byteArray.length + ") != 4");
			return 0;
		}
	    return ByteBuffer.wrap(byteArray).getInt();
	}
	
	
	// double <-> byteArray
	public static byte[] doubleToByteArray(double doubleValue) {
		return ByteBuffer.allocate(8).putDouble(doubleValue).array();
	}

	public static double byteArrayToDouble(byte[] byteArray) {
		if (byteArray.length != 8) {
			System.out.println("GRAVE NetBuffer.byteArrayToDouble : byteArray.length(" + byteArray.length + ") != 8");
			return 0;
		}
	    return ByteBuffer.wrap(byteArray).getDouble();
	}

	public static long byteArrayToLong(byte[] byteArray) {
		if (byteArray.length != 8) {
			System.out.println("GRAVE NetBuffer.byteArrayToLong : byteArray.length(" + byteArray.length + ") != 8");
			return 0;
		}
	    return ByteBuffer.wrap(byteArray).getLong();
	}

	/*
	// String <-> byteArray
	public static byte[] stringToByteArray(double doubleValue) {
		return ByteBuffer.allocate(8).putDouble(doubleValue).array();
	}
	
	public static String byteArrayToString(byte[] byteArray) {
		if (byteArray.length < 4) {
			System.out.println("GRAVE NetBuffer.byteArrayToString : byteArray.length(" + byteArray.length + ") < 4");
			return "";
		}
		
		
		ByteBuffer strByteBuff = ByteBuffer.allocate(totalBuffSize);
		strByteBuff.put((byte) dataType.toInteger());
		strByteBuff.putInt(strAsBytes.length);
		strByteBuff.put(strAsBytes);
		byte[] stringResultByteArray = new byte[totalBuffSize];
		stringResultByteArray[0] = (byte) dataType.toInteger();
		System.arraycopy(strAsBytes, 0, stringResultByteArray, 1, strAsBytes.length);
		
		
	    return ByteBuffer.wrap(byteArray).getDouble();
	}*/
	
	
	
	/*
	// String <-> byteArray
	public static byte[] stringToByteArray(String stringValue) {
		int totalBuffSize = 4 + stringValue.length();
		ByteBuffer byteBuff = ByteBuffer.allocate(totalBuffSize);
		byteBuff.putInt(totalBuffSize);
		for (int iChar = 0; iChar < stringValue.length(); iChar++) {
			
		}
		
		return byteBuff.array();
	}
	
	public static double byteArrayToDouble(byte[] byteArray) {
		if (byteArray.length != 8) {
			System.out.println("GRAVE NetBuffer.byteArrayToDouble : byteArray.length(" + byteArray.length + ") != 8");
			return 0;
		}
	    return ByteBuffer.wrap(byteArray).getDouble();
	}
	
	
	
	public byte[] encodeToByteArray() {
		
	}*/
	
	private boolean dataIsEqual(Object myData, Object otherData) {
		if ((myData == null) ^ (otherData == null)) return false;
		if (myData == null) return true; // les deux sont null donc
		return myData.equals(otherData);
	}
	
	
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (this.getClass() != o.getClass()) return false;
		
		NetBufferData compareTo = (NetBufferData) o;
		
		if (this.dataType.equals(compareTo.dataType) == false) return false;
		
		if ( dataIsEqual(integerData, compareTo.integerData) == false
		  || dataIsEqual(doubleData, compareTo.doubleData) == false
		  || dataIsEqual(longData, compareTo.longData) == false
		  || dataIsEqual(stringData, compareTo.stringData) == false
		  || dataIsEqual(booleanData, compareTo.booleanData) == false
		  || dataIsEqual(byteData, compareTo.byteData) == false
		  || dataIsEqual(byteArrayData, compareTo.byteArrayData) == false)
			return false;
		
		return true;
	}
	
	public static boolean checkEquals(NetBufferData buffData1, NetBufferData buffData2) {
		if ((buffData1 == null) ^ (buffData2 == null)) return false;
		if (buffData1 == null) return true;
		return buffData1.equals(buffData2);
	}
	
}
