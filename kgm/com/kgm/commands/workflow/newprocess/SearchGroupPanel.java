package com.kgm.commands.workflow.newprocess;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.teamcenter.rac.aif.common.AIFTreeNode;
import com.teamcenter.rac.common.organization.OrgObject;
import com.teamcenter.rac.common.organization.OrgTreePanel;
import com.teamcenter.rac.common.organization.OrganizationTree;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.VerticalLayout;

/**
 * 그룹을 검색하고 추가/삭제 하는 패널
 * 유저 및 Role은 추가 할 수 있으나, Group은 추가할 수 없다.
 * @Copyright : S-PALM
 * @author   : 이정건
 * @since    : 2012. 3. 23.
 * Package ID : com.pungkang.commands.workflow.newprocess.SearchGroupPanel.java
 */
@SuppressWarnings({"unused", "rawtypes"})
public class SearchGroupPanel extends JPanel implements ActionListener{
	/** */
	private static final long serialVersionUID = 1L;

	private Registry registry = Registry.getRegistry(this);

	/** 조직도 패널 */
	private OrgTreePanel orgTreePanel;

	/** Role 추가 버튼 */
	private JButton addRoleButton = new JButton(registry.getString("Add.TEXT"), registry.getImageIcon("GroupAdd.ICON"));

	/** Role 삭제 버튼 */
	private JButton removeRoleButton = new JButton(registry.getString("Remove.TEXT"), registry.getImageIcon("GroupRemove.ICON"));

	/** 결재 템플릿 트리 */
	private JTree workflowTemplateTree;

	/** 결재 템플릿 트리모델 */
	private DefaultTreeModel resourceTreeModel;

	/** 결재 템플릿 트리에서 선택 한 타스크 노드 */
	private DefaultMutableTreeNode selectedTaskNode;

	/** 결재 요청 다이얼로그 */
	private NewProcessDialog dialog;

	/** 조직도 트리 */
	private OrganizationTree orgTree;

	public SearchGroupPanel(TCSession session, NewProcessDialog dialog, JTree resourceTree, DefaultTreeModel resourceTreeModel) {
		super(new VerticalLayout());
		this.dialog = dialog;
		this.workflowTemplateTree = resourceTree;
		this.resourceTreeModel = resourceTreeModel;

		setBackground(Color.WHITE);

		orgTreePanel = new OrgTreePanel(session);

		// 특정 Node를  루트로 해서 Tree를 생성 하게 되면
		// Tree를 Expansion 할 경우 에러 발생 : 버그로 추정 됨.
		// 따라서 어쩔수 없이 아래와 같이 수동으로 (주)풍강 노드만 제외하고 트리를 재 구성 함 
		orgTree = orgTreePanel.getOrgTree();
		orgTree.loadAllNodes(orgTree.getRootNode());
		Object[] nodes = orgTree.getRootNode().getChildNodes();
		if(nodes != null){
			for(int i=0; i<nodes.length; i++){
				if(!nodes[i].toString().equals("(주)풍강")){
					orgTree.removeNode((AIFTreeNode)nodes[i]);
				}
			}
		}
		orgTree.expandRow(1);
		orgTreePanel.getRefreshButton().setVisible(false);
		orgTreePanel.getUserRoleGroupPanel().setVisible(false);
//		orgTreePanel.getRoleButton().setVisible(false);
		orgTreePanel.getUserButton().setVisible(false);
		orgTreePanel.getSearchTextField().setVisible(false);
		orgTreePanel.setOpaque(false);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setOpaque(false);
		buttonPanel.add(addRoleButton);
		buttonPanel.add(removeRoleButton);

		add("unbound", orgTreePanel);
		add("bottom.bind.center.center", buttonPanel);

		addListener();

		setPreferredSize(new Dimension(200,200));
	}

	private void addListener(){
		addRoleButton.setActionCommand("ADD");
		addRoleButton.addActionListener(this);
		removeRoleButton.setActionCommand("REMOVE");
		removeRoleButton.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("ADD")){
			try {
				if(workflowTemplateTree.getSelectionCount() == 0){
					MessageBox.post(dialog, "추가 할 Task를 먼저 선택 하시기 바랍니다.", registry.getString("Message.TITLE"), MessageBox.INFORMATION);
					return;
				}

				selectedTaskNode = (DefaultMutableTreeNode)(workflowTemplateTree.getSelectionPath().getLastPathComponent());
				if(selectedTaskNode.toString().equals("작성자")){
					MessageBox.post(dialog, "작성자에는 User를 할당 할 수 없습니다.", registry.getString("Message.TITLE"), MessageBox.INFORMATION);
					return;

				}

				if(orgTree.getSelectionCount() == 0){
					MessageBox.post(dialog, "User를 선택 해 주세요.", registry.getString("Message.TITLE"), MessageBox.INFORMATION);
					return;
				}

				if(orgTree.getSelectedObject() == null){
					return;
				}

				TreePath[] treepath = workflowTemplateTree.getSelectionPaths();

				for(int i = 0; i < treepath.length; i++){
					if(orgTree.getSelectedObject().getType() == OrgObject.GROUPMEMBER){
						TCComponent childComponent = orgTree.getSelectedObject().getComponent();
						DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)treepath[i].getLastPathComponent();
						DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(childComponent);

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
						resourceTreeModel.insertNodeInto(childNode, parentNode, parentNode.getChildCount());
						workflowTemplateTree.expandPath(treepath[i]);
					}
					else if(orgTree.getSelectedObject().getType() == OrgObject.ROLE){
						if(!selectedTaskNode.toString().equals("배포")){
							MessageBox.post(dialog, "배포 타스크에만 팀을 지정 할 수 있습니다.", registry.getString("Message.TITLE"), MessageBox.INFORMATION);
							return;
						}
						int selectedRowCount = orgTree.getSelectionRows()[0];
						orgTree.loadAllNodes(orgTree.getSelectedNode());
						Object[] obj = orgTree.getSelectedNode().getChildNodes();
						AIFTreeNode[] userNodes = new AIFTreeNode[obj.length];

						for(int k=0; k<obj.length; k++){
							userNodes[k] = (AIFTreeNode)obj[k];
						}
						orgTree.setSelectedNode(userNodes);

						OrgObject[] orgObjects = orgTree.getSelectedObjects();

						DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)treepath[i].getLastPathComponent();

						for(int j=0; j<orgObjects.length; j++){
							DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(orgObjects[j].getComponent());
							if(parentNode.isRoot()){
								orgTree.clearSelection();
								continue;
							}
							if(!(parentNode.getUserObject() instanceof TCComponentTaskTemplate)){
								orgTree.clearSelection();
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
							resourceTreeModel.insertNodeInto(childNode, parentNode, parentNode.getChildCount());
							workflowTemplateTree.expandPath(treepath[i]);
							orgTree.clearSelection();
						}
					}
					else if(orgTree.getSelectedObject().getType() == OrgObject.GROUP){
						MessageBox.post(dialog, "부서를 선택 하실 수 없습니다. 부서 하위의 설계자/승인자 Role중 하나를 선택 해 주세요.", registry.getString("Message.TITLE"), MessageBox.INFORMATION);
						return;
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		else if(e.getActionCommand().equals("REMOVE")){
			if(workflowTemplateTree.getSelectionCount() == 0){
				MessageBox.post(dialog, registry.getString("Message3"), registry.getString("Message.TITLE"), MessageBox.INFORMATION);
				return;
			}
			TreePath[] treepath = workflowTemplateTree.getSelectionPaths();
			for(int i = 0; i < treepath.length; i++){
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)treepath[i].getLastPathComponent();
				if(selectedNode.getUserObject() instanceof TCComponentTaskTemplate)	{
					continue;
				}
				((DefaultTreeModel)workflowTemplateTree.getModel()).removeNodeFromParent(selectedNode);
			}
		}
	}
}
