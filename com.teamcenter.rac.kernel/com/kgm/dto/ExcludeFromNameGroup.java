package com.kgm.dto;

import java.io.Serializable;
import java.sql.Date;

public class ExcludeFromNameGroup implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public ExcludeFromNameGroup() {
		// TODO Auto-generated constructor stub
	}
	
	public static int ADDED_FLAG = 1;
	public static int CHANGED_FLAG = 2;
	public static int DELETED_FLAG = 3;
	
	private int changedFlag;
	private String id;
	private String product;
	private String my;
	private String groupName;
	private String remarks;
	private Date createDate;
	/**
	 * @return the changedFlag
	 */
	public int getChangedFlag() {
		
		return changedFlag;
	}
	/**
	 * @param changedFlag the changedFlag to set
	 */
	public void setChangedFlag(int changedFlag) {
		this.changedFlag = changedFlag;
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the partNo
	 */
	public String getProduct() {
		return product;
	}
	/**
	 * @param partNo the partNo to set
	 */
	public void setProduct(String product) {
		this.product = product;
	}
	/**
	 * @return the partName
	 */
	public String getGroupName() {
		return groupName;
	}
	/**
	 * @param partName the partName to set
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	/**
	 * @return the partName
	 */
	public String getMy() {
		return my;
	}
	/**
	 * @param partName the partName to set
	 */
	public void setMy(String my) {
		this.my = my;
	}
	/**
	 * @return the remarks
	 */
	public String getRemarks() {
		return remarks;
	}
	/**
	 * @param remarks the remarks to set
	 */
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	/**
	 * @return the createDate
	 */
	public Date getCreateDate() {
		return createDate;
	}
	/**
	 * @param createDate the createDate to set
	 */
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ExcludeFromNameGroup [changedFlag=" + changedFlag + ", id=" + id + ", product=" + product + ", my=" + my +", groupName=" + groupName + ", remarks="
				+ remarks + ", createDate=" + createDate + "]";
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		ExcludeFromNameGroup other = (ExcludeFromNameGroup) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	

}
