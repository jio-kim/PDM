package com.symc.plm.rac.prebom.prebom.operation.precreate;

import org.eclipse.core.runtime.IStatus;

import com.ssangyong.common.SimpleProgressBar;
import com.ssangyong.common.utils.CustomUtil;
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
 * [SR140702-057][20140617] shcho M-Product 자동 생성 기능 추가 : E-BOM에 New Product가 생성되면 자동으로  M-Product 생성 (F605/M605 용접Function포함)
 */

public abstract class PreAbstractCreateOperation extends AbstractAIFOperation {
	
	/** 새롭게 생성 되는 Item */
	protected TCComponentItem newComp = null;

	protected TCSession session;

	/** operation 싱행도중 에러 발생시 rollback 시켜주기위한 MarkPoint */
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

			/** 새로운 아이템 생성 */
			createItem();

			if (newComp != null) {

				/** 속성값 입력 */
				setProperties();

				// [SR140702-057][20140617] shcho M-Product 자동 생성 기능 추가 : E-BOM에 New Product가 생성되면 자동으로  M-Product 생성 (F605/M605 용접Function포함)
				// 조건 : Product가 생성되고, 이 Product가 Vehicle관련 인 경우 (예 : ID가 PV로 시작)
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
			MessageBox.post(AIFUtility.getActiveDesktop(), "오류 발생", e.getMessage(), "알림",
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
	 * 아이템 생성 부분을 구현 한다. 반드시 상위클래스로 exception을 위임한다.
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2012. 4. 25.
	 * @throws Exception
	 */
	public abstract void createItem() throws Exception;

	/**
	 * 속성값 입력 부분을 구현 한다 반드시 상위클래스로 exception을 위임한다.
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2012. 4. 25.
	 * @throws Exception
	 */
	public abstract void setProperties() throws Exception;

	/**
	 * 실제 오퍼레이션 이전 작업에 필요한 부분을 구현 한다. 반드시 상위클래스로 exception을 위임한다.
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2012. 4. 25.
	 * @throws Exception
	 */
	public abstract void startOperation() throws Exception;

	/**
	 * 실제 오퍼레이션 이후 작업에 필요한 부분을 구현 한다. 반드시 상위클래스로 exception을 위임한다.
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2012. 4. 25.
	 * @throws Exception
	 */
	public abstract void endOperation() throws Exception;
	
	public TCComponent getNewComp() {
		return newComp;
	}

}
