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
 * 1.Command Class �� �����ڴ� public ���� �Ѵ�.
 * 2.Command Class �� �����ڴ� String parameter �ϳ��� �������� �Ѵ�.
 * 3.Command ID = Command Class�� Ǯ ��Ű�� �� + Ŭ���� �̸�
 * ex) Command Class �� com.pungkang.newprocess.NewProcessCommand.java �� ��� Command ID �� "com.pungkang.newprocess.NewProcessCommand" �� �Ǿ� �� �Ѵ�.
 * 4.Command Class ���� �ݵ�� Default �����ڰ� ���� �Ͽ��� �Ѵ�. 
 * @Copyright : S-PALM
 * @author   : ������
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
