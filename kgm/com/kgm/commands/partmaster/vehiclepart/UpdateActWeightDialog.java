package com.kgm.commands.partmaster.vehiclepart;

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

import com.kgm.common.SYMCLabel;
import com.kgm.common.SYMCText;
import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.utils.SYMTcUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.Registry;

/**
 * Actual Weight Update Dialog
 * 
 * Release �Ŀ� ���� �ϹǷ� ���� Object�� ����
 */
public class UpdateActWeightDialog extends SYMCAbstractDialog {

	/** TC Registry */
	private Registry registry;
	/** Actual Weight Field */
	private SYMCText actWeightText;
	/** Target Revision */
	TCComponentItemRevision targetRevision;

	public UpdateActWeightDialog(Shell paramShell) {
		super(paramShell);
		this.registry = Registry.getRegistry(this);

		super.setApplyButtonVisible(false);
		setParentDialogCompositeSize(new Point(300, 150));

	}

	/**
	 * ȭ�� �ʱ�ȭ
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

		SYMCLabel actWeightLabel = new SYMCLabel(composite, "Act. Weight", labelFormData);
		FormData formData = new FormData(93, SWT.DEFAULT);
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(actWeightLabel, 5);

		actWeightText = new SYMCText(composite, formData);
		labelFormData = new FormData();
		labelFormData.top = new FormAttachment(0);
		labelFormData.left = new FormAttachment(actWeightText, 5);
		Label kg4label = new Label(composite, SWT.NONE);
		kg4label.setText("��");
		kg4label.setLayoutData(labelFormData);

		try {
			// Release �Ŀ� ���� �ϹǷ� ���� Object�� ����
			InterfaceAIFComponent comp = AIFUtility.getCurrentApplication().getTargetComponent();
			if (comp != null && comp instanceof TCComponentItemRevision) {
				targetRevision = (TCComponentItemRevision) comp;

				// Act. Weight(kg)
				actWeightText.setText(this.getFormatedString(targetRevision.getDoubleProperty("s7_ACT_WEIGHT"), "########.####"));
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
			// Reference Object�� �����ϴ� ��� Update
			if (refComp != null) {
				refComp.setProperty("s7_ACT_WEIGHT", actWeightText.getText());
			}
			// Reference Object�� �������� �ʴ� ��� ����
			else {
				refComp = SYMTcUtil.createApplicationObject(targetRevision.getSession(), "S7_Vehpart_TypedReference", new String[] { "s7_ACT_WEIGHT", "s7_BOUNDINGBOX" }, new String[] { actWeightText.getText(), "" });
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
