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
 * @Copyright : PLMSOFT
 * @author   : Á¶¼®ÈÆ
 * @since    : 2018. 05. 30
 */
public class EBOMWeightAllReportHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			EBOMWeightDialog weightDialog = new EBOMWeightDialog(AIFUtility.getActiveDesktop().getFrame(), CustomUtil.getTCSession());
			weightDialog.run();
			weightDialog.setModal(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
