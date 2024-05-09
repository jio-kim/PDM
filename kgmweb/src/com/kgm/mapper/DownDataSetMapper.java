package com.kgm.mapper;

import java.util.ArrayList;

import com.kgm.common.remote.DataSet;

@SuppressWarnings("rawtypes")
public interface DownDataSetMapper {
	public Integer downDataSetlogInsert(DataSet ds);
	public ArrayList downDataSetlogSelect(DataSet ds); 
	public ArrayList downDataSetLogDateSelect(DataSet ds);
}
