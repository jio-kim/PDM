/**
 * 
 */
package com.symc.plm.me.sdv.view.common;

import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.AbstractSDVSWTDialog;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.sdv.core.util.ProgressBar;

import swing2swt.layout.BorderLayout;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVLOVComboBox;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVQryTextLocaleConstant;
import com.symc.plm.me.common.SDVText;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.common.TCTable;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCTextService;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : SearchTypedItemView
 * Class Description :
 * 
 * @date 2013. 11. 12.
 * [SR141229-020][20150206][jclee] BOM Window의 최상위 BOM Line이 갖는 Proejct Code의 MECO만 가져오는 기능 추가
 */
public class SearchTypedItemView extends AbstractSDVViewPane {
	private Label lblItemName;
	private SDVText textItemID;
	private SDVText textItemName;
	private SDVLOVComboBox lovCreator;
	private Button btnOwningCheck;
	private Button btnRelevantProject;
	private TCTable table;
	private TCSession session;
	private TCTextService textService;
	private Registry registry;
	private String itemType = "Item";
	private Integer itemReleaseType = AllItem;

	public static String ReturnSelectedKey = "SELECTED_ITEM";
	public static String SEARCH_ITEM_TYPE_KEY = "ItemType";
	public static String SEARCH_RELEASE_TYPE_KEY = "ReleaseType";

	public static Integer ReleaseItem = 0;
	public static Integer UnReleaseItem = 1;
	public static Integer AllItem = 2;

	private boolean isShowProgress = false;
	private ProgressBar progressShell;
	private Shell thisShell;
	private Button btnSearch;

	/**
	 * @param parent
	 * @param style
	 * @param id
	 */
	public SearchTypedItemView(Composite parent, int style, String id) {
		super(parent, style, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sdv.core.ui.view.AbstractSDVViewPane#initUI()
	 */
	@Override
	protected void initUI(Composite parent) {
		try {
			thisShell = getShell();
			registry = Registry.getRegistry(SearchTypedItemView.class);
			session = CustomUtil.getTCSession();
			textService = session.getTextService();

			// setLayout(new FillLayout(SWT.HORIZONTAL));

			Composite searchMain = new Composite(parent, SWT.NONE);
			searchMain.setLayout(new BorderLayout(0, 0));

			Composite searchCondition = new Composite(searchMain, SWT.TOP);
			searchCondition.setLayoutData(BorderLayout.NORTH);
			searchCondition.setLayout(new FillLayout(SWT.HORIZONTAL));

			Composite group = new Composite(searchCondition, SWT.NONE);
			group.setLayout(new GridLayout(4, false));

			Label lblItemID = new Label(group, SWT.NONE);
			lblItemID.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblItemID.setText(textService.getTextValue("item_id"));

			textItemID = new SDVText(group, SWT.BORDER | SWT.SINGLE);
			GridData gd_textItemID = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd_textItemID.minimumWidth = 100;
			gd_textItemID.widthHint = 130;
			textItemID.setLayoutData(gd_textItemID);

			Label lblNewLabel = new Label(group, SWT.NONE);
			GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_lblNewLabel.widthHint = 150;
			lblNewLabel.setLayoutData(gd_lblNewLabel);

			btnSearch = new Button(group, SWT.NONE);
			GridData gd_btnSearch = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
			gd_btnSearch.widthHint = 60;
			btnSearch.setLayoutData(gd_btnSearch);
			btnSearch.setText(registry.getString("search.TITLE"));
			btnSearch.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					if (thisShell.isEnabled()) {
						Display.getCurrent().syncExec(new Runnable() {
							@Override
							public void run() {
								searchItems();
							}
						});
					}
				}
			});

			lblItemName = new Label(group, SWT.NONE);
			lblItemName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblItemName.setText(textService.getTextValue(SDVPropertyConstant.ITEM_OBJECT_NAME));

			textItemName = new SDVText(group, SWT.BORDER | SWT.SINGLE);
			GridData gd_textItemName = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			gd_textItemName.minimumWidth = 200;
			gd_textItemName.widthHint = 72;
			textItemName.setLayoutData(gd_textItemName);

			Label label_1 = new Label(group, SWT.NONE);
			label_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

			Label lblCreator = new Label(group, SWT.NONE);
			lblCreator.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblCreator.setText(textService.getTextValue(SDVPropertyConstant.ITEM_OWNING_USER));

			lovCreator = new SDVLOVComboBox(group, SWT.BORDER, session, "User Ids");
			GridData gd_combo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_combo.widthHint = 200;
			lovCreator.setLayoutData(gd_combo);

			btnOwningCheck = new Button(group, SWT.CHECK);
			btnOwningCheck.setText("Owned MECO");
//			GridData gdOwningCheck = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
//			gdOwningCheck.widthHint = 100;
//			btnOwningCheck.setLayoutData(gdOwningCheck);

			btnOwningCheck.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {

					try {
						if (btnOwningCheck.getSelection()) {
							lovCreator.setSelectedString(session.getUser().toString());
							lovCreator.setEnabled(false);
						} else {
							lovCreator.setSelectedString("");
							lovCreator.setEnabled(true);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
			
			/**
			 * [SR141229-020][20150206][jclee] BOM Window의 최상위 BOM Line이 갖는 Proejct Code의 MECO만 가져오는 기능 추가
			 */
			btnRelevantProject = new Button(group, SWT.CHECK);
			btnRelevantProject.setText("Relevant Project");
			GridData gdRelevantProject = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
			gdRelevantProject.horizontalSpan = 1;
			btnRelevantProject.setLayoutData(gdRelevantProject);
			btnRelevantProject.setSelection(true);

			Composite searchResult = new Composite(searchMain, SWT.CENTER);
			searchResult.setLayoutData(BorderLayout.CENTER);
			searchResult.setLayout(new BorderLayout(0, 0));

			Label label = new Label(searchResult, SWT.SEPARATOR | SWT.HORIZONTAL);
			label.setLayoutData(BorderLayout.NORTH);

			Composite composite = new Composite(searchResult, SWT.EMBEDDED);
			composite.setLayoutData(BorderLayout.CENTER);

			Frame frame = SWT_AWT.new_Frame(composite);

			Panel panel = new Panel();
			frame.add(panel);
			panel.setLayout(new java.awt.BorderLayout(0, 0));

			JRootPane rootPane = new JRootPane();
			panel.add(rootPane);
			JScrollPane scrollPane = new JScrollPane();

			String[] columnNames = new String[] { SDVPropertyConstant.ITEM_ITEM_ID, SDVPropertyConstant.ITEM_REVISION_ID, SDVPropertyConstant.ITEM_OBJECT_NAME, SDVPropertyConstant.ITEM_REV_RELEASE_STATUS_LIST, SDVPropertyConstant.ITEM_OWNING_USER };
			table = new TCTable(session, columnNames);
			rootPane.getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);
			scrollPane.getViewport().add(table);
			String[] columnWidths = new String[] { "24", "8", "30", "15", "15" };
			table.setColumnWidths(columnWidths);
			table.setEditable(false);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.addEmptyRow();
			table.addMouseListener(new MouseListener() {

				@Override
				public void mouseReleased(java.awt.event.MouseEvent e) {
				}

				@Override
				public void mousePressed(java.awt.event.MouseEvent e) {
				}

				@Override
				public void mouseExited(java.awt.event.MouseEvent e) {
				}

				@Override
				public void mouseEntered(java.awt.event.MouseEvent e) {
				}

				@Override
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if (e.getClickCount() == 2 && e.getButton() == 1) {
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								((Dialog) UIManager.getCurrentDialog()).close();
							}
						});
					}
				}
			});

			thisShell.setDefaultButton(btnSearch);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	protected void showProgress(boolean show) {
		if (this.isShowProgress != show) {
			if (show) {
				if (progressShell == null) {
					try {
						// thisShell.setEnabled(false);
						// thisShell.update();
						progressShell = new ProgressBar(thisShell);
						progressShell.start();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			} else if (progressShell != null) {
				// thisShell.setEnabled(true);
				// thisShell.update();
				progressShell.close();
				progressShell = null;
			}

			isShowProgress = show;
		}
	}

	/**
	 * 
	 * @method searchItems
	 * @date 2013. 11. 13.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	protected void searchItems() {
		final String item_type = itemType;
		final String item_id = textItemID.getText();
		final String item_name = textItemName.getText();
		final boolean isRelevantProject = btnRelevantProject.getSelection();
		Object item_owner = null;

		try {
			if (item_type == null || item_type.trim().length() == 0) {
				// 아이템 타입은 필수입니다. 관리자에게 연락해 주세요.
				MessageBox.post(thisShell, registry.getString("SearchTypeIsNull.MESSAGE", "Search Type is required value. connect to System Admin."), registry.getString("Error.TITLE", "Error"), MessageBox.ERROR);
				return;
			}

			item_owner = btnOwningCheck.getSelection() ? session.getUser().getUserId() : lovCreator.getSelectedObject();
			if (item_owner == null || item_owner.toString().trim().length() == 0)
				item_owner = "*";

			if ((item_id == null || item_id.trim().length() == 0 || item_id.trim().equals("*")) &&
                (item_name == null || item_name.trim().length() == 0 || item_name.trim().equals("*")) &&
                (item_owner == null || item_owner.toString().trim().length() == 0 || item_owner.toString().trim().equals("*"))) {
				// 검색조건이 너무 많은 결과를 초래할 수 있습니다. 조건을 다시 입력해 주세요.
				MessageBox.post(thisShell, registry.getString("SearchConditionIsNull.MESSAGE", "Please enter a search condition."), registry.getString("Information.TITLE", "Information"), MessageBox.INFORMATION);
				return;
			}

			showProgress(true);

			final Object l_item_owner = item_owner;
			try {

				TCTextService text_service = session.getTextService();
				TCComponentQueryType query_type = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
				final TCComponentQuery saved_query = (TCComponentQuery) query_type.find(text_service.getTextValue(SDVQryTextLocaleConstant.ITEM));
				ArrayList<String> searchNameList = new ArrayList<String>();
				ArrayList<String> searchValueList = new ArrayList<String>();
				// final String []paramNames = new String[4];
				// final String []paramValues = new String[4];

				searchNameList.add(text_service.getTextValue("Type"));
				searchValueList.add(item_type);

				if (item_id != null && item_id.trim().length() > 0) {
					searchNameList.add(text_service.getTextValue("ItemID"));
					searchValueList.add(item_id);
				}

				if (item_name != null && item_name.trim().length() > 0) {
					searchNameList.add(text_service.getTextValue(item_type.equals(SDVTypeConstant.MECO_ITEM) ? "Description" : "Name"));
					searchValueList.add(item_name);
				}

				if (!l_item_owner.equals("*")) {
					searchNameList.add(text_service.getTextValue("OwningUser"));
					searchValueList.add(l_item_owner.toString());
				}

				final String[] paramNames = searchNameList.toArray(new String[0]);
				final String[] paramValues = searchValueList.toArray(new String[0]);
				// paramNames[0] = text_service.getTextValue("Type");
				// paramNames[1] = text_service.getTextValue("ItemID");
				// paramNames[2] = text_service.getTextValue(item_type.equals(SDVTypeConstant.MECO_ITEM) ? "Description" : "Name");
				// paramNames[3] = text_service.getTextValue("Owning User");
				// paramValues[0] = item_type;
				// paramValues[1] = item_id == null || item_id.trim().length() == 0 ? "*" : item_id;
				// paramValues[2] = item_name == null || item_name.trim().length() == 0 ? "*" : item_name;
				// paramValues[3] = l_item_owner.toString();

				AbstractAIFOperation op = new AbstractAIFOperation() {

					@Override
					public void executeOperation() throws Exception {
						TCComponent[] query_result = saved_query.execute(paramNames, paramValues);

						ArrayList<TCComponentItemRevision> revList = new ArrayList<TCComponentItemRevision>();
						for (TCComponent result : query_result) {
							if (itemReleaseType == UnReleaseItem && CustomUtil.isWorkingStatus(((TCComponentItem) result).getLatestItemRevision()))
								revList.add(((TCComponentItem) result).getLatestItemRevision());
							else if (itemReleaseType == ReleaseItem && CustomUtil.isReleased(((TCComponentItem) result).getLatestItemRevision()))
								revList.add(((TCComponentItem) result).getLatestItemRevision());
							else if (itemReleaseType == AllItem)
								revList.add(((TCComponentItem) result).getLatestItemRevision());
						}

						storeOperationResult(revList.toArray(new TCComponent[0]));
					}
				};
				op.addOperationListener(new InterfaceAIFOperationListener() {

					@Override
					public void startOperation(String paramString) {
						setDialogCursor(SWT.CURSOR_WAIT);
					}

					@Override
					public void endOperation() {
						setDialogCursor(SWT.CURSOR_ARROW);

						thisShell.getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								showProgress(false);
							}
						});
					}
				});
				session.queueOperationAndWait(op);
				
				Object result = op.getOperationResult();
				if (result != null)
				{
					if (((TCComponent []) result).length > 0)
					{
						// Revise 대상 Project와 일치하는 MECO만 추린 후 result 변수에 재 할당
						if (isRelevantProject) {
							TCComponent [] aResult = (TCComponent []) result;
							ArrayList<TCComponent> alResult = new ArrayList<TCComponent>();
							
							// 현재 BOM WINDOW
							MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
							TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();
							TCComponentBOMLine bomLine = bomWindow.getTopBOMLine();
							
							String sSelectedTargetProject = "";
							String sSelectedTargetProduct = "";

							TCComponentItemRevision revision = bomLine.getItemRevision();
							sSelectedTargetProduct = revision.getProperty("m7_PRODUCT_CODE");
							sSelectedTargetProject = getProjectCodeFromProductID(sSelectedTargetProduct);
							
							for (int i = 0; i < aResult.length; i++) {
								String sProject = aResult[i].getProperty("m7_PROJECT");
								
								if (sProject != null && sProject.equals(sSelectedTargetProject)) {
									alResult.add(aResult[i]);
								}
							}
							
							TCComponent[] tempComps = new TCComponent[alResult.size()];
							for (int inx = 0; inx < alResult.size(); inx++) {
								tempComps[inx] = alResult.get(inx);
							}
							
							result = tempComps;
						}

						table.removeAllRows();
						// result 변수의 개수 재 확인. (Relevant Project 체크 기능 후 결과가 남아있는지 확인하기 위해)
						if (((TCComponent []) result).length > 0) {
							table.addRows(result);
						}
					}
					else
						MessageBox.post(thisShell, registry.getString("SearchResultIsNothing.MESSAGE", "There is no search result."), registry.getString("Information.TITLE", "Information"), MessageBox.INFORMATION);
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private String getProjectCodeFromProductID(String sProductID) {
		String sProjectCode = "";
		
		try {
			TCComponent[] comps = CustomUtil.queryComponent("Item...", new String[] { "Item ID" }, new String[] { sProductID });
			if (comps.length == 1) {
				TCComponent comp = comps[0];
				if (comp instanceof TCComponentItem) {
					TCComponentItem item = (TCComponentItem) comp;
					TCComponentItemRevision revision = item.getLatestItemRevision();
					sProjectCode = revision.getProperty("s7_PROJECT_CODE");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return sProjectCode;
	}

	/**
	 * @param paramters
	 *            the parameters to set
	 */
	@Override
	public void setParameters(Map<String, Object> parameters) {
		if (parameters != null) {
			itemType = parameters.get(SEARCH_ITEM_TYPE_KEY).toString();
			IDialog thisDialog = UIManager.getAvailableDialog("symc.me.bop.SearchTypedItemDialog");
			((AbstractSDVSWTDialog) thisDialog).setDialogTitle("Search " + itemType);
			if (itemType.equals(SDVTypeConstant.MECO_ITEM)) {
				try {
					lblItemName.setText(textService.getTextValue(SDVPropertyConstant.ITEM_OBJECT_DESC));
				} catch (TCException e) {
					e.printStackTrace();
				}
				table.setColumnNames(new String[] { SDVPropertyConstant.ITEM_ITEM_ID, SDVPropertyConstant.ITEM_CREATION_DATE, SDVPropertyConstant.ITEM_OBJECT_DESC, SDVPropertyConstant.ITEM_OWNING_USER }, null);

				table.setColumnWidths(new String[] { "15", "19", "30", "17" });

				btnOwningCheck.setSelection(true);
				lovCreator.setSelectedString(SDVBOPUtilities.getTCSession().getUser().toString());
				lovCreator.setEnabled(false);
			}

			Object release_type = parameters.get(SEARCH_RELEASE_TYPE_KEY);
			if (release_type == null)
				itemReleaseType = AllItem;
			else
				itemReleaseType = (Integer) parameters.get(SEARCH_RELEASE_TYPE_KEY);
		}
	}

	/**
	 * 
	 * @method setDialogCursor
	 * @date 2013. 11. 13.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	protected void setDialogCursor(final int cursorType) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				setCursor(new Cursor(getDisplay(), cursorType));
			}
		});
	}

	@Override
	public void setLocalDataMap(IDataMap dataMap) {
	}

	@Override
	public IDataMap getLocalDataMap() {
		return null;
	}

	@Override
	public IDataMap getLocalSelectDataMap() {
		// 다른데서 사용하기 위한 내용을 리턴하는 부분
		RawDataMap mecoData = new RawDataMap();

		InterfaceAIFComponent[] selectedItems = table.getSelectedComponents();

		mecoData.put(ReturnSelectedKey, selectedItems == null || selectedItems.length == 0 ? null : selectedItems[0], IData.OBJECT_FIELD);

		return mecoData;
	}

	@Override
	public Composite getRootContext() {
		return null;
	}

	@Override
	public AbstractSDVInitOperation getInitOperation() {
		return null;
	}

	@Override
	public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
	}

	@Override
	public void uiLoadCompleted() {
	}

}
