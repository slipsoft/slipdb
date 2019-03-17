package zArchive.sj.simpleDB.arrayList;

public class AL_RowArgument {
	
	public final AL_SStorageDataType argType;
	public final Object value;
	
	public AL_RowArgument(AL_SStorageDataType argumentType, Object value) {
		argType = argumentType;
		this.value = value;
	}
	
}
