package com.symc.plm.rac.prebom.ccn.operation;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.WaitProgressBar;
import com.symc.plm.rac.prebom.ccn.commands.dao.CustomCCNDao;
import com.symc.plm.rac.prebom.ccn.view.PreCCNInfoPanel;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.common.util.BomUtil;
import com.symc.plm.rac.prebom.common.util.SDVPreBOMUtilities;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

/**
 * [20160715][ymjang] CCN EPL ������ ����
 * [20160718] IF CCN Master ���� ���� ���� ���� --> Stored Procedure �� �̰���.
 * [20170309][ymjang] �����ڰ� ���� ���, ���� �߼� ���� ����
 * APPS �� MECO ���μ����� ī���ؿ� 
 * @author JWLEE
 *
 */
public class CCNProcessOperation extends AbstractAIFOperation {

    private TCSession session;
    private TCComponentChangeItemRevision changeRevision;
    private String ccnNo;
    
    private TCComponent[] solutionList;
    private Registry registry = null;
    
    private WaitProgressBar progress;
    private String message;
    
    private CustomCCNDao dao = new CustomCCNDao();
    private Frame frame;
    
    private final static String EVENT_START = "  ��";
    
    private boolean isOkValidation = true;
    private String msg = "";

    private boolean isProgress = true;
    
    private PreCCNInfoPanel ccnViewComposite = null; // CCN UI Composite
    
    private String type = "";
     
    public CCNProcessOperation(TCSession session, TCComponentChangeItemRevision changeRevision) {
        this.session = session;
        this.changeRevision = changeRevision;
        registry = Registry.getRegistry(this);
    }
    
    public CCNProcessOperation(Frame frame, TCSession session, TCComponentChangeItemRevision changeRevision) {
        this.session = session;
        this.changeRevision = changeRevision;
        this.frame = frame;
        registry = Registry.getRegistry(this);
    }
    
    public CCNProcessOperation(TCSession session, TCComponentChangeItemRevision changeRevision, boolean isProgress) {
        this.session = session;
        this.changeRevision = changeRevision;
        this.isProgress = isProgress;
        registry = Registry.getRegistry(this);
    }
    
    /**
     * [20160610]Approval ����� Approval ��ư  Ȱ��ȭ/��Ȱ��ȭ �ϱ����ؼ� ������ �߰�
     * (��ư�� ������ ������ �Ǳ� ����)
     * @param session
     * @param changeRevision
     * @param ccnViewComposite CCN Viewer �� Composite
     */
	/*	
	 * [CF-4358][20230901]Pre-BOM���� I-PASS(���Ű����ý���)���� �������̽� ���� �߰� ��û (SYSTEM, TEAM, CHARGER)
	 *	������ ���� ���� �������� ������� �ʾ� �ּ� ó�� buildCCNEPL__�� �����
	 *  ������� �ʴ� ���� ������ ���� �ҷ��� String type���� �޾� �Դµ� ��� ���� �ʴ� ���� �ּ� ó���� �ʿ� ��� ������ 
	 *  public CCNProcessOperation(TCSession session, TCComponentChangeItemRevision changeRevision, PreCCNInfoPanel ccnViewComposite, String type) { 
	*/
    public CCNProcessOperation(TCSession session, TCComponentChangeItemRevision changeRevision, PreCCNInfoPanel ccnViewComposite) {
        this(session, changeRevision);
        this.ccnViewComposite = ccnViewComposite;
//        this.type = type;
    }

    public void executeOperation() throws Exception {
//    	System.out.println("executeOperation start : "+ new Date());
    	Markpoint mp = new Markpoint(session);
    	
        try {
            if (isProgress)
            {
                if(frame == null){
                    progress = new WaitProgressBar(AIFUtility.getCurrentApplication().getDesktop());
                }else{
                    progress = new WaitProgressBar(frame);
                }
            }
            if (progress != null)
            {
                progress.setWindowSize(500, 400);
                progress.start();       
                progress.setShowButton(true);
                progress.setStatus("CCN Workflow creation start.");
                progress.setAlwaysOnTop(true);
            }
            
            ccnNo = changeRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMID);
            boolean isOk = false;
            
            // # 1. CCN �۾� ����[����]�� ���� �ַ�Ǿ����� ��ũ ����
            if (progress != null){
                progress.setStatus(EVENT_START + "Checking Solution Item(s)...", false);
            }
//            System.out.println("addSolutionItems start : "+ new Date());
            addSolutionItems();
//            System.out.println("addSolutionItems end : "+ new Date());
            if (progress != null){
                progress.setStatus("is done!");
            }
            
            // # 2. ���μ��� Ÿ�� ����
            if (progress != null){
                progress.setStatus(EVENT_START + "Checking targets...", false);
            }
//            System.out.println("getTargets start : "+ new Date());
            getTargets();
//            System.out.println("getTargets end : "+ new Date());
            if (progress != null){
                progress.setStatus("is done!");
            }
            
            // # 3. �ش� Ÿ���� BOM ���� ����ϴ��� üũ
            if (progress != null){
                progress.setStatus(EVENT_START + "Checking targets PreBOM Structure...", false);
            }
//            System.out.println("targetCheckStructure start : "+ new Date());
            targetCheckStructure();
//            System.out.println("targetCheckStructure end : "+ new Date());
            if (progress != null){
                progress.setStatus("is done!");
            }
            
            if(!isOkValidation) {
                throw new Exception("Unsuccesfully completed to validate... Please check Log File!");
            }

            // # 4. CCN ���� ������ ���̺� ��Insert �Ѵ�
            if (progress != null){
                progress.setStatus(EVENT_START + "CCN EPL INFO Insert...", false);
            }

//            System.out.println("buildCCNEPL start : "+ new Date());
            buildCCNEPL(progress);
//            System.out.println("buildCCNEPL end : "+ new Date());
            if (progress != null){
                progress.setStatus("is done!");
            }
            
            // # 5. CCN ���� ������ ���ռ� Ȯ��
            if (progress != null){
            	progress.setStatus(EVENT_START + "Checking Validation...", false);
            }
//            System.out.println("validateCCNEPL start : "+ new Date());
            validateCCNEPL();
//            System.out.println("validateCCNEPL end : "+ new Date());
            if (progress != null){
            	progress.setStatus("is done!");
            }
            
            // Usage ���� Null�� ���� Validation���� Pass
            String sTempMsg = msg;
            sTempMsg = sTempMsg.replace("MLM Usage is not generated!", "").trim();
            
            //[2016-07-07] ���� �� ��츸 ����
            if (progress != null){            	
	            if(!sTempMsg.equals("") && !isOkValidation) {
	                throw new Exception("Unsuccesfully completed to validate... Please check Log File!");
	            }
            }
            
            // # 6 �ڰ����� ����
            if (progress != null){
                progress.setStatus(EVENT_START + "Creating process...", false);
            }            
            // ���簡 �ι� �Ǵ� ���� ����
            changeRevision.refresh();
            if (!SDVPreBOMUtilities.isReleased(changeRevision))
//            	System.out.println("createProcess start : "+ new Date());
            	createProcess();    
//            	System.out.println("createProcess end : "+ new Date());
          	if (progress != null){
                progress.setStatus("is done!");
            }
            
            // # 7 CCN EPL ������ ����
            if (progress != null){
            	progress.setStatus(EVENT_START + "Correcting CCN EPL...", false);
            }
            // [20160715][ymjang] CCN EPL ������ ����
//            System.out.println("correctCCNEPL start : "+ new Date());
            isOk = dao.correctCCNEPL(changeRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMID));
//            System.out.println("correctCCNEPL end : "+ new Date());
            if (isOk) {
                if (progress != null){
                    progress.setStatus("is done!");
                }
            } else {
            	throw new Exception("Error for Correcting CCN EPL!. Please check Log File!");
            }
            
            // # 8 IF CCN Master ����
            if (progress != null){
                progress.setStatus(EVENT_START + "Saving I/F CCN Master...", false);
            }
            // [20160718] IF CCN Master ���� ���� ���� ���� --> Stored Procedure �� �̰���.
//            System.out.println("createIfCCN start : "+ new Date());
            isOk = dao.createIfCCN(changeRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMID));
//            System.out.println("createIfCCN end : "+ new Date());
            if (isOk) {
                if (progress != null){
                    progress.setStatus("is done!");
                }
            } else {
            	throw new Exception("Error for Saving I/F CCN Master!. Please check Log File!");
            }
            
            // # 9 ���� �߼� 
            if (progress != null){
                progress.setStatus(EVENT_START + "Mailing...", false);
            }
            sendMail();
            if (progress != null){
                progress.setStatus("is done!");
            }
            
            mp.forget();
//            System.out.println("executeOperation end : "+ new Date());
        }catch(Exception e){
            e.printStackTrace();
            if(progress != null){
                progress.setStatus("is fail!");
                progress.setStatus("�� Error Message : ");
                message = " " + e.getMessage();
            }
            
            /**
             * ������ ��ư Ȱ��ȭ
             */
            setEnableAppprvalButton(true);
            
            mp.rollBack();
        }finally{
            if(progress != null){
                if(message != null){
                    progress.setStatus(message);
                    progress.close("Error", true, true);
                }else{
                    progress.close();
                    createCompletePopUp();
                }
            }
        }
    }
    
    private void targetCheckStructure() throws Exception {
        TCComponentRevisionRule revRule = SDVPreBOMUtilities.getRevisionRule(session, "Latest Working");
        for(TCComponent solutionItemComponent : solutionList){
            TCComponent[] comps = solutionItemComponent.whereUsed(TCComponent.WHERE_USED_CONFIGURED, revRule);
            for (TCComponent comp : comps) {
                if (solutionItemComponent.getType().equals(TypeConstant.S7_PREFUNCMASTERREVISIONTYPE)) {
                    if (!comp.getType().equals(TypeConstant.S7_PREFUNCTIONREVISIONTYPE)) {
                        isOkValidation = false;
                        return;
                    }
                }else if (solutionItemComponent.getType().equals(TypeConstant.S7_PREVEHICLEPARTREVISIONTYPE)){
                    if (!comp.getType().equals(TypeConstant.S7_PREFUNCMASTERREVISIONTYPE) && !comp.getType().equals(TypeConstant.S7_PREVEHICLEPARTREVISIONTYPE)) {
                        isOkValidation = false;
                        return;
                    }
                }else{
                    isOkValidation = false;
                    return;
                }
            }
        }
    }

	/*	
	 * [CF-4358][20230901]Pre-BOM���� I-PASS(���Ű����ý���)���� �������̽� ���� �߰� ��û (SYSTEM, TEAM, CHARGER)
	 *  ������� �ʴ� ���� ������ ���� �Ϸ��� String type�� �޾� �Դµ� �ʿ� ��� ������
	 *  private void buildCCNEPL(WaitProgressBar progress, String type) throws Exception{ 
	*/

    @SuppressWarnings("static-access")
    private void buildCCNEPL(WaitProgressBar progress) throws Exception{
        BomUtil bomUtil = new BomUtil();
        CustomCCNDao dao = new CustomCCNDao();
        String ccn_id = changeRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMID);
        
        ArrayList<HashMap<String, Object>> arrResultEPL = null;
		/*	
		 * [CF-4358][20230901]Pre-BOM���� I-PASS(���Ű����ý���)���� �������̽� ���� �߰� ��û (SYSTEM, TEAM, CHARGER)
		 *	if������ oldüũ �ϴ� �κ��� ������ ���� ���� �������� ������� �ʾ� �ּ� ó�� buildCCNEPL__�� ����� 
		*/
//        if(type.equals("old")){
//        	arrResultEPL = bomUtil.buildCCNEPL(changeRevision, false, progress);
//        } else {
        	arrResultEPL = bomUtil.buildCCNEPL__(changeRevision, false, progress);
//        }
        //old
//        ArrayList<HashMap<String, Object>> arrResultEPL = bomUtil.buildCCNEPL(changeRevision, false, progress);
        //new
//        ArrayList<HashMap<String, Object>> arrResultEPL = bomUtil.buildCCNEPL__(changeRevision, false, progress);
        
//        System.out.println("insertCCNEplList start : "+ new Date());
        // ������ ���� DB ���̺� �ִ´�
        if (null != arrResultEPL && arrResultEPL.size() > 0) {
//        	if(type.equals("old")){
//        		dao.insertCCNEplList(ccn_id, arrResultEPL);
//            } else {
            	dao.insertCCNEplList_(ccn_id, arrResultEPL);
//            }
        	//old
//            dao.insertCCNEplList(ccn_id, arrResultEPL);
        	//new
//            dao.insertCCNEplList_(ccn_id, arrResultEPL);
        }
//        System.out.println("insertCCNMaster start : "+ new Date());
        
     // CCN Master ���̺� ���� �ִ´� (Validation�� ���� �ӽ÷� Data Insert)
        dao.deleteCCNMaster(ccn_id);
        dao.insertCCNMaster(changeRevision, false);
//        System.out.println("insertCCNMaster end : "+ new Date());
    }
    
    /**
     * CCN EPL Generate ���� �Ϸ� ���� Ȯ��
     * 
     * 1. Master List�� �����Ǿ��ִ��� ���� Ȯ��
     * 2. System Row Key �ߺ� ���� Ȯ��
     * 3. Usage ���� ���� Ȯ��
     * 4. Gate No�� Null�� �ԷµǾ��ִ� �׸� ���� ���� Ȯ��
     * 5. Project Type�� Null�� �ԷµǾ��ִ� �׸� ���� ���� Ȯ��
     * 6. ������ Part�� Cut, Paste �Ǿ��ִ� �׸� ���� ���� Ȯ��
     * 7. LEV�� �Է����� ���� �׸� ���� ���� Ȯ��
     * 
     * @return
     */
    private void validateCCNEPL() throws Exception {
    	StringBuffer sbMessage = new StringBuffer();
    	
    	CustomCCNDao dao = new CustomCCNDao();
    	ArrayList<HashMap<String, Object>> result = dao.selectCCNValidateMessage(changeRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMID).toString());

		for (int inx = 0; inx < result.size(); inx++) {
			HashMap<String, Object> hmResult = result.get(inx);
			Object oMessage = hmResult.get("MESSAGE");
			
			if (oMessage != null && !oMessage.toString().trim().equals("")) {
				sbMessage.append(oMessage.toString()).append("\n");
			}
		}
        
        if(sbMessage != null && !sbMessage.toString().trim().equals("")){
            msg = sbMessage.toString();
            displayMessage(msg, true);
            isOkValidation = false;
        }
    }
    
    private void createCompletePopUp() {
        final Shell shell = AIFUtility.getActiveDesktop().getShell();

        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
                box.setText(registry.getString("CCNProcessOperation.CCN.Information"));
                box.setMessage(registry.getString("CCNProcessOperation.CCN.RequestWorkflow"));
                box.open();
            }
        });
    }
    

    /**
     * CCN �۾� ����[����]�� ���� �ַ�Ǿ����� ��ũ ����
     * @throws Exception
     */
    private void addSolutionItems() throws Exception {
        // �ߺ� ���� ����
        solutionList = SDVPreBOMUtilities.getSolutionItemsAfterReGenerate(changeRevision);
        
        // Ÿ���� �ϳ��� ������ üũ
        String retrunMessage = "";
        if(null == solutionList || solutionList.length == 0){
            retrunMessage = "Solution target does not exist.";
        }
        
        if(!retrunMessage.equals("")){
            msg = retrunMessage;
            displayMessage(msg, true);
            isOkValidation = false;
        }
    }
    
    /**
     * ���� Ÿ�� Ȯ��
     * check out ���ε� ���� Ȯ��
     * dataset �Ӽ��� CCN NO�� �Է�
     * @throws TCException
     */
    private void getTargets() throws TCException, Exception{
        
        String checkOutlist = "";
        
        for(TCComponent solutionItemComponent : solutionList){
            TCComponentItemRevision solutionItemrevision = (TCComponentItemRevision) solutionItemComponent;
            
            if(solutionItemrevision.isCheckedOut()){
                checkOutlist = checkOutlist + solutionItemrevision + "\n";
            }
        }
        
        String retrunMessage = "";
        if(!checkOutlist.equals("")){
            retrunMessage = "Check-out Componet is exist.\nCheck belows and fix it.\n"+checkOutlist;
        }
        
        if(!retrunMessage.equals("")){
            msg = retrunMessage;
            displayMessage(msg, true);
            isOkValidation = false;
        }
    }
    
    /**
     * ���μ��� ����
     * @throws Exception
     */
    private void createProcess() throws Exception{
        SDVPreBOMUtilities.selfRelease(changeRevision, "CSR");
    }
    
    private void displayMessage(String msg, boolean nextLine) {
        if (msg == null){
            return;
        }
        if (progress != null)
            progress.setStatus(msg, nextLine);
    }
    
    /**
     * ���� �߼�
     * Vision-Net�� CALS ���ν��� ȣ��
     * @throws Exception
     */
    private void sendMail() throws Exception{
        String project = changeRevision.getProperty(PropertyConstant.ATTR_NAME_PROJCODE);
        String changeDesc = changeRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMDESC);
        
        String fromUser = session.getUser().getUserId();
        String title = "New PLM : CCN[" + ccnNo + "] ���� �Ϸ�";
        
        String body = "<PRE>";
        body += "New PLM ���� �Ʒ��� ���� CCN ���簡 �Ϸ� �Ǿ����� Ȯ�� �ٶ��ϴ�." + "<BR>";
        body += " -CCN NO. : " + ccnNo + "<BR>";
        body += " -Project : " + project + "<BR>";
        body += " -Change Desc. : " + changeDesc + "<BR>";
        body += " -��û�μ� : " + changeRevision.getTCProperty("owning_group") + "<BR>";
        body += " -��û��  : " + changeRevision.getTCProperty("owning_user") + "<BR>";
        body += "</PRE>";
        
        TCProperty referenceDeptCodeTCProperty = changeRevision.getTCProperty(PropertyConstant.ATTR_NAME_DEPLOYMENTTARGET);
        String[] deployTarget = referenceDeptCodeTCProperty.getStringArrayValue();
        if (null != deployTarget && deployTarget.length > 0) {
            for (int i = 0; i < deployTarget.length; i++) {
                String toUsers = deployTarget[i];
                // [20170309][ymjang] �����ڰ� ���� ���, ���� �߼� ���� ����
                if (toUsers != null) {
                    dao.sendMail(fromUser, title, body, toUsers);
                }
            }
            
        }
    }
    
    /**
     *  ���� ��� ��ư Ȱ��ȭ ��Ȱ��ȭ ���
     * @param isEnable
     */
    private void setEnableAppprvalButton(final boolean isEnable)
    {
    	if(ccnViewComposite == null)
    		return;
    	if(ccnViewComposite.getApprovalButton()== null)
    		return;
    	Shell shell = AIFUtility.getActiveDesktop().getShell();
    	shell.getDisplay().syncExec(new Runnable() {
            public void run() {
            	ccnViewComposite.getApprovalButton().setEnabled(isEnable);
            }
        });
    	
    }
}
