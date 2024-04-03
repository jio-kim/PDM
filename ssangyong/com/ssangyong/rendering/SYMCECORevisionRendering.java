package com.ssangyong.rendering;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.commands.ec.eco.ECODWGSWTRendering;
import com.ssangyong.commands.ec.eco.ECOEPLSWTRendering;
import com.ssangyong.commands.ec.eco.ECOPartListSWTRendering;
import com.ssangyong.commands.ec.eco.ECOSWTRendering;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.PreferenceService;
import com.ssangyong.dto.ApprovalLineData;
import com.ssangyong.viewer.AbstractSYMCViewer;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.MessageBox;

/**
 * [20170105] CM ECO �� ��� Module Validate�� ���� ����
 */
@SuppressWarnings({"rawtypes"})
public class SYMCECORevisionRendering extends AbstractSYMCViewer {

    /** ECO Tab View */
    private TabFolder tabFolder;
    /** A Sheet */
    private ECOSWTRendering ecoInfoRender;
    /** B Sheet */
    private ECODWGSWTRendering ecoDwgRender;
    /** C Sheet */
    private ECOEPLSWTRendering ecoEPLRender;
    /** D Sheet */
    private ECOPartListSWTRendering ecoPartRender;
    private TCSession session;

    /**
     * ������
     * 
     * @param parent
     */
    public SYMCECORevisionRendering(Composite parent) {
        super(parent);
    }

    /**
     * ����
     */
    @Override
    public void save() {
        ecoInfoRender.save();
        if (ecoDwgRender != null)
            ecoDwgRender.save();
        if (ecoEPLRender != null)
            ecoEPLRender.save();
        if (ecoPartRender != null)
            ecoPartRender.save();
    }

    /**
     * ���� ���� ���� ���� Ȯ��
     */
    @Override
    public boolean isDirty() {
        return ecoInfoRender.isModified() || (ecoDwgRender != null && ecoDwgRender.isDirty()) || (ecoEPLRender != null && ecoEPLRender.isDirty()) || (ecoPartRender != null && ecoPartRender.isDirty());
    }

    /**
     * ȭ�� ����
     */
    @Override
    public void createPanel(Composite parent) {

    	session = (TCSession)AIFUtility.getCurrentApplication().getSession();
        tabFolder = new TabFolder(parent, SWT.NONE);

        TabItem ecoInfo = new TabItem(tabFolder, SWT.NONE);
        ecoInfo.setText("ECO A");

        ScrolledComposite sc = new ScrolledComposite(tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
        ecoInfoRender = new ECOSWTRendering(sc);
        Composite composite = ecoInfoRender.getComposite();
        sc.setContent(composite);
        sc.setMinSize(800, 1500);
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);
        ecoInfo.setControl(sc);

        TabItem dwgInfo = new TabItem(tabFolder, SWT.NONE);
        dwgInfo.setText("ECO B");
        TabItem ecoEPL = new TabItem(tabFolder, SWT.NONE);
        ecoEPL.setText("ECO C");
        TabItem ecoPart = new TabItem(tabFolder, SWT.NONE);
        ecoPart.setText("ECO D");
        // Add Tab Folder Listener
        addTabFolderListener();
    }

    /**
     * Add Tab Folder Listener
     * 
     * @method addTabFolderListener
     * @date 2013. 2. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void addTabFolderListener() {
        tabFolder.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
                if ("ECO B".equals(tabFolder.getSelection()[0].getText())) {
                    if (tabFolder.getSelection()[0].getControl() == null) {
                        ScrolledComposite dwgInfoScrolled = new ScrolledComposite(tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
                        ecoDwgRender = new ECODWGSWTRendering(dwgInfoScrolled);
                        ecoDwgRender.setEditable(isEditable());
                        dwgInfoScrolled.setContent(ecoDwgRender);
                        dwgInfoScrolled.setExpandHorizontal(true);
                        dwgInfoScrolled.setExpandVertical(true);
                        tabFolder.getSelection()[0].setControl(dwgInfoScrolled);
                        ecoDwgRender.load();
                    }
                } else if ("ECO C".equals(tabFolder.getSelection()[0].getText())) {
                    if (tabFolder.getSelection()[0].getControl() == null) {
                        ScrolledComposite ecoEPLScrolled = new ScrolledComposite(tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
                        ecoEPLRender = new ECOEPLSWTRendering(ecoEPLScrolled);
                        ecoEPLRender.setEditable(isEditable());
                        ecoEPLScrolled.setContent(ecoEPLRender);
                        ecoEPLScrolled.setExpandHorizontal(true);
                        ecoEPLScrolled.setExpandVertical(true);
                        tabFolder.getSelection()[0].setControl(ecoEPLScrolled);
                        ecoEPLRender.load();
                    }
                } else if ("ECO D".equals(tabFolder.getSelection()[0].getText())) {
                    if (tabFolder.getSelection()[0].getControl() == null) {
                        ScrolledComposite ecoPartScrolled = new ScrolledComposite(tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
                        ecoPartRender = new ECOPartListSWTRendering(ecoPartScrolled);
                        ecoPartRender.setEditable(isEditable());
                        ecoPartScrolled.setContent(ecoPartRender);
                        ecoPartScrolled.setExpandHorizontal(true);
                        ecoPartScrolled.setExpandVertical(true);
                        tabFolder.getSelection()[0].setControl(ecoPartScrolled);
                        ecoPartRender.load();
                    }
                }
            }
        });
    }

    /**
     * Refresh
     */
    @Override
    public void load() {
        if(targetComp != null) {
            try {
            	// [NoSR][20160106][jclee] �ٸ� ����ڰ� ������ ������ Load�� �ٷ� �����ϱ� ���� Refresh
//            	targetComp.refresh();
            	
                //if(targetComp.getProperty("release_status_list").equals("") && targetComp.getProperty("process_stage_list").equals("")) {
                //if(targetComp.getProperty("release_status_list").equals("") && targetComp.getProperty("process_stage_list").indexOf("Creator") != -1) {
                // �ۼ��� �Ǵ� ���� �������� ���
                if(targetComp.getProperty("release_status_list").equals("")) {
                	// [20240404][UPGRADE] TC12.2 ���� process_stage_list �� Root Task �� ǥ���ϵ��� �Ǿ� �־� fnd0StartedWorkflowTasks �� ��ü 
//                    String stage = targetComp.getProperty("process_stage_list");
                    String stage = targetComp.getProperty("fnd0StartedWorkflowTasks");
                    SYMCRemoteUtil remote = new SYMCRemoteUtil();
                    DataSet ds = new DataSet();
                    String ecoUser = "";
                    String sessionUser = "";
                    
                    // Working �Ǵ� ���μ��� ���°� �ź��� ���
                    if(stage.equals("") || stage.indexOf("Creator") != -1) {
                        sessionUser = session.getUser().getUserId();
                        ecoUser = ((TCComponentUser)targetComp.getReferenceProperty("owning_user")).getUserId();
                        if(sessionUser.equals(ecoUser)) {
                            ds.put("ecoNo", targetComp.getProperty("item_id"));
                            Boolean result = (Boolean)remote.execute("com.ssangyong.service.ECOHistoryService", "isECOEPLChanged", ds);
                            if(result.booleanValue()) {
                                int response = ConfirmDialog.prompt(getShell(), "ECO EPL", "ECO EPL is Out Of Date!\nRegenerate It?");
                                if(response == 2) {
                					try{
                						showProgress(true, getShell());
                                    	/**
                                    	 * [SR����][2015.04.27][jclee] ECO Generate �� ��� EPL ���⳻���� ������ �� Regenerate
                                    	 */
//                						remote.execute("com.ssangyong.service.ECOHistoryService", "extractEPL", ds);
                						remote.execute("com.ssangyong.service.ECOHistoryService", "generateECO", ds);
                						
                						/**
                						 * [2015.02.26][jclee] IN ECO ����
                						 */
                						makeBOMHistoryMaster();
                						
//                						//���� Generate �� ��� Validation Button�� ������ �ʴ� ���� ����.
//                						ds.clear();
//                						ds.put("eco_no", targetComp.getProperty("item_id"));
//                						ArrayList moduleList = (ArrayList)remote.execute("com.ssangyong.service.ModuleBomValidationService", "getModulePart", ds);
//                						if( moduleList != null && !moduleList.isEmpty()){
//                							ecoInfoRender.getModuleBOMvalidBtn().setVisible(true);
//                						}else{
//                							ecoInfoRender.getModuleBOMvalidBtn().setVisible(false);
//                						}
                					}catch( Exception e){
                						throw e;
                					}finally{
                						showProgress(false, getShell());
                					}
                                }
                            }
                        }
                    }
                    
                    /**
                     * [SR140926-025][20140926][jclee] ECO�� Module BOM Validate ����̰� ���� ������ ����ڰ� ������� �������� ��� Module Validate ��ư�� Ȱ��ȭ
                     */
                    // 1. Module BOM Validate ���� ���� (Creator �������� ������ ��������� ����ڴ� ������ ����)
                    // ���� ������ Module BOM Validate ��� ECO���� ����ǹǷ� �ð� ������ ���� Module BOM Validate���� Ȯ���Ѵ�.
                    ds.clear();
                    ds.put("eco_no", targetComp.getProperty("item_id"));
					ArrayList moduleList = (ArrayList)remote.execute("com.ssangyong.service.ModuleBomValidationService", "getModulePart", ds);
					//FIXME: [20170105] CM ECO �� ��� Module Validate�� ���� ����
					boolean isCMECO = targetComp.getProperty("item_id").startsWith("CM")?true:false;
					
                    if (moduleList != null && !moduleList.isEmpty() && !isCMECO) {
                    	CustomECODao dao = new CustomECODao();
                    	ApprovalLineData theLine = new ApprovalLineData();
                    	boolean isEngineeringMgr = false; 
                    	
                    	// ECO�� ���缱 ������ �����´�
                    	theLine.setEco_no(targetComp.getProperty("item_id"));
                    	ArrayList<ApprovalLineData> paramList = dao.getApprovalLine(theLine);
                    	TCComponentUser currentUser = session.getUser();	// ���� ������ �����
                    	
                    	// ���� ������ ������� Group ����
                    	TCComponentGroup[] currentUserGroups = currentUser.getGroups();
                    	for (int i = 0; i < currentUserGroups.length; i++) {
							TCComponentGroup currentUserGroup = (TCComponentGroup)currentUserGroups[i];
							String sGroupName = currentUserGroup.getProperty("name");
							//2023-10 �������� �ϵ� �ڵ��� �׷���� Preference�� ���� 
							if (sGroupName.equals(PreferenceService.getValue("RnD MANAGEMENT"))) {
								isEngineeringMgr = true;
								break;
							}
						}
                    	
                    	// 2. ���� ������ ������� Group ������ ����������̰ų� ��Ŵܰ��� ���
                    	if (isEngineeringMgr || (stage.equals("") || stage.indexOf("Creator") != -1)) {
                    		// 3. ���缱 �� ���� ������ ����ڰ� �ִ��� Ȯ��.
                    		for(ApprovalLineData map : paramList){
                    			String sTCMemberPuid = map.getTc_member_puid();
                    			
                    			TCComponentGroupMember tcComponent = (TCComponentGroupMember)session.stringToComponent(sTCMemberPuid);
                    			String sApprovalUserID = tcComponent.getProperty("user_name");
                    			
                    			// 4. ���缱�� ���� ������ ����ڰ� ���� ��� Module BOM Validation ��ư Ȱ��ȭ.
                    			// �� ���������� Module BOM Validation ���, ���� ������ ����ڰ� ��������� �μ��̰� ���缱�� ������ ��� ��ư�� Ȱ��ȭ ��Ŵ.
                    			if (currentUser.getUserId().equals(sApprovalUserID)) {
                    				ecoInfoRender.getModuleBOMvalidBtn().setVisible(true);
                    				if (ecoInfoRender.getRequestApprovalBtn() != null) {
                    					ecoInfoRender.getRequestApprovalBtn().setEnabled(false);
									}
                    				
                    				if (ecoInfoRender.getRequestApprovalTopBtn() != null) {
                    					ecoInfoRender.getRequestApprovalTopBtn().setEnabled(false);
									}
                    				ecoInfoRender.setIsEngineeringManager(true);
                    				break;
                    			} else {
                    				ecoInfoRender.getModuleBOMvalidBtn().setVisible(false);
                    				if (ecoInfoRender.getRequestApprovalBtn() != null) {
                    					ecoInfoRender.getRequestApprovalBtn().setEnabled(true);
									}
                    				
                    				if (ecoInfoRender.getRequestApprovalTopBtn() != null) {
                    					ecoInfoRender.getRequestApprovalTopBtn().setEnabled(true);
									}
                    				ecoInfoRender.setIsEngineeringManager(false);
                    			}
                    		}
                    		
                    		// ECO Owner�� ��� Module BOM Validate ��ư Ȱ��ȭ
                    		if (ecoUser.equals(sessionUser)) {
                    			ecoInfoRender.getModuleBOMvalidBtn().setVisible(true);
                    			
                    			if (ecoInfoRender.getRequestApprovalBtn() != null) {
                    				ecoInfoRender.getRequestApprovalBtn().setEnabled(false);
								}
                    			
                    			if (ecoInfoRender.getRequestApprovalTopBtn() != null) {
                    				ecoInfoRender.getRequestApprovalTopBtn().setEnabled(false);
								}
                    			ecoInfoRender.setIsEngineeringManager(isEngineeringMgr);
                    		}
                    	}

					} else {
						ecoInfoRender.getModuleBOMvalidBtn().setVisible(false);
						
						if (ecoInfoRender.getRequestApprovalBtn() != null) {
							ecoInfoRender.getRequestApprovalBtn().setEnabled(true);
						}
						
						if (ecoInfoRender.getRequestApprovalTopBtn() != null) {
							ecoInfoRender.getRequestApprovalTopBtn().setEnabled(true);
						}
						
        				ecoInfoRender.setIsEngineeringManager(false);
					}
                }
            } catch (Exception e) {
                MessageBox.post(getShell()
                        , "EPL Generation Failed." +
                          "\nPlease try again or contact administrator."
                        , "ECO EPL", MessageBox.ERROR);
                e.printStackTrace();
            }
        }
        
        try {
            ecoInfoRender.setProperties();
        } catch(Exception e) {
            e.printStackTrace();
        }
        try {
            if(ecoEPLRender != null) {
                ecoEPLRender.load();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        try {
            if(ecoDwgRender != null) {
                ecoDwgRender.load();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        try {
            if(ecoPartRender != null) {
                ecoPartRender.load();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isSavable() {
         return ecoInfoRender.isSavable();
    }
	
	/**
	 * IN ECO ����
	 * @throws Exception
	 */
	private void makeBOMHistoryMaster() throws Exception {
		CustomECODao dao = new CustomECODao();
		dao.makeBOMHistoryMaster(targetComp.getProperty("item_id"));
	}

}
