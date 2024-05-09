package com.kgm.common.operation;

import com.kgm.common.SimpleProgressBar;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

/**
 * 1. ���۷��̼� ���� �޼����� ���ڷ� ������ ����. 
 * 2. ���۷��̼� �Ϸ� �� �޼����� ���ڷ� ���� �� ����. 
 * 3. ���۷��̼� ���� ���� �߻��� rollback ��. 
 * 4. �������� ������ �߻� �޼ҵ� 4���� �ݵ�� try/catch �������� ���� ó���� �ϸ� �ȵǰ�,����Ŭ������ throws ���� �־����.
 * 
 * @Copyright : S-PALM
 * @author : ������
 * @since : 2012. 4. 25. Package ID :
 *        com.pungkang.common.operation.SpalmAbstractCreateOperation.java
 */
public abstract class SYMCAWTAbstractCreateOperation extends AbstractAIFOperation {

	/** ���Ӱ� ���� �Ǵ� Item */
	protected TCComponentItem newComp = null;

	protected TCSession session;

	/** operation ���൵�� ���� �߻��� rollback �����ֱ����� MarkPoint */
	private Markpoint mp;

	protected AbstractAIFDialog dialog;

	private String message;

	public SYMCAWTAbstractCreateOperation(AbstractAIFDialog dialog) {
		this(dialog, CustomUtil.getTCSession(), null);
	}

	public SYMCAWTAbstractCreateOperation(AbstractAIFDialog dialog, String startMessage) {
		this(dialog, CustomUtil.getTCSession(), startMessage);
	}

	public SYMCAWTAbstractCreateOperation(AbstractAIFDialog dialog, TCSession session, String startMessage) {
		super(startMessage);
		this.message = startMessage;
		this.session = session;
		this.dialog = dialog;
	}

	@Override
	public void executeOperation() throws Exception {
		SimpleProgressBar bar = null;
		try {
			bar = new SimpleProgressBar(dialog, message);

			startOperation();

			mp = new Markpoint(session);

			/** ���ο� ������ ���� */
			createItem();

			if (newComp != null) {

				/** �Ӽ��� �Է� */
				setProperties();

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

			bar.closeProgressBar();

		} catch (Exception e) {
			if (bar != null) {
				bar.closeProgressBar();
			}
			MessageBox.post(AIFUtility.getActiveDesktop(), "���� �߻�", e.getMessage(), "�˸�",
					MessageBox.INFORMATION, false);
			e.printStackTrace();
		} finally {
			if (mp != null) {
				bar.closeProgressBar();
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
