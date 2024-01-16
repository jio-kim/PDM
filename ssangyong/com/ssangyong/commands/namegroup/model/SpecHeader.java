package com.ssangyong.commands.namegroup.model;

public class SpecHeader {
	
	private String spec = null;
	private String puid = null;
	
	public SpecHeader(String spec, String puid){
		this.spec = spec;
		this.puid = puid;
	}
	
	public String getKey(){
		return spec + "_" + puid;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return spec;
	}
	
	
}
