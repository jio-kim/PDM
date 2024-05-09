package com.kgm.commands.variantconditionset;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.common.actions.AbstractAIFAction;

/**
 * OOTB�� ����� �� Dialog���
 * ���� ������ Dialog�� ����ϵ��� ������.
 * 
 * @author slobbie
 *
 */
public class ConditionSetAction extends AbstractAIFAction {

	public ConditionSetAction(AbstractAIFUIApplication abstractaifuiapplication, String s)
    {
        super(abstractaifuiapplication, s);
    }
	public ConditionSetAction(AIFDesktop arg0, String arg1) {
		super(arg0, arg1);
	}

	@Override
	public void run() {
		ConditionSetCommand command = new ConditionSetCommand();
		try {
			command.executeCommand();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
