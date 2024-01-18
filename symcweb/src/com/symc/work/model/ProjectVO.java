package com.symc.work.model;

public class ProjectVO {

	private String baseProjectCode = null;
	private String projectId = null;
	private String projectRevId = null;
	private ProductInfoVO product = null;	//Base Project라면 대응하는 Product가 하나 존재한다.(없을 수도 있슴)

	public String getBaseProjectCode() {
		return baseProjectCode;
	}
	public void setBaseProjectCode(String baseProjectCode) {
		this.baseProjectCode = baseProjectCode;
	}
	public String getProjectId() {
		return projectId;
	}
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	public String getProjectRevId() {
		return projectRevId;
	}
	public void setProjectRevId(String projectRevId) {
		this.projectRevId = projectRevId;
	}
	public ProductInfoVO getProduct() {
		return product;
	}
	public void setProduct(ProductInfoVO product) {
		this.product = product;
	}

}
