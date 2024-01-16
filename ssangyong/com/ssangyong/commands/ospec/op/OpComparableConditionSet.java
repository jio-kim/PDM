package com.ssangyong.commands.ospec.op;

import java.util.Vector;

public class OpComparableConditionSet extends Vector implements Comparable{

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		if( o instanceof OpComparableConditionSet){
			
			OpComparableConditionSet conditionSet = (OpComparableConditionSet)o;
			if( isEmpty() || conditionSet.isEmpty()){
				return toString().compareTo(o.toString());
			}
			
			Object obj = conditionSet.get(0);
			if( obj instanceof String){
				Object obj2 = get(0);
				if( obj2 instanceof String){
					toString().compareTo(o.toString());
				}else{
					return ((String)get(size()-1)).compareTo((String)conditionSet.get(conditionSet.size() - 1));
				}
			}else{
				return (get(size()-1).toString()).compareTo(conditionSet.get(conditionSet.size() - 1).toString());
			}
		}
		return toString().compareTo(o.toString());
	}

}
