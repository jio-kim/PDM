/**
 * LovManageCommand.java
 * LOV ���� ����� �����մϴ�.
 */
package com.kgm.admin.lovmanage;

import java.awt.Dimension;

import javax.swing.JOptionPane;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.util.MessageBox;

public class LovManageCommand extends AbstractAIFCommand
{

	private String infodbaPassword = "";

	@SuppressWarnings("rawtypes")
	public LovManageCommand()
	{
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				//infodba �н����带 �޴´�.
				infodbaPassword = JOptionPane.showInputDialog("infodba �н����带 �Է��ϼ���.");
				if (infodbaPassword == null || infodbaPassword.trim().isEmpty())
				{
					MessageBox.post(AIFDesktop.getActiveDesktop(), "�н����尡 �Էµ��� �ʾҽ��ϴ�.", "����", MessageBox.ERROR);
					return;
				}
				try
				{
					LovManagerDialog dlg = new LovManagerDialog(infodbaPassword);
					dlg.setVisible(true);
					while(!dlg.isVisible())
					{
						Thread.sleep(1000);
					}
					Thread.sleep(2000);
					Dimension dimension = dlg.getSize();
					dlg.setSize(dimension.width, dimension.height + 10);

				} catch (Exception e)
				{
					MessageBox.post(AIFDesktop.getActiveDesktop(), e);
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}
}
