package com.ssangyong.commands.revise;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.ssangyong.common.dialog.SYMCAWTAbstractDialog;
import com.teamcenter.rac.aif.InterfaceAIFOperationExecutionListener;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Revise Dialog
 */
@SuppressWarnings("serial")
public class SYMCReviseDialog extends SYMCAWTAbstractDialog {
	
	/** Revise Info Panel */
	private SYMCRevisePanel SYRevisePanel;
	/** TC Registry */
	private Registry registry;
	private Exception ex;
	
	public SYMCReviseDialog(Frame frame) throws Exception {
		super(frame);
		this.registry = Registry.getRegistry(this);
		initUI();
		ex = null;
	}

	private void initUI() throws Exception {
		/** Title 지정 */
		setTitle(registry.getString("ReviseDialog.TITLE"));
		/** 상위 텍스트 및 아이콘 지정. 필수 적용 메소드 항목 */
		createDialogUI(registry.getString("ReviseDialog.MESSGAE_TITLE"), registry.getImageIcon("Revise_32.ICON"));
		
		SYRevisePanel = new SYMCRevisePanel(this);
		JScrollPane mainScrollPane = new JScrollPane(SYRevisePanel);
		mainScrollPane.getViewport().setBackground(Color.white);
		mainScrollPane.updateUI();
		add("unbound.bind", mainScrollPane);
		showVisible(false);
	}

	@Override
	protected JPanel getUIPanel() {
		return SYRevisePanel;
	}

	@Override
	public boolean validCheck(){
		return SYRevisePanel.validCheck();
	}

	@Override
	public void invokeOperation(ActionEvent e) throws Exception{
		SYMCReviseOperation operation = new SYMCReviseOperation(this, SYRevisePanel);
		ex = null;
		operation.addOperationListener(new InterfaceAIFOperationExecutionListener()
		{
			@Override
			public void startOperation(String paramString)
			{
			}
			
			@Override
			public void endOperation()
			{
			}
			
			@Override
			public void exceptionThrown(Exception paramException)
			{
				ex = paramException;
			}
		});
		session.queueOperationAndWait(operation);
		if(ex != null)
		{
			throw ex;
		}
	}
	

}
