package com.kgm.commands.bomcompare;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.tree.TreePath;

import org.apache.axis.utils.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

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
import com.teamcenter.rac.util.MessageBox;

/**
 * [SR141007-051][20141007] shcho, Option에 BOMLine의 값을 컨버팅하여 뿌려주는 기능 추가 (ECO EPL에 없는 BOMLine에 한하여)
 * 
 */
public class BOMCompareECOGridDialog extends SYMCAbstractDialog {
    /** compare BOM tree */
	private Grid grid;
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
    /** Corporate Option */
//    private CorporateOptionData dataCorporateOption;
    private HashMap<String, String> hmCorpOption;

    private SYMCRemoteUtil remote = new SYMCRemoteUtil();

    public BOMCompareECOGridDialog(Shell parent, BOMPanel bomPanel, BOMTreeTable bomTree, BOMLineNode bomNode) {
        super(parent);
        setBlockOnOpen(false);
//        int newStyle = SWT.CLOSE | SWT.RESIZE | SWT.MODELESS;
        int newStyle = SWT.CLOSE | SWT.MODELESS;
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
                int confirm = ConfirmDialog.prompt(parent, "Confirm", "Top BOMLine리비젼('" + targetRevId +"')이 최신버젼('" + latestRevId + "')이 아닙니다.\n보기를 진행하시겠습니까?" );
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
                int confirm = ConfirmDialog.prompt(parent, "Confirm", "현재 Window Revision Rule이 'Latest Working' 상태가 아닙니다.\n보기를 진행하시겠습니까?" );
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
     * [SR141107-016][20151208][jclee] BOM 하위에 모든 Part가 Cut되었을 경우를 대비하여 Assy가 아니더라도 Open 할 수 있도록 수정
     */
    @Override
    protected Composite createDialogPanel(final ScrolledComposite parentScrolledComposite) {
    	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    	getShell().setSize(screenSize.width - 20, screenSize.height - 60);
    	getShell().setLocation(10, 10);
        getShell().setText("BOM Compare With ECO EPL");

        Composite parent = new Composite(parentScrolledComposite, SWT.NONE);
        parent.setLayout(new GridLayout(1, true));
        parent.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        grid = new Grid(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER | SWT.MULTI | SWT.WRAP);
        grid.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        addGridColumn("Part No", 180, true);
        addGridColumn("Part Origin", 25);
        addGridColumn("Part Rev", 60);
        addGridColumn("Part Name", 180);
        addGridColumn("CT", 30);
        addGridColumn("IN_ECO", 65);
        addGridColumn("Find No", 65);
        addGridColumn("S/Mode", 60);
        addGridColumn("Quantity", 60);
        addGridColumn("ALT", 40);
        addGridColumn("SEL", 40);
        addGridColumn("CAT", 50);
        addGridColumn("Color", 45);
        addGridColumn("Color Section", 120);
        addGridColumn("MODLE CODE", 90);
        addGridColumn("Shown-On", 90);
        addGridColumn("Options", 400);
        addGridColumn("Option Desc", 400);
        grid.setLinesVisible(true);
        grid.setHeaderVisible(true);
        grid.setRowsResizeable(true);
        
        grid.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if(e.item == null) {
                    return;
                }
                processNodeSelection_Grid((BOMHistoryGridItem)e.item);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        // 하위 구조 펼치기
        grid.addListener(SWT.Expand, new Listener() {
            public void handleEvent(Event e) {
            	BOMHistoryGridItem gridItem = (BOMHistoryGridItem)e.item;
                BOMLineNode parentNode = null;
                
                if(gridItem.getItemCount() == 1 && gridItem.getItem(0).getText().equals("")) {
                    gridItem.getItem(0).dispose();
                    parentNode = gridItem.getBOMNodes().get(0);
                    loadHistoryBOM_Grid(parentNode, gridItem);
                }
            }
        });
        
        return parent;
    }
    
    private void addGridColumn(String sColumnName, int iWidth) {
    	this.addGridColumn(sColumnName, iWidth, false);
    }
    
    private void addGridColumn(String sColumnName, int iWidth, boolean isTree) {
    	GridColumn gc = new GridColumn(this.grid, SWT.BORDER);
    	gc.setTree(isTree);
    	gc.setText(sColumnName);
    	gc.setWidth(iWidth);
    	gc.setMoveable(true);
    	gc.setWordWrap(true);
    }
    
    /**
     * Dialog 구성 후 control change, History Load
     */
    protected void afterCreateContents() {
        Display display = getShell().getDisplay();
        newColor = new Color(display, 178, 235, 244);   //연파랑
        delColor = new Color(display, 255, 178, 217);   //연분홍
        newReplacedColor = new Color(display, 152, 247, 145);   //연초록
        oldChangedColor = new Color(display, 214, 214, 214);    //회색
        newChangedColor = new Color(display, 255, 255, 72); //노란색
        notEcoColor = new Color(display, 197, 111, 193);    //보라색
        
        cancelButton.setText("Close");
        cancelButton.redraw();
        try {
            ecoNo = targetBOMLine.getProperty("s7_ECO_NO");
            getShell().setText(getShell().getText() + "-" + ecoNo);
        } catch (TCException e) {
            e.printStackTrace();
        }
        loadHistoryBOM_Grid(targetBOMNode, null);
    }
    
    /**
     * History tree line 선택 시 PSE 에 해당 BOMLine 이 선택되도록 한다.
     * @param gridItem
     */
    private void processNodeSelection_Grid(BOMHistoryGridItem gridItem) {
    	AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
    	if(!(application.getApplicationPanel() instanceof PSEApplicationPanel)) {
    		return;
    	}
    	PSEApplicationPanel panel = (PSEApplicationPanel)application.getApplicationPanel();
    	BOMPanel currentBOMPanel = panel.getCurrentBOMPanel();
    	if(currentBOMPanel == null || !currentBOMPanel.equals(bomPanel)) {
    		return;
    	}
    	
    	ArrayList<BOMLineNode> bomNodes = gridItem.getBOMNodes();
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
     * ECO History 내용을 읽어 Tree에 구성
     * [SR141107-016][20151208][jclee] BOM 하위에 모든 Part가 Cut되었을 경우를 대비하여 Assy가 아니더라도 Open 할 수 있도록 수정
     * 
     * @param bomNode
     * @param parentGridItem
     */
	private void loadHistoryBOM_Grid(final BOMLineNode bomNode, final BOMHistoryGridItem parentGridItem) {
		new Job("Load BOM...") {
			@SuppressWarnings("unchecked")
			protected IStatus run(IProgressMonitor pm) {
				// Root Level line
				if (parentGridItem == null) {
					getShell().getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							getShell().setCursor(getShell().getDisplay().getSystemCursor(Cursor.WAIT_CURSOR));
							try {
								TCComponentBOMLine bomLine = bomNode.getBOMLine();
								BOMHistoryGridItem root = new BOMHistoryGridItem(bomLine);
								root.setImage(TCTypeRenderer.getTypeImage(bomLine.getProperty("bl_rev_object_type"), null));
								loadHistoryBOM_Grid(bomNode, root);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				}
				// Root 이하 레벨 구성
				else {
					if (!bomTree.isExpanded(bomNode) && bomNode.hasChildren()) {
						bomNode.expandNode();
					}
					while (!bomTree.isExpanded(bomNode) && bomNode.hasChildren()) {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}

					final DataSet ds = new DataSet();
					getShell().getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							ds.put("ecoNo", ecoNo);
							ds.put("parentNo", parentGridItem.getText(0));
							ds.put("parentRev", parentGridItem.getText(2));
						}
					});
					try {
						final ArrayList<SYMCBOMEditData> children = (ArrayList<SYMCBOMEditData>) remote.execute("com.kgm.service.ECOHistoryService", "selectECOBOMList", ds);
						// ECO 삭제 조회
						ArrayList<SYMCBOMEditData> ecoDeleteList = getDeleteEcoDataList_Grid(children);
						int bomChildCnt = bomNode.getChildCount();
						for (int i = 0; i < bomChildCnt; i++) {
							BOMLineNode childNode = (BOMLineNode) bomNode.getChildAt(i);

							// 삭제(CT ='D') ECO ECO ITEM 추가
							if (ecoDeleteList.size() > 0) {
								createDeleteBOMLineItem_Grid(parentGridItem, childNode, ecoDeleteList);
							}
							// BOM COMPARE 기준 변경
							// 1. ECO_BOM_LIST 테이블에서 TC BOM기준으로 변경
							// 2. ECO에 관계 없는 Part들도 하위 Expand가능하게 변경
							createItems(parentGridItem, childNode, children);
						}
						// 삭제처리
						createLastDeleteBOMLineItem_Grid(parentGridItem, ecoDeleteList);
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
					 * SYMCRemoteUtil remote = new SYMCRemoteUtil(); final DataSet ds = new DataSet(); getShell().getDisplay().syncExec(new Runnable() {
					 * 
					 * @Override public void run() { ds.put("ecoNo", ecoNo); ds.put("parentNo", parentTreeItem.getText(0)); ds.put("parentRev", parentTreeItem.getText(2)); } }); // Child Item 구성 try { final ArrayList<SYMCBOMEditData> children = (ArrayList<SYMCBOMEditData>) remote.execute("com.kgm.service.ECOHistoryService", "selectECOBOMList", ds); if(children == null) { return Status.CANCEL_STATUS; } getShell().getDisplay().syncExec(new Runnable() {
					 * @Override public void run() { for(final SYMCBOMEditData bomData : children) { // 변경 전 Item BOMHistoryTreeItem oldChild = null; if(bomData.getPartNoOld() != null && !bomData.getPartNoOld().equals("")) { try { oldChild = new BOMHistoryTreeItem(parentTreeItem); oldChild.loadProperties(bomData, true); oldChild.setImage(TCTypeRenderer.getTypeImage(bomData.getPartTypeOld() + "Revision", "ItemRevision")); } catch (Exception e) { e.printStackTrace(); } } // 변경 후 Item BOMHistoryTreeItem newChild = null; if(bomData.getPartNoNew() != null && !bomData.getPartNoNew().equals("")) { try { newChild = new BOMHistoryTreeItem(parentTreeItem); newChild.loadProperties(bomData, false); newChild.setImage(TCTypeRenderer.getTypeImage(bomData.getPartTypeNew() + "Revision", "ItemRevision")); String bomUids = bomData.getOccUid(); TCComponentBOMLine childLine = null; int pos = 0; while(pos < bomUids.length()) { String occUid = bomUids.substring(pos, pos + 14); BOMLineNode childNode =
					 *           nodeMap.get(occUid); newChild.addBOMNode(childNode); childLine = childNode.getBOMLine(); pos = pos + 14; } //if(childLine != null && childLine.hasChildren() && ecoNo.equals(childLine.getProperty("s7_ECO_NO"))) { // ECO에 관계 없는 Child도 Expand할 수 있게 변경 if(childLine != null && childLine.hasChildren()) { new BOMHistoryTreeItem(newChild); } } catch (Exception e) { e.printStackTrace(); } } // Color 설정 String ct = nullToString(bomData.getChangeType()); if(ct.equals("D")) { oldChild.setBackground(delColor); } else if(ct.equals("N2")) { newChild.setBackground(newColor); } else if(ct.equals("N1")) { oldChild.setBackground(oldChangedColor); newChild.setBackground(newReplacedColor); } else if(ct.startsWith("F")) { if(oldChild != null) { oldChild.setBackground(oldChangedColor); newChild.setBackground(newReplacedColor); } else { newChild.setBackground(newColor); } } else if(ct.startsWith("R")) { oldChild.setBackground(oldChangedColor);
					 *           newChild.setBackground(newChangedColor); } } parentTreeItem.setExpanded(true); } }); } catch (Exception e) { e.printStackTrace(); } finally { getShell().getDisplay().syncExec(new Runnable() { public void run() { getShell().setCursor(getShell().getDisplay().getSystemCursor(Cursor.DEFAULT_CURSOR)); } }); }
					 ***************************************************/
				}

				while (!bomTree.isExpanded(bomNode)) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
				
				return Status.OK_STATUS;
			}

			private void createItems(BOMHistoryGridItem parentGridItem, BOMLineNode childNode, ArrayList<SYMCBOMEditData> children) throws Exception {
				createEcoBOMLineItem(parentGridItem, childNode, children);

			}

			private void createEcoBOMLineItem(final BOMHistoryGridItem parentGridItem, final BOMLineNode childNode, final ArrayList<SYMCBOMEditData> children) throws Exception {
				final String childNodeOccPuid = childNode.getBOMLine().getProperty("bl_occurrence_uid");
				getShell().getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						try {
							SYMCBOMEditData bomData = getEcoBOMData(childNodeOccPuid, children);
							if (bomData != null) {
								// 변경 전 Item
								BOMHistoryGridItem oldChild = null;
								// 변경 후 Item
								BOMHistoryGridItem newChild = null;
								// String bomUids = bomData.getOccUid();
								String ct = nullToString(bomData.getChangeType());
								// OLD Grid ITEM생성
								if (!StringUtils.isEmpty(bomData.getPartNoOld())) {
									oldChild = new BOMHistoryGridItem(parentGridItem);
									oldChild.loadProperties(bomData, true);
									oldChild.setImage(TCTypeRenderer.getTypeImage(bomData.getPartTypeOld() + "Revision", "ItemRevision"));
								}
								// New Grid ITEM생성
								if (!StringUtils.isEmpty(bomData.getPartNoNew())) {
									newChild = new BOMHistoryGridItem(parentGridItem);
									// IN_ECO를 ECO_BOM_LIST 테이블 데이터를 사용하지 않고 팀센터 BOMLine 속성 데이터를 사용한다.
									bomData.setEcoNo(PropertyService.getStringValue(childNode.getBOMLine().getTCProperty("S7_IN_ECO")));
									newChild.loadProperties(bomData, false);
									newChild.setImage(TCTypeRenderer.getTypeImage(bomData.getPartTypeNew() + "Revision", "ItemRevision"));
									newChild.addBOMNode(childNode);
									TCComponentBOMLine childLine = null;
									childLine = childNode.getBOMLine();
									// if(childLine != null && childLine.hasChildren()) {
									// [SR151221-007][20151222][jclee] 현재 BOM Line에 하위 Part가 없더라도 ECO 상에서 하위가 Cut된 EPL이 존재할 경우 Tree를 Exand할 수 있도록 로직 수정
									if ((childLine != null && childLine.hasChildren()) || hasDeleteChildren(childLine)) {
										new BOMHistoryGridItem(newChild);
									}
								}
								// Color 설정
								if (ct.equals("N2")) {
									newChild.setBackground(newColor);
								} else if (ct.equals("N1")) {
									oldChild.setBackground(oldChangedColor);
									newChild.setBackground(newReplacedColor);
								} else if (ct.startsWith("F")) {
									if (oldChild != null) {
										oldChild.setBackground(oldChangedColor);
										newChild.setBackground(newReplacedColor);
									} else {
										newChild.setBackground(newColor);
									}
								} else if (ct.startsWith("R")) {
									oldChild.setBackground(oldChangedColor);
									newChild.setBackground(newChangedColor);
								} else {
									createNotEcoBOMLineItem(parentGridItem, childNode);
								}
							}
							// ECO에 없는 BOMLine
							else {
								createNotEcoBOMLineItem(parentGridItem, childNode);
							}
							parentGridItem.setExpanded(true);
							
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					/**
					 * 현재 BOM Line의 하위에 ECO 상 Cut된 EPL이 존재하는지 확인
					 * 
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
						for (final SYMCBOMEditData bomData : children) {
							String bomUids = bomData.getOccUid();
							int pos = 0;
							while (pos < bomUids.length()) {
								String occUid = bomUids.substring(pos, pos + 14);
								if (occUid.equals(childNodeOccPuid)) {
									String ct = StringUtil.nullToString(bomData.getChangeType());
									// [SR없음][20141006] shcho, ChangeType이 "XX" 이거나 Null인 경우에는 비교를 하지 않는다. (XX, Null 인경우 BOMCompare 비교 대상이 아님)
									if (ct.equals("XX") || ct.equals("")) {
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
			 * ECO에 포함되지않은 BOMLine Part
			 * 
			 * @method createNotEcoBOMLineItem
			 * @date 2014. 9. 23.
			 * @param
			 * @return void
			 * @exception
			 * @throws
			 * @see
			 */
			private void createNotEcoBOMLineItem(BOMHistoryGridItem parentGridItem, BOMLineNode childNode) throws Exception {
				BOMHistoryGridItem child = new BOMHistoryGridItem(parentGridItem);
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
				if (childNode.getBOMLine().getItemRevision().getTCProperty("s7_SHOWN_PART_NO") != null) {
					TCComponentItem shownNoPart = (TCComponentItem) childNode.getBOMLine().getItemRevision().getTCProperty("s7_SHOWN_PART_NO").getModelObjectValue();
					if (shownNoPart != null) {
						bomData.setShownOnNew(shownNoPart.getProperty("item_id"));
					}
				}
				// [SR141007-051][20141007] shcho, Option에 BOMLine의 값을 컨버팅하여 뿌려주는 기능 추가 (ECO EPL에 없는 BOMLine에 한하여)
				bomData.setVcNew(SYMStringUtil.convertToSimple(childNode.getBOMLine().getProperty("bl_occ_mvl_condition")));
				child.loadProperties(bomData, false);
				child.addBOMNode(childNode);
				child.setImage(TCTypeRenderer.getTypeImage(bomData.getPartTypeNew() + "Revision", "ItemRevision"));
				// child.setBackground(notEcoColor);

				if (childNode.getBOMLine() != null && childNode.getBOMLine().hasChildren()) {
					new BOMHistoryGridItem(child);
				}
			}
		}.schedule();
	}
    
    /**
     * ECO 삭제 조회 (CT = 'D')
     * @param children
     * @return
     * @throws Exception
     */
    private ArrayList<SYMCBOMEditData> getDeleteEcoDataList_Grid(ArrayList<SYMCBOMEditData> children) throws Exception {
        ArrayList<SYMCBOMEditData> ecoDeleteList = new ArrayList<SYMCBOMEditData>();
        for (int i = 0; i < children.size(); i++) {
            String ct = nullToString(children.get(i).getChangeType());
            if("D".equals(ct)){               
                ecoDeleteList.add(children.get(i));
            }
        }
        return ecoDeleteList;
    }
    
    private void createDeleteBOMLineItem_Grid(final BOMHistoryGridItem parentGridItem, final BOMLineNode childNode,  ArrayList<SYMCBOMEditData> ecoDeleteList) throws Exception {          
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
                	  BOMHistoryGridItem child = new BOMHistoryGridItem(parentGridItem);
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
        // 전체 대상에서 삭제. (Item Add한 DELETE ITEM Remove)
        for(SYMCBOMEditData bomData : deleteItemList) {        
            ecoDeleteList.remove(bomData);
        }
    }
    
    private void createLastDeleteBOMLineItem_Grid(final BOMHistoryGridItem parentGridItem, ArrayList<SYMCBOMEditData> ecoDeleteList) throws Exception {
        for(final SYMCBOMEditData bomData : ecoDeleteList) {                   
            getShell().getDisplay().syncExec(new Runnable() {
                public void run() {
                  try {                            
                	  BOMHistoryGridItem child = new  BOMHistoryGridItem(parentGridItem);
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
    private class BOMHistoryGridItem extends GridItem {
    	
    	/** History mapping PSE BOMLines */
    	private ArrayList<BOMLineNode> bomNodes = new ArrayList<BOMLineNode>();
    	
    	private BOMHistoryGridItem(TCComponentBOMLine bomLine) throws Exception {
    		super(grid, SWT.MULTI | SWT.WRAP);
    		setText(0, bomLine.getProperty("bl_item_item_id"));
    		setText(2, bomLine.getProperty("bl_rev_item_revision_id"));
    	}
    	
    	private BOMHistoryGridItem(BOMHistoryGridItem parent) throws Exception {
    		super(parent, SWT.MULTI | SWT.WRAP);
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
    			
    			String sVC = nullToString(bomData.getVcOld()).replace("@", "\n").replace(" or ", "\n").replace(" OR ", "\n").toUpperCase();
    			setText(16, sVC);
    			setText(17, getOptionNameStatement(sVC));
    			
    			setHeight(getHeight() * sVC.split("\n").length);
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
    			
    			String sVC = nullToString(bomData.getVcNew()).replace("@", "\n").replace(" or ", "\n").replace(" OR ", "\n").toUpperCase();
    			setText(16, sVC);
    			setText(17, getOptionNameStatement(sVC));
    			
    			setHeight(getHeight() * sVC.split("\n").length);
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
    	
    	public void setBackground(Color color) {
    		for (int inx = 0; inx <= 17; inx++) {
    			setBackground(inx, color);
			}
    	}
    }
    
    /**
     * Option을 Description으로 해석 후 반환
     * [SR160316-021][20160321][jclee] 전사 Option Dataset이 아니라 전사 Option Desc을 저장해둔 DB에서 직접 값을 가져오도록 변경 (Dataset에는 누락된 Option이 존재할 가능성이 있음)
     * @param sVC
     * @return
     * @throws  
     */
    private String getOptionNameStatement(String sVC) {
//    	try {
//    		if (dataCorporateOption == null) {
//    			dataCorporateOption = new CorporateOptionData();
//    		}
//    		
//    		return dataCorporateOption.getOptionNameStatement(sVC, "●");
//		} catch (Exception e) {
//			MessageBox.post(e);
//		}
//    	
//    	return "";
    	
    	StringBuffer sbResult = new StringBuffer();
    	String[] sSplittedVC = null;
    	String sSpace = " ";
    	String sSeparate = "●";
    	
    	if (sVC == null || sVC.equals("") || sVC.length() == 0) {
			return "";
		} else {
			sSplittedVC = sVC.toUpperCase().split("\n");
		}
    	
    	try {
    		if (hmCorpOption == null || hmCorpOption.isEmpty()) {
    			hmCorpOption = new HashMap<String, String>();
    			
    			DataSet ds = new DataSet();
    			ds.put("code_name", null);
    			
    			@SuppressWarnings("unchecked")
    			ArrayList<HashMap<String, String>> alResult = (ArrayList<HashMap<String, String>>) remote.execute("com.kgm.service.VariantService", "getVariantValueDesc", ds);
    			if (!(alResult.size() > 0)) {
    				return "";  
    			}
    			
    			for (int inx = 0; inx < alResult.size(); inx++) {
    				HashMap<String, String> hmResult = alResult.get(inx);
    				String sCode = "";
    				String sDesc = "";
    				
    				sCode = hmResult.get("CODE_NAME");
    				sDesc = hmResult.get("CODE_DESC");
    				
    				hmCorpOption.put(sCode, sDesc);
    			}
			}
    		
    		for (int inx = 0; inx < sSplittedVC.length; inx++) {
        		String[] sAndSplittedVC = sSplittedVC[inx].split(" AND ");
        		StringBuffer sbResultTemp = new StringBuffer();	// \n(OR)으로 Split된 Line 별 변환 결과
        		
        		for (int jnx = 0; jnx < sAndSplittedVC.length; jnx++) {
        			String sOptionCodeTemp = sAndSplittedVC[jnx].trim();
        			if (hmCorpOption.containsKey(sOptionCodeTemp)) {
						if (sbResultTemp.length() == 0) {
							sbResultTemp.append(hmCorpOption.get(sOptionCodeTemp));
						} else {
							sbResultTemp.append(sSpace).append(sSeparate).append(sSpace).append(hmCorpOption.get(sOptionCodeTemp));
						}
					} else {
    					// Corporate Option 내에 존재하지 않는 Option 발견 시
        				if (sbResultTemp.length() == 0) {
        					sbResultTemp.append("No Name");
        				} else {
        					sbResultTemp.append(sSpace).append(sSeparate).append(sSpace).append("No Name");
        				}
    				}
    			}
        		
        		if (inx != sSplittedVC.length - 1) {
        			sbResult.append(sbResultTemp.toString()).append("\n");
        		} else {
        			sbResult.append(sbResultTemp.toString());
        		}
    		}
        	
        	return sbResult.toString();
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
    	
    	return "";
    }
}
