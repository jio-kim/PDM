package com.symc.plm.rac.prebom.masterlist.model;

/**
 * [SR160621-031][20160707] taeku.jeong
 * 주간 단위로 생성된 Pre-BOM 데이터를 활용하여 엑셀로 출력할 수 있는 기능 개발
 */
public class MasterAndUssageFindKey {
	
	public String projectCode = null;
	public String masterEAICreateTime = null;
	public String ospecEAICreateTime = null;
	public String ussageEAICreateTime = null;
	public String eaiCreateTime = null;

	/**
	 * Project List Up에 사용될 Data 표현을 위한 Class
	 * @param projectCode
	 * @param eaiCreateTime
	 * @param masterEAICreateTime
	 * @param ospecEAICreateTime
	 * @param ussageEAICreateTime
	 */
	public MasterAndUssageFindKey(String projectCode, String eaiCreateTime, String masterEAICreateTime,  String ospecEAICreateTime, String ussageEAICreateTime){
		this.projectCode = projectCode;
		this.masterEAICreateTime = masterEAICreateTime;
		this.ospecEAICreateTime = ospecEAICreateTime;
		this.ussageEAICreateTime = ussageEAICreateTime;
		this.eaiCreateTime = eaiCreateTime;
	}
}
