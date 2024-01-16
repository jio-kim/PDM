package com.ssangyong.handlers;

import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ssangyong.commands.partmaster.editparts.EditMultiPartsDialog;
import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.utils.progressbar.WaitProgressor;
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
		//선택 대상 체크..
		final InterfaceAIFComponent[] targetComps = AIFUtility.getCurrentApplication().getTargetComponents();
		if (targetComps == null || targetComps.length == 0)
		{
			MessageBox.post(shell, "선택한 대상이 없습니다 Vehpart Revision을 선택해 주세요.", "경고", MessageBox.WARNING);
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
				waitProgressor.isShowMessageTable(true);//테이블 형태로 나오게
				int total = targetComps.length;
				int count = 1;
				try
				{
					for (InterfaceAIFComponent targetComp : targetComps)
					{
						((TCComponent)targetComp).refresh();
						TCComponentItemRevision revision = null;
						waitProgressor.setMessage("선택 대상 확인 중... \n["+count+++"/"+total+"] " + targetComp.toString());//상위에 메시지 띄우기.
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
							//오류 처리...
							waitProgressor.setTableMessage("경고", targetComp.toString(), "Vehpart Revision이 아닙니다.");
							continue;
						}
						if(!revision.okToModify())
						{
							//오류 처리...
							waitProgressor.setTableMessage("경고", targetComp.toString(), "수정 권한이 없습니다.");
							continue;
						}
						revisionList.add(revision);
					}
					waitProgressor.end(waitProgressor.getTableMessageList().isEmpty());
					if (!waitProgressor.getTableMessageList().isEmpty())
					{
						waitProgressor.setMessage("선택 대상 확인 결과 > 문제 대상 " + waitProgressor.getTableMessageList().size() + "건 발견\n아래 대상을 제외하거나 문제 해결 후 다시 시도하세요.");
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
