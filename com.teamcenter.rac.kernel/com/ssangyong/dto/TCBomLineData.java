package com.ssangyong.dto;

import java.io.Serializable;
import java.sql.Date;

public class TCBomLineData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String functionNo;
	private String systemCode;
	
	private TCPartModel parent;
	private TCPartModel child;
	
	private String seq;
	private String bomLevel;
	private String supplyMode;
	private String orderNo;
	private String options;
	
	private TCEcoModel eco;
	
	private Date createDate;

	/**
	 * @return the parent
	 */
	public TCPartModel getParent() {
		
		if( parent == null )
			parent = new TCPartModel();
		
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(TCPartModel parent) {
		this.parent = parent;
	}

	/**
	 * @return the child
	 */
	public TCPartModel getChild() {
		
		if( child == null )
			child = new TCPartModel();
		
		return child;
	}

	/**
	 * @param child the child to set
	 */
	public void setChild(TCPartModel child) {
		this.child = child;
	}

	/**
	 * @return the seq
	 */
	public String getSeq() {
		return seq;
	}

	/**
	 * @param seq the seq to set
	 */
	public void setSeq(String seq) {
		this.seq = seq;
	}

	/**
	 * @return the bomLevel
	 */
	public String getBomLevel() {
		return bomLevel;
	}

	/**
	 * @param bomLevel the bomLevel to set
	 */
	public void setBomLevel(String bomLevel) {
		this.bomLevel = bomLevel;
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
	 * @return the orderNo
	 */
	public String getOrderNo() {
		return orderNo;
	}

	/**
	 * @param orderNo the orderNo to set
	 */
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
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
	 * @return the eco
	 */
	public TCEcoModel getEco() {
		return eco;
	}

	/**
	 * @param eco the eco to set
	 */
	public void setEco(TCEcoModel eco) {
		this.eco = eco;
	}

	/**
	 * @return the functionNo
	 */
	public String getFunctionNo() {
		return functionNo;
	}

	/**
	 * @param functionNo the functionNo to set
	 */
	public void setFunctionNo(String functionNo) {
		this.functionNo = functionNo;
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
		return "TCBomLineData [functionNo=" + functionNo + ", systemCode=" + systemCode + ", parent=" + parent + ", child=" + child + ", seq=" + seq
				+ ", bomLevel=" + bomLevel + ", supplyMode=" + supplyMode + ", orderNo=" + orderNo + ", options=" + options + ", eco=" + eco + ", createDate="
				+ createDate + "]";
	}
	
	
}
