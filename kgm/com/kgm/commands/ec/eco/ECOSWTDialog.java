package com.kgm.commands.ec.eco;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.dialog.SYMCAbstractDialog;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class ECOSWTDialog extends SYMCAbstractDialog {
	
	private Registry registry;
	private ECOSWTRendering infoPanel;
	public ECOSWTDialog(Shell parent) {
		super(parent);
		this.registry = Registry.getRegistry(this);
		this.setApplyButtonVisible(false);
	}
	
	protected void createDialogWindow(Composite paramComposite) {
		super.createDialogWindow(paramComposite);
	}

	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
		setDialogTextAndImage(registry.getString("ECOMasterDialog.TITLE"), null);
		infoPanel = new ECOSWTRendering(parentScrolledComposite, true);
		return infoPanel.getComposite();
	}
	
	/**
	 * "X" 버튼 클릭 시 확인 창 오픈
	 * 닫기 버튼 클릭도 같이 수정 해야 함.
	 */
	@Override
	protected boolean canHandleShellCloseEvent() {
		if(infoPanel.getEcoRevision() == null){
			org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO | SWT.CANCEL);
			box.setText("Confirm Dialog");
			box.setMessage("Do not close the stores as it is disappears.\nWould you really close?");

			int choice = box.open();
			if(choice == SWT.CANCEL) {
				return false;
			} else if(choice == SWT.NO) {
				return false;
			}
		}

		return true;
	}
	
	/**
	 * 확인 창 오픈 후 닫기 실행
	 */
	@Override
	protected void cancelPressed() {
		if(canHandleShellCloseEvent()){
			this.saveDisplayParameters();
			this.close();
		}
	}

	/**
	 * 적용 전 벨리데이션
	 */
	@Override
	protected boolean validationCheck() {
		return infoPanel.isSavable();
	}

	/**
	 * 저장
	 */
	@Override
	protected boolean apply() {
		StringBuffer message = new StringBuffer(); 
		if(infoPanel.getEcoRevision() == null){
			infoPanel.create();
			message.append("ECO has been successfully registered.\n");
		}
		
		if(infoPanel.getEcoRevision() != null) {
			infoPanel.save();
			message.append(infoPanel.getEcoRevision() + " has been saved successfully.\n");
		}else{
			return false;
		}
		if(message.length() > 0)
			MessageBox.post(this.getShell(), message.toString(), "ECO Information", MessageBox.INFORMATION);
		return true;
	}

}
