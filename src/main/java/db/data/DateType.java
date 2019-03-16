package db.data;

import java.nio.ByteBuffer;
//import java.util.Date;
import java.util.Date;

import com.dant.utils.Utils;

public class DateType extends DataType {
	
	protected final static Operator[] compatibleOperatorsList = {
		Operator.equals,
		Operator.greater,
		Operator.less,
		Operator.greaterOrEquals,
		Operator.lessOrEquals,
	};
	
	// Nécessaire pour rendre les opérations trhead-safe, il ne peut pas y avoir de Utils ayant des méthodes statiques utilisant les mêmes objets instanciés.
	// (sous peine d'exceptions dus à des problèmes de concurrence)
	// Et ne pas utiliser de synchronized ou volatile, de préférence, cela réduirait grandement les performances)
	Utils utilsInstance = new Utils();
	
	public DateType() {
		super();
		this.sizeInBytes = 4;
		// peu de dates crées ! :) System.out.println("DateType : nouvelle date !");
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Class getAssociatedClassType() {
		return Integer.class;
	}

	@Override
	public void writeToBuffer(String input, ByteBuffer outputBuffer) {
		int dateAsInt = Utils.dateToSecInt(utilsInstance.dateFromString(input));
		outputBuffer.putInt(dateAsInt);
	}
	
	@Override
	public Integer writeToBufferAndReturnValue(String input, ByteBuffer outputBuffer) {
		Integer dateAsInt = Utils.dateToSecInt(utilsInstance.dateFromString(input));
		outputBuffer.putInt(dateAsInt);
		return dateAsInt;
	}
	
	@Override
	// Date -> Integer, for it to be indexed faster, and the same way Integers are (that's really convenient, see IndexTree for more)
	public Date readTrueValue(byte[] bytes) {
		ByteBuffer wrapped = ByteBuffer.wrap(bytes);
		int dateAsInt = wrapped.getInt(); // converts the byte array into an int
		return Utils.dateFromSecInt(dateAsInt);
	}
	
	@Override
	public Integer readIndexValue(byte[] bytes) {
		ByteBuffer wrapped = ByteBuffer.wrap(bytes);
		return wrapped.getInt();
	}
}
