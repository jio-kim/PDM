package com.symc.plm.rac.prebom.prebom.operation.precreate;

import org.eclipse.core.runtime.IStatus;

import com.kgm.common.SimpleProgressBar;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.dialog.AbstractSWTDialog;



/** 
 * 
 * [SR140702-057][20140617] shcho M-Product �ڵ� ���� ��� �߰� : E-BOM�� New Product�� �����Ǹ� �ڵ�����  M-Product ���� (F605/M605 ����Function����)
 */

public abstract class PreAbstractCreateOperation extends AbstractAIFOperation {
	
	/** ���Ӱ� ���� �Ǵ� Item */
	protected TCComponentItem newComp = null;

	protected TCSession session;

	/** operation ���൵�� ���� �߻��� rollback �����ֱ����� MarkPoint */
	private Markpoint mp;

	protected AbstractSWTDialog dialog;

	@SuppressWarnings("unused")
    private String message;

	public PreAbstractCreateOperation(AbstractSWTDialog dialog) {
		this(dialog, CustomUtil.getTCSession(), null);
	}

	public PreAbstractCreateOperation(AbstractSWTDialog dialog, String startMessage) {
		this(dialog, CustomUtil.getTCSession(), startMessage);
	}

	public PreAbstractCreateOperation(AbstractSWTDialog dialog, TCSession session, String startMessage) {
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

				// [SR140702-057][20140617] shcho M-Product �ڵ� ���� ��� �߰� : E-BOM�� New Product�� �����Ǹ� �ڵ�����  M-Product ���� (F605/M605 ����Function����)
				// ���� : Product�� �����ǰ�, �� Product�� Vehicle���� �� ��� (�� : ID�� PV�� ����)
//				if (newComp.getProperty("object_type").equals("Product") && newComp.getProperty("item_id").startsWith("PV")) {
//				    MProductOperation MPOperation = new MProductOperation(newComp);
//				    MPOperation.createMproduct();
//				    MPOperation.createFunction();
//				    MPOperation.createFMP();
//				}
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

			this.storeOperationResult(IStatus.OK);
		} catch (Exception e) {
//			if (bar != null) {
//				bar.closeProgressBar();
//			}
		    this.storeOperationResult(IStatus.ERROR);
			MessageBox.post(AIFUtility.getActiveDesktop(), "���� �߻�", e.getMessage(), "�˸�",
					MessageBox.INFORMATION, false);
			e.printStackTrace();
			throw e;
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
