package com.symc.plm.rac.prebom.masterlist.model;

/**
 * [SR160621-031][20160707] taeku.jeong
 * 주간 단위로 생성된 Pre-BOM 데이터를 활용하여 엑셀로 출력할 수 있는 기능 개발
 */
public class UssageDataType {
	
	public String area = null;
	public String passenger = null;
	public String engine = null;
	public String grade = null;
	public String trim = null;
	public String ussageKey = null;
	
	public int ussageQty = 0;
	public String ussageType = null;
	public String qValue = null;

	/**
	 * Ussage Data를 Column의 Header 순서에 맞춰서 표현하기위해 사용될 Data Class
	 * @param area
	 * @param passenger
	 * @param engine
	 * @param grade
	 * @param trim
	 * @param ussageKey
	 * @param ussageQty
	 * @param ussageType
	 * @param qValue
	 */
	public UssageDataType(String area, String passenger, String engine, String grade, String trim, String ussageKey, 
			int ussageQty, String ussageType, String qValue){
		
		this.area = area;
		this.passenger = passenger;
		this.engine = engine;
		this.grade = grade;
		this.trim = trim;
		this.ussageKey = ussageKey;
		
		this.ussageQty = ussageQty;
		this.ussageType = ussageType;
		this.qValue = qValue;
		
	}

	@Override
	public String toString() {
		return this.ussageKey;
	}
	
	
}
