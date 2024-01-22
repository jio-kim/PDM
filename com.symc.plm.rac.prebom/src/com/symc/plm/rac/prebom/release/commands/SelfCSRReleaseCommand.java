package com.symc.plm.rac.prebom.release.commands;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.SYMTcUtil;
import com.symc.plm.rac.prebom.common.TypeConstant;
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
 * (PreProduct/PreFunction/PreFunctionMaster/PreVehPart) SelfRelese Command(CSR)
 * 
 */
public class SelfCSRReleaseCommand extends AbstractAIFCommand {

	@Override
    protected void executeCommand() throws Exception {
	       /** Dialog 호출. */
        final InterfaceAIFComponent[] coms = CustomUtil.getTargets();

        if (coms != null && coms.length > 0) {
            StringBuffer szMessage = new StringBuffer();

            for (int i = 0; i < coms.length; i++) {
                // 상위 Part Check
                if (!(coms[i] instanceof TCComponentItemRevision)) {
                    szMessage.append(coms[i].toString() + " Type Is Not PreProduct/PreFunction/PreFunctionMaster/PreVehPart Revision \n");
                } else if (!(coms[i].getType().equals(TypeConstant.S7_PREPRODUCTREVISIONTYPE)
                        || coms[i].getType().equals(TypeConstant.S7_PREFUNCTIONREVISIONTYPE) || coms[i].getType().equals(TypeConstant.S7_PREFUNCMASTERREVISIONTYPE)
                        || coms[i].getType().equals(TypeConstant.S7_PREVEHICLEPARTREVISIONTYPE))) {
                    szMessage.append(coms[i].toString() + " Type Is Not PreProduct/PreFunction/PreFunctionMaster/PreVehPart Revision \n");
                } else {
                    TCComponentItemRevision targetRevision = (TCComponentItemRevision) coms[i];
                    // Release Check
                    if (CustomUtil.isReleased(targetRevision)) {
                        szMessage.append(coms[i].toString() + " is Released \n");
                    } // CheckOut Check
                    else if (CustomUtil.isTargetCheckOut(targetRevision)) {
                        szMessage.append(coms[i].toString() + " is Checked-Out  \n");
                    }
                }
            }

            // Error 발생시 Return
            if (!"".equals(szMessage.toString())) {
                MessageBox.post(AIFUtility.getActiveDesktop().getShell(), szMessage.toString(), "INFORMATION", MessageBox.ERROR);
                return;
            }

            final WaitProgressBar waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
            waitProgress.setShowButton(true);
            waitProgress.start();

            int nError = 0;
            for (int i = 0; i < coms.length; i++) {
                try {
                    TCComponentItemRevision targetRevision = (TCComponentItemRevision) coms[i];

                    waitProgress.setStatus("\nRelease " + targetRevision.toString() + "...", true);

                    String maturity = targetRevision.getProperty("s7_MATURITY");
                    targetRevision.setProperty("s7_MATURITY", "Released");

                    try {
                        // Product Self Release
                        SYMTcUtil.selfRelease(targetRevision, "CSR");
                    }
                    catch (Exception ee) {
                        targetRevision.setProperty("s7_MATURITY", maturity);
                        throw ee;
                    }

                    //Effectivity Data를 결재당일의 새벽 00시로 변경함.
                    TCComponentReleaseStatus status = (TCComponentReleaseStatus) targetRevision.getRelatedComponent("release_status_list");
                    if (status != null) {
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
                catch (Exception e) {
                    e.printStackTrace();
                    waitProgress.setStatus("Error : " + e.getMessage(), true);
                    nError++;
                }
            }

            if (nError > 0){
                waitProgress.close("Error", true, false);
            } else {
                waitProgress.close("Completed", false, false);
            }
        }
	}
}
