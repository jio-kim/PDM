package com.ssangyong.commands.release;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.widgets.Shell;

import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentEffectivity;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentReleaseStatus;
import com.teamcenter.rac.kernel.tcservices.TcEffectivityService;
import com.teamcenter.rac.util.MessageBox;

/**
 * Vehicle Part SelfRelese Command(DSR)
 * 
 * 정규품번('P' Stage)이 아닌경우만 가능
 *
 */
public class SelfReleaseCommand extends AbstractAIFCommand
{

	@SuppressWarnings("unused")
    public SelfReleaseCommand() throws Exception
	{
		/** Dialog 호출. */
		Shell shell = AIFUtility.getActiveDesktop().getShell();
		final InterfaceAIFComponent[] coms = CustomUtil.getTargets();

		if (coms != null && coms.length > 0)
		{
			StringBuffer szMessage = new StringBuffer();

			for (int i = 0; i < coms.length; i++)
			{
				// Vehicle Part Revision만 허용
				if (!(coms[i] instanceof TCComponentItemRevision))
				{
					szMessage.append(coms[i].toString() + " Type Is Not Vehicle Part Revision \n");
				}
				else if (!((TCComponentItemRevision) coms[0]).getType().equals(SYMCClass.S7_VEHPARTREVISIONTYPE))
				{
					szMessage.append(coms[i].toString() + " Type Is Not Vehicle Part Revision \n");
				}
				else
				{
					TCComponentItemRevision targetRevision = (TCComponentItemRevision) coms[i];
					// Release Check
					if (CustomUtil.isReleased(targetRevision))
					{
						szMessage.append(coms[i].toString() + " is Released \n");
					}
					// CheckOut Check
					else if (CustomUtil.isTargetCheckOut(targetRevision))
					{
						szMessage.append(coms[i].toString() + " is Checked-Out  \n");
					}
					// 'P' Stage가 아닌경우만 허용
					else
					{
						String strStage = targetRevision.getProperty("s7_STAGE");

						if ("P".equals(strStage))
						{
							szMessage.append(coms[i].toString() + " Is 'P' Stage, Only D/F Stage Available \n");
						}

					}
				}

			}

			// Error 발생시 Return
			if (!"".equals(szMessage.toString()))
			{
				MessageBox.post(AIFUtility.getActiveDesktop().getShell(), szMessage.toString(), "INFORMATION", MessageBox.ERROR);
				return;
			}

			final WaitProgressBar waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
			waitProgress.setShowButton(true);
			waitProgress.start();

			int nError = 0;
			for (int i = 0; i < coms.length; i++)
			{
				try
				{
					TCComponentItemRevision targetRevision = (TCComponentItemRevision) coms[i];

					waitProgress.setStatus("\nRelease " + targetRevision.toString() + "...", true);

					// Maturiy속성을 Released로 변경
					String maturity = targetRevision.getProperty("s7_MATURITY");
					targetRevision.setProperty("s7_MATURITY", "Released");

					try
					{
						// Design Self Release
						SYMTcUtil.selfRelease(targetRevision, "DSR");
					}
					catch (Exception ee)
					{
						// Error 발생시 Maturity 속성 복원
						targetRevision.setProperty("s7_MATURITY", maturity);
						throw ee;
					}
					

					// Effectivity Data를 결재당일의 새벽 00시로 변경함(OOTB 기능 불가)
					TCComponentReleaseStatus status = (TCComponentReleaseStatus) targetRevision.getRelatedComponent("release_status_list");
					if (status != null)
					{
						Date releaseDate = targetRevision.getDateProperty("date_released");

						Date adate[] = new Date[1];
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						String stringReleasDate = sdf.format(releaseDate);
						adate[0] = sdf.parse(stringReleasDate);

						TcEffectivityService.createReleaseStatusEffectivity(targetRevision.getSession(), (TCComponent) status, "", null, null, "", adate,
								TCComponentEffectivity.OpenEndedStatus.UP.getPropertyValue(), false);
					}

					waitProgress.setStatus("Completed", true);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					waitProgress.setStatus("Error : " + e.getMessage(), true);
					nError++;

				}
			}

			if (nError > 0)
				waitProgress.close("Error", true, false);
			else
				waitProgress.close("Completed", false, false);

		}
	}
}
