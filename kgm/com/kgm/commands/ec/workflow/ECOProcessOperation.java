package com.kgm.commands.ec.workflow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.kgm.commands.ec.SYMCECConstant;
import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.commands.workflow.SYMCDecisionDialog;
import com.kgm.commands.workflow.changetoreplace.ChangeToReplace;
import com.kgm.common.SYMCClass;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.PreferenceService;
import com.kgm.common.utils.ProcessUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.kgm.dto.ApprovalLineData;
import com.kgm.rac.kernel.SYMCBOMEditData;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCAttachmentScope;
import com.teamcenter.rac.kernel.TCAttachmentType;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentContextList;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentProcessType;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentSignoffType;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentTaskTemplateType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCSignoffOriginType;
import com.teamcenter.rac.kernel.TCTaskState;
import com.teamcenter.rac.pse.services.PSEApplicationService;

/**
 * ���� ECO ��� �� ECO ���μ��� ���� �� �ݷ� �� �� ��� �� ȣ�� ��.
 * 
 * [SR140702-056][20140422] modified yunjae, ���� ���� ���� ����. 
 * SRME::[�ݿ�����][SR140702-027][20140702] bskwak, status ���� ó���� WF handler�� �̰�, (��û ������, WF handler�۾�:������)
 * [20161117][ymjang] CM���� ���۵Ǵ� ECO �� S201 �ε� ������� ECO �μ� ������ ������.
 * [20170613][ljg] OrderNo �ߺ� �Ǵ� üũ ������ ������ ������ �Ϻ� ���� �ʾƼ�, üũ ���� �߰� ��.
 * [SR170828-015][LJG]Chassis module ������ ���� ���� ���� �߰� ��û
 *  1. ECO ���� Part S/mode : P7YP8, C0YP8, P7CP8, P7UP8, PDYP8, P7
 * 	2. Module code : FCM or RCM
 * 	3. Part�� Option : Z999�� �����ϴ� ���
 * [SR180130-037][LJG] ECO Validation ������ ����
 * [20180206][LJG] ������ �ʼ����� -> �������� �߰� �Ǹ� �ȵǴ°ŷ� ����
 * [SR180207-027][LJG] ������ȹ���� �߰��Ǹ� �ȵǰ�, ����������� �߰� �Ǿ����
 * [CF-1635] [20201208 by SYChon] ������Ʈ ������ ���� ���� ���� �߰� ��û - �����ɼ��ڵ� ���Է��� ��쿡�� ECO ����� �Ǵ� ���� �ذ�
 * @author DJKIM
 *
 */
public class ECOProcessOperation extends AbstractAIFOperation {

	private TCSession session;
	private TCComponentChangeItemRevision changeRevision;
	private String ecoNo;

	private TCComponentProcess newProcess;

	private ArrayList<TCComponent> targetList = new ArrayList<TCComponent>();
	private HashMap<String, ArrayList<TCComponentGroupMember>> reviewers = new HashMap<String, ArrayList<TCComponentGroupMember>>();
	private TCComponent[] solutionList;
	private TCComponent[] problemList;

	private WaitProgressBar progress;
	private String message;

	private CustomECODao dao = new CustomECODao();
	private SYMCDecisionDialog parent;

	private int iReturnCode = -1;

	private final static String EVENT_START = "  ��";

	public ECOProcessOperation(TCSession session, TCComponentChangeItemRevision changeRevision) {
		this.session = session;
		this.changeRevision = changeRevision;
	}

	public ECOProcessOperation(SYMCDecisionDialog parent, TCSession session, TCComponentChangeItemRevision changeRevision) {
		this.session = session;
		this.changeRevision = changeRevision;
		this.parent = parent;
	}

	@Override
	public void executeOperation() throws Exception {
		try {
			if(parent != null && changeRevision == null){
				String processName = parent.getCurrentJob().getName();
				String ecoNo = processName.substring(processName.indexOf("[")+1, processName.indexOf("]"));
				String ecoRevisionPuid = dao.getEcoRevisionPuid(ecoNo);
				changeRevision = (TCComponentChangeItemRevision) session.stringToComponent(ecoRevisionPuid);
				// 20130611, �����C, ��ũ�÷ο� ���� �� �� Ÿ���� ���� �������� ��쿡 eco revision�� ã�Ƽ� �ٿ��ִ� ���� 
				parent.getCurrentJob().getRootTask().add("root_target_attachments", changeRevision); 
			}
			
			ecoNo = changeRevision.getProperty("item_id");
			/**
			 * [SR141120-043][2014.11.21][jclee] ECO ���� �� Color ID�� None�� �ƴϸ鼭 Color Section No�� ���� ��� Warning Message Box Open
			 */
			iReturnCode = -1;
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						iReturnCode = checkColorID();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			if (iReturnCode == SWT.NO) {
				return;
			}

			/**
			 * [SR141205-027][2014.12.16][jclee] Color ID�� ����� �׸��� ���� ��� Warning Message Box Open
			 */
			iReturnCode = -1;
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						iReturnCode = checkColorIDChanged();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			if (iReturnCode == SWT.NO) {
				return;
			}

			/**
			 * [SR150313-028][2015.03.25][jclee] Change Type�� F2�� �׸� �� ���� S/Mode�� ���Ͽ� ���� �ٸ� Part ����� ������ ��� Warning Message Box Open
			 */
			iReturnCode = -1;
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						iReturnCode = selectECOEplCOSModeCompareList();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			if (iReturnCode == SWT.NO) {
				return;
			}

			//			/**
			//			 * [SR����][20151016][jclee] EPL Cut �� Revise�Ͽ� �ٽ� Paste�� ���(Revise �̷� ����)
			// [SR����][20160205][jclee] ���� ����. ������ ���� ��û.
			//			 */
			//			iReturnCode = -1;
			//			Display.getDefault().syncExec(new Runnable() {
			//				@Override
			//				public void run() {
			//					try {
			//						iReturnCode = getCANNOTGeneratedReviseList();
			//					} catch (Exception e) {
			//						e.printStackTrace();
			//					}
			//				}
			//			});
			//			
			//			if (iReturnCode == SWT.OK) {
			//				return;
			//			}

			//[SR170828-015][LJG]Chassis module ������ ���� ���� ���� �߰� ��û
			// 1. ECO ���� Part S/mode : P7YP8, C0YP8, P7CP8, P7UP8, PDYP8, P7
			// 2. Module code : FCM or RCM
			// 3. Part�� Option : Z999�� �����ϴ� ���
			// [20191024]���躯�� ���� �ӵ� ���� ������Ʈ���� �̻���ϱ�� ����(���������)
//			iReturnCode = -1;
//			Display.getDefault().syncExec(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						iReturnCode = checkChassisModule();
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			});
//
//			if (iReturnCode == SWT.OK) {
//				return;
//			}
			
			// Structure Manager���� �������� BOMWindow �ִ��� üũ
//			iReturnCode = -1;
//			Display.getDefault().syncExec(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						iReturnCode = checkBOMEdit();
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			});
//
//			if (iReturnCode == SWT.OK) {
//				return;
//			}

			if(parent == null){
				progress = new WaitProgressBar(AIFUtility.getActiveDesktop().getFrame());
			}else{
				progress = new WaitProgressBar(parent);
			}
			
			progress.setWindowSize(500, 400);
			progress.start();						
			progress.setShowButton(true);
			progress.setStatus("ECO Workflow creation start.");
			progress.setAlwaysOnTop(true);

			//üũ�� ����Ʈ totalCount�� üũ ����Ʈ�� �ϳ��� �þ ������ totalCount ������ �����ؾ� �Ѵ�.
			int totalCount = 23;
			//���� �� üũ ���� �ܰ踦 �����ִ� ���� checkCount++�� �ϳ��� ī��Ʈ�� �÷��ش�.
			int checkCount = 1;
			
			// Structure Manager���� �������� BOMWindow �ִ��� üũ
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking not saved BOMWindow...", false);
			checkBOMEdit();
			progress.setStatus("is done!");
			
			// # 0. FIXED, 2013.06.01, Ÿ�ٸ���Ʈ���� ECORevision�� ������ ��� ã�Ƽ� �ٿ� �ְ�, CreateWorkflow�� ���� �޽�¡
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking ECO has Workflow...", false);
			checkHasWorkflow();
			progress.setStatus("is done!");
			
			// # 1. ECO EPL GENERATE CHECK
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking ECO EPL generation...", false);
			checkChangeEPL();
			progress.setStatus("is done!");
			
			
			// # 2-1. Cut and Paste ����
			//SRME::[][20140926] Yun sung won. Cut & paste �� TC BOM ���� ����.
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Correcting 'Cut and Paste'...", false);
			cutNpasteCorrecting();		
			progress.setStatus("is done!");
			
			/** 
			 * [SR150213-010][2015.02.25][jclee] EPL���� Ư�� FMP ���� 1Lv Part �� Supply Mode�� P�� �����ϴ� EPL�� Car Project�� �����ϰ����� ���� ���
			 */
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking CarProject...", false);
			getCarProjectInEPL();
			progress.setStatus("is done!");

			// [CF-1635] [20201208 by SYChon] ������Ʈ ������ ���� ���� ���� �߰� ��û - �����ɼ��ڵ� ���Է��� ��쿡�� ECO ����� �Ǵ� ���� �ذ�
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking S/Mode P7/PD...", false);
			checkSModeP7PD();
			progress.setStatus("is done!");
			
			
			/**
			 * [2015.02.25][jclee] ECO No�� ���� �����鼭 EPL�� New Part No �� ���ԵǾ� ���� �ʴ� Part ����� ������ ���
			 */
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking Missing parts in the EPL...", false);
			getCANNOTGeneratedList();
			progress.setStatus("is done!");
			
			/**
			 * [20170613][ljg] OrderNo �ߺ� üũ ���� �߰�(�ߺ��Ǹ� �ȵ�)
			 */
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking Duplicate Order No...", false);
			duplicateOrderNoCheck();
			progress.setStatus("is done!");

			// # 1.1. EPL VC VALIDATION
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking ECO EPL VC...", false);
			getDuplicateCategoryInVC();
			progress.setStatus("is done!");

			// # 2. ECO EPL VALIDATION
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking ECO EPL...", false);
			checkEPL();
			progress.setStatus("is done!");

			// # 2-2. Function ���� ���� Ȯ��
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking BOM Connection With Function...", false);
//			System.out.println("checkParentConnection Start Time : "+ new Date());
			checkParentConnection();	
//			System.out.println("checkParentConnection End Time : "+ new Date());
			progress.setStatus("is done!");
			
			// # 2-3. Ư��������Ʈ�� ���� SYSTEM CODE Null Check
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking Null Value (System Code)...", false);
			checkNullValue();		
			progress.setStatus("is done!");
			
			// # 3-3. Old Part �� Revision ���ռ� üũ
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking Old Part Revision...", false);
			checkOldRevIsMatched();
			progress.setStatus("is done!");
			
			// # 4. ECO �۾� ����[C��]�� ���� ���������� ��ũ ����
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking Problem Items...", false);
			addProblemItems();
			progress.setStatus("is done!");

			// # 5. ECO �۾� ����[C��]�� ���� �ַ�Ǿ����� ��ũ ����
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking Solution Items...", false);
			addSolutionItems();
			progress.setStatus("is done!");
			
			/**
			 * [20160415][jclee] Solution Part List �� Part �� Material, Alt Material Part�� Obsolete�� �׸��� ���� ���
			 */
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking Obsoleted Material Part...", false);
			checkObsoleteMaterial();
			progress.setStatus("is done!");

			// # 6. ECO Affected Project ���� ����
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking Affected Project...", false);
			setArrectedProject();
			progress.setStatus("is done!");

			// # 3. ���缱 Ȯ��
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking approval line...", false);
			checkReviewer();
			// # 3-2.���缱 �ߺ����� üũ
			checkDupApprovalLines();
			progress.setStatus("is done!");

			// # 7. ECO_EPL�� DVP �׸� DR1,DR2�� �ϳ��� ������ �������� �������� ������ �߰�
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking check Certification Team...", false);
			//[SR180130-037][LJG] ECO Validation ������ ���� -> �ּ� ó����
			//[20180206][LJG] ������ �ʼ����� -> �������� �߰� �Ǹ� �ȵǴ°ŷ� ����
			checkCertification();
			progress.setStatus("is done!");

			// # 7-1.[SR180207-027][LJG] ���� ��ȹ ���� �߰��Ǹ� �ȵǰ�, ���� ������� �߰� �Ǿ����
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +") Checking check Cost Team...", false);
			checkCostTeam();
			progress.setStatus("is done!");

			// # 8. BOM Structure �󿡼� end item �ؿ� end item�� �����ϸ� �ʵȴ�
			//[20191025] ���躯�� ���� �ӵ� ���� ������Ʈ���� ���� (���������) 
//			progress.setStatus(EVENT_START+"Checking end item to end item relation...", false);
//			checkEndToEnd();
//			progress.setStatus("is done!");

			// ## ���μ��� Ÿ�� ����
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +")Checking targets...", false);
			getTargets();
			progress.setStatus("is done!");

			/*
			 * [20230406][CF-3876]
			 * Vehicle ECO�� EPL Proj�Ӽ��� �Ŀ�Ʈ���� ������Ʈ�� ���� �ϴ��� üũ 
			 * Vehicle ECO(���� ECO)�� EPL Proj�Ӽ��� �Ŀ�Ʈ���� ������Ʈ ����� ���� ó�� 
			 * �Ŀ�Ʈ���� ������Ʈ�� Power Traing ECO(���� ECO)�� �۾� �ؾ��Ѵ�.*/
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +")Checking Power Train check ...", false);
			checkPowerTrain();
			progress.setStatus("is done!");

			/*
			 *[CF-4217][20230719]ECO ���� ���� 35����Ʈ �̳� �ۼ�
			 * ���� �߻� ���� : I/F������ ������ 35����Ʈ�� �ʰ��Ͽ� �����߻� (PLM�ִ� �Է°�(40), IF_LEC_ECO ���̺� �÷� �ִ� �Է°�(35))
			 * ���� ���� : ���� �� Change Eff. Point���� 35����Ʈ�� �Ѿ�� �� üũ �ϰ� 35����Ʈ�� �Ѿ �� ���� �޽��� �߻� 
			 * */
			progress.setStatus(EVENT_START+"(" + checkCount++ + "/" + totalCount +")Change Eff. Point check ...", false);
			checkChangeEffPoint();
			progress.setStatus("is done!");
			
			// ## ���� ����
			// [SR140702-027][20140702] bskwak, status ���� ó���� WF handler�� �̰�, (��û ������, WF handler�۾�:������)
			// 2014.10.31 jclee �ݿ� �Ϸ�.
			//			progress.setStatus(EVENT_START+"Change Status...", false);
			//			changeStatus();
			//			progress.setStatus("is done!");

			if(parent == null){
				// ## ���μ��� ����
				progress.setStatus(EVENT_START+"Creating process...", false);
				createProcess();
				progress.setStatus("is done!");

				// ## Ÿ��ũ �Ҵ�
				progress.setStatus(EVENT_START+"Assigning...", false);
				assignSignoffs();
				progress.setStatus("is done!");

				// ## ���� �߼� 
				progress.setStatus(EVENT_START+"Mailing...", false);
				sendMail();
				progress.setStatus("is done!");
			}
			
			//[20180718][CSH]End Item���� 500�� �ʰ��� HBOM(�̱��� ����)�� Mail�뺸
			//[20191211]�Ѽ��� ������ �ʿ���ٰ� �ϳ�. �ּ�
//			senMailEndItemCount();

		}catch(Exception e){
			e.printStackTrace();
			if(progress != null){
				progress.setStatus("is fail!");
				progress.setStatus("�� Error Message : ");
				message = " " + e.getMessage();
				rollback();
			}
		}finally{
			if(progress != null){
				if(message != null){
					progress.setStatus(message);
					progress.close("Error", true, true);
				}else{
					progress.close();
					
					if(parent != null){
						parent.runOperation();
					} else {
						createCompletePopUp();
					}
				}
			}
		}
	}

	/**
	 * [SR141120-043][2014.11.21][jclee] ECO ���� �� Color ID�� None�� �ƴϸ鼭 Color Section No�� ���� ��� Warning Message Box Open
	 * OK : ���� ����
	 * Cancel : ���� ���� ���
	 * @throws Exception
	 */
	private int checkColorID() throws Exception {
		DataSet ds = new DataSet();
		ds.put("ecoNo", ecoNo);
		ArrayList<HashMap<String, String>> results = dao.getColorIDWarningList(ds);

		if (results.size() > 0) {
			StringBuffer sbMessage = new StringBuffer();
			for (int inx = 0; inx < results.size(); inx++) {
				HashMap<String, String> result = results.get(inx);
				String sPartNo = result.get("PART_NO");
				String sPartRev = result.get("PART_REV");
				String sColorID = result.get("COLOR_ID");

				sbMessage.append(sPartNo);
				sbMessage.append("/");
				sbMessage.append(sPartRev);
				sbMessage.append(" ");
				sbMessage.append("Color ID : ");
				sbMessage.append(sColorID);
				sbMessage.append("\n");
			}
			sbMessage.append("\n");
			sbMessage.append("These Parts have no Color Section No.\n");
			sbMessage.append("Do you want to proceed?");

			org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.YES | SWT.NO);
			box.setText("Ask Proceed");
			box.setMessage(sbMessage.toString());

			return box.open();
		}
		return -1;
	}

	/**
	 * [SR141205-027][2014.12.16][jclee] Color ID�� ����� �׸��� ������ ��� Warning Message Box Open
	 * OK : ���� ����
	 * Cancel : ���� ���� ���
	 * @throws Exception
	 */
	private int checkColorIDChanged() throws Exception {
//		CustomECODao dao = new CustomECODao();
		DataSet ds = new DataSet();
//		String sECONo = changeRevision.getProperty("item_id");
		ds.put("ecoNo", ecoNo);
		ArrayList<HashMap<String, String>> results = dao.getColorIDChangingList(ds);

		if (results.size() > 0) {
			StringBuffer sbMessage = new StringBuffer();
			for (int inx = 0; inx < results.size(); inx++) {
				HashMap<String, String> result = results.get(inx);
				String sNewPartNo = result.get("NEW_PART_NO");
				String sNewPartRev = result.get("NEW_PART_REV");
				String sOldColorID = result.get("OLD_PART_COLOR_ID");
				String sNewColorID = result.get("NEW_PART_COLOR_ID");

				sbMessage.append(sNewPartNo);
				sbMessage.append("/");
				sbMessage.append(sNewPartRev);
				sbMessage.append(" ");
				sbMessage.append("Color ID : ");
				sbMessage.append(sOldColorID);
				sbMessage.append(" -> ");
				sbMessage.append(sNewColorID);
				sbMessage.append("\n");
			}
			sbMessage.append("\n");
			sbMessage.append("These Parts have changed Color ID.\n");
			sbMessage.append("Do you want to proceed?");

			org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.YES | SWT.NO);
			box.setText("Ask Proceed");
			box.setMessage(sbMessage.toString());

			return box.open();
		}
		return -1;
	}

	/**
	 * [SR150213-010][2015.02.25][jclee] EPL���� Ư�� FMP ���� 1Lv Part �� Supply Mode�� P�� �����ϴ� EPL�� Car Project�� �����ϴ��� ��ȸ
	 * @return
	 * @throws Exception
	 */
	private void getCarProjectInEPL() throws Exception {
//		CustomECODao dao = new CustomECODao();
		DataSet ds = new DataSet();
//		String sECONo = changeRevision.getProperty("item_id");
		ds.put("ecoNo", ecoNo);
		ArrayList<HashMap<String, String>> results = dao.getCarProjectInEPL(ds);
		int iResult = 0;

		if (results.size() > 0) {
			StringBuffer sbMessage = new StringBuffer();
			for (int inx = 0; inx < results.size(); inx++) {
				HashMap<String, String> result = results.get(inx);
				String sNewPartNo = result.get("NEW_PART_NO");
				String sNewProjectCode = result.get("NEW_PROJECT");

				if (sNewProjectCode == null || sNewProjectCode.length() == 0 || sNewProjectCode.equals("")) {
					sbMessage.append(sNewPartNo);
					sbMessage.append("\n");
					iResult++;
				}
			}

			if (iResult != 0) {
				sbMessage.append("\n");
				sbMessage.append("These Part's EPLs have no specific Project Codes.\n");
				sbMessage.append("Contact to Administrator.\n");

				throw (new Exception(sbMessage.toString()));
			}
		}
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private void getCANNOTGeneratedList() throws Exception {
//		CustomECODao dao = new CustomECODao();
		DataSet ds = new DataSet();
//		String sECONo = changeRevision.getProperty("item_id");
		ds.put("ecoNo", ecoNo);
		ArrayList<HashMap<String, String>> results = dao.getCANNOTGeneratedList(ds);

		if (results.size() > 0) {
			StringBuffer sbMessage = new StringBuffer();
			for (int inx = 0; inx < results.size(); inx++) {
				HashMap<String, String> result = results.get(inx);
				String sPartNo = result.get("PART_NO");
				String sPartRev = result.get("PART_REV");

				sbMessage.append(sPartNo).append("/").append(sPartRev);
				sbMessage.append("\n");
			}

//			sbMessage.append("\n");
			sbMessage.append("Check These Part's revision \n");
			sbMessage.append("�ش� �������� Part�� ���� �Ǿ����� EPL�� ���ԵǾ����� �ʽ��ϴ�.\nBOM�� �����Ǿ� �ִ��� Ȯ���ϼ���.");

//			org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.OK);
//			box.setText("Check EPL");
//			box.setMessage(sbMessage.toString());
//
//			return box.open();
			throw (new Exception(sbMessage.toString()));
		}
//		return -1;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private int selectECOEplCOSModeCompareList() throws Exception {
//		CustomECODao dao = new CustomECODao();
//		String sECONo = changeRevision.getProperty("item_id");
		ArrayList<HashMap<String, String>> results = dao.selectECOEplCOSModeCompareList(ecoNo);

		if (results.size() > 0) {
			StringBuffer sbMessage = new StringBuffer();
			for (int inx = 0; inx < results.size(); inx++) {
				HashMap<String, String> result = results.get(inx);
				String sPartNo = result.get("NEW_PART_NO");
				String sPartRev = result.get("NEW_PART_REV");

				sbMessage.append(sPartNo).append("/").append(sPartRev);
				sbMessage.append("\n");
			}
			sbMessage.append("\n");
			sbMessage.append("Check These Part's S/Mode are changed.\n");
			sbMessage.append("Do you want to proceed?");

			org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.YES | SWT.NO);
			box.setText("Ask Proceed");
			box.setMessage(sbMessage.toString());

			return box.open();
		}
		return -1;
	}

	/**
	 * EPL Cut �� Revise�Ͽ� �ٽ� Paste�� ��� Ȯ�� (Revise �̷� ����)
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private int getCANNOTGeneratedReviseList() throws Exception {
//		CustomECODao dao = new CustomECODao();
		DataSet ds = new DataSet();
//		String sECONo = changeRevision.getProperty("item_id");
		ds.put("ecoNo", ecoNo);
		ArrayList<HashMap<String, String>> results = dao.getCANNOTGeneratedReviseList(ds);

		if (results.size() > 0) {
			StringBuffer sbMessage = new StringBuffer();
			for (int inx = 0; inx < results.size(); inx++) {
				HashMap<String, String> result = results.get(inx);
				String sPartNo = result.get("PART_NO");

				sbMessage.append(sPartNo);
				sbMessage.append("\n");
			}

			sbMessage.append("\n");
			//2023-10 �������� �ϵ� �ڵ��� �׷���� Preference�� ���� 
			sbMessage.append("These Parts are omitted from the revise history. Contact to the " + PreferenceService.getValue("RnD MANAGEMENT") + ".");

			org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.OK);
			box.setText("Check EPL");
			box.setMessage(sbMessage.toString());

			return box.open();
		}
		return -1;
	}

	/**
	 * [20160415][jclee] Solution Part List �� Part �� Material, Alt Material Part�� Obsolete�� �׸��� ���� ��� Error Message Box Open
	 * @return
	 * @throws Exception
	 */
	private void checkObsoleteMaterial() throws Exception {
		HashMap<String, HashMap<String, String>> result = new HashMap<String, HashMap<String, String>>();
		TCComponent[] cSolutions = this.changeRevision.getReferenceListProperty("CMHasSolutionItem");

		for (int inx = 0; inx < cSolutions.length; inx++) {
			TCComponent cSolution = cSolutions[inx];
			HashMap<String, String> hmResult = new HashMap<String, String>();

			if (cSolution instanceof TCComponentItemRevision) {
				TCComponentItemRevision irSolution = (TCComponentItemRevision) cSolution;

				if (irSolution.getType().equals("S7_VehpartRevision")) {
					TCComponentContextList materials = irSolution.getRelatedList(new String[] {"s7_MATERIAL", "s7_ALT_MATERIAL"});
					TCComponent[] caMaterials = materials.toTCComponentArray();

					for (int jnx = 0; jnx < caMaterials.length; jnx++) {
						String sMaturity = caMaterials[jnx].getProperty("s7_MATURITY");

						if (sMaturity.equals("Obsolete")) {
							hmResult.put(caMaterials[jnx].getType(), caMaterials[jnx].toString());
						}
					}
				}
			}

			if (!hmResult.isEmpty()) {
				result.put(cSolution.toString(), hmResult);
			}
		}

		StringBuffer sbResult = new StringBuffer();

		Set<String> ksSolution = result.keySet();
		Iterator<String> itSolution = ksSolution.iterator();
		while (itSolution.hasNext()) {
			String sSolution = itSolution.next();

			HashMap<String, String> hmResult = result.get(sSolution);
			Set<String> ksResult = hmResult.keySet();
			Iterator<String> itResult = ksResult.iterator();

			while (itResult.hasNext()) {
				String sKey = itResult.next();

				String sResult = hmResult.get(sKey);

				if (sbResult.length() > 0) {
					sbResult.append("\n");
				}

				sbResult.append(sSolution + " has an OBSOLETED " + sKey + " : " + sResult);
			}
		}

		if (sbResult.length() > 0) {
//			org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.OK);
//			box.setText("Check Part Master");
//			box.setMessage(sbResult.toString());

//			return box.open();
			throw (new Exception(sbResult.toString()));
		}

//		return -1;
	}

	/**
	 * SRME::[][20140926] Yun sung won. Cut & paste �� TC BOM ���� ����.
	 * 
	 * @return
	 * @throws Exception 
	 */
	private void cutNpasteCorrecting() throws Exception{

		ChangeToReplace op = new ChangeToReplace(ecoNo);
		try {
			op.execute();
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * OrderNo �ߺ� üũ ���� �߰�
	 * @Copyright : Plmsoft
	 * @author : ������
	 * @since  : 2017. 6. 13.
	 * @throws Exception
	 */
	private void duplicateOrderNoCheck() throws Exception{
		DataSet ds = new DataSet();
		ds.put("ecoNo", ecoNo);
		StringBuffer sbMessage = new StringBuffer();

		ArrayList<HashMap<String, Object>> childNodes = dao.duplicateOrderNoCheck(ds);
		if (childNodes.size() > 0) {
			for (int inx = 0; inx < childNodes.size(); inx++) {
				HashMap<String, Object> result = childNodes.get(inx);
				String parent_no = (String)result.get("PARENT_ID");
				String order_no = result.get("ORDER_NO").toString();
				sbMessage.append(parent_no + " : " + order_no);
				sbMessage.append("\n");
			}

			sbMessage.append("Order No. Duplicated\nPlease Contact Your Administrator.\n");
			
			throw (new Exception(sbMessage.toString()));
		}

//		if(CustomUtil.isNullString(sbMessage.toString())){
//			return -1;
//		}else{
//			org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.OK);
//			box.setText("Duplicate Order No.");
//			box.setMessage(sbMessage.toString());
//			return box.open();
//		}
	}
//	private int duplicateOrderNoCheck() throws Exception{
////		CustomECODao dao = new CustomECODao();
//		DataSet ds = new DataSet();
//		ds.put("ecoNo", changeRevision.getItem().getStringProperty("item_id"));
//		ArrayList<String> eco_epl = dao.getECOEPL(ds);
//		ds.clear();
//		StringBuffer sbMessage = new StringBuffer();
//
//		for(int i=0; i<eco_epl.size(); i++){
//			ds.clear();
//			ds.put("ITEM_ID", eco_epl.get(i));
//			ArrayList<HashMap<String, Object>> childNodes = dao.duplicateOrderNoCheck(ds);
//			if (childNodes.size() > 0) {
//				for (int inx = 0; inx < childNodes.size(); inx++) {
//					HashMap<String, Object> result = childNodes.get(inx);
//					String parent_no = (String)result.get("PARENT_ID");
//					String order_no = result.get("ORDER_NO").toString();
//					sbMessage.append(parent_no + " : " + order_no);
//					sbMessage.append("\n");
//				}
//
//				sbMessage.append("Order No. Duplicated\nPlease Contact Your Administrator.\nTel : 3176\n");
//			}
//		}
//
//		if(CustomUtil.isNullString(sbMessage.toString())){
//			return -1;
//		}else{
//			org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.OK);
//			box.setText("Duplicate Order No.");
//			box.setMessage(sbMessage.toString());
//			return box.open();
//		}
//	}	

	/**
	 * ECO�� �����ϰ� �ִ� ��� Part�� BOM���� Function ������ �����Ǿ��ִ��� Ȯ��
	 * @throws Exception
	 */
	private void checkParentConnection() throws Exception {
		try {
			DataSet ds = new DataSet();
			ds.put("ecoNo", ecoNo);
			
			ArrayList<HashMap<String, Object>> resultList = dao.notConnectedFunctionList(ds);
			if (resultList.size() > 0) {
				StringBuffer sbMessage = new StringBuffer();
				for (int inx = 0; inx < resultList.size(); inx++) {
					HashMap<String, Object> result = resultList.get(inx);
					String funcConnYn = result.get("FUNC_CONN_YN").toString();
					
					if(funcConnYn.equals("0")){
						String partNo = result.get("PART_NO").toString();
						String partRev = result.get("PART_REV").toString();
						sbMessage.append(partNo + "/" + partRev);
						sbMessage.append("\n");
					}
				}
				if(sbMessage.length() > 0){
					throw (new Exception(sbMessage.toString() + " is not connected with Function.\nPlease check the BOM Structure."));
				}
			}
		} catch (Exception e) {
			throw e;
		}
	}
//	private void checkParentConnection() throws Exception {
//		try {
//			// 1. ���� ECO�� ������ �ϴ� ��� Part�� �����´�. 
//			AIFComponentContext[] accReferenceds = changeRevision.whereReferenced();
//
//			// 2. ��� Part�� ���� ���Ǵ� Part�� ������ ã�ư� Function�� �پ��ִ��� Ȯ��
//			for (int inx= 0; inx < accReferenceds.length; inx++) {
//				AIFComponentContext accReferenced = accReferenceds[inx];
//				if (accReferenced.getComponent() instanceof TCComponentItemRevision) {
//					TCComponentItemRevision itemRevision = (TCComponentItemRevision) accReferenced.getComponent();
//					String[] sItemTypes = new String[] {SYMCClass.S7_VEHPARTREVISIONTYPE, SYMCClass.S7_STDPARTREVISIONTYPE, SYMCClass.S7_FNCMASTPARTREVISIONTYPE, SYMCClass.S7_FNCPARTREVISIONTYPE};
//					String sReferenceItemRevisionType = itemRevision.getProperty("object_type");
//					// 2.0. ���� Part�� ���� Part�� Functoin, FMP, Vehicle Part, Standard Part �̿� �ٸ� Part�� ��� �ǳʶ�.
//					// (Concurrent ECO�� �ɷ����� ����)
//					if (!(sReferenceItemRevisionType.equals(sItemTypes[0]) || 
//							sReferenceItemRevisionType.equals(sItemTypes[1]) ||
//							sReferenceItemRevisionType.equals(sItemTypes[2]) || 
//							sReferenceItemRevisionType.equals(sItemTypes[3]))
//							) {
//						continue;
//					}
//
//					// 2.1. ���� Part�� ���� Part �� Function, FMP�� ���� ��� ���� Part�� �Ѿ.
//					boolean isConnectWithFunction = false;
//
//					if (sReferenceItemRevisionType.equals(SYMCClass.S7_FNCPARTREVISIONTYPE) || sReferenceItemRevisionType.equals(SYMCClass.S7_FNCMASTPARTREVISIONTYPE)) {
//						isConnectWithFunction = true;
//					}
//
//					if (!isConnectWithFunction) {
//						isConnectWithFunction = findFunction(itemRevision);
//					}
//
//					// 3. ECO�� ������ �ϴ� Part �� �� �ϳ��� Part�� ��� ���� Part�� Function�� ���� ��� Exception �߻�.
//					if (!isConnectWithFunction) {
//						throw (new Exception(itemRevision.getProperty("item_id") + "/" + itemRevision.getProperty("item_revision_id") + " is not connected with Function.\nPlease check the BOM Structure."));
//					}
//				}
//			}
//		} catch (Exception e) {
//			throw e;
//		}
//	}

	/**
	 * ���� Function�� �ִ��� Recursive�� Ȯ��.
	 * @param itemRevision
	 * @return
	 */
	private boolean findFunction(TCComponentItemRevision itemRevision) {
		try {
			TCComponentRevisionRule revRule = SYMTcUtil.getRevisionRule(session, "Latest Working");
			TCComponent[] usedComponents = itemRevision.whereUsed(TCComponent.WHERE_USED_CONFIGURED, revRule);

			// ���� Part�� ���� Part �� 1Lv ���� Used �� Function Master Part�� ���� ��� ���� Part�� �Ѿ�� ���� Return
			if (usedComponents.length > 0) {
				for (int jnx = 0; jnx < usedComponents.length; jnx++) {
					if (usedComponents[jnx] instanceof TCComponentItemRevision) {
						TCComponentItemRevision usedComponent = (TCComponentItemRevision) usedComponents[jnx];
						if (usedComponent.getProperty("object_type").equals(SYMCClass.S7_FNCMASTPARTREVISIONTYPE)) {
							return true;
						} else {
							// ���� 1Lv ���� Part�� Function�� �ƴ� ��� �ٽ� Function ã���� �ö�.
							boolean isFindFunction = findFunction(usedComponent);
							if (isFindFunction) {
								return true;
							} else {
								continue;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			return false;
		}

		return false;
	}

	// Ÿ�ٸ���Ʈ���� ECORevision�� ������ ��� ã�Ƽ� �ٿ� �ְ�, CreateWorkflow�� ���� �޽�¡
	private void checkHasWorkflow() throws Exception {
		if(parent == null){
			// Workflow �˻�
			if(!dao.workflowCount(ecoNo).equals("0")){
				throw (new Exception("Workflow has been created already.\nCheck the task to perfrom folder in My Worklist, and please proceed by approval."));
			}
		}else{
			// FIXED, 2013.06.01, DJKIM, Ÿ�ٸ���Ʈ���� ECORevision�� ������ ��� ã�Ƽ� �ٿ� ��.
			if(changeRevision == null){
				String processName = parent.getCurrentJob().getName();
				String ecoNo = processName.substring(processName.indexOf("[")+1, processName.indexOf("]"));
				String ecoRevisionPuid = dao.getEcoRevisionPuid(ecoNo);
				changeRevision = (TCComponentChangeItemRevision) session.stringToComponent(ecoRevisionPuid);
				// 20130611, �����C, ��ũ�÷ο� ���� �� �� Ÿ���� ���� �������� ��쿡 eco revision�� ã�Ƽ� �ٿ��ִ� ���� 
				parent.getCurrentJob().getRootTask().add("root_target_attachments", changeRevision); 
			}
		}
	}

	private void createCompletePopUp() {
		final Shell shell = AIFUtility.getActiveDesktop().getShell();

		shell.getDisplay().syncExec(new Runnable()
		{

			public void run()
			{
				org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
				box.setText("Information");
				box.setMessage("���� ��û�� �Ϸ�Ǿ����ϴ�");
				box.open();
			}

		});
	}

	/**
	 * ���� �߻� �� ��� ���� �ʱ�ȭ
	 * @throws Exception
	 */
	private void rollback() {
		try{
			changeRevision.setProperty("s7_AFFECTED_PROJECT", dao.getAffectedProject(ecoNo));
			// SRME::[�ݿ�����][SR140702-027][20140702] bskwak, status ���� ó���� WF handler�� �̰�, (��û ������, WF handler�۾�:������)
			dao.updateEcoStatus(changeRevision.getUid(), "In Work", "In Work");

			problemList = changeRevision.getRelatedComponents(SYMCECConstant.PROBLEM_REL);
			if(problemList != null && problemList.length > 0)
				changeRevision.remove(SYMCECConstant.PROBLEM_REL, problemList);

			solutionList = changeRevision.getRelatedComponents(SYMCECConstant.SOLUTION_REL);
			for(TCComponent solutionItemComponent : solutionList){
				TCComponentItemRevision solutionItemrevision = (TCComponentItemRevision) solutionItemComponent;
				ArrayList<TCComponent> solutionDatasetList = ProcessUtil.getDatasets(solutionItemrevision, "IMAN_specification");
				for(TCComponent dataset : solutionDatasetList){
					if(ProcessUtil.isWorkingStatus(dataset) && !dataset.getType().equals("PDF")){
						if(!solutionItemrevision.getType().equals("S7_StdpartRevision")){
							dataset.setProperty("s7_ECO_NO", "");
						}
					}
				}
			}

			if(solutionList != null && solutionList.length > 0)
				changeRevision.remove(SYMCECConstant.SOLUTION_REL, solutionList);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * ECO Affected Project ���� ����
	 * @throws Exception
	 */
	private void setArrectedProject() throws Exception {
		String affecedProject = "";
		String affecedProjects = dao.getAffectedProject(ecoNo);
		if(affecedProjects != null){
			String[] affProjs = affecedProjects.split(",");
			ArrayList<String> affecedProjectList = new ArrayList<String>();
			for(String affProj : affProjs){
				if(!affecedProjectList.contains(affProj))
					affecedProjectList.add(affProj);
			}

			for(String affProj : affecedProjectList){
				if(affecedProject.equals("")){
					affecedProject = affProj;
				}else{
					affecedProject = affecedProject+","+affProj;
				}
			}
		}
		changeRevision.setProperty("s7_AFFECTED_PROJECT", affecedProject);
	}

	/**
	 * ���� ����
	 * IitemRevision�� Maturity��
	 * EcoRevision�� Eco Maturity�� ������Ʈ ��.
	 * SRME::[�ݿ�����][SR140702-027][20140702] bskwak, status ���� ó���� WF handler�� �̰�, (��û ������, WF handler�۾�:������)
	 * ==> java���� maturity ���� ���� ����. ���� method �ּ� ó�� ��. 
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private void changeStatus() throws Exception {
		boolean isok = dao.updateEcoStatus(changeRevision.getUid(), "In Review1", "Processing");
		if(!isok){
			throw (new Exception("changeStatus() Method Error"));
		}
	}

	/**
	 * ECO �۾� ����[C��]�� ���� ���������� ��ũ ����
	 * @throws Exception
	 */
	private void addProblemItems() throws Exception {
		// �ߺ� ���� ����
		problemList = changeRevision.getRelatedComponents(SYMCECConstant.PROBLEM_REL);
		if(problemList != null && problemList.length > 0)
			changeRevision.remove(SYMCECConstant.PROBLEM_REL, problemList);

		ArrayList<String> resultList = dao.getProblemItems(ecoNo);
		if(resultList!=null && resultList.size() > 0){
			int resultCount = resultList.size();
			String[] itemRevisions = new String[resultCount];
			for(int i = 0 ; i < resultCount ; i++){
				itemRevisions[i] = resultList.get(i);
				//System.out.println(resultList.get(i));
			}
			TCComponent[] tcComponents = session.stringToComponent(itemRevisions);
			if(tcComponents.length > 0)
				changeRevision.add(SYMCECConstant.PROBLEM_REL, tcComponents);
		}
	}

	/**
	 * ECO �۾� ����[C��]�� ���� �ַ�Ǿ����� ��ũ ����
	 * @throws Exception
	 */
	private void addSolutionItems() throws Exception {
		// �ߺ� ���� ����
		solutionList = changeRevision.getRelatedComponents(SYMCECConstant.SOLUTION_REL);
		if(solutionList != null && solutionList.length > 0)
			changeRevision.remove(SYMCECConstant.SOLUTION_REL, solutionList);
		/**
		 * Part �� �ٸ� ECO�� ����Ǿ��ִ��� üũ��
		 */
		checkRefEcoFromPart();

		ArrayList<String> resultList = dao.getSolutionItems(ecoNo);
		if(resultList!=null && resultList.size() > 0){
			int resultCount = resultList.size();
			String[] itemRevisions = new String[resultCount];
			for(int i = 0 ; i < resultCount ; i++){
				itemRevisions[i] = resultList.get(i);
			}
			TCComponent[] tcComponents = session.stringToComponent(itemRevisions);
			if(tcComponents.length > 0)
				changeRevision.add(SYMCECConstant.SOLUTION_REL, tcComponents);
		}
		
		//[csh 20180424] ���� ���� �� check out list �� dataset�� eco no ���Էµ� ����.
		solutionList = changeRevision.getRelatedComponents(SYMCECConstant.SOLUTION_REL);
		
	}

	/**
	 * ECO EPL ���� ���� Ȯ��
	 * ����� �� ��� ECO EPL Ȯ�� ���� �޽���
	 * @throws Exception
	 */
	private void checkChangeEPL() throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("ecoNo", ecoNo);
		Boolean result = (Boolean)remote.execute("com.kgm.service.ECOHistoryService", "isECOEPLChanged", ds);
		if(result.booleanValue()) {
			/**
			 * [SR����][2015.04.27][jclee] ECO Generate �� ��� EPL ���⳻���� ������ �� Regenerate
			 */
			//        	remote.execute("com.kgm.service.ECOHistoryService", "extractEPL", ds);
			remote.execute("com.kgm.service.ECOHistoryService", "generateECO", ds);

			progress.close();
			if(parent != null){
				try {
					parent.setVisible(false);
					parent.disposeDialog();
					session.getUser().getUserInBox().refresh();
				} catch (TCException e1) {
					e1.printStackTrace();
				}
			}
			final Shell shell = AIFUtility.getActiveDesktop().getShell();

			shell.getDisplay().syncExec(new Runnable()
			{

				public void run()
				{
					org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.ICON_WARNING | SWT.CLOSE);
					box.setText("Information");
					box.setMessage("ECO EPL is auto generated. \nPlease, Check the ECO EPL again.");
					box.open();
				}

			});
			throw (new Exception("Please check the ECO EPL."));
		}
	}

	/**
	 * ECO EPL VC üũ
	 * #1. Option �� ������ Category �� �ɼ��� �ΰ� �̻� ���õ� ��찡 �ִ��� Ȯ��.
	 * @throws Exception 
	 */
	private void getDuplicateCategoryInVC() throws Exception{
		final String sMessage = dao.getDuplicateCategoryInVC(ecoNo);

		if(sMessage != null && sMessage.length() > 0){
			progress.close();

			final Shell shell = AIFUtility.getActiveDesktop().getShell();

			shell.getDisplay().syncExec(new Runnable()
			{
				public void run()
				{
					SYMCAbstractDialog dialog = new SYMCAbstractDialog(shell) {
						@Override
						protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
							getShell().setText("ECO EPL VC Validation Result");

							Composite composite = new Composite(parentScrolledComposite, SWT.NONE);
							composite.setLayout(new GridLayout());

							Text txtMessage = new Text(composite, SWT.MULTI);
							txtMessage.setLayoutData(new GridData(GridData.FILL_BOTH));
							txtMessage.setEditable(false);
							txtMessage.setText(sMessage);

							return composite;
						}

						@Override
						protected boolean validationCheck() {
							return true;
						}

						@Override
						protected boolean apply() {
							return false;
						}

						@Override
						protected void okPressed() {
							this.close();
						}
					};

					dialog.setApplyButtonVisible(false);

					dialog.open();
				}

			});
			throw (new Exception("Please check the ECO EPL."));
		}
	}

	/**
	 * ECO EPL üũ
	 * #1. Function Master�� Vehicle Part �� ��� �ű� ��� ������ �� ECO ��ȣ�� ���� ECO ���� Ȯ��
	 * #2. Standard Part�� Vehicle Part �� ��� SUPPLY MODE�� ���� �Ǿ����� Ȯ��.
	 * #3. IC(ȣȯ��), ���(PLT/AS) Ȯ��
	 * ���� �߻� �� ���̺� ���·� ��Ÿ�� �ش�.
	 * @throws Exception 
	 */
	private void checkEPL() throws Exception{
		final ArrayList<HashMap<String, String>> resultList = dao.checkECOEPL(ecoNo);

		if(resultList != null && resultList.size() > 0){
			progress.close();

			final Shell shell = AIFUtility.getActiveDesktop().getShell();

			shell.getDisplay().syncExec(new Runnable()
			{

				public void run()
				{
					ValidationResultDialog dialog = new ValidationResultDialog(shell, SWT.NONE, ecoNo, resultList);
					dialog.open();

				}

			});
			throw (new Exception("Please check the ECO EPL."));
		}
	}
	
	/**
	 * Null Value üũ
	 * #1. BI �Ӽ� (System Code / NMCD)�� Ư�� ������Ʈ���� Null �̸� �ȵ�.
	 * ���� �߻� �� ���̺� ���·� ��Ÿ�� �ش�.
	 * @throws Exception 
	 */
	private void checkNullValue() throws Exception{
		String[] checkedProject = PreferenceService.getValues(TCPreferenceService.TC_preference_site, "SYMC_ECO_VAL_PRJ");
		if( checkedProject != null ){
			DataSet ds = new DataSet();
			ArrayList<String> projectCodeList = new ArrayList<String>();
			for( int i =0; i < checkedProject.length ; i++){
				projectCodeList.add(checkedProject[i]);
			}
			ds.put("PRJ_CD", projectCodeList);
			ds.put("ECO_NO", ecoNo);
//			ds.put("ECO_NO", "21BH006");
			
			ArrayList<HashMap<String, String>> resultList = dao.getEcoNullValueList(ds);
			
			if(resultList != null && resultList.size() > 0){
				String parent_no = "";
				String child_no = "";
				String seq = "";
				progress.setStatus("");
				for (HashMap<String, String> map : resultList) {
					parent_no = map.get("PARENT_NO");
					child_no = map.get("NEW_PART_NO");
					seq = map.get("NEW_SEQ");
		            progress.setStatus("    PARENT_NO : " + parent_no + ", NEW_PART_NO : " + child_no + ", NEW_SEQ : " + seq);
				}
				throw (new Exception("Please check the BI Null Value (System Code)."));
			}
		}
	}

	/**
	 * BOM Structure �󿡼� end item �ؿ� end item�� �����ϸ� �ʵȴ�
	 * @throws TCException
	 * @throws Exception
	 */
	private void checkEndToEnd() throws TCException, Exception {
		ArrayList<String> resultList = dao.checkEndtoEnd(ecoNo);
		if(resultList !=null && resultList.size() > 0){
			String list = "";
			for(String result : resultList){
				list += result + "\n";
			}
			throw (new Exception("Cannot make end item to end item structure.\nCheck belows and fix it.\n"+list));
		}
	}

	/**
	 * ���缱�� ���ø��� �°� ���� �Ǿ� �ִ��� Ȯ��
	 * @return
	 * @throws Exception
	 */
	private void checkReviewer() throws Exception {
		ApprovalLineData theLine = new ApprovalLineData();
		theLine.setEco_no(changeRevision.getProperty("item_id"));

		// ���� �ÿ����� ���缱 üũ ����
		ArrayList<ApprovalLineData> paramList = null;
		if(parent == null){
			//���缱 ���� ����
			paramList = dao.getApprovalLine(theLine);
		}else{
			TCComponentTask rootTask = changeRevision.getCurrentJob().getRootTask();
			TCComponentTask[] subTasks = rootTask.getSubtasks();
			paramList = new ArrayList<ApprovalLineData>();

			for(TCComponentTask subTask : subTasks) {
				if(subTask.getTaskType().equals(SYMCECConstant.EPM_REVIEW_TASK_TYPE) 
						|| subTask.getTaskType().equals(SYMCECConstant.EPM_ACKNOWLEDGE_TASK_TYPE)){
					if(!subTask.getName().equals("Creator")){
						TCComponentTask performSignoffTask = subTask.getSubtask("perform-signoffs");
						TCComponentSignoff[] signoffs = performSignoffTask.getValidSignoffs();
						if(signoffs.length > 0) {

							for(TCComponentSignoff signoff : signoffs) {
								signoff.refresh();
								TCComponentGroupMember groupMember = signoff.getGroupMember();
								String[] groupMemberProperties = groupMember.getProperties(new String[]{"the_group","the_user"});
								ApprovalLineData approvalLine = new ApprovalLineData();
								approvalLine.setTask(subTask.getName());
								approvalLine.setTeam_name(groupMemberProperties[0]);
								approvalLine.setUser_name(groupMemberProperties[1]);
								approvalLine.setTc_member_puid(groupMember.getUid());
								paramList.add(approvalLine);
							}
						}
					}
				}
			}
		}

		if(paramList == null || paramList.size() < 1) {
			throw (new Exception("Cannot find approval line.\nPlease check the approval line."));
		}

		//Ÿ��ũ�� TCComponentGroupMember���� ���� �� �ʼ� ���� ���缱 Ȯ��
		ArrayList<String> requiredAssingTask = new ArrayList<String>();
		String[] taskList = SYMCECConstant.ECO_TASK_LIST;
		for(String task : taskList){
			if(task.equals("Sub-team Leader") || task.equals("Reference Department")) continue;
			requiredAssingTask.add(task);
		}

		String mapTask = "";
		TCComponentGroupMember groupMember = null;
		ArrayList<TCComponentGroupMember> groupMemberList = null;

		for(ApprovalLineData map : paramList){
			mapTask = map.getTask();

			// FIXED 2013.05.14, DJKIM, �ڼ��� CJ: ������� ���� ������ �߻� �Ҽ� �����Ƿ� ����� ���� Ȯ���Ͽ� �������� ����ڰ� ���缱�� �Ҵ� ���� �ʵ��� ��.
			try{
				groupMember =  (TCComponentGroupMember) session.stringToComponent(map.getTc_member_puid());
			}catch(Exception e){
				throw (new Exception("Cannot find group member.\n"+map.getUser_name() + " in " + map.getTeam_name() + " is removed. \nPlease check and replace him."));
			}
			if(groupMember.getMemberInactive()){
				throw (new Exception("Group member is in inactive status.\n"+map.getUser_name() + " in " + map.getTeam_name() + " is in inactive status. \nPlease check and replace him."));
			}
			if(!groupMember.getUser().isValid()){
				throw (new Exception("User is in inactive status.\n"+map.getUser_name() + " in " + map.getTeam_name() + " is in inactive status. \nPlease check and replace him."));
			}
			if(reviewers.containsKey(mapTask)){
				groupMemberList = reviewers.get(mapTask);
				groupMemberList.add(groupMember);
			}else{
				groupMemberList = new ArrayList<TCComponentGroupMember>();
				groupMemberList.add(groupMember);
			}
			reviewers.put(mapTask, groupMemberList);
			if(requiredAssingTask.contains(mapTask)) 
				requiredAssingTask.remove(mapTask);
		}

		if(requiredAssingTask.size() > 0){
			String addTasks = "";
			for(String requiredAssingTaskName : requiredAssingTask){
				addTasks = addTasks+requiredAssingTaskName+"\n";
			}
			throw (new Exception("Workflow task checking information.\nPlease, Add the following tasks.\n"+addTasks));
		}

		// FIXED ���缱�� ����[COST_ENGINEER], �������[BOMADMIN], �����[TEAM_LEADER]�� �ϳ� �̻����� üũ ��.
		PreferenceService.createService(session);
		String checkRole = null;
		// [20161117][ymjang] CM���� ���۵Ǵ� ECO �� S201 �ε� ������� ECO �μ� ������ ������.  
		if (theLine.getEco_no().startsWith("CM")) {
			checkRole = PreferenceService.getValue("SYMC_ECO_WF_CHECK_ROLE_S201"); // BOMADMIN,TEAM_LEADER
		} else {
			checkRole = PreferenceService.getValue("SYMC_ECO_WF_CHECK_ROLE"); // COST_ENGINEER,BOMADMIN,TEAM_LEADER
		}
		if(checkRole.equals("")){
			checkRole = "COST_ENGINEER,BOMADMIN,TEAM_LEADER";
		}
		String[] checkRoles = checkRole.split(",");
		ArrayList<String> checkRoleList = new ArrayList<String>();
		for(String role : checkRoles){
			if(!role.equals("")){
				checkRoleList.add(role.trim());
			}
		}
		Object[] tasks = reviewers.keySet().toArray();
		for(Object task : tasks){
			groupMemberList = reviewers.get(task+"");
			String roleName = "";
			for(TCComponentGroupMember member : groupMemberList){
				roleName = member.getRole().getProperty("role_name");
				if(checkRoleList.contains(roleName)){
					checkRoleList.remove(roleName);
				}
			}
		}

		if(checkRoleList.size() > 0){
			String addRoles = "";
			for(String addRole : checkRoleList){
				addRoles = addRoles+addRole+"\n";
			}
			throw (new Exception("Workflow role checking information.\nPlease, Add someone with the following roles.\n"+addRoles));
		}
	}

	/**
	 * [20180206][LJG] ������ �ʼ����� -> �������� �߰� �Ǹ� �ȵǴ°ŷ� ����
	 * �ű� ��Ʈ�� EPL�� Category�� DR1/2�� ������ ��Ʈ�� ���� �ϸ� �������� �ʼ��� �����Ǿ�� ��.
	 * 2013.01.10
	 * REQ. �۴뿵
	 * REF. ������
	 * @return
	 * @throws Exception
	 */
	//    private void checkCertification() throws Exception {
	//        boolean hasCertificationPart = false;
	//        //CM�� ��� ������ üũ Pass
	//        String ecoId = changeRevision.getProperty(IPropertyName.ITEM_ID);
	//        if(ecoId.startsWith("CM"))
	//        	return;
	//        
	//        solutionList = changeRevision.getRelatedComponents(SYMCECConstant.SOLUTION_REL);
	//        problemList = changeRevision.getRelatedComponents(SYMCECConstant.PROBLEM_REL);
	//        
	//        ArrayList<String> checkCategory = new  ArrayList<String>();
	//        checkCategory.add("DR1");
	//        checkCategory.add("DR2");
	//        
	//    	for(TCComponent solutionItemComponent : solutionList){
	//    		TCComponentItemRevision solutionItemrevision = (TCComponentItemRevision) solutionItemComponent;
	//    		if(checkCategory.contains(solutionItemrevision.getProperty("s7_REGULATION"))){
	//    			hasCertificationPart = true;
	//    			break;
	//    		}
	//    	}
	//        
	//    	boolean hasCerfiticationTeam = false;
	//        if(hasCertificationPart){
	//        	PreferenceService.createService(session);
	//        	String theTask = "Reference Department";
	//        	String certificationTeam = PreferenceService.getValue("SYMC_ECO_Certification_Team");
	//        	ArrayList<TCComponentGroupMember> memberList = reviewers.get(theTask);
	//        	
	//        	if(memberList == null){
	//        		throw (new Exception("Certification part is exist.\nCertification team must be added in workflow[Reference Department]."));
	//        	}
	//        	
	//        	for(TCComponentGroupMember member : memberList){
	//        		if(member.getGroup().getGroupName().equals(certificationTeam)){
	//        			hasCerfiticationTeam = true;
	//        			break;
	//        		}
	//        	}
	//        	
	//        	if(!hasCerfiticationTeam){
	//        		throw (new Exception("Certification part is exist.\nCertification team must be added in workflow[Reference Department]."));
	//        	}
	//        }
	//    }

	/**
	 * [20180206][LJG] ������ �ʼ����� -> �������� �߰� �Ǹ� �ȵǴ°ŷ� ����
	 * @Copyright : Plmsoft
	 * @author : ������
	 * @since  : 2018. 2. 6.
	 * @throws Exception
	 */
	private void checkCertification() throws Exception {
		boolean hasCertificationPart = false;
		PreferenceService.createService(session);
		String theTask = "Reference Department";
		String certificationTeam = PreferenceService.getValue("SYMC_ECO_Certification_Team");
		String certificationTeam1 = "VEHICLE CERTIFICATION";
		ArrayList<TCComponentGroupMember> memberList = reviewers.get(theTask);

		if(memberList != null && memberList.size() > 0){
			for(TCComponentGroupMember member : memberList){
				if(member.getGroup().getGroupName().equalsIgnoreCase(certificationTeam) || member.getGroup().getGroupName().equalsIgnoreCase(certificationTeam1)){
					hasCertificationPart = true;
					break;
				}
			}
		}

		if(hasCertificationPart){
			throw (new Exception("Certification part is exist.\n"+certificationTeam+" team must be removed in workflow[Reference Department]."));
		}
	}
	
	/**
	 * [SR180207-027][LJG] ������ȹ���� �߰��Ǹ� �ȵǰ�, ����������� �߰� �Ǿ����
	 * @Copyright : Plmsoft
	 * @author : ������
	 * @since  : 2018. 2. 7.
	 * @throws Exception
	 */
	private void checkCostTeam() throws Exception {
		boolean hasCostTeam = false;
		PreferenceService.createService(session);
		String theTask = "Related Team Review";
		String costTeam = PreferenceService.getValue("SYMC_ECO_Cost_Team");
		ArrayList<TCComponentGroupMember> memberList = reviewers.get(theTask);

		if(memberList != null && memberList.size() > 0){
			for(TCComponentGroupMember member : memberList){
				if(member.getGroup().getGroupName().equalsIgnoreCase(costTeam)){
					hasCostTeam = true;
					break;
				}
			}
		}

		if(!hasCostTeam){
			throw (new Exception("Cost part is wrong.\n"+costTeam+" team must be added in workflow[Related Team Review]."));
		}
	}

	/**
	 * ���� Ÿ�� Ȯ��
	 * check out ���ε� ���� Ȯ��
	 * dataset �Ӽ��� eco no�� �Է�
	 * @throws TCException
	 */
	private void getTargets() throws TCException, Exception{

		// FIXED, 2013.05.20, DJKIM, �ݷ� �� ��� �Ϻ� ������ �� ���� �߻�. �ݷ��� ���۾� �� Ÿ���� �̸� ����[������ ���� �۾� ������ Ȯ�� �ϴ� �κп� üũ ���� �ʵ���]
		TCComponentProcess process = null;
		TCComponentTask rootTask = null;

		if(parent != null){
			process = changeRevision.getCurrentJob();
			rootTask = process.getRootTask();

			// �ߺ� ���� ����
			TCComponent[] oldTargetList = rootTask.getRelatedComponents("root_target_attachments");
			if(oldTargetList != null && oldTargetList.length > 0){
				rootTask.remove("root_target_attachments", oldTargetList);
			}
		}

		String checkOutlist = "";
		// FIXED, 2013.06.01, DJKIM ���� ������ �ִ��� ������ üũ
		String noChildrenBVRList = "";
		targetList.add((TCComponent)changeRevision);
		ArrayList<TCComponent> datasetList = ProcessUtil.getDatasets(changeRevision, "IMAN_specification");
		for(TCComponent dataset : datasetList){
			if(dataset.isCheckedOut()){
				checkOutlist = checkOutlist + dataset + "\n";
			}else{
				//FIXED, 2013.06.12, YUNJAE, only adding Working dataset
				if(ProcessUtil.isWorkingStatus(dataset) && !dataset.getType().equals("PDF")) {
					//    				[SR140702-056][20140422] modified yunjae
					//    				targetList.add(dataset);
				}
			}
		}
		

		for(TCComponent solutionItemComponent : solutionList){
			TCComponentItemRevision solutionItemrevision = (TCComponentItemRevision) solutionItemComponent;
			if(solutionItemrevision.isCheckedOut()){
				checkOutlist = checkOutlist + solutionItemrevision + "\n";
			}else{
				//    			[SR140702-056][20140422] modified yunjae
				//    			targetList.add(solutionItemrevision);
			}

			TCComponentBOMViewRevision view = (TCComponentBOMViewRevision) solutionItemrevision.getRelatedComponent("structure_revisions");
			if(view != null){
				if(view.isCheckedOut()){
					checkOutlist = checkOutlist + view + "\n";
				}else{
					// FIXED, 2013.06.01, DJKIM ���� ������ �ִ��� ������ üũ
					// [SR����][20150727][jclee]FMP�� ��� �ӽ� ����� ��� �����ϴ� ��찡 �����Ƿ� ���������� ������ �������� ���
					if(dao.childrenCount(view.getUid()).equals("0") && !solutionItemrevision.getType().equals("S7_FunctionMastRevision")){
						noChildrenBVRList = noChildrenBVRList + solutionItemrevision + "\n";
					}else{
						//        				[SR140702-056][20140422] modified yunjae
						//        				targetList.add(view);
					}
				}
			}

			ArrayList<TCComponent> solutionDatasetList = ProcessUtil.getDatasets(solutionItemrevision, "IMAN_specification");
			for(TCComponent dataset : solutionDatasetList){
				if(ProcessUtil.isWorkingStatus(dataset) && !dataset.getType().equals("PDF")){
					if(dataset.isCheckedOut()){
						checkOutlist = checkOutlist + dataset + "\n";
					}else{
						if(!solutionItemrevision.getType().equals("S7_StdpartRevision")){
							dataset.setProperty("s7_ECO_NO", ecoNo);
						}
						//            			[SR140702-056][20140422] modified yunjae
						//            			targetList.add(dataset);
					}
				}
			}
		}

		String retrunMessage = "";
		if(!checkOutlist.equals("")){
			retrunMessage = "Check-out Componet is exist.\nCheck belows and fix it.\n"+checkOutlist;
		}

		// FIXED, 2013.06.01, DJKIM ���� ������ �ִ��� ������ üũ
		if(!noChildrenBVRList.equals("")){
			retrunMessage = retrunMessage + "\nThe part that does not have a sub-structure exists.\nCheck belows and BOMViewResion and CATProduct Dataset.\n"+noChildrenBVRList;
		}

		// �ݷ� �� ���  Ÿ�� �缳��
		if(parent != null && rootTask != null){   		
			// Ÿ�� �缳��
			rootTask.add("root_target_attachments", targetList);
		}
		
		// [CSH]��ġ���� >> Ÿ�� �缳�� ���� Exception �߻��� ���� �Ͼ�� ECO�� target���� ����������.   
		if(!retrunMessage.equals("")){
			throw (new TCException(retrunMessage));
		}
	}

	//[SR170828-015][LJG]Chassis module ������ ���� ���� ���� �߰� ��û
	// 1. ECO ���� Part S/mode : P7YP8, C0YP8, P7CP8, P7UP8, PDYP8, P7
	// 2. Module code : FCM or RCM
	// 3. Part�� Option : Z999�� �����ϴ� ���
	private int checkChassisModule() throws Exception{
		String eco_no = changeRevision.getProperty("item_id");
		ArrayList<String> resultList = dao.checkChassisModule(eco_no);
		StringBuffer sbMessage = new StringBuffer();
		if(resultList !=null && resultList.size() > 0){
			for(String result : resultList){
				sbMessage.append(" �� " + result + "\n");
			}
		}

		if(CustomUtil.isNullString(sbMessage.toString())){
			return -1;
		}else{
			org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.OK);
			box.setText("Information");
			box.setMessage("Check Chassis Module Error.\nCheck belows and fix it.\n" + sbMessage.toString());
			return box.open();
		}
	}
	
	// Structure Manager���� �������� BOMWindow �ִ��� üũ
	private void checkBOMEdit() throws Exception{
		List<AbstractAIFUIApplication>  applications = AIFDesktop.getActiveDesktop().getApplications();
		
		if(applications.size() > 0){
			StringBuffer sb = new StringBuffer();
			for(AbstractAIFUIApplication application : applications){
				if(application.getPerspectiveDef().getLabel().equals("Structure Manager")){
					PSEApplicationService pseApp = (PSEApplicationService) application;
					if(pseApp.getBOMWindow() != null && pseApp.getBOMWindow().isModified()){
//						org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.OK);
//						box.setText("BOM Edit Check");
//						box.setMessage("�������� BOMWindow�� �����մϴ�.\nStructure Manager���� �������� ������ ���� �� �����ϼ���.\nTopLine : " + pseApp.getBOMWindow().getTopBOMLine().toString());
//						return box.open();
						sb.append("�������� BOMWindow�� �����մϴ�.\nStructure Manager���� �������� ������ ���� �� �����ϼ���.\nTopLine : " + pseApp.getBOMWindow().getTopBOMLine().toString());
						break;
					}
					if(application.isDirty()){
//						org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.OK);
//						box.setText("BOM Edit Check");
//						box.setMessage("�������� BOMWindow�� �����մϴ�.\nStructure Manager���� �������� ������ ���� �� �����ϼ���.");
//						return box.open();
						sb.append("�������� BOMWindow�� �����մϴ�.\nStructure Manager���� �������� ������ ���� �� �����ϼ���.");
						break;
					}
				}
			}
			
			if(sb.length() >0)
				throw (new Exception(sb.toString()));
			}
		
	}

	/**
	 * ���μ��� ����
	 * @throws Exception
	 */
	private void createProcess() throws Exception{

		TCComponentTaskTemplateType compTaskTmpType = (TCComponentTaskTemplateType)session.getTypeComponent("EPMTaskTemplate");
		TCComponentTaskTemplate template = compTaskTmpType.find(SYMCECConstant.ECO_PROCESS_TEMPLATE, 0);

		TCComponentProcessType processtype = (TCComponentProcessType)session.getTypeComponent("Job");

		TCComponent[] componentList = new TCComponent[targetList.size()];
		for(int i = 0 ; i < targetList.size() ; i++){
			componentList[i] = targetList.get(i);
		}

		newProcess = (TCComponentProcess)processtype.create("ECO [" + changeRevision.getProperty("item_id") + "] approval request.", "Please confirm ASAP.", template, componentList, ProcessUtil.getAttachTargetInt(componentList));
	}

	/**
	 * ���缱 �Ҵ�
	 * @throws TCException
	 */
	private void assignSignoffs() throws Exception {

		TCComponentTask rootTask = newProcess.getRootTask();
		TCComponentTask[] subTasks = rootTask.getSubtasks();
		TCComponentSignoffType signoffType = (TCComponentSignoffType) session.getTypeComponent("Signoff");
		for(TCComponentTask subTask : subTasks) {
			String reviewTaskName = subTask.getName();
			TCComponentTask selectSignoffTeam = subTask.getSubtask("select-signoff-team");

			ArrayList<TCComponentGroupMember> taskReviewers = new ArrayList<TCComponentGroupMember>();

			try{
				if(selectSignoffTeam == null) continue;

				selectSignoffTeam.lock();
				if(reviewers.containsKey(reviewTaskName)){
					taskReviewers = reviewers.get(reviewTaskName);
				}else{
					if(reviewTaskName.equals("Creator")){
						taskReviewers.add(session.getUser().getGroupMembers()[0]);
					}
				}

				TCComponentSignoff[] sifnoffList = new TCComponentSignoff[taskReviewers.size()];
				int[] attachTypeList = new int[taskReviewers.size()];
				int i = 0;
				for(TCComponentGroupMember reviewMember :taskReviewers){
					sifnoffList[i] = signoffType.create(reviewMember, TCSignoffOriginType.ADHOC_USER, null);
					attachTypeList[i] = TCAttachmentType.SIGNOFF;
					i++;
				}

				if(taskReviewers.size() > 0){
					selectSignoffTeam.addAttachments(TCAttachmentScope.LOCAL, sifnoffList, attachTypeList);
					//        			selectSignoffTeam.getTCProperty("done").setLogicalValue(true);
					selectSignoffTeam.getTCProperty("task_result").setStringValue("Completed");
					selectSignoffTeam.save();
					if(selectSignoffTeam.getParent().getState().equals(TCTaskState.STARTED)) {
						selectSignoffTeam.performAction(TCComponentTask.COMPLETE_ACTION, "");
					}
				}else{
					//        			selectSignoffTeam.getTCProperty("done").setLogicalValue(true);
					selectSignoffTeam.getTCProperty("task_result").setStringValue("Completed");
					selectSignoffTeam.save();
				}

			} catch(Exception e){
				throw e;
			} finally {
				if(selectSignoffTeam != null)
					selectSignoffTeam.unlock();
			}
		}
	}
	
	/**
	 * End Item���� 500�� �ʰ��� HBOM(�̱��� ����)�� Mail�뺸
	 * @throws Exception
	 */
	private void senMailEndItemCount(){
		try{
			//end item count
			DataSet ds = new DataSet();
			ds.put("ecoNo", ecoNo);
			String count = dao.getEcoEndItemCount(ds);
			
			//500�� �ʰ��� Mail �߼�
			if(count != null && !count.equals("") && Integer.parseInt(count) > 500){
				String title = "New PLM : ECO[" + ecoNo + "]�� End Item ���� " +count+ "�� �Դϴ�.";
				String body = "<PRE>";
				body += "�����ϼ���." + "<BR>";
				body += "</PRE>";
				String fromUser = "";
				String toUsers = "208748,128698";
				dao.sendMail(fromUser, title, body, toUsers);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		
	}

	/**
	 * ���� �߼�
	 * Vision-Net�� CALS ���ν��� ȣ��
	 * @throws Exception
	 */
	private void sendMail() throws Exception{
		//String products = changeRevision.getProperty("plant_code");
		String products = changeRevision.getProperty("s7_PLANT_CODE");
		String changeDesc = changeRevision.getProperty("object_desc");

		String fromUser = session.getUser().getUserId();
		String title = "New PLM : ECO[" + ecoNo + "] ���� ��û";

		String body = "<PRE>";
		body += "New PLM���� �Ʒ��� ���� ���� ��û �Ǿ����� Ȯ�� �� ���� �ٶ��ϴ�." + "<BR>";
		body += " -ECO NO. : " + ecoNo + "<BR>";
		body += " -Product : " + products + "<BR>";
		body += " -Change Desc. : " + changeDesc + "<BR>";
		body += " -��û�μ� : " + changeRevision.getTCProperty("owning_group") + "<BR>";
		body += " -��û��  : " + changeRevision.getTCProperty("owning_user") + "<BR>";
		body += "</PRE>";

		String toUsers = "";
		String[] ecoTasks = SYMCECConstant.ECO_TASK_LIST;
		ArrayList<TCComponentGroupMember> receivedUserList = reviewers.get(ecoTasks[0]); // ���� �μ�

		for(TCComponentGroupMember member : receivedUserList){
			//20190228 ��������� ������ �ްڽ��ϴ�. �۴뿵 å��
//			if(!member.getGroup().getGroupName().startsWith("ENGINEERING MANAGEMENT")){ // FIXED 2013.05.20, BY DJKIM ��������� ���� �߼ۿ��� ����
				if(toUsers.equals("")){
					toUsers = member.getUser().getUserId();
				}else{
					toUsers += SYMCECConstant.SEPERATOR + member.getUser().getUserId();
				}
//			}
		}
		/**
		 * ���� �μ��� ������ ���� ������ ����
		 */
		if(toUsers.isEmpty())
			return;

		dao.sendMail(fromUser, title, body, toUsers);
	}

	/**
	 * Part �� �ٸ� ECO�� ����Ǿ��ִ��� üũ��
	 */
	private void checkRefEcoFromPart() throws Exception
	{
//		CustomECODao dao = new CustomECODao();
		ArrayList<HashMap<String, String>> refEcoList = dao.getRefEcoFromPartList(ecoNo);
		StringBuffer sb = new StringBuffer();
		for(HashMap<String, String> refEcoHash: refEcoList)
		{
			String partNo = refEcoHash.get("PART_NO");
			String revision = refEcoHash.get("REV");
			String refEcoNo = refEcoHash.get("REF_ECO");

			sb.append(partNo +"/"+revision+ " is referenced by other ECO ["+refEcoNo+"] \n ");
		}
		if(sb.length() >0)
			throw (new Exception(sb.toString()));
	}

	/**
	 * ���� Task ��  �ߺ��� ���缱�� �����ϴ��� üũ��
	 * @throws Exception
	 */
	private void checkDupApprovalLines() throws Exception
	{
//		CustomECODao dao = new CustomECODao();
		ArrayList<HashMap<String, Object>> dupApprovalLines = dao.getEcoDupApprovalLines(ecoNo);
		StringBuffer sbTask = new StringBuffer();
		for(HashMap<String, Object> depApprovalLine: dupApprovalLines)
		{
			String taskName = (String)depApprovalLine.get("TASK");
			String userName = (String)depApprovalLine.get("USER_NAME");
			BigDecimal count = (BigDecimal)depApprovalLine.get("CNT");
			sbTask.append("�� "+ taskName +" / "+ userName+" (x"+String.valueOf(count)+") \n ");
		}

		if(sbTask.length() >0)
		{
			StringBuffer sbMain = new StringBuffer();
			sbMain.append("The approval line was duplicated\n ");
			sbMain.append(sbTask);
			throw (new Exception(sbMain.toString()));
		}

	}

	/**
	 * Old Part Revision �� �ùٸ��� üũ��
	 * @throws Exception
	 */
	private void checkOldRevIsMatched() throws Exception
	{
//		CustomECODao dao = new CustomECODao();
		ArrayList<HashMap<String, String>> notMatchedPartList = dao.getOldRevNotMatchedParts(ecoNo);
		StringBuffer sbNotMachedParts = new StringBuffer();
		for(HashMap<String, String> notMatchedPart: notMatchedPartList)
		{
			String partNo = notMatchedPart.get("OLD_PART_NO");
			String oldPartRev = notMatchedPart.get("OLD_PART_REV");
			String realPreRev = notMatchedPart.get("REAL_PRE_REV");
			String newPartRev = notMatchedPart.get("NEW_PART_REV");
			sbNotMachedParts.append(" New: "+ partNo +"/"+newPartRev + ", Old: "+ partNo +"/"+oldPartRev+"(�� "+realPreRev+") \n ");
		}

		if(sbNotMachedParts.length() >0)
		{
			StringBuffer sbMain = new StringBuffer();
			sbMain.append("Previous revision of part is invalid. Contact to administrator. \n ");
			sbMain.append(sbNotMachedParts);
			throw (new Exception(sbMain.toString()));
		}

	}

	/**	[CF-1635] [20201208 by SYChon] ������Ʈ ������ ���� ���� ���� �߰� ��û - �����ɼ��ڵ� ���Է��� ��쿡�� ECO ����� �Ǵ� ���� �ذ�
	// 1. Engine Product���� SMode�� P7, PD�� ��, A00?/C00?�� �ִ��� üũ
	// 2. Module code : 
	// 3. Part�� Option : Engine Product���� S/Mode�� P7, PD�� ��� ����+�����ɼ�(A00? & C00?) �ʼ� �Է� 
	 * @throws Exception
	 */
	private void checkSModeP7PD() throws Exception{

		//�ش� ECO�� Product�� �ҷ���(ECO A�� Product ��) 
		String products = changeRevision.getProperty("s7_PLANT_CODE");

		if (products == null || products.isEmpty())
		{
			return;
		}
		
		ArrayList<SYMCBOMEditData> resultList = dao.selectECOEplList(ecoNo);	

		if (resultList == null || resultList.isEmpty())
		{
			return;
		}

		if (products.contains("E")) {
			for (int i = 0; i < resultList.size(); i++)
			{
				String supplyModeNew = resultList.get(i).getSupplyModeNew(); 	// ���� ���� �ű� ��Ʈ�� Supply Mode
				String vcNew = resultList.get(i).getVcNew();					// ���� ���� �ű� ��Ʈ�� Variant Condition(Option)
				String partNew = resultList.get(i).getPartNoNew();				// ���� ���� �ű� Part No
				
				// Product�� �����̰�, Supply Mode�� P7,PD �̸�
				if (supplyModeNew != null &&(supplyModeNew.equals("P7") || supplyModeNew.equals("PD")))				
				{
					// VA��(�ɼǰ�)�� A00,C00�� �������� ���� ���
					if (vcNew == null || (!vcNew.contains("A00") || !vcNew.contains("C00")))
					{
						throw (new Exception("PartNo : " + partNew + "\n" + " Options : " + vcNew + "\n" + " ������ Product�� ��� ����+�����ɼ�(A00? & C00?) �ʼ��Է�! ���Է½� �̻��"));						
					}
				}

			} 		
			
		}
		
	}
	
	
	/**
	 * [20230406][CF-3876] ��������� �̺��� å��, ������ å�� ��û
	 * #1. Vehicle ECO�� �Ŀ�Ʈ���� ������Ʈ ���� ���� üũ 
	 * #2. Vehicle ECO(���� ECO)�� ECO C�� Proj�Ӽ��� �Ŀ�Ʈ���� ������Ʈ ����� ���� ó��(�Ŀ�Ʈ���� ������Ʈ ����� Power Traing ECO(���� ECO)�� �۾� �ؾ��Ѵ�.) 
	 */
	private void checkPowerTrain() throws Exception{
		DataSet ds = new DataSet();
		ds.put("ecoNo", ecoNo);
		ArrayList<HashMap<String, String>> resultList = dao.checkPowerTraing(ds);
		if(resultList.get(0).get("CHECK_ENGINE_PART").equals("error")){
			throw (new Exception("PT ECO�� �ۼ� ���."));
		}
	}
	
	private void checkChangeEffPoint() throws Exception{
		String changeEffPoint = changeRevision.getProperty("s7_EFFECT_POINT_DATE");
		if(changeEffPoint.getBytes().length > 35) {
			throw (new Exception("���� ����(Change Eff. Point) 35����Ʈ �̳� �Է� ���"));
		}
	}
}
