package com.ssangyong.commands.namegroup.model;

import javax.swing.JTable;

public class PngWeeklyReportData {
	private String productNo = null;
	private String groupId = null;
	private String specNo = null;
	private String fromDate = null;
	private String endDate = null;
	private JTable resultTable = null;

	public PngWeeklyReportData() {

	}

	/**
	 * @return the productNo
	 */
	public String getProductNo() {
		return productNo;
	}

	/**
	 * @param productNo
	 *            the productNo to set
	 */
	public void setProductNo(String productNo) {
		this.productNo = productNo;
	}

	/**
	 * @return the groupId
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId
	 *            the groupId to set
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * @return the specNo
	 */
	public String getSpecNo() {
		return specNo;
	}

	/**
	 * @param specNo
	 *            the specNo to set
	 */
	public void setSpecNo(String specNo) {
		this.specNo = specNo;
	}

	/**
	 * @return the fromDate
	 */
	public String getFromDate() {
		return fromDate;
	}

	/**
	 * @param fromDate
	 *            the fromDate to set
	 */
	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	/**
	 * @return the endDate
	 */
	public String getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate
	 *            the endDate to set
	 */
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the resultTable
	 */
	public JTable getResultTable() {
		return resultTable;
	}

	/**
	 * @param resultTable
	 *            the resultTable to set
	 */
	public void setResultTable(JTable resultTable) {
		this.resultTable = resultTable;
	}

}
