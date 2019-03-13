package sj.network.buffers;

/** Types de données stockées par NetBufferData
 * 
 * @author admin
 *
 */
public enum NetBufferDataType {
	// Association d'un nbuméro à chaque type pour que même si leur ordre est réorganisé entre le client et le serveur, on ait les mêmes valeurs.
	// plus sur que .valueOf()
	BYTE_ARRAY(1), // tableau d'octets
	STRING(2),
	DOUBLE(3),
	INTEGER(4),
	BOOLEAN(5),
	LONG(6),
	BYTE(7),
	UNKNOWN(0);
	
	private int typeId;
	
	NetBufferDataType(int arg_typeId) {
		typeId = arg_typeId;
	}
	
	public int toInteger() {
		return typeId;
	}
	
	public static NetBufferDataType getType(int intValue) {
		switch (intValue) {
		case 1 : return BYTE_ARRAY;
		case 2 : return STRING;
		case 3 : return DOUBLE;
		case 4 : return INTEGER;
		case 5 : return BOOLEAN;
		case 6 : return LONG;
		case 7 : return BYTE;
		default : return UNKNOWN;
		}
	}
}
