package com.kgm.commands.commonpartcheck;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.Separator;
import com.teamcenter.rac.util.VerticalLayout;

public class CommonPartCheckDialog extends AbstractAIFDialog {

	private static final long serialVersionUID = -4408474910899088180L;

	private ECOSearchPanel ecoSearchPanel;
	private ExceptionPanel exceptionPanel;
	private VerificationPanel verificationPanel;
	private ErrorReportPanel errorReportPanel;

	/**
	 * 
	 * @copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2018. 4. 6.
	 * @throws Exception
	 */
	public CommonPartCheckDialog() throws Exception {
		super(AIFUtility.getActiveDesktop().getFrame(), false);
		initUI();
	}

	/**
	 * UI 그리기 시작
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2018. 4. 6.
	 */
	private void initUI(){
		setTitle("Common Part Check");
		
		getContentPane().setLayout(new VerticalLayout(5,5,5,5,5));
		getContentPane().add("unbound.bind.center.center", createTabPanel());
		getContentPane().add("bottom.bind.center.center", createButtonPanel());
		getContentPane().add("bottom.bind.center.center", new Separator());
		
		setPreferredSize(new Dimension(1200,700));
	}

	/**
	 * TabPanel 생성
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2018. 4. 6.
	 * @return
	 */
	private JTabbedPane createTabPanel(){
		JTabbedPane tabbedPane = new JTabbedPane();

		tabbedPane.addTab("ECO Search", createECOSearchPanel());
		tabbedPane.addTab("Exception", createExceptionPanel());
		tabbedPane.addTab("Verification", createVerificationPanel());
		tabbedPane.addTab("Error Report", createErrorReportPanel());
		
		 ChangeListener changeListener = new ChangeListener() {
			 
			 public void stateChanged(ChangeEvent changeEvent) {
				 
				 JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
				 int index = sourceTabbedPane.getSelectedIndex();
				 System.out.println(index);
				 Component component = sourceTabbedPane.getComponentAt(index);
				 if( component instanceof ExceptionPanel ){
					 
					 ExceptionPanel exceptionPanel = (ExceptionPanel) component;
					 exceptionPanel.search();
					 
				 }
				 
			 }
		 };
		 tabbedPane.addChangeListener(changeListener);
		
		return tabbedPane;
	}

	/**
	 * TabPanel의 첫번째 패널
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2018. 4. 6.
	 * @return
	 */
	private JPanel createECOSearchPanel(){
		ECOSearchPanel panel = new ECOSearchPanel(this);

		return panel;
	}

	/**
	 * TabPanel의 두번째 패널
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2018. 4. 6.
	 * @return
	 */
	private JPanel createExceptionPanel(){
		ExceptionPanel panel = new ExceptionPanel(this);
		
		return panel;
	}

	/**
	 * TabPanel의 세번째 패널
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2018. 4. 6.
	 * @return
	 */
	private JPanel createVerificationPanel(){
		VerificationPanel panel = new VerificationPanel(this);

		return panel;
	}

	/**
	 * TabPanel의 네번째 패널
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2018. 4. 6.
	 * @return
	 */
	private JPanel createErrorReportPanel(){
		ErrorReportPanel panel = new ErrorReportPanel(this);

		return panel;
	}

	/**
	 * 하단 Close 버튼 패널
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2018. 4. 6.
	 * @return
	 */
	private JPanel createButtonPanel(){
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton cancelButton = new JButton("Close");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				dispose();
			}
		});
		buttonPane.add(cancelButton);
		
		return buttonPane;
	}
}