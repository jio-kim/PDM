package com.ssangyong.soa.ospec;

/**
 * @author slobbie_vm
 * EX) O/Spec���� �ɼǰ�.
 */
@SuppressWarnings("rawtypes")
public class Option implements Comparable{
	private String op;
	private String opName;
	private String opValue;
	private String opValueName;
	private String packageName;
	private String driveType;
	private String all;
	private String value;
	private String effIn;
	private String remark;
	
	private OpTrim opTrim;
	
	private String availableCondition = "";
	private String notAvailableCondition = "";
	
	private int colOrder = 0;
	private int rowOrder = 0;
	
	public Option(String op, String opName, String opValue, String opValueName
			, String packageName, String driveType, String all, String value, String effIn, String remark, int colOrder, int rowOrder){
		this.op = op;
		this.opName = opName;
		this.opValue = opValue;
		this.opValueName = opValueName;
		this.packageName = packageName;
		this.driveType = driveType;
		this.all = all;
		this.value = value;
		this.effIn = effIn;
		this.remark = remark != null ? remark.trim():null;
		if( remark != null && !remark.equals("")){
			
			int colonIdx = remark.indexOf(":");
			if(remark.indexOf("NOT Available IF") == (colonIdx + 2)){
				int beginIndex = remark.indexOf("(");
				int endIndex = remark.indexOf("-");
				notAvailableCondition = (remark.substring(beginIndex, endIndex)).trim();
			}else if(remark.indexOf("Available IF") == (colonIdx + 2)){
				int beginIndex = remark.indexOf("(");
				int endIndex = remark.indexOf("-");
				availableCondition = (remark.substring(beginIndex, endIndex)).trim();
			}
		}
		this.colOrder = colOrder;
		this.rowOrder = rowOrder;
	}
	
	public int getColOrder() {
		return colOrder;
	}

	public void setColOrder(int colOrder) {
		this.colOrder = colOrder;
	}

	public int getRowOrder() {
		return rowOrder;
	}

	public void setRowOrder(int rowOrder) {
		this.rowOrder = rowOrder;
	}

	public String getEffIn() {
		return effIn;
	}

	public void setEffIn(String effIn) {
		this.effIn = effIn;
	}

	public String getAll() {
		return all;
	}

	public void setAll(String all) {
		this.all = all;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getAvailableCondition() {
		return availableCondition;
	}
	public void setAvailableCondition(String availableCondition) {
		this.availableCondition = availableCondition;
	}
	public String getNotAvailableCondition() {
		return notAvailableCondition;
	}
	public void setNotAvailableCondition(String notAvailableCondition) {
		this.notAvailableCondition = notAvailableCondition;
	}
	public OpTrim getOpTrim() {
		return opTrim;
	}
	public void setOpTrim(OpTrim opTrim) {
		this.opTrim = opTrim;
	}
	public String getOp() {
		return op;
	}
	public void setOp(String op) {
		this.op = op;
	}
	public String getOpName() {
		return opName;
	}
	public void setOpName(String opName) {
		this.opName = opName;
	}
	public String getOpValue() {
		return opValue;
	}
	public void setOpValue(String opValue) {
		this.opValue = opValue;
	}
	public String getOpValueName() {
		return opValueName;
	}
	public void setOpValueName(String opValueName) {
		this.opValueName = opValueName;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public String getDriveType() {
		return driveType;
	}
	public void setDriveType(String driveType) {
		this.driveType = driveType;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public boolean equals(Object obj) {
//		if( obj instanceof Option){
//			Option option = (Option)obj;
//			return colOrder == option.getColOrder() && rowOrder == option.getRowOrder();
//		}
		return super.equals(obj);
	}

	@Override
	public int compareTo(Object o) {
		if(o instanceof Option){
			Option target = (Option)o;
			if(rowOrder < target.getRowOrder()){
				return -1;
			}else if( rowOrder > target.getRowOrder()){
				return 1;
			}else{
				if (colOrder < target.getColOrder() ){
					return -1;
				}else if(colOrder > target.getColOrder()){
					return 1;
				}else{
					return 0;
				}
			}
			
		}
		
		return 0;
	}

	public Object clone() {
		Option newOption = new Option(op, opName, opValue, opValueName, packageName, driveType, all, value, effIn, remark, colOrder, rowOrder);
		newOption.setOpTrim(this.opTrim);
		return newOption;
	}

}
