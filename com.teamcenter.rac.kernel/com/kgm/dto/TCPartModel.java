package com.kgm.dto;

import java.io.Serializable;

public class TCPartModel implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String puid;
	private String partNo;
	private String version;
	private String partName;
	private String projectNo;
	private String partType;
	private String origin;
	private String sel;
	private String designCode;
	private String unit;
	private String systemCode;
	private String supplyMode;
	private int sequence;
	private TCEcoModel ecoModel;
	/**
	 * @return the puid
	 */
	public String getPuid() {
		return puid;
	}
	/**
	 * @param puid the puid to set
	 */
	public void setPuid(String puid) {
		this.puid = puid;
	}
	/**
	 * @return the partNo
	 */
	public String getPartNo() {
		return partNo;
	}
	/**
	 * @param partNo the partNo to set
	 */
	public void setPartNo(String partNo) {
		this.partNo = partNo;
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
	 * @return the partName
	 */
	public String getPartName() {
		return partName;
	}
	/**
	 * @param partName the partName to set
	 */
	public void setPartName(String partName) {
		this.partName = partName;
	}
	/**
	 * @return the projectNo
	 */
	public String getProjectNo() {
		return projectNo;
	}
	/**
	 * @param projectNo the projectNo to set
	 */
	public void setProjectNo(String projectNo) {
		this.projectNo = projectNo;
	}
	/**
	 * @return the partType
	 */
	public String getPartType() {
		return partType;
	}
	/**
	 * @param partType the partType to set
	 */
	public void setPartType(String partType) {
		this.partType = partType;
	}
	/**
	 * @return the origin
	 */
	public String getOrigin() {
		return origin;
	}
	/**
	 * @param origin the origin to set
	 */
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	/**
	 * @return the sel
	 */
	public String getSel() {
		return sel;
	}
	/**
	 * @param sel the sel to set
	 */
	public void setSel(String sel) {
		this.sel = sel;
	}
	/**
	 * @return the designCode
	 */
	public String getDesignCode() {
		return designCode;
	}
	/**
	 * @param designCode the designCode to set
	 */
	public void setDesignCode(String designCode) {
		this.designCode = designCode;
	}
	/**
	 * @return the unit
	 */
	public String getUnit() {
		return unit;
	}
	/**
	 * @param unit the unit to set
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}
	/**
	 * @return the systemCode
	 */
	public String getSystemCode() {
		return systemCode;
	}
	/**
	 * @param systemCode the systemCode to set
	 */
	public void setSystemCode(String systemCode) {
		this.systemCode = systemCode;
	}
	/**
	 * @return the supplyMode
	 */
	public String getSupplyMode() {
		return supplyMode;
	}
	/**
	 * @param supplyMode the supplyMode to set
	 */
	public void setSupplyMode(String supplyMode) {
		this.supplyMode = supplyMode;
	}
	/**
	 * @return the sequence
	 */
	public int getSequence() {
		return sequence;
	}
	/**
	 * @param sequence the sequence to set
	 */
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	/**
	 * @return the ecoModel
	 */
	public TCEcoModel getEcoModel() {
		return ecoModel;
	}
	/**
	 * @param ecoModel the ecoModel to set
	 */
	public void setEcoModel(TCEcoModel ecoModel) {
		this.ecoModel = ecoModel;
	}
	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	
	
	
	@Override
	public String toString() {
		return "TCPartModel [puid=" + puid + ", partNo=" + partNo + ", version=" + version + ", partName=" + partName + ", projectNo=" + projectNo
				+ ", partType=" + partType + ", origin=" + origin + ", sel=" + sel + ", designCode=" + designCode + ", unit=" + unit + ", systemCode="
				+ systemCode + ", supplyMode=" + supplyMode + ", sequence=" + sequence + ", ecoModel=" + ecoModel + "]";
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((puid == null) ? 0 : puid.hashCode());
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
		TCPartModel other = (TCPartModel) obj;
		if (puid == null) {
			if (other.puid != null)
				return false;
		} else if (!puid.equals(other.puid))
			return false;
		return true;
	}
	
	
	
}
