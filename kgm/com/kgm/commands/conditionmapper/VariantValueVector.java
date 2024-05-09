package com.kgm.commands.conditionmapper;

import java.util.Vector;

import com.kgm.common.utils.variant.VariantOption;
import com.kgm.common.utils.variant.VariantValue;

/**
 * 조합식의 데이타 형식 및 표현방법을 재정의.
 * 
 * @author slobbie
 *
 */
@SuppressWarnings({ "serial", "rawtypes", "unused" })
public class VariantValueVector extends Vector implements Comparable{

	boolean isSelected = false;
	
	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	@Override
	public String toString() {
		
		int cnt = 0;
		StringBuffer buffer = new StringBuffer();
		for( int i  = 0; i < size(); i++){
			Object obj = get(i);
			if( obj instanceof VariantValue){
				cnt++;
				VariantValue value = (VariantValue)obj;
				VariantOption option = value.getOption();
				if( cnt > 1){
					buffer.append(" AND ");
				}	
				buffer.append(value.getValueName());
			}
			
		}
		return buffer.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if( obj instanceof VariantValueVector){
			VariantValueVector list = (VariantValueVector)obj;
			
			if( size() != list.size()) 
				return false;
			
			for( int i  = 0; i < size(); i++){
				
				Object obj1 = get(i);
				if( obj1 instanceof VariantValue){
					VariantValue value = (VariantValue)obj1;
					
					boolean bFlag = false;
					for( int j = 0; j < list.size(); j++){
						Object obj2 = list.get(i);
						if( obj2 instanceof VariantValue){
							VariantValue value2 = (VariantValue)obj2;
							if( value.equals(value2)){
								bFlag = true;
								break;
							}
						}
					}
					
					if( !bFlag){
						return false;
					}
				}
				
			}
			
			return true;
		}
		return super.equals(obj);
	}

	
	/**
	 * SRME:: [][20140708] 옵션 Sorting.
	 */		
	@Override
	public int compareTo(Object obj) {
		if( obj instanceof VariantValueVector){
			VariantValueVector vvv = (VariantValueVector)obj;
			return toString().compareTo(vvv.toString());
		}
		return 0;
	}
	
	
}
