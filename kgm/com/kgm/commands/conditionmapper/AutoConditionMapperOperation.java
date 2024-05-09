package com.kgm.commands.conditionmapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTable;

import com.kgm.common.WaitProgressBar;
import com.kgm.common.utils.variant.VariantOption;
import com.kgm.common.utils.variant.VariantValue;
import com.teamcenter.rac.aif.AbstractAIFOperation;

/**
 * Positive �Ǵ� Negative Filter�� �߰��� �ɼǵ��� �����Ͽ�
 * ��밡���� ��� �ɼ� ����� ���� ����Ѵ�.
 * 
 * @author slobbie
 *
 */
@SuppressWarnings({"rawtypes", "unchecked", "unused"})
public class AutoConditionMapperOperation extends AbstractAIFOperation {

    private JList selectedOptions = null;
	private JList positiveFilterList = null;
	private JList negativeFilterList = null;
	private JTable resultConditionTable = null;
	private HashMap<VariantValue, ArrayList<VariantValue>> positiveFilterMap = new HashMap();
	private HashMap<VariantValue, ArrayList<VariantValue>> negativeFilterMap = new HashMap();
	private HashMap<String, HashMap<String, VariantOption>> filteredVariantValues = new HashMap();
	private AutoConditionMapperDialog dialog = null;
	private WaitProgressBar waitProgress = null;
	
	// AutoConditionMapperDialog �� ������ �ɼ� �ڵ带 ��� �Ѱ�
	private ArrayList filterValues = new ArrayList();
	
	public AutoConditionMapperOperation(AutoConditionMapperDialog dialog, WaitProgressBar waitProgress){
		this.dialog = dialog;
		this.waitProgress = waitProgress;
		this.selectedOptions= dialog.getSelectedOptions();
		this.positiveFilterList = dialog.getPositiveFilterList();
		this.negativeFilterList = dialog.getNegativeFilterList();
		this.resultConditionTable = dialog.getResultConditionTable();
	}
	
	@Override
	public void executeOperation() throws Exception {

		ArrayList<VariantValueVector> allCondition = new ArrayList();
		try{
			ArrayList valueSetList = new ArrayList();
			DefaultListModel model = (DefaultListModel)selectedOptions.getModel();
			for( int i = 0; i < model.getSize(); i++){
				VariantOption option = (VariantOption)model.get(i);
				List<VariantValue> values = getEnableValues( option );
				valueSetList.add(values);
			}
			
			int[] indexes = new int[model.size()];
			for( int i = 0; i < model.size(); i++){
				indexes[i] = 0;
			}
			
			waitProgress.setStatus("Applying filter..");
			//Filter ���� Start
			applyFilter();
			//Filter ���� END
			
			waitProgress.setStatus("it takes a few minutes...");
			getAllCondition(allCondition, valueSetList, null, null, 0);
			System.out.println("����� �� : " + allCondition.size());
			dialog.setConditionData(allCondition);
			dialog.setComboFilter(filterValues);
			waitProgress.close();
		}catch( Exception e){
			e.printStackTrace();
			waitProgress.setStatus(e.getMessage());
			waitProgress.setShowButton(true);
		}finally{
			dialog.getCountLabel().setText(allCondition.size() + "/" + allCondition.size());
		}
		
	}

	private void getAllCondition(ArrayList<VariantValueVector> allCondition, ArrayList<List<VariantValue>> valueSetList, ArrayList<VariantValue> enableValues, VariantValueVector baseValues, int idx1){
		
		List<VariantValue> valueSet = null;
		if( enableValues == null){
			valueSet = (List<VariantValue>)valueSetList.get(idx1);
		}else{
			valueSet = enableValues;
		}
		
		if( baseValues == null ){
			baseValues = new VariantValueVector();
		}
		
		for( VariantValue value : valueSet){
			VariantOption option = value.getOption();
			
			if( !filterValues.contains(value)){
				filterValues.add(value);
			}
			
			VariantValueVector clonedValues = (VariantValueVector)baseValues.clone();
			clonedValues.add(value);
			
			if( idx1 + 1 < valueSetList.size()){
				
				//���� �ɼ��� ������ �ɼ��ڵ尪�� ���͸� �̿��Ͽ� �̸� ��������.
				ArrayList<VariantValue> nextEnableValues = null; 
				ArrayList<VariantValue> tmpList = (ArrayList<VariantValue>)((ArrayList<VariantValue>)valueSetList.get(idx1 + 1)).clone();
				VariantOption tmpOption = tmpList.get(0).getOption();
				HashMap<String, VariantOption> map = filteredVariantValues.get(value.getValueName());
				if( map != null){
					if( map.containsValue(tmpOption)){
						tmpOption = map.get(tmpOption.getOptionName());
						nextEnableValues = (ArrayList<VariantValue>)tmpOption.getValues();
					}else{
						nextEnableValues = (ArrayList<VariantValue>)tmpList;
					}
				}else{
					nextEnableValues = (ArrayList<VariantValue>)tmpList;
				}
				
				if( nextEnableValues == null){
					nextEnableValues = new ArrayList();
				}
				
				//A�� B�̴�.
				//A�� C�̴�. �� ���� ��츦 ã������ ���� ���Ͱ��� ã�´�.
				if( baseValues.size() > 0){
					
					ArrayList<VariantValue> finalEnableValues = new ArrayList();
					for( int i = 0; i < baseValues.size(); i++){
						VariantValue tmpValue = (VariantValue)baseValues.get( i );
						HashMap<String, VariantOption> preMap = filteredVariantValues.get(tmpValue.getValueName());

						if( preMap != null && preMap.containsValue(tmpOption)){
							VariantOption tOption = preMap.get(tmpOption.getOptionName());
							ArrayList<VariantValue> tValues = (ArrayList<VariantValue>)tOption.getValues();
							for( int j = 0; tValues != null && j < tValues.size(); j++){
								if( nextEnableValues.contains(tValues.get(j))){
									finalEnableValues.add(tValues.get(j));
								}
							}
							nextEnableValues.clear();
							if( !finalEnableValues.isEmpty()){
								nextEnableValues.addAll(finalEnableValues);
							}
						}
					}
				}
				
				if( tmpOption == null){
					getAllCondition(allCondition, valueSetList, null, clonedValues, idx1 + 1);
				}else{
					getAllCondition(allCondition, valueSetList, nextEnableValues, clonedValues, idx1 + 1);
				}
				
			}else{
				
				clonedValues.insertElementAt(new Boolean(false), 0);
				clonedValues.insertElementAt("", 1);
				if( !allCondition.contains(clonedValues)){
					allCondition.add(clonedValues);
				}
				
			}
		}
		
	}
	
	
	/**
	 * ���͸� �����Ͽ� ���õ� �ɼǿ� �ش��ϴ� ���ս�(����� ��)�� ����.
	 * 
	 * ������ ���� �ɼ��ڵ尡 ��� �ɼ��ڵ庸�� index���� ũ�� �ȵ�.
	 * 	Selected Options List���� ������ ���ǰ� ������� ������ �ݴ��� ���� �߰������� �������� ������ �ɾ�� �Ѵ�.
	 *	EX) A50=A50E THEN A30=A30S	POSITIVE
	 *	EX) A50=A50E THEN A30!=A30S	NEGATIVE
	 *	 �� ����.
	 * 
	 */
	private void applyFilter(){
		
		//Negative���� ����.
		filteredVariantValues.clear();
		negativeFilterMap.clear();
		positiveFilterMap.clear();
		
		DefaultListModel model = (DefaultListModel)negativeFilterList.getModel();
		for( int i = 0; i < model.size(); i++){
			List row = (List)model.get(i);
			ArrayList list = negativeFilterMap.get(row.get(0));
			if( list == null ){
				list = new ArrayList();
			}
			list.add(row.get(2));
			negativeFilterMap.put((VariantValue)row.get(0), list);
		}
		
		model = (DefaultListModel)positiveFilterList.getModel();
		for( int i = 0; i < model.size(); i++){
			List row = (List)model.get(i);
			ArrayList list = positiveFilterMap.get(row.get(0));
			if( list == null ){
				list = new ArrayList();
			}
			list.add(row.get(2));
			positiveFilterMap.put((VariantValue)row.get(0), list);
		}
		
		DefaultListModel selectedOptionModel = (DefaultListModel)selectedOptions.getModel();
		model = (DefaultListModel)negativeFilterList.getModel();
		for( int i = 0; i < model.getSize(); i++){
			List list = (List)model.getElementAt(i);
			VariantValue leftValue = (VariantValue)list.get(0);
			VariantOption leftOption = leftValue.getOption();
			int leftIdx = -1;
			for( int j = 0; j < selectedOptionModel.size(); j++){
				VariantOption sOption = (VariantOption)selectedOptionModel.get(j);
				if( leftOption.equals(sOption)){
					leftIdx = j;
					break;
				}
			}
			
			//���͸� �����ؾ��ϴµ� �ش��ϴ� �ڵ带 ã���� ���ٸ�..����.
			if( leftIdx < 0) return;
			
			VariantValue rightValue = (VariantValue)list.get(2);
			VariantOption rightOption = rightValue.getOption();
			int rightIdx = -1;
			for( int j = 0; j < selectedOptionModel.size(); j++){
				VariantOption sOption = (VariantOption)selectedOptionModel.get(j);
				if( rightOption.equals(sOption)){
					rightIdx = j;
					break;
				}
			}
			
			//���͸� �����ؾ��ϴµ� �ش��ϴ� �ڵ带 ã���� ���ٸ�..����.
			if( rightIdx < 0) return;
			
			if( leftIdx > rightIdx){
				//�ε����� ���� �ʴ� ���ʹ� ���� �� ���ο� ���͸� �����Ѵ�.
				negativeFilterMap.remove(leftValue);

				ArrayList negativeList = negativeFilterMap.get(rightValue);
				if( negativeList == null){
					negativeList = new ArrayList();
				}
				
				if( !negativeList.contains(leftValue)){
					negativeList.add(leftValue);
				}
				
				negativeFilterMap.put(rightValue, negativeList);
			}
		}
		
		//Positive���� ����.
		model = (DefaultListModel)positiveFilterList.getModel();
		for( int i = 0; i < model.getSize(); i++){
			List list = (List)model.getElementAt(i);
			VariantValue leftValue = (VariantValue)list.get(0);
			VariantOption leftOption = leftValue.getOption();
			int leftIdx = -1;
			for( int j = 0; j < selectedOptionModel.size(); j++){
				VariantOption sOption = (VariantOption)selectedOptionModel.get(j);
				if( leftOption.equals(sOption)){
					leftIdx = j;
					break;
				}
			}
			
			//���͸� �����ؾ��ϴµ� �ش��ϴ� �ڵ带 ã���� ���ٸ�..����.
			if( leftIdx < 0) return;
			
			VariantValue rightValue = (VariantValue)list.get(2);
			VariantOption rightOption = rightValue.getOption();
			int rightIdx = -1;
			for( int j = 0; j < selectedOptionModel.size(); j++){
				VariantOption sOption = (VariantOption)selectedOptionModel.get(j);
				if( rightOption.equals(sOption)){
					rightIdx = j;
					break;
				}
			}
			
			//���͸� �����ؾ��ϴµ� �ش��ϴ� �ڵ带 ã���� ���ٸ�..����.
			if( rightIdx < 0) return;
			
			//���� �߰� �ʿ�
			//EX) A50=A50E THEN A30=A30S	POSITIVE
			if( leftIdx > rightIdx){
				
				positiveFilterMap.remove(leftValue);
				
				ArrayList negativeList = positiveFilterMap.get(rightValue);
				if( negativeList == null){
					negativeList = new ArrayList();
				}
				
				if( !negativeList.contains(leftValue)){
					negativeList.add(leftValue);
				}
				
				positiveFilterMap.put(rightValue, negativeList);
				
			}
		}
		
		
		//Negative�϶�
		//A�϶� B�� �ƴϴ�.
		Set set = negativeFilterMap.keySet();
		Iterator its = set.iterator();
		while( its.hasNext()){
			
			VariantValue key = (VariantValue)its.next();
			ArrayList<VariantValue> values = negativeFilterMap.get(key);
			HashMap<String, VariantOption> filteredOptions = filteredVariantValues.get(key.getValueName());
			if( filteredOptions == null){
				filteredOptions = new HashMap();
			}
			
			for( VariantValue fValue : values){
				// key �϶��� fValue�� �ƴϴ�.
				
				VariantOption option = fValue.getOption();
				
				//�ش� Ű�� ����� �ɼ��� ������ ��������, ������ Duplicate�Ѵ�. 
				if( filteredOptions.containsValue(option)){
					option = filteredOptions.get(fValue.getOption().getOptionName());
				}else{
					option = option.duplicate();
					ArrayList<VariantValue> list = getEnableValues( option );
					HashMap<String, VariantValue> valueMap = new HashMap();
					for( VariantValue value : list){
						if( !value.equals(fValue)){
							valueMap.put(value.getValueName(), value);
						}
					}
					option.setValues(valueMap);
					
					filteredOptions.put(option.getOptionName(), option);
					filteredVariantValues.put(key.getValueName(), filteredOptions);	
					continue;
				}
				
				ArrayList<VariantValue> storedValues = (ArrayList<VariantValue>)option.getValues();
				HashMap<String, VariantValue> valueMap = new HashMap();
				for( VariantValue tValue : storedValues){
					//fValue�� �ٸ��Ƿ� �߰���.
					if( !tValue.equals(fValue)){
						valueMap.put(tValue.getValueName(), tValue);
						break;
					}
				}
				option.setValues(valueMap);
				filteredOptions.put(option.getOptionName(), option);
				filteredVariantValues.put(key.getValueName(), filteredOptions);	
			}
		}
		
		set = positiveFilterMap.keySet();
		its = set.iterator();
		while( its.hasNext()){
			
			VariantValue key = (VariantValue)its.next();
			ArrayList<VariantValue> values = positiveFilterMap.get(key);
			HashMap<String, VariantOption> filteredOptions = filteredVariantValues.get(key.getValueName());
			if( filteredOptions == null){
				filteredOptions = new HashMap();
			}
			
			for( VariantValue fValue : values){
				// key �϶��� fValue�̴�.
				
				VariantOption option = fValue.getOption();
				
				//�ش� Ű�� ����� �ɼ��� ������ ��������, ������ Duplicate�Ѵ�. 
				if( filteredOptions.containsValue(option)){
					option = filteredOptions.get(fValue.getOption().getOptionName());
				}else{
					option = option.duplicate();
					ArrayList<VariantValue> list = getEnableValues( option );
					HashMap<String, VariantValue> valueMap = new HashMap();
					valueMap.put(fValue.getValueName(), fValue);
					option.setValues(valueMap);
					
					filteredOptions.put(option.getOptionName(), option);
					filteredVariantValues.put(key.getValueName(), filteredOptions);	
					continue;
				}
				
				ArrayList<VariantValue> storedValues = (ArrayList<VariantValue>)option.getValues();
				HashMap<String, VariantValue> valueMap = new HashMap();
				for( VariantValue tValue : storedValues){
					//fValue�� �����Ƿ� �߰���.
					if( tValue.equals(fValue)){
						valueMap.put(tValue.getValueName(), tValue);
						break;
					}
				}
				option.setValues(valueMap);
				filteredOptions.put(option.getOptionName(), option);
				filteredVariantValues.put(key.getValueName(), filteredOptions);	
			}
			
		}
		
	}

	/**
	 * ������  Values�� ������.
	 * 
	 * @param option
	 * @return
	 */
	static ArrayList<VariantValue> getEnableValues(VariantOption option){
		if( option == null) return null;
		
		if( option.hasValues()){
			ArrayList result = new ArrayList();
			ArrayList<VariantValue> list = (ArrayList)option.getValues();
			for( VariantValue value : list){
				if( value.getValueStatus() == VariantValue.VALUE_USE){
					if( !result.contains(value))
						result.add(value);
				}
			}
			
			return result;
		}
		
		return null;
	}
}
