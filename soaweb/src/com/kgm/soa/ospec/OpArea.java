package com.kgm.soa.ospec;


/**
 * @author slobbie_vm
 * EX)O/Spec���� DOM(LHD), EU(LHD/RHD), GEN(LHD/RHD)									
 */
public class OpArea {
	protected String area;
//	private ArrayList children = new ArrayList();
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
//	public ArrayList getChildren() {
//		return children;
//	}
	@Override
	public String toString() {
		return area;
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
	protected Object clone() throws CloneNotSupportedException {
		OpArea opArea = new OpArea();
		opArea.setArea(area);
		return opArea;
	}
	
}
