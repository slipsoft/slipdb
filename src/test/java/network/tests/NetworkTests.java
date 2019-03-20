package network.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import sj.network.tcpAndBuffers.NetBuffer;

public class NetworkTests {
	
	
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
	
}
