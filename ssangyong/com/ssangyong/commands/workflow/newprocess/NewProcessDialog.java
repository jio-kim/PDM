package com.ssangyong.commands.workflow.newprocess;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.SimpleProgressBar;
import com.ssangyong.common.dialog.SYMCAWTAbstractDialog;
import com.ssangyong.common.randerer.IconColorCellRenderer;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.PreferenceService;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.common.TCTypeRenderer;
import com.teamcenter.rac.kernel.ResourceMember;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAssignmentList;
import com.teamcenter.rac.kernel.TCComponentAssignmentListType;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentGroupMemberType;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentProcessType;
import com.teamcenter.rac.kernel.TCComponentProfile;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentTaskTemplateType;
import com.teamcenter.rac.kernel.TCComponentTaskType;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCComponentUserType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCTaskState;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Painter;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.VerticalLayout;
import com.teamcenter.rac.util.iTextArea;
import com.teamcenter.rac.util.iTextField;
import com.teamcenter.rac.util.combobox.iComboBox;
import com.teamcenter.rac.workflow.commands.complete.CompleteOperation;
import com.teamcenter.rac.workflow.common.taskproperties.TaskAttachmentsPanel;

/**
 * 1. 결재선 기능 추가 하여 사용가능 하지만, 프로세스 템플릿에 같은 이름의 타스크가 존재하면 안됨.
 * 2. 사용자 할당 시 Role 도 할당 가능(Group은 안됨)
 * ex1) 작성 -> 검토 -> 검토 -> 승인   : 사용 불가
 * ex2) 작성 -> 검토1 -> 검토2 -> 승인 : 사용 가능
 * @Copyright : S-PALM
 * @author   : 이정건
 * @since    : 2012. 3. 14.
 * Package ID : com.pungkang.commands.workflow.newprocess.NewProcessDialog.java
 */
@SuppressWarnings({"rawtypes", "unused", "unchecked"})
public class NewProcessDialog extends SYMCAWTAbstractDialog {

	/** */
	private static final long serialVersionUID = 1L;

	private Registry registry = Registry.getRegistry(this);

	/** 제목 */
	private iTextField titleTextField = new iTextField(30, 128, true);

	/** 설명 */
	private iTextArea commentTextArea = new iTextArea(3, 40, 256, false);

	/** 결재 템플릿 */
    private JComboBox taskTemplateComboBox;

	/** 결재선 */
	private iComboBox assignmentListCB;

	/** 결재선 enable 유/무 */
	private JCheckBox assignedCheckbox;

	/** 유저 검색 버튼 */
	private JButton searchButton = new JButton(registry.getString("Search.TEXT"), registry.getImageIcon("Search.ICON"));

	/** 유저 추가 버튼 */
	private JButton addUserButton = new JButton(registry.getString("UserAdd.TEXT"), registry.getImageIcon("UserAdd.ICON"));

	/** 유저 삭제 버튼 */
	private JButton removeUserButton = new JButton(registry.getString("UserRemove.TEXT"), registry.getImageIcon("UserRemove.ICON"));

	/** 유저 이름 검색 필드 */
	private JTextField nameTextField = new JTextField();

	/** 결재 대상 첨부파일  패널 */
	private TaskAttachmentsPanel taskAttachmentsPanel;

	/** 결재 대상 */
	private TCComponent[] targetComponent;

	/** 선택한 결재 선 */
	private TCComponentAssignmentList selectedAssignmentList;

	/** 유저 테이블 모델 */
	private UserTableModel userTableModel = new UserTableModel();

	/** 유저 검색 결과 테이블 */
	private JTable userTable = new JTable(userTableModel);

	/** 화면 중앙 좌측 결재 템플릿 트리 */
	private JTree workflowTemplateTree = new JTree();

	/** 결재 템플릿 트리 모델 */
	private DefaultTreeModel workflowTemplateTreeModel;

	private static String DISTRIBUTION = "배포";

	private static String APPROVE = "승인";

	private static String RND_APPROVE = "연구소 승인";

	private static String RND_RECIVE_TASK = "ECR 접수";

	private static String RND_REVIEW_TASK = "연구소 검토";

	private static String RND_RECIVE_TASK_USER_PreferenceName = "PK_RND_RECIVE_TASK_USER";

	private static String RND_REVIEW_TASK_USER_PreferenceName = "PK_RND_REVIEW_TASK_USER";

	private static boolean IS_FIRST_DISTRIBUTION_TASK = true;

	private JPanel mainPanel;

	/**
	 * 생성자
	 * @copyright : S-PALM
	 * @author : 이정건
	 * @since  : 2012. 3. 22.
	 * @param frame
	 * @param targetComponent
	 */
	public NewProcessDialog(Frame frame, TCComponent[] targetComponent){
		super(frame, false);
		try{
			this.targetComponent = targetComponent;

			/** UI 셋팅 */
			jbInit();

			/** 리스터 추가 */
			addListener();

			/** 템플릿,결재 정보 등 기본 데이터 셋팅*/
			initData();

		} catch(Exception ex){
			ex.printStackTrace();
		}
	}

	/**
	 * 생성자
	 * @copyright : S-PALM
	 * @author : 이정건
	 * @since  : 2012. 3. 22.
	 * @param dialog
	 * @param targetComponent
	 */
	public NewProcessDialog(Dialog dialog, TCComponent[] targetComponent){
		super(dialog, false);
		try	{
			this.targetComponent = targetComponent;

			/** UI 셋팅 */
			jbInit();

			/** 리스너 추가 */
			addListener();

			/** 템플릿,결재 정보 등 기본 데이터 셋팅*/
			initData();

		} catch(Exception ex){
			ex.printStackTrace();
		}
	}

	private void jbInit() throws Exception{
		setTitle(registry.getString("NewProcessDialog.TITLE"));
		createDialogUI("결재 프로세스를 생성 하는 다이얼로그 입니다. \n 1.승인자는 필수로 지정하셔야 합니다. \n 2.배포 타스크가 있는경우 배포 타스크 또한 필수로 지정 하셔야 합니다. \n 3.배포타스크에는 팀/개인 둘다 지정할 수 있습니다.", registry.getImageIcon("NewProcessDialogHeader.ICON"));
		setHeaderFont(new Font("맑은 고딕", Font.BOLD, 13));
		applyButton.setVisible(false);
		okButton.setText(registry.getString("NewProcess.TEXT"));

		mainPanel = new JPanel(new VerticalLayout());
		mainPanel.setOpaque(false);

		mainPanel.add("top.bind", getInfoPanel());
		mainPanel.add("unbound.bind", getAssignPanel());
		mainPanel.add("bottom.bind", getTaskAttachmentsPanel());

		JScrollPane scrollPane = new JScrollPane(mainPanel);
		scrollPane.getViewport().setBackground(Color.white);
		add("unbound.bind", scrollPane);
	}

	/**
	 * 화면 상당 결재 기본 정보 패널
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since  : 2012. 3. 22.
	 * @return
	 */
	private JPanel getInfoPanel(){
		JPanel infoPanel = new JPanel(new PropertyLayout(5, 5, 5, 5, 5, 5));
		infoPanel.setOpaque(false);

		Border border1 = BorderFactory.createEtchedBorder(EtchedBorder.RAISED, Color.white, new Color(165, 163, 151));
		TitledBorder border2 = new TitledBorder(border1, registry.getString("Information.TEXT"));
		border2.setTitleColor(SystemColor.BLUE);
		infoPanel.setBorder(border2);

		infoPanel.add("1.1.right.center", new JLabel(registry.getString("Title.LABEL")));
		infoPanel.add("2.1.right.center", new JLabel(registry.getString("Desc.LABEL")));
		infoPanel.add("3.1.right.center", new JLabel(registry.getString("TemplateType.LABEL")));
		infoPanel.add("4.1.right.center", new JLabel(registry.getString("AddressList.LABEL")));

		titleTextField.setPreferredSize(new Dimension(400, 20));
		titleTextField.setEditable(false);

		assignmentListCB = new iComboBox(false);
		assignmentListCB.setMaximumRowCount(10);
		assignmentListCB.getTextField().setColumns(23);

		JScrollPane jScrollPane1 = new JScrollPane();
		jScrollPane1.getViewport().add(commentTextArea);

		assignedCheckbox = new JCheckBox(registry.getString("AssignmentList.LABEL"));
		assignedCheckbox.setEnabled(true);
		assignedCheckbox.setOpaque(false);

		taskTemplateComboBox = new JComboBox(){
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				Painter.paintIsRequired(this, g);
			}
		};
		taskTemplateComboBox.setPreferredSize(new Dimension(300, 20));

		infoPanel.add("1.2.left.center", titleTextField);
		infoPanel.add("2.2.left.center", jScrollPane1);
		infoPanel.add("3.2.left.center", taskTemplateComboBox);
		infoPanel.add("4.2.left.center", assignmentListCB);
		infoPanel.add("4.3.left.center", assignedCheckbox);

		return infoPanel;
	}

	/**
	 * 화면 중단 결재 템플릿 트리 및 유저 검색 테이블 패널
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since  : 2012. 3. 22.
	 * @return
	 */
	private JPanel getAssignPanel(){
		JPanel assignPanel = new JPanel(new BorderLayout());
		assignPanel.setOpaque(false);

		Border border3 = BorderFactory.createEtchedBorder(EtchedBorder.RAISED, Color.white, new Color(165, 163, 151));
		TitledBorder border4 = new TitledBorder(border3, registry.getString("ApproverSelect.TEXT"));
		assignPanel.setBorder(border4);
		border4.setTitleColor(Color.BLUE);

		assignPanel.setPreferredSize(new Dimension(650, 250));

		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(registry.getString("SelectWorkflowType.TEXT"));
		workflowTemplateTreeModel = new DefaultTreeModel(rootNode);
		workflowTemplateTree.setModel(workflowTemplateTreeModel);
		ResourceTreeCellRenderer resourceTreeCellRenderer = new ResourceTreeCellRenderer();
		workflowTemplateTree.setCellRenderer(resourceTreeCellRenderer);

		/** 검색 조건을 입력 하는 패널 */
		JPanel searchConditionPanel = new JPanel(new HorizontalLayout(5,5,5,5,5));
		searchConditionPanel.setOpaque(false);
		searchConditionPanel.add("left.bind.center.center", new JLabel(registry.getString("Name.LABEL")));
		nameTextField.setPreferredSize(new Dimension(100, 20));
		searchConditionPanel.add("unbound", nameTextField);
		searchConditionPanel.add("right.bind.center.center", searchButton);
		searchButton.setMargin(new Insets(2,4,2,4));

		/** 검색결과 테이블을 포함하는 패널 */
		JPanel resultPanel = new JPanel(new BorderLayout());
		resultPanel.setOpaque(false);
		Border border7 = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		resultPanel.setBorder(border7);
		JScrollPane jScrollPane3 = new JScrollPane();
		jScrollPane3.setOpaque(false);
		jScrollPane3.getViewport().setOpaque(false);
		jScrollPane3.getViewport().add(userTable);
		userTable.getTableHeader().setReorderingAllowed(false);
		userTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() > 1){
					userAddActionPerformed(userTable.getSelectedRow());
				}
			}
		});
		TableColumnModel model = userTable.getColumnModel();
		for(int i=0; i<model.getColumnCount(); i++ ){
			model.getColumn(0).setCellRenderer(new IconColorCellRenderer(TCTypeRenderer.getIcon(SYMCClass.QryUserSearch), new Color(230,230,230)));
		}
		resultPanel.add(jScrollPane3, java.awt.BorderLayout.CENTER);

		/** 유저 추가/삭제 패널 */
		JPanel assignButtonPanel = new JPanel();
		assignButtonPanel.setOpaque(false);
		assignButtonPanel.add(addUserButton);
		assignButtonPanel.add(removeUserButton);

		/** 검색조건 및 검색 결과 테이블을 포함 하는 패널 */
		JPanel userPanel = new JPanel(new BorderLayout());
		userPanel.setOpaque(false);
		userPanel.add(assignButtonPanel, java.awt.BorderLayout.SOUTH);
		userPanel.add(resultPanel, java.awt.BorderLayout.CENTER);
		userPanel.add(searchConditionPanel, java.awt.BorderLayout.NORTH);

		JTabbedPane tabPane = new JTabbedPane();
		tabPane.addTab(registry.getString("UserSearch.TEXT"), registry.getImageIcon("User.ICON"), userPanel);
		tabPane.addTab(registry.getString("OrgSearch.TEXT"), registry.getImageIcon("Group.ICON"), new SearchGroupPanel(session, this, workflowTemplateTree, workflowTemplateTreeModel));

		/** 결재 템플릿 트리를 포함하는 JScrollPane*/
		JScrollPane jScrollPane2 = new JScrollPane();
		jScrollPane2.getViewport().add(workflowTemplateTree);

		/** 화면 중앙의 분할 패널 */
		JSplitPane jSplitPane1 = new JSplitPane();
		jSplitPane1.setOpaque(false);
		jSplitPane1.setResizeWeight(1.0);
		jSplitPane1.add(jScrollPane2, JSplitPane.LEFT);
		jSplitPane1.add(tabPane, JSplitPane.RIGHT);
		jSplitPane1.setDividerLocation(250);

		assignPanel.add(jSplitPane1, java.awt.BorderLayout.CENTER);

		return assignPanel;
	}

	/**
	 * 화면 하단 결재 타겟 첨부 파일 패널
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since  : 2012. 3. 22.
	 * @return
	 */
	private TaskAttachmentsPanel getTaskAttachmentsPanel(){
		taskAttachmentsPanel = new TaskAttachmentsPanel(session, targetComponent, AIFUtility.getCurrentApplication());
		Border border5 = BorderFactory.createEtchedBorder(EtchedBorder.RAISED, Color.white, new Color(165, 163, 151));
		TitledBorder border6 = new TitledBorder(border5, registry.getString("Attachments.TEXT"));
		taskAttachmentsPanel.setBorder(border6);
		border6.setTitleColor(SystemColor.BLUE);
		taskAttachmentsPanel.setOpaque(false);
		taskAttachmentsPanel.setPreferredSize(new Dimension(600, 150));

		for(int i = 0; i < taskAttachmentsPanel.getComponentCount(); i++){
			Component component = taskAttachmentsPanel.getComponent(i);
			if(component instanceof JPanel){
				((JPanel)component).setOpaque(false);
			}
		}
		//		taskAttachmentsPanel.setVisible(false);
		return taskAttachmentsPanel;
	}

	private void addListener(){
		taskTemplateComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				IS_FIRST_DISTRIBUTION_TASK = true;
				taskTemplateLoad((TCComponentTaskTemplate)taskTemplateComboBox.getSelectedItem());
			}
		});

		assignedCheckbox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent paramActionEvent){
				try	{
					TCComponent[] arrayOfTCComponent = getAssignmentLists(assignedCheckbox.isSelected());
					if (arrayOfTCComponent != null){
						assignmentListCB.removeAllItems();
						String[] arrayOfString = createRenderIcons(arrayOfTCComponent);
						assignmentListCB.addItems(arrayOfTCComponent, arrayOfString);
						assignmentListCB.sort(arrayOfString);
						assignmentListCB.validate();
					}
				}
				catch (Exception localException){
					localException.printStackTrace();
				}
			}
		});

		assignmentListCB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent paramActionEvent){
				try {
					if (assignmentListCB.getSelectedItemCount() < 1)
						return;
					Object object = assignmentListCB.getSelectedItem();
					if (!(object instanceof TCComponentAssignmentList))
						return;
					selectedAssignmentList = (TCComponentAssignmentList)object;
					taskTemplateLoad(selectedAssignmentList.getProcessTemplate());
					taskTemplateComboBox.setSelectedItem(selectedAssignmentList.getProcessTemplate());

					DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)workflowTemplateTreeModel.getRoot();
					Enumeration children = rootNode.children();

					ResourceMember[] resourceMember = selectedAssignmentList.getDetails();
					for(int i=0; i<resourceMember.length; i++){
						while(children.hasMoreElements()){
							DefaultMutableTreeNode taskNode = (DefaultMutableTreeNode)children.nextElement();
							TCComponent comp = session.getComponentManager().getTCComponent("");
							if(resourceMember[i].getTaskTemplate().toDisplayString().equals(taskNode.toString())){
								TCComponent[] resources = resourceMember[i].getResources();
								for(int k=0; k<resourceMember[i].getResources().length; k++){
									DefaultMutableTreeNode resourceNode = new DefaultMutableTreeNode((TCComponentGroupMember)resources[k]);
									workflowTemplateTreeModel.insertNodeInto(resourceNode, taskNode, taskNode.getChildCount());
								}
								break;
							}
						}
					}
					expandAllTree();
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
		});

		nameTextField.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				searchUser();
			}
		});

		searchButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				searchUser();
			}
		});

		addUserButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				userAddActionPerformed(userTable.getSelectedRow());
			}
		});

		removeUserButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(workflowTemplateTree.getSelectionCount() == 0){
					MessageBox.post(NewProcessDialog.this, registry.getString("Message3"), registry.getString("Message.TITLE"), MessageBox.INFORMATION);
					return;
				}
				TreePath[] treepath = workflowTemplateTree.getSelectionPaths();
				for(int i = 0; i < treepath.length; i++){
					DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)treepath[i].getLastPathComponent();
					if(selectedNode.getUserObject() instanceof TCComponentTaskTemplate){
						continue;
					}
					workflowTemplateTreeModel.removeNodeFromParent(selectedNode);
				}
			}
		});
	}

	private void userAddActionPerformed(int selectedRow){
		if(workflowTemplateTree.getSelectionCount() == 0){
			MessageBox.post(NewProcessDialog.this, registry.getString("Message1"), registry.getString("Message.TITLE"), MessageBox.INFORMATION);
			return;
		}
		if(userTable.getSelectedRow() == -1){
			MessageBox.post(NewProcessDialog.this, registry.getString("Message2"), registry.getString("Message.TITLE"), MessageBox.INFORMATION);
			return;
		}
		TCComponentGroupMember member = userTableModel.getTCComponentGroupMember(selectedRow);
		TreePath[] treepath = workflowTemplateTree.getSelectionPaths();
		for(int i = 0; i < treepath.length; i++){
			DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)treepath[i].getLastPathComponent();
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(member);
			if(parentNode.isRoot()){
				continue;
			}
			if(!(parentNode.getUserObject() instanceof TCComponentTaskTemplate)){
				continue;
			}
			boolean hasChild = false;
			Enumeration enum1 = parentNode.children();
			while(enum1.hasMoreElements()){
				DefaultMutableTreeNode tmpChildNode = (DefaultMutableTreeNode)enum1.nextElement();
				if(tmpChildNode.getUserObject().equals(childNode.getUserObject())){
					hasChild = true;
					break;
				}
			}
			if(hasChild){
				continue;
			}
			workflowTemplateTreeModel.insertNodeInto(childNode, parentNode, parentNode.getChildCount());
			workflowTemplateTree.expandPath(treepath[i]);
		}
	}

	/**
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since  : 2012. 3. 13.
	 * @param paramBoolean
	 * @return 저장된 결재선 배열
	 * @throws Exception
	 */
	private TCComponent[] getAssignmentLists(boolean paramBoolean) throws Exception {
		TCComponent[] arrayOfTCComponent = null;
		try{
			if (paramBoolean){
				TCComponentAssignmentListType localTCComponentAssignmentListType = (TCComponentAssignmentListType)this.session.getTypeComponent("EPMAssignmentList");
				arrayOfTCComponent = localTCComponentAssignmentListType.extent();
			}
			else{
				assignmentListCB.removeAllItems();
			}
		}
		catch (Exception localException){
			localException.printStackTrace();
			throw localException;
		}
		return arrayOfTCComponent;
	}

	private String[] createRenderIcons(TCComponent[] paramArrayOfTCComponent){
		String[] arrayOfString1 = null;
		int i = (paramArrayOfTCComponent != null) ? paramArrayOfTCComponent.length : 0;
		if (i > 0){
			arrayOfString1 = new String[i];
			String[] arrayOfString2 = { "shared" };
			String[][] arrayOfString = (String[][])null;
			try{
				
				int paramSize = paramArrayOfTCComponent.length;
				ArrayList<TCComponent> list = new ArrayList<TCComponent>();
				for(int k=0; k<paramSize; k++){
					list.add(paramArrayOfTCComponent[k]);
				}
				arrayOfString = TCComponentType.getPropertiesSet(list, arrayOfString2);
				String str = null;
				for (int j = 0; j < i; ++j){
					str = arrayOfString[j][0];
					if (str.equalsIgnoreCase("Y"))
						arrayOfString1[j] = "sharedList";
					else
						arrayOfString1[j] = "blank";
				}
			}
			catch (Exception localException){
				MessageBox localMessageBox = new MessageBox(localException);
				localMessageBox.setVisible(true);
			}
		}
		return arrayOfString1;
	}

	private void initData() throws Exception{
		CustomUtil.addTextFieldListenerForKorean(commentTextArea);
		CustomUtil.addTextFieldListenerForKorean(nameTextField);
		titleTextField.setText(targetComponent[0].toString());
		TCComponentTaskTemplateType imancomponenttasktemplatetype = (TCComponentTaskTemplateType)session.getTypeComponent("EPMTaskTemplate");
		if(imancomponenttasktemplatetype != null){
			//TCComponentTaskTemplate[] aimancomponenttasktemplate = imancomponenttasktemplatetype.extentReadyTemplates(true);
			TCComponentTaskTemplate[] aimancomponenttasktemplate = imancomponenttasktemplatetype.getProcessTemplates(true, false, null, null, null);
			int i = 0;
			if(aimancomponenttasktemplate != null){
				i = aimancomponenttasktemplate.length;
			}
			taskTemplateComboBox.removeAllItems();
			for(int j = 0; j < i; j++){
				String templateName = aimancomponenttasktemplate[j].toDisplayString();
				if(templateName.equals("설계변경통보") || templateName.equals("설계변경요청") || templateName.equals("문서결재") || templateName.equals("합의문서결재")){
					taskTemplateComboBox.addItem(aimancomponenttasktemplate[j]);
				}
				targetTypeCheck(aimancomponenttasktemplate[j]);
			}
		}
	}

	/**
	 * 결재 대상의 타입에 따라서 자동으로 w/f 템플릿을 지정해 준다.
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since  : 2012. 5. 9.
	 * @param taskTemplate
	 */
	private void targetTypeCheck(TCComponentTaskTemplate taskTemplate){
	}

	/**
	 * 텍스트 필드에 입력된 유저 검색
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since  : 2012. 3. 22.
	 */
	private void searchUser(){
		String userName = nameTextField.getText();
		if(userName.equals("")){
			MessageBox.post(this, registry.getString("Message4"), registry.getString("Message.TITLE"), MessageBox.INFORMATION);
			return;
		}
		userTableModel.removeAllRow();
		try	{
			String[] entryName = {CustomUtil.getTextServerString(session, "Name")};
			String[] entryValue = {userName};
			TCComponent[] userComponent = CustomUtil.queryComponent(SYMCClass.QryUserSearch, entryName, entryValue);
			if(userComponent == null || userComponent.length == 0)
			{
				MessageBox.post(this, registry.getString("Message5"), registry.getString("Message.TITLE"), MessageBox.INFORMATION);
				return;
			}
			TCComponentGroupMemberType imancomponentgroupmembertype = (TCComponentGroupMemberType)session.getTypeComponent("GroupMember");
			for(int i = 0; i < userComponent.length; i++)
			{
				TCComponentGroupMember groupmember[] = imancomponentgroupmembertype.findByUser((TCComponentUser)userComponent[i]);
				userTableModel.addRow(groupmember);
			}
		} catch(Exception ex){
			ex.printStackTrace();
			MessageBox.post(this, ex);
		}
	}

	/**
	 * 결재 테플릿을 읽어 와서 트리를 구성 한다.
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since  : 2012. 3. 22.
	 * @param currentTemplate
	 */
	private void taskTemplateLoad(TCComponentTaskTemplate currentTemplate){
		try	{
			DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(currentTemplate);
			workflowTemplateTreeModel.setRoot(rootNode);
			TCComponentTaskTemplate[] startTask = currentTemplate.getStartSuccessors();
			if(currentTemplate.toDisplayString().equals(SYMCClass.CONSENSUS_DOC_WORKFLOW)){
				makeConsensusDocTemplateSubTask(startTask, rootNode);
			}
			else{
				makeSubTask(startTask, rootNode);
			}
			workflowTemplateTree.expandRow(0);
		} catch(Exception ex){
			ex.printStackTrace();
			MessageBox.post(this, ex);
		}
	}

	/**
	 * 결재 템플릿을 읽어와서 부모 타스크로 부터 하위 타스크를 재귀적으로 구성한다.
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since  : 2012. 3. 22.
	 * @param subTask
	 * @param rootNode
	 * @throws Exception
	 */
	private void makeSubTask(TCComponentTaskTemplate[] subTask, DefaultMutableTreeNode rootNode){
		try{
			if(subTask == null || subTask.length == 0){
				return;
			}
			for(int i = 0; i < subTask.length; i++){
				if(subTask[i].getType().equals(TCComponentTaskTemplateType.EPM_CONDITION_TASKTEMPLATE_TYPE)
						|| subTask[i].getType().equals(TCComponentTaskTemplateType.EPM_ADDSTATUS_TASKTEMPLATE_TYPE)
						|| subTask[i].getType().equals(TCComponentTaskTemplateType.EPM_NOTIFY_TASKTEMPLATE_TYPE)
						|| subTask[i].getType().equals("EPMOrTaskTemplate"))
				{
					continue;
				}
				DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(subTask[i]);
				rootNode.add(subNode);
				PreferenceService.createService(session);
				if(subTask[i].getName().equals(RND_RECIVE_TASK)){
					String rnd_recive_task_user_preferenceName = PreferenceService.getValue(RND_RECIVE_TASK_USER_PreferenceName);
					if(rnd_recive_task_user_preferenceName == null || rnd_recive_task_user_preferenceName.equals("")){
						MessageBox.post("\"ECR 접수\" 타스크 담당자가 설정 되어 있지 않습니다.\n 옵션에서 \"PK_RND_RECIVE_TASK_USER\" 값을 설정해 주세요", "알림", MessageBox.INFORMATION);
						return;
					}
					TCComponentUserType userType = (TCComponentUserType)session.getTypeComponent("User");
					TCComponentGroupMemberType groupMemberType = (TCComponentGroupMemberType)session.getTypeComponent("GroupMember");
					TCComponentUser user = userType.find(rnd_recive_task_user_preferenceName);
					if(user != null){
						TCComponentGroupMember[] groupMember = groupMemberType.findByUser(user);
						subNode.add(new DefaultMutableTreeNode(groupMember[0]));
					}
				}
				if(subTask[i].getName().equals(RND_REVIEW_TASK)){
					String rnd_recive_task_user_preferenceName = PreferenceService.getValue(RND_REVIEW_TASK_USER_PreferenceName);
					if(rnd_recive_task_user_preferenceName == null || rnd_recive_task_user_preferenceName.equals("")){
						MessageBox.post("\"연구소 검토\" 타스크 담당자가 설정 되어 있지 않습니다.\n 옵션에서 \"PK_RND_REVIEW_TASK_USER\" 값을 설정해 주세요", "알림", MessageBox.INFORMATION);
						return;
					}
					TCComponentUserType userType = (TCComponentUserType)session.getTypeComponent("User");
					TCComponentGroupMemberType groupMemberType = (TCComponentGroupMemberType)session.getTypeComponent("GroupMember");
					TCComponentUser user = userType.find(rnd_recive_task_user_preferenceName);
					if(user != null){
						TCComponentGroupMember[] groupMember = groupMemberType.findByUser(user);
						subNode.add(new DefaultMutableTreeNode(groupMember[0]));
					}
				}
			}
			for(int i = 0; i < subTask.length; i++){
				TCComponentTaskTemplate[] nextTask = subTask[i].getSuccessors();
				makeSubTask(nextTask, rootNode);
			}
		}
		catch (TCException e) {
			MessageBox.post(e);
			e.printStackTrace();
		}
	}

	/**
	 * (주)풍강의 합의 문서 결재는 병렬 템플릿이라서 메소드 추가함.
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since  : 2012. 5. 14.
	 * @param subTask
	 * @param rootNode
	 * @throws Exception
	 */
	private void makeConsensusDocTemplateSubTask(TCComponentTaskTemplate[] subTask, DefaultMutableTreeNode rootNode) throws Exception{
		if(subTask == null || subTask.length == 0){
			return;
		}
		for(int i = 0; i < subTask.length; i++){
			if(subTask[i].getType().equals(TCComponentTaskTemplateType.EPM_CONDITION_TASKTEMPLATE_TYPE)
					|| subTask[i].getType().equals(TCComponentTaskTemplateType.EPM_ADDSTATUS_TASKTEMPLATE_TYPE)
					|| subTask[i].getType().equals(TCComponentTaskTemplateType.EPM_NOTIFY_TASKTEMPLATE_TYPE)
					|| subTask[i].getType().equals("EPMOrTaskTemplate"))
			{
				continue;
			}
			DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(subTask[i]);
			rootNode.add(subNode);
		}
		for(int i = 0; i < subTask.length; i++){
			TCComponentTaskTemplate[] nextTask = subTask[i].getSuccessors();
			if(nextTask != null && nextTask.length != 0){
				if(nextTask[0].toDisplayString().equals("배포")){
					if(IS_FIRST_DISTRIBUTION_TASK){
						makeConsensusDocTemplateSubTask(nextTask, rootNode);
						IS_FIRST_DISTRIBUTION_TASK = false;
					}
				}
				else{
					makeConsensusDocTemplateSubTask(nextTask, rootNode);
				}
			}
		}
	}

	/**
	 * 결재 프로세스 생성 메소드
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since  : 2012. 3. 22.
	 * @param e
	 */
	private void createWorkflowProcess(ActionEvent e){
		try	{
			String title = titleTextField.getText();
			String comment = commentTextArea.getText();
			if(taskTemplateComboBox.getSelectedItem() == null || taskTemplateComboBox.getSelectedObjects().length == 0){
				return;
			}
			TCComponentTaskTemplate taskTemplate = (TCComponentTaskTemplate)taskTemplateComboBox.getSelectedItem();
			TCComponent[] attComp = taskAttachmentsPanel.getAttachments();
			int[] attType = taskAttachmentsPanel.getAttachmentTypes();
			ResourceMember[] resourceMember = getResourceMember();
			TCComponentProcessType processtype = (TCComponentProcessType)session.getTypeComponent("Job");
			TCComponentProcess newProcess = (TCComponentProcess)processtype.create(title, comment, taskTemplate, attComp, attType, resourceMember);
			completeConditionTask(newProcess);
			setDescription(newProcess, comment);
			refreshTarget(attComp);
		} catch(Exception ex){
			ex.printStackTrace();
			MessageBox.post(ex);
		}
	}

	/**
	 * 컨디션 타스크는 자동 시작 시킨다.
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since  : 2012. 3. 22.
	 * @param newProcess
	 * @throws Exception
	 */
	private void completeConditionTask(TCComponentProcess newProcess) throws Exception{
		TCComponentTask[] subTask = newProcess.getRootTask().getSubtasks();
		for(int i = 0; i < subTask.length; i++){
			if(subTask[i].getType().equals(TCComponentTaskType.EPM_CONDITION_TASK_TYPE)){
				TCTaskState imantaskstate = subTask[i].getState();
				if(imantaskstate == TCTaskState.STARTED){
					// 1은 참.
					subTask[i].setProperty("condition_result", "1");
					CompleteOperation operation = new CompleteOperation(AIFUtility.getActiveDesktop(), new TCComponentTask[]{subTask[i]}, "");
					session.queueOperation(operation);
				}
				break;
			}
		}
	}

	/**
	 * 프로세스에 디스크립션 추가
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since  : 2012. 3. 22.
	 * @param newProcess
	 * @param comment
	 * @throws Exception
	 */
	private void setDescription(TCComponentProcess newProcess, String comment) throws Exception{
		TCProperty allTaskProperty = newProcess.getTCProperty("all_tasks");
		TCComponent[] taskArray = allTaskProperty.getReferenceValueArray();
		for(int i = 0; i < taskArray.length; i++){
			if(taskArray[i].getType().equals(TCComponentTaskType.EPM_PERFORM_SIGNOFF_TASK_TYPE)){
				TCComponentTask performTask = (TCComponentTask)taskArray[i];
				performTask.setInstructions(comment);
			}
		}
	}

	/**
	 * 결재 대상 및 하위 까지 모든 대상을 refresh
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since  : 2012. 3. 22.
	 * @param target
	 */
	private void refreshTarget(TCComponent[] target){
		if(target == null){
			return;
		}
		for(int i = 0; i < target.length; i++){
			try{
				target[i].refresh();
				//TCComponent[] child = target[i].getChildrenList("").toTCComponentArray();
				//for(int j = 0; j < child.length; j++){
				//	child[j].refresh();
				//}
				AIFComponentContext[] child = target[i].getChildren("");
				for(int j = 0; j < child.length; j++){
				    ((TCComponent) child[j].getComponent()).refresh();
				}
			} catch(TCException ex){
				ex.printStackTrace();
			}
		}
	}

	/**
	 * 템플릿 트리로 부터 리소스 멤버를 생성 함.
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since  : 2012. 3. 22.
	 * @return
	 * @throws Exception
	 */
	private ResourceMember[] getResourceMember() throws Exception{
		Vector<ResourceMember> resourceMemberVector = new Vector<ResourceMember>();
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)workflowTemplateTreeModel.getRoot();
		Enumeration enum1 = rootNode.children();
		while(enum1.hasMoreElements())
		{
			DefaultMutableTreeNode taskNode = (DefaultMutableTreeNode)enum1.nextElement();
			//task template
			TCComponentTaskTemplate taskTemplate = (TCComponentTaskTemplate)taskNode.getUserObject();
			Enumeration enum2 = taskNode.children();
			Vector<TCComponentGroupMember> memberVector = new Vector<TCComponentGroupMember>();
			while(enum2.hasMoreElements())
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)enum2.nextElement();

				TCComponentGroupMember member = (TCComponentGroupMember)node.getUserObject();
				memberVector.add(member);
			}

			//member component
			TCComponent[] memberComponent = new TCComponent[memberVector.size()];
			if(memberVector.size() == 0)
			{
				continue;
			}
			memberVector.toArray(memberComponent);
			//profiles
			//			TCComponentProfile[] signoffProfiles = getSignoffProfiles(taskTemplate);
			TCComponentProfile[] signoffProfiles = new TCComponentProfile[memberComponent.length];
			Integer action = getActions(taskTemplate);
			Integer[] actions = new Integer[memberComponent.length];
			for(int i = 0; i < actions.length; i++)
			{
				actions[i] = action;
			}
			//RevQuorum, AckQuorum
			int[] quorum = getSignoffQuorum(taskTemplate);
			ResourceMember resourceMember = new ResourceMember(taskTemplate, memberComponent, signoffProfiles, actions, quorum[0], quorum[1],0);
			resourceMemberVector.add(resourceMember);
		}
		ResourceMember[] resourceMemberArray = null;
		if(resourceMemberVector.size() != 0)
		{
			resourceMemberArray = new ResourceMember[resourceMemberVector.size()];
			resourceMemberVector.toArray(resourceMemberArray);
		}
		return resourceMemberArray;
	}

	private int[] getSignoffQuorum(TCComponentTaskTemplate imancomponenttasktemplate) throws TCException{
		int ai[] = new int[2];
		ai[0] = -1;
		ai[1] = -1;
		try	{
			String s = imancomponenttasktemplate.getType();
			if(s.compareTo("EPMReviewTaskTemplate") == 0){
				TCComponentTaskTemplate aimancomponenttasktemplate[] = imancomponenttasktemplate.getSubtaskDefinitions();
				if(aimancomponenttasktemplate != null && aimancomponenttasktemplate.length == 2){
					TCProperty imanproperty = aimancomponenttasktemplate[0].getTCProperty("review_task_quorum");
					if(imanproperty != null){
						ai[0] = imanproperty.getIntValue();
					}
				}
			} else if(s.compareTo("EPMAcknowledgeTaskTemplate") == 0){
				TCComponentTaskTemplate aimancomponenttasktemplate1[] = imancomponenttasktemplate.getSubtaskDefinitions();
				if(aimancomponenttasktemplate1 != null && aimancomponenttasktemplate1.length == 2){
					TCProperty imanproperty1 = aimancomponenttasktemplate1[0].getTCProperty("review_task_quorum");
					if(imanproperty1 != null){
						ai[1] = imanproperty1.getIntValue();
					}
				}
			} else if(s.compareTo("EPMRouteTaskTemplate") == 0){
				TCComponentTaskTemplate aimancomponenttasktemplate2[] = imancomponenttasktemplate.getSubtaskDefinitions();
				if(aimancomponenttasktemplate2 != null && aimancomponenttasktemplate2.length == 3){
					int ai1[] = getSignoffQuorum(aimancomponenttasktemplate2[0]);
					int ai2[] = getSignoffQuorum(aimancomponenttasktemplate2[1]);
					ai[0] = ai1[0];
					ai[1] = ai2[1];
				}
			}
		} catch(TCException imanexception){
			throw imanexception;
		}
		return ai;
	}

	private Integer getActions(TCComponentTaskTemplate imancomponenttasktemplate){
		if(imancomponenttasktemplate.getType().equals("EPMAcknowledgeTaskTemplate")){
			return new Integer(2);
		} else if(imancomponenttasktemplate.getType().equals("EPMReviewTaskTemplate") || imancomponenttasktemplate.getType().equals("EPMRouteTaskTemplate")){
			return new Integer(1);
		} else{
			return new Integer(0);
		}
	}

	private class ResourceTreeCellRenderer extends DefaultTreeCellRenderer{
		/** */
		private static final long serialVersionUID = 1L;

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus){
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			ImageIcon icon = TCTypeRenderer.getIcon(((DefaultMutableTreeNode)value).getUserObject());
			if(icon != null){
				setIcon(icon);
			}
			return this;
		}
	}

	/**
	 * 결재 요청 Title을 입력한다.
	 * @param title String
	 */
	public void setProcessTitle(String title){
		titleTextField.setText(title);
	}

	/**
	 * 결재 Template을 선택한다.
	 * @param templateName String
	 */
	public void setSelectTemplate(String templateName){
		for(int i=0;i<taskTemplateComboBox.getItemCount();i++)
		{
			Object templateObject = taskTemplateComboBox.getItemAt(i);
			if(templateObject.toString().equals(templateName))
			{
				taskTemplateComboBox.setSelectedIndex(i);
				break;
			}
		}
	}

	/**
	 * 해당 task에 group member를 할당한다.
	 * @param taskName String
	 * @param groupMemberList ArrayList 반드시 TCComponentGroupMember Array여야 한다.
	 */
	public void setAssignGroupMemberToTask(String taskName, ArrayList groupMemberList){
		expandAllTree();
		for(int i = 0; i < workflowTemplateTree.getRowCount(); i++)
		{
			TreePath treepath = workflowTemplateTree.getPathForRow(i);
			DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)treepath.getLastPathComponent();
			if(parentNode.toString().equals(taskName))
			{
				for(int j=0;j<groupMemberList.size();j++)
				{
					TCComponentGroupMember member = (TCComponentGroupMember)groupMemberList.get(j);
					DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(member);
					workflowTemplateTreeModel.insertNodeInto(childNode, parentNode, parentNode.getChildCount());
				}
				workflowTemplateTree.expandPath(treepath);
				break;
			}
		}
	}

	/**
	 * 해당 task에 group member를 할당한다.
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since  : 2012. 3. 22.
	 * @param taskName
	 * @param members
	 */
	public void setAssignGroupMemberToTask(String taskName, TCComponentGroupMember[] members){
		expandAllTree();
		for(int i = 0; i < workflowTemplateTree.getRowCount(); i++)
		{
			TreePath treepath = workflowTemplateTree.getPathForRow(i);
			DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)treepath.getLastPathComponent();
			if(parentNode.toString().equals(taskName))
			{
				for(int j=0;j<members.length;j++)
				{
					DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(members[j]);
					workflowTemplateTreeModel.insertNodeInto(childNode, parentNode, parentNode.getChildCount());
				}
				workflowTemplateTree.expandPath(treepath);
				break;
			}
		}
	}

	/**
	 * expandAll
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since  : 2012. 3. 22.
	 */
	private void expandAllTree(){
		for(int i = 0; i < workflowTemplateTree.getRowCount(); i++){
			workflowTemplateTree.expandRow(i);
		}
	}

	/**
	 * 테이블 모델
	 * @Copyright : S-PALM
	 * @author   : 이정건
	 * @since    : 2012. 3. 22.
	 * Package ID : com.pungkang.commands.workflow.newprocess.NewProcessDialog.java
	 */
	private class UserTableModel extends DefaultTableModel{
		/** */
		private static final long serialVersionUID = 1L;

		private Object[] column = registry.getStringArray("column.ARRAY");

		public UserTableModel()	{
			setColumnIdentifiers(column);
		}

		public void addRows(TCComponentGroupMember[] groupMember){
			for(int i = 0; i < groupMember.length; i++)	{
				addRow(new Object[]{groupMember[i]});
			}
		}

		public TCComponentGroupMember getTCComponentGroupMember(int row){
			return(TCComponentGroupMember)super.getValueAt(row, 0);
		}

		public Object getValueAt(int row, int column){
			Object data = null;
			try{
				TCComponentGroupMember groupMember = (TCComponentGroupMember)super.getValueAt(row, 0);
				if(column == 0){
					data = groupMember.getGroup();
				} else if(column == 1){
					data = groupMember.getRole();
				} else if(column == 2){
					data = groupMember.getUser();
				}
			} catch(TCException ex){
				ex.printStackTrace();
			}
			return data;
		}

		public boolean isCellEditable(int row, int column){
			return false;
		}

		public void removeAllRow(){
			for(int i = 0; i < getRowCount(); ){
				removeRow(i);
			}
		}
	}

	@Override
	public void invokeOperation(final ActionEvent e) {
		session.queueOperation(new AbstractAIFOperation() {
			@Override
			public void executeOperation(){
				SimpleProgressBar bar = null;
				try {
					bar = new SimpleProgressBar(NewProcessDialog.this, "결재 요청 중...");
					createWorkflowProcess(e);
					bar.closeProgressBar();
				} catch (Exception e2) {
					e2.printStackTrace();
					bar.closeProgressBar();
					MessageBox.post(AIFUtility.getActiveDesktop(), "결재 상신 중 오류 발생", e2.getMessage(), "알림", MessageBox.INFORMATION, false);
				}
			}
		});
	}

	/**
	 * 모든 템플릿의 승인 타스크는 필수로 지정하여야함.
	 * ECR에서 연구소 승인 타스크또한 필수로 지정
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since  : 2012. 5. 11.
	 * @override
	 * @see com.SYMCAWTAbstractDialog.common.dialog.SpalmAbstractDialog#validCheck()
	 * @return
	 */
	@Override
	public boolean validCheck() {
		TCComponent[] attComp = taskAttachmentsPanel.getAttachments();
		ResourceMember[] resourceMember = null;
		try {
			resourceMember = getResourceMember();
		} catch (Exception e) {
			e.printStackTrace();
		}
		int count = ((DefaultMutableTreeNode)workflowTemplateTreeModel.getRoot()).getChildCount();

		if(attComp == null || attComp.length == 0){
			MessageBox.post(NewProcessDialog.this,  registry.getString("Message6"), registry.getString("Message.TITLE"), MessageBox.INFORMATION);
			return false;
		}
		if(count != 0 && (resourceMember == null || resourceMember.length == 0)){
			MessageBox.post(NewProcessDialog.this, registry.getString("Message7"), registry.getString("Message.TITLE"), MessageBox.INFORMATION);
			return false;
		}

		if(titleTextField.getText() == null || titleTextField.getText().equals("")){
			titleTextField.setText(targetComponent[0].toString());
		}

		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)workflowTemplateTreeModel.getRoot();
		Enumeration enumeration = rootNode.children();
		while(enumeration.hasMoreElements()){
			DefaultMutableTreeNode subTaskNode = (DefaultMutableTreeNode)enumeration.nextElement();
			if(subTaskNode.toString().equals(RND_APPROVE)){
				if(subTaskNode.isLeaf()){
					MessageBox.post(RND_APPROVE + " 타스크는 필수로 담당자를 지정 하셔야 합니다.", "알림", MessageBox.INFORMATION);
					return false;
				}
			}
			else if(subTaskNode.toString().equals(APPROVE)){
				if(subTaskNode.isLeaf()){
					MessageBox.post(APPROVE + " 타스크는 필수로 담당자를 지정 하셔야 합니다.", "알림", MessageBox.INFORMATION);
					return false;
				}
			}
			//			else if(subTaskNode.toString().equals(DISTRIBUTION)){
			//				if(subTaskNode.isLeaf()){
			//					MessageBox.post(DISTRIBUTION + " 타스크는 필수로 담당자를 지정 하셔야 합니다.", "알림", MessageBox.INFORMATION);
			//					return false;
			//				}
			//			}
		}
		return true;
	}

	@Override
	protected JPanel getUIPanel() {
		return mainPanel;
	}

	@Override
	public boolean confirmCheck() {
		return true;
	}
}
