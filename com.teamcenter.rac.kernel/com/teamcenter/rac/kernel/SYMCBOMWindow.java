package com.teamcenter.rac.kernel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;


/**
 * [SR150128-017][20150211][jclee] Variant ������ Function �ű� ���� �� BOPADMIN Role ����ڿ��� E-Mail �߼�
 * [20161221] Project Code �� S201, SM01 �� ���, Function �ű� ���� �� BOPADMIN���� ���� ������ ����
 * [20170118] sendMailToBOPADMIN Function �� ������ ��Ÿ������. 4000���̻��� ��� ������
 */
public class SYMCBOMWindow extends TCComponentBOMWindow {
    
	private ArrayList<TCComponentItemRevision> revisionsToDelete = null;
    private boolean skipHistory;
    
    public void skipHistory(boolean skip) {
        this.skipHistory = skip;
    }
    
    boolean isSkipHistory() {
        return skipHistory;
    }

//    public void save_super() throws TCException {
//    	super.save();
//    }
    /**
     * ����
     */
    @Override
    public void save() throws TCException {
    	TCComponentBOMLine tblTopLine = getTopBOMLine();
    	
    	if (tblTopLine instanceof SYMCBOMLine) {
    		SYMCBOMLine sblTopLine = (SYMCBOMLine) tblTopLine;
    		String sType = sblTopLine.getProperty("bl_item_object_type");
    		
    		if (sType == null || sType.equals("") || sType.length() == 0) {
				super.save();
				return;
			}
    		
    		// checkVariantModifiedAndSendMail�� �ӵ� ���Ϲ����� ��ġ������ �̵� Start
    		/*
    		// 1. TOP Line�� Product�� ���
    		if (sType.equals("Product")) {
    			// 1.1. Variant ����
    			AIFComponentContext[] children = sblTopLine.getChildren();
    			for (AIFComponentContext child : children) {
    				InterfaceAIFComponent component = child.getComponent();
    				if (component instanceof SYMCBOMLine) {
    					SYMCBOMLine bomLine = (SYMCBOMLine)component;
    					checkVariantModifiedAndSendMail(sblTopLine, bomLine);
    				}
    			}
			}
    		
    		// 2. TOP Line�� Variant�� ���
    		else if (sType.equals("Variant")) {
    			checkVariantModifiedAndSendMail(sblTopLine);
			}else 
			*/
    		// checkVariantModifiedAndSendMail�� �ӵ� ���Ϲ����� ��ġ������ �̵� End
			if (sType.equals("Pre Function Master") || sType.equals("Pre Function") 
					|| sType.equals("Pre Product") || sType.equals("Pre Vehicle Part")) {
				
				//Pre BOM�� BOMADMIN�� ������ �ٸ� Role�� ���� �� �� ����.
				boolean hasRole = false;
				InterfaceAIFComponent[] targets = AIFUtility.getCurrentApplication().getTargetComponents();
				if( targets != null && targets.length > 0){
					if( targets[0] instanceof TCComponentBOMLine){
						TCComponentBOMLine line = (TCComponentBOMLine)targets[0];
						TCSession session = line.getSession();
						TCComponentUser user = session.getUser();
						Map<TCComponentGroup, List<TCComponentRole>> roleTable = user.getGroupRolesTable();
						for( TCComponentGroup group : roleTable.keySet()){
							List<TCComponentRole> roleList = roleTable.get(group);
							for( TCComponentRole role : roleList){
								String roleName = role.getProperty("role_name");
								if( roleName.equals("BOMADMIN") || roleName.equals("DBA") || roleName.equals("PROJECT_ENGINEER")){
									hasRole = true;
								}
							}
							
							if( hasRole){
								break;
							}
						}
						
						if( !hasRole){
							MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), "Pre BOM can not be modified in the Structure Manager.", "BOM Save", MessageBox.INFORMATION);
							return;
			    		}
					}
				}
				
			}
		}
    	
    	super.save();
    }
    
    //�׽�Ʈ �ӽ�
    /*
    public void save_() throws TCException {
    	TCComponentBOMLine tblTopLine = getTopBOMLine();
    	
    	if (tblTopLine instanceof SYMCBOMLine) {
    		SYMCBOMLine sblTopLine = (SYMCBOMLine) tblTopLine;
    		String sType = sblTopLine.getProperty("bl_item_object_type");
    		
    		if (sType == null || sType.equals("") || sType.length() == 0) {
				super.save();
				return;
			}
    		
    		// checkVariantModifiedAndSendMail�� �ӵ� ���Ϲ����� ��ġ������ �̵� Start
    		
    		// 1. TOP Line�� Product�� ���
    		if (sType.equals("Product")) {
    			// 1.1. Variant ����
    			AIFComponentContext[] children = sblTopLine.getChildren();
    			for (AIFComponentContext child : children) {
    				InterfaceAIFComponent component = child.getComponent();
    				if (component instanceof SYMCBOMLine) {
    					SYMCBOMLine bomLine = (SYMCBOMLine)component;
    					checkVariantModifiedAndSendMail(sblTopLine, bomLine);
    				}
    			}
			}
    		
    		// 2. TOP Line�� Variant�� ���
    		else if (sType.equals("Variant")) {
    			checkVariantModifiedAndSendMail(sblTopLine);
			}else 
			
    		// checkVariantModifiedAndSendMail�� �ӵ� ���Ϲ����� ��ġ������ �̵� End
			if (sType.equals("Pre Function Master") || sType.equals("Pre Function") 
					|| sType.equals("Pre Product") || sType.equals("Pre Vehicle Part")) {
				
				//Pre BOM�� BOMADMIN�� ������ �ٸ� Role�� ���� �� �� ����.
				boolean hasRole = false;
				InterfaceAIFComponent[] targets = AIFUtility.getCurrentApplication().getTargetComponents();
				if( targets != null && targets.length > 0){
					if( targets[0] instanceof TCComponentBOMLine){
						TCComponentBOMLine line = (TCComponentBOMLine)targets[0];
						TCSession session = line.getSession();
						TCComponentUser user = session.getUser();
						Map<TCComponentGroup, List<TCComponentRole>> roleTable = user.getGroupRolesTable();
						for( TCComponentGroup group : roleTable.keySet()){
							List<TCComponentRole> roleList = roleTable.get(group);
							for( TCComponentRole role : roleList){
								String roleName = role.getProperty("role_name");
								if( roleName.equals("BOMADMIN") || roleName.equals("DBA") || roleName.equals("PROJECT_ENGINEER")){
									hasRole = true;
								}
							}
							
							if( hasRole){
								break;
							}
						}
						
						if( !hasRole){
							MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), "Pre BOM can not be modified in the Structure Manager.", "BOM Save", MessageBox.INFORMATION);
							return;
			    		}
					}
				}
				
			}
		}
    	
    	super.save();
    }
    */
  //�׽�Ʈ �ӽ� ��
    /**
     * Variant ���� Function �ű� ���� ���� Ȯ�� �� E-Mail �߼�
     * @param variant
     * @throws TCException
     */
    private void checkVariantModifiedAndSendMail(SYMCBOMLine variant) throws TCException {
    	checkVariantModifiedAndSendMail(null, variant);
    }
    
    /**
     * Product, Variant ���� Function �ű� ���� ���� Ȯ�� �� E-Mail �߼�
     * @param variant
     * @throws TCException
     */
    private void checkVariantModifiedAndSendMail(SYMCBOMLine product, SYMCBOMLine variant) throws TCException {
    	// 1.1 Variant ������ ����Ǿ����� Ȯ��
    	ArrayList<String> alAddedFunction = null;
    	if (product == null) {
    		alAddedFunction = getAddedFunction(variant);
		} else {
			alAddedFunction = getAddedFunction(product);
		}
    	
    	if (alAddedFunction.size() > 0) {
    		sendMailToBOPADMIN(product, variant, alAddedFunction);
    	}
    }
    
    /**
     * ����� Variant ���
     * @param parent
     * @return
     * @throws TCException
     */
    private ArrayList<String> getAddedVariant(SYMCBOMLine parent) throws TCException {
    	AIFComponentContext[] aifVariants = parent.getChildren();
    	ArrayList<String> alAddedVariant = new ArrayList<String>();
    	
    	for (AIFComponentContext aifFunction : aifVariants) {
			InterfaceAIFComponent aifComponent = aifFunction.getComponent();
			if (aifComponent instanceof SYMCBOMLine) {
				SYMCBOMLine sblChild = (SYMCBOMLine) aifComponent;
				String sType = sblChild.getProperty("bl_item_object_type");
				
				// Variant �߿� ���Ӱ� �߰��� ������ ���� ���
				if (sType.equals("Variant")) {
					boolean isAdded = sblChild.getAdded();
					String sVariant = sblChild.getItem().getProperty("item_id");
					if (isAdded) {
						alAddedVariant.add(sVariant);
					}
				}
			}
		}

    	return alAddedVariant;
    }
    
    /**
     * ����� Function ���
     * @param variant
     * @return
     * @throws TCException
     */
    private ArrayList<String> getAddedFunction(SYMCBOMLine parent) throws TCException {
    	AIFComponentContext[] aifFunctions = parent.getChildren();
    	ArrayList<String> alAddedFunction = new ArrayList<String>();
    	
    	for (AIFComponentContext aifFunction : aifFunctions) {
			InterfaceAIFComponent aifComponent = aifFunction.getComponent();
			if (aifComponent instanceof SYMCBOMLine) {
				SYMCBOMLine sblChild = (SYMCBOMLine) aifComponent;
				String sType = sblChild.getProperty("bl_item_object_type");
				
				// Function �߿� ���Ӱ� �߰��� ������ ���� ���
				if (sType.equals("Function")) {
					boolean isAdded = sblChild.getAdded();
					String sFunction = sblChild.getItem().getProperty("item_id");
					if (isAdded) {
						alAddedFunction.add(sFunction);
					}
				} else if (sType.equals("Variant")) {
					ArrayList<String> alTemp = getAddedFunction(sblChild);
					
					if (alTemp != null) {
						for (int inx = 0; inx < alTemp.size(); inx++) {
							alAddedFunction.add(alTemp.get(inx));
						}
					}
				}
			}
		}

    	return alAddedFunction;
    }
    
    /**
     * Variant ������ Function �ű� ���� �� BOPADMIN���� E-Mail �߼�
     * @throws TCException
     */
    public void sendMailToBOPADMIN(SYMCBOMLine product, SYMCBOMLine variant, ArrayList<String> alAddedFunction) throws TCException {
    	String itemType = variant.getProperty("bl_item_object_type");
    	//FIXME: [20161221] S201,SM01  �� ��� ������ ����
    	String projectCode = variant.getItemRevision().getProperty("s7_PROJECT_CODE");
    	if("S201".equals(projectCode) || "SM01".equals(projectCode))
    		return;
    	
    	if(itemType.equals("Variant")) {
    		TCSession session = getSession();
    		String sCurrentUserID = session.getUser().getUserId();
    		TCComponentUser[] userBOPADMIN = findUserByGroupRoleName(getSession(), "ME PLANNING.SYMC_MFG", "BOPADMIN");
    		StringBuffer sbTitle = new StringBuffer();
    		StringBuffer sbBody = new StringBuffer();
    		
    		sbTitle.append("[PLM] �ű� Function ����");
    		
    		sbBody.append("Please see a new constitute Variant.").append("<BR>").append("<BR>");
    		if (product != null) {
    			sbBody.append("Product : " + product.getItem()).append("<BR>");
    			
    			ArrayList<String> alAddedVariants = getAddedVariant(product);
    			
    			sbBody.append("Added Variant Count : " + alAddedVariants.size()).append("<BR>");
    			
    			for (int inx = 0; inx < alAddedVariants.size(); inx++) {
    				String sAddedVariant = alAddedVariants.get(inx);
        			
        			if (sAddedVariant == null || sAddedVariant.equals("") || sAddedVariant.length() == 0) {
    					continue;
    				}
        			
        			sbBody.append(sAddedVariant).append("<BR>");
				}
			} else {
				sbBody.append("Variant : " + variant.getItem()).append("<BR>");
			}
    		//[20170117] 4000�� �̻��� ��� ���� �߻��Ͽ� Function �� ������ ǥ����
    		//sbBody.append("Added Function Count : " + alAddedFunction.size()).append("<BR>");
    		String firstAddedFunction = null;
    		for (int inx = 0; inx < alAddedFunction.size(); inx++) {
    			String sAddedFunction = alAddedFunction.get(inx);
    			
    			if (sAddedFunction == null || sAddedFunction.equals("") || sAddedFunction.length() == 0) {
					continue;
				}
    			firstAddedFunction = sAddedFunction;
    			break;
    			//sbBody.append(sAddedFunction).append("<BR>");
			}
    		sbBody.append("Added Function Count : " +firstAddedFunction +" ���� �� " + alAddedFunction.size() +" ��").append("<BR>");
    		
    		for (int inx = 0; inx < userBOPADMIN.length; inx++) {
				String sBOPADMINID = userBOPADMIN[inx].getUserId();
				
				if (sBOPADMINID.toUpperCase().equals("INFODBA0")) {
					continue;
				}
				
				try {
					sendMail(sCurrentUserID, sbTitle.toString(), sbBody.toString(), sBOPADMINID);
//					System.out.println(sbBody.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
    	}
    }

    /**
     * �׷��� �Ѹ��� �̿��� ����� �����迭�� �����Ѵ�.
     * 
     * @param session
     * @param groupName
     * @param roleName
     * @return
     * @throws TCException
     */
    public static TCComponentUser[] findUserByGroupRoleName(TCSession session, String groupName, String roleName) throws TCException {
    	TCComponentGroupType groupType = (TCComponentGroupType) session.getTypeComponent("Group");
    	TCComponentGroup group = groupType.find(groupName);
        TCComponentRoleType roleType = (TCComponentRoleType) session.getTypeComponent("Role");
        TCComponentRole role = roleType.find(roleName);
        TCComponentGroupMemberType memberType = (TCComponentGroupMemberType) session.getTypeComponent("GroupMember");
        TCComponentGroupMember[] member = memberType.findByRole(role, group);

        TCComponentUser[] users = new TCComponentUser[member.length];
        for (int i = 0; i < member.length; i++) {
            TCComponentUser user = (TCComponentUser) member[i].getTCProperty("user").getReferenceValue();
            users[i] = user;
        }
        return users;
    }
    
    /**
     * Mail �߼�
     * @param fromUser
     * @param title
     * @param body
     * @param toUsers
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public boolean sendMail(String fromUser, String title, String body, String toUsers) throws Exception{
    	SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
//    	SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil("http://localhost:8080/ssangyongweb/HomeServlet");
    	DataSet ds = new DataSet();
		ds.put("the_sysid", "NPLM");
		
		if(fromUser == null || fromUser.equals(""))
			ds.put("the_sabun", "NPLM");
		else
			ds.put("the_sabun", fromUser);
		
		ds.put("the_title", title);
		ds.put("the_remark", body);
		ds.put("the_tsabun", toUsers);
		return (Boolean) remoteQuery.execute("com.kgm.service.ECOService", "sendMail", ds);
	}
    
    public void addRevisionToDeleteList(TCComponentItemRevision revision){
    	if( revisionsToDelete == null){
    		revisionsToDelete = new ArrayList();
    	}
    	if( !revisionsToDelete.contains(revision) ){
    		revisionsToDelete.add(revision);
    	}
    }
    
    public void removeRevisionFromDeleteList(TCComponentItemRevision revision){
    	if( revisionsToDelete == null){
    		revisionsToDelete = new ArrayList();
    	}
    	revisionsToDelete.remove(revision);
    }

	@Override
	public void close() throws TCException {
		// TODO Auto-generated method stub
		if( revisionsToDelete != null){
			for( TCComponentItemRevision revision : revisionsToDelete){
				try{
					revision.delete();
				}catch(TCException tce){
					tce.printStackTrace();
				}
			}
		}
		
		super.close();
	}

    /*public void save() throws TCException {
        if(!skipHistory && bomChangedData.getEditingCount() > 0) {
            if(!saveECOEPL()) {
                return;
            } else {
                bomChangedData.clear();
            }
        }
        super.save();
        fireChangeEvent();
        System.out.println("SAVED....with event....");
    }
    
    public void saveWithoutEvent() throws TCException {
        if(!skipHistory && bomChangedData.getEditingCount() > 0) {
            if(!saveECOEPL()) {
                return;
            } else {
                bomChangedData.clear();
            }
        }
        super.saveWithoutEvent();
        System.out.println("SAVED....without event....");
    }
    
    @SuppressWarnings("unchecked")
    private boolean saveECOEPL() {
        Iterator<SYMCBOMEditData> editingData = bomChangedData.getChangedBOMEditData().values().iterator();
        /*StringBuffer sModeErr = null;
        while(editingData.hasNext()) {
            SYMCBOMEditData bomEdit = editingData.next();
            String sMode = bomEdit.getSupplyModeNew();
            if(!bomEdit.getChangeType().equals(SYMCBOMEditData.BOM_CUT) && !bomEdit.getParentType().equals("Function") && (sMode == null || sMode.equals(""))) {
                if(sModeErr == null) {
                    sModeErr = new StringBuffer();
                    sModeErr.append("Make sure It is required to input Supply Mode.\n");
                }
                sModeErr.append("\nParent : " + bomEdit.getParentNo() + "/" + bomEdit.getParentRev() + ", Child : " + bomEdit.getPartNoNew() + "/" + bomEdit.getPartRevNew());
            }
        }
        if(sModeErr != null) {
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), sModeErr.toString(), "BOM Save", MessageBox.INFORMATION);
            return false;
        }--
        
        Registry registry = Registry.getRegistry("com.kgm.commands.ec.history.history");
        
        InterfaceSYMCECOSelect bomSave = (InterfaceSYMCECOSelect)registry.newInstanceFor("bomECOSelectDialog", new Object[]{AIFUtility.getActiveDesktop().getShell()});
        String ecoNo = bomSave.getECONo();
        if(ecoNo == null) {
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), "You must input ECO No. to save BOM change information.", "BOM Save", MessageBox.INFORMATION);
            return false;
        }
        SYMCRemoteUtil remote = new SYMCRemoteUtil();
        try {
            DataSet ds = new DataSet();
            ds.put("ecoNo", ecoNo);
            ds.put("userId", getSession().getUser().getUserId());
            ds.put("bomEditData", bomChangedData.getChangedBOMEditData());
            Boolean returnValue = (Boolean)remote.execute("com.kgm.service.ECOHistoryService", "insertECOBOMWork", ds);
            return returnValue.booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell()
                    , "Saving BOM operation was failed." +
                      "\nAn error occurred while saving change information." +
                      "\nPlease try again or contact administrator."
                    , "BOM Save", MessageBox.INFORMATION);
        }
        
        return false;
    }
    
    public void addHistory(TCComponentBOMLine bomLine) throws TCException {
        bomChangedData.add(bomLine);
    }
    
    public void cutHistory(TCComponentBOMLine bomLine) throws TCException {
        bomChangedData.cut(bomLine);
    }
    
    public SYMCBOMEditData[] cutHistoryBefore(TCComponentBOMLine bomLine) throws TCException {
        return bomChangedData.cutBefore(bomLine);
    }
    
    public void cutHistoryAfter(SYMCBOMEditData[] cutData) {
        bomChangedData.cutAfter(cutData);
    }
    
    public SYMCBOMEditData[] replaceHistoryBefore(TCComponentBOMLine bomLine) throws TCException {
        return bomChangedData.replaceBefore(bomLine);
    }
    
    public void replaceHistoryAfter(SYMCBOMEditData[] editData, TCComponentBOMLine bomLine, TCComponentItemRevision newRev) throws TCException {
        bomChangedData.replaceAfter(editData, bomLine, newRev);
    }
    
    public void setHistoryProperty(TCComponentBOMLine bomLine, String propertyName, String oldValue, String newValue) throws TCException {
        bomChangedData.setProperty(bomLine, propertyName, oldValue, newValue);
    }
    
    public void setHistoryVC(TCComponentBOMLine bomLine, String oldVC, String newVC) throws TCException {
        bomChangedData.setVCProperty(bomLine, oldVC, newVC);
    }*/
    
}