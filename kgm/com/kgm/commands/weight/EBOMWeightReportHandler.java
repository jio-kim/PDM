package com.kgm.commands.weight;

import java.lang.reflect.Constructor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SWTUtilities;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.SWTUIUtilities;

/**
 * 1.Command Class 의 생성자는 public 으로 한다.
 * 2.Command Class 의 생성자는 String parameter 하나를 가지도록 한다.
 * 3.Command ID = Command Class의 풀 패키지 명 + 클래스 이름
 * ex) Command Class 가 com.pungkang.newprocess.NewProcessCommand.java 인 경우 Command ID 는 "com.pungkang.newprocess.NewProcessCommand" 가 되어 야 한다.
 * 4.Command Class 에는 반드시 Default 생성자가 존재 하여야 한다. 
 * @Copyright : S-PALM
 * @author   : 이정건
 * @since    : 2012. 03. 22
 * Package ID : com.pungkang.handlers.SpalmCommonHandler.java
 */
public class EBOMWeightReportHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			EBOMWeightMasterListDialog weightDialog = new EBOMWeightMasterListDialog(AIFUtility.getActiveDesktop().getFrame(), CustomUtil.getTCSession());
			weightDialog.run();
			weightDialog.setModal(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
