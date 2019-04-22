package index.memDic;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.lang3.ArrayUtils;

import com.dant.utils.Log;
import com.dant.utils.MemUsage;
import com.dant.utils.Timer;

import db.structure.Column;
import db.structure.Table;

/**
 * 
 *
 */
public class IndexMemDic {
	
	public final int totalLength;
	private int[] sortedPositions;
	// Pensé pour être mono-thread, pour l'instant
	public static Table table;
	public static int[] colIndexArray; // dans l'ordre
	public static Column[] indexOnThisColArray;
	
	public IndexMemDic(Table argTable, int argTotalLength, int[] argColIndexArray) {
		totalLength = argTotalLength;
		sortedPositions = new int[totalLength];
		colIndexArray = argColIndexArray;
		table = argTable;
		
		indexOnThisColArray = new Column[colIndexArray.length];
		for (int i = 0; i < colIndexArray.length; i++) {
			indexOnThisColArray[i] = table.getColumns().get(colIndexArray[i]);
			//dataAsBytesTotalLength += choosenColArray[i].dataSizeInBytes;
		}
		
	}
	
	/*
	public void setPosition(int sortedByValueIndex, int lineOriginalPosition) {
		sortedPositions[sortedByValueIndex] = lineOriginalPosition;
	}*/
	
	
	public void sortAllv1() {
		
		Timer t = new Timer("IndexMemDic.sortAll pris :");
		MemUsage.printMemUsage();
		IndexMemDicTemporaryItem[] tempSortArray = new IndexMemDicTemporaryItem[totalLength];
		for (int i = 0; i < totalLength; i++) {
			tempSortArray[i] = new IndexMemDicTemporaryItem(i);
		}
		
		Arrays.sort(tempSortArray);
		for (int i = 0; i < totalLength; i++) {
			sortedPositions[i] = tempSortArray[i].originalLinePosition;
		}
		
		t.log();
		MemUsage.printMemUsage();
		
	}
	
}
