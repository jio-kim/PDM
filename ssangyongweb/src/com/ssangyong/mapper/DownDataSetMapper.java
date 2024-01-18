package com.ssangyong.mapper;

import java.util.ArrayList;

import com.ssangyong.common.remote.DataSet;

@SuppressWarnings("rawtypes")
public interface DownDataSetMapper {
	public Integer downDataSetlogInsert(DataSet ds);
	public ArrayList downDataSetlogSelect(DataSet ds); 
	public ArrayList downDataSetLogDateSelect(DataSet ds);
}
