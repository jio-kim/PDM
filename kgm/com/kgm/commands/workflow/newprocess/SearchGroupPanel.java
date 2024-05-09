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
 * �׷��� �˻��ϰ� �߰�/���� �ϴ� �г�
 * ���� �� Role�� �߰� �� �� ������, Group�� �߰��� �� ����.
 * @Copyright : S-PALM
 * @author   : ������
 * @since    : 2012. 3. 23.
 * Package ID : com.pungkang.commands.workflow.newprocess.SearchGroupPanel.java
 */
@SuppressWarnings({"unused", "rawtypes"})
public class SearchGroupPanel extends JPanel implements ActionListener{
	/** */
	private static final long serialVersionUID = 1L;

	private Registry registry = Registry.getRegistry(this);

	/** ������ �г� */
	private OrgTreePanel orgTreePanel;

	/** Role �߰� ��ư */
	private JButton addRoleButton = new JButton(registry.getString("Add.TEXT"), registry.getImageIcon("GroupAdd.ICON"));

	/** Role ���� ��ư */
	private JButton removeRoleButton = new JButton(registry.getString("Remove.TEXT"), registry.getImageIcon("GroupRemove.ICON"));

	/** ���� ���ø� Ʈ�� */
	private JTree workflowTemplateTree;

	/** ���� ���ø� Ʈ���� */
	private DefaultTreeModel resourceTreeModel;

	/** ���� ���ø� Ʈ������ ���� �� Ÿ��ũ ��� */
	private DefaultMutableTreeNode selectedTaskNode;

	/** ���� ��û ���̾�α� */
	private NewProcessDialog dialog;

	/** ������ Ʈ�� */
	private OrganizationTree orgTree;

	public SearchGroupPanel(TCSession session, NewProcessDialog dialog, JTree resourceTree, DefaultTreeModel resourceTreeModel) {
		super(new VerticalLayout());
		this.dialog = dialog;
		this.workflowTemplateTree = resourceTree;
		this.resourceTreeModel = resourceTreeModel;

		setBackground(Color.WHITE);

		orgTreePanel = new OrgTreePanel(session);

		// Ư�� Node��  ��Ʈ�� �ؼ� Tree�� ���� �ϰ� �Ǹ�
		// Tree�� Expansion �� ��� ���� �߻� : ���׷� ���� ��.
		// ���� ��¿�� ���� �Ʒ��� ���� �������� (��)ǳ�� ��常 �����ϰ� Ʈ���� �� ���� �� 
		orgTree = orgTreePanel.getOrgTree();
		orgTree.loadAllNodes(orgTree.getRootNode());
		Object[] nodes = orgTree.getRootNode().getChildNodes();
		if(nodes != null){
			for(int i=0; i<nodes.length; i++){
				if(!nodes[i].toString().equals("(��)ǳ��")){
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
					MessageBox.post(dialog, "�߰� �� Task�� ���� ���� �Ͻñ� �ٶ��ϴ�.", registry.getString("Message.TITLE"), MessageBox.INFORMATION);
					return;
				}

				selectedTaskNode = (DefaultMutableTreeNode)(workflowTemplateTree.getSelectionPath().getLastPathComponent());
				if(selectedTaskNode.toString().equals("�ۼ���")){
					MessageBox.post(dialog, "�ۼ��ڿ��� User�� �Ҵ� �� �� �����ϴ�.", registry.getString("Message.TITLE"), MessageBox.INFORMATION);
					return;

				}

				if(orgTree.getSelectionCount() == 0){
					MessageBox.post(dialog, "User�� ���� �� �ּ���.", registry.getString("Message.TITLE"), MessageBox.INFORMATION);
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
						if(!selectedTaskNode.toString().equals("����")){
							MessageBox.post(dialog, "���� Ÿ��ũ���� ���� ���� �� �� �ֽ��ϴ�.", registry.getString("Message.TITLE"), MessageBox.INFORMATION);
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
						MessageBox.post(dialog, "�μ��� ���� �Ͻ� �� �����ϴ�. �μ� ������ ������/������ Role�� �ϳ��� ���� �� �ּ���.", registry.getString("Message.TITLE"), MessageBox.INFORMATION);
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
