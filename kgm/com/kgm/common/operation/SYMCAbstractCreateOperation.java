package com.kgm.common.operation;

import com.kgm.commands.partmaster.MProductOperation;
import com.kgm.common.SimpleProgressBar;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.PreferenceService;
import com.kgm.common.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.dialog.AbstractSWTDialog;



/** 
 * 
 * [SR140702-057][20140617] shcho M-Product �ڵ� ���� ��� �߰� : E-BOM�� New Product�� �����Ǹ� �ڵ�����  M-Product ���� (F605/M605 ����Function����)
 * [20161221] S201 �� ��� M Product �� �������� �ʵ��� ��
 */

public abstract class SYMCAbstractCreateOperation extends AbstractAIFOperation {
	
	/** ���Ӱ� ���� �Ǵ� Item */
	protected TCComponentItem newComp = null;

	protected TCSession session;

	/** operation ���൵�� ���� �߻��� rollback �����ֱ����� MarkPoint */
	private Markpoint mp;

	protected AbstractSWTDialog dialog;

	@SuppressWarnings("unused")
    private String message;

	public SYMCAbstractCreateOperation(AbstractSWTDialog dialog) {
		this(dialog, CustomUtil.getTCSession(), null);
	}

	public SYMCAbstractCreateOperation(AbstractSWTDialog dialog, String startMessage) {
		this(dialog, CustomUtil.getTCSession(), startMessage);
	}

	public SYMCAbstractCreateOperation(AbstractSWTDialog dialog, TCSession session, String startMessage) {
		super(startMessage);
		this.message = startMessage;
		this.session = session;
		this.dialog = dialog;
	}
	@SuppressWarnings("unused")
    @Override
	public void executeOperation() throws Exception {
		SimpleProgressBar bar = null;
		try {
//			bar = new SimpleProgressBar(dialog, message);
			startOperation();

			mp = new Markpoint(session);

			/** ���ο� ������ ���� */
			createItem();

			if (newComp != null) {

				/** �Ӽ��� �Է� */
				setProperties();

				String projectCode = newComp.getLatestItemRevision().getProperty("s7_PROJECT_CODE");
				
				// [SR140702-057][20140617] shcho M-Product �ڵ� ���� ��� �߰� : E-BOM�� New Product�� �����Ǹ� �ڵ�����  M-Product ���� (F605/M605 ����Function����)
				// ���� : Product�� �����ǰ�, �� Product�� Vehicle���� �� ��� (�� : ID�� PV�� ����)
				//[20161221] S201 �� ��� M Product�� �������� �ʵ��� ��
				if (newComp.getProperty("object_type").equals("Product") && newComp.getProperty("item_id").startsWith("PV") && !"S201".equals(projectCode)) {
					String sMProductDefaultOwner = "";
					PreferenceService.createService(session);
					sMProductDefaultOwner = PreferenceService.getValue("MProductDefaultOwner");
					
					if (sMProductDefaultOwner == null || sMProductDefaultOwner.equals("") || sMProductDefaultOwner.length() == 0) {
						MessageBox.post("A BOPAdmin is not exist or assigned who is a MProduct default owner.\nPlease contact to administrator.", "Change Owner", MessageBox.ERROR);
						return;
					}
					
					TCComponentUser user = SYMTcUtil.getTCUser(session, sMProductDefaultOwner).getUser();
					TCComponentGroup[] groups = user.getGroups();
					TCComponentGroup groupBOPAdmin = null;
					
					for (int inx = 0; inx < groups.length; inx++) {
						TCComponentRole[] roles = user.getRoles(groups[inx]);
						
						for (int jnx = 0; jnx < roles.length; jnx++) {
							String sRole = roles[jnx].toDisplayString();
							if (sRole.toUpperCase().equals("BOPADMIN")) {
								groupBOPAdmin = groups[inx];
								break;
							}
						}
					}
					
					// MProduct ���� �� Owner ����.
					if (user == null || groupBOPAdmin == null) {
						MessageBox.post("A BOPAdmin is not exist or assigned who is a MProduct default owner.\nPlease contact to administrator.", "Change Owner", MessageBox.ERROR);
						return;
					}
					
				    MProductOperation MPOperation = new MProductOperation(newComp);
				    TCComponentItem itemMproduct = MPOperation.createMproduct();
				    TCComponentItem itemFunction = MPOperation.createFunction();
				    TCComponentItem itemFMP = MPOperation.createFMP();
				    
				    // MProduct�� Owner�� Ư�� ����ڷ� ����
				    // ����� ���� Preference : MProductDefaultOwner 
				    itemMproduct.changeOwner(user, groupBOPAdmin);
				    itemMproduct.getLatestItemRevision().changeOwner(user, groupBOPAdmin);
//				    itemFunction.changeOwner(user, groupBOPAdmin);
//				    itemFunction.getLatestItemRevision().changeOwner(user, groupBOPAdmin);
//				    itemFMP.changeOwner(user, groupBOPAdmin);
//				    itemFMP.getLatestItemRevision().changeOwner(user, groupBOPAdmin);
				    
				    // Weld Function, Weld FMP �ڰ�����
				    SYMTcUtil.selfRelease(itemFunction.getLatestItemRevision(), "PSR");
				    SYMTcUtil.selfRelease(itemFMP.getLatestItemRevision(), "PSR");
				}
				//---------------------------------------------------------------------------------------------------------

				InterfaceAIFComponent[] components = AIFUtility.getCurrentApplication().getTargetComponents();
				if(components != null && components.length != 0) {
					if(components[0] instanceof TCComponentFolder) {
						((TCComponentFolder) components[0]).add("contents", newComp);
					} else {
						session.getUser().getHomeFolder().add("contents", newComp);
					}
				} else {
					session.getUser().getHomeFolder().add("contents", newComp);
				}
				
//				if (AIFUtility.getTargetComponents() != null && AIFUtility.getTargetComponents().length != 0) {
//					if (AIFUtility.getTargetComponents()[0] instanceof TCComponentFolder) {
//						((TCComponentFolder) (AIFUtility.getTargetComponents()[0])).add("contents", newComp);
//					} else {
//						session.getUser().getHomeFolder().add("contents", newComp);
//					}
//				} else {
//					session.getUser().getHomeFolder().add("contents", newComp);
//				}
			}

			mp.forget();
			mp = null;

			endOperation();

			if( bar != null )
			  bar.closeProgressBar();

		} catch (Exception e) {
//			if (bar != null) {
//				bar.closeProgressBar();
//			}
			MessageBox.post(AIFUtility.getActiveDesktop(), "���� �߻�", e.getMessage(), "�˸�",
					MessageBox.INFORMATION, false);
			e.printStackTrace();
		} finally {
			if (mp != null) {
//				bar.closeProgressBar();
				mp.rollBack();
			}
		}
	}
	
	/**
	 * ������ ���� �κ��� ���� �Ѵ�. �ݵ�� ����Ŭ������ exception�� �����Ѵ�.
	 * 
	 * @Copyright : S-PALM
	 * @author : ������
	 * @since : 2012. 4. 25.
	 * @throws Exception
	 */
	public abstract void createItem() throws Exception;

	/**
	 * �Ӽ��� �Է� �κ��� ���� �Ѵ� �ݵ�� ����Ŭ������ exception�� �����Ѵ�.
	 * 
	 * @Copyright : S-PALM
	 * @author : ������
	 * @since : 2012. 4. 25.
	 * @throws Exception
	 */
	public abstract void setProperties() throws Exception;

	/**
	 * ���� ���۷��̼� ���� �۾��� �ʿ��� �κ��� ���� �Ѵ�. �ݵ�� ����Ŭ������ exception�� �����Ѵ�.
	 * 
	 * @Copyright : S-PALM
	 * @author : ������
	 * @since : 2012. 4. 25.
	 * @throws Exception
	 */
	public abstract void startOperation() throws Exception;

	/**
	 * ���� ���۷��̼� ���� �۾��� �ʿ��� �κ��� ���� �Ѵ�. �ݵ�� ����Ŭ������ exception�� �����Ѵ�.
	 * 
	 * @Copyright : S-PALM
	 * @author : ������
	 * @since : 2012. 4. 25.
	 * @throws Exception
	 */
	public abstract void endOperation() throws Exception;
	
	public TCComponent getNewComp() {
		return newComp;
	}

}
