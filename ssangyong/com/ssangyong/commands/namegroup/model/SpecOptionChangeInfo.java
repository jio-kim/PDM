/**
 *[ SR180410-037 ]
 * - 2018-04-19
 * - beenlaho
 */
package com.ssangyong.commands.namegroup.model;

import java.util.HashMap;

import org.apache.poi.ss.usermodel.IndexedColors;

public class SpecOptionChangeInfo {
	
	private String specNo;
	private String version;
	private String options;
	private String flag;
	
	public static final String ADD_FLAG = "A";
	public static final String CHANGE_FLAG = "C";
	public static final String DELETE_FLAG = "D";
	
	public SpecOptionChangeInfo() {
		// TODO Auto-generated constructor stub
	}
	
	public SpecOptionChangeInfo(HashMap<String, String> map) {
		
		this.specNo = map.get("SPEC_NO");
		this.version = map.get("VERSION");
		this.options = map.get("OPTIONS");
		this.flag = map.get("FLAG");
	
	}
	
	/**
	 * @return the specNo
	 */
	public String getSpecNo() {
		return specNo;
	}

	/**
	 * @param specNo the specNo to set
	 */
	public void setSpecNo(String specNo) {
		this.specNo = specNo;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the options
	 */
	public String getOptions() {
		return options;
	}

	/**
	 * @param options the options to set
	 */
	public void setOptions(String options) {
		this.options = options;
	}

	/**
	 * @return the flag
	 */
	public String getFlag() {
		return flag;
	}

	/**
	 * @param flag the flag to set
	 */
	public void setFlag(String flag) {
		this.flag = flag;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		result = prime * result + ((specNo == null) ? 0 : specNo.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SpecOptionChangeInfo other = (SpecOptionChangeInfo) obj;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		if (specNo == null) {
			if (other.specNo != null)
				return false;
		} else if (!specNo.equals(other.specNo))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SpecOptionChangeInfo [specNo=" + specNo + ", version=" + version + ", options=" + options + ", flag=" + flag + "]";
	}
	
	public static short getAddDisplayColor (){
		return IndexedColors.BLUE.getIndex(); 
	}
	
	public static short getDeleteDisplayColor(){
		return IndexedColors.GREY_50_PERCENT.getIndex();
	}
		
	public static short getChangeDisplayColor(){
		return IndexedColors.GREEN.getIndex();
	}
	
	public short getDisplayColor () {
		
		if( this.flag != null ){
			
			if( this.flag.equals(ADD_FLAG) ){
				
				return getAddDisplayColor();
			
			}else if( this.flag.equals(DELETE_FLAG) ){
			
				return getDeleteDisplayColor();
			
			}else if( this.flag.equals(CHANGE_FLAG) ){
			
				return getChangeDisplayColor();
			
			}
		
		}
		
		return IndexedColors.WHITE.getIndex();
	}

}
