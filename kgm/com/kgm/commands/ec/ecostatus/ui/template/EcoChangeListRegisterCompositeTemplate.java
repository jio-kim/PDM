package com.kgm.commands.ec.ecostatus.ui.template;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Panel;

import javax.swing.JRootPane;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

public class EcoChangeListRegisterCompositeTemplate extends Composite {
	private Text textBefore;
	private Text textAfter;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public EcoChangeListRegisterCompositeTemplate(Composite parent, int style) {
		super(parent, style);
		initUI();
	}
	
	private void initUI() {
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		this.setLayout(gridLayout);
		FormToolkit toolkit = new FormToolkit(getParent().getDisplay());

		Section sectionbBasic = toolkit.createSection(this, Section.TITLE_BAR);
		GridData gd_sectionbBasic = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_sectionbBasic.widthHint = 829;
		gd_sectionbBasic.minimumWidth = 0;
		sectionbBasic.setLayoutData(gd_sectionbBasic);

		Composite compositeBasic = toolkit.createComposite(sectionbBasic, SWT.NONE);
		compositeBasic.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		sectionbBasic.setClient(compositeBasic);
		GridLayout gl_compositeBasic = new GridLayout(6, false);
		gl_compositeBasic.marginLeft = 30;
		compositeBasic.setLayout(gl_compositeBasic);
		
		Label lblAfter = new Label(compositeBasic, SWT.NONE);
		lblAfter.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblAfter.setText("변경전");
		
		textBefore = new Text(compositeBasic, SWT.BORDER);
		GridData gd_textBefore = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_textBefore.widthHint = 120;
		textBefore.setLayoutData(gd_textBefore);
		toolkit.adapt(textBefore, true, true);
		
		Label lblBlank = new Label(compositeBasic, SWT.NONE);
		GridData gd_lblBlank = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblBlank.widthHint = 30;
		lblBlank.setLayoutData(gd_lblBlank);
		toolkit.adapt(lblBlank, true, true);
		
		Label lblBefore = new Label(compositeBasic, SWT.NONE);
		lblBefore.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(lblBefore, true, true);
		lblBefore.setText("변경후");
		
		textAfter = new Text(compositeBasic, SWT.BORDER);
		GridData gd_textAfter = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_textAfter.widthHint = 120;
		textAfter.setLayoutData(gd_textAfter);
		toolkit.adapt(textAfter, true, true);
		
		Composite compositeTopButton = new Composite(compositeBasic, SWT.NONE);
		compositeTopButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		compositeTopButton.setLayout(new GridLayout(2, false));
		
		Button btnCompare = new Button(compositeTopButton, SWT.NONE);
		btnCompare.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});

		btnCompare.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(btnCompare, true, true);
		btnCompare.setText("O/Spec Compare");

		Button btnChangeInformInput = new Button(compositeTopButton, SWT.NONE);
		btnChangeInformInput.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		toolkit.adapt(btnChangeInformInput, true, true);
		btnChangeInformInput.setText("변경 정보 입력");
		
		Composite compositeCenter = new Composite(this, SWT.NONE);
		compositeCenter.setLayout(new FillLayout(SWT.HORIZONTAL));
		compositeCenter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		toolkit.adapt(compositeCenter);
		toolkit.paintBordersFor(compositeCenter);
		
		Composite composite = new Composite(compositeCenter, SWT.EMBEDDED);
		toolkit.adapt(composite);
		toolkit.paintBordersFor(composite);
		
		Frame frameEmbedded = SWT_AWT.new_Frame(composite);
		
		Panel panelTop = new Panel();
		frameEmbedded.add(panelTop);
		panelTop.setLayout(new BorderLayout(0, 0));
		
		JRootPane rootPane = new JRootPane();
		rootPane.getContentPane().setBackground(Color.WHITE);
		panelTop.add(rootPane);
		
	}
}
