package serialization.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import db.data.ByteType;
import db.data.DataType;
import db.data.DateType;
import db.data.StringType;

public class TestsSerialization {
	
	
	@Test
	public void mainTest() throws IOException, ClassNotFoundException {
		ArrayList<DataType> alDataType = new ArrayList<DataType>();
		//ArrayList<DataType> alDataTypeCheck = new ArrayList<DataType>();

		alDataType.add(new StringType(2));
		alDataType.add(new DateType());
		alDataType.add(new ByteType());
		
		FileOutputStream fs = new FileOutputStream("miniTest.bin");
		ObjectOutputStream os = new ObjectOutputStream(fs);
		
		for (DataType d : alDataType) {
			os.writeObject(d);
		}
		
		os.close();
		FileInputStream ifs = new FileInputStream("miniTest.bin");
		ObjectInputStream is = new ObjectInputStream(ifs);
		Object read = is.readObject();

		assertEquals(true, read.getClass() == StringType.class);
		DataType readAsDataType = (DataType) read;
		assertEquals(true, readAsDataType.getSize() == 2);
		
		read = is.readObject();
		
		assertEquals(true, read.getClass() == DateType.class);
		DateType date = (DateType) read;
		assertEquals(true, date.utilsInstance != null);/**/
		
		//date.utilsInstance.dateToString(new Date());
		is.close();
		
		//assertEquals(true, read.equals(new DateType()));
		
		
		
		
	}
}
