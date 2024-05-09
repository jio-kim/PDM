package com.kgm.commands.ospec.op;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class OSpec {
	
	private String ospecNo = "";
	private String gModel = "";
	private String project = "";
	private String releasedDate = "";
	private String version = "";
	
	private HashMap<String, OpTrim> trims = null;
	private HashMap<String, ArrayList<Option>> options = null;	// Trim <==> Options
	private HashMap<String, HashMap<String, OpCategory>> category = null;	// Trim <==> Category
	private ArrayList<OpValueName> opNameList = null;
	private HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> packageMap = null; //<Trim, <Package_Name, <category, options>>>
	private HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> driveTypeMap = null; //<Trim, <Package_Name, <category, options>>>
	
	public OSpec(String gModel, String oSpecNo, String releasedDate){
		this.ospecNo = oSpecNo;
		this.releasedDate = releasedDate;
		
		int tmpIdx = oSpecNo.indexOf("-", 4);
		this.project = oSpecNo.substring(4, tmpIdx);
		this.gModel = gModel;
		this.version = oSpecNo.substring(tmpIdx + 1);
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getOspecNo() {
		return ospecNo;
	}

	public void setOspecNo(String ospecNo) {
		this.ospecNo = ospecNo;
	}

	public String getgModel() {
		return gModel;
	}

	public void setgModel(String gModel) {
		this.gModel = gModel;
	}

	public String getReleasedDate() {
		return releasedDate;
	}

	public void setReleasedDate(String releasedDate) {
		this.releasedDate = releasedDate;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public HashMap<String, OpTrim> getTrims() {
		return trims;
	}

	public void setTrims(HashMap<String, OpTrim> trims) {
		this.trims = trims;
	}
	
    public ArrayList<OpTrim> getTrimList(){
		if( trims == null ) return null;
		ArrayList list = new ArrayList(trims.values());
		Collections.sort(list);
		return list;
	}

	public HashMap<String, ArrayList<Option>> getOptions() {
		return options;
	}

	public void setOptions(HashMap<String, ArrayList<Option>> options) {
		this.options = options;
	}
	 
	public ArrayList<Option> getOptionList(){
		if( options == null ) return null;

		ArrayList tmpList = new ArrayList();
		for(ArrayList<Option> opList : options.values()){
			tmpList.addAll(opList);
		}
		
		ArrayList<Option> list = new ArrayList(tmpList);
		Collections.sort(list);
		return list;
	}

	public ArrayList<OpValueName> getOpNameList() {
		Collections.sort(opNameList);
		return opNameList;
	}

	public void setOpNameList(ArrayList<OpValueName> opNameList) {
		this.opNameList = opNameList;
	}
	
	public HashMap<String, HashMap<String, OpCategory>> getCategory() {
		return category;
	}

	public void setCategory(HashMap<String, HashMap<String, OpCategory>> category) {
		this.category = category;
	}

	public HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> getPackageMap() {
		return packageMap;
	}

	public void setPackageMap(
			HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> packageMap) {
		this.packageMap = packageMap;
	}

	public HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> getDriveTypeMap() {
		return driveTypeMap;
	}

	public void setDriveTypeMap(
			HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> driveTypeMap) {
		this.driveTypeMap = driveTypeMap;
	}

	public ArrayList<OpValueName> getLaterOpNameList(OpValueName opValueName) {
		
		ArrayList<OpValueName> laterList = new ArrayList();
		ArrayList<OpValueName> list = getOpNameList();
		for( OpValueName opValue : list){
			if( opValueName.compareTo(opValue) > 0){
				laterList.add(opValue);
			}
		}
		return laterList;
	}

	public Object clone() throws CloneNotSupportedException {
		OSpec ospec = new OSpec(gModel, ospecNo, releasedDate);
		
		if( trims != null){
			HashMap<String, OpTrim> trims = new HashMap();
			Iterator<String> its = this.trims.keySet().iterator();
			while(its.hasNext()){
				String key = its.next();
				trims.put(key, (OpTrim)this.trims.get(key).clone());
			}
			ospec.setTrims(trims);
		}
		
		if( options != null ){
			
			HashMap<String, HashMap<String, OpCategory>> category = new HashMap();
			ospec.setCategory(category);
			
			HashMap<String, ArrayList<Option>> options = new HashMap();
			Iterator<String> its = this.options.keySet().iterator();
			while(its.hasNext()){
				String key = its.next();
				
				HashMap<String, OpCategory> categoryMap = category.get(key);
				ArrayList<Option> newList = new ArrayList();
				ArrayList<Option> list = (ArrayList<Option>)this.options.get(key);
				for( int i = 0; list!=null && i < list.size(); i++){
					Option option = (Option)list.get(i).clone();
					newList.add(option);
					
					if( categoryMap == null){
						categoryMap = new HashMap();
						ArrayList<Option> opValueList = new ArrayList();
						opValueList.add(option);
						OpCategory opCategory = new OpCategory(option.getOp(), option.getOpName());
						opCategory.setOpValueList(opValueList);
						categoryMap.put(option.getOp(), opCategory);
						category.put(key, categoryMap);
					}else{
						OpCategory opCategory = categoryMap.get(option.getOp());
						if( opCategory == null){
							ArrayList<Option> opValueList = new ArrayList();
							opValueList.add(option);
							opCategory = new OpCategory(option.getOp(), option.getOpName());
							opCategory.setOpValueList(opValueList);
							categoryMap.put(option.getOp(), opCategory);
						}else{
							if( !opCategory.getOpValueList().contains(option)){
								opCategory.getOpValueList().add(option);
							}
						}
					}
				}
				options.put(key, newList);
			}
			ospec.setOptions(options);
		}
		
		if( opNameList != null ){
			ArrayList<OpValueName> opNameList = new ArrayList();
			for( OpValueName opValueName : this.opNameList){
				opNameList.add((OpValueName)opValueName.clone());
			}
			ospec.setOpNameList(opNameList);
		}
		
		return ospec;
	}
	
}
