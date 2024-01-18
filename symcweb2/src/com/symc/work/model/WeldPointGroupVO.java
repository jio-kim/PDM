package com.symc.work.model;

/**
 * [차체 Function 용접점 관리 방안] [20150907][ymjang] X100 이외의 차종은 별도의 용접점 관리 Part를 생성하여 관리하고, X100 의 경우만, 기존의 용접점 대상 추출 로직을 그대로 적용한다.
 * [20151016][ymjang] 추가된 항목 eqaul 함수에 추가함.
 */
public class WeldPointGroupVO {

	private String ecoNo = null;	//ECO NO
	private String fmpId = null;	//FMP ID
	private String parentId = null;	//Weld Point Group시 참조될 아이템 아이디.
	private String parentRevId = null;//Weld Point Group시 참조될 아이템 리비전 아이디.
	private String itemId = null;	//Weld Point Group시 참조될 아이템 아이디.
	private String itemRevId = null;//Weld Point Group시 참조될 아이템 리비전 아이디.
	private String changeType = null;//Change Type D, R0, F0....
	private String eplId = null;	// ECO_BOM_LIST의 epl_id
	private String projectCode = null;
	
	// 변환 요청시 변환 실패에 관한 에러 로그를 저장 하기 위한 변수 추가 
	private String transFailReason = null;
	
	/**
	 * @return the transFailReason
	 */
	public String getTransFailReason() {
		return transFailReason;
	}
	/**
	 * @param transFailReason the transFailReason to set
	 */
	public void setTransFailReason(String transFailReason) {
		this.transFailReason = transFailReason;
	}
	// 용접점 변환 개선안
	// 미변환 용점점을 추가 적으로 변환 시킴을 알 수 있는 Flag 추가
	private boolean notTransformFlag = false;

	/**
	 * @return the notTransformFlag
	 */
	public boolean isNotTransformFlag() {
		return notTransformFlag;
	}
	/**
	 * @param notTransformFlag the notTransformFlag to set
	 */
	public void setNotTransformFlag(boolean notTransformFlag) {
		this.notTransformFlag = notTransformFlag;
	}
	public String getProjectCode() {
		if( projectCode == null){
			return "";
		}
		return projectCode;
	}
	public void setProjectCode(String projectCode) {
		this.projectCode = projectCode;
	}
	public String getEcoNo() {
		if( ecoNo == null){
			return "";
		}
		return ecoNo;
	}
	public void setEcoNo(String ecoNo) {
		this.ecoNo = ecoNo;
	}
	public String getFmpId() {
		if( fmpId == null){
			return "";
		}
		return fmpId;
	}
	public void setFmpId(String fmpId) {
		this.fmpId = fmpId;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public String getParentRevId() {
		return parentRevId;
	}
	public void setParentRevId(String parentRevId) {
		this.parentRevId = parentRevId;
	}
	public String getItemId() {
		if( itemId == null){
			return "";
		}
		return itemId;
	}
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	public String getItemRevId() {
		if( itemRevId == null){
			return "";
		}
		return itemRevId;
	}
	public void setItemRevId(String itemRevId) {
		this.itemRevId = itemRevId;
	}
	public String getChangeType() {
		if( changeType == null){
			return "";
		}
		return changeType;
	}
	public void setChangeType(String changeType) {
		this.changeType = changeType;
	}
	public String getEplId() {
		if( eplId == null){
			return "";
		}
		return eplId;
	}
	public void setEplId(String eplId) {
		this.eplId = eplId;
	}
	@Override
	public boolean equals(Object obj) {
		if( obj instanceof WeldPointGroupVO){

			WeldPointGroupVO comp = (WeldPointGroupVO)obj;

			if( !getEcoNo().equals(comp.getEcoNo())){
				return false;
			}

			if( !getChangeType().equals(comp.getChangeType())){
				return false;
			}

			if( !getEplId().equals(comp.getEplId())){
				return false;
			}

			if( !getFmpId().equals(comp.getFmpId())){
				return false;
			}

			if( !getItemId().equals(comp.getItemId())){
				return false;
			}

			if( !getItemRevId().equals(comp.getItemRevId())){
				return false;
			}

			if( !getProjectCode().equals(comp.getProjectCode())){
				return false;
			}
			
			if( !getParentId().equals(comp.getParentId())){
				return false;
			}
			
			if( !getParentRevId().equals(comp.getParentRevId())){
				return false;
			}
			
			if( !getEplId().equals(comp.getEplId())){
				return false;
			}
			
			if( !getProjectCode().equals(comp.getProjectCode())){
				return false;
			}
			return true;
		}
		return super.equals(obj);
	}


}
