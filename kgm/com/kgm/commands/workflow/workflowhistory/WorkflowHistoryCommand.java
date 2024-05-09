package com.kgm.commands.workflow.workflowhistory;

import java.awt.Frame;

import com.teamcenter.rac.aif.AbstractAIFApplication;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class WorkflowHistoryCommand extends AbstractAIFCommand {
	public TCSession session;
	public Frame frame;
	private Registry registry = Registry.getRegistry(this);
	
	public WorkflowHistoryCommand(){
		this(AIFUtility.getActiveDesktop(), AIFUtility.getCurrentApplication());
	}

	public WorkflowHistoryCommand(Frame frame, AbstractAIFApplication application) {
		this.session = (TCSession)application.getSession();
		this.frame = frame;
		InterfaceAIFComponent[] components = application.getTargetComponents();
		try {
			if (!isValidTarget(application.getTargetComponents()))
				return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		setRunnable(new WorkflowHistoryDialog(frame, (TCComponent)components[0]));
	}

	private boolean isValidTarget(InterfaceAIFComponent[] components) {
		if (components == null) {
			MessageBox.post("조회할 하나의 대상을 선택해 주세요.", registry.getString("Message.TITLE"), MessageBox.INFORMATION);
			return false;
		}
		if (components.length != 1) {
			MessageBox.post("조회할 하나의 대상을 선택해 주세요.",registry.getString("Message.TITLE"), MessageBox.INFORMATION);
			return false;
		}
		TCComponent component = (TCComponent)components[0];
		if (component.getType().equals("Job"))
			return true;
		try {
			if (component.getCurrentJob() == null) {
				MessageBox.post("결재 이력이 포함되어 있지 않습니다.", registry.getString("Message.TITLE"), MessageBox.INFORMATION);
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
