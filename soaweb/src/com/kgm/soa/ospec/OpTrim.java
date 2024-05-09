package com.kgm.soa.ospec;

/**
 * @author slobbie_vm
 * EX)O/Spec���� D7KWS	D7KWD	D7KWH ==> TC���� Variant�� �ش�.
 */
@SuppressWarnings("rawtypes")
public class OpTrim extends OpGrade implements Comparable{
	private String trim;
	private int colOrder;

	public int getColOrder() {
		return colOrder;
	}

	public void setColOrder(int colOrder) {
		this.colOrder = colOrder;
	}

	public String getTrim() {
		return trim;
	}

	public void setTrim(String trim) {
		this.trim = trim;
	}

	@Override
	public String toString() {
		return super.toString() + "_" + trim;
	}
	
	@Override
	public boolean equals(Object obj) {
		if( getClass().equals(obj.getClass())){
			return toString().equals(obj.toString());
		}else if( obj instanceof String){
			return toString().equals(obj);
		}else{
			return false;
		}

	}

	@Override
	public int compareTo(Object o) {
		if(o instanceof OpTrim){
			OpTrim target = (OpTrim)o;
			if (colOrder < target.getColOrder() ){
				return -1;
			}else if(colOrder > target.getColOrder()){
				return 1;
			}else{
				return 0;
			}
		}
		return 0;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		OpTrim opTrim = new OpTrim();
		opTrim.setColOrder(colOrder);
		opTrim.setArea(area);
		opTrim.setPassenger(passenger);
		opTrim.setEngine(engine);
		opTrim.setGrade(grade);
		opTrim.setTrim(trim);
		
		return opTrim;
	}
	
	
}
