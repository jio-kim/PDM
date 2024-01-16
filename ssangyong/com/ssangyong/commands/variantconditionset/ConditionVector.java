package com.ssangyong.commands.variantconditionset;

import java.util.Vector;

import com.teamcenter.rac.pse.variants.modularvariants.ConditionElement;

/**
 * Condition Element를 담아둘 새로운 타입을 정의함.
 * 
 * @author slobbie
 *
 */
@SuppressWarnings({ "rawtypes", "serial" })
public class ConditionVector extends Vector<ConditionElement> implements Comparable{

	@Override
	public synchronized String toString() {
		String s = "";
		ConditionElement[] conditions = new ConditionElement[elementCount];
		System.arraycopy(elementData, 0, conditions, 0, elementCount);
		for( int i = 0; i < elementCount; i++){
			ConditionElement condition = conditions[i];
			String opStr = "";
			if( condition.ifOrAnd == null || condition.ifOrAnd.equals("")){
				opStr = "";
			}else{
				if( condition.ifOrAnd.equalsIgnoreCase("and")){
					opStr = condition.ifOrAnd.toUpperCase();
				}
			}
			s += " " + opStr + " " +  condition.value;
		}
		return s;
	}

	//[SR140722-022][20140522] Condition Sorting 을 위해 추가함.
	//[20140626] YunSungWon. 'Or' Sorting	
	@Override
	public int compareTo(Object obj) {
		if( obj instanceof ConditionVector){
			ConditionVector vec = (ConditionVector)obj;
			return this.toString().compareTo(vec.toString());
		}
		
		return 0;
	}

}
