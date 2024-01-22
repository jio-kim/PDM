/**
 * 
 */
package com.symc.plm.rac.prebom.prebom.commands;

import javax.swing.JFrame;

import com.ssangyong.common.utils.CustomUtil;
import com.symc.plm.rac.prebom.prebom.dialog.weightmasterlist.WeightMasterListDialog;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;

/**
 * @author jinil
 * 
 */
public class WeightMasterListCommand extends AbstractAIFCommand {
    // private Registry registry =
    // Registry.getRegistry("com.symc.plm.rac.prebom.prebom.dialog.revise.revise");
    private TCComponentItemRevision targetPreProduct = null;
    private TCComponentBOMLine bomLine = null;
    private TCComponentItemRevision ospecRevision = null;

    @Override
    protected void executeCommand() throws Exception {
//        String oSpecNo = null;
        InterfaceAIFComponent[] targetComponents = AIFUtility.getCurrentApplication().getTargetComponents();
        if (targetComponents != null && targetComponents.length == 1)
        {
//            if (targetComponents[0] instanceof TCComponentItem)
//                oSpecNo = ((TCComponentItem) targetComponents[0]).getLatestItemRevision().getProperty(PropertyConstant.ATTR_NAME_OSPECNO);
//            else if (targetComponents[0] instanceof TCComponentItemRevision)
//                oSpecNo = ((TCComponentItemRevision) targetComponents[0]).getProperty(PropertyConstant.ATTR_NAME_OSPECNO);
//            else if (targetComponents[0] instanceof TCComponentBOMLine)
//                oSpecNo = ((TCComponentBOMLine) targetComponents[0]).window().getTopBOMLine().getItemRevision().getProperty(PropertyConstant.ATTR_NAME_OSPECNO);
        }
//
//        Thread t = new Thread(){
//
//            @Override
//            public void run() {
//                // TODO Auto-generated method stub
//                try {
//                    WeightMasterListDialog weightDialog = new WeightMasterListDialog(AIFUtility.getActiveDesktop().getFrame(), CustomUtil.getTCSession());
//                    weightDialog.setModal(true);
//                    weightDialog.setVisible(true);
//                } catch (Exception e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//                
//            }
//            
//        };
//        t.start();
//        WeightMasterListDialog weightDialog = new WeightMasterListDialog(AIFUtility.getActiveDesktop().getFrame(), CustomUtil.getTCSession());
////        weightDialog.setModal(true);
//        setRunnable(weightDialog);
//        super.executeCommand();

        AbstractAIFOperation runDialog = new AbstractAIFOperation() {
            @Override
            public void executeOperation() throws Exception {
                WeightMasterListDialog weightDialog = new WeightMasterListDialog(AIFUtility.getActiveDesktop().getFrame(), CustomUtil.getTCSession());
                weightDialog.setVisible(true);
            }
        };
        CustomUtil.getTCSession().queueOperation(runDialog);

//        SelectOspecDialog ospecDialog = new SelectOspecDialog(AIFUtility.getActiveDesktop().getShell(), oSpecNo);
//        ospecDialog.open();
        

//        if (!startValidate())
//            return;
//
//        bomLine = CustomUtil.getBomline(targetPreProduct, targetPreProduct.getSession());
//        ospecRevision = getOSpecRevision();
//        if (ospecRevision == null) {
//            throw new Exception("Could not found OSpec Revision.");
//        }
//
//        TCComponentItemRevision ospecLatestRevision = ospecRevision.getItem().getLatestItemRevision();
//        if (!ospecRevision.equals(ospecLatestRevision)) {
//            int response = JOptionPane.showConfirmDialog(null, "OSpec version has been changed. \nAre you sure you want to apply the latest version?",
//                    "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
//            if (response == JOptionPane.YES_OPTION) {
//                // 최신 Ospec을 가져온다.
//                ospecRevision = ospecLatestRevision;
//            } else if (response == JOptionPane.NO_OPTION) {
//            } else {
//                return;
//            }
//        }
//
//        final WaitProgressBar waitBar = new WaitProgressBar(AIFUtility.getActiveDesktop().getFrame());
//        waitBar.start();
//
//        final WeightMasterListDialogInitOperation initOp = new WeightMasterListDialogInitOperation(bomLine, ospecRevision, waitBar);
//        initOp.addOperationListener(new InterfaceAIFOperationListener() {
//
//            @Override
//            public void startOperation(String arg0) {
//                waitBar.setStatus("FMP BOM Loading...");
//            }
//
//            @Override
//            public void endOperation() {
//                waitBar.setStatus("Creating dialog...");
//
//                try {
//                    WeightMasterListDialog dlg = new WeightMasterListDialog(bomLine, initOp.getFMPLines(), ospecRevision, initOp.getOspec(), initOp
//                            .getAllOptionManagerMap(), initOp.getAllVariantOption(), waitBar);
//                    dlg.addWindowListener(new WindowAdapter() {
//
//                        @Override
//                        public void windowClosing(WindowEvent windowevent) {
//
//                            if (bomLine != null) {
//                                try {
//                                    TCComponentBOMWindow window = bomLine.window();
//                                    if (window != null) {
//                                        window.close();
//                                    }
//                                } catch (TCException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                            super.windowClosed(windowevent);
//                        }
//                    });
//                    waitBar.dispose();
//                    dlg.setVisible(true);
//                } catch (Exception e) {
//                    try {
//                        bomLine.window().close();
//                    } catch (Exception e1) {
//                        e1.printStackTrace();
//                    } finally {
//                        e.printStackTrace();
//                        waitBar.setStatus(e.getMessage());
//                        waitBar.setShowButton(true);
//                    }
//                }
//            }
//        });
//        bomLine.getSession().queueOperation(initOp);
    }

//    private TCComponentItemRevision getOSpecRevision() throws Exception {
//        try {
//            String ospecNo = targetPreProduct.getProperty(PropertyConstant.ATTR_NAME_OSPECNO);
//
//            if (ospecNo == null || ospecNo.equals("")) {
//                throw new TCException("Could not found OSPEC_NO.");
//            }
//
//            int idx = ospecNo.lastIndexOf("-");
//            if (idx < 0) {
//                throw new TCException("Invalid OSPEC_NO.");
//            }
//
//            String ospecId = ospecNo.substring(0, idx);
//            String ospecRevId = ospecNo.substring(idx + 1);
//
//            return CustomUtil.findItemRevision(TypeConstant.S7_OSPECREVISIONTYPE, ospecId, ospecRevId);
//        } catch (Exception ex) {
//            throw ex;
//        }
//    }
//
//    private boolean startValidate() throws Exception {
//        try {
//            InterfaceAIFComponent[] targetComponents = AIFUtility.getCurrentApplication().getTargetComponents();
//
//            if (targetComponents == null || targetComponents.length != 1) {
//                MessageBox.post(AIFUtility.getActiveDesktop(), "조회할 Pre Product Part를 선택해 주세요.", "확인", MessageBox.INFORMATION);
//                return false;
//            }
//
//            for (InterfaceAIFComponent targetComponent : targetComponents) {
//                if ((! (targetComponent instanceof TCComponentBOMLine) && !(targetComponent.getType().equals(TypeConstant.S7_PREPRODUCTREVISIONTYPE))
//                        && !(targetComponent.getType().equals(TypeConstant.S7_PREPRODUCTTYPE))) || 
//                        ((targetComponent instanceof TCComponentBOMLine) && !(((TCComponentBOMLine) targetComponent).getItemRevision().getType().equals(TypeConstant.S7_PREPRODUCTREVISIONTYPE)))) {
//                    MessageBox.post(AIFUtility.getActiveDesktop(), "조회할 Pre Product Part를 선택해 주세요.", "확인", MessageBox.INFORMATION);
//                    return false;
//                }
//            }
//
//            if (targetComponents[0].getType().equals(TypeConstant.S7_PREPRODUCTREVISIONTYPE))
//                targetPreProduct = ((TCComponentItemRevision) targetComponents[0]).getItem().getLatestItemRevision();
//            else
//                if (targetComponents[0] instanceof TCComponentBOMLine)
//                    targetPreProduct = ((TCComponentBOMLine) targetComponents[0]).getItem().getLatestItemRevision();
//                else
//                    targetPreProduct = ((TCComponentItem) targetComponents[0]).getLatestItemRevision();
//
//            return true;
//        } catch (Exception ex) {
//            throw ex;
//        }
//    }
}
