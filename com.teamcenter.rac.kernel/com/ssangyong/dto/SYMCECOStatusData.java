package com.ssangyong.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SYMCECOStatusData implements Serializable  {
	private String ecrNo;
	private String eciNo;
	private String eciStatus;
	private String ecoNo;
	private String ecoTitle;
	private String ecoStatus;
	private String owningTeam;
	private String owningUser;
	private String createDate;
	private String createDateFrom;
	private String createDateTo;
	private String requestDate;
	private String requestDateFrom;
	private String requestDateTo;
	private String releaseDateFrom;
	private String releaseDateTo;
	private String realseDate;
	public String getEcrNo() {
		return ecrNo;
	}
	public void setEcrNo(String ecrNo) {
		this.ecrNo = ecrNo;
	}
	public String getEciNo() {
		return eciNo;
	}
	public void setEciNo(String eciNo) {
		this.eciNo = eciNo;
	}
	public String getEciStatus() {
		return eciStatus;
	}
	public void setEciStatus(String eciStatus) {
		this.eciStatus = eciStatus;
	}
	public String getEcoNo() {
		return ecoNo;
	}
	public void setEcoNo(String ecoNo) {
		this.ecoNo = ecoNo;
	}
	public String getEcoTitle() {
		return ecoTitle;
	}
	public void setEcoTitle(String ecoTitle) {
		this.ecoTitle = ecoTitle;
	}
	public String getEcoStatus() {
		return ecoStatus;
	}
	public void setEcoStatus(String ecoStatus) {
		this.ecoStatus = ecoStatus;
	}
	public String getOwningTeam() {
		return owningTeam;
	}
	public void setOwningTeam(String owningTeam) {
		this.owningTeam = owningTeam;
	}
	public String getOwningUser() {
		return owningUser;
	}
	public void setOwningUser(String owningUser) {
		this.owningUser = owningUser;
	}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public String getCreateDateFrom() {
		return createDateFrom;
	}
	public void setCreateDateFrom(String createDateFrom) {
		this.createDateFrom = createDateFrom;
	}
	public String getCreateDateTo() {
		return createDateTo;
	}
	public void setCreateDateTo(String createDateTo) {
		this.createDateTo = createDateTo;
	}
	public String getRequestDate() {
		return requestDate;
	}
	public void setRequestDate(String requestDate) {
		this.requestDate = requestDate;
	}
	public String getRequestDateFrom() {
		return requestDateFrom;
	}
	public void setRequestDateFrom(String requestDateFrom) {
		this.requestDateFrom = requestDateFrom;
	}
	public String getRequestDateTo() {
		return requestDateTo;
	}
	public void setRequestDateTo(String requestDateTo) {
		this.requestDateTo = requestDateTo;
	}
	public String getRealseDate() {
		return realseDate;
	}
	public void setRealseDate(String realseDate) {
		this.realseDate = realseDate;
	}
	public String getReleaseDateFrom() {
		return releaseDateFrom;
	}
	public void setReleaseDateFrom(String releaseDateFrom) {
		this.releaseDateFrom = releaseDateFrom;
	}
	public String getReleaseDateTo() {
		return releaseDateTo;
	}
	public void setReleaseDateTo(String releaseDateTo) {
		this.releaseDateTo = releaseDateTo;
	}
	
}
