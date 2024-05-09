package com.kgm.commands.bomcompare;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.pse.PSEApplicationPanel;
import com.teamcenter.rac.pse.common.BOMLineNode;
import com.teamcenter.rac.pse.common.BOMPanel;
import com.teamcenter.rac.pse.common.BOMTreeTable;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.MessageBox;

public class BOMCompareECOCommand extends AbstractAIFCommand {
    
    /** parent shell */
    private Shell parent;
    /** BOM save response */
    private int response;
    
    /** PSE current BOM Panel */
    private BOMPanel bomPanel;
    /** PSE BOM TreeTable */
    private BOMTreeTable bomTree;
    /** compare target BOMLineNode */
    private BOMLineNode bomNode;

	public BOMCompareECOCommand() {
	}

    protected void executeCommand() throws Exception {
	    parent = AIFDesktop.getActiveDesktop().getShell();
	    AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
	    TCComponentBOMLine bomLine = null;
	    if(!(application.getApplicationPanel() instanceof PSEApplicationPanel)) {
	        return;
	    }
        PSEApplicationPanel psePanel = (PSEApplicationPanel)application.getApplicationPanel();
        bomPanel = psePanel.getCurrentBOMPanel();
        
        bomTree = bomPanel.getTreeTable();
        BOMLineNode[] bomLineNodes = bomTree.getSelectedBOMLineNodes();
        if(bomLineNodes == null || bomLineNodes.length == 0) {
            MessageBox.post(parent, "Select Compare Taget Line", "BOM Compare", MessageBox.INFORMATION);
            return;
        }
        bomNode = bomLineNodes[0];
        bomLine = bomNode.getBOMLine();
        String itemType = bomLine.getProperty("bl_item_object_type");
        if(!itemType.equals("Vehicle Part") && !itemType.equals("Function Master")) {
            MessageBox.post(parent, "Select Function Master or Vehicle Part.", "BOM Compare", MessageBox.INFORMATION);
            return;
        }
        // [SR141107-016][20151208][jclee] BOM 하위에 모든 Part가 Cut되었을 경우를 대비하여 Assy가 아니더라도 Open 할 수 있도록 수정
//        if(!bomLine.hasChildren()) {
//            MessageBox.post(parent, "Selected Part is not a assembly.\n", "BOM Compare", MessageBox.INFORMATION);
//            return;
//        }
        
        TCComponentItemRevision ecoIr = (TCComponentItemRevision)bomLine.getItemRevision().getRelatedComponent("s7_ECO_NO");
        if(ecoIr == null) {// || !ecoIr.getProperty("release_status_list").equals("")) {
            MessageBox.post(parent, "Selected Part hasn't ECO No.\n", "BOM Compare", MessageBox.INFORMATION);
            return;
        }
        
        loadECOEPL(ecoIr, bomLine);
	}
	
    /**
     * BOM ECO Work load
     * @param ecoIr
     * @param bomLine
     */
	private void loadECOEPL(final TCComponentItemRevision ecoIr, final TCComponentBOMLine bomLine) {
        new Job("Load ECO EPL...") {
            @SuppressWarnings("unchecked")
            protected IStatus run(IProgressMonitor pm) {
                try {
                    boolean isSaved = false;
                    // 현재 BOM 저장 여부 저장이 안되어 있으면 저장하도록
                    if(bomLine.window().isModified()) {
                        parent.getDisplay().syncExec(new Runnable() {
                            public void run() {
                                response = ConfirmDialog.prompt(parent, "BOM Save"
                                        , "BOM is Modified.\nIn order to compare BOM shoud be save first!\n\nSAVE?(ECO EPL is auto generated)");
                            }
                        });
                        if(response == 2) {
                            bomLine.window().save();
                            isSaved = true;
                        } else {
                            return Status.CANCEL_STATUS;
                        }
                    }

                    // ECO EPL generate
                    // [20240404][UPGRADE] TC12.2 이후 process_stage_list 는 Root Task 만 표시하도록 되어 있어 fnd0StartedWorkflowTasks 로 교체
//                    String procList = ecoIr.getProperty("process_stage_list");
                    String procList = ecoIr.getProperty("fnd0StartedWorkflowTasks");
                    if(ecoIr.getProperty("release_status_list").equals("") && (procList.equals("") || procList.indexOf("Creator") != -1)) {
                        String ecoNo = ecoIr.getProperty("item_id");
                        String sessionUser = bomLine.getSession().getUser().getUserId();
                        String ecoUser = ((TCComponentUser)ecoIr.getReferenceProperty("owning_user")).getUserId();
                        SYMCRemoteUtil remote = new SYMCRemoteUtil();
                        DataSet ds = new DataSet();
                        ds.put("ecoNo", ecoNo);
                        Boolean result = (Boolean)remote.execute("com.kgm.service.ECOHistoryService", "isECOEPLChanged", ds);
                        if(result.booleanValue()) {
                            if(sessionUser.equals(ecoUser)) {
                                if(isSaved) {
                                	/**
                                	 * [SR없음][2015.04.27][jclee] ECO Generate 시 모든 EPL 추출내역을 삭제한 후 Regenerate
                                	 */
//                                    remote.execute("com.kgm.service.ECOHistoryService", "extractEPL", ds);
                                    remote.execute("com.kgm.service.ECOHistoryService", "generateECO", ds);
                                } else {
                                    parent.getDisplay().syncExec(new Runnable() {
                                        public void run() {
                                            response = ConfirmDialog.prompt(parent, "ECO EPL", "ECO EPL is Out Of Date!\nRegenerate It?");
                                        }
                                    });
                                    if(response == 2) {
                                    	/**
                                    	 * [SR없음][2015.04.27][jclee] ECO Generate 시 모든 EPL 추출내역을 삭제한 후 Regenerate
                                    	 */
//                                        remote.execute("com.kgm.service.ECOHistoryService", "extractEPL", ds);
                                        remote.execute("com.kgm.service.ECOHistoryService", "generateECO", ds);
                                    } else {
                                        parent.getDisplay().syncExec(new Runnable() {
                                            public void run() {
                                                MessageBox.post(parent, "ECO EPL is differ from BOM Work.\nIn order to compare, ECO EPL is newly created.", "BOM Compare", MessageBox.INFORMATION);
                                            }
                                        });
                                        return Status.CANCEL_STATUS;
                                    }
                                }
                            } else {
                                parent.getDisplay().syncExec(new Runnable() {
                                    public void run() {
                                        MessageBox.post(parent, "ECO EPL is differ from BOM Work.\nIn order to compare, ECO EPL is newly created.", "BOM Compare", MessageBox.INFORMATION);
                                    }
                                });
                                return Status.CANCEL_STATUS;
                            }
                        }
                    }
                    // open compare dialog
                    parent.getDisplay().syncExec(new Runnable() {
                        public void run() {
                            BOMCompareECOGridDialog dialog = new BOMCompareECOGridDialog(parent, bomPanel, bomTree, bomNode);
//                            BOMCompareECODialog dialog = new BOMCompareECODialog(parent, bomPanel, bomTree, bomNode);
                            dialog.open();
                        }
                    });
                    
                } catch(Exception e) {
                    e.printStackTrace();
                }

                return Status.OK_STATUS;
            }
        }.schedule();
	}
	
}