package com.kgm.commands.workflow.dotask;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.kgm.common.SYMCAWTTitledBorder;
import com.kgm.common.SYMCClass;
import com.teamcenter.rac.kernel.TCAttachmentScope;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.UIUtilities;
import com.teamcenter.rac.util.Utilities;
import com.teamcenter.rac.util.VerticalLayout;
import com.teamcenter.rac.util.iButton;

/**
 * ECR Workflow ���� "������ ����"dotask �� ��� ���� ����� ����/�Ұ� ���� �ϳ��� �����ϰ�, dotask�� �Ϸ� �Ͽ��� �Ѵ�.
 * ���� dotask �Ϸ��� ����/�Ұ� �� ����� ���̾�αװ� �ʿ���. 
 * @Copyright : S-PALM
 * @author   : ������
 * @since    : 2012. 6. 18.
 * Package ID : com.pungkang.commands.workflow.dotask.EcrReviewResultConfirmDialog.java
 */
public class EcrReviewResultConfirmDialog extends JDialog{

	private static final long serialVersionUID = 1L;

	private TCComponentTask task;

	private ButtonGroup bg;

	private JRadioButton apply;

	private JRadioButton nonApply;

	private iButton okButton;

	private Registry registry = Registry.getRegistry(this);

	private TCPreferenceService service;

	private String[] IS_APPLY = null;

	public EcrReviewResultConfirmDialog(TCComponentTask task){
		super(Utilities.getCurrentFrame(), true);
		setResizable(false);
		setTitle("ECR ���� ���");
		setAlwaysOnTop(true);
		this.task = task;
		service = task.getSession().getPreferenceService();
		//IS_APPLY = service.getStringArray(TCPreferenceService.TC_preference_site, SYMCClass.ECR_REVIEW_RESULT_LOV);
		IS_APPLY = service.getStringValuesAtLocation(SYMCClass.ECR_REVIEW_RESULT_LOV, TCPreferenceLocation.convertLocationFromLegacy(TCPreferenceService.TC_preference_site));

		Container container = getContentPane();
		container.setBackground(Color.WHITE);
		container.setLayout(new VerticalLayout(5,5,5,5,5));

		JPanel header = UIUtilities.createGradientHeader("���躯���û(ECR) Ÿ�缺 ���� ����� \n����/�Ұ� �� �ϳ��� ���� �� �ּ���.", registry.getImageIcon("Information.ICON"), 1);

		JPanel topPanel = new JPanel(new HorizontalLayout(5,30,30,5,5));
		topPanel.setOpaque(false);
		topPanel.setBorder(new SYMCAWTTitledBorder("ECR ���� ���"));

		apply = new JRadioButton("����", true);
		nonApply = new JRadioButton("�Ұ�", false);
		apply.setOpaque(false);
		nonApply.setOpaque(false);

		bg = new ButtonGroup();
		bg.add(apply);
		bg.add(nonApply);

		topPanel.add("left.bind", apply);
		topPanel.add("right.bind", nonApply);


		JPanel bottomPanel = new JPanel(new HorizontalLayout(5,5,5,5,5));
		bottomPanel.setOpaque(false);

		okButton = new iButton("Ȯ��", registry.getImageIcon("OK_16.ICON"));

		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				okButtonClicked();
			}
		});

		bottomPanel.add("right.bind", okButton);

		container.add("top.bind", header);
		container.add("unbound.bind", topPanel);
		container.add("bottom.bind", bottomPanel);

		setPreferredSize(new Dimension(270,250));
		pack();
		validate();
		UIUtilities.centerToScreen(this);
		setVisible(true);
	}

	private void okButtonClicked(){

		String confirmMessage = "���翡 ÷�εǾ��ִ� ECR�� �������� \"����\"���� ���� �Ͽ����ϴ�.";

		if(nonApply.isSelected()){
			confirmMessage = "���翡 ÷�εǾ��ִ� ECR�� �������� \"�Ұ�\"�� ���� �Ͽ����ϴ�.";
		}

		int showOK = JOptionPane.showConfirmDialog(null, confirmMessage +  "\n���� �Ͻ÷��� ��(Y) ��ư�� ��������.", "Create...", JOptionPane.YES_NO_OPTION);
		if(showOK == JOptionPane.OK_OPTION){
			setProperty();
		}
	}

	@SuppressWarnings("rawtypes")
    private void setProperty(){
		try {
			Vector allAttachmentsVector = task.getAllAttachments(TCAttachmentScope.GLOBAL);

			if(allAttachmentsVector == null || allAttachmentsVector.size() == 0){
				return;
			}

			for(int i=0; i<allAttachmentsVector.size(); i++){
				if(allAttachmentsVector.get(i) instanceof TCComponentItem){
					TCComponentItem ecr = (TCComponentItem)allAttachmentsVector.get(i);
					if(ecr.toDisplayString().startsWith("ECR")){
						if(ecr.getProperty("pk4_review_result") == null || ecr.getProperty("pk4_review_result").equals("")){
							if(apply.isSelected()){
								ecr.setProperty("pk4_review_result", IS_APPLY[0]);
							}
							else{
								ecr.setProperty("pk4_review_result", IS_APPLY[1]);
							}
						}
					}
				}
			}
		}catch (TCException e) {
			e.printStackTrace();
		}
		setVisible(false);
		dispose();
	}
}
