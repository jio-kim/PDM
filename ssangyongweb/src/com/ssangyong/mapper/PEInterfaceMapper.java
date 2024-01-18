package com.ssangyong.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;

/**
 * [용점접 2차 개선안] [20150907][ymjang] 용접점 처리시 각 대상 Part 별로 상태를 기록하기 위해 상태 테이블에 데이터를 생성한다.
 */
public interface PEInterfaceMapper {
	
	public  ArrayList<HashMap<String, Object>> getProductEndItemABSOccPuidList(DataSet ds) throws Exception;
	
}
