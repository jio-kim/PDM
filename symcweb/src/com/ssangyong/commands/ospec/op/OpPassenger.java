package com.ssangyong.commands.ospec.op;

/**
 * @author slobbie_vm
 *	EX)O/Spec����  7 PASS		5 PASS				
 */
public class OpPassenger extends OpArea {
	protected String passenger;

	public String getPassenger() {
		return passenger;
	}

	public void setPassenger(String passenger) {
		this.passenger = passenger;
	}

	@Override
	public String toString() {
		return super.toString() + "_" + passenger;
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
		OpPassenger opPassenger = new OpPassenger();
		opPassenger.setArea(area);
		opPassenger.setPassenger(passenger);
		
		return opPassenger;
	}
	
	
}
