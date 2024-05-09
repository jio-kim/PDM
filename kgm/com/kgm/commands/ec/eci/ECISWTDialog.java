package com.kgm.commands.ec.eci;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.dialog.SYMCAbstractDialog;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class ECISWTDialog extends SYMCAbstractDialog {
	
	private Registry registry;
	private ECISWTRendering infoPanel;
	private TCComponentItemRevision selectedItemRevision;
	
	public ECISWTDialog(Shell parent, TCComponentItemRevision selectedItemRevision) {
		super(parent);
		registry = Registry.getRegistry(this);
		this.selectedItemRevision = selectedItemRevision;
		this.setApplyButtonVisible(false);
		
	}
	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
		setDialogTextAndImage(registry.getString("ECIMasterDialog.TITLE"), null);
		infoPanel = new ECISWTRendering(parentScrolledComposite, selectedItemRevision);
		infoPanel.setCreateMode(true);	// [SR140701-022] jclee, ECI 생성 모드로 화면 Open. (Create Workflow 버튼 Hiding)
		return infoPanel.getComposite();
	}
	@Override
	protected boolean validationCheck() {
		return true;
	}
	@Override
	protected boolean apply() {
		StringBuffer message = new StringBuffer(); 
		if(infoPanel.getECIRevision() == null){
			infoPanel.create();
			message.append("ECI has been successfully registered.\n");	// [SR140701-022] jclee, ECI 생성분으로 메시지 출력 변경
		}
		
		if(infoPanel.getECIRevision() != null) {
			infoPanel.save();
			message.append(infoPanel.getECIRevision() + " has been saved successfully.\n");
		}else{
			return false;
		}
		if(message.length() > 0)
			MessageBox.post(this.getShell(), message.toString(), "ECI Information", MessageBox.INFORMATION);	// [SR140701-022] jclee, ECI 생성분으로 메시지 출력 변경
		return true;
	}
}
