package com.symc.plm.rac.prebom.prebom.dialog.updateweight;

import java.text.DecimalFormat;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.SYMCLabel;
import com.kgm.common.SYMCText;
import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.utils.ProgressBar;
import com.kgm.common.utils.SYMTcUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.common.util.SDVPreBOMUtilities;
import com.symc.plm.rac.prebom.prebom.operation.updateprevehpart.UpdatePreVehPartPropertyOperation;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Estimate Weight Update Dialog
 * [20180213][ljg] 시스템 코드 리비전 정보에서 bomline정보로 이동
 */
public class UpdateEstWeightDialog extends SYMCAbstractDialog {
    private Composite mainComposite;
	/** TC Registry */
	private Registry registry;
	/** Estimate Weight Field */
	private SYMCText estWeightText;
	private SYMCText targetRevisionText;
	private SYMCText updateReasonText;
	/** Target Revision */
	TCComponentItemRevision targetRevision;
	TCComponentBOMLine targetBOMLine;
	
	public UpdateEstWeightDialog(Shell paramShell, TCComponentBOMLine targetBOMLine) {
		super(paramShell);
		this.registry = Registry.getRegistry(this);
		try {
			this.targetBOMLine = targetBOMLine;
			this.targetRevision = targetBOMLine.getItem().getLatestItemRevision();
		} catch (TCException e) {
			e.printStackTrace();
		}

		super.setApplyButtonVisible(false);
		setParentDialogCompositeSize(new Point(300, 250));
	}

	private void setInitData() {
	    AbstractAIFOperation op = new AbstractAIFOperation() {
	        @Override
	        public void executeOperation() throws Exception {
	            mainComposite.getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (targetRevision == null)
                                    return;
                                
                                // Act. Weight(kg)
                                targetRevisionText.setText(targetRevision.toString());
                                targetRevisionText.setData(targetRevision);
                            estWeightText.setText(getFormatedString(targetRevision.getDoubleProperty(PropertyConstant.ATTR_NAME_ESTWEIGHT), "########.####"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
	        }
	    };
	    if (targetRevision != null)
	    {
	        targetRevision.getSession().queueOperation(op);
	    }
    }

    /**
	 * 화면 초기화
	 */
	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
		parentScrolledComposite.setBackground(new Color(null, 255, 255, 255));
		setDialogTextAndImage(registry.getString("EstWeightUpdateDialog.TITLE"), registry.getImage("NewPartMasterDialogHeader.ICON"));

		FormLayout groupLayout = new FormLayout();
		groupLayout.marginTop = 20;
		groupLayout.marginBottom = 5;
		groupLayout.marginLeft = 5;
		groupLayout.marginRight = 20;

		mainComposite = new Composite(parentScrolledComposite, SWT.NONE);
		mainComposite.setLayout(groupLayout);

		FormData labelFormData = new FormData(93, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(0);
		labelFormData.left = new FormAttachment(0);

        SYMCLabel targetRevisionLabel = new SYMCLabel(mainComposite, "Target Revision", labelFormData);
        FormData formData = new FormData(200, SWT.DEFAULT);
        formData.top = new FormAttachment(0);
        formData.left = new FormAttachment(targetRevisionLabel, 5);

        targetRevisionText = new SYMCText(mainComposite, formData);
        targetRevisionText.setEditable(false);
//        targetRevisionText.setEnabled(false);
        targetRevisionText.setBackground(mainComposite.getDisplay().getSystemColor(SWT.COLOR_GRAY));
        labelFormData = new FormData(98, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(targetRevisionLabel, 10);
        labelFormData.left = new FormAttachment(0);

		SYMCLabel estWeightLabel = new SYMCLabel(mainComposite, "Est. Weight ", labelFormData);
		formData = new FormData(93, SWT.DEFAULT);
		formData.top = new FormAttachment(targetRevisionText, 10);
		formData.left = new FormAttachment(estWeightLabel);

		estWeightText = new SYMCText(mainComposite, formData);
		estWeightText.setMandatory(true);
		labelFormData = new FormData();
		labelFormData.top = new FormAttachment(targetRevisionText, 10);
		labelFormData.left = new FormAttachment(estWeightText, 5);
		Label kg4label = new Label(mainComposite, SWT.NONE);
		kg4label.setText("㎏");
		kg4label.setLayoutData(labelFormData);

        labelFormData = new FormData(98, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(estWeightLabel, 10);
        labelFormData.left = new FormAttachment(0);

        SYMCLabel updateReasonLabel = new SYMCLabel(mainComposite, "Update Reason ", labelFormData);
        formData = new FormData(186, 100);
        formData.top = new FormAttachment(estWeightText, 10);
        formData.left = new FormAttachment(updateReasonLabel);

        updateReasonText = new SYMCText(mainComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        updateReasonText.setMandatory(true);
        updateReasonText.setAlwaysShowScrollBars(false);
//        GridData gd_textUpdateReason = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 0);
//        gd_textUpdateReason.heightHint = 50;
        updateReasonText.setLayoutData(formData);

		setInitData();

		return mainComposite;
	}

	@Override
	protected boolean apply() {
		try {
		    String estWeightString = estWeightText.getText();
		    if (estWeightString == null || estWeightString.trim().length() == 0)
		    {
		        mainComposite.getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        MessageBox.post(mainComposite.getShell(), "Est. Weight 값은 필수 입력항목 입니다.", "확인", MessageBox.INFORMATION);
                    }
                });

		        return false;
		    }
		    double originalWeight = targetRevision.getDoubleProperty(PropertyConstant.ATTR_NAME_ESTWEIGHT);
		    if (originalWeight == Double.valueOf(estWeightString))
		    {
		        mainComposite.getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        MessageBox.post(mainComposite.getShell(), "Est. Weight 값이 변경전과 동일합니다.", "확인", MessageBox.INFORMATION);
                    }
                });

		        return false;
		    }
		    String updateReason = updateReasonText.getText();
		    if (updateReason == null || updateReason.trim().length() == 0)
		    {
		        mainComposite.getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        MessageBox.post(mainComposite.getShell(), "변경사유는 필수 입력항목입니다.", "확인", MessageBox.INFORMATION);
                    }
                });

                return false;
		    }

		    HashMap<String, Object> propMap = new HashMap<String, Object>();
		    propMap.put(PropertyConstant.ATTR_NAME_ESTWEIGHT, Double.valueOf(estWeightString));
		    propMap.put(PropertyConstant.ATTR_NAME_ITEMDESC, updateReason);

		    HashMap<String, Object> ccnPropMap = new HashMap<String, Object>();
		    ccnPropMap.put(PropertyConstant.ATTR_NAME_PROJCODE, targetRevision.getProperty(PropertyConstant.ATTR_NAME_PROJCODE));
		    ccnPropMap.put(PropertyConstant.ATTR_NAME_GATENO, getGateNo(targetRevision.getProperty(PropertyConstant.ATTR_NAME_PROJCODE)));
		    ccnPropMap.put(PropertyConstant.ATTR_NAME_BL_BUDGETCODE, targetBOMLine.getProperty(PropertyConstant.ATTR_NAME_BL_BUDGETCODE));
		    ccnPropMap.put(PropertyConstant.ATTR_NAME_PROJECTTYPE, "02");
            ccnPropMap.put(PropertyConstant.ATTR_NAME_REGULATION, false);
            ccnPropMap.put(PropertyConstant.ATTR_NAME_COSTDOWN, false);
            ccnPropMap.put(PropertyConstant.ATTR_NAME_ORDERINGSPEC, false);
            ccnPropMap.put(PropertyConstant.ATTR_NAME_QUALITYIMPROVEMENT, false);
            ccnPropMap.put(PropertyConstant.ATTR_NAME_CORRECTIONOFEPL, false);
            ccnPropMap.put(PropertyConstant.ATTR_NAME_STYLINGUPDATE, false);
            ccnPropMap.put(PropertyConstant.ATTR_NAME_WEIGHTCHANGE, true);
            ccnPropMap.put(PropertyConstant.ATTR_NAME_MATERIALCOSTCHANGE, false);
            ccnPropMap.put(PropertyConstant.ATTR_NAME_THEOTHERS, false);
            ccnPropMap.put(PropertyConstant.ATTR_NAME_ITEMDESC, updateReason);

		    final ProgressBar progressBar = new ProgressBar(mainComposite.getShell());
		    progressBar.start();

		    final UpdatePreVehPartPropertyOperation updateOperation = new UpdatePreVehPartPropertyOperation(targetRevision, propMap, ccnPropMap);
		    // 여기서 Operation이 끝나기를 기다릴지에 대한 코딩 필요.
		    updateOperation.addOperationListener(new InterfaceAIFOperationListener() {
                @Override
                public void startOperation(String paramString) {
                }
                
                @Override
                public void endOperation() {
                    mainComposite.getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.close();

                            if (! updateOperation.getOperationResult().equals("Success"))
                            {
                                mainComposite.getDisplay().asyncExec(new Runnable() {
                                    @Override
                                    public void run() {
                                        MessageBox.post(mainComposite.getShell(), updateOperation.getErrorMessage(), "확인", MessageBox.INFORMATION);
                                    }
                                });
                            }
                        }
                    });
                }
            });
		    targetRevision.getSession().queueOperationAndWait(updateOperation);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

    private String getGateNo(String projectId) throws Exception {
        TCComponent[] tcComponents = SDVPreBOMUtilities.queryComponent("__SYMC_S7_PreProductRevision", new String[]{"Project Code"}, new String[]{projectId});
        if (null != tcComponents && tcComponents.length > 0) {
            TCComponentItemRevision productRevision = null;
            for (TCComponent tcComponent : tcComponents) {
                if (tcComponent.getType().equals(TypeConstant.S7_PREPRODUCTREVISIONTYPE)) {
                    productRevision = SYMTcUtil.getLatestReleasedRevision(((TCComponentItemRevision)tcComponent).getItem());
                    break;
                }
            }
            String sGateNo = productRevision.getProperty(PropertyConstant.ATTR_NAME_GATENO);
            
            return sGateNo;
        }
        return null;
    }

	@Override
	protected boolean validationCheck() {
		return true;
	}

	protected String getFormatedString(double value, String format) {
		DecimalFormat df = new DecimalFormat(format);
		return df.format(value);
	}

}
