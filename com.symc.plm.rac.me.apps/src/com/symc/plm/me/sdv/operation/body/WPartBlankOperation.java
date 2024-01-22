package com.symc.plm.me.sdv.operation.body;

import java.util.Date;

import com.symc.plm.me.utils.WPartBlankUtility;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.cme.accountabilitycheck.Activator;
import com.teamcenter.rac.cme.accountabilitycheck.services.impl.AccountabilityCheckResultServiceImpl;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCSession;


/**
 * WPartBlankUtility Command의 Operation
 * [NON-SR] [20160219] taeku.jeong  Visualizatoin에 Load된 Part중 W로 Item Id가 시작되는것을 찾아서 Blank 시켜주는 Operation Class
 * @author Taeku
 *
 */
public class WPartBlankOperation extends AbstractAIFOperation {

    private TCSession session;
    private TCComponentUser loginUser ;
    private TCComponentRole loginRole ;
    private TCComponentGroup loginGroup ;
    
    private AccountabilityCheckResultServiceImpl localAccountabilityCheckResultServiceImpl;
    
    @Override
    public void executeOperation() throws Exception {
    	
//    	this.session = (TCSession) Activator.getDefault().getSession();
//		this.loginUser = session.getUser();
//		this.loginRole = session.getRole();
//		this.loginGroup = session.getGroup();
		
        try {
            System.out.println("[" + new Date() + "]" + "WPartBlankUtility Start.");
            
            WPartBlankUtility aWPartBlankUtility = new WPartBlankUtility();
            aWPartBlankUtility.doBlankStartWithItemIdIsW();
            
        } catch(Exception e) {
        	System.out.println("[" + new Date() + "]" + "WPartBlankUtility Exception.");
            e.printStackTrace();
            throw e;
        } finally {
        	// Operation 완료...
        	System.out.println("[" + new Date() + "]" + "WPartBlankUtility Complete.");
        }
		
    }

	
}
