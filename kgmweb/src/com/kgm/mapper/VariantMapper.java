package com.kgm.mapper;

import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;

/**
 * [NON-SR] [20150609] [ymjang] Base Spec IF 데이터 생성
 */
@SuppressWarnings("rawtypes")
public interface VariantMapper {
	public HashMap getItem(DataSet ds);
	public int insertVariantValueDesc(DataSet ds);
	public int updateVariantValueDesc(DataSet ds);
	public List getVariantValueDesc(DataSet ds);
	public int getUsedCount(DataSet ds);
	public List getUsedOptions(DataSet ds);
	public int insertOsiInfo(DataSet ds);
	public int deleteOsiInfo(DataSet ds);
	// [NON-SR] [20150609] [ymjang] Base Spec IF 데이터 생성
	public int insertBSpecInfo(DataSet ds);
	// [NON-SR] [20150609] [ymjang] Base Spec IF 데이터 삭제
	public int deleteBSpecInfo(DataSet ds);
	public List getBuildSpecList(DataSet ds);
	public List getLocalBuildSpecList(DataSet ds);
	public List getBuildSpecInfo(DataSet ds);
	public int insertBuidSpecList(DataSet ds);
	public int deleteBuildSpecList(DataSet ds);
	public String getNewId(DataSet ds);
	// 보류
	// public String getNextId(DataSet ds);
	public List getProjectCodes();
	public List getValidationInfoList(DataSet ds);
	public int insertValidationInfo(DataSet ds);
	public int deleteValidationInfo(DataSet ds);
	public List getSpecOptions(DataSet ds);
	public List getMinusInfo(DataSet ds);
	// [SR없음][20150910][jclee] OSpec Trim
	public List<HashMap<String, String>> selectOSpecTrim(DataSet ds);
	public void deleteOSpecTrim(DataSet ds);
	public int insertOSpecTrim(DataSet ds);
}
