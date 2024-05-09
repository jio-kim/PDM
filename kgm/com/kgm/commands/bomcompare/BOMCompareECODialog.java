package com.kgm.commands.bomcompare;

import java.awt.Cursor;
import java.util.ArrayList;

import javax.swing.tree.TreePath;

import org.apache.axis.utils.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.PropertyService;
import com.kgm.common.utils.SYMStringUtil;
import com.kgm.common.utils.StringUtil;
import com.kgm.rac.kernel.SYMCBOMEditData;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.common.TCTypeRenderer;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.pse.PSEApplicationPanel;
import com.teamcenter.rac.pse.common.BOMLineNode;
import com.teamcenter.rac.pse.common.BOMPanel;
import com.teamcenter.rac.pse.common.BOMTreeTable;
import com.teamcenter.rac.util.ConfirmDialog;

/**
 * [SR141007-051][20141007] shcho, Option�� BOMLine�� ���� �������Ͽ� �ѷ��ִ� ��� �߰� (ECO EPL�� ���� BOMLine�� ���Ͽ�)
 * 
 */
public class BOMCompareECODialog extends SYMCAbstractDialog {
    
    /** compare BOM tree */
    private Tree tree;
    /** PSE BOM Target Panel */
    private BOMPanel bomPanel;
    /** PSE BOM Target TreeTable */
    private BOMTreeTable bomTree;
    /** PSE Target BOMLineNode */
    private BOMLineNode targetBOMNode;
    /** PSE Target BOMLine */
    private TCComponentBOMLine targetBOMLine;
    /** PSE Target BOMLine Part ECO No */
    private String ecoNo;
    
    /** Part New Added Color */
    private Color newColor;
    /** Part Old Replaced Color */
    private Color newReplacedColor;
    /** BOM Old Changed Color */
    private Color oldChangedColor;
    /** Part Old Deleted Color */
    private Color delColor;
    /** Part New Changed Color */
    private Color newChangedColor;
    /** Part NOT ECO Color */
    @SuppressWarnings("unused")
	private Color notEcoColor;

    private SYMCRemoteUtil remote = new SYMCRemoteUtil();
    

    public BOMCompareECODialog(Shell parent, BOMPanel bomPanel, BOMTreeTable bomTree, BOMLineNode bomNode) {
        super(parent);
        setBlockOnOpen(false);
        int newStyle = SWT.CLOSE | SWT.RESIZE | SWT.MODELESS;
        setShellStyle(newStyle);
        this.bomPanel = bomPanel;
        this.bomTree = bomTree;
        this.targetBOMNode = bomNode;
        this.targetBOMLine = bomNode.getBOMLine();
        setApplyButtonVisible(false);
        setOKButtonVisible(false);
        String targetRevId = "";
        String latestRevId = "";
        try {
            targetRevId = this.targetBOMNode.getBOMLine().getItemRevision().getProperty("item_revision_id");
            latestRevId = this.targetBOMLine.getItem().getLatestItemRevision().getProperty("item_revision_id");
            
            if(!latestRevId.equals(targetRevId)) {
                int confirm = ConfirmDialog.prompt(parent, "Confirm", "Top BOMLine������('" + targetRevId +"')�� �ֽŹ���('" + latestRevId + "')�� �ƴմϴ�.\n���⸦ �����Ͻðڽ��ϱ�?" );
                if(confirm != 2) {
                    while (!this.getShell().isDisposed()) {
                        if (!this.getShell().getDisplay().readAndDispatch()) {
                            this.getShell().getDisplay().sleep();
                        }
                    }
                    this.getShell().getDisplay().dispose();            
                }
            }
            String viewRevisionRule = this.targetBOMNode.getBOMLine().window().getRevisionRule().toDisplayString();
            if(!"Latest Working".equals(viewRevisionRule)) {
                int confirm = ConfirmDialog.prompt(parent, "Confirm", "���� Window Revision Rule�� 'Latest Working' ���°� �ƴմϴ�.\n���⸦ �����Ͻðڽ��ϱ�?" );
                if(confirm != 2) {
                    while (!this.getShell().isDisposed()) {
                        if (!this.getShell().getDisplay().readAndDispatch()) {
                            this.getShell().getDisplay().sleep();
                        }
                    }
                    this.getShell().getDisplay().dispose();            
                }
            }
        } catch (TCException e) {            
            e.printStackTrace();
        }
       
        
        
    }

    /**
     * initialize control
     * [SR141107-016][20151208][jclee] BOM ������ ��� Part�� Cut�Ǿ��� ��츦 ����Ͽ� Assy�� �ƴϴ��� Open �� �� �ֵ��� ����
     */
    @Override
    protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
        getShell().setText("BOM Compare With ECO EPL");
        Composite parent = new Composite(parentScrolledComposite, SWT.None);
        parent.setLayout(new FillLayout());
        
        tree = new Tree(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER);
        addTreeColumn("Part No", 180);
        addTreeColumn("Part Origin", 25);
        addTreeColumn("Part Rev", 60);
        addTreeColumn("Part Name", 180);
        addTreeColumn("CT", 30);
        addTreeColumn("IN_ECO", 65);
        addTreeColumn("Find No", 65);
        addTreeColumn("S/Mode", 60);
        addTreeColumn("Quantity", 60);
        addTreeColumn("ALT", 40);
        addTreeColumn("SEL", 40);
        addTreeColumn("CAT", 50);
        addTreeColumn("Color", 45);
        addTreeColumn("Color Section", 120);
        addTreeColumn("MODLE CODE", 90);
        addTreeColumn("Shown-On", 90);
        addTreeColumn("Options", 180);
        
        tree.setLinesVisible(true);
        tree.setHeaderVisible(true);
        // Tree Line ����
        tree.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if(e.item == null) {
                    return;
                }
                processNodeSelection((BOMHistoryTreeItem)e.item);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
            
        });
        // ���� ���� ��ġ��
        tree.addListener(SWT.Expand, new Listener() {
            public void handleEvent(Event e) {
                BOMHistoryTreeItem treeItem = (BOMHistoryTreeItem)e.item;
                BOMLineNode parentNode = null;
                
                if(treeItem.getItemCount() == 1 && treeItem.getItem(0).getText().equals("")) {
                    treeItem.getItem(0).dispose();
                    parentNode = treeItem.getBOMNodes().get(0);
                    loadHistoryBOM(parentNode, treeItem);
                }
            }
        });
        
        parent.pack();
        return parent;
    }
    
    /**
     * Tree Column ����
     * @param columnName
     * @param width
     */
    private void addTreeColumn(String columnName, int width) {
        TreeColumn tc = new TreeColumn(this.tree, SWT.BORDER);
        tc.setText(columnName);
        tc.setWidth(width);
        tc.setMoveable(true);
    }
    
    /**
     * Dialog ���� �� control change, History Load
     */
    protected void afterCreateContents() {
        Display display = getShell().getDisplay();
        newColor = new Color(display, 178, 235, 244);   //���Ķ�
        delColor = new Color(display, 255, 178, 217);   //����ȫ
        newReplacedColor = new Color(display, 152, 247, 145);   //���ʷ�
        oldChangedColor = new Color(display, 214, 214, 214);    //ȸ��
        newChangedColor = new Color(display, 255, 255, 72); //�����
        notEcoColor = new Color(display, 197, 111, 193);    //�����
        
        cancelButton.setText("Close");
        cancelButton.redraw();
        try {
            ecoNo = targetBOMLine.getProperty("s7_ECO_NO");
            getShell().setText(getShell().getText() + "-" + ecoNo);
        } catch (TCException e) {
            e.printStackTrace();
        }
        loadHistoryBOM(targetBOMNode, null);
    }
    
    /**
     * History tree line ���� �� PSE �� �ش� BOMLine �� ���õǵ��� �Ѵ�.
     * @param treeItem
     */
    private void processNodeSelection(BOMHistoryTreeItem treeItem) {
        AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
        if(!(application.getApplicationPanel() instanceof PSEApplicationPanel)) {
            return;
        }
        PSEApplicationPanel panel = (PSEApplicationPanel)application.getApplicationPanel();
        BOMPanel currentBOMPanel = panel.getCurrentBOMPanel();
        if(currentBOMPanel == null || !currentBOMPanel.equals(bomPanel)) {
            return;
        }
        
        ArrayList<BOMLineNode> bomNodes = treeItem.getBOMNodes();
        if(bomNodes.size() == 0) {
            return;
        }
        bomTree.clearSelection();
        ArrayList<TreePath> treePaths = new ArrayList<TreePath>();
        for(BOMLineNode bomNode : bomNodes) {
            if(bomNode == null || bomTree.getRowForPath(bomNode.getTreePath()) == -1) {
                continue;
            }
            if(bomTree.isCollapsed((BOMLineNode)bomNode.getParent())) {
                bomTree.expandBelow((BOMLineNode)bomNode.getParent());
            }
            treePaths.add(bomNode.getTreePath());
        }
        if(treePaths.size() > 0) {
            bomTree.setSelectionPaths((TreePath[])treePaths.toArray(new TreePath[treePaths.size()]));
        }
    }
    
    /**
     * ECO History ������ �о� Tree�� ����
     * [SR141107-016][20151208][jclee] BOM ������ ��� Part�� Cut�Ǿ��� ��츦 ����Ͽ� Assy�� �ƴϴ��� Open �� �� �ֵ��� ����
     * 
     * @param bomNode
     * @param parentTreeItem
     */
    private void loadHistoryBOM(final BOMLineNode bomNode, final BOMHistoryTreeItem parentTreeItem) {
        new Job("Load BOM...") {
            @SuppressWarnings("unchecked")
            protected IStatus run(IProgressMonitor pm) {
                // Root Level line
                if(parentTreeItem == null) {
                    getShell().getDisplay().syncExec(new Runnable() {
                        @Override
                        public void run() {
                            getShell().setCursor(getShell().getDisplay().getSystemCursor(Cursor.WAIT_CURSOR));
                            try {
                                TCComponentBOMLine bomLine = bomNode.getBOMLine();
                                BOMHistoryTreeItem root = new BOMHistoryTreeItem(bomLine);
                                root.setImage(TCTypeRenderer.getTypeImage(bomLine.getProperty("bl_rev_object_type"), null));
                                loadHistoryBOM(bomNode, root);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                // Root ���� ���� ����
                else {
                    if(!bomTree.isExpanded(bomNode) && bomNode.hasChildren()) {
                        bomNode.expandNode();
                    }
                    while(!bomTree.isExpanded(bomNode) && bomNode.hasChildren()) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }

//                    SYMCRemoteUtil remote = new SYMCRemoteUtil();
                    final DataSet ds = new DataSet();
                    getShell().getDisplay().syncExec(new Runnable() {
                        @Override
                        public void run() {
                            ds.put("ecoNo", ecoNo);
                            ds.put("parentNo", parentTreeItem.getText(0));
                            ds.put("parentRev", parentTreeItem.getText(2));
                        }
                    });
                    try {
                        final ArrayList<SYMCBOMEditData> children = (ArrayList<SYMCBOMEditData>) remote.execute("com.kgm.service.ECOHistoryService", "selectECOBOMList", ds);
                        // ECO ���� ��ȸ
                        ArrayList<SYMCBOMEditData> ecoDeleteList = getDeleteEcoDataList(children);
                        int bomChildCnt = bomNode.getChildCount();                        
                        for(int i = 0 ; i < bomChildCnt ; i++) {
                            BOMLineNode childNode = (BOMLineNode)bomNode.getChildAt(i);
                            TCComponentBOMLine bomLine = childNode.getBOMLine();
                            
                            // ����(CT ='D') ECO ECO ITEM �߰�
                            if(ecoDeleteList.size() > 0) {
                                createDeleteBOMLineItem(parentTreeItem, childNode, ecoDeleteList);
                            }
                            // FIXME:  BOM COMPARE ���� ����
                            // 1. ECO_BOM_LIST ���̺��� TC BOM�������� ���� 
                            // 2. ECO�� ���� ���� Part�鵵 ���� Expand�����ϰ� ����
                            createItems(parentTreeItem, childNode, children);
                        }
                        // ����ó��
                        createLastDeleteBOMLineItem(parentTreeItem, ecoDeleteList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        getShell().getDisplay().syncExec(new Runnable() {
                            public void run() {
                                getShell().setCursor(getShell().getDisplay().getSystemCursor(Cursor.DEFAULT_CURSOR));
                            }
                        });
                    }
                    
                    
                    
                    /***************************************************
                     
                    SYMCRemoteUtil remote = new SYMCRemoteUtil();
                    final DataSet ds = new DataSet();
                    getShell().getDisplay().syncExec(new Runnable() {
                        @Override
                        public void run() {
                            ds.put("ecoNo", ecoNo);
                            ds.put("parentNo", parentTreeItem.getText(0));
                            ds.put("parentRev", parentTreeItem.getText(2));
                        }
                    });
                    // Child Item ����
                    try {
                        final ArrayList<SYMCBOMEditData> children = (ArrayList<SYMCBOMEditData>) remote.execute("com.kgm.service.ECOHistoryService", "selectECOBOMList", ds);
                        if(children == null) {
                            return Status.CANCEL_STATUS;
                        }
                        getShell().getDisplay().syncExec(new Runnable() {
                            @Override
                            public void run() {
                                for(final SYMCBOMEditData bomData : children) {
                                    // ���� �� Item
                                    BOMHistoryTreeItem oldChild = null;
                                    if(bomData.getPartNoOld() != null && !bomData.getPartNoOld().equals("")) {
                                        try {
                                            oldChild = new  BOMHistoryTreeItem(parentTreeItem);
                                            oldChild.loadProperties(bomData, true);
                                            oldChild.setImage(TCTypeRenderer.getTypeImage(bomData.getPartTypeOld() + "Revision", "ItemRevision"));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    // ���� �� Item
                                    BOMHistoryTreeItem newChild = null;
                                    if(bomData.getPartNoNew() != null && !bomData.getPartNoNew().equals("")) {
                                        try {
                                             newChild = new  BOMHistoryTreeItem(parentTreeItem);
                                             newChild.loadProperties(bomData, false);
                                             newChild.setImage(TCTypeRenderer.getTypeImage(bomData.getPartTypeNew() + "Revision", "ItemRevision"));
                                             String bomUids = bomData.getOccUid();
                                             TCComponentBOMLine childLine = null;
                                             int pos = 0;
                                             while(pos < bomUids.length()) {
                                                 String occUid = bomUids.substring(pos, pos + 14);
                                                 BOMLineNode childNode = nodeMap.get(occUid);
                                                 newChild.addBOMNode(childNode);
                                                 childLine = childNode.getBOMLine();
                                                 pos = pos + 14;
                                             }
                                             //if(childLine != null && childLine.hasChildren() && ecoNo.equals(childLine.getProperty("s7_ECO_NO"))) {
                                             // FIXME: ECO�� ���� ���� Child�� Expand�� �� �ְ� ����
                                             if(childLine != null && childLine.hasChildren()) {
                                                 new BOMHistoryTreeItem(newChild);
                                             }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    // Color ����
                                    String ct = nullToString(bomData.getChangeType());
                                    if(ct.equals("D")) {
                                        oldChild.setBackground(delColor);
                                    } else if(ct.equals("N2")) {
                                        newChild.setBackground(newColor);
                                    } else if(ct.equals("N1")) {
                                        oldChild.setBackground(oldChangedColor);
                                        newChild.setBackground(newReplacedColor);
                                    } else if(ct.startsWith("F")) {
                                        if(oldChild != null) {
                                            oldChild.setBackground(oldChangedColor);
                                            newChild.setBackground(newReplacedColor);
                                        } else {
                                            newChild.setBackground(newColor);
                                        }
                                    } else if(ct.startsWith("R")) {
                                        oldChild.setBackground(oldChangedColor);
                                        newChild.setBackground(newChangedColor);
                                    }
                                }
                                parentTreeItem.setExpanded(true);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        getShell().getDisplay().syncExec(new Runnable() {
                            public void run() {
                                getShell().setCursor(getShell().getDisplay().getSystemCursor(Cursor.DEFAULT_CURSOR));
                            }
                        });
                    }
                    
                    ***************************************************/
                }

                while(!bomTree.isExpanded(bomNode)) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
                return Status.OK_STATUS;
            }

            private void createItems(BOMHistoryTreeItem parentTreeItem, BOMLineNode childNode, ArrayList<SYMCBOMEditData> children) throws Exception {
                createEcoBOMLineItem(parentTreeItem, childNode, children);
                
            }
            
            private void createEcoBOMLineItem(final BOMHistoryTreeItem parentTreeItem, final BOMLineNode childNode, final ArrayList<SYMCBOMEditData> children) throws Exception {        
                final String childNodeOccPuid = childNode.getBOMLine().getProperty("bl_occurrence_uid");             
                getShell().getDisplay().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        try {                           
                            SYMCBOMEditData bomData = getEcoBOMData(childNodeOccPuid, children);
                            if(bomData != null) {
                            	// ���� �� Item
                                BOMHistoryTreeItem oldChild = null;
                                // ���� �� Item
                                BOMHistoryTreeItem newChild = null;
                            	//String bomUids = bomData.getOccUid();
                            	String ct = nullToString(bomData.getChangeType());
                                // OLD Tree ITEM����
                                if(!StringUtils.isEmpty(bomData.getPartNoOld())) {
                                    oldChild = new  BOMHistoryTreeItem(parentTreeItem);
                                    oldChild.loadProperties(bomData, true);
                                    oldChild.setImage(TCTypeRenderer.getTypeImage(bomData.getPartTypeOld() + "Revision", "ItemRevision"));
                                }   
                                // New Tree ITEM����
                                if(!StringUtils.isEmpty(bomData.getPartNoNew())) {
                                    newChild = new  BOMHistoryTreeItem(parentTreeItem);
                                    // IN_ECO�� ECO_BOM_LIST ���̺� �����͸� ������� �ʰ� ������ BOMLine �Ӽ� �����͸� ����Ѵ�.
                                    bomData.setEcoNo(PropertyService.getStringValue(childNode.getBOMLine().getTCProperty("S7_IN_ECO")));                                    
                                    newChild.loadProperties(bomData, false);
                                    newChild.setImage(TCTypeRenderer.getTypeImage(bomData.getPartTypeNew() + "Revision", "ItemRevision"));
                                    newChild.addBOMNode(childNode);
                                    TCComponentBOMLine childLine = null;
                                    childLine = childNode.getBOMLine();
//                                    if(childLine != null && childLine.hasChildren()) {
                                    // [SR151221-007][20151222][jclee] ���� BOM Line�� ���� Part�� ������ ECO �󿡼� ������ Cut�� EPL�� ������ ��� Tree�� Exand�� �� �ֵ��� ���� ����
                                    if((childLine != null && childLine.hasChildren()) || hasDeleteChildren(childLine)) {
                                    	BOMHistoryTreeItem ti = new BOMHistoryTreeItem(newChild);
                                    }
                                }
                                // Color ����                                        
                                if(ct.equals("N2")) {
                                    newChild.setBackground(newColor);
                                } else if(ct.equals("N1")) {
                                    oldChild.setBackground(oldChangedColor);
                                    newChild.setBackground(newReplacedColor);
                                } else if(ct.startsWith("F")) {
                                    if(oldChild != null) {
                                        oldChild.setBackground(oldChangedColor);
                                        newChild.setBackground(newReplacedColor);
                                    } else {
                                        newChild.setBackground(newColor);
                                    }
                                } else if(ct.startsWith("R")) {
                                    oldChild.setBackground(oldChangedColor);
                                    newChild.setBackground(newChangedColor);
                                } else {
                                    createNotEcoBOMLineItem(parentTreeItem, childNode);
                                }                                
                            } 
                            // ECO�� ���� BOMLine
                            else {
                                createNotEcoBOMLineItem(parentTreeItem, childNode);
                            }
                            parentTreeItem.setExpanded(true);
                        
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                    
                    /**
                     * ���� BOM Line�� ������ ECO �� Cut�� EPL�� �����ϴ��� Ȯ�� 
                     * @param childLine
                     * @return
                     * @throws Exception
                     */
                    @SuppressWarnings("unchecked")
					private boolean hasDeleteChildren(TCComponentBOMLine childLine) throws Exception {
                    	boolean hasDeleteChildren = false;
                    	DataSet ds = new DataSet();
                    	
                    	ds.put("ecoNo", ecoNo);
                        ds.put("parentNo", childLine.getItem().getProperty("item_id"));
                        ds.put("parentRev", childLine.getItemRevision().getProperty("item_revision_id"));
                        
                    	ArrayList<SYMCBOMEditData> children = (ArrayList<SYMCBOMEditData>) remote.execute("com.kgm.service.ECOHistoryService", "selectECOBOMList", ds);

                    	for (int inx = 0; inx < children.size(); inx++) {
							SYMCBOMEditData child = children.get(inx);
							String sCT = child.getChangeType();
							
							if (sCT != null && sCT.equals("D")) {
								hasDeleteChildren = true;
								break;
							}
						}
                    	
                    	return hasDeleteChildren;
                    }

					private SYMCBOMEditData getEcoBOMData(String childNodeOccPuid, ArrayList<SYMCBOMEditData> children) {						                          
						 for(final SYMCBOMEditData bomData : children) {
                             String bomUids = bomData.getOccUid();
                             int pos = 0;    
							 while(pos < bomUids.length()) {
	                        	 String occUid = bomUids.substring(pos, pos + 14);                                   
	                             if(occUid.equals(childNodeOccPuid)) {
	                                 String ct = StringUtil.nullToString(bomData.getChangeType());
	                                 //FIXME :: [SR����][20141006] shcho, ChangeType�� "XX" �̰ų� Null�� ��쿡�� �񱳸� ���� �ʴ´�. (XX, Null �ΰ�� BOMCompare �� ����� �ƴ�)
	                            	 if(ct.equals("XX") || ct.equals("")) {
	                            	     return null;
	                            	 } else {
	                            	     return bomData;
	                            	 }
	                             }
	                             pos = pos + 14;
	                         }
						 }
                         return null;
					}
                });
                
            }
            
            /**
             * ECO�� ���Ե������� BOMLine Part
             * 
             * @method createNotEcoBOMLineItem 
             * @date 2014. 9. 23.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            private void createNotEcoBOMLineItem(BOMHistoryTreeItem parentTreeItem, BOMLineNode childNode) throws Exception {
                BOMHistoryTreeItem child = new  BOMHistoryTreeItem(parentTreeItem);
                SYMCBOMEditData bomData = new SYMCBOMEditData();
                bomData.setPartNoNew(childNode.getBOMLine().getProperty("bl_item_item_id"));
                bomData.setPartOriginNew(childNode.getBOMLine().getItemRevision().getProperty("s7_PART_TYPE"));
                bomData.setPartRevNew(childNode.getBOMLine().getItemRevision().getProperty("item_revision_id"));
                bomData.setPartNameNew(childNode.getBOMLine().getItem().getProperty("object_name"));
                bomData.setChangeType("");                
                bomData.setEcoNo(PropertyService.getStringValue(childNode.getBOMLine().getTCProperty("S7_IN_ECO")));
                bomData.setSeqNew(childNode.getBOMLine().getProperty("bl_sequence_no"));
                bomData.setSupplyModeNew(childNode.getBOMLine().getProperty("S7_SUPPLY_MODE"));                
                bomData.setQtyNew(childNode.getBOMLine().getProperty("bl_quantity"));
                bomData.setAltNew(childNode.getBOMLine().getProperty("S7_ALTER_PART"));                
                bomData.setSelNew(childNode.getBOMLine().getItemRevision().getProperty("s7_SELECTIVE_PART"));
                bomData.setCatNew(childNode.getBOMLine().getItemRevision().getProperty("s7_REGULATION"));
                bomData.setColorIdNew(childNode.getBOMLine().getItemRevision().getProperty("s7_COLOR"));
                bomData.setColorSectionNew(childNode.getBOMLine().getItemRevision().getProperty("s7_COLOR_ID"));
                bomData.setModuleCodeNew(childNode.getBOMLine().getProperty("S7_MODULE_CODE"));
                if(childNode.getBOMLine().getItemRevision().getTCProperty("s7_SHOWN_PART_NO") != null) {
                    TCComponentItem shownNoPart = (TCComponentItem)childNode.getBOMLine().getItemRevision().getTCProperty("s7_SHOWN_PART_NO").getModelObjectValue();
                    if(shownNoPart != null) {
                        bomData.setShownOnNew(shownNoPart.getProperty("item_id"));
                    }
                }
                //[SR141007-051][20141007] shcho, Option�� BOMLine�� ���� �������Ͽ� �ѷ��ִ� ��� �߰� (ECO EPL�� ���� BOMLine�� ���Ͽ�)
                bomData.setVcNew(SYMStringUtil.convertToSimple(childNode.getBOMLine().getProperty("bl_occ_mvl_condition")));
                child.loadProperties(bomData, false);
                child.addBOMNode(childNode);
                child.setImage(TCTypeRenderer.getTypeImage(bomData.getPartTypeNew() + "Revision", "ItemRevision"));
                //child.setBackground(notEcoColor);
                
                if(childNode.getBOMLine() != null && childNode.getBOMLine().hasChildren()) {
                    new BOMHistoryTreeItem(child);
                }
            }
        }.schedule();
    }
    
    /**
     * ���� ECO BOMLine Part (Target BOMWindow�� ����������� ���� BOMLine)
     * 
     * @method createDeleteBOMLineItem 
     * @date 2014. 9. 23.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
//    private void createDeleteBOMLineItem(final BOMHistoryTreeItem parentTreeItem, ArrayList<SYMCBOMEditData> children) throws Exception {
//        for(final SYMCBOMEditData bomData : children) {
//            final String bomUids = bomData.getOccUid();
//            String ct = nullToString(bomData.getChangeType());
//            if("D".equals(ct)){               
//                getShell().getDisplay().syncExec(new Runnable() {
//                    public void run() {
//                        try {                            
//                            BOMHistoryTreeItem child = new  BOMHistoryTreeItem(parentTreeItem);            
//                            child.loadProperties(bomData, true);
//                            child.setImage(TCTypeRenderer.getTypeImage(bomData.getPartTypeOld() + "Revision", "ItemRevision"));  
//                            child.setBackground(delColor);                            
//                        } catch(Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//            }
//        }        
//    }
    
    /**
     * ECO ���� ��ȸ (CT = 'D')
     * @param children
     * @return
     * @throws Exception
     */
    private ArrayList<SYMCBOMEditData> getDeleteEcoDataList(ArrayList<SYMCBOMEditData> children) throws Exception {
        ArrayList<SYMCBOMEditData> ecoDeleteList = new ArrayList<SYMCBOMEditData>();
        for (int i = 0; i < children.size(); i++) {
            String ct = nullToString(children.get(i).getChangeType());
            if("D".equals(ct)){               
                ecoDeleteList.add(children.get(i));
            }
        }
        return ecoDeleteList;
    }
    
    private void createDeleteBOMLineItem(final BOMHistoryTreeItem parentTreeItem, final BOMLineNode childNode,  ArrayList<SYMCBOMEditData> ecoDeleteList) throws Exception {          
        String seqNo = childNode.getBOMLine().getProperty("bl_sequence_no");
        ArrayList<SYMCBOMEditData> deleteItemList = new ArrayList<SYMCBOMEditData>();
        for(SYMCBOMEditData bomData : ecoDeleteList) {          
            if(StringUtil.nullToString(bomData.getSeqOld()).compareTo(seqNo) <= 0) {
                deleteItemList.add(bomData);              
            } 
        }      
        for(final SYMCBOMEditData bomData : deleteItemList) {                   
            getShell().getDisplay().syncExec(new Runnable() {
                public void run() {
                  try {                            
                      BOMHistoryTreeItem child = new  BOMHistoryTreeItem(parentTreeItem);
                      // IN_ECO No = ""
                      bomData.setEcoNo("");
                      bomData.setEcoNoOld("");                      
                      child.loadProperties(bomData, true);
                      child.setImage(TCTypeRenderer.getTypeImage(bomData.getPartTypeOld() + "Revision", "ItemRevision"));  
                      child.setBackground(delColor);
                  } catch(Exception e) {
                      e.printStackTrace();
                  }
              }
            });      
        }      
        // ��ü ��󿡼� ����. (Item Add�� DELETE ITEM Remove)
        for(SYMCBOMEditData bomData : deleteItemList) {        
            ecoDeleteList.remove(bomData);
        }
    }
    
    private void createLastDeleteBOMLineItem(final BOMHistoryTreeItem parentTreeItem, ArrayList<SYMCBOMEditData> ecoDeleteList) throws Exception {
        for(final SYMCBOMEditData bomData : ecoDeleteList) {                   
            getShell().getDisplay().syncExec(new Runnable() {
                public void run() {
                  try {                            
                      BOMHistoryTreeItem child = new  BOMHistoryTreeItem(parentTreeItem);
                      // IN_ECO No = ""
                      bomData.setEcoNo("");
                      bomData.setEcoNoOld("");
                      child.loadProperties(bomData, true);
                      child.setImage(TCTypeRenderer.getTypeImage(bomData.getPartTypeOld() + "Revision", "ItemRevision"));  
                      child.setBackground(delColor);
                  } catch(Exception e) {
                      e.printStackTrace();
                  }
              }
            });      
        }
    }
    
    private String nullToString(String str) {
        if(str == null) {
            return "";
        }
        return str;
    }
    
    @Override
    protected boolean validationCheck() {
        return false;
    }

    @Override
    protected boolean apply() {
        return false;
    }
    
    /**
     * Compare BOMTree Item
     */
    private class BOMHistoryTreeItem extends TreeItem {
        
        /** History mapping PSE BOMLines */
        private ArrayList<BOMLineNode> bomNodes = new ArrayList<BOMLineNode>();
        
        private BOMHistoryTreeItem(TCComponentBOMLine bomLine) throws Exception {
            super(tree, SWT.None);
            setText(0, bomLine.getProperty("bl_item_item_id"));
            setText(2, bomLine.getProperty("bl_rev_item_revision_id"));
        }
        
        private BOMHistoryTreeItem(BOMHistoryTreeItem parent) throws Exception {
            super(parent, SWT.None);
        }
        
        private void loadProperties(SYMCBOMEditData bomData, boolean isOld) throws TCException {
            if(isOld) {
                setText(0, nullToString(bomData.getPartNoOld()));
                setText(1, nullToString(bomData.getPartOriginOld()));
                setText(2, nullToString(bomData.getPartRevOld()));
                setText(3, nullToString(bomData.getPartNameOld()));
                setText(4, nullToString(bomData.getChangeType()));
                setText(5, nullToString(bomData.getEcoNoOld()));
                setText(6, nullToString(bomData.getSeqOld()));
                setText(7, nullToString(bomData.getSupplyModeOld()));
                setText(8, nullToString(bomData.getQtyOld()));
                setText(9, nullToString(bomData.getAltOld()));
                setText(10, nullToString(bomData.getSelOld()));
                setText(11, nullToString(bomData.getCatOld()));
                setText(12, nullToString(bomData.getColorIdOld()));
                setText(13, nullToString(bomData.getColorSectionOld()));
                setText(14, nullToString(bomData.getModuleCodeOld()));
                setText(15, nullToString(bomData.getShownOnOld()));
                setText(16, nullToString(bomData.getVcOld()));
            } else {
                setText(0, nullToString(bomData.getPartNoNew()));
                setText(1, nullToString(bomData.getPartOriginNew()));
                setText(2, nullToString(bomData.getPartRevNew()));
                setText(3, nullToString(bomData.getPartNameNew()));
                setText(4, nullToString(bomData.getChangeType()));
                setText(5, nullToString(bomData.getEcoNo()));
                setText(6, nullToString(bomData.getSeqNew()));
                setText(7, nullToString(bomData.getSupplyModeNew()));
                setText(8, nullToString(bomData.getQtyNew()));
                setText(9, nullToString(bomData.getAltNew()));
                setText(10, nullToString(bomData.getSelNew()));
                setText(11, nullToString(bomData.getCatNew()));
                setText(12, nullToString(bomData.getColorIdNew()));
                setText(13, nullToString(bomData.getColorSectionNew()));
                setText(14, nullToString(bomData.getModuleCodeNew()));
                setText(15, nullToString(bomData.getShownOnNew()));
                setText(16, nullToString(bomData.getVcNew()));
            }
        }
        
        private void addBOMNode(BOMLineNode bomNode) {
            bomNodes.add(bomNode);
        }
        
        private ArrayList<BOMLineNode> getBOMNodes() {
            return bomNodes;
        }
        
        public void checkSubclass() {
        }

    }

}
