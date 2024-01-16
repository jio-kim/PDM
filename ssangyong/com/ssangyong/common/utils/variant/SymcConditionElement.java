package com.ssangyong.common.utils.variant;

import com.teamcenter.rac.pse.variants.modularvariants.ConditionElement;

/**
 * @author slobbie_vm
 * Condition Sorting을 위해 생성
 * [SR140722-022][2014. 7. 02.] swyoon 최초 등록
 */
@SuppressWarnings("rawtypes")
public class SymcConditionElement extends ConditionElement implements Comparable{

	@Override
	public int compareTo(Object obj) {
		if( obj instanceof ConditionElement){
			ConditionElement cElement = (ConditionElement)obj;
			return value.compareTo(cElement.value);
		}		
		return toString().compareTo(obj.toString());
	}

}
