package com.ssangyong.commands.ospec.op;

/**
 * @author slobbie_vm
 * EX) O/Spec���� STD	DLX	H/DLX
 */
public class OpGrade extends OpEngine{
	protected String grade;

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	@Override
	public String toString() {
		return super.toString() + "_" + grade;
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
		OpGrade opGrade = new OpGrade();
		opGrade.setArea(area);
		opGrade.setPassenger(passenger);
		opGrade.setEngine(engine);
		opGrade.setGrade(grade);
		return opGrade;
	}
	
}
