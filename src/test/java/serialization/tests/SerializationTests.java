package serialization.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.dant.utils.Utils;

import db.data.types.ByteType;
import db.data.types.DataType;
import db.data.types.DateType;
import db.data.types.DoubleType;
import db.data.types.FloatType;
import db.data.types.StringType;
import db.serial.SerialStructure;
import db.structure.Database;
import db.structure.Table;
import db.structure.recherches.TableHandler;

public class SerializationTests {
	
	
	
	public void createNewTable() throws Exception {
		Table table;
		Utils currentlyUsedUils = new Utils(); // For thread-safety ! (but, here, it's static so thread unsafe... ^^')
		
		TableHandler tableHandler = new TableHandler("NYtest");
		assertEquals(true, tableHandler != null);
		
		//getValuesOfLineByIdForSignleQuery

		tableHandler.addColumn("VendorID", new ByteType());
		// -> On a bien les mêmes résultats en castant la date et en la traîtant comme une string
		tableHandler.addColumn("tpep_pickup_datetime", new DateType()); //new StringType(19));//
		tableHandler.addColumn("tpep_dropoff_datetime", new DateType());//new StringType(19)); // 
		tableHandler.addColumn("passenger_count", new ByteType());
		tableHandler.addColumn("trip_distance", new FloatType());
		tableHandler.addColumn("pickup_longitude", new DoubleType());
		tableHandler.addColumn("pickup_latitude", new DoubleType());
		tableHandler.addColumn("RateCodeID", new ByteType());
		tableHandler.addColumn("store_and_fwd_flag", new StringType(1));
		tableHandler.addColumn("dropoff_longitude", new DoubleType());
		tableHandler.addColumn("dropoff_latitude", new DoubleType());
		tableHandler.addColumn("payment_type",  new ByteType());
		tableHandler.addColumn("fare_amount", new FloatType());
		tableHandler.addColumn("extra", new FloatType());
		tableHandler.addColumn("mta_tax", new FloatType());
		tableHandler.addColumn("tip_amount", new FloatType());
		tableHandler.addColumn("tolls_amount", new FloatType());
		tableHandler.addColumn("improvement_surcharge", new FloatType());
		tableHandler.addColumn("total_amount", new FloatType());
		
		table = tableHandler.createTable();
		
		tableHandler.clearDataDirectory();
		
		Database.getInstance().getAllTables().add(table);
	}
	
	
	
	@Test
	public void mainTest() throws Exception {
		//createNewTable();
		SerialStructure.loadStructure();
		
	}
	
	
	
	//@Test
	public void simpleTest() throws IOException, ClassNotFoundException {
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
