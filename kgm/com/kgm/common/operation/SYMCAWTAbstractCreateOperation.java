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
 * 1. 오퍼레이션 상태 메세지를 인자로 받을수 있음. 
 * 2. 오퍼레이션 완료 후 메세지를 인자로 받을 수 있음. 
 * 3. 오퍼레이션 도중 에러 발생시 rollback 됨. 
 * 4. 하위에서 구현할 추상 메소드 4개는 반드시 try/catch 문장으로 예외 처리를 하면 안되고,상위클래스로 throws 시켜 주어야함.
 * 
 * @Copyright : S-PALM
 * @author : 이정건
 * @since : 2012. 4. 25. Package ID :
 *        com.pungkang.common.operation.SpalmAbstractCreateOperation.java
 */
public abstract class SYMCAWTAbstractCreateOperation extends AbstractAIFOperation {

	/** 새롭게 생성 되는 Item */
	protected TCComponentItem newComp = null;

	protected TCSession session;

	/** operation 싱행도중 에러 발생시 rollback 시켜주기위한 MarkPoint */
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

			/** 새로운 아이템 생성 */
			createItem();

			if (newComp != null) {

				/** 속성값 입력 */
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
			MessageBox.post(AIFUtility.getActiveDesktop(), "오류 발생", e.getMessage(), "알림",
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
