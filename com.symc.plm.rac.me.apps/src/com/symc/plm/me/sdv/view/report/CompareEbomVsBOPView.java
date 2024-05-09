package com.symc.plm.me.sdv.view.report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.sdv.core.util.UIUtil;

import swing2swt.layout.BorderLayout;

import com.kgm.common.remote.DataSet;
import com.kgm.common.swtsearch.SearchItemDialog;
import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.common.viewer.AbstractSDVViewer;
import com.symc.plm.me.sdv.command.meco.dao.CustomMECODao;
import com.symc.plm.me.utils.BundleUtil;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.cme.framework.treetable.CMEBOMTreeTable;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.AdapterUtil;
import com.teamcenter.rac.util.PlatformHelper;
import com.teamcenter.rac.util.Registry;

/**
 * [20140507] EBOM vs BOP Multi 비교를 위한 전체적인 소스 수정
 * [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제된 MProduct를 찾을 수 있도록 수정
 *
 * @author bykim
 * 
 */
public class CompareEbomVsBOPView extends AbstractSDVViewer {

	private Registry registry;

	private Composite csCondition;
	private DateTime txtebomReleaseDate, txtbopReleaseDate;
	private Table table;
	private Text txtFunction;
	private Text txtShop;

	private String processType = "";
	private String operationType = "";

	private ArrayList<TableItem> tableItemOfFunction = new ArrayList<TableItem>();

	private ArrayList<TCComponentBOPLine> openedBOPLine = new ArrayList<TCComponentBOPLine>();

	private SimpleDateFormat simpleDateFormat;

	public CompareEbomVsBOPView(Composite parent) {
		super(parent);

		simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	@Override
	public void createPanel(Composite parent) {
		registry = Registry.getRegistry("com.kgm.common.common");

		setLayout(new FillLayout(SWT.HORIZONTAL));

		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(new BorderLayout());

		csCondition = new Composite(composite, SWT.NONE);
		csCondition.setLayoutData(BorderLayout.NORTH);
		csCondition.setLayout(new GridLayout(7, false));
		csCondition.setBackground(UIUtil.getColor(SWT.COLOR_GRAY));

		Label lbFunction = new Label(csCondition, SWT.NONE);
		lbFunction.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		lbFunction.setText("Function");

		GridData gd_txtFunction = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_txtFunction.widthHint = 150;
		txtFunction = new Text(csCondition, SWT.BORDER);
		txtFunction.setLayoutData(gd_txtFunction);
		txtFunction.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
		txtFunction.setEditable(false);

		Button btnFunctionSearch = new Button(csCondition, SWT.NONE);
		btnFunctionSearch.setText("Search");
		btnFunctionSearch.setImage(registry.getImage("Search.ICON"));
		btnFunctionSearch.addSelectionListener(new ItemSelectionAdapter(txtFunction, "S7_Function"));

		GridData gd_emptyLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_emptyLabel.widthHint = 50;
		Label emptyLabel = new Label(csCondition, SWT.NONE);
		emptyLabel.setLayoutData(gd_emptyLabel);

		Label lbShop = new Label(csCondition, SWT.NONE);
		lbShop.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		lbShop.setText("Shop");

		GridData gd_txtShop = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_txtShop.widthHint = 150;
		txtShop = new Text(csCondition, SWT.BORDER);
		txtShop.setLayoutData(gd_txtShop);
		txtShop.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
		txtShop.setEditable(false);

		Button btnShopSearch = new Button(csCondition, SWT.NONE);
		btnShopSearch.setText("Search");
		btnShopSearch.setImage(registry.getImage("Search.ICON"));
		btnShopSearch.addSelectionListener(new ItemSelectionAdapter(txtShop, "M7_BOPShop"));

		Label lbEbomReleaseDate = new Label(csCondition, SWT.NONE);
		lbEbomReleaseDate.setText("Released Date");

		txtebomReleaseDate = new DateTime(csCondition, SWT.BORDER | SWT.DROP_DOWN);
		txtebomReleaseDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		new Label(csCondition, SWT.NONE);
		new Label(csCondition, SWT.NONE);

		Label lbBopReleaseDate = new Label(csCondition, SWT.NONE);
		lbBopReleaseDate.setText("Released Date");

		txtbopReleaseDate = new DateTime(csCondition, SWT.BORDER | SWT.DROP_DOWN);
		txtbopReleaseDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		new Label(csCondition, SWT.NONE);

		Composite csProcessMonitor = new Composite(composite, SWT.BORDER);
		csProcessMonitor.setLayoutData(BorderLayout.CENTER);
		csProcessMonitor.setLayout(new GridLayout(1, false));

		initTable(csProcessMonitor);
	}

	/**
	 * 
	 * 
	 * @method initTable
	 * @date 2014. 3. 25.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	private void initTable(Composite parent) {
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gridData.heightHint = 600;

		table = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		table.setLayoutData(gridData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setEnabled(true);

		createTableColumn("No", 50);
		createTableColumn("Proj.", 50);
		createTableColumn("Function", 100);
		createTableColumn("SEQ", 80);
		createTableColumn("Lev", 50);
		createTableColumn("Part No", 100);
		createTableColumn("Ver", 50);
		createTableColumn("S/Mode", 80);
		createTableColumn("Part Name", 200);
		createTableColumn("Option", 80);
		createTableColumn("Pos Description", 150);
		createTableColumn("CAT", 80);
		createTableColumn("In ECO", 80);
		createTableColumn("In ECO Released Date", 150);
		createTableColumn("BP Date", 100);
		createTableColumn("E-BOM QTY", 100);
		createTableColumn("Shop", 80);
		createTableColumn("Line", 80);
		createTableColumn("Line Rev.", 80);
		createTableColumn("공정", 80);
		createTableColumn("공정 Rev.", 80);
		createTableColumn("공법", 150);
		createTableColumn("공법 Rev.", 80);
		createTableColumn("공법 Status", 100);
		createTableColumn("공법명", 200);
		createTableColumn("공법사양", 80);
		createTableColumn("BOP 수량", 80);
		createTableColumn("In MECO", 100);
		createTableColumn("In MECO Released Date", 150);
	}

	/**
	 * 
	 * 
	 * @method createTableColumn
	 * @date 2014. 3. 25.
	 * @param
	 * @return TableColumn
	 * @exception
	 * @throws
	 * @see
	 */
	private TableColumn createTableColumn(String columnName, int width) {
		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setText(columnName);
		column.setWidth(width);
		column.setResizable(true);
		column.setMoveable(true);

		return column;
	}

	/**
	 * 
	 * 
	 * @method validationCheck
	 * @date 2014. 3. 25.
	 * @param
	 * @return boolean
	 * @exception
	 * @throws
	 * @see
	 */
	public boolean validationCheck() throws Exception {
		// Function
		if (txtFunction.getData() == null) {
			MessageDialog.openInformation(getShell(), "Information", "Please select to Function.");
			return false;
		}

		// Shop
		if (txtShop.getData() == null) {
			MessageDialog.openInformation(getShell(), "Information", "Please select to shop.");
			return false;
		}

		// 검색하려는 Shop의 BOP Load 유무 체크
		// 사용자가 선택한 Shop
		TCComponentItem[] items = (TCComponentItem[]) txtShop.getData();

		// Opened View Check
		IViewReference[] viewReferences = PlatformHelper.getCurrentPage().getViewReferences();
		for (IViewReference viewRefrence : viewReferences) {
			IViewPart viewPart = viewRefrence.getView(false);
			if (viewPart == null) {
				continue;
			}

			CMEBOMTreeTable cmeBOMTreeTable = (CMEBOMTreeTable) AdapterUtil.getAdapter(viewPart, CMEBOMTreeTable.class);
			if (cmeBOMTreeTable == null) {
				continue;
			}

			// Process View Check
			if (viewRefrence.getId().equals("com.teamcenter.rac.cme.processView")) {
				TCComponentBOMLine bomRoot = cmeBOMTreeTable.getBOMRoot();
				for (TCComponentItem item : items) {
					if (item.getProperty(SDVPropertyConstant.ITEM_ITEM_ID).equals(bomRoot.getProperty(SDVPropertyConstant.BL_ITEM_ID))) {
						openedBOPLine.add((TCComponentBOPLine) bomRoot);
					}
				}
			}
		}

		if (items.length != openedBOPLine.size()) {
			openedBOPLine.clear();
			MessageDialog.openError(getShell(), "Error", "Please open BOP of selected shop");

			return false;
		}

		return true;
	}

	/**
	 * 
	 * 
	 * @method compareEBOMVsBOP
	 * @date 2014. 3. 25.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	public void compareEBOMVsBOP() {
		try {
			searchEndItemListOfFunction();
			searchOperationList();
		} catch (Exception e) {
			MessageDialog.openError(getShell(), "Error", "Error has occurred" + "\n" + "Please contact to administrator.");
			e.printStackTrace();
		} finally {
			openedBOPLine.clear();
		}
	}

	/**
	 * 
	 * 
	 * @method searchEndItemListOfFunction
	 * @date 2014. 3. 25.
	 * @param
	 * @return ArrayList<ArrayList<HashMap<String,Object>>>
	 * @exception
	 * @throws
	 * @see
	 */
	public ArrayList<ArrayList<HashMap<String, Object>>> searchEndItemListOfFunction() throws Exception {
		tableItemOfFunction.clear();
		table.removeAll();

		CustomMECODao dao = new CustomMECODao();
		DataSet ds = new DataSet();

		ArrayList<ArrayList<HashMap<String, Object>>> endItemLists = new ArrayList<ArrayList<HashMap<String, Object>>>();
		ArrayList<HashMap<String, Object>> endItemList = null;

		TCComponentItem[] items = (TCComponentItem[]) txtFunction.getData();
		for (TCComponentItem item : items) {
			String year = String.valueOf(txtebomReleaseDate.getYear());
			String month = String.valueOf(txtebomReleaseDate.getMonth() + 1);
			month = month.length() == 1 ? "0" + month : month;
			String day = String.valueOf(txtebomReleaseDate.getDay());
			day = day.length() == 1 ? "0" + day : day;

			ds.put("function_no", item.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));
			ds.put("release_date", year + month + day);

			endItemList = dao.getEndItemListOnFunction(ds);

			if (endItemList != null) {
				endItemLists.add(endItemList);

				for (int i = 0; i < endItemList.size(); i++) {
					HashMap<String, Object> dataMap = endItemList.get(i);
					printBOMInfo(dataMap);
				}
			}
		}

		return endItemLists;
	}

	/**
	 * 
	 * 
	 * @method searchOperationList
	 * @date 2014. 3. 25.
	 * @param
	 * @return List<HashMap<String,Object>>
	 * @exception
	 * @throws
	 * @see
	 */
	@SuppressWarnings("unchecked")
	public List<HashMap<String, Object>> searchOperationList() throws Exception {
		List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();

		for (TCComponentBOPLine shopBOPLine : openedBOPLine) {
			if (shopBOPLine != null) {
				// process type
				processType = shopBOPLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PROCESS_TYPE);
				// if (processType.equals("BODY")) {
				// operationType = SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM;
				// } else if (processType.equals("PAINT")) {
				// operationType = SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM;
				// } else if (processType.equals("ASSY")) {
				// operationType = SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM;
				// }
				if (processType.startsWith("B")) {
					operationType = SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM;
				} else if (processType.startsWith("P")) {
					operationType = SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM;
				} else if (processType.startsWith("A")) {
					operationType = SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM;
				}

				dataList = getChildrenList(dataList, shopBOPLine);
				for (HashMap<String, Object> dataMap : dataList) {
					List<HashMap<String, Object>> endItemList = (List<HashMap<String, Object>>) dataMap.get("endItemList");
					for (HashMap<String, Object> endItemMap : endItemList) {
						printBOPInfo(dataMap, endItemMap);
					}
				}
			}
		}

		return dataList;
	}

	/**
	 * 
	 * 
	 * @method getChildrenList
	 * @date 2014. 3. 24.
	 * @param
	 * @return List<HashMap<String,Object>>
	 * @exception
	 * @throws
	 * @see
	 */
	private List<HashMap<String, Object>> getChildrenList(List<HashMap<String, Object>> dataList, TCComponentBOPLine parentLine) throws Exception {
		if (parentLine.getChildrenCount() > 0) {
			AIFComponentContext[] context = parentLine.getChildren();
			for (int i = 0; i < context.length; i++) {
				if (context[i].getComponent() instanceof TCComponentBOPLine) {
					TCComponentBOPLine childLine = (TCComponentBOPLine) context[i].getComponent();
					String type = childLine.getItem().getType();

					// 미할당 Line 제외
					if (type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
						if (SDVBOPUtilities.isAssyTempLine(childLine)) {
							continue;
						}
					}

					if (SDVTypeConstant.EBOM_MPRODUCT.equals(type) || SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM.equals(type)) {
						continue;
					}

					if (operationType.equals(type)) {
						HashMap<String, Object> dataMap = new HashMap<String, Object>();

						// 공법 정보
						dataMap = getOperationInfo(childLine, dataMap);

						// 공정 정보
						dataMap = getParentInfo(childLine, dataMap);

						// End Item
						dataMap = getEndItemList(childLine, dataMap);

						dataList.add(dataMap);
					} else {
						getChildrenList(dataList, childLine);
					}
				}
			}
		}

		return dataList;
	}

	/**
	 * 
	 * 
	 * @method getOperationInfo
	 * @date 2014. 3. 24.
	 * @param
	 * @return HashMap<String,Object>
	 * @exception
	 * @throws
	 * @see
	 */
	private HashMap<String, Object> getOperationInfo(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
		// 공법
		dataMap.put("operation_id", operation.getProperty(SDVPropertyConstant.BL_ITEM_ID));

		// 공법 Rev.
		dataMap.put("operation_rev", operation.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID));

		// 공법 Status
		dataMap.put("operation_status", operation.getProperty(SDVPropertyConstant.BL_RELEASE_STATUS));

		// 공법명
		dataMap.put("operation_name", operation.getProperty(SDVPropertyConstant.BL_OBJECT_NAME));

		// 공법 사양
		HashMap<String, Object> option = SDVBOPUtilities.getVariant(operation.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION));
		dataMap.put("operation_spec", option.get("printValues"));

		TCComponentChangeItemRevision changeItemRevision = (TCComponentChangeItemRevision) operation.getItemRevision().getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);

		if (changeItemRevision != null) {
			// IN_MECO
			dataMap.put("in_meco", changeItemRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));

			// IN_MECO_RELEASED_DATE
			Date date_released = changeItemRevision.getDateProperty(SDVPropertyConstant.ITEM_DATE_RELEASED);
			if (date_released != null) {
				dataMap.put("in_meco_released_date", simpleDateFormat.format(date_released));
			}
		}

		return dataMap;
	}

	/**
	 * 
	 * 
	 * @method getParentInfo
	 * @date 2014. 3. 20.
	 * @param
	 * @return HashMap<String,Object>
	 * @exception
	 * @throws
	 * @see
	 */
	private HashMap<String, Object> getParentInfo(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
		// Shop Code
		TCComponentBOPLine shop = getParentBOPLine(operation, SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
		if (shop != null) {
			dataMap.put("shop_code", shop.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_SHOP_CODE));
		}

		// Line Code, Line Revision
		TCComponentBOPLine line = getParentBOPLine(operation, SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
		if (line != null) {
			dataMap.put("line_code", line.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE));
			dataMap.put("line_rev", line.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID));
		}

		if (processType.startsWith("B") || processType.startsWith("P")) {
			// Station Code, Station Revision
			TCComponentBOPLine station = getParentBOPLine(operation, SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
			if (station != null) {
				dataMap.put("station_code", station.getItemRevision().getProperty(SDVPropertyConstant.STATION_LINE) + "-" + station.getItemRevision().getProperty(SDVPropertyConstant.STATION_STATION_CODE));
				dataMap.put("station_rev", station.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID));
			}
		} else {
			// Station No(조립)
			dataMap.put("station_code", operation.getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO));
		}

		return dataMap;
	}

	/**
	 * 
	 * 
	 * @method getParentBOPLine
	 * @date 2014. 3. 20.
	 * @param
	 * @return TCComponentBOPLine
	 * @exception
	 * @throws
	 * @see
	 */
	private TCComponentBOPLine getParentBOPLine(TCComponentBOPLine bopLine, String itemType) throws TCException {
		TCComponentBOPLine parentBOPLine = null;

		if (bopLine.parent() != null) {
			parentBOPLine = (TCComponentBOPLine) bopLine.parent();
			if (!parentBOPLine.getItem().getType().equals(itemType)) {
				return getParentBOPLine(parentBOPLine, itemType);
			}
		}

		return parentBOPLine;
	}

	/**
	 * 
	 * 
	 * @method getEndItemList
	 * @date 2014. 3. 20.
	 * @param
	 * @return HashMap<String,Object>
	 * @throws Exception
	 * @exception
	 * @throws
	 * @see
	 */
	private HashMap<String, Object> getEndItemList(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws Exception {
		List<HashMap<String, Object>> endItemList = new ArrayList<HashMap<String, Object>>();

		if (operation.getChildrenCount() > 0) {
			AIFComponentContext[] context = operation.getChildren();
			for (int i = 0; i < context.length; i++) {
				if (context[i].getComponent() instanceof TCComponentBOPLine) {
					TCComponentBOPLine childLine = (TCComponentBOPLine) context[i].getComponent();
					String type = childLine.getItem().getType();
					if (SDVTypeConstant.EBOM_VEH_PART.equals(type) || SDVTypeConstant.EBOM_STD_PART.equals(type)) {
						HashMap<String, Object> endItemMap = new HashMap<String, Object>();
						endItemMap.put("endItem_id", childLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));
						endItemMap.put("endItem_quantity", childLine.getProperty(SDVPropertyConstant.BL_QUANTITY).split("\\.")[0]);

						// BOP에 할당된 End Item을 MProduct에서 찾아서 Sequence, variant 가져오기
						TCComponentBOMWindow bomWindow = childLine.window();
						TCComponentBOMLine topBOMLine = bomWindow.getTopBOMLine();

						// [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제된 MProduct를 찾을 수 있도록 수정
						TCComponentBOMWindow mProductBOMWindow = SDVBOPUtilities.getConnectedMProductBOMWindow(topBOMLine.getItemRevision());
						TCComponentBOMLine mProductTopBOMLine = mProductBOMWindow.getTopBOMLine();

						boolean absOccIdCheck = true;
						if (childLine.getProperty(SDVPropertyConstant.BL_ABS_OCC_ID).equals("")) {
							absOccIdCheck = false;
						}

						TCComponent[] partBomLine = { (TCComponent) childLine };
						TCComponent[] resultBomLines = CustomUtil.findOtherWindowBomLine((TCComponent) mProductTopBOMLine, partBomLine, absOccIdCheck, false);
						if (resultBomLines != null && resultBomLines.length > 0) {
							// Sequence
							endItemMap.put("endItem_findNoOfMProduct", resultBomLines[0].getProperty(SDVPropertyConstant.BL_SEQUENCE_NO));

							// Option
							HashMap<String, Object> option = SDVBOPUtilities.getVariant(resultBomLines[0].getProperty(SDVPropertyConstant.BL_VARIANT_CONDITION));
							endItemMap.put("endItem_variantOfMProduct", option.get("printValues"));
						}

						endItemList.add(endItemMap);
					}
				}
			}
		}
		dataMap.put("endItemList", endItemList);

		return dataMap;
	}

	/**
	 * 
	 * 
	 * @method printBOMInfo
	 * @date 2014. 3. 24.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	public void printBOMInfo(HashMap<String, Object> dataMap) {
		TableItem tableItem = new TableItem(table, SWT.NONE);
		tableItem.setText(0, String.valueOf(table.getItemCount()));
		tableItem.setText(1, BundleUtil.nullToString((String) dataMap.get("PROJECT")));
		tableItem.setText(2, BundleUtil.nullToString((String) dataMap.get("FUNCTION_NO")));
		tableItem.setText(3, BundleUtil.nullToString((String) dataMap.get("SEQ")));
		tableItem.setText(4, dataMap.get("BOM_LEVEL") == null ? "" : dataMap.get("BOM_LEVEL").toString());
		tableItem.setText(5, BundleUtil.nullToString((String) dataMap.get("PART_NO")));
		tableItem.setText(6, BundleUtil.nullToString((String) dataMap.get("VER")));
		tableItem.setText(7, BundleUtil.nullToString((String) dataMap.get("SUPPMODE")));
		tableItem.setText(8, BundleUtil.nullToString((String) dataMap.get("PART_NAME")));
		tableItem.setText(9, BundleUtil.nullToString((String) dataMap.get("OPTIONS")));
		tableItem.setText(10, BundleUtil.nullToString((String) dataMap.get("POST_DESC")));
		tableItem.setText(11, BundleUtil.nullToString((String) dataMap.get("U_CATEGORY")));
		tableItem.setText(12, BundleUtil.nullToString((String) dataMap.get("IN_ECO")));
		if (dataMap.get("IN_DATE") != null) {
			tableItem.setText(13, dataMap.get("IN_DATE").toString());
		}
		if (dataMap.get("BP_DATE") != null) {
			tableItem.setText(14, dataMap.get("BP_DATE").toString());
		}
		tableItem.setText(15, dataMap.get("QTY") == null ? "" : dataMap.get("QTY").toString());

		tableItem.setData("PART_NO", BundleUtil.nullToString((String) dataMap.get("PART_NO")));

		tableItemOfFunction.add(tableItem);
	}

	/**
	 * 
	 * 
	 * @method printBOPInfo
	 * @date 2014. 3. 24.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	public void printBOPInfo(HashMap<String, Object> dataMap, HashMap<String, Object> endItemMap) {
		String endItem_id = (String) endItemMap.get("endItem_id");
		String endItem_findNoOfMProduct = (String) endItemMap.get("endItem_findNoOfMProduct");
		String endItem_variantOfMProduct = (String) endItemMap.get("endItem_variantOfMProduct");
		String num = null;

		TableItem tableItem = null;
		for (TableItem temp : tableItemOfFunction) {
			if (temp.getData("PART_NO") != null) {
				String partNo = temp.getData("PART_NO").toString();
				String seq = temp.getText(3);
				String variant = temp.getText(9);
				if (partNo.equals(endItem_id) && seq.equals(endItem_findNoOfMProduct) && variant.equals(endItem_variantOfMProduct)) {
					tableItem = temp;
					num = tableItem.getText(0);
				}
			}
		}

		if (tableItem == null) {
			tableItem = new TableItem(table, SWT.NONE);
		} else {
			if (!tableItem.getText(16).equals("")) {
				tableItem = new TableItem(table, SWT.NONE);
				num = null;
			}
		}

		tableItem.setText(0, num == null ? String.valueOf(table.getItemCount()) : num);
		tableItem.setText(16, BundleUtil.nullToString((String) dataMap.get("shop_code")));
		tableItem.setText(17, BundleUtil.nullToString((String) dataMap.get("line_code")));
		tableItem.setText(18, BundleUtil.nullToString((String) dataMap.get("line_rev")));
		tableItem.setText(19, BundleUtil.nullToString((String) dataMap.get("station_code")));
		tableItem.setText(20, BundleUtil.nullToString((String) dataMap.get("station_rev")));
		tableItem.setText(21, BundleUtil.nullToString((String) dataMap.get("operation_id")));
		tableItem.setText(22, BundleUtil.nullToString((String) dataMap.get("operation_rev")));
		tableItem.setText(23, BundleUtil.nullToString((String) dataMap.get("operation_status")));
		tableItem.setText(24, BundleUtil.nullToString((String) dataMap.get("operation_name")));
		tableItem.setText(25, BundleUtil.nullToString((String) dataMap.get("operation_spec")));
		tableItem.setText(26, BundleUtil.nullToString((String) endItemMap.get("endItem_quantity")));
		tableItem.setText(27, BundleUtil.nullToString((String) dataMap.get("in_meco")));
		tableItem.setText(28, BundleUtil.nullToString((String) dataMap.get("in_meco_released_date")));
	}

	/**
	 * 
	 * 
	 * @method getConditionInfo
	 * @date 2014. 3. 24.
	 * @param
	 * @return HashMap<String,Object>
	 * @exception
	 * @throws
	 * @see
	 */
	public HashMap<String, Object> getConditionInfo() {
		String bom_year = String.valueOf(txtebomReleaseDate.getYear());
		String bom_month = String.valueOf(txtebomReleaseDate.getMonth() + 1);
		bom_month = bom_month.length() == 1 ? "0" + bom_month : bom_month;
		String bom_day = String.valueOf(txtebomReleaseDate.getDay());
		bom_day = bom_day.length() == 1 ? "0" + bom_day : bom_day;

		String bop_year = String.valueOf(txtbopReleaseDate.getYear());
		String bop_month = String.valueOf(txtbopReleaseDate.getMonth() + 1);
		bop_month = bop_month.length() == 1 ? "0" + bop_month : bop_month;
		String bop_day = String.valueOf(txtbopReleaseDate.getDay());
		bop_day = bop_day.length() == 1 ? "0" + bop_day : bop_day;

		HashMap<String, Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("function", txtFunction.getText());
		conditionMap.put("shop", txtShop.getText());
		conditionMap.put("ebom_release_date", bom_year + "-" + bom_month + "-" + bom_day);
		conditionMap.put("bop_release_date", bop_year + "-" + bop_month + "-" + bop_day);

		return conditionMap;
	}

	/**
	 * 
	 * 
	 * @method getTableDataList
	 * @date 2014. 3. 24.
	 * @param
	 * @return List<HashMap<String,Object>>
	 * @exception
	 * @throws
	 * @see
	 */
	public List<HashMap<String, Object>> getTableDataList() {
		List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();

		TableItem[] tableItems = table.getItems();
		for (TableItem tableItem : tableItems) {
			HashMap<String, Object> dataMap = new HashMap<String, Object>();

			dataMap.put("no", tableItem.getText(0));
			dataMap.put("project", tableItem.getText(1));
			dataMap.put("function_no", tableItem.getText(2));
			dataMap.put("seq", tableItem.getText(3));
			dataMap.put("bom_level", tableItem.getText(4));
			dataMap.put("part_no", tableItem.getText(5));
			dataMap.put("ver", tableItem.getText(6));
			dataMap.put("suppmode", tableItem.getText(7));
			dataMap.put("part_name", tableItem.getText(8));
			dataMap.put("options", tableItem.getText(9));
			dataMap.put("pos_desc", tableItem.getText(10));
			dataMap.put("u_category", tableItem.getText(11));
			dataMap.put("in_eco", tableItem.getText(12));
			dataMap.put("in_eco_released_date", tableItem.getText(13));
			dataMap.put("bp_date", tableItem.getText(14));
			dataMap.put("qty", tableItem.getText(15));
			dataMap.put("shop_code", tableItem.getText(16));
			dataMap.put("line_code", tableItem.getText(17));
			dataMap.put("line_rev", tableItem.getText(18));
			dataMap.put("station_code", tableItem.getText(19));
			dataMap.put("station_rev", tableItem.getText(20));
			dataMap.put("operation_id", tableItem.getText(21));
			dataMap.put("operation_rev", tableItem.getText(22));
			dataMap.put("operation_status", tableItem.getText(23));
			dataMap.put("operation_name", tableItem.getText(24));
			dataMap.put("operation_spec", tableItem.getText(25));
			dataMap.put("endItem_quantity", tableItem.getText(26));
			dataMap.put("in_meco", tableItem.getText(27));
			dataMap.put("in_meco_released_date", tableItem.getText(28));

			dataList.add(dataMap);
		}

		return dataList;
	}

	public Composite getComposite() {
		return this;
	}

	@Override
	public boolean isSavable() {
		return false;
	}

	@Override
	public void load() {

	}

	@Override
	public void save() {

	}

	/**
	 * Item 검색시 사용되는 Selection Adapter
	 */
	class ItemSelectionAdapter extends SelectionAdapter {
		/** 검색된 Item ID값이 Setting될 Field */
		Text targetText;
		/** 검색할 Item Type */
		String strItemType;

		/**
		 * Selection Adapter 생성자
		 * 
		 * @param targetText
		 *            : 검색된 Item ID값이 Setting될 Field
		 * @param strItemRevType
		 *            : 검색할 Item Type
		 */
		ItemSelectionAdapter(Text targetText, String strItemType) {
			this.targetText = targetText;
			this.strItemType = strItemType;
		}

		public void widgetSelected(SelectionEvent event) {
			SearchItemDialog itemDialog = new SearchItemDialog(getShell(), SWT.MULTI, strItemType);
			TCComponentItem[] selectedItems = (TCComponentItem[]) itemDialog.open();

			if (selectedItems != null) {
				try {
					String item_id = "";
					for (int i = 0; i < selectedItems.length; i++) {
						item_id += selectedItems[i].getProperty("item_id");
						if (i < selectedItems.length - 1) {
							item_id += ", ";
						}
					}
					targetText.setText(item_id);
					targetText.setData(selectedItems);
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
