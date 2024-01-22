package com.symc.plm.rac.prebom.masterlist.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import com.ssangyong.common.utils.variant.VariantOption;
import com.symc.plm.rac.prebom.common.util.OptionManager;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;

public interface MasterListReq {
	public String getProject();
	public ArrayList<String> getEssentialNames();
	public boolean isEditable();
	public TCComponentItemRevision getFmpRevision();
	public HashMap<String, Vector> getKeyRowMapper();
	public HashMap<String, Vector> getReleaseKeyRowMapper();
	public OptionManager getOptionManager();
	public ArrayList<VariantOption> getEnableOptionSet();
	public ArrayList<TCComponentBOMLine> getBOMLines(String systemRowKey); 
	public String getCurrentUserId();
	public String getCurrentUserName();
	public String getCurrentUserGroup();
	public String getCurrentUserPa6Group();
	public boolean isCordinator();
	public HashMap<String, ArrayList<String>> getWorkingChildRowKeys();
}
