package com.kgm.commands.bomviewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.kgm.common.operation.LoadInCatiaOperation;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.controls.SWTComboBox;

public class BOMViewerTablePanel extends Composite {
	private TCSession session;
	private Registry registry;
	private TableViewer tableViewer;
	private Table table;
	private ArrayList<HashMap<String, String>> resultData;
	
	private static final int IDX_LV = 0;
	private static final int IDX_PARENT_UID = 1;
	private static final int IDX_PARENT_NO = 2;
	private static final int IDX_PARENT_REV = 3;
	private static final int IDX_PARENT_NAME = 4;
	private static final int IDX_PARENT_TYPE = 5;
	private static final int IDX_PARENT_MATURITY = 6;
	private static final int IDX_PART_UID = 7;
	private static final int IDX_PART_NO = 8;
	private static final int IDX_PART_REV = 9;
	private static final int IDX_PART_NAME = 10;
	private static final int IDX_PART_TYPE = 11;
	private static final int IDX_PART_MATURITY = 12;
	private static final int IDX_OCC_THREADS = 13;
	private static final int IDX_SEQ = 14;
	private static final int IDX_POSITION_DESC = 15;
	private static final int IDX_SMODE = 16;
	private static final int IDX_APART = 17;
	private static final int IDX_MCODE = 18;
	private static final int IDX_CATPRODUCT = 19;
	private static final int IDX_CATPRODUCT_UID = 20;
	private static final int IDX_CATPART = 21;
	private static final int IDX_CATPART_UID = 22;
	private static final int IDX_CATDRAWING = 23;
	private static final int IDX_CATDRAWING_UID = 24;
	private static final int IDX_PDF = 25;
	private static final int IDX_PDF_UID = 26;
	
	private static final String COL_NAME_LV = "LV";
	private static final String COL_NAME_PARENT_UID = "PARENT_UID";
	private static final String COL_NAME_PARENT_NO = "Parent No";
	private static final String COL_NAME_PARENT_REV = "Parent Rev";
	private static final String COL_NAME_PARENT_NAME = "Parent Name";
	private static final String COL_NAME_PARENT_TYPE = "Parent Type";
	private static final String COL_NAME_PARENT_MATURITY = "Parent Maturity";
	private static final String COL_NAME_PART_UID = "PART_UID";
	private static final String COL_NAME_PART_NO = "Part No";
	private static final String COL_NAME_PART_REV = "Part Rev";
	private static final String COL_NAME_PART_NAME = "Part Name";
	private static final String COL_NAME_PART_TYPE = "Part Type";
	private static final String COL_NAME_PART_MATURITY = "Part Maturity";
	private static final String COL_NAME_OCC_THREADS = "OCC_THREADS";
	private static final String COL_NAME_SEQ = "SEQ";
	private static final String COL_NAME_POSITION_DESC = "Position Desc";
	private static final String COL_NAME_SMODE = "Supply Mode";
	private static final String COL_NAME_APART = "Alter Part";
	private static final String COL_NAME_MCODE = "Module Code";
	private static final String COL_NAME_CATPRODUCT = "CATProduct";
	private static final String COL_NAME_CATPRODUCT_UID = "CATPRODUCT_UID";
	private static final String COL_NAME_CATPART = "CATPart";
	private static final String COL_NAME_CATPART_UID = "CATPART_UID";
	private static final String COL_NAME_CATDRAWING = "CATDrawing";
	private static final String COL_NAME_CATDRAWING_UID = "CATDRAWING_UID";
	private static final String COL_NAME_PDF = "PDF";
	private static final String COL_NAME_PDF_UID = "PDF_UID";
	
	private static final int COL_WIDTH_LV = 50;
	private static final int COL_WIDTH_PARENT_UID = 0;
	private static final int COL_WIDTH_PARENT_NO = 110;
	private static final int COL_WIDTH_PARENT_REV = 45;
	private static final int COL_WIDTH_PARENT_NAME = 300;
	private static final int COL_WIDTH_PARENT_TYPE = 0;
	private static final int COL_WIDTH_PARENT_MATURITY = 80;
	private static final int COL_WIDTH_PART_UID = 0;
	private static final int COL_WIDTH_PART_NO = 110;
	private static final int COL_WIDTH_PART_REV = 45;
	private static final int COL_WIDTH_PART_NAME = 300;
	private static final int COL_WIDTH_PART_TYPE = 0;
	private static final int COL_WIDTH_PART_MATURITY = 80;
	private static final int COL_WIDTH_OCC_THREADS = 0;
	private static final int COL_WIDTH_SEQ = 80;
	private static final int COL_WIDTH_POSITION_DESC = 100;
	private static final int COL_WIDTH_SMODE = 50;
	private static final int COL_WIDTH_APART = 50;
	private static final int COL_WIDTH_MCODE = 50;
	private static final int COL_WIDTH_CATPRODUCT = 50;
	private static final int COL_WIDTH_CATPRODUCT_UID = 0;
	private static final int COL_WIDTH_CATPART = 50;
	private static final int COL_WIDTH_CATPART_UID = 0;
	private static final int COL_WIDTH_CATDRAWING = 50;
	private static final int COL_WIDTH_CATDRAWING_UID = 0;
	private static final int COL_WIDTH_PDF = 50;
	private static final int COL_WIDTH_PDF_UID = 0;
	
	private final Color evenColor = new Color(Display.getDefault(), 192, 214, 248);
	private final Color oddColor = new Color(Display.getDefault(), 255, 255, 255);
	
	private SWTComboBox cbLV;
	private SWTComboBox cbCATProduct;
	private SWTComboBox cbCATPart;
	private SWTComboBox cbCATDrawing;
	private SWTComboBox cbPDF;
	
	/**
	 * Constructor
	 * @param parent
	 * @param style
	 */
	public BOMViewerTablePanel(Composite parent, int style) {
		super(parent, style);
		setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		this.session = CustomUtil.getTCSession();
		this.registry = Registry.getRegistry("com.kgm.common.common");
		
		createFilter();
		
		initTable();
		
		this.layout();
	}
	
	/**
	 * Filter 영역 생성
	 */
	private void createFilter() {
		Composite cpsFilter = new Composite(this, SWT.NONE);
		cpsFilter.setLayout(new GridLayout(10, false));
		cpsFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label lblLV = new Label(cpsFilter, SWT.NONE);
		lblLV.setText("LV");
		cbLV = new SWTComboBox(cpsFilter, SWT.BORDER);
		cbLV.addPropertyChangeListener(getListener());
		
		Label lblCATProduct = new Label(cpsFilter, SWT.NONE);
		lblCATProduct.setText("CATProduct");
		cbCATProduct = new SWTComboBox(cpsFilter, SWT.BORDER);
		cbCATProduct.addItems(new String[] {"", "O", "X"}, new String[] {"", "O", "X"});
		cbCATProduct.setSelectedIndex(0);
		cbCATProduct.addPropertyChangeListener(getListener());
		
		Label lblCATPart = new Label(cpsFilter, SWT.NONE);
		lblCATPart.setText("CATPart");
		cbCATPart = new SWTComboBox(cpsFilter, SWT.BORDER);
		cbCATPart.addItems(new String[] {"", "O", "X"}, new String[] {"", "O", "X"});
		cbCATPart.setSelectedIndex(0);
		cbCATPart.addPropertyChangeListener(getListener());
		
		Label lblCATDrawing = new Label(cpsFilter, SWT.NONE);
		lblCATDrawing.setText("CATDrawing");
		cbCATDrawing = new SWTComboBox(cpsFilter, SWT.BORDER);
		cbCATDrawing.addItems(new String[] {"", "O", "X"}, new String[] {"", "O", "X"});
		cbCATDrawing.setSelectedIndex(0);
		cbCATDrawing.addPropertyChangeListener(getListener());
		
		Label lblPDF = new Label(cpsFilter, SWT.NONE);
		lblPDF.setText("PDF");
		cbPDF = new SWTComboBox(cpsFilter, SWT.BORDER);
		cbPDF.addItems(new String[] {"", "O", "X"}, new String[] {"", "O", "X"});
		cbPDF.setSelectedIndex(0);
		cbPDF.addPropertyChangeListener(getListener());
	}
	
	/**
	 * Combobox 값이 변경될때마다 Filtering을 수행하는 Listener 생성
	 * @return
	 */
	private IPropertyChangeListener getListener() {
		return new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent arg0) {
            	try {
					setInput();
				} catch (Exception e) {
					e.printStackTrace();
					MessageBox.post(e);
				}
            }
        };
	}

	/**
	 * Table 생성
	 */
	private void initTable() {
		tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        
		createTableColumn(BOMViewerTablePanel.COL_NAME_LV, BOMViewerTablePanel.COL_WIDTH_LV);
		createTableColumn(BOMViewerTablePanel.COL_NAME_PARENT_UID, BOMViewerTablePanel.COL_WIDTH_PARENT_UID);
		createTableColumn(BOMViewerTablePanel.COL_NAME_PARENT_NO, BOMViewerTablePanel.COL_WIDTH_PARENT_NO);
		createTableColumn(BOMViewerTablePanel.COL_NAME_PARENT_REV, BOMViewerTablePanel.COL_WIDTH_PARENT_REV);
		createTableColumn(BOMViewerTablePanel.COL_NAME_PARENT_NAME, BOMViewerTablePanel.COL_WIDTH_PARENT_NAME);
		createTableColumn(BOMViewerTablePanel.COL_NAME_PARENT_TYPE, BOMViewerTablePanel.COL_WIDTH_PARENT_TYPE);
		createTableColumn(BOMViewerTablePanel.COL_NAME_PARENT_MATURITY, BOMViewerTablePanel.COL_WIDTH_PARENT_MATURITY);
		createTableColumn(BOMViewerTablePanel.COL_NAME_PART_UID, BOMViewerTablePanel.COL_WIDTH_PART_UID);
		createTableColumn(BOMViewerTablePanel.COL_NAME_PART_NO, BOMViewerTablePanel.COL_WIDTH_PART_NO);
		createTableColumn(BOMViewerTablePanel.COL_NAME_PART_REV, BOMViewerTablePanel.COL_WIDTH_PART_REV);
		createTableColumn(BOMViewerTablePanel.COL_NAME_PART_NAME, BOMViewerTablePanel.COL_WIDTH_PART_NAME);
		createTableColumn(BOMViewerTablePanel.COL_NAME_PART_TYPE, BOMViewerTablePanel.COL_WIDTH_PART_TYPE);
		createTableColumn(BOMViewerTablePanel.COL_NAME_PART_MATURITY, BOMViewerTablePanel.COL_WIDTH_PART_MATURITY);
		createTableColumn(BOMViewerTablePanel.COL_NAME_OCC_THREADS, BOMViewerTablePanel.COL_WIDTH_OCC_THREADS);
		createTableColumn(BOMViewerTablePanel.COL_NAME_SEQ, BOMViewerTablePanel.COL_WIDTH_SEQ);
		createTableColumn(BOMViewerTablePanel.COL_NAME_POSITION_DESC, BOMViewerTablePanel.COL_WIDTH_POSITION_DESC);
		createTableColumn(BOMViewerTablePanel.COL_NAME_SMODE, BOMViewerTablePanel.COL_WIDTH_SMODE);
		createTableColumn(BOMViewerTablePanel.COL_NAME_APART, BOMViewerTablePanel.COL_WIDTH_APART);
		createTableColumn(BOMViewerTablePanel.COL_NAME_MCODE, BOMViewerTablePanel.COL_WIDTH_MCODE);
		createTableColumn(BOMViewerTablePanel.COL_NAME_CATPRODUCT, BOMViewerTablePanel.COL_WIDTH_CATPRODUCT);
		createTableColumn(BOMViewerTablePanel.COL_NAME_CATPRODUCT_UID, BOMViewerTablePanel.COL_WIDTH_CATPRODUCT_UID);
		createTableColumn(BOMViewerTablePanel.COL_NAME_CATPART, BOMViewerTablePanel.COL_WIDTH_CATPART);
		createTableColumn(BOMViewerTablePanel.COL_NAME_CATPART_UID, BOMViewerTablePanel.COL_WIDTH_CATPART_UID);
		createTableColumn(BOMViewerTablePanel.COL_NAME_CATDRAWING, BOMViewerTablePanel.COL_WIDTH_CATDRAWING);
		createTableColumn(BOMViewerTablePanel.COL_NAME_CATDRAWING_UID, BOMViewerTablePanel.COL_WIDTH_CATDRAWING_UID);
		createTableColumn(BOMViewerTablePanel.COL_NAME_PDF, BOMViewerTablePanel.COL_WIDTH_PDF);
		createTableColumn(BOMViewerTablePanel.COL_NAME_PDF_UID, BOMViewerTablePanel.COL_WIDTH_PDF_UID);
		
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent event) {
				try {
					openComponent(event.x, event.y);
				} catch (Exception e) {
					e.printStackTrace();
					MessageBox.post(e);
				}
			}
		});
	}
	
	/**
	 * Table에 선택한 항목을 My Teamcenter or Load In Catia로 보냄.
	 */
	private void openComponent(int iX, int iY) throws Exception {
		ViewerCell vcSelectedCell = tableViewer.getCell(new Point(iX, iY));
		
		if (vcSelectedCell == null) {
			return;
		}
		
		int iSelectedColumn = vcSelectedCell.getColumnIndex();
		int iSelectedRow = table.getSelectionIndex();

		TableItem item = table.getItem(iSelectedRow);
		TCComponentItemRevision revision = null;
		
		revision = getItemRevision(item.getText(BOMViewerTablePanel.IDX_PART_UID));
		
		if (iSelectedColumn == BOMViewerTablePanel.IDX_CATPRODUCT) {
			TCComponentDataset dataset = getDataset(item.getText(BOMViewerTablePanel.IDX_CATPRODUCT_UID));
			openDataset(dataset, revision, iSelectedColumn);
		} else if (iSelectedColumn == BOMViewerTablePanel.IDX_CATPART) {
			TCComponentDataset dataset = getDataset(item.getText(BOMViewerTablePanel.IDX_CATPART_UID));
			openDataset(dataset, revision, iSelectedColumn);
		} else if (iSelectedColumn == BOMViewerTablePanel.IDX_CATDRAWING) {
			TCComponentDataset dataset = getDataset(item.getText(BOMViewerTablePanel.IDX_CATDRAWING_UID));
			openDataset(dataset, revision, iSelectedColumn);
		} else if (iSelectedColumn == BOMViewerTablePanel.IDX_PDF) {
			TCComponentDataset dataset = getDataset(item.getText(BOMViewerTablePanel.IDX_PDF_UID));
			openDataset(dataset, revision, iSelectedColumn);
		} else {
			TCComponentItem cItem = revision.getItem();
			openComponent(cItem);
		}
		
	}
	
	/**
	 * Open Dataset
	 * @param dataset
	 */
	private void openDataset(TCComponentDataset dataset, TCComponentItemRevision revision, int iSelectedColumn) {
		try {
			if (iSelectedColumn == BOMViewerTablePanel.IDX_CATPRODUCT || iSelectedColumn == BOMViewerTablePanel.IDX_CATPART || iSelectedColumn == BOMViewerTablePanel.IDX_CATDRAWING) {
				TCComponentRole role = session.getRole();
				String sRoleName = role.getProperty("role_name");
				
				// 특정 Role의 사용자들만 CATIA Open 가능하도록 설정.
				if (!(sRoleName.equals("RND_ENGINEER1") || sRoleName.equals("RND_ENGINEER2") || sRoleName.equals("BOMADMIN") || sRoleName.equals("DRAWINGCOMPLETEADMIN") || sRoleName.equals("DBA"))) {
					MessageBox.post("Check Role.", "Can't open a dataset.\nInvalid Role for open in catia v5.", "Dataset Open Error", MessageBox.ERROR);
					return;
				}
				
				loadCATIA(dataset, revision);
			} else if (iSelectedColumn == BOMViewerTablePanel.IDX_PDF) {
				loadPDF(dataset);
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
	}
	
	/**
	 * Load In CATIA
	 * @param dataset
	 * @param revision
	 * @throws Exception
	 */
	private void loadCATIA(TCComponentDataset dataset, TCComponentItemRevision revision) throws Exception {
		LoadInCatiaOperation operation = new LoadInCatiaOperation(dataset, revision);
		operation.execute(null);
	}
	
	/**
	 * Load PDF
	 * @param dataset
	 * @throws Exception
	 */
	private void loadPDF(TCComponentDataset dataset) throws Exception {
		dataset.open();
	}
	
	/**
	 * Open Item in My Teamcenter
	 * @param item
	 */
	private void openComponent(TCComponentItem item) {
		AIFUtility.getCurrentOpenService().open(item);
	}

	/**
	 * Create Table Column
	 * @param columnName
	 * @param width
	 */
    public void createTableColumn(String columnName, int width) {
        final TableColumn tableColumn = new TableColumn(table, SWT.NONE);
        tableColumn.setText(columnName);
        tableColumn.setToolTipText(columnName);
        tableColumn.setWidth(width);
        tableColumn.setMoveable(true);
    }
    
    public void setResultData(ArrayList<HashMap<String, String>> resultData) {
    	this.resultData = resultData;
    }
    
    /**
     * Set Result To Table
     * @param resultData
     */
    public void setInput() throws Exception {
    	table.removeAll();
    	
    	for (int inx = 0; inx < resultData.size(); inx++) {
			HashMap<String, String> hmData = resultData.get(inx);
			
			// Filtering
			if (isFiltered(hmData)) {
				continue;
			}
			
			TableItem item = new TableItem(table, SWT.NONE);
			
			item.setText(BOMViewerTablePanel.IDX_LV, getString(hmData.get(BOMViewerConstants.PROP_LV)));
			item.setText(BOMViewerTablePanel.IDX_PARENT_UID, getString(hmData.get(BOMViewerConstants.PROP_PARENT_UID)));
			item.setText(BOMViewerTablePanel.IDX_PARENT_NO, getString(hmData.get(BOMViewerConstants.PROP_PARENT_NO)));
			item.setText(BOMViewerTablePanel.IDX_PARENT_REV, getString(hmData.get(BOMViewerConstants.PROP_PARENT_REV)));
			item.setText(BOMViewerTablePanel.IDX_PARENT_NAME, getString(hmData.get(BOMViewerConstants.PROP_PARENT_NAME)));
			item.setText(BOMViewerTablePanel.IDX_PARENT_TYPE, getString(hmData.get(BOMViewerConstants.PROP_PARENT_TYPE)));
			item.setText(BOMViewerTablePanel.IDX_PARENT_MATURITY, getString(hmData.get(BOMViewerConstants.PROP_PARENT_MATURITY)));
			item.setText(BOMViewerTablePanel.IDX_PART_UID, getString(hmData.get(BOMViewerConstants.PROP_PART_UID)));
			item.setText(BOMViewerTablePanel.IDX_PART_NO, getString(hmData.get(BOMViewerConstants.PROP_PART_NO)));
			item.setText(BOMViewerTablePanel.IDX_PART_REV, getString(hmData.get(BOMViewerConstants.PROP_PART_REV)));
			item.setText(BOMViewerTablePanel.IDX_PART_NAME, getString(hmData.get(BOMViewerConstants.PROP_PART_NAME)));
			item.setText(BOMViewerTablePanel.IDX_PART_TYPE, getString(hmData.get(BOMViewerConstants.PROP_PART_TYPE)));
			item.setText(BOMViewerTablePanel.IDX_PART_MATURITY, getString(hmData.get(BOMViewerConstants.PROP_PART_MATURITY)));
			item.setText(BOMViewerTablePanel.IDX_OCC_THREADS, getString(hmData.get(BOMViewerConstants.PROP_OCC_THREADS)));
			item.setText(BOMViewerTablePanel.IDX_SEQ, getString(hmData.get(BOMViewerConstants.PROP_SEQ)));
			item.setText(BOMViewerTablePanel.IDX_POSITION_DESC, getString(hmData.get(BOMViewerConstants.PROP_POSITION_DESC)));
			item.setText(BOMViewerTablePanel.IDX_SMODE, getString(hmData.get(BOMViewerConstants.PROP_SMODE)));
			item.setText(BOMViewerTablePanel.IDX_APART, getString(hmData.get(BOMViewerConstants.PROP_APART)));
			item.setText(BOMViewerTablePanel.IDX_MCODE, getString(hmData.get(BOMViewerConstants.PROP_MCODE)));
			item.setImage(BOMViewerTablePanel.IDX_CATPRODUCT, getImage(hmData.get(BOMViewerConstants.PROP_CATPRODUCT), BOMViewerConstants.TYPE_DATASET_CATPRODUCT));
			item.setText(BOMViewerTablePanel.IDX_CATPRODUCT_UID, getString(hmData.get(BOMViewerConstants.PROP_CATPRODUCT)));
			item.setImage(BOMViewerTablePanel.IDX_CATPART, getImage(hmData.get(BOMViewerConstants.PROP_CATPART), BOMViewerConstants.TYPE_DATASET_CATPART));
			item.setText(BOMViewerTablePanel.IDX_CATPART_UID, getString(hmData.get(BOMViewerConstants.PROP_CATPART)));
			item.setImage(BOMViewerTablePanel.IDX_CATDRAWING, getImage(hmData.get(BOMViewerConstants.PROP_CATDRAWING), BOMViewerConstants.TYPE_DATASET_CATDRAWING));
			item.setText(BOMViewerTablePanel.IDX_CATDRAWING_UID, getString(hmData.get(BOMViewerConstants.PROP_CATDRAWING)));
			item.setImage(BOMViewerTablePanel.IDX_PDF, getImage(hmData.get(BOMViewerConstants.PROP_PDF), BOMViewerConstants.TYPE_DATASET_PDF));
			item.setText(BOMViewerTablePanel.IDX_PDF_UID, getString(hmData.get(BOMViewerConstants.PROP_PDF)));
			
			item.setData("hmData", hmData);
		}
    	
    	TableItem[] items = table.getItems();
    	for (int inx = 0; inx < items.length; inx++) {
			if (inx % 2 == 0) {
				items[inx].setBackground(evenColor);
			} else {
				items[inx].setBackground(oddColor);
			}
		}
    }
    
    /**
     * 
     * @param resultData
     */
    public void setFilter() {
    	ArrayList<String> alLV = new ArrayList<String>();
    	
    	cbLV.addItem("", "");
    	for (int inx = 0; inx < resultData.size(); inx++) {
    		HashMap<String, String> hmData = resultData.get(inx);
    		String sLV = getString(hmData.get(BOMViewerConstants.PROP_LV));
    		
    		if (!alLV.contains(sLV)) {
    			alLV.add(sLV);
			}
		}
    	
    	Collections.sort(alLV);
    	cbLV.addItems(alLV.toArray(), alLV.toArray());
    }
    
    /**
     * Filtering 여부
     * @param hmData
     * @return
     */
    private boolean isFiltered(HashMap<String, String> hmData) {
    	boolean isFiltered = false;
    	String sLV = getString(hmData.get(BOMViewerConstants.PROP_LV));
    	String sCATProduct = getString(hmData.get(BOMViewerConstants.PROP_CATPRODUCT));
    	String sCATPart = getString(hmData.get(BOMViewerConstants.PROP_CATPART));
    	String sCATDrawing = getString(hmData.get(BOMViewerConstants.PROP_CATDRAWING));
    	String sPDF = getString(hmData.get(BOMViewerConstants.PROP_PDF));
    	
    	String sFilterLV = getString(cbLV.getTextField().getText());
    	String sFilterCATProduct = getString(cbCATProduct.getTextField().getText());
    	String sFilterCATPart = getString(cbCATPart.getTextField().getText());
    	String sFilterCATDrawing = getString(cbCATDrawing.getTextField().getText());
    	String sFilterPDF = getString(cbPDF.getTextField().getText());
    	
    	if (!sFilterLV.equals("") && !(Integer.valueOf(sFilterLV) >= Integer.valueOf(sLV))) {
			isFiltered = true;
		}
    	
    	if (!isFiltered && !sFilterCATProduct.equals("") && ((sFilterCATProduct.equals("O") && sCATProduct.equals("")) || (sFilterCATProduct.equals("X") && !sCATProduct.equals(""))) ) {
    		isFiltered = true;
    	}
    	
    	if (!isFiltered && !sFilterCATPart.equals("") && ((sFilterCATPart.equals("O") && sCATPart.equals("")) || (sFilterCATPart.equals("X") && !sCATPart.equals(""))) ) {
    		isFiltered = true;
    	}
    	
    	if (!isFiltered && !sFilterCATDrawing.equals("") && ((sFilterCATDrawing.equals("O") && sCATDrawing.equals("")) || (sFilterCATDrawing.equals("X") && !sCATDrawing.equals(""))) ) {
    		isFiltered = true;
    	}
    	
    	if (!isFiltered && !sFilterPDF.equals("") && ((sFilterPDF.equals("O") && sPDF.equals("")) || (sFilterPDF.equals("X") && !sPDF.equals(""))) ) {
    		isFiltered = true;
    	}
    	
    	return isFiltered;
    }
    
    /**
     * Get Null String To Empty.
     * @param object
     * @return
     */
    private String getString(Object object) {
    	if (object == null) {
			return "";
		} else {
			return object.toString();
		}
    }
    
    /**
     * Get Image
     * @param object
     * @return
     * @throws Exception
     */
    private Image getImage(Object object, String sKey) throws Exception {
    	Image image = null;
    	if (object == null) {
			return null;
		} else {
			if (sKey.equals(BOMViewerConstants.TYPE_DATASET_CATPRODUCT)) {
				image = registry.getImage("CATProduct.ICON");
			} else if (sKey.equals(BOMViewerConstants.TYPE_DATASET_CATPART)) {
				image = registry.getImage("CATPart.ICON");
			} else if (sKey.equals(BOMViewerConstants.TYPE_DATASET_CATDRAWING)) {
				image = registry.getImage("CATDrawing.ICON");
			} else if (sKey.equals(BOMViewerConstants.TYPE_DATASET_PDF)) {
				image = registry.getImage("PDF.ICON");
			}
			
			return image;
		}
    }
    
    /**
     * Get Item Revision From Table
     * @param object
     * @return
     * @throws Exception
     */
    private TCComponentItemRevision getItemRevision(Object object) throws Exception {
    	TCComponentItemRevision revision = null;
    	if (object == null) {
			return null;
		} else {
			TCComponent component = session.stringToComponent(getString(object));
			
			if (component instanceof TCComponentItemRevision) {
				revision = (TCComponentItemRevision) component;
			}
			
			return revision;
		}
    }
    
    /**
     * Get Dataset From Table
     * @param object
     * @return
     * @throws Exception
     */
    private TCComponentDataset getDataset(Object object) throws Exception {
    	TCComponentDataset dataset = null;
    	if (object == null) {
			return null;
		} else {
			TCComponent component = session.stringToComponent(getString(object));
			
			if (component instanceof TCComponentDataset) {
				dataset = (TCComponentDataset) component;
			}
			
			return dataset;
		}
    }
}

