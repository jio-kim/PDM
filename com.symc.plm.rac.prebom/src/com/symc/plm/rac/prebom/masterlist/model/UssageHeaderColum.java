package com.symc.plm.rac.prebom.masterlist.model;

/**
 * [SR160621-031][20160707] taeku.jeong
 * 주간 단위로 생성된 Pre-BOM 데이터를 활용하여 엑셀로 출력할 수 있는 기능 개발
 */
public class UssageHeaderColum {
	
	public String area = null;
	public String passenger = null;
	public String engine = null;
	public String grade = null;
	public String trim = null;
	public String ussageKey = null;

	/**
	 * Ussage 정보를 나타내는 Colum에 표시될 Titl들을 기록하기위한 Data Class의 정의
	 * @param area
	 * @param passenger
	 * @param engine
	 * @param grade
	 * @param trim
	 * @param ussageKey
	 */
	public UssageHeaderColum(String area, String passenger, String engine, String grade, String trim, String ussageKey){
		
		this.area = area;
		this.passenger = passenger;
		this.engine = engine;
		this.grade = grade;
		this.trim = trim;
		this.ussageKey = ussageKey;
		
	}

	@Override
	public String toString() {
		return this.ussageKey;
	}
	
	
}
