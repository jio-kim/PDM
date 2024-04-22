package com.symc.plm.me.sdv.operation.report;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.common.IDialog;
import org.sdv.core.ui.UIManager;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCSoaWebUtil;
import com.ssangyong.common.remote.SecurityUtil;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.teamcenter.rac.aif.AIFPortal;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentCCObject;
import com.teamcenter.rac.kernel.TCComponentPerson;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class ReportSubsidiaryAllListSOAOperation extends AbstractTCSDVOperation {

    protected int mode;
    protected String dialogId;
    protected String titleDescription;
	
	private Registry registry;
    
	public ReportSubsidiaryAllListSOAOperation() {
	}
	
	/**
	 * �޽�����  �����ִ� Dialog�� �����ֵ��� ó���Ѵ�.
	 */
	@Override
	public void startOperation(String commandId) {
		
		System.out.println("SubsidiaryMaterialListSOAOperation : startOperation");

		 final Shell shell = AIFUtility.getActiveDesktop().getShell();
        shell.getDisplay().syncExec(new Runnable() {
        	
            @Override
            public void run() {
                int returnValue = -1;

                try {
                    IDialog dialog = UIManager.getDialog(shell, dialogId);
                    ((DialogStubBean) dialog.getStub()).setDescription(titleDescription);
                    returnValue = dialog.open();

                    System.out.println("returnValue = "+returnValue);

                    // Close ��ư�� ���� ���
                    if(returnValue == 0){
                    	mode = 0;
                    	cancel();
                    // OK ��ư�� ���� ���
                    } else if(returnValue == 1) {
                        mode = 1;
                    }
                } catch(Exception e) {
                    returnValue = -1;
                    cancel();
                    MessageBox.post(shell, e.getMessage(), "Report", MessageBox.ERROR);
                }
            }
        });
		
	}
	
	/**
	 * Operation�� �����ϴ� �κ�
	 */
	@Override
	public void executeOperation() throws Exception {
		
		System.out.println("SubsidiaryMaterialListSOAOperation : executeOperation");
		
		if(mode != 1){
			System.out.println("SubsidiaryMaterialListSOAOperation : Cancle()");
			// Close ��ư�� ������ ��� �̹Ƿ� Operation�� ���� ���� �ʵ��� �Ѵ�.
		}
		
		registry = Registry.getRegistry(this);

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm");
		String startTime = df.format(new Date());
		String expandingTime = null;
		String endTime = null;

		
		// ���� ���� ������ ���⿡ ��� �Ѵ�.
		AIFPortal  aAIFPortal  = AIFUtility.getAIFPortal();

		MFGLegacyApplication mfgLegacyApplication = null;
		AbstractAIFUIApplication application = 
			AIFUtility.getActiveDesktop().getCurrentApplication();
		if(application instanceof MFGLegacyApplication){
			mfgLegacyApplication = (MFGLegacyApplication)application;
		}
		
        TCSession session = (TCSession)application.getSession();
        String userId = session.getUser().getUserId();
        String userPass = session.getCredentials().getPassword();
        
        String ccName = null;
        TCComponentCCObject ccObject = mfgLegacyApplication.getBOMWindow().getCC();
        // [20240423][UPGRADE] MECollaborationContext �� ���� ��� ���� ������ �������� �ʵ��� �߰� 
//        System.out.println("ccObject : "+ccObject.toString());
        if(ccObject!=null && ccObject.getType().equalsIgnoreCase("MECollaborationContext")==true){
        	ccName = ccObject.getProperty("object_name");
        } else {
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "There are no MECollaborationContext in Target Item.", "failure", MessageBox.ERROR);		
        	return;
        }
        
        //System.out.println("userId = "+userId);
        //System.out.println("userPass = "+userPass);
        
        String userEncId = null;
        String userEncPass = null;
        userEncId = SecurityUtil.encrypt(userId);
        userEncPass = SecurityUtil.encrypt(userPass);
        System.out.println("userEncId = "+userEncId);
        System.out.println("userEncPass = "+userEncPass);
        
        DataSet ds = new DataSet();
        ds.put("userId", userEncId);
		ds.put("password", userEncPass);
		/**
         * [SR180212-044] BOP Paint ������ Report �߰� ���� �޴� ���� �Ƿ�
         *  ������ ��� ������ List ��� �� �������� ��� �Ͽ� Data�� �����ϰ� �Ǿ� ������ 
         *  Ƽ���� ������ ��� �� ������ �����ϰ� ���� �ؾ� �ϴ� �� ����� �����縦 �� ���� ����.
         */
		ds.put("typeOfReport", "Subsidiary Material All List Report");
		ds.put("ccObjectName", ccName);
		
		SYMCSoaWebUtil soaWebUtil = new SYMCSoaWebUtil();
		String returnVal = (String)soaWebUtil.execute("com.ssangyong.soa.service.BOPReportService",
			"makeReport",
			ds);
		
		if(returnVal!=null && returnVal.toUpperCase().startsWith("SUCCEED")==true){
//			String messageStr = returnVal;
//			final Shell shell = AIFUtility.getActiveDesktop().getShell();
//			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), messageStr, "Succeed", MessageBox.INFORMATION);		            
		}else{
			String messageStr = "Error while processing web service request";
			if(returnVal!=null){
				messageStr =  returnVal;
			}
			final Shell shell = AIFUtility.getActiveDesktop().getShell();
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), messageStr, "failure", MessageBox.ERROR);		            
		}
		System.out.println("returnVal = "+returnVal);
		
		expandingTime = df.format(new Date());
		endTime = df.format(new Date());
		System.out.println("Start Time = "+startTime);
		System.out.println("Expand  Time = "+expandingTime);
		System.out.println("End Time = "+endTime);

	}

	/**
	 * �������� ���� �߰� ó���� �ʿ��ϸ� ���⿡ ���� �Ѵ�.
	 */
	@Override
	public void endOperation() {
		// Cancle ���� ���� ��� ������� ���� �ȴ�.
		// �߰��� Close ��ư�� ���� Cancle �ϰԵǸ� executeOperation(), endOperation()�� ���� ���� �ʴ´�.
		System.out.println("SubsidiaryMaterialListSOAOperation : endOperation");
		
	}
    
    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }	
    public String getDialogId() {
        return dialogId;
    }

    public void setDialogId(String dialogId) {
        this.dialogId = dialogId;
    }
    
    public String getTitleDescription() {
        return titleDescription;
    }

    public void setTitleDescription(String titleDescription) {
        this.titleDescription = titleDescription;
    }

}
