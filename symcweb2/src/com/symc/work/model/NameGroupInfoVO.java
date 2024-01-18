package com.symc.work.model;

import java.util.ArrayList;
import java.util.Collections;

public class NameGroupInfoVO {
	private boolean isChanged = false; 
	private String sGroupID = null;
	private String sGroupName = null;
	private boolean isEnable = false;
	private int iDefaultQuantity = 1;
	private String sRefFunctions = "";
	private String sDescription = "";
	private ArrayList<String> alPartNameList = null;
	private ArrayList<NameGroupConditionVO> alConditionList = null;
	
	public NameGroupInfoVO(String sGroupID, String sGroupName){
		this.sGroupID = sGroupID;
		this.sGroupName = sGroupName;
	}
	
	public boolean isChanged() {
		return isChanged;
	}
	
	public void setChanged(boolean isChanged) {
		this.isChanged = isChanged;
	}
	
	public String getGroupID() {
		return sGroupID;
	}
	
	public void setGroupID(String sGroupID) {
		this.sGroupID = sGroupID;
	}
	
	public String getGroupName() {
		return sGroupName;
	}
	
	public void setGroupName(String sGroupName) {
		this.sGroupName = sGroupName;
	}
	
	public boolean isEnable() {
		return isEnable;
	}
	
	public void setEnable(boolean isEnable) {
		this.isEnable = isEnable;
	}
	
	public int getDefaultQuantity() {
		return iDefaultQuantity;
	}
	
	public void setDefaultQuantity(int iDefaultQuantity) {
		this.iDefaultQuantity = iDefaultQuantity;
	}
	
	public String getRefFunctions() {
		return sRefFunctions;
	}
	
	public void setRefFunctions(String sRefFunctions) {
		this.sRefFunctions = sRefFunctions;
	}
	
	public String getDescription() {
		return sDescription;
	}
	
	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}
	
	public ArrayList<String> getPartNameList() {
		return alPartNameList;
	}
	
	public void setPartNameList(ArrayList<String> alPartNameList) {
		this.alPartNameList = alPartNameList;
	}

	public ArrayList<NameGroupConditionVO> getConditionList() {
		if (alConditionList == null || alConditionList.size() == 0) {
			return null;
		}
		
		Collections.sort(alConditionList);
		return alConditionList;
	}
	
	public void setConditionList(ArrayList<NameGroupConditionVO> alConditionList) {
		this.alConditionList = alConditionList;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return sGroupID;
	}
	
}
