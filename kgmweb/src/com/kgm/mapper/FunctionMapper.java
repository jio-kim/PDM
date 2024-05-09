package com.kgm.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;

/**
 * [SR140724-013][20140725] shcho, Product와 M-Product Function Sync 기능 추가를 위한 Class 신규 생성
 */
public interface FunctionMapper {
	public ArrayList<HashMap<String, String>> serchProductFunction(DataSet ds);
}
