package com.ssangyong.commands.ec.ecostatus.ui;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wb.swt.SWTResourceManager;

import ca.odell.glazedlists.EventList;

import com.ssangyong.commands.ec.ecostatus.model.EcoChangeData;
import com.teamcenter.rac.util.MessageBox;

public class MultiInputValueDialog extends Dialog {

	private CCombo comboEcoPublish;

	private EventList<EcoChangeData> tableDataList = null;
	private ArrayList<String> ecoPublishList = null;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public MultiInputValueDialog(Shell parentShell, EventList<EcoChangeData> tableDataList, ArrayList<String> ecoPublishList) {
		super(parentShell);
		setShellStyle(SWT.CLOSE);
		this.tableDataList = tableDataList;
		this.ecoPublishList = ecoPublishList;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("일괄속성 입력");
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
		sectionbBasic.setText("적용할 행을 체크하여 주십시오");

		Composite compositeBasic = toolkit.createComposite(sectionbBasic, SWT.NONE);
		compositeBasic.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		sectionbBasic.setClient(compositeBasic);
		GridLayout gl_compositeBasic = new GridLayout(4, false);
		gl_compositeBasic.marginHeight = 10;
		gl_compositeBasic.horizontalSpacing = 10;
		compositeBasic.setLayout(gl_compositeBasic);

		Label lblEcoPublish = new Label(compositeBasic, SWT.NONE);
		lblEcoPublish.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblEcoPublish, true, true);
		lblEcoPublish.setText("ECO 발행");

		comboEcoPublish = new CCombo(compositeBasic, SWT.BORDER);
		GridData gd_comboStageType = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboStageType.widthHint = 160;
		comboEcoPublish.setLayoutData(gd_comboStageType);
		toolkit.adapt(comboEcoPublish);
		toolkit.paintBordersFor(comboEcoPublish);
		new Label(compositeBasic, SWT.NONE);
		new Label(compositeBasic, SWT.NONE);
		initDataLoad();
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
		Button button = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		createButton(parent, IDialogConstants.CANCEL_ID, "Close", false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(300, 200);
	}

	private void initDataLoad() {
		for (String ecoPublishValue : ecoPublishList) {
			comboEcoPublish.add(ecoPublishValue);
			comboEcoPublish.setData(ecoPublishValue);
		}
	}

	@Override
	protected void okPressed() {
		try {
			ArrayList<EcoChangeData> checkedDataList = new ArrayList<EcoChangeData>();
			for (EcoChangeData data : tableDataList) {
				boolean isChecked = data.isRowCheck();
				if (!isChecked)
					continue;
				checkedDataList.add(data);
			}

			if (checkedDataList.size() == 0) {
				MessageBox.post(this.getShell(), "적용할 행을 체크하여 주십시오", "주의", MessageBox.WARNING);
				return;
			}

			if (comboEcoPublish.getText().isEmpty()) {
				MessageBox.post(this.getShell(), "ECO 발행을 선택하여 주십시오", "주의", MessageBox.WARNING);
				return;
			}
			
			for (EcoChangeData data : checkedDataList) {
				data.setEcoPublish(comboEcoPublish.getText());
			}

			super.okPressed();
		} catch (Exception ex) {
			MessageBox.post(this.getShell(), ex.toString(), "Error", MessageBox.ERROR);
		}
	}

}
