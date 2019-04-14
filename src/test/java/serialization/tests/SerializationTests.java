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

public class SerializationTests {
	private static Table table;
	
	
	public static void createNewTable() throws Exception {
		table = new Table("NYtest");
		Utils currentlyUsedUils = new Utils(); // For thread-safety ! (but, here, it's static so thread unsafe... ^^')

		assertEquals(true, table != null);
		
		//getValuesOfLineByIdForSignleQuery

		table.addColumn("VendorID", new ByteType());
		// -> On a bien les mêmes résultats en castant la date et en la traîtant comme une string
		table.addColumn("tpep_pickup_datetime", new DateType()); //new StringType(19));//
		table.addColumn("tpep_dropoff_datetime", new DateType());//new StringType(19)); //
		table.addColumn("passenger_count", new ByteType());
		table.addColumn("trip_distance", new FloatType());
		table.addColumn("pickup_longitude", new DoubleType());
		table.addColumn("pickup_latitude", new DoubleType());
		table.addColumn("RateCodeID", new ByteType());
		table.addColumn("store_and_fwd_flag", new StringType(1));
		table.addColumn("dropoff_longitude", new DoubleType());
		table.addColumn("dropoff_latitude", new DoubleType());
		table.addColumn("payment_type",  new ByteType());
		table.addColumn("fare_amount", new FloatType());
		table.addColumn("extra", new FloatType());
		table.addColumn("mta_tax", new FloatType());
		table.addColumn("tip_amount", new FloatType());
		table.addColumn("tolls_amount", new FloatType());
		table.addColumn("improvement_surcharge", new FloatType());
		table.addColumn("total_amount", new FloatType());

		
		table.clearDataDirectory();
		
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
