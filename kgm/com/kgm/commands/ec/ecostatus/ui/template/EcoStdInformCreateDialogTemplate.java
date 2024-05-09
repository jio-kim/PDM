package com.kgm.commands.ec.ecostatus.ui.template;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;

/**
 * 기준정보 등록
 * 
 * @author baek
 * 
 */
public class EcoStdInformCreateDialogTemplate extends Dialog {
	private Text textOspecId;
	private Text textEstChangePeriod;
	private Text textCreateDate;
	private Text textChangeDesc;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public EcoStdInformCreateDialogTemplate(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.PRIMARY_MODAL);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("기준정보 등록");
	}


	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		Composite container = (Composite) super.createDialogArea(parent);

		container.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		container.setBackgroundMode(SWT.INHERIT_FORCE);

		GridLayout gl_container = new GridLayout(1, false);
		gl_container.marginWidth = 0;
		gl_container.marginHeight = 0;
		gl_container.verticalSpacing = 0;
		container.setLayout(gl_container);
		
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());

		Section sectionbBasic = toolkit.createSection(container, Section.TITLE_BAR);
		GridData gd_sectionbBasic = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_sectionbBasic.minimumWidth = 0;
		sectionbBasic.setLayoutData(gd_sectionbBasic);
		sectionbBasic.setText("기준정보 ");


		Composite compositeBasic = toolkit.createComposite(sectionbBasic, SWT.NONE);
		compositeBasic.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		sectionbBasic.setClient(compositeBasic);
		GridLayout gl_compositeBasic = new GridLayout(4, false);
		gl_compositeBasic.marginHeight = 10;
		gl_compositeBasic.horizontalSpacing = 10;
		compositeBasic.setLayout(gl_compositeBasic);
		
		Label lblStageType = new Label(compositeBasic, SWT.NONE);
		lblStageType.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblStageType, true, true);
		lblStageType.setText("\uBD84\uB958");
		
		CCombo comboStageType = new CCombo(compositeBasic, SWT.BORDER);
		GridData gd_comboStageType = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboStageType.widthHint = 160;
		comboStageType.setLayoutData(gd_comboStageType);
		toolkit.adapt(comboStageType);
		toolkit.paintBordersFor(comboStageType);
		
		Label lblGModel = new Label(compositeBasic, SWT.NONE);
		lblGModel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblGModel, true, true);
		lblGModel.setText("G-Model");
		
		CCombo comboGmodel = new CCombo(compositeBasic, SWT.BORDER);
		GridData gd_comboGmodel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboGmodel.widthHint = 160;
		comboGmodel.setLayoutData(gd_comboGmodel);
		toolkit.adapt(comboGmodel);
		toolkit.paintBordersFor(comboGmodel);
		
		Label lblProject = new Label(compositeBasic, SWT.NONE);
		lblProject.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblProject, true, true);
		lblProject.setText("Project");
		
		CCombo comboProject = new CCombo(compositeBasic, SWT.BORDER);
		GridData gd_comboProject = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboProject.widthHint = 160;
		comboProject.setLayoutData(gd_comboProject);
		toolkit.adapt(comboProject);
		toolkit.paintBordersFor(comboProject);
		
		Label lblStatus = new Label(compositeBasic, SWT.NONE);
		lblStatus.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblStatus, true, true);
		lblStatus.setText("\uC0C1\uD0DC");
		
		CCombo comboStatus = new CCombo(compositeBasic, SWT.BORDER);
		GridData gd_comboStatus = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboStatus.widthHint = 160;
		comboStatus.setLayoutData(gd_comboStatus);
		toolkit.adapt(comboStatus);
		toolkit.paintBordersFor(comboStatus);
		
		Composite compositeOspec = new Composite(compositeBasic, SWT.NONE);
		GridLayout gl_compositeOspec = new GridLayout(3, false);
		gl_compositeOspec.marginLeft = 18;
		gl_compositeOspec.marginHeight = 0;
		gl_compositeOspec.marginWidth = 0;
		compositeOspec.setLayout(gl_compositeOspec);
		compositeOspec.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		toolkit.adapt(compositeOspec);
		toolkit.paintBordersFor(compositeOspec);
		
		Label lblOspecId = new Label(compositeOspec, SWT.NONE);
		lblOspecId.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblOspecId, true, true);
		lblOspecId.setText("\uAD6C\uBD84(O/SPEC)");
		
		textOspecId = new Text(compositeOspec, SWT.BORDER);
		GridData gd_textOspecId = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_textOspecId.widthHint = 407;
		textOspecId.setLayoutData(gd_textOspecId);
		toolkit.adapt(textOspecId, true, true);
		
		Button btnSearchOspec = new Button(compositeOspec, SWT.NONE);
		btnSearchOspec.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(btnSearchOspec, true, true);
		btnSearchOspec.setImage(SWTResourceManager.getImage(EcoStdInformCreateDialogTemplate.class, "/icons/search_16.png"));
		
		Label lblEstApplyDate = new Label(compositeBasic, SWT.NONE);
		lblEstApplyDate.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblEstApplyDate, true, true);
		lblEstApplyDate.setText("\uC608\uC0C1 \uC801\uC6A9\uC2DC\uC810");
		
		Label lblNewLabel = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblNewLabel, true, true);
		lblNewLabel.setText("New Label");
		
		Label lblReceiptDate = new Label(compositeBasic, SWT.NONE);
		lblReceiptDate.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblReceiptDate, true, true);
		lblReceiptDate.setText("O/SEC \uC811\uC218\uC77C");
		
		Label lblNewLabel_1 = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblNewLabel_1, true, true);
		lblNewLabel_1.setText("New Label");
		
		Label lblEcoCompleteReqDate = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblEcoCompleteReqDate, true, true);
		lblEcoCompleteReqDate.setText("ECO \uC644\uB8CC\uC694\uCCAD\uC77C");
		
		Label lblNewLabel_2 = new Label(compositeBasic, SWT.NONE);
		toolkit.adapt(lblNewLabel_2, true, true);
		lblNewLabel_2.setText("New Label");
		
		Label lblEstChangePeriod = new Label(compositeBasic, SWT.NONE);
		lblEstChangePeriod.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblEstChangePeriod, true, true);
		lblEstChangePeriod.setText("\uC608\uC0C1 \uC124\uACC4\uBCC0\uACBD\uAE30\uAC04");
		
		textEstChangePeriod = new Text(compositeBasic, SWT.BORDER);
		textEstChangePeriod.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(textEstChangePeriod, true, true);
		
		Label lblChangeDesc = new Label(compositeBasic, SWT.NONE);
		lblChangeDesc.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 2));
		toolkit.adapt(lblChangeDesc, true, true);
		lblChangeDesc.setText("\uBCC0\uACBD\uB0B4\uC6A9");
		
		textChangeDesc = new Text(compositeBasic, SWT.BORDER);
		textChangeDesc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 2));
		toolkit.adapt(textChangeDesc, true, true);
		
		Label lblCreateDate = new Label(compositeBasic, SWT.NONE);
		lblCreateDate.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblCreateDate, true, true);
		lblCreateDate.setText("\uB4F1\uB85D\uC77C");
		
		textCreateDate = new Text(compositeBasic, SWT.BORDER);
		textCreateDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(textCreateDate, true, true);
		new Label(compositeBasic, SWT.NONE);
		new Label(compositeBasic, SWT.NONE);



		return container;
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		createButton(parent, IDialogConstants.OK_ID, "Save", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(591, 300);
	}
}
