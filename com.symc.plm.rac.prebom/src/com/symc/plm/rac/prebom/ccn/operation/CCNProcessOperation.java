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
 * [20160715][ymjang] CCN EPL 데이터 보정
 * [20160718] IF CCN Master 정보 생성 로직 개선 --> Stored Procedure 로 이관함.
 * [20170309][ymjang] 수신자가 없을 경우, 메일 발송 오류 수정
 * APPS 의 MECO 프로세스를 카피해옴 
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
    
    private final static String EVENT_START = "  ▶";
    
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
     * [20160610]Approval 실행시 Approval 버튼  활성화/비활성화 하기위해서 생성자 추가
     * (버튼이 여러번 실행이 되기 때문)
     * @param session
     * @param changeRevision
     * @param ccnViewComposite CCN Viewer 의 Composite
     */
	/*	
	 * [CF-4358][20230901]Pre-BOM에서 I-PASS(구매관리시스템)으로 인터페이스 정보 추가 요청 (SYSTEM, TEAM, CHARGER)
	 *	데이터 생성 로직 변경으로 사용하지 않아 주석 처리 buildCCNEPL__을 사용함
	 *  사용하지 않는 생성 로직을 구분 할려고 String type값을 받아 왔는데 사용 하지 않는 로직 주석 처리로 필요 없어서 제거함 
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
            
            // # 1. CCN 작업 내용[을지]을 보고 솔루션아이템 링크 생성
            if (progress != null){
                progress.setStatus(EVENT_START + "Checking Solution Item(s)...", false);
            }
//            System.out.println("addSolutionItems start : "+ new Date());
            addSolutionItems();
//            System.out.println("addSolutionItems end : "+ new Date());
            if (progress != null){
                progress.setStatus("is done!");
            }
            
            // # 2. 프로세스 타겟 설정
            if (progress != null){
                progress.setStatus(EVENT_START + "Checking targets...", false);
            }
//            System.out.println("getTargets start : "+ new Date());
            getTargets();
//            System.out.println("getTargets end : "+ new Date());
            if (progress != null){
                progress.setStatus("is done!");
            }
            
            // # 3. 해당 타입이 BOM 에서 사용하는지 체크
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

            // # 4. CCN 을지 마스터 테이블 에Insert 한다
            if (progress != null){
                progress.setStatus(EVENT_START + "CCN EPL INFO Insert...", false);
            }

//            System.out.println("buildCCNEPL start : "+ new Date());
            buildCCNEPL(progress);
//            System.out.println("buildCCNEPL end : "+ new Date());
            if (progress != null){
                progress.setStatus("is done!");
            }
            
            // # 5. CCN 을지 마스터 정합성 확인
            if (progress != null){
            	progress.setStatus(EVENT_START + "Checking Validation...", false);
            }
//            System.out.println("validateCCNEPL start : "+ new Date());
            validateCCNEPL();
//            System.out.println("validateCCNEPL end : "+ new Date());
            if (progress != null){
            	progress.setStatus("is done!");
            }
            
            // Usage 값이 Null일 경우는 Validation에서 Pass
            String sTempMsg = msg;
            sTempMsg = sTempMsg.replace("MLM Usage is not generated!", "").trim();
            
            //[2016-07-07] 결재 일 경우만 적용
            if (progress != null){            	
	            if(!sTempMsg.equals("") && !isOkValidation) {
	                throw new Exception("Unsuccesfully completed to validate... Please check Log File!");
	            }
            }
            
            // # 6 자가결재 수행
            if (progress != null){
                progress.setStatus(EVENT_START + "Creating process...", false);
            }            
            // 결재가 두번 되는 현상 방지
            changeRevision.refresh();
            if (!SDVPreBOMUtilities.isReleased(changeRevision))
//            	System.out.println("createProcess start : "+ new Date());
            	createProcess();    
//            	System.out.println("createProcess end : "+ new Date());
          	if (progress != null){
                progress.setStatus("is done!");
            }
            
            // # 7 CCN EPL 데이터 보정
            if (progress != null){
            	progress.setStatus(EVENT_START + "Correcting CCN EPL...", false);
            }
            // [20160715][ymjang] CCN EPL 데이터 보정
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
            
            // # 8 IF CCN Master 저장
            if (progress != null){
                progress.setStatus(EVENT_START + "Saving I/F CCN Master...", false);
            }
            // [20160718] IF CCN Master 정보 생성 로직 개선 --> Stored Procedure 로 이관함.
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
            
            // # 9 메일 발송 
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
                progress.setStatus("＠ Error Message : ");
                message = " " + e.getMessage();
            }
            
            /**
             * 오류시 버튼 활성화
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
	 * [CF-4358][20230901]Pre-BOM에서 I-PASS(구매관리시스템)으로 인터페이스 정보 추가 요청 (SYSTEM, TEAM, CHARGER)
	 *  사용하지 않는 생성 로직을 구분 하려고 String type를 받아 왔는데 필요 없어서 제거함
	 *  private void buildCCNEPL(WaitProgressBar progress, String type) throws Exception{ 
	*/

    @SuppressWarnings("static-access")
    private void buildCCNEPL(WaitProgressBar progress) throws Exception{
        BomUtil bomUtil = new BomUtil();
        CustomCCNDao dao = new CustomCCNDao();
        String ccn_id = changeRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMID);
        
        ArrayList<HashMap<String, Object>> arrResultEPL = null;
		/*	
		 * [CF-4358][20230901]Pre-BOM에서 I-PASS(구매관리시스템)으로 인터페이스 정보 추가 요청 (SYSTEM, TEAM, CHARGER)
		 *	if문으로 old체크 하는 부분은 데이터 생성 로직 변경으로 사용하지 않아 주석 처리 buildCCNEPL__을 사용함 
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
        // 가져온 값을 DB 테이블에 넣는다
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
        
     // CCN Master 테이블에 값을 넣는다 (Validation을 위해 임시로 Data Insert)
        dao.deleteCCNMaster(ccn_id);
        dao.insertCCNMaster(changeRevision, false);
//        System.out.println("insertCCNMaster end : "+ new Date());
    }
    
    /**
     * CCN EPL Generate 정상 완료 여부 확인
     * 
     * 1. Master List가 생성되어있는지 여부 확인
     * 2. System Row Key 중복 여부 확인
     * 3. Usage 누락 여부 확인
     * 4. Gate No가 Null로 입력되어있는 항목 존재 여부 확인
     * 5. Project Type이 Null로 입력되어있는 항목 존재 여부 확인
     * 6. 동일한 Part가 Cut, Paste 되어있는 항목 존재 여부 확인
     * 7. LEV을 입력하지 않은 항목 존재 여부 확인
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
     * CCN 작업 내용[을지]을 보고 솔루션아이템 링크 생성
     * @throws Exception
     */
    private void addSolutionItems() throws Exception {
        // 중복 방지 삭제
        solutionList = SDVPreBOMUtilities.getSolutionItemsAfterReGenerate(changeRevision);
        
        // 타겟이 하나도 없는지 체크
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
     * 결재 타켓 확인
     * check out 여부도 같이 확인
     * dataset 속성에 CCN NO도 입력
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
     * 프로세스 생성
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
     * 메일 발송
     * Vision-Net의 CALS 프로시져 호출
     * @throws Exception
     */
    private void sendMail() throws Exception{
        String project = changeRevision.getProperty(PropertyConstant.ATTR_NAME_PROJCODE);
        String changeDesc = changeRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMDESC);
        
        String fromUser = session.getUser().getUserId();
        String title = "New PLM : CCN[" + ccnNo + "] 결재 완료";
        
        String body = "<PRE>";
        body += "New PLM 에서 아래와 같이 CCN 결재가 완료 되었으니 확인 바랍니다." + "<BR>";
        body += " -CCN NO. : " + ccnNo + "<BR>";
        body += " -Project : " + project + "<BR>";
        body += " -Change Desc. : " + changeDesc + "<BR>";
        body += " -요청부서 : " + changeRevision.getTCProperty("owning_group") + "<BR>";
        body += " -요청자  : " + changeRevision.getTCProperty("owning_user") + "<BR>";
        body += "</PRE>";
        
        TCProperty referenceDeptCodeTCProperty = changeRevision.getTCProperty(PropertyConstant.ATTR_NAME_DEPLOYMENTTARGET);
        String[] deployTarget = referenceDeptCodeTCProperty.getStringArrayValue();
        if (null != deployTarget && deployTarget.length > 0) {
            for (int i = 0; i < deployTarget.length; i++) {
                String toUsers = deployTarget[i];
                // [20170309][ymjang] 수신자가 없을 경우, 메일 발송 오류 수정
                if (toUsers != null) {
                    dao.sendMail(fromUser, title, body, toUsers);
                }
            }
            
        }
    }
    
    /**
     *  결재 상신 버튼 활성화 비활성화 기능
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
