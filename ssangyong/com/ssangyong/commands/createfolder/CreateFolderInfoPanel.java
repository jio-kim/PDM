package com.ssangyong.commands.createfolder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import com.ssangyong.common.FunctionField;
import com.ssangyong.common.SYMCAWTLabel;
import com.ssangyong.common.SYMCAWTTitledBorder;
import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.SYMCInterfaceInfoPanel;
import com.ssangyong.common.utils.ComponentService;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.PreferenceService;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.VerticalLayout;

public class CreateFolderInfoPanel extends JPanel implements SYMCInterfaceInfoPanel {

	private static final long serialVersionUID = 1L;
	private JDialog dialog;

	/** Folder Name textfield */
	private FunctionField folderNameTF = new FunctionField(13, true);
	/** Folder Desc textarea */
	private TextArea folderDescTA = new TextArea(5, 18);
	/** Model Copy checkbox */
	private JCheckBox modelCopyCK = new JCheckBox("Check Model Copy");
	/** Model ID textfield */
	private FunctionField modelIDTF = new FunctionField(13, false);

	private HashMap<String, String> folderTypeMap = new HashMap<String, String>();
	private String selRadioValue;

	/**
	 * 생성자.
	 * 
	 * @copyright : S-PALM
	 * @author : 권상기
	 * @param dialog
	 * @since : 2013. 1. 10.
	 */
	public CreateFolderInfoPanel(JDialog dialog) {
		super(new VerticalLayout(5, 5, 5, 5, 5));

		this.dialog = dialog;

		initUI();

		/** Action 처리 메소드 */
		actionListnelAdd();
	}

	/**
	 * UI Panel.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2013. 1. 10.
	 */
	private void initUI() {
		setOpaque(false);

		JPanel leftPanel = new JPanel(new PropertyLayout(5, 5));
		leftPanel.setOpaque(false);
		leftPanel.setBorder(new SYMCAWTTitledBorder("Folder Type"));

		JPanel rightPanel = new JPanel(new PropertyLayout(5, 5));
		rightPanel.setOpaque(false);
		rightPanel.setBorder(new SYMCAWTTitledBorder("Folder Property"));
		rightPanel.setPreferredSize(new Dimension(370, 210));

		JPanel mainPanel = new JPanel(new PropertyLayout(5, 5));
		mainPanel.setOpaque(false);

		/** Radio Button */
		String[] folderTypes = SYMCClass.CREATEFOLDERTYPE();
		ButtonGroup group = new ButtonGroup();

		JScrollPane scrollPane = new JScrollPane(leftPanel);
		scrollPane.getViewport().setBackground(Color.WHITE);
		scrollPane.setPreferredSize(new Dimension(150, 210));
		scrollPane.setBorder(null);

		TCSession session = CustomUtil.getTCSession();
		String roleName = "";
		String groupName = "";
		try {
			 roleName = session.getRole().getProperty("role_name");
			 groupName = session.getGroup().getGroupName();
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		int i = 0;
		for (String str : folderTypes) {
			final String[] strSplit = str.split(";");
			
			/* 설계자는 Folder Create 메뉴에서 General을 Default로 지정해주고 Corporate Folder는 선택이 안되도록 Dim처리 한다 */
			if(roleName.startsWith("RND_ENGINEER")) {
				if(strSplit[0].toString().equals("Corporate Folder"))
				continue;
			}
			// Engineering Management 그룹 관계자만 Corporate Folder를 만들 수 있도록 수정함. 2015-01-13
			//2023-10 조직변경 하드 코딩된 그룹명을 Preference로 변경 
			if (! groupName.equalsIgnoreCase(PreferenceService.getValue("RnD MANAGEMENT")))
			{
                if(strSplit[0].toString().equals("Corporate Folder"))
                continue;
			}

			if (strSplit == null || strSplit.length == 0) {
			} else if (strSplit.length > 1) {

				folderTypeMap.put(strSplit[0].toString(), strSplit[1].toString());
				JRadioButton radio = new JRadioButton(strSplit[0].toString());
				radio.setOpaque(false);
				group.add(radio);
				leftPanel.add((i + 1) + ".1.right.center.resizable.preferred", radio);

				if (i == 0) {
					radio.setSelected(true);
					setSelRadioValue(strSplit[0].toString());
				}

				i++;

				radio.setActionCommand(strSplit[0].toString());

				radio.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						JRadioButton selRadio = (JRadioButton) arg0.getSource();

						String act = selRadio.getActionCommand();

						setSelRadioValue(act);
					}
				});
			} else {
				folderTypeMap.put(strSplit[0].toString(), strSplit[0].toString());
				JRadioButton radio = new JRadioButton(strSplit[0].toString());
				radio.setOpaque(false);
				group.add(radio);
				leftPanel.add((i + 1) + ".1.right.center.resizable.preferred", radio);

				if (i == 0) {
					radio.setSelected(true);
					setSelRadioValue(strSplit[0].toString());
				}

				i++;

				radio.setActionCommand(strSplit[0].toString());

				radio.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						JRadioButton selRadio = (JRadioButton) arg0.getSource();

						String act = selRadio.getActionCommand();

						setSelRadioValue(act);
					}
				});
			}
		}

		/** 정보 입력 창 */
		rightPanel.add("1.1.right.center.preferred.preferred", new SYMCAWTLabel("Folder Name"));
		rightPanel.add("1.2.right.center.resizable.preferred", folderNameTF);
		SYMCAWTLabel ssangyongLabel = new SYMCAWTLabel("Folder Desc");
		rightPanel.add("2.1.right.center.preferred.preferred", ssangyongLabel);
		rightPanel.add("2.2.right.center.resizable.preferred", folderDescTA);

		modelIDTF.setEditable(false);
		modelIDTF.setEnabled(false);

		modelCopyCK.setOpaque(false);

		mainPanel.add("1.1.right.center.resizable.preferred", scrollPane);
		mainPanel.add("1.2.right.center.resizable.preferred", rightPanel);

		add("unbound.bind", mainPanel);

		ComponentService.setComboboxSize(rightPanel, 150, 22);
		ComponentService.setLabelSize(rightPanel, 120, 22);
	}

	@Override
	public boolean validCheck() {
		if (folderNameTF.getText() == null || folderNameTF.getText().equals("")) {
			Registry registry = Registry.getRegistry(this);
			MessageBox.post(dialog, registry.getString("CreateFolderDialog.MESSAGE.InputFolderName"), 
					registry.getString("CreateFolderDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
			return false;
		}
		return true;
	}

	/**
	 * 체크박스 선택 액션 처리.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2013. 1. 10.
	 */
	private void actionListnelAdd() {
		modelCopyCK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (modelCopyCK.isSelected()) {
					ItemSearchDialog searchDialog = new ItemSearchDialog(dialog, "Copy Model Search",
							"Copy Model Search", 0, SYMCClass.ITEM_TYPE, modelIDTF);
					searchDialog.run();
				} else {
					modelIDTF.setText("");
					((CreateFolderDialog) dialog).setSelectComp(null);
				}
			}
		});
	}

	public HashMap<String, String> getFolderTypeMap() {
		return folderTypeMap;
	}

	public void setSelRadioValue(String selValue) {
		this.selRadioValue = selValue;
	}

	public String getSelRadioValue() {
		return selRadioValue;
	}

	public FunctionField getFolderNameTF() {
		return folderNameTF;
	}

	public TextArea getFolderDescTA() {
		return folderDescTA;
	}

	public JCheckBox getModelCopyCK() {
		return modelCopyCK;
	}

	public FunctionField getModelIDTF() {
		return modelIDTF;
	}
}
