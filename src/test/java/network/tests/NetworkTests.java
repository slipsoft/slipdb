package network.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
		
		assertEquals(true, buff.equals(buff), "Problème dans la fonction NetBuffer.equals().");
		
		byte[] asByteArray = buff.convertToByteArray();
		
		NetBuffer checkBuffer = NetBuffer.getBufferFromByteArray(asByteArray);
		
		assertEquals(true, buff.equals(checkBuffer), "Problème dans la fonction NetBuffer.getBufferFromByteArray(...)");
		
	}
	
}
