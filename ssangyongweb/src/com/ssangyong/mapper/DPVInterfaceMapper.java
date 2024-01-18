package com.ssangyong.mapper;

import java.util.ArrayList;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dto.DPVInterfaceData;

public interface DPVInterfaceMapper {

	public ArrayList<DPVInterfaceData> searchTargetReports(DataSet ds);

	public void updateInterfaceInfo(DataSet ds);

	public String getItemRevisionPuid(DataSet ds);

	public void updatePartProperty(DataSet ds);
}
