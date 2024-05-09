package com.kgm.handlers;

import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.kgm.commands.partmaster.editparts.EditMultiPartsDialog;
import com.kgm.common.SYMCClass;
import com.kgm.common.utils.progressbar.WaitProgressor;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class EditMultiPartsHandler extends AbstractHandler
{

	public EditMultiPartsHandler()
	{
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		final Shell shell = HandlerUtil.getActiveShell(event);
		//���� ��� üũ..
		final InterfaceAIFComponent[] targetComps = AIFUtility.getCurrentApplication().getTargetComponents();
		if (targetComps == null || targetComps.length == 0)
		{
			MessageBox.post(shell, "������ ����� �����ϴ� Vehpart Revision�� ������ �ּ���.", "���", MessageBox.WARNING);
			return null;
		}
		final ArrayList<TCComponentItemRevision> revisionList = new ArrayList<TCComponentItemRevision>();
		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				WaitProgressor waitProgressor = new WaitProgressor(shell);
				waitProgressor.start();
				waitProgressor.isShowMessageTable(true);//���̺� ���·� ������
				int total = targetComps.length;
				int count = 1;
				try
				{
					for (InterfaceAIFComponent targetComp : targetComps)
					{
						((TCComponent)targetComp).refresh();
						TCComponentItemRevision revision = null;
						waitProgressor.setMessage("���� ��� Ȯ�� ��... \n["+count+++"/"+total+"] " + targetComp.toString());//������ �޽��� ����.
						if (targetComp instanceof TCComponentBOMLine)
						{
							targetComp = ((TCComponentBOMLine) targetComp).getItemRevision();
						}
						if ((targetComp instanceof TCComponentItemRevision) && targetComp.getType().equals(SYMCClass.S7_VEHPARTREVISIONTYPE))
						{
							revision = (TCComponentItemRevision) targetComp;
						}
						if (revision == null)
						{
							//���� ó��...
							waitProgressor.setTableMessage("���", targetComp.toString(), "Vehpart Revision�� �ƴմϴ�.");
							continue;
						}
						if(!revision.okToModify())
						{
							//���� ó��...
							waitProgressor.setTableMessage("���", targetComp.toString(), "���� ������ �����ϴ�.");
							continue;
						}
						revisionList.add(revision);
					}
					waitProgressor.end(waitProgressor.getTableMessageList().isEmpty());
					if (!waitProgressor.getTableMessageList().isEmpty())
					{
						waitProgressor.setMessage("���� ��� Ȯ�� ��� > ���� ��� " + waitProgressor.getTableMessageList().size() + "�� �߰�\n�Ʒ� ����� �����ϰų� ���� �ذ� �� �ٽ� �õ��ϼ���.");
						return;
					}
				} catch (TCException e)
				{
					e.printStackTrace();
					waitProgressor.end();
					MessageBox.post(shell, e, true);
					return;
				}
				shell.getDisplay().asyncExec(new Runnable()
				{

					@Override
					public void run()
					{
						EditMultiPartsDialog editMultiPartsDialog = new EditMultiPartsDialog(shell, revisionList);
						editMultiPartsDialog.open();
					}
				});
			}
		}).start();
		return null;
	}

}
