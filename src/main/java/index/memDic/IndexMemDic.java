package index.memDic;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.lang3.ArrayUtils;

import com.dant.utils.Log;

/**
 * 
 *
 */
public class IndexMemDic {
	
	public final int totalLength;
	private int[] sortedPositions;
	
	public IndexMemDic(int argTotalLength) {
		totalLength = argTotalLength;
		sortedPositions = new int[totalLength];
	}
	
	public void setPosition(int sortedByValueIndex, int lineOriginalPosition) {
		sortedPositions[sortedByValueIndex] = lineOriginalPosition;
	}
	
	public void sortAll() {
		Arrays.sort(sortedPositions);
	}
	
}
