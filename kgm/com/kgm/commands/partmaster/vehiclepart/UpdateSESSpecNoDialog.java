package com.kgm.commands.partmaster.vehiclepart;

import java.text.DecimalFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.SYMCClass;
import com.kgm.common.SYMCLabel;
import com.kgm.common.SYMCText;
import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.utils.SYMTcUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * [SR140324-030][20140619] KOG DEV SES Spec No Update Dialog
 * 
 * Release 후에 수정 하므로 별도 Object로 관리
 */
public class UpdateSESSpecNoDialog extends SYMCAbstractDialog {

    /** TC Registry */
    private Registry registry;
    /** SES Spec No Field */
    private SYMCText sesSpecNoText;
    /** Target Revision */
    TCComponentItemRevision targetRevision;
    /** SES Spec No. Property Name */
    private final String SES_SPEC_NO = "s7_SES_SPEC_NO";
    private String strTypedRefPropertyName = "";
    private String strTypedReferenceName = "";
    private String STR_DIALOG_TITLE;

    public UpdateSESSpecNoDialog(Shell paramShell) {
        super(paramShell);
        this.registry = Registry.getRegistry(this);
        super.setApplyButtonVisible(false);
        setParentDialogCompositeSize(new Point(450, 170));
    }

    /**
     * 화면 초기화
     */
    @Override
    protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
        parentScrolledComposite.setBackground(new Color(null, 255, 255, 255));

        FormLayout groupLayout = new FormLayout();
        groupLayout.marginTop = 20;
        groupLayout.marginBottom = 5;
        groupLayout.marginLeft = 5;
        groupLayout.marginRight = 20;

        Composite composite = new Composite(parentScrolledComposite, SWT.NONE);
        composite.setLayout(groupLayout);

        FormData labelFormData = new FormData(80, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(0);
        labelFormData.left = new FormAttachment(0);

        SYMCLabel actWeightLabel = new SYMCLabel(composite, "SES Spec No.", labelFormData);
        FormData formData = new FormData(347, 77);
        formData.top = new FormAttachment(0);
        formData.left = new FormAttachment(actWeightLabel, 5);

        sesSpecNoText = new SYMCText(composite, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI, formData);
        labelFormData = new FormData();
        labelFormData.top = new FormAttachment(0);
        labelFormData.left = new FormAttachment(sesSpecNoText, 20);
        sesSpecNoText.addVerifyKeyListener(new VerifyKeyListener() {

            @Override
            public void verifyKey(VerifyEvent event) {
                char ch = event.character;
                if (ch == SWT.CR) {
                    event.doit = false;
                    return;
                }
            }
        });

        try {
            // Release 후에 수정 하므로 별도 Object로 관리
            InterfaceAIFComponent comp = AIFUtility.getCurrentApplication().getTargetComponent();
            if (comp != null && comp instanceof TCComponentItemRevision) {
                targetRevision = (TCComponentItemRevision) comp;
                // Update SES Spec No. Dialog, SES Spec No. 값 Text Field에 세팅.
                if (targetRevision.getType().equals(SYMCClass.S7_VEHPARTREVISIONTYPE)) {
                    strTypedReferenceName = "S7_Vehpart_TypedReference";
                    strTypedRefPropertyName = "s7_Vehpart_TypedReference";
                    STR_DIALOG_TITLE = "Vehicle PartMaster Creation Dialog";
                } else if (targetRevision.getType().equals(SYMCClass.S7_STDPARTREVISIONTYPE)) {
                    strTypedReferenceName = "S7_Stdpart_TypedReference";
                    strTypedRefPropertyName = "s7_Stdpart_TypedReference";
                    STR_DIALOG_TITLE = "Standard PartMaster Creation Dialog";
                }
                TCComponent sesSpecNoComp = targetRevision.getReferenceProperty(strTypedRefPropertyName);
                if (sesSpecNoComp != null) {
                    sesSpecNoText.setText(sesSpecNoComp.getStringProperty(SES_SPEC_NO));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        setDialogTextAndImage(STR_DIALOG_TITLE, registry.getImage("NewPartMasterDialogHeader.ICON"));

        return composite;
    }

    @Override
    protected boolean apply() {
        try {
            TCComponent refComp = targetRevision.getReferenceProperty(strTypedRefPropertyName);
            // Reference Object가 존재하는 경우 Update
            if (refComp != null) {
                refComp.setProperty(SES_SPEC_NO, sesSpecNoText.getText());
            }
            // Reference Object가 존재하지 않는 경우 생성
            else {
                refComp = SYMTcUtil.createApplicationObject(targetRevision.getSession(), strTypedReferenceName, new String[] { SES_SPEC_NO }, new String[] { sesSpecNoText.getText() });
                if (targetRevision.getType().equals(SYMCClass.S7_VEHPARTREVISIONTYPE)) {
                    refComp.setStringProperty("s7_BOUNDINGBOX", "");
                }
                targetRevision.setReferenceProperty(strTypedRefPropertyName, refComp);
            }
            targetRevision.clearCache();
            targetRevision.refresh();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected boolean validationCheck() {
        String strSESSpecno = sesSpecNoText.getText();
        if (strSESSpecno.getBytes().length > 500) {
            MessageBox.post("SES Spec No. limited 500 bytes !!", "SES Spec No. Update Dialog", MessageBox.ERROR);
            return false;
        }
        return true;
    }

    public String getFormatedString(double value, String format) {
        DecimalFormat df = new DecimalFormat(format);//
        return df.format(value);
    }

}
