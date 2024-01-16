package com.ssangyong.commands.bomcompare;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentEvent;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponentEventListener;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.pse.PSEApplicationPanel;
import com.teamcenter.rac.pse.common.BOMPanel;
import com.teamcenter.rac.pse.operations.ExpandBelowOperation;
import com.teamcenter.rac.util.MessageBox;

public class BOMCompareCommand extends AbstractAIFCommand {

	public BOMCompareCommand() {
	}

	protected void executeCommand() throws Exception {
	    
	    AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
	    PSEApplicationPanel psePanel = null;
	    if(application.getApplicationPanel() instanceof PSEApplicationPanel) {
	        psePanel = (PSEApplicationPanel)application.getApplicationPanel();
	    }
	    // Top 이 Function 인지 Check
        final BOMPanel leftPanel = psePanel.getLeftBOMPanel();
        TCComponentBOMLine leftTopLine = leftPanel.getBOMWindow() == null ? null : leftPanel.getBOMWindow().getTopBOMLine();
        String topLineType = leftTopLine == null ? null : leftTopLine.getProperty("bl_item_object_type");
        if(topLineType == null || !topLineType.equals("Function")) {
            MessageBox.post(application.getDesktop().getShell(), "BOM top must be Function.", "BOM Compare", MessageBox.INFORMATION);
            return;
        }
	    BOMPanel rightPanel = psePanel.getRightBOMPanel();
	    
	    // Revision Rule Check
        final TCSession session = leftTopLine.getSession();
        TCComponentRevisionRule latestWorkingRule = null;
        TCComponentRevisionRule latestReleasedRule = null;
        TCComponentRevisionRule[] allRevisionRules = TCComponentRevisionRule.listAllRules(session);
        for(TCComponentRevisionRule revisionRule : allRevisionRules) {
            String ruleName = revisionRule.getProperty("object_name");
            if(ruleName.equals("Latest Released_revision_rule")) {
                latestReleasedRule = revisionRule;
                continue;
            }
            if(ruleName.equals("Latest Working")) {
                latestWorkingRule = revisionRule;
                continue;
            }
        }
        if(latestWorkingRule == null || latestReleasedRule == null) {
            MessageBox.post(application.getDesktop().getShell(), "BOM Revision Rule(Any Status; No Working, Latest Working)을 찾을 수 없습니다.", "BOM Compare", MessageBox.INFORMATION);
            return;
        }
	    
        // BOM Panel 분할
	    if(rightPanel == null) {
	        psePanel.zoomBOMPanel(leftPanel);
	        rightPanel = psePanel.getRightBOMPanel();
	    } else {
	        // 열려있으면 다시 분할
    	    if(rightPanel.getBOMWindow() != null) {
    	        if(!rightPanel.getBOMWindow().getTopBOMLine().getItem().equals(leftTopLine.getItem())) {
    	            psePanel.zoomBOMPanel(leftPanel);
    	            psePanel.zoomBOMPanel(leftPanel);
    	            rightPanel = psePanel.getRightBOMPanel();
    	        }
    	    }
	    }
	    
	    // Left No Working
        if(!leftPanel.getRevisionRule().equals(latestReleasedRule)) {
            leftPanel.setRevisionRule(latestReleasedRule);
            session.addAIFComponentEventListener(new InterfaceAIFComponentEventListener() {
                public void processComponentEvents(AIFComponentEvent[] events) {
                    for(AIFComponentEvent ace : events) {
                        if(ace.getComponent().equals(leftPanel.getBOMWindow())) {
                            for(String eventName : ace.getEventStrings()) {
                                if(eventName.equals("revision_rule")) {
                                    session.queueOperation(new ExpandBelowOperation(leftPanel.getTreeTable(), 1, true));
                                    session.removeAIFComponentEventListener(this);
                                }
                            }
                        }
                    }
                    
                }
            });
        }
        
        // Right Latest Working
        if(rightPanel.getBOMWindow() != null) {
            if(!rightPanel.getRevisionRule().equals(latestWorkingRule)) {
                final BOMPanel newRightPanel = rightPanel;
                rightPanel.setRevisionRule(latestWorkingRule);
                session.addAIFComponentEventListener(new InterfaceAIFComponentEventListener() {
                    public void processComponentEvents(AIFComponentEvent[] events) {
                        for(AIFComponentEvent ace : events) {
                            if(ace.getComponent().equals(newRightPanel.getBOMWindow())) {
                                for(String eventName : ace.getEventStrings()) {
                                    if(eventName.equals("revision_rule")) {
                                        session.queueOperation(new ExpandBelowOperation(newRightPanel.getTreeTable(), 1, true));
                                        session.removeAIFComponentEventListener(this);
                                    }
                                }
                            }
                        }
                        
                    }
                });
            }
        } else {
            rightPanel.setRevisionRule(latestWorkingRule);
            rightPanel.open(leftTopLine.getItem(), latestWorkingRule, null);
            while(rightPanel.getTreeTable().getRowCount() == 0) {
                Thread.sleep(500);
            }
            session.queueOperation(new ExpandBelowOperation(rightPanel.getTreeTable(), 1, true));
        }
	}
	
	// CFM_date_info migration 
    /*AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
    TCSession session = (TCSession)application.getSession();
    TCClassService localTCClassService = session.getClassService();
    
    SYMCRemoteUtil remote = new SYMCRemoteUtil();
    ArrayList<HashMap<String, String>> result = (ArrayList<HashMap<String, String>>)remote.execute("com.ssangyong.service.ECOHistoryService", "selectReleasedECO", new DataSet());
    for(HashMap<String, String> ecoInfo : result) {
        String ecoNo = ecoInfo.get("ECO_ID");
        String ecoUid = ecoInfo.get("ECO_UID");
        
        TCComponent[] effComps = localTCClassService.findByClass("CFM_date_info", "id", ecoNo);
        if ((effComps != null) && (effComps.length > 0)) {
            System.out.println(ecoNo + " effectivity already created!");
            continue;
        }
        
        TCComponent ecoIR = session.stringToComponent(ecoUid);
        Date releaseDate = ecoIR.getDateProperty("date_released");

        Date adate[] = new Date[1];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String stringReleasDate = sdf.format(releaseDate);
        // FIXED, 2013.05.15, DJKIM, "ECO Released 날짜의 00:00:00 초"에서 "ECO Released 날짜의 23:59:59 초"로 변경
        adate[0] = sdf.parse(stringReleasDate);
        adate[0].setTime(sdf.parse(stringReleasDate).getTime() + (( (long) 1000 * 60 * 60 * 24 )-1000) );
        
        // FIXED, 2013.05.15, DJKIM, SOA로 TCComponentOccEffectivity를 setting 하기 위해 객체 생성.
        TCComponentOccEffectivityType effType = (TCComponentOccEffectivityType)session.getTypeComponent("CFM_date_info");
        TCComponentOccEffectivity effComp = (TCComponentOccEffectivity) effType.create(ecoNo);
        effComp.setStringProperty("id", ecoNo);
        TCProperty tcproperty = effComp.getTCProperty("eff_date");
        tcproperty.setPropertyArrayData(adate);
        effComp.setTCProperty(tcproperty);
        //          effComp.setDateValueArray("eff_date", "ECO Released 날짜의 23:59:59 초");
        effComp.save();
    }*/
    
}