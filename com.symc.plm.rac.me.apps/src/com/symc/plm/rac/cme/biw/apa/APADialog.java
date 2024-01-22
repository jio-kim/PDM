package com.symc.plm.rac.cme.biw.apa;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.IProgressService;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.rac.cme.biw.apa.resulttable.datastructure.PartData;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aif.kernel.AbstractAIFSession;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.biw.Activator;
import com.teamcenter.rac.cme.biw.apa.utils.APAViewer;
import com.teamcenter.rac.cme.biw.apa.utils.ExportTabletoExcel;
import com.teamcenter.rac.cme.biw.apa.utils.SettingsDialog;
import com.teamcenter.rac.cme.framework.treetable.CMEBOMTreeTable;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.print.GridInfo;
import com.teamcenter.rac.psebase.AbstractBOMLineViewerApplication;
import com.teamcenter.rac.psebase.common.AbstractViewableTreeTable;
import com.teamcenter.rac.services.IPrintService;
import com.teamcenter.rac.util.Cookie;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.OSGIUtil;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.dialog.AbstractSWTDialog;

// Referenced classes of package com.teamcenter.rac.cme.biw.apa:
//            APATableComparator, APAColumnLabelProvider, APAConnectionModel, APAContentProvider


/**
 * [SR140611-032][20140611] jwlee 결합판넬 검색 개수를 15개에서 25개로 변경.
 *  결합판넬 Dialog에 표시되는 개수를 Preference로 조절할 수 있도로 변경.
 *  결합판넬 Dialog에 표시되는 개수를 검색대상에서 Assay Item은 제외 (Part ID의 다섯번째 자리가 0이면 Assay Item이니 이것들은 검색대상에서 제외 처리함)
 * [SR140721-004][20140729] shcho, 결합판넬 Dialog에 표시되는 개수를 Dialog에서 사용자가 입력한 값으로 조절 할 수 있도로 변경.
 * [NON-SR][20150703] shcho, 검색 결과가 화면에 올바로 반영되지 않는 경우가 간헐적으로 발생하여 동기화 처리 로직 추가
 */
@SuppressWarnings("restriction")
public class APADialog extends AbstractSWTDialog
{
    private static final Logger logger = Logger.getLogger(APADialog.class);

    public static final String APA_COOKIE = "MEAPACookie";
    public static final int COLUMN_INDEX_WELD_NAME = 0;
    public static final int COLUMN_INDEX_WELD_NUMBER_OF_SHEET = 1;
    // [SR151207-042][20151209] taeku.jeong Find No 추가
    public static final int COLUMN_INDEX_FIND_NO = 2;
    public static final int COLUMN_INDEX_WELD_TYPE = 3;
    
    public static final int COLUMN_INDEX_PART1 = 4;
    public static final int COLUMN_INDEX_PART2 = 5;
    public static final int COLUMN_INDEX_PART3 = 6;
    public static final int COLUMN_INDEX_PART4 = 7;
    // jwlee 소스 변경 시작
    public static final int COLUMN_INDEX_PART5 = 8;
    public static final int COLUMN_INDEX_PART6 = 9;
    public static final int COLUMN_INDEX_PART7 = 10;
    public static final int COLUMN_INDEX_PART8 = 11;
    public static final int COLUMN_INDEX_PART9 = 12;
    public static final int COLUMN_INDEX_PART10 = 13;
    public static final int COLUMN_INDEX_PART11 = 14;
    public static final int COLUMN_INDEX_PART12 = 15;
    public static final int COLUMN_INDEX_PART13 = 16;
    public static final int COLUMN_INDEX_PART14 = 17;
    public static final int COLUMN_INDEX_PART15 = 18;
    // [SR140611-032][20140611] jwlee 결합판넬 검색 개수를 15개에서 25개로 변경.
    public static final int COLUMN_INDEX_PART16 = 19;
    public static final int COLUMN_INDEX_PART17 = 120;
    public static final int COLUMN_INDEX_PART18 = 21;
    public static final int COLUMN_INDEX_PART19 = 22;
    public static final int COLUMN_INDEX_PART20 = 23;
    public static final int COLUMN_INDEX_PART21 = 24;
    public static final int COLUMN_INDEX_PART22 = 25;
    public static final int COLUMN_INDEX_PART23 = 26;
    public static final int COLUMN_INDEX_PART24 = 27;
    public static final int COLUMN_INDEX_PART25 = 28;
    //public static final int MAX_COLUMN_INDEX = 5;
    public static final int MAX_COLUMN_INDEX = 29;
    // jwlee 소스 변경 끝
    
    public static final int CusomAddedColumnCount = 3;

    private final String defaultColumnWidth = "120";
     // [SR151207-042][20151209] taeku.jeong Find No 추가
    private final String columnTitles[] = {
        getReg().getString("MFGName_Header"), getReg().getString("MFGType_Number_of_Sheet"), getReg().getString("MFGFindNo_Header"), getReg().getString("MFGType_Header"), getReg().getString("Part1_Header"),
        getReg().getString("Part2_Header"), getReg().getString("Part3_Header"), getReg().getString("Part4_Header"),
        // jwlee 소스변경 시작
        getReg().getString("Part5_Header"), getReg().getString("Part6_Header"), getReg().getString("Part7_Header"),
        getReg().getString("Part8_Header"), getReg().getString("Part9_Header"), getReg().getString("Part10_Header"),
        getReg().getString("Part11_Header"), getReg().getString("Part12_Header"), getReg().getString("Part13_Header"),
        getReg().getString("Part14_Header"), getReg().getString("Part15_Header")
        // [SR140611-032][20140611] jwlee 결합판넬 검색 개수를 15개에서 25개로 변경.
        ,getReg().getString("Part16_Header"), getReg().getString("Part17_Header"),getReg().getString("Part18_Header"), getReg().getString("Part19_Header")
        ,getReg().getString("Part20_Header"),getReg().getString("Part21_Header"),getReg().getString("Part22_Header"),getReg().getString("Part23_Header")
        ,getReg().getString("Part24_Header"),getReg().getString("Part25_Header")
        // jwlee 소스변경 끝
    };

    private Shell shell;
    private Registry reg;
    private AbstractAIFSession session;
    private TCComponentBOMLine selectedComponent;
    private TCComponentBOMLine rootBomline;
    private AbstractBOMLineViewerApplication viewerApp;

    private boolean jobRunning;
    private boolean filterMFGType;
    private boolean empty;

    protected ArrayList<String> filterTypes;
    private Grid grid;
    private GridTableViewer tableViewer;
    private APAContentProvider contentProvider;
    private Button searchButton;
    private Button assignButton;
    private Button assignAllButton;
    private Button closeButton;
    private ToolBar upperBar;
    private Menu typeMenu;
    private MenuItem defaultMenuItem;

    public boolean setDialogVisible;

    // jwlee 소스변경 시작
    //String columnWidths[];
    // [SR151207-042][20151209] taeku.jeong Find No 추가
    protected String columnWidths[] = {"100", "30", "30", "100", "100", "100", "100", "100", "100", "100", "100", "100",
            // [SR140611-032][20140611] jwlee 결합판넬 검색 개수를 15개에서 25개로 변경.
                             "100", "100", "100", "100", "100", "100", "100"
                             ,"100", "100", "100", "100", "100", "100", "100", "100", "100", "100"};
            //---------------------------------------------------------------------------
    // jwlee 소스변경 끝
    protected GridItem selectedItem;
    protected List<GridItem> selectedItems;
    protected int selectedColumn;

//    private Text searchConditionText;

//    private int searchCondition;

    public APADialog(Shell shell1, AbstractAIFSession abstractaifsession, TCComponentBOMLine tccomponentbomline, TCComponentBOMLine productBOMLine)
    {
        super(shell1);
        session = null;
        selectedComponent = null;
        rootBomline = null;
        viewerApp = null;
        setDialogVisible = false;
        reg = null;
        filterMFGType = false;
        filterTypes = null;
        jobRunning = false;
        grid = null;
        selectedItems = new ArrayList<GridItem>();
        empty = true;
        setShellStyle(2160);
        setBlockOnOpen(false);
        shell = shell1;
        session = abstractaifsession;
        selectedComponent = tccomponentbomline;
        //jwlee 선택된 BOMLine 정보
        rootBomline = productBOMLine;
    }

    protected void configureShell(Shell shell1)
    {
        super.configureShell(shell1);
        shell1.setText(getReg().getString("APADialog.TITLE"));
        shell1.setMinimumSize(350, 170);
    }

    private void loadTableData()
    {
        if(jobRunning)
            return;
        IProgressService iprogressservice = PlatformUI.getWorkbench().getProgressService();
        try
        {
            iprogressservice.busyCursorWhile(new IRunnableWithProgress() {

                public void run(IProgressMonitor iprogressmonitor)
                {
                    iprogressmonitor.beginTask(getReg().getString("APADialog.LoadingMessage"), -1);
                    getContentProvider().generateData(getSelectedComponent());
                    iprogressmonitor.done();
                }
            });
        }
        catch(InvocationTargetException invocationtargetexception)
        {
            logger.error(invocationtargetexception.getClass().getName(), invocationtargetexception);
            MessageBox.post(invocationtargetexception);
        }
        catch(InterruptedException interruptedexception)
        {
            logger.error(interruptedexception.getClass().getName(), interruptedexception);
            MessageBox.post(interruptedexception);
        }
        jobRunning = false;
    }

    Image getIcon(String s)
    {
        Image image = null;
        ImageDescriptor imagedescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.getDefault().getBundleSymbolicName(), s);
        if(imagedescriptor != null)
            image = imagedescriptor.createImage();
        return image;
    }

    public int open()
    {
        viewerApp = (AbstractBOMLineViewerApplication)AIFUtility.getCurrentApplication();
        // jwlee Dialog(Connected to) 를 띄우기전 선택한 view 와 연결된 E-BOM view 정보를 같이 가져온다
        //rootBomline = getContentProvider().setBOMLine(selectedComponent);

        //MProduct 자동 Expand 기능 추가
        /*
        AbstractViewableTreeTable[] abstractViewableTreeTables = viewerApp.getViewableTreeTables();
        for(AbstractViewableTreeTable treeTable : abstractViewableTreeTables) {
            if(treeTable instanceof CMEBOMTreeTable) {
                if(rootBomline.equals(((CMEBOMTreeTable) treeTable).getRoot())) {
                    SDVBOPUtilities.executeExpandOneLevel(treeTable);
                    try {
                        treeTable.getBOMWindow().refresh();
                        rootBomline.refresh();
                    } catch (TCException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        */
                
        return super.open();

    }

    //[SR140721-004][20140729] shcho, 결합판넬 Dialog에 표시되는 개수를 Dialog에서 사용자가 입력한 값으로 조절 할 수 있도로 변경.
    protected void createDialogWindow(Composite composite) {
        Composite secondComposite = new Composite(composite, SWT.NONE);
        GridData gdSubComposite = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        secondComposite.setLayoutData(gdSubComposite);
        secondComposite.setLayout(new FormLayout());

        upperBar = new ToolBar(secondComposite, 0x800100);
        addTypeMenuToUpperBar(secondComposite);
        addButtonToUpperBar(getReg().getString("APADialog.SettingsIcon.ICON"), 1027, getReg().getString("APADialog.settingButton.ToolTip"), 8, true);
        addButtonToUpperBar(getReg().getString("APADialog.Filter_assigned_partsIcon.ICON"), 1028, getReg().getString("APADialog.filteroutAssignedButton.ToolTip"), 32, true);
        addButtonToUpperBar(getReg().getString("APADialog.RemovePartIcon.ICON"), 1029, getReg().getString("APADialog.removepartButton.ToolTip"), 8, false);
        addButtonToUpperBar(getReg().getString("APADialog.AddPartIcon.ICON"), 1030, getReg().getString("APADialog.addpartButton.ToolTip"), 8, false);
        addButtonToUpperBar(getReg().getString("APADialog.DisplayIcon.ICON"), 1031, getReg().getString("APADialog.displayButton.ToolTip"), 8, false);
        addButtonToUpperBar(getReg().getString("APADialog.PrintIcon.ICON"), 1032, getReg().getString("APADialog.printButton.ToolTip"), 8, true);
        addButtonToUpperBar(getReg().getString("APADialog.ExportToExcelIcon.ICON"), 1033, getReg().getString("APADialog.toexcelButton.ToolTip"), 8, true);
        upperBar.pack();
        
        FormData fdUpperBar = new FormData();
        fdUpperBar.top = new FormAttachment(0);
        fdUpperBar.left = new FormAttachment(0);
        upperBar.setLayoutData(fdUpperBar);
        
        FormData fdthirdComposite = new FormData();
        fdthirdComposite.top = new FormAttachment(0);
        fdthirdComposite.right = new FormAttachment(100, 0);
        fdthirdComposite.width = 200;
        
        Composite thirdComposite = new Composite(secondComposite, SWT.NONE);
        thirdComposite.setLayoutData(fdthirdComposite);
        thirdComposite.setLayout(new GridLayout(2, false));
        
//        searchConditionText = new Text(thirdComposite, SWT.BORDER);
//        searchConditionText.setText("25");
//        searchConditionText.setEditable(true);
        
//        GridData gdSearchConditionText = new GridData();
//        gdSearchConditionText.horizontalAlignment = SWT.RIGHT;
//        gdSearchConditionText.widthHint = 100;
//        gdSearchConditionText.grabExcessHorizontalSpace = true;
//        searchConditionText.setLayoutData(gdSearchConditionText);
//        searchConditionText.addVerifyListener(new VerifyListener() {
//
//            @Override
//            public void verifyText(VerifyEvent e) {
//                switch (e.keyCode) {
//                case SWT.BS: // Backspace
//                case SWT.DEL: // Delete
//                case SWT.HOME: // Home
//                case SWT.END: // End
//                case SWT.ARROW_LEFT: // Left arrow
//                case SWT.ARROW_RIGHT: // Right arrow
//                    return;
//                }
//
//                if (!Character.isDigit(e.character)) { // NUMERIC
//                    e.doit = false; // disallow the action
//                }
//                return;
//            }
//
//        });

        searchButton = createButton(thirdComposite, 1026, getReg().getString("APADialog.search"), false);
        GridData gdSearchButton = new GridData();
        gdSearchButton.horizontalAlignment = SWT.RIGHT;
        gdSearchButton.widthHint = 100;
        gdSearchButton.grabExcessHorizontalSpace = true;
        searchButton.setLayoutData(gdSearchButton);
        
        thirdComposite.layout();
        secondComposite.layout();
        composite.layout();

        addTableViewer(composite);
    }

    private void addTableViewer(Composite composite)
    {
        grid = new Grid(composite, 2838);
        GridData griddata = new GridData();
        griddata.verticalAlignment = 4;
        griddata.horizontalAlignment = 4;
        griddata.grabExcessVerticalSpace = true;
        griddata.grabExcessHorizontalSpace = true;
        grid.setLayoutData(griddata);
        grid.setHeaderVisible(true);
        grid.setRowHeaderVisible(true);
        grid.setLinesVisible(true);
        grid.setCellSelectionEnabled(true);
        addColumns(composite);
        tableViewer.setContentProvider(getContentProvider());
        addTableListener(grid);
        tableViewer.setInput(getSelectedComponent());
       // tableViewer.setInput(getRootBomline());

        tableViewer.refresh();
        modifyTableView();
    }

    private void addTableListener(final Grid table)
    {
        table.addListener(13, new Listener() {

            public void handleEvent(Event event)
            {
                selectedItems.clear();
                table.getSelectionIndex();
                Point apoint[] = table.getCellSelection();
                HashSet<GridItem> hashset = new HashSet<GridItem>();
                Point apoint1[] = apoint;
                int i = apoint1.length;
                for(int j = 0; j < i; j++)
                {
                    Point point1 = apoint1[j];
                    if(point1.x == 0 || point1.x == 1)
                        hashset.add(table.getItem(point1.y));
                }

                selectedItems.addAll(hashset);
                updateControls(apoint);
                Point point = new Point(event.x, event.y);
                GridItem griditem = table.getItem(point);
                if(griditem == null)
                    return;
                else
                    return;
            }
        });
    }

    private void updateControls(Point apoint[])
    {
        assignButton.setEnabled(false);
        setEnabledToolBarItem(1029, false);
        setEnabledToolBarItem(1030, false);
        Point apoint1[] = apoint;
        int i = apoint1.length;
        for(int j = 0; j < i; j++)
        {
            Point point = apoint1[j];
            int k = point.x;
            int l = point.y;

            String s = grid.getColumn(k).getText();
            GridItem griditem = grid.getItem(l);
            String s1 = griditem.getText(k);
            if(s1 == null || s1.length() <= 0)
                continue;
            setEnabledToolBarItem(1031, true);
            // jwlee 소스변경 Part4개까지만 체크 하던것을 part17 로 늘림
            if(getReg().getString("Part1_Header").equals(s) || getReg().getString("Part2_Header").equals(s) || getReg().getString("Part3_Header").equals(s) || getReg().getString("Part4_Header").equals(s) ||
               getReg().getString("Part5_Header").equals(s) || getReg().getString("Part6_Header").equals(s) ||
               getReg().getString("Part7_Header").equals(s) || getReg().getString("Part8_Header").equals(s) ||
               getReg().getString("Part9_Header").equals(s) || getReg().getString("Part10_Header").equals(s) ||
               getReg().getString("Part11_Header").equals(s) || getReg().getString("Part12_Header").equals(s) ||
               getReg().getString("Part13_Header").equals(s) || getReg().getString("Part14_Header").equals(s) ||
               getReg().getString("Part15_Header").equals(s) || getReg().getString("Part16_Header").equals(s) ||
               getReg().getString("Part17_Header").equals(s) || getReg().getString("Part18_Header").equals(s)
               // [SR140611-032][20140611] jwlee 결합판넬 검색 개수를 15개에서 25개로 변경.
               ||getReg().getString("Part19_Header").equals(s) || getReg().getString("Part20_Header").equals(s)
               ||getReg().getString("Part21_Header").equals(s)||getReg().getString("Part22_Header").equals(s)
               ||getReg().getString("Part23_Header").equals(s)||getReg().getString("Part24_Header").equals(s)
               ||getReg().getString("Part25_Header").equals(s)
               //----------------------------------------------------------------------
               )
            {
                PartData partdata = getContentProvider().getPartData(griditem.getData(), k);
                if(partdata.isDoConnection())
                    setEnabledToolBarItem(1029, true);
                else
                    setEnabledToolBarItem(1030, true);
            }
            else
            {
                assignButton.setEnabled(true);
            }
        }

    }

    private void setEnabledToolBarItem(int i, boolean flag)
    {
        ToolItem atoolitem[] = upperBar.getItems();
        int j = atoolitem.length;
        for(int k = 0; k < j; k++)
        {
            ToolItem toolitem = atoolitem[k];
            if(i == ((Integer)toolitem.getData()).intValue())
                toolitem.setEnabled(flag);
        }

    }

    private void addColumns(Composite composite)
    {
        Cookie cookie = Cookie.getCookie("MEAPACookie", true);
        //columnWidths = cookie.getStringArray((new StringBuilder()).append(getClass().getName()).append(".COLUMN_WIDTH").toString());
        try
        {
            cookie.close();
        }
        catch(IOException ioexception)
        {
            logger.error(ioexception.getClass().getName(), ioexception);
        }
        tableViewer = new GridTableViewer(grid);
        APATableComparator apatablecomparator = new APATableComparator();
        GridColumn gridcolumn = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn, columnTitles[0], 0);

        GridColumn gridcolumn1 = new GridColumn(grid, SWT.CENTER);
        addColumn(apatablecomparator, gridcolumn1, columnTitles[1], 1);
        GridColumn gridcolumn2 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn2, columnTitles[2], 2);
        GridColumn gridcolumn3 = new GridColumn(grid, SWT.LEFT);

        addColumn(apatablecomparator, gridcolumn3, columnTitles[3], 3);
        GridColumn gridcolumn4 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn4, columnTitles[4], 4);
        GridColumn gridcolumn5 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn5, columnTitles[5], 5);
        // jwlee 소스변경 시작
        GridColumn gridcolumn6 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn6, columnTitles[6], 6);
        GridColumn gridcolumn7 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn7, columnTitles[7], 7);
        GridColumn gridcolumn8 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn8, columnTitles[8], 8);
        GridColumn gridcolumn9 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn9, columnTitles[9], 9);
        GridColumn gridcolumn10 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn10, columnTitles[10], 10);
        GridColumn gridcolumn11 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn11, columnTitles[11], 11);
        GridColumn gridcolumn12 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn12, columnTitles[12], 12);
        GridColumn gridcolumn13 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn13, columnTitles[13], 13);
        GridColumn gridcolumn14 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn14, columnTitles[14], 14);
        GridColumn gridcolumn15 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn15, columnTitles[15], 15);
        GridColumn gridcolumn16 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn16, columnTitles[16], 16);
        GridColumn gridcolumn17 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn17, columnTitles[17], 17);

        //[SR140611-032][20140611] jwlee 결합판넬 검색 개수를 15개에서 25개로 변경.
        GridColumn gridcolumn18 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn18, columnTitles[18], 18);
        GridColumn gridcolumn19 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn19, columnTitles[19], 19);
        GridColumn gridcolumn20 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn20, columnTitles[20], 20);
        GridColumn gridcolumn21 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn21, columnTitles[21], 21);
        GridColumn gridcolumn22 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn22, columnTitles[22], 22);
        GridColumn gridcolumn23 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn23, columnTitles[23], 23);
        GridColumn gridcolumn24 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn24, columnTitles[24], 24);
        GridColumn gridcolumn25 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn25, columnTitles[25], 25);
        GridColumn gridcolumn26 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn26, columnTitles[26], 26);
        GridColumn gridcolumn27 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn27, columnTitles[27], 27);
        // [SR151207-042][20151209] taeku.jeong Find No 추가
        GridColumn gridcolumn28 = new GridColumn(grid, SWT.LEFT);
        addColumn(apatablecomparator, gridcolumn28, columnTitles[28], 28);
        //---------------------------------------------------------------------
        // jwlee 소스변경 끝
        tableViewer.setComparator(apatablecomparator);
    }

    private void addColumn(final APATableComparator comparator, GridColumn gridcolumn, String s, int i)
    {
        gridcolumn.setMoveable(true);
        String s1;
        if(columnWidths != null && columnWidths.length > 0)
            s1 = columnWidths[i];
        else
            s1 = defaultColumnWidth;
        gridcolumn.setWidth(Integer.valueOf(s1).intValue());
        gridcolumn.setText(s);
        GridViewerColumn gridviewercolumn = new GridViewerColumn(tableViewer, gridcolumn);
        gridviewercolumn.setLabelProvider(new APAColumnLabelProvider(i));
        final int columnIndex = i;
        if(getReg().getString("MFGName_Header").equals(gridcolumn.getText()))
        {
            grid.setSortDirection(128);
            grid.setSortColumn(gridcolumn);
            gridcolumn.setSort(128);
            comparator.setAscending(true);
            comparator.setSortColumn(i);
        }
        gridcolumn.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent selectionevent)
            {
                if(columnIndex == comparator.getSortColumn())
                {
                    comparator.setAscending(!comparator.isAscending());
                    if(comparator.isAscending())
                        grid.getColumn(columnIndex).setSort(128);
                    else
                        grid.getColumn(columnIndex).setSort(1024);
                } else
                {
                    grid.getColumn(comparator.getSortColumn()).setSort(0);
                    grid.getColumn(columnIndex).setSort(128);
                    comparator.setAscending(true);
                }
                comparator.setSortColumn(columnIndex);
                grid.setSortColumn(grid.getColumn(columnIndex));
                tableViewer.refresh();
            }
        });
    }

    private void addTypeMenuToUpperBar(Composite composite)
    {
        typeMenu = new Menu(composite.getShell(), 8);
        addItemToTypeMenu("ShowAll", false);
        addItemToTypeMenu("DatumPoint", false);
        addItemToTypeMenu("WeldPoint", true);
        filterMFGType = false;
    }

    private void addItemToTypeMenu(String s, boolean flag)
    {
        MenuItem menuitem = new MenuItem(typeMenu, 32);
        menuitem.setText(s);
        menuitem.setSelection(true);
        menuitem.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent selectionevent)
            {
                MenuItem menuitem1 = (MenuItem)selectionevent.widget;
                Menu menu = menuitem1.getParent();
                boolean flag1 = menuitem1.getSelection();
                filterTypes = new ArrayList<String>();
                if(menuitem1.equals(defaultMenuItem))
                {
                    for(int i = 0; i < menu.getItems().length; i++)
                        menu.getItem(i).setSelection(flag1);

                } else
                {
                    menuitem1.setSelection(flag1);
                }
                MenuItem amenuitem[] = menu.getItems();
                int j = amenuitem.length;
                for(int k = 0; k < j; k++)
                {
                    MenuItem menuitem2 = amenuitem[k];
                    if(menuitem2.getSelection())
                        filterTypes.add(menuitem2.getText());
                }

                if(filterTypes.isEmpty())
                    filterTypes = null;
                processFiltering();
            }

        });
        if(flag)
            defaultMenuItem = menuitem;
    }

    private void addButtonToUpperBar(String s, int i, String s1, int j, boolean flag)
    {
        Image image = getIcon(s);
        ToolItem toolitem = new ToolItem(upperBar, j);
        toolitem.setImage(image);
        toolitem.setEnabled(true);
        toolitem.setToolTipText(s1);
        toolitem.setData(Integer.valueOf(i));
        toolitem.setEnabled(flag);
        toolitem.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent selectionevent)
            {
                buttonPressed(selectionevent);
            }
        });
    }

    protected Control createButtonBar(Composite composite)
    {
        Composite composite1 = (Composite)super.createButtonBar(composite);
        composite1.setLayoutData(new GridData(132));
        return composite1;
    }

    protected void createButtonsForButtonBar(Composite composite)
    {
        // jwlee 수정 소스 부분
        //[SR140721-004][20140729] shcho, 결합판넬 Dialog에 표시되는 개수를 Dialog에서 사용자가 입력한 값으로 조절 할 수 있도로 변경.
        // searchButton = createButton(composite, 1026, getReg().getString("APADialog.search"), false);
        // searchButton.setToolTipText(getReg().getString("APADialog.searchButton.ToolTip"));
        assignButton = createButton(composite, 1034, getReg().getString("APADialog.assign"), false);
        assignButton.setEnabled(false);
        assignButton.setToolTipText(getReg().getString("APADialog.assignButton.ToolTip"));
        assignAllButton = createButton(composite, 1035, getReg().getString("APADialog.assignall"), false);
        assignAllButton.setToolTipText(getReg().getString("APADialog.assignAllButton.ToolTip"));
        closeButton = createButton(composite, 12, getReg().getString("APADialog.closeButton"), false);
        closeButton.setToolTipText(getReg().getString("APADialog.closeButton.ToolTip"));
    }

    protected void buttonPressed(int i)
    {
        if(12 == i){
            cancelPressed();
        } else if(1034 == i){
            acceptPressed();
        } else if(1035 == i){
            acceptAllPressed();
        } else if(1026 == i){
            searchPressed();
        }
    }

    protected Registry getReg()
    {
        if(reg == null)
            reg = Registry.getRegistry(com.symc.plm.rac.cme.biw.apa.APADialog.class);
        return reg;
    }

    protected void buttonPressed(SelectionEvent selectionevent)
    {
        int i = ((Integer)selectionevent.widget.getData()).intValue();
        if(1025 == i) {
            filterPressed(selectionevent);
            // 저장된 판넬 갯수와 겹수를 비교하여 색을 바꾼다
            modifyTableView();
        }else if(1027 == i){
            settingsPressed();
            // 저장된 판넬 갯수와 겹수를 비교하여 색을 바꾼다
            modifyTableView();
        }else if(1028 == i){
            filterOutAssignedPressed();
            // 저장된 판넬 갯수와 겹수를 비교하여 색을 바꾼다
            modifyTableView();
        }else if(1029 == i){
            removePartPressed();
            // 저장된 판넬 갯수와 겹수를 비교하여 색을 바꾼다
            modifyTableView();
        }else if(1030 == i){
            addPartPressed();
            // 저장된 판넬 갯수와 겹수를 비교하여 색을 바꾼다
            modifyTableView();
        }else if(1031 == i){
            displayPressed();
        }else if(1032 == i){
            printPressed();
        }else if(1033 == i){
            exportToExcelPressed();
        }else {
            super.buttonPressed(i);
        }
    }

    private void settingsPressed()
    {
        SettingsDialog settingsdialog = new SettingsDialog(getShell(), session);
        settingsdialog.open();
    }

    private void acceptAllPressed()
    {
        invokeAcceptOperation(true);
    }

    private void invokeAcceptOperation(final boolean acceptAll)
    {
        Display display = shell.getDisplay();
        Cursor cursor = display.getSystemCursor(1);
        getShell().setCursor(cursor);
        getShell().setEnabled(false);
        session.setStatus(getReg().getString("APADialog.working"));
        List<Object> arraylist = new ArrayList<Object>();
        if(selectedItems != null)
        {
            for(GridItem griditem : selectedItems){
                arraylist.add(griditem.getData());
            }
        }
        final APAConnectionModel connectionModel = new APAConnectionModel(getContentProvider(), arraylist);
        AbstractAIFOperation abstractaifoperation = new AbstractAIFOperation() {

            public void executeOperation()
            {
                connectionModel.handleAssign(acceptAll);
            }
        };
        abstractaifoperation.addOperationListener(new InterfaceAIFOperationListener() {

            public void endOperation()
            {
                shell.getDisplay().syncExec(new Runnable() {

                    public void run()
                    {
                        tableViewer.refresh();
                        tableViewer.getElementAt(0);
                        tableViewer.getElementAt(1);
                        tableViewer.getElementAt(2);
                        tableViewer.getElementAt(10);
                        getShell().setCursor(null);
                        getShell().setEnabled(true);
                        session.setReadyStatus();
                        modifyTableView();
                    }

                });
            }

            public void startOperation(String s)
            {
            }
        });
        session.queueOperation(abstractaifoperation);
    }

    private void acceptPressed()
    {
        invokeAcceptOperation(false);
    }

    private void exportToExcelPressed()
    {
        ExportTabletoExcel exporttabletoexcel = new ExportTabletoExcel(grid);
        exporttabletoexcel.invokeSaveExcelData();
    }

    private void printPressed()
    {
        Display.getDefault().asyncExec(new Runnable() {

            public void run()
            {
                GridInfo gridinfo = new GridInfo(grid);
                GridInfo gridinfo1 = gridinfo;
                IPrintService iprintservice = (IPrintService)OSGIUtil.getService(Activator.getDefault(), com.teamcenter.rac.services.IPrintService.class);
                if(gridinfo1 != null && iprintservice != null)
                    iprintservice.printGrid(gridinfo1);
            }
        });
    }

    private void displayPressed()
    {
        final APAViewer display = new APAViewer(viewerApp, session);
        Point apoint[] = grid.getCellSelection();
        ArrayList<InterfaceAIFComponent> selectedPartLine = new ArrayList<InterfaceAIFComponent>();
        for(int i = 0; i < apoint.length; i++)
        {
            Point point = apoint[i];
            int j = point.y;
            int k = point.x;

            Object obj = grid.getItem(j).getData();
            Object partLine = getContentProvider().getDisplayPartLine(obj, k);
            if(partLine != null && partLine instanceof InterfaceAIFComponent)
                selectedPartLine.add((InterfaceAIFComponent)partLine);
        }

        final ArrayList<InterfaceAIFComponent> resultLines = new ArrayList<InterfaceAIFComponent>(selectedPartLine);
        Display display1 = shell.getDisplay();
        Cursor cursor = display1.getSystemCursor(1);
        shell.setCursor(cursor);
        session.setStatus(getReg().getString("APADialog.working"));
        AbstractAIFOperation abstractaifoperation = new AbstractAIFOperation() {

            public void executeOperation()
            {
                display.displayLines(resultLines);
            }
        };

        abstractaifoperation.addOperationListener(new InterfaceAIFOperationListener() {

            public void endOperation()
            {
                Display.getDefault().syncExec(new Runnable() {

                    public void run()
                    {
                        tableViewer.refresh();
                        shell.setCursor(null);
                        session.setReadyStatus();
                    }

                });
            }

            public void startOperation(String s)
            {
            }
        });
        session.queueOperation(abstractaifoperation);
    }

    private void addPartPressed()
    {
        updateParts(true);
    }

    private void removePartPressed()
    {
        updateParts(false);
    }

    private void updateParts(final boolean flag)
    {
        shell.getDisplay().syncExec(new Runnable() {
            
            @Override
            public void run() {
                Point apoint[] = grid.getCellSelection();
                Point apoint1[] = apoint;
                int i = apoint1.length;
                for(int j = 0; j < i; j++)
                {
                    Point point = apoint1[j];
                    int k = point.y;
                    int l = point.x;

                    Object obj = grid.getItem(k).getData();
                    if(l != 0 && l != 1)
                        getContentProvider().modifyPartSettings(flag, flag, obj, l);
                }
                
                tableViewer.refresh();
                updateControls(apoint);
            }
        });
    }

    private void filterOutAssignedPressed()
    {
        boolean flag = getContentProvider().isFilter_out_assigned();
        getContentProvider().setFilter_out_assigned(!flag);
        shell.getDisplay().syncExec(new Runnable() {
            
            @Override
            public void run() {
                tableViewer.refresh();
                updateControls(grid.getCellSelection());
            }
        });
    }

    // jwlee 수정 소스 부분
    //[SR140721-004][20140729] shcho, 결합판넬 Dialog에 표시되는 개수를 Dialog에서 사용자가 입력한 값으로 조절 할 수 있도로 변경.
    private void searchPressed() {
//        String strSearchCondition = searchConditionText.getText();
//        if(strSearchCondition.length() == 0 || strSearchCondition.equals("0")) {
//            MessageBox.post(getShell(), "The search range cannot be smaller than 1.", "INFORMATION", MessageBox.INFORMATION);
//            return;
//        }
//        searchCondition = Integer.parseInt(strSearchCondition);
//        String number = ((TCSession) session).getPreferenceService().getString(0, "MEAPCConnectedNumber");
//        String number = ((TCSession) session).getPreferenceService().getStringValue("MEAPCConnectedNumber");
//        if (searchCondition > Integer.parseInt(number)) {
//            MessageBox.post(getShell(), "The search range cannot be greater than " + number + ".", "INFORMATION", MessageBox.INFORMATION);
//            return;
//        }
        
        Display display = shell.getDisplay();
        Cursor cursor = display.getSystemCursor(1);
        getShell().setCursor(cursor);
        getShell().setEnabled(false);
        session.setStatus(getReg().getString("APADialog.working"));
        if(contentProvider == null)
            contentProvider = (APAContentProvider)tableViewer.getContentProvider();
        AbstractAIFOperation abstractaifoperation = new AbstractAIFOperation() {

            public void executeOperation() {
                try {
//                    contentProvider.performSpatialSearch(rootBomline, searchCondition);
                    contentProvider.performSpatialSearch(rootBomline);
                } catch (Exception exception) {
                    APADialog.logger.error(exception.getClass().getName(), exception);
                }
            }
        };
        
        abstractaifoperation.addOperationListener(new InterfaceAIFOperationListener() {

            public void endOperation()
            {
                Display.getDefault().syncExec(new Runnable() {

                    public void run()
                    {
                        tableViewer.refresh();
                        getShell().setCursor(null);
                        getShell().setEnabled(true);
                        session.setReadyStatus();
                        modifyTableView();
                    }

                });
            }

            public void startOperation(String s)
            {
            }
        });
        
        session.queueOperation(abstractaifoperation);
    }

    private void filterPressed(SelectionEvent selectionevent)
    {
        ToolItem toolitem = (ToolItem)selectionevent.widget;
        if(selectionevent.detail == 4)
        {
            Rectangle rectangle = toolitem.getBounds();
            Point point = new Point(rectangle.x, rectangle.y + rectangle.height);
            point = upperBar.toDisplay(point);
            typeMenu.setLocation(point.x, point.y);
            typeMenu.setVisible(true);
            toolitem.setImage(getIcon(getReg().getString("APADialog.Filter_assigned_partsIcon.ICON")));
            filterMFGType = true;
        } else
        if(selectionevent.detail != 4)
        {
            if(filterMFGType)
            {
                toolitem.setImage(getIcon(getReg().getString("APADialog.Filter_connectionsIcon.ICON")));
                filterMFGType = false;
                filterTypes = null;
            } else
            {
                filterTypes = new ArrayList<String>();
                toolitem.setImage(getIcon(getReg().getString("APADialog.Filter_assigned_partsIcon.ICON")));
                filterMFGType = true;
                MenuItem amenuitem[] = typeMenu.getItems();
                int i = amenuitem.length;
                for(int j = 0; j < i; j++)
                {
                    MenuItem menuitem = amenuitem[j];
                    if(menuitem.getSelection())
                        filterTypes.add(menuitem.getText());
                }

                if(filterTypes.isEmpty())
                    filterTypes = null;
            }
            processFiltering();
        }
    }

    public void processFiltering()
    {
        APAContentProvider apacontentprovider = getContentProvider();
        apacontentprovider.setFilterTypes(filterTypes, filterMFGType);
        tableViewer.refresh();
    }

    private APAContentProvider getContentProvider()
    {
        if(contentProvider == null)
            contentProvider = new APAContentProvider(this);
        return contentProvider;
    }

    public AbstractAIFSession getSession()
    {
        return session;
    }

    public TCComponentBOMLine getSelectedComponent()
    {
        return selectedComponent;
    }

    protected void handleShellCloseEvent()
    {
        String as[] = getCurrentColumnWidths();
        Cookie cookie = Cookie.getCookie("MEAPACookie", true);
        cookie.setStringArray((new StringBuilder()).append(getClass().getName()).append(".COLUMN_WIDTH").toString(), as);
        try
        {
            cookie.close();
        }
        catch(IOException ioexception)
        {
            logger.error(ioexception.getClass().getName(), ioexception);
        }
        super.handleShellCloseEvent();
    }

    private String[] getCurrentColumnWidths()
    {
        ArrayList<String> arraylist = new ArrayList<String>();
        Object aobj [] = grid.getColumns();
        int i = aobj.length;
        for(int j = 0; j < i; j++)
        {
            GridColumn gridcolumn = (GridColumn) aobj[j];
            arraylist.add(String.valueOf(gridcolumn.getWidth()));
        }

        String [] currentColumnWidths = new String[arraylist.size()];
        return arraylist.toArray(currentColumnWidths);
    }

    public boolean isEmpty()
    {
        return empty;
    }

    public void setContent()
    {
        loadTableData();
        if(!getContentProvider().isEmpty())
        {
            empty = false;
        }
        else
        {
            String s = getReg().getString("PartSearch.NoMFGFound");
            MessageBox.post(s, getReg().getString("APADialog.error"), 1);
        }
    }

    /**
     * Table 에 저장된 데이터를 가지고 겹수와 실제 저장된 판넬에 갯수를 비교하여
     * 색을 바꾸어 준다
     *
     * @method modifyTableView
     * @date 2014. 4. 4.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void modifyTableView() {
        shell.getDisplay().syncExec(new Runnable() {
            
            @Override
            public void run() {
                Device device = grid.getItem(0).getFont(COLUMN_INDEX_PART1).getDevice();
                Color redColor = new Color(device, 255, 0, 0);
                Color applyColor = new Color(device, 0, 128, 0);
                Color blackColor = new Color(device, 0, 0, 0);
                Color blueColor = new Color(device, 243, 97, 220);
                for (int i = 0; i < grid.getItemCount(); i++) {
                    int numberSheet = Integer.parseInt(grid.getItem(i).getText(COLUMN_INDEX_WELD_NUMBER_OF_SHEET));
                    int rearSheet = 0 ;
                    for (int j = 4; j < 19; j++) {
                        Color getColor = grid.getItem(i).getForeground(j);
                        if (!grid.getItem(i).getText(j).equals("") && (applyColor.equals(getColor) || blackColor.equals(getColor))) {
                            rearSheet++;
                        }
                    }
                    // 겹수 보다 실제 저장될 대상이 많으면 빨강, 적으면 파랑으로 표시한다
                    if (rearSheet > numberSheet) {
                        grid.getItem(i).setBackground(COLUMN_INDEX_WELD_NAME, redColor);
                    }else if (rearSheet < numberSheet){
                        grid.getItem(i).setBackground(COLUMN_INDEX_WELD_NAME, blueColor);
                    }
                }
            }
        });
    }

}
