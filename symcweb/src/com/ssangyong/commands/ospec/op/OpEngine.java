package com.ssangyong.commands.ospec.op;

/**
 * @author slobbie_vm
 * EX)O/Spec���� D20DTR	D27DT	D27DTP		G32D
 */
public class OpEngine extends OpPassenger {
	protected String engine;

	public String getEngine() {
		return engine;
	}

	public void setEngine(String engine) {
		this.engine = engine;
	}

	@Override
	public String toString() {
		return super.toString() + "_" + engine;
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
		OpEngine opEngine = new OpEngine();
		opEngine.setArea(area);
		opEngine.setPassenger(passenger);
		opEngine.setEngine(engine);
		
		return opEngine;
	}
	
	
}
