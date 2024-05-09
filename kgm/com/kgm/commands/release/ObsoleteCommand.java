package com.kgm.commands.release;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.ProcessUtil;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentProcessType;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentTaskTemplateType;
import com.teamcenter.rac.util.MessageBox;

/**
 * [SR170223-016][20170224] EPL Report ����_�̰��� Variant ����
 */
public class ObsoleteCommand extends AbstractAIFCommand {

	/**
	 * Obsolete
	 */
	public ObsoleteCommand() {
		try {
			InterfaceAIFComponent[] aifTargets = CustomUtil.getTargets();
			
			for (int inx = 0; inx < aifTargets.length; inx++) {
				InterfaceAIFComponent aifTarget = aifTargets[inx];
				
				// Vehicle Part, Standard Part Item Revision�� ���.
				if (aifTarget instanceof TCComponentItemRevision) {
					TCComponentItemRevision ir = (TCComponentItemRevision) aifTarget;
					
					// [SR170223-016][20170224] EPL Report ����_�̰��� Variant ���� - EPL Report�� ������ڰ� ���� Variant �����÷����� �������� ����
					if (!ir.getType().equals("S7_VariantRevision") &&
						!ir.getType().equals("S7_MaterialRevision") ) {
						MessageBox.post("Select a Variant or Material Part.", "Error", MessageBox.ERROR);
						return;
					}
					/*
					if (!ir.getType().equals("S7_VehpartRevision") && !ir.getType().equals("S7_StdpartRevision") && !ir.getType().equals("S7_MaterialRevision")) {
						MessageBox.post("Select a Vehicle, Standard or Material Part.", "Error", MessageBox.ERROR);
						return;
					}
					*/
					TCComponentTaskTemplate templateTask = null;
					TCComponentTaskTemplateType typeTemplateTask = (TCComponentTaskTemplateType) ir.getSession().getTypeComponent("EPMTaskTemplate");
					
					TCComponentTaskTemplate[] templateTasks = typeTemplateTask.getProcessTemplates(false, false, null, null, null);
					for (int jnx = 0; jnx < templateTasks.length; jnx++) {
						String sTemplateName = templateTasks[jnx].toString();
						
						if (sTemplateName.equals("Obsolete")) {
							templateTask = templateTasks[jnx];
							break;
						}
					}
					
					if (templateTask == null) {
						MessageBox.post("Contact to administrator.", "Error", MessageBox.ERROR);
					}
					
					TCComponentProcessType typeProcess = (TCComponentProcessType) ir.getSession().getTypeComponent("Job");
					typeProcess.create("Obsolete", "", templateTask, new TCComponent[] {ir}, ProcessUtil.getAttachTargetInt(new TCComponent[] {ir}));
				} else {
					MessageBox.post("Select a Item Revision.", "Error", MessageBox.ERROR);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
	}
}
