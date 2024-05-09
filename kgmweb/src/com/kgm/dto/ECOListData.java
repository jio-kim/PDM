package com.kgm.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ECOListData implements Serializable  {
	private String seq;
	private String plantCode;
	private String ecoNo;
	private String changeReason;
	private String owningTeam;
	private String owningUser;
	private String ownerTel;
	private String approvalUser;
	private String approvalTel;
	private String ecoStatus;
	private String releaseDate;
	private String releaseDateFrom;
	private String releaseDateTo;
	private String ecrNo;
	private String ecrDate;
	private String ecrDept;
	private String eciNo;
	private String eciReleaseDate;
	private String eciDept;
	private String createDate;
	
	private String regNsafe;
	private String cfgEffectPoint;
	private String concurrentImpl;
	private String ecoTitle;
	
	private String requestDate;
	private String requestLT;
	private String costDate;
	private String costLT;
	private String techDate;
	private String techLT;
	private String purcDate;
	private String purcLT;
	private String inApprovalDate;
	private String inApprovalLT;
	private String approvedDate;
	private String approvedLT;
	private String completedDate;
	private String completedLT;
	
	public String getSeq() {
		return seq;
	}
	public void setSeq(String seq) {
		this.seq = seq;
	}
	public String getPlantCode() {
		return plantCode;
	}
	public void setPlantCode(String plantCode) {
		this.plantCode = plantCode;
	}
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
	public String getReleaseDate() {
		return releaseDate;
	}
	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
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
	public String getRequestDate() {
		return requestDate;
	}
	public void setRequestDate(String requestDate) {
		this.requestDate = requestDate;
	}
	public String getChangeReason() {
		return changeReason;
	}
	public void setChangeReason(String changeReason) {
		this.changeReason = changeReason;
	}
	public String getOwnerTel() {
		return ownerTel;
	}
	public void setOwnerTel(String ownerTel) {
		this.ownerTel = ownerTel;
	}
	public String getApprovalUser() {
		return approvalUser;
	}
	public void setApprovalUser(String approvalUser) {
		this.approvalUser = approvalUser;
	}
	public String getApprovalTel() {
		return approvalTel;
	}
	public void setApprovalTel(String approvalTel) {
		this.approvalTel = approvalTel;
	}
	public String getEcrDate() {
		return ecrDate;
	}
	public void setEcrDate(String ecrDate) {
		this.ecrDate = ecrDate;
	}
	public String getEcrDept() {
		return ecrDept;
	}
	public void setEcrDept(String ecrDept) {
		this.ecrDept = ecrDept;
	}
	public String getEciReleaseDate() {
		return eciReleaseDate;
	}
	public void setEciReleaseDate(String eciReleaseDate) {
		this.eciReleaseDate = eciReleaseDate;
	}
	public String getEciDept() {
		return eciDept;
	}
	public void setEciDept(String eciDept) {
		this.eciDept = eciDept;
	}
	public String getRegNsafe() {
		return regNsafe;
	}
	public void setRegNsafe(String regNsafe) {
		this.regNsafe = regNsafe;
	}
	public String getCfgEffectPoint() {
		return cfgEffectPoint;
	}
	public void setCfgEffectPoint(String cfgEffectPoint) {
		this.cfgEffectPoint = cfgEffectPoint;
	}
	public String getConcurrentImpl() {
		return concurrentImpl;
	}
	public void setConcurrentImpl(String concurrentImpl) {
		this.concurrentImpl = concurrentImpl;
	}
	public String getCostDate() {
		return costDate;
	}
	public void setCostDate(String costDate) {
		this.costDate = costDate;
	}
	public String getTechDate() {
		return techDate;
	}
	public void setTechDate(String techDate) {
		this.techDate = techDate;
	}
	public String getPurcDate() {
		return purcDate;
	}
	public void setPurcDate(String purcDate) {
		this.purcDate = purcDate;
	}
	public String getInApprovalDate() {
		return inApprovalDate;
	}
	public void setInApprovalDate(String inApprovalDate) {
		this.inApprovalDate = inApprovalDate;
	}
	public String getApprovedDate() {
		return approvedDate;
	}
	public void setApprovedDate(String approvedDate) {
		this.approvedDate = approvedDate;
	}
	public String getCompletedDate() {
		return completedDate;
	}
	public void setCompletedDate(String completedDate) {
		this.completedDate = completedDate;
	}
	public String getRequestLT() {
		return requestLT;
	}
	public void setRequestLT(String requestLT) {
		this.requestLT = requestLT;
	}
	public String getCostLT() {
		return costLT;
	}
	public void setCostLT(String costLT) {
		this.costLT = costLT;
	}
	public String getTechLT() {
		return techLT;
	}
	public void setTechLT(String techLT) {
		this.techLT = techLT;
	}
	public String getPurcLT() {
		return purcLT;
	}
	public void setPurcLT(String purcLT) {
		this.purcLT = purcLT;
	}
	public String getInApprovalLT() {
		return inApprovalLT;
	}
	public void setInApprovalLT(String inApprovalLT) {
		this.inApprovalLT = inApprovalLT;
	}
	public String getApprovedLT() {
		return approvedLT;
	}
	public void setApprovedLT(String approvedLT) {
		this.approvedLT = approvedLT;
	}
	public String getCompletedLT() {
		return completedLT;
	}
	public void setCompletedLT(String completedLT) {
		this.completedLT = completedLT;
	}
}
