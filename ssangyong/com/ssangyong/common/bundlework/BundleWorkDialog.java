/**
 * 일괄작업 공통 Dialog
 * 일괄작업 기능 구현시 현재 Class를 상속받아서 구현합니다.
 * 
 * 상속시 기본화면은 구성되어 있으며.. Properties에 정의된 Text 값을 필요시 Override 하시기 바랍니다.
 */

package com.ssangyong.common.bundlework;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.ssangyong.Activator;
import com.ssangyong.common.bundlework.bwutil.BWItemData;
import com.ssangyong.common.utils.TxtReportFactory;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.util.Registry;

/**
 * [20170314][ymjang] 암호화 문서 경고 문구 삽입
 */
public abstract class BundleWorkDialog extends Dialog implements BundleWorkImpl
{
    
    // 일괄작업에 해당하는 TCComponent Type 명시(Excel Header 기준)
    /* Item Type */
    public static final String CLASS_TYPE_ITEM = "Item";
    /* Revision Type */
    public static final String CLASS_TYPE_REVISION = "Revision";
    /* Dataset Type */
    public static final String CLASS_TYPE_DATASET = "Dataset";
    /* BomLine Type */
    public static final String CLASS_TYPE_BOMLINE = "BOMLine";
    /* Class Type */
    public static final String CLASS_TYPE_CLASSIFICATION = "Classification";
    
    /* 일괄작업 해당 TCComponent Type List */
    public static final String[] CLASS_TYPE_LIST = { CLASS_TYPE_ITEM, CLASS_TYPE_REVISION, CLASS_TYPE_DATASET, CLASS_TYPE_BOMLINE, CLASS_TYPE_CLASSIFICATION };
    
    // TreeTable에 표시되는 해당 Item 상태
    /* StandBy Status */
    public static final int STATUS_STANDBY = 0;
    /* Inprogress Status */
    public static final int STATUS_INPROGRESS = 1;
    /* Complete Status */
    public static final int STATUS_COMPLETED = 2;
    /* Error Occure Status */
    public static final int STATUS_ERROR = 3;
    /* Warning Status */
    public static final int STATUS_WARNING = 4;
    public static final int STATUS_SKIP = 5;
    
    public Registry registry;
    
    /* 일괄작업 해당 Java Class Instance */
    public Class<?> dlgClass;
    
    /* Import Dialog */
    public Shell shell;
    public Label lblAttachFileMsg;
    /* File Path Text */
    public Text fileText;
    /* Upload Log TextArea */
    public Text text;
    
    /* Item,DataSet 관리 TreeTable */
    public Tree tree;
    
    public Group logGroup;
    
    /* Target 지정 Group Compoent */
    public Group excelFileGroup;
    public Group excelFileGroup1;
    /* 확인(실행) Button */
    public Button executeButton;
    /* 닫기(취소) Button */
    public Button cancelButton;
    /* 로그보기 Button */
    public Button viewLogButton;
    /* Target 검색 Button */
    public Button searchButton;
    
    /* Wait Cursor */
    public Cursor waitCursor;
    /* Arrow Cursor */
    public Cursor arrowCursor;
    
    /* Excel 파일의 Directory 경로 */
    public String strImageRoot;
    /* Log File FullPath */
    public String strLogFileFullPath;
    
    /* Status Display Name Array */
    public String[] szStatus;
    
    /* Tree Header Names */
    public String[] szHeaderNames;
    /* Text Report Header Pixel Widths */
    public int[] szTxtHeaderWidth;
    
    /* TreeItem Column과 Item속성의 Mapping 정보관리 */
    public ArrayList<String[]> attrMappingMap;
    
    /* Status Column Index */
    public int nStatusIndex = -1;
    /* Message Column Index */
    public int nMessageIndex = -1;
    
    Image revImage;
    Image datasetImage;
    /**
     * Create the dialog.
     * 
     * @param parent
     * @param style
     */
    public BundleWorkDialog(Shell parent, int style, Class<?> dlgClass)
    {
        super(parent, style);
        
        try
        {
            this.revImage = Activator.imageDescriptorFromPlugin("com.teamcenter.rac.common", "icons/itemrevision_16.png").createImage();
            this.datasetImage = Activator.imageDescriptorFromPlugin("com.teamcenter.rac.common", "icons/newdataset_16.png").createImage();
            
            this.registry = Registry.getRegistry(this);
            
            
            this.shell = new Shell(parent.getDisplay(), style);
            //this.shell = new Shell(PlatformUI.createDisplay(), style);
            this.shell.setSize(890, 672);
            
            // dialog를 화면 중앙으로 이동
            Rectangle screen = this.shell.getDisplay().getMonitors()[0].getBounds();
            Rectangle shellBounds = this.shell.getBounds();
            this.shell.setBounds((screen.width - shellBounds.width) / 2, (screen.height - shellBounds.height) / 2, shellBounds.width, shellBounds.height);
            
            this.dlgClass = dlgClass;
            
            this.shell.setText(this.getTextBundle("title", null, dlgClass));
            
            this.szStatus = this.getTextBundleArray("STATUS", null, dlgClass);
            
            this.waitCursor = new Cursor(this.shell.getDisplay(), SWT.CURSOR_WAIT);
            this.arrowCursor = new Cursor(this.shell.getDisplay(), SWT.CURSOR_ARROW);
            
            String strStatusIndex = this.getTextBundle("StatusIndex", null, dlgClass);
            String strMessageIndex = this.getTextBundle("MessageIndex", null, dlgClass);
            
            this.nStatusIndex = Integer.parseInt(strStatusIndex);
            this.nMessageIndex = Integer.parseInt(strMessageIndex);
            
            this.generateUI();
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Dialog 화면 구성
     */
    public void generateUI() throws Exception
    {
        
        this.excelFileGroup = new Group(shell, SWT.NONE);
        this.excelFileGroup.setText(this.getTextBundle("excelFileGroup", null, this.dlgClass));
        
        this.lblAttachFileMsg = new Label(excelFileGroup, SWT.NONE);
        this.lblAttachFileMsg.setFont(SWTResourceManager.getFont("맑은 고딕", 13, SWT.BOLD));
        this.lblAttachFileMsg.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
        this.lblAttachFileMsg.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 8, 1));
        this.lblAttachFileMsg.setText("※ 암호화된 문서는 반드시 암호를 해제하신 후, 등록하셔야 합니다. ※");
        
        this.fileText = new Text(excelFileGroup, SWT.BORDER);
        this.fileText.setEditable(false);
        this.fileText.setForeground(this.shell.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        this.fileText.setBackground(this.shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        
        this.searchButton = new Button(excelFileGroup, SWT.NONE);
        this.searchButton.setText(this.getTextBundle("searchButton", null, this.dlgClass));
//        this.searchButton.setImage(Activator.imageDescriptorFromPlugin("com.symc-newplm", "icons/search_16.png").createImage());
        
        this.logGroup = new Group(shell, SWT.NONE);
        logGroup.setText(this.getTextBundle("logGroup", null, this.dlgClass));
        
        this.szHeaderNames = this.getTextBundleArray("TreeColumnNames", null, this.dlgClass);
        String[] strColumnWidths = this.getTextBundleArray("TreeColumnWidths", null, this.dlgClass);
        String[] strTxtColmnWidths = this.getTextBundleArray("TxtRptColumnWidths", null, this.dlgClass);
        
        if (this.szHeaderNames == null || strColumnWidths == null || strTxtColmnWidths == null)
        {
            throw new Exception("Tree Table Header 정보가 옳바르지 않습니다. 관리자에게 문의하세요.");
        }
        
        this.szTxtHeaderWidth = new int[strTxtColmnWidths.length];
        
        if (this.szHeaderNames.length != strColumnWidths.length || this.szHeaderNames.length != strTxtColmnWidths.length)
            throw new Exception("Tree Table Header 정보가 옳바르지 않습니다. 관리자에게 문의하세요.");
        
        this.tree = new Tree(logGroup, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER);
        for (int i = 0; i < this.szHeaderNames.length; i++)
        {
            TreeColumn tc = new TreeColumn(this.tree, SWT.BORDER);
            tc.setText(this.szHeaderNames[i]);
            tc.setWidth(Integer.parseInt(strColumnWidths[i]));
            
            this.szTxtHeaderWidth[i] = Integer.parseInt(strTxtColmnWidths[i]);
            
        }
        
        Listener heightSetter = new Listener()
        {
            public void handleEvent(Event event)
            {
                event.height = 20;
            }
        };
        this.tree.addListener(SWT.MeasureItem, heightSetter);
        
        tree.setLinesVisible(true);
        tree.setHeaderVisible(true);

        
        this.text = new Text(logGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);

        this.text.setEditable(false);
        this.text.setForeground(this.shell.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        this.text.setBackground(this.shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        
        // 
        this.executeButton = new Button(shell, SWT.NONE);

        this.executeButton.setText(this.getTextBundle("executeButton", null, this.dlgClass));
        this.executeButton.setEnabled(false);
//        this.executeButton.setImage(Activator.imageDescriptorFromPlugin("com.symc-newplm", "icons/ok.png").createImage());

        
        this.cancelButton = new Button(shell, SWT.NONE);

        this.cancelButton.setText(this.getTextBundle("cancelButton", null, this.dlgClass));
//        this.cancelButton.setImage(Activator.imageDescriptorFromPlugin("com.symc-newplm", "icons/cancel.png").createImage());

        
        this.viewLogButton = new Button(shell, SWT.NONE);

        this.viewLogButton.setText(this.getTextBundle("viewLogButton", null, this.dlgClass));
        this.viewLogButton.setEnabled(false);
        this.viewLogButton.setEnabled(false);
//        this.viewLogButton.setImage(Activator.imageDescriptorFromPlugin("com.symc-newplm", "icons/report_16.png").createImage());
        
        this.executeButton.addSelectionListener(new SelectionListener()
        {
            public void widgetSelected(SelectionEvent e)
            {

                shell.setCursor(waitCursor);
                try
                {
                    // Load된 TCComponet Upload
                    // 하위 Class에서 구현해야 함
                    execute();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                finally
                {
                    shell.setCursor(arrowCursor);
                }
            }
            
            public void widgetDefaultSelected(SelectionEvent e)
            {
            }
        });
        
        searchButton.addSelectionListener(new SelectionListener()
        {
            public void widgetSelected(SelectionEvent e)
            {

             
                shell.setCursor(waitCursor);
                try
                {
                    // Excel 파일 선택, 선택된 Excel Loading
                    selectTarget();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                finally
                {
                    shell.setCursor(arrowCursor);
                }
            }
            
            public void widgetDefaultSelected(SelectionEvent e)
            {
            }
        });
        cancelButton.addSelectionListener(new SelectionListener()
        {
            public void widgetSelected(SelectionEvent e)
            {
                // dialog Close
                shell.dispose();
            }
            
            public void widgetDefaultSelected(SelectionEvent e)
            {
            }
        });
        
        viewLogButton.addSelectionListener(new SelectionListener()
        {
            public void widgetSelected(SelectionEvent e)
            {
                try
                {
                    if (strLogFileFullPath == null)
                        return;
                    
                    Runtime.getRuntime().exec("cmd /c \"" + strLogFileFullPath + "\"");
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
            
            public void widgetDefaultSelected(SelectionEvent e)
            {
            }
        });
        
        /* Data양이 많을 경우 너무 느림..-_)y==* ~~
        Listener paintListener = new Listener()
        {
            public void handleEvent(Event event)
            {
                switch (event.type)
                {
                    case SWT.MeasureItem:
                    {
                        int nHeightMargin = 7;
                        TreeItem item = (TreeItem) event.item;
                        String text = getText(item, event.index);
                        Point size = event.gc.textExtent(text);
                        event.width = size.x;
                        event.height = Math.max(event.height, size.y + nHeightMargin);
                        break;
                    }
                    case SWT.PaintItem:
                    {
                        
                        TreeItem item = (TreeItem) event.item;
                        String text = getText(item, event.index);
                        Point size = event.gc.textExtent(text);
                        // int offset2 = event.index == 0 ? Math.max(0, (event.height - size.y) / 2) : 0;
                        int offset2 = Math.max(0, (event.height - size.y) / 2);
                        int xMargin = 3;
                        if (event.index == 0)
                        {
                            Image img = item.getImage(0);
                            if (img != null)
                            {
                                event.gc.drawImage(img, event.x, event.y + offset2);
                                event.gc.drawText(text, event.x + 18, event.y + offset2, true);
                            }
                            else
                            {
                                event.gc.drawText(text, event.x, event.y + offset2, true);
                            }
                        }
                        else
                        {
                            event.gc.drawText(text, event.x + xMargin, event.y + offset2, true);
                        }
                        
                        break;
                    }
                    case SWT.EraseItem:
                    {
                        event.detail &= ~SWT.FOREGROUND;
                        break;
                    }
                }
            }
            
            String getText(TreeItem item, int column)
            {
                String text = item.getText(column);
                return text;
            }
        };
        tree.addListener(SWT.MeasureItem, paintListener);
        tree.addListener(SWT.PaintItem, paintListener);
        tree.addListener(SWT.EraseItem, paintListener);
        */
        
        // TreeItem Column과 Item속성의 Mapping 정보관리 Map
        this.attrMappingMap = new ArrayList<String[]>();
        String[] szAttr = this.getTextBundleArray("TreeColumnAttrs", null, this.dlgClass);
        
        if( szAttr == null || szAttr.length != this.szHeaderNames.length )
            throw new Exception("Tree Table Header 속성 Mapping 정보가 옳바르지 않습니다. 관리자에게 문의하세요.");
        
        for( int i = 0 ; i < szAttr.length ; i++)
        {
           
            String[] szItemAttr = BundleWorkDialog.getSplitString(szAttr[i], ".");
            attrMappingMap.add(szItemAttr);
        }
        

        
    }
    
    
    @Override
    public void dialogOpen() throws Exception
    {
//        this.excelFileGroup.setBounds(10, 10, 769, 60);
//        this.fileText.setBounds(10, 25, 390, 24);
//        this.searchButton.setBounds(410, 26, 77, 22);
//        this.logGroup.setBounds(10, 75, 863, 481);
//        this.tree.setBounds(10, 22, 843, 300);
//        this.text.setBounds(10, 330, 843, 141);
//        this.executeButton.setBounds(338, 576, 77, 22);
//        this.cancelButton.setBounds(459, 576, 77, 22);
//        this.viewLogButton.setBounds(750, 576, 120, 22);

    	this.excelFileGroup.setBounds(10, 10, 769, 90);
        this.lblAttachFileMsg.setBounds(10, 20, 600, 30);
        this.fileText.setBounds(10, 55, 390, 24);
        this.searchButton.setBounds(410, 56, 77, 22);
        this.logGroup.setBounds(10, 100, 863, 481);
        this.tree.setBounds(10, 22, 843, 300);
        this.text.setBounds(10, 330, 843, 141);
        this.executeButton.setBounds(338, 600, 77, 22);
        this.cancelButton.setBounds(459, 600, 77, 22);
        this.viewLogButton.setBounds(750, 600, 120, 22);
        
        this.shell.open();
        this.shell.layout();
    }
    
    
    
    /**
     * Template DownLoad 기능 활성화(대부분 일괄업로드 시)
     * 
     */
    public void enableTemplateButton()
    {
        Button templateButton = new Button(excelFileGroup, SWT.NONE);
        templateButton.setBounds(570, 56, 180, 22);
        templateButton.setText(this.getTextBundle("templateButton", null, this.dlgClass));
//        templateButton.setImage(Activator.imageDescriptorFromPlugin("com.symc-newplm", "icons/exceldataset_16.png").createImage());
        
        templateButton.addSelectionListener(new SelectionListener()
        {
            public void widgetSelected(SelectionEvent e)
            {
                downLoadTemplate();
            }
            
            public void widgetDefaultSelected(SelectionEvent e)
            {
            }
        });
        
    }
    
    /**
     * Target이 Excel 파일인 경우
     * 하위 class에서 selectTarget() Method를 Override하여 호출
     */
    public void selectTargetFile()
    {
        
        FileDialog fDialog = new FileDialog(this.shell, SWT.SINGLE);
        fDialog.setFilterNames(new String[] { "Excel File" });
        // *.xls, *.xlsx Filter 설정
        fDialog.setFilterExtensions(new String[] { "*.xls*" });
        fDialog.open();
        
        String strfileName = fDialog.getFileName();
        if ((strfileName == null) || (strfileName.equals("")))
            return;
        
        this.strImageRoot = fDialog.getFilterPath();
        this.fileText.setText(this.strImageRoot + File.separatorChar + strfileName);
        
        // 선택된 Excel File Loading
        // this.load();
        
    }
    
    /**
     * Target이 디렉토리인 경우
     * 하위 class에서 selectTarget() Method를 Override하여 호출
     */
    public void selectTargetDirectory()
    {
       
        DirectoryDialog dialog = new DirectoryDialog(shell);
        String platform = SWT.getPlatform();
        dialog.setFilterPath(platform.equals("win32") || platform.equals("wpf") ? "c:\\" : "/");
        String strDirectory = dialog.open();
        this.fileText.setText(strDirectory);
        this.strImageRoot = strDirectory;
        
    }
    
    /**
     * Integer 값 Return
     * 
     * @param strValue
     * @return
     */
    public int getIntValue(String strValue)
    {
        int nValue = -1;
        
        try
        {
            if (strValue != null && !strValue.trim().equals(""))
                nValue = (int) (Double.parseDouble(strValue));
        }
        catch (NumberFormatException e)
        {
            e.printStackTrace();
        }
        
        return nValue;
        
    }
    
    /**
     * Excel Cell Value Return
     * 
     * CELL_TYPE_NUMERIC인 경우 Integer로 Casting하여 반환함
     * Long 형태의 값을 원할경우 다르게 구현해야 함.
     * 
     * @param cell
     * @return
     */
    public String getCellText(Cell cell)
    {
        String value = "";
        if (cell != null)
        {
            
            switch (cell.getCellType())
            {
                case XSSFCell.CELL_TYPE_FORMULA:
                    value = cell.getCellFormula();
                    break;
                
                // Integer로 Casting하여 반환함
                case XSSFCell.CELL_TYPE_NUMERIC:
                    value = "" +  getFormatedString(cell.getNumericCellValue());
                    break;
                
                case XSSFCell.CELL_TYPE_STRING:
                    value = "" + cell.getStringCellValue();
                    break;
                
                case XSSFCell.CELL_TYPE_BLANK:
                    // value = "" + cell.getBooleanCellValue();
                    value = "";
                    break;
                
                case XSSFCell.CELL_TYPE_ERROR:
                    value = "" + cell.getErrorCellValue();
                    break;
                default:
            }
            
        }
        
        return value;
    }
    
    
    public String getFormatedString(double value)
    {
      // if( value == null || "".equals(value.trim()))
      // {
      // return "";
      // }

      DecimalFormat df = new DecimalFormat("#####################.####");//
      return df.format(value);
    }
    
    
    /**
     * Text 형태의 ReportFactory Instance를 생성함
     * 
     * @param useNumFlag : Number Column 사용여부
     * @param useLevelFlag : Level Column 사용여부
     * @return TxtReportFactory
     */
    public TxtReportFactory generateReport(boolean useNumFlag, boolean useLevelFlag)
    {
        
        TxtReportFactory rptFactory = new TxtReportFactory(szHeaderNames, szTxtHeaderWidth, useNumFlag, useLevelFlag);
        
        // Top Tree Items
        TreeItem[] topItems = this.tree.getItems();
        
        for (int i = 0; i < topItems.length; i++)
        {
            // Top TreeItem 부터 하위로 전개하여 Text Report 생성
            this.generateReportData((ManualTreeItem) topItems[i], rptFactory, 0);
        }
        
        return rptFactory;
        
    }
    
    /**
     * ManualTreeItem의 Report 생성
     * 
     * @param treeItem
     * @param rptFactory
     * @param nLevel
     */
    private void generateReportData(ManualTreeItem treeItem, TxtReportFactory rptFactory, int nLevel)
    {
        
        ArrayList<String> dataList = new ArrayList<String>();
        
        // TreeTable 정보를 읽어옴
        for (int i = 0; i < this.szHeaderNames.length; i++)
        {
            dataList.add(treeItem.getText(i));
        }
        
        rptFactory.setReportData(dataList, nLevel);
        
        TreeItem[] childItems = treeItem.getItems();
        for (int i = 0; i < childItems.length; i++)
        {
            // 하위 TreeItem
            ManualTreeItem childItem = (ManualTreeItem) childItems[i];
            // 재귀호출
            this.generateReportData(childItem, rptFactory, nLevel + 1);
        }
        
    }
    
    /**
     * 상속관계에 따른 TextBundle Getter
     * 하위 Class에서 Override된 Text Property 인경우 상위 Text Property는 무시함
     * 
     * ClassName+"."+MiddleName+"."+BundleName
     * ex) BundleWorkDialog.MSG.ERROR
     * 
     * @param strBundleName : Text Property 마지막 Name
     * @param strMiddleName : Text Property 중간에 포함되는 Name
     * @param dlgClass : 현재 기능을 호출한 Java Class Instance
     * @return Text Property
     */
    public String getTextBundle(String strBundleName, String strMiddleName, Class<?> dlgClass)
    {
        // Middle Name 구성
        String middleName = ".";
        if (strMiddleName != null && !strMiddleName.equals(""))
            middleName = "." + strMiddleName + ".";
        
        // Full Bundle Name
        String strFullBundleName = dlgClass.getSimpleName() + middleName + strBundleName;
        
        String strBundleText = registry.getString(strFullBundleName);
        
        // 현재 Class Instance에 정의 되어 있지 않다면 상위 Class에서 찾음
        if (strBundleText == null || strBundleText.equals("") || strFullBundleName.equals(strBundleText))
        {
            // BundleWorkDialog(최상위) Class 에서도 Bundle을 찾을 수 없다면 공백을 Return
            if (BundleWorkDialog.class.getSimpleName().equals(dlgClass.getSimpleName()))
            {
                return "";
            }
            else
            {
                // 상위 Class에서 찾음(재귀호출)
                Class<?> supDlgClass = dlgClass.getSuperclass();
                strBundleText = this.getTextBundle(strBundleName, strMiddleName, supDlgClass);
            }
        }
        
        return strBundleText;
    }
    
    /**
     * 상속관계에 따른 TextBundle Array Getter
     * 하위 Class에서 Override된 Text Property 인경우 상위 Text Property는 무시함
     * 
     * ClassName+"."+MiddleName+"."+BundleName
     * ex) BundleWorkDialog.MSG.ClassArray
     * 
     * @param strBundleName : Text Property 마지막 Name
     * @param strMiddleName : Text Property 중간에 포함되는 Name
     * @param dlgClass : 현재 기능을 호출한 Java Class Instance
     * @return Text Property Array
     */
    public String[] getTextBundleArray(String strBundleName, String strMiddleName, Class<?> dlgClass)
    {
        String middleName = ".";
        if (strMiddleName != null && !strMiddleName.equals(""))
            middleName = "." + strMiddleName + ".";
        
        String[] szBundleText = null;
        String strFullBundleName = dlgClass.getSimpleName() + middleName + strBundleName;
        
        String strBundleText = registry.getString(strFullBundleName);

        if (strBundleText == null || strBundleText.equals("") || strFullBundleName.equals(strBundleText))
        {
            if (BundleWorkDialog.class.getSimpleName().equals(dlgClass.getSimpleName()))
            {
                return null;
            }
            else
            {
                Class<?> supDlgClass = dlgClass.getSuperclass();
                szBundleText = this.getTextBundleArray(strBundleName, strMiddleName, supDlgClass);
            }
        }
        else
        {
            szBundleText = BundleWorkDialog.getSplitString(strBundleText, ",");
        }
        
        return szBundleText;
    }
    
    /**
     * splitter로 구별되는 문자열을 쪼개어 Vector로 return한다. 빈문자열은 문자열로 간주안함.
     */
    public static String[] getSplitString(String strValue, String splitter)
    {
        if (strValue == null || strValue.length() == 0)
            return null;
        
        if (splitter == null)
            return null;
        
        StringTokenizer split = new StringTokenizer(strValue, splitter);
        ArrayList<String> strList = new ArrayList<String>();
        while (split.hasMoreTokens())
        {
            strList.add(new String(split.nextToken().trim()));
        }
        
        String[] szValue = new String[strList.size()];
        for (int i = 0; i < strList.size(); i++)
        {
            szValue[i] = strList.get(i);
        }
        
        return szValue;
    }
    
    @Override
    public void downLoadTemplate()
    {
        // 하위 일괄Upload class에서 구현
    }
    
    /**
     * 일괄작업 속성을 관리하는 Custom TreeItem Class
     * Item/DataSet 속성을 관리
     * Upload Status/LogMessage 관리
     * 
     */
    public class ManualTreeItem extends TreeItem
    {
        /* Item Type */
        public static final String ITEM_TYPE_TCITEM = "Item";
        /* DataSet Type */
        public static final String ITEM_TYPE_TCDATASET = "Dataset";
        
        // TreeTable에 표시되지 않지만 일괄작업시 필요한 모든 속성값이 저장되는 Class
        private BWItemData stcItems;
        
        // DataSet이 존재하는 경우 관리함
        private TCComponentDataset dataset;
        
        // Current Item Type
        private String strItemType;
        
        // Item ID
        private String strItemID; //jh 변경
        
        // Parent Item ID
        private String strParentItemID; //jh 변경
        
        
        // TreeItem level
        private int nLevel = -1;
        
        // TreeItem에 표시되지 않는 Item 속성관리 Map
        private HashMap<String, String> itemExtraData;
        
        // 해당 TreeItem의 상태값
        private int nStatus = 0;
        
        // Upload Log Message
        private StringBuffer szUploadMessage;
        
        
        private TCComponent tcComp;
        
        


        /**
         * Top TreeItem인 경우 사용되는 생성자
         * 
         * @param parent : Tree
         * @param index : Add 위치
         * @param itemType : Item Type(Item,Dataset)
         * @param itemID : Item ID
         */
        public ManualTreeItem(Tree parent, int index, String itemType, String itemID)
        {
            super(parent, SWT.NONE, index);
            this.strItemType = itemType;
            this.strItemID = itemID;
            this.szUploadMessage = new StringBuffer();
            this.setIcon();
            
            this.itemExtraData = new HashMap<String, String>();
            
            this.setStatus(STATUS_STANDBY);
            
            shell.redraw();
        }
        
        /**
         * 하위 TreeItem인 경우 사용되는 생성자
         * 
         * @param parent : 상위 TreeItem
         * @param index : Add 위치
         * @param itemType : Item Type(Item,Dataset)
         * @param itemID : Item ID
         */
        public ManualTreeItem(ManualTreeItem parent, int index, String itemType, String itemID)
        {
            super(parent, SWT.NONE, index);
            this.strItemType = itemType;
            this.strItemID = itemID;
            this.szUploadMessage = new StringBuffer();
            this.strParentItemID = parent.getItemID();
            this.setIcon();
            parent.setExpanded(true);
            
            this.itemExtraData = new HashMap<String, String>();
            this.setStatus(STATUS_STANDBY);
            
            shell.redraw();
            
        }
        
        private void setIcon()
        {
            if (this.strItemType.equals(ITEM_TYPE_TCITEM))
                setImage(revImage);
            else if (this.strItemType.equals(ITEM_TYPE_TCDATASET))
                setImage(datasetImage);
            
        }
        
        /**
         * Level Setter
         */
        public void setLevel(int nLevel)
        {
            this.nLevel = nLevel;
        }
        
        /**
         * Level Getter
         */
        public int getLevel()
        {
            return this.nLevel;
        }
        
        /**
         * Item Type Getter(Item,Dataset)
         */
        public String getItemType()
        {
            return this.strItemType;
        }
        
        /**
         * Item ID Getter
         */
        public String getItemID()
        {
            return this.strItemID;
        }
        
        /**
         * Item ID Setter
         */
        public void setItemID( String itemID ){
            this.strItemID = itemID;
        }

        /**
         * Parent Item ID Getter
         */
        public String getParentItemID()
        {
            return this.strParentItemID;
        }
        
        
        /**
         * TreeItem 추가 Data Setter
         * 정형화 되지 않은 Data관리
         * 
         * @param nKey : Column Index
         * @param strValue : Value
         */
        public void setItemExtraData(int nKey, String strValue)
        {
            this.itemExtraData.put(nKey + "", strValue);
        }
        
        /**
         * TreeItem 추가 Data Getter
         * 정형화 되지 않은 Data관리
         * 
         * @param strKey : Column Index
         */
        public String getItemExtraData(String strKey)
        {
            if (this.itemExtraData.containsKey(strKey))
                return this.itemExtraData.get(strKey);
            else
                return "";
        }
        
        /**
         * BWItemData Setter
         * TreeTable에 표시되지 않지만 일괄작업시 필요한 모든 속성값이 저장되는 Class
         * 
         * @param stcItems
         */
        public void setBWItemData(BWItemData stcItems)
        {
            this.stcItems = stcItems;
        }
        
        
        /**
         * BWItemData 속성 값 Setter
         * 
         * @param strItemName : Item Type Name
         * @param strAttrName : Attribute Name
         * @param strAttrValue: Attribute Value
         */
        public void setBWItemAttrValue(String strItemName, String strAttrName, String strAttrValue)
        {
            if (this.stcItems == null)
            {
                this.stcItems = new BWItemData();
            }
            
            this.stcItems.setItemData(strItemName, strAttrName, strAttrValue);
        }
        
        /**
         * BWItemData 속성 List Getter
         * 
         * @param strItemName : Item Type Name
         * @return
         */
        public HashMap<String, String> getSTCItemAttrs(String strItemName)
        {
            return this.stcItems.getItemAttrs(strItemName);
        }
        
        /**
         * BWItemData 속성 값 Getter
         * 
         * @param strItemName : Item Type Name
         * @param strAttrName : Item Attr Name
         * @return
         */
        public String getBWItemAttrValue(String strItemName, String strAttrName)
        {
            return this.stcItems.getItemAttrValue(strItemName, strAttrName);
        }
        
        /**
         * Status Setter
         * 
         * @param nStatus
         */
        public void setStatus(int nStatus)
        {
            // 저장된 상태가 Error이면 더이상 Status는 변경이 없음
            if (this.nStatus == STATUS_ERROR)
                return;
            
            // 저장된 상태가 Warning이면 신규상태가 Error가 아닌이상 변경이 없음
            // if (this.nStatus == STATUS_WARNING && nStatus != STATUS_ERROR)
            // return;
            
            this.setText(nStatusIndex, szStatus[nStatus]);
            this.nStatus = nStatus;
            
            // Error 인경우 Background 설정
            if (nStatus == STATUS_ERROR)
            {
                this.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_RED));
            }
            else if (nStatus == STATUS_WARNING)
            {
                this.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_GREEN));
            }
            else
            {
                this.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
            }
        }
        
        /**
         * Status Setter
         * 
         * @param nStatus : 상태 값
         * @param strUploadMessage : 상태 Message
         */
        public void setStatus(int nStatus, String strUploadMessage)
        {
            this.setStatus(nStatus);
            if (!this.szUploadMessage.toString().equals(""))
                this.szUploadMessage.append(",");
            
            this.szUploadMessage.append(strUploadMessage);
            this.setText(nMessageIndex, this.szUploadMessage.toString());
            text.append(this.strItemID + " : " + strUploadMessage + "\n\n");
        }
        
        /**
         * Status Getter
         * 
         * @return
         */
        public int getStatus()
        {
            return this.nStatus;
        }
        
        /**
         * Upload Message Getter
         */
        public String getUploadMessage()
        {
            return this.szUploadMessage.toString();
        }
        
        /**
         * DataSet Setter
         * Item Type이 Dataset인 경우만 해당
         * 
         * @param dataset
         */
        public void setDataSet(TCComponentDataset dataset)
        {
            this.dataset = dataset;
        }
        
        /**
         * DataSet Getter
         * Item Type이 Dataset인 경우만 해당
         */
        public TCComponentDataset getDataSet()
        {
            return this.dataset;
            
        }
        
        /**
         * 필수 Override Method
         */
        protected void checkSubclass()
        {
        }
        
        

        public HashMap<String, HashMap<String, String>> getModelMap()
        {
          return stcItems.getModelMap();
        }
        
        public TCComponent getTcComp()
        {
          return tcComp;
        }

        public void setTcComp(TCComponent tcComp)
        {
          this.tcComp = tcComp;
        }
        
        
    }
    


    
    
}
