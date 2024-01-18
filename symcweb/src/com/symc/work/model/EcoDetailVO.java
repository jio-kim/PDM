package com.symc.work.model;

import java.util.Date;
import java.util.HashMap;

public class EcoDetailVO {

	private String ecoId = null;	//ECO_ID
	private String plant = null;	// Plant
	private String ecoReason = null;	//ECO Reason
	private String changeDesc = null;	//변경 설명
	private String affectedProject = null;	//Affectd Project
	private Date releaseDate = null;
	private HashMap<String, ProjectVO> projectMap = null;	//연관된 Product
	private HashMap<String,ProductInfoVO> productMap = null;	//연관된 Product

	public String getEcoId() {
		return ecoId;
	}
	public void setEcoId(String ecoId) {
		this.ecoId = ecoId;
	}
	public String getPlant() {
		return plant;
	}
	public void setPlant(String plant) {
		this.plant = plant;
	}
	public String getEcoReason() {
		return ecoReason;
	}
	public void setEcoReason(String ecoReason) {
		this.ecoReason = ecoReason;
	}
	public String getChangeDesc() {
		return changeDesc;
	}
	public void setChangeDesc(String changeDesc) {
		this.changeDesc = changeDesc;
	}
	public String getAffectedProject() {
		return affectedProject;
	}
	public void setAffectedProject(String affectedProject) {
		this.affectedProject = affectedProject;
	}
	public Date getReleaseDate() {
		return releaseDate;
	}
	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}
	public HashMap<String, ProjectVO> getProjectMap() {
		return projectMap;
	}
	public void setProjectMap(HashMap<String, ProjectVO> projectMap) {
		this.projectMap = projectMap;
	}
	public HashMap<String, ProductInfoVO> getProductMap() {
		return productMap;
	}
	public void setProductMap(HashMap<String, ProductInfoVO> productMap) {
		this.productMap = productMap;
	}



}
