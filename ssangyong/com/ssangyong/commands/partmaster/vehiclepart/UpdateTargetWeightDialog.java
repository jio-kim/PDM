package com.ssangyong.commands.partmaster.vehiclepart;

import java.text.DecimalFormat;

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

import com.ssangyong.common.SYMCLabel;
import com.ssangyong.common.SYMCText;
import com.ssangyong.common.dialog.SYMCAbstractDialog;
import com.ssangyong.common.utils.SYMTcUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.Registry;

/**
 * Target Weight Update Dialog
 * 
 * Release 후에 수정 하므로 별도 Object로 관리
 * E-BOM 개선 과제 Target Weight 속성 추가
 */
public class UpdateTargetWeightDialog extends SYMCAbstractDialog {

	/** TC Registry */
	private Registry registry;
	/** Actual Weight Field */
	private SYMCText targetWeightText;
	/** Target Revision */
	TCComponentItemRevision targetRevision;

	public UpdateTargetWeightDialog(Shell paramShell) {
		super(paramShell);
		this.registry = Registry.getRegistry(this);

		super.setApplyButtonVisible(false);
		setParentDialogCompositeSize(new Point(300, 150));

	}

	/**
	 * 화면 초기화
	 */
	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
		parentScrolledComposite.setBackground(new Color(null, 255, 255, 255));
		setDialogTextAndImage(registry.getString("ActWeightUpdateDialog.TITLE"), registry.getImage("NewPartMasterDialogHeader.ICON"));

		FormLayout groupLayout = new FormLayout();
		groupLayout.marginTop = 20;
		groupLayout.marginBottom = 5;
		groupLayout.marginLeft = 5;
		groupLayout.marginRight = 20;

		Composite composite = new Composite(parentScrolledComposite, SWT.NONE);
		composite.setLayout(groupLayout);

		FormData labelFormData = new FormData(100, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(0);
		labelFormData.left = new FormAttachment(0);

		SYMCLabel actWeightLabel = new SYMCLabel(composite, "Target. Weight", labelFormData);
		FormData formData = new FormData(93, SWT.DEFAULT);
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(actWeightLabel, 5);

		targetWeightText = new SYMCText(composite, formData);
		labelFormData = new FormData();
		labelFormData.top = new FormAttachment(0);
		labelFormData.left = new FormAttachment(targetWeightText, 5);
		Label kg4label = new Label(composite, SWT.NONE);
		kg4label.setText("㎏");
		kg4label.setLayoutData(labelFormData);

		try {
			// Release 후에 수정 하므로 별도 Object로 관리
			InterfaceAIFComponent comp = AIFUtility.getCurrentApplication().getTargetComponent();
			if (comp != null && comp instanceof TCComponentItemRevision) {
				targetRevision = (TCComponentItemRevision) comp;

				// Target. Weight(kg)
				TCComponent refComp = targetRevision.getReferenceProperty("s7_Vehpart_TypedReference");
				targetWeightText.setText(this.getFormatedString(refComp.getDoubleProperty("s7_TARGET_WEIGHT"), "########.####"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return composite;
	}

	@Override
	protected boolean apply() {
		try {
			TCComponent refComp = targetRevision.getReferenceProperty("s7_Vehpart_TypedReference");
			// Reference Object가 존재하는 경우 Update
			if (refComp != null) {
				refComp.setProperty("s7_TARGET_WEIGHT", targetWeightText.getText());
			}
			// Reference Object가 존재하지 않는 경우 생성
			else {
				refComp = SYMTcUtil.createApplicationObject(targetRevision.getSession(), "S7_Vehpart_TypedReference", new String[] { "s7_TARGET_WEIGHT", "s7_BOUNDINGBOX" }, new String[] { targetWeightText.getText(), "" });
				targetRevision.setReferenceProperty("s7_Vehpart_TypedReference", refComp);
			}

			targetRevision.refresh();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override
	protected boolean validationCheck() {
		return true;
	}

	public String getFormatedString(double value, String format) {
		DecimalFormat df = new DecimalFormat(format);
		return df.format(value);
	}

}
