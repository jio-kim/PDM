/**
 * LovManageCommand.java
 * LOV 관리 기능을 제공합니다.
 */
package com.ssangyong.admin.lovmanage;

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
				//infodba 패스워드를 받는다.
				infodbaPassword = JOptionPane.showInputDialog("infodba 패스워드를 입력하세요.");
				if (infodbaPassword == null || infodbaPassword.trim().isEmpty())
				{
					MessageBox.post(AIFDesktop.getActiveDesktop(), "패스워드가 입력되지 않았습니다.", "오류", MessageBox.ERROR);
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
