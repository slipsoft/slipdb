package db.data.types;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
//import java.util.Date;
import java.util.Date;

import org.apache.commons.lang3.ArrayUtils;

import com.dant.utils.Utils;

import db.search.Operator;

public class DateType extends DataType implements Serializable {
	private static final long serialVersionUID = -2160294934147905077L;

	public static boolean sizeIsRequired = false;
	
	// Nécessaire pour rendre les opérations trhead-safe, il ne peut pas y avoir de Utils ayant des méthodes statiques utilisant les mêmes objets instanciés.
	// (sous peine d'exceptions dus à des problèmes de concurrence)
	// Et ne pas utiliser de synchronized ou volatile, de préférence, cela réduirait grandement les performances)
	transient public Utils utilsInstance = new Utils();
	
	/** Pour la déserialisation
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		utilsInstance = new Utils();
	}
	
	public DateType() {
		super();
		utilsInstance = new Utils();
		this.sizeInBytes = 4;
		// peu de dates crées ! :) System.out.println("DateType : nouvelle date !");
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Class getAssociatedClassType() {
		return Integer.class;
	}
	
	@Override
	public Object parseAndWriteToBuffer(String input, ByteBuffer outputBuffer) throws IllegalArgumentException { // throws NumberFormatException {
		Integer dateAsInt = Utils.dateToSecInt(utilsInstance.dateFromString(input));
		outputBuffer.putInt(dateAsInt);
		/*
		Utilisé pour chercher un bug (qui s'sst évéré être le passage du format 12H au format 24H
		int debugSearchForDate = Utils.dateToSecInt(Utils.dateFromStringNoThreadSafe(("2015-04-04 00:18:57")));
		if (debugSearchForDate == dateAsInt) {
			Log.error("AHAH : debugSearchForDate == dateAsInt " +  dateAsInt + "   input = " +input);
		}*/
		
		return dateAsInt;
	}
	
	/*
	public Object parseAndWriteToBufferDateThreadSafe(String input, ByteBuffer outputBuffer, Utils localUtilsInstance) throws IllegalArgumentException { // throws NumberFormatException {
		Integer dateAsInt = Utils.dateToSecInt(localUtilsInstance.dateFromString(input));
		outputBuffer.putInt(dateAsInt);
		return dateAsInt;
	}*/
	
	
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

	@Override
	public boolean isOperatorCompatible(Operator op) {
		return ArrayUtils.contains(new Operator[] {
			Operator.equals,
			Operator.greater,
			Operator.less,
			Operator.greaterOrEquals,
			Operator.lessOrEquals,
		}, op);
	}
}
