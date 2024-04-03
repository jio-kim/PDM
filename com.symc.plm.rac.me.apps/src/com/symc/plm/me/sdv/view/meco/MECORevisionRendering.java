package com.symc.plm.me.sdv.view.meco;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.symc.plm.me.common.viewer.AbstractSDVViewer;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class MECORevisionRendering extends AbstractSDVViewer {

	private TabFolder tabFolder;
	private MECOSWTRenderingView mecoInfoRender;
	private MECOEPLSWTRenderingView mecoEPLRender;
	
	public MECORevisionRendering(Composite parent) {
		super(parent);
	}

	@SuppressWarnings("unused")
    @Override
	public void load() {
	       if(targetComp != null) {
	            try {
	                //if(targetComp.getProperty("release_status_list").equals("") && targetComp.getProperty("process_stage_list").equals("")) {
	                //if(targetComp.getProperty("release_status_list").equals("") && targetComp.getProperty("process_stage_list").indexOf("Creator") != -1) {
	                // 작성중 또는 승인 진행중인 경우
	                if(targetComp.getProperty("release_status_list").equals("")) {
	            	    // [20240404][UPGRADE] TC12.2 이후 process_stage_list 는 Root Task 만 표시하도록 되어 있어 fnd0StartedWorkflowTasks 로 교체
//	                    String stage = targetComp.getProperty("process_stage_list");
	                    String stage = targetComp.getProperty("fnd0StartedWorkflowTasks");
	                    // Working 또는 프로세스 상태가 거부인 경우
	                    if(stage.equals("") || stage.indexOf("Creator") != -1) {
	                        String sessionUser = ((TCSession)AIFUtility.getCurrentApplication().getSession()).getUser().getUserId();
	                        String ecoUser = ((TCComponentUser)targetComp.getReferenceProperty("owning_user")).getUserId();
	                        if(sessionUser.equals(ecoUser)) {
	                            SYMCRemoteUtil remote = new SYMCRemoteUtil();
	                            DataSet ds = new DataSet();
	                            ds.put("ecoNo", targetComp.getProperty("item_id"));
//	                            Boolean result = (Boolean)remote.execute("com.ssangyong.service.ECOHistoryService", "isECOEPLChanged", ds);
//	                            if(result.booleanValue()) {
//	                                int response = ConfirmDialog.prompt(getShell(), "ECO EPL", "ECO EPL is Out Of Date!\nRegenerate It?");
//	                                if(response == 2) {
//	                                    remote.execute("com.ssangyong.service.ECOHistoryService", "extractEPL", ds);
//	                                }
//	                            }
	                        }
	                    }
	                }
	            } catch (Exception e) {
	                MessageBox.post(getShell()
	                        , "EPL Generation Failed." +
	                          "\nPlease try again or contact administrator."
	                        , "MECO EPL", MessageBox.ERROR);
	                e.printStackTrace();
	            }
	        }
	        
	        try {
	            mecoInfoRender.setProperties();
	        } catch(Exception e) {
	            e.printStackTrace();
	        }
	        try {
	            if(mecoEPLRender != null) {
	            	mecoEPLRender.load();
	            }
	        } catch(Exception e) {
	            e.printStackTrace();
	        }

	}

	@Override
	public void save() {
		mecoInfoRender.save();
	}

	@Override
	public boolean isSavable() {
		return mecoInfoRender.isSavable();
	}

	@Override
	public void createPanel(Composite parent) {
        tabFolder = new TabFolder(parent, SWT.NONE);

        TabItem mecoInfo = new TabItem(tabFolder, SWT.NONE);
        mecoInfo.setText("MECO A");

        ScrolledComposite sc = new ScrolledComposite(tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
        mecoInfoRender = new MECOSWTRenderingView(sc);
        Composite composite = mecoInfoRender.getComposite();
        sc.setContent(composite);
        sc.setMinSize(800, 1400);
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);
        mecoInfo.setControl(sc);
//        mecoInfoRender.updateUI();
        mecoInfoRender.load();
        TabItem mecoEPL = new TabItem(tabFolder, SWT.NONE);
        mecoEPL.setText("MECO EPL");

        // Add Tab Folder Listener
        addTabFolderListener();
        
	}
	
    private void addTabFolderListener() {
        tabFolder.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
                if ("MECO EPL".equals(tabFolder.getSelection()[0].getText())) {
                    if (tabFolder.getSelection()[0].getControl() == null) {
//                        ScrolledComposite mecoEPLScrolled = new ScrolledComposite(tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
                        ScrolledComposite mecoEPLScrolled = new ScrolledComposite(tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
                        mecoEPLRender = new MECOEPLSWTRenderingView(mecoEPLScrolled);
                        mecoEPLRender.setSize(1024, 1400);
                        mecoEPLRender.setEditable(isEditable());
                        mecoEPLScrolled.setContent(mecoEPLRender);
                        mecoEPLScrolled.setExpandHorizontal(true);
                        mecoEPLScrolled.setExpandVertical(true);
                        tabFolder.getSelection()[0].setControl(mecoEPLScrolled);
                        mecoEPLRender.load();
                    	}
                    }
            }
        });
    }

}
