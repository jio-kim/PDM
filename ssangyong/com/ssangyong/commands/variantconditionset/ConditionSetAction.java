package com.ssangyong.commands.variantconditionset;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.common.actions.AbstractAIFAction;

/**
 * OOTB의 컨디션 셋 Dialog대신
 * 새로 생성한 Dialog를 사용하도록 수정됨.
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
