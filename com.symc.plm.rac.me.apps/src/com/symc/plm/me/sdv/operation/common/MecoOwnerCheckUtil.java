package com.symc.plm.me.sdv.operation.common;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentPerson;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class MecoOwnerCheckUtil {
	
	private TCSession session;
	private TCComponentItemRevision mecoRevision;
	
	public MecoOwnerCheckUtil(TCComponentItemRevision mecoRevision, TCSession session){
		
		this.session = session;
		this.mecoRevision = mecoRevision;
	}
	
	public MecoOwnerCheckUtil(String mecoNo, TCSession session){

		this.session = session;
		TCComponentItem mecoItem = null;
		try {
			mecoItem = SDVBOPUtilities.FindItem(mecoNo, SDVTypeConstant.MECO_ITEM);
			if (mecoItem != null) {
				this.mecoRevision = mecoItem.getLatestItemRevision();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public TCComponentItemRevision getOwnedMecoRevision(){
		
		TCComponentItemRevision ownedMecoRevision = null;
		String mecoUserName = null;
		TCComponentUser user = null;
		if(mecoRevision!=null){
			try {
				user = (TCComponentUser) mecoRevision.getReferenceProperty(SDVPropertyConstant.ITEM_OWNING_USER);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		TCComponentUser loginUser = session.getUser();
		if(user!=null && loginUser!=null && loginUser.equals(user)){
			ownedMecoRevision = mecoRevision;
		}
		
		return ownedMecoRevision;
	}

}
