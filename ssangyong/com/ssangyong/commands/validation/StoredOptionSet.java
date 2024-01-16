package com.ssangyong.commands.validation;


@SuppressWarnings("rawtypes")
public class StoredOptionSet implements Comparable{
	private String name = null;	//Sos Name
	private String puid = null;	//Sos Puid
	
	/**
	 * Stored Option Set °´Ã¼ Á¤ÀÇ
	 * 
	 * @param name
	 * @param puid
	 */
	public StoredOptionSet(String name, String puid){
		this.name = name;
		this.puid = puid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPuid() {
		return puid;
	}

	public void setPuid(String puid) {
		this.puid = puid;
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public int compareTo(Object obj) {
		if( obj instanceof StoredOptionSet){
			StoredOptionSet tSos = (StoredOptionSet)obj;
			return name.compareTo(tSos.getName());
		}
		return 0;
	}

	
}
