package com.kgm.mapper;

import java.util.ArrayList;

import com.kgm.common.remote.DataSet;
import com.kgm.dto.DPVInterfaceData;

public interface DPVInterfaceMapper {

	public ArrayList<DPVInterfaceData> searchTargetReports(DataSet ds);

	public void updateInterfaceInfo(DataSet ds);

	public String getItemRevisionPuid(DataSet ds);

	public void updatePartProperty(DataSet ds);
}
