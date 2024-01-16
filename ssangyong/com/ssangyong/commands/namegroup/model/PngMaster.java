package com.ssangyong.commands.namegroup.model;

import java.util.ArrayList;
import java.util.Collections;

public class PngMaster {
	
	private boolean isChanged = false; 
	private String groupID = null;
	private String groupName = null;
	private boolean isEnable = false;
	private int defaultQuantity = 1;
	private String refFunctions = "";
	private String description = "";
	private ArrayList<String> partNameList = null;
	private ArrayList<PngCondition> conditionList = null;
	
	public PngMaster(String groupID, String groupName){
		this.groupID = groupID;
		this.groupName = groupName;
	}
	public boolean isChanged() {
		return isChanged;
	}
	public void setChanged(boolean isChanged) {
		this.isChanged = isChanged;
	}
	public String getGroupID() {
		return groupID;
	}
	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public boolean isEnable() {
		return isEnable;
	}
	public void setEnable(boolean isEnable) {
		this.isEnable = isEnable;
	}
	public int getDefaultQuantity() {
		return defaultQuantity;
	}
	public void setDefaultQuantity(int defaultQuantity) {
		this.defaultQuantity = defaultQuantity;
	}
	public String getRefFunctions() {
		return refFunctions;
	}
	public void setRefFunctions(String refFunctions) {
		this.refFunctions = refFunctions;
	}
	
	/**
	 * [SR150416-025][2015.05.27][jclee] Description 컬럼 추가
	 */
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public ArrayList<String> getPartNameList() {
		return partNameList;
	}
	public void setPartNameList(ArrayList<String> partNameList) {
		this.partNameList = partNameList;
	}

	public ArrayList<PngCondition> getConditionList() {
		Collections.sort(conditionList);
		return conditionList;
	}
	public void setConditionList(ArrayList<PngCondition> conditionList) {
		this.conditionList = conditionList;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return groupID;
	}
	
}
