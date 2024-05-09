/**
 * �ϰ��۾� ���� Dialog
 * �ϰ��۾� ��� ������ ���� Class�� ��ӹ޾Ƽ� �����մϴ�.
 * 
 * ��ӽ� �⺻ȭ���� �����Ǿ� ������.. Properties�� ���ǵ� Text ���� �ʿ�� Override �Ͻñ� �ٶ��ϴ�.
 */

package com.kgm.common.bundlework;

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

import com.kgm.Activator;
import com.kgm.common.bundlework.bwutil.BWItemData;
import com.kgm.common.utils.TxtReportFactory;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.util.Registry;

/**
 * [20170314][ymjang] ��ȣȭ ���� ��� ���� ����
 */
public abstract class BundleWorkDialog extends Dialog implements BundleWorkImpl
{
    
    // �ϰ��۾��� �ش��ϴ� TCComponent Type ���(Excel Header ����)
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
    
    /* �ϰ��۾� �ش� TCComponent Type List */
    public static final String[] CLASS_TYPE_LIST = { CLASS_TYPE_ITEM, CLASS_TYPE_REVISION, CLASS_TYPE_DATASET, CLASS_TYPE_BOMLINE, CLASS_TYPE_CLASSIFICATION };
    
    // TreeTable�� ǥ�õǴ� �ش� Item ����
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
    
    /* �ϰ��۾� �ش� Java Class Instance */
    public Class<?> dlgClass;
    
    /* Import Dialog */
    public Shell shell;
    public Label lblAttachFileMsg;
    /* File Path Text */
    public Text fileText;
    /* Upload Log TextArea */
    public Text text;
    
    /* Item,DataSet ���� TreeTable */
    public Tree tree;
    
    public Group logGroup;
    
    /* Target ���� Group Compoent */
    public Group excelFileGroup;
    public Group excelFileGroup1;
    /* Ȯ��(����) Button */
    public Button executeButton;
    /* �ݱ�(���) Button */
    public Button cancelButton;
    /* �α׺��� Button */
    public Button viewLogButton;
    /* Target �˻� Button */
    public Button searchButton;
    
    /* Wait Cursor */
    public Cursor waitCursor;
    /* Arrow Cursor */
    public Cursor arrowCursor;
    
    /* Excel ������ Directory ��� */
    public String strImageRoot;
    /* Log File FullPath */
    public String strLogFileFullPath;
    
    /* Status Display Name Array */
    public String[] szStatus;
    
    /* Tree Header Names */
    public String[] szHeaderNames;
    /* Text Report Header Pixel Widths */
    public int[] szTxtHeaderWidth;
    
    /* TreeItem Column�� Item�Ӽ��� Mapping �������� */
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
            
            // dialog�� ȭ�� �߾����� �̵�
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
     * Dialog ȭ�� ����
     */
    public void generateUI() throws Exception
    {
        
        this.excelFileGroup = new Group(shell, SWT.NONE);
        this.excelFileGroup.setText(this.getTextBundle("excelFileGroup", null, this.dlgClass));
        
        this.lblAttachFileMsg = new Label(excelFileGroup, SWT.NONE);
        this.lblAttachFileMsg.setFont(SWTResourceManager.getFont("���� ���", 13, SWT.BOLD));
        this.lblAttachFileMsg.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
        this.lblAttachFileMsg.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 8, 1));
        this.lblAttachFileMsg.setText("�� ��ȣȭ�� ������ �ݵ�� ��ȣ�� �����Ͻ� ��, ����ϼž� �մϴ�. ��");
        
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
            throw new Exception("Tree Table Header ������ �ǹٸ��� �ʽ��ϴ�. �����ڿ��� �����ϼ���.");
        }
        
        this.szTxtHeaderWidth = new int[strTxtColmnWidths.length];
        
        if (this.szHeaderNames.length != strColumnWidths.length || this.szHeaderNames.length != strTxtColmnWidths.length)
            throw new Exception("Tree Table Header ������ �ǹٸ��� �ʽ��ϴ�. �����ڿ��� �����ϼ���.");
        
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
                    // Load�� TCComponet Upload
                    // ���� Class���� �����ؾ� ��
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
                    // Excel ���� ����, ���õ� Excel Loading
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
        
        /* Data���� ���� ��� �ʹ� ����..-_)y==* ~~
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
        
        // TreeItem Column�� Item�Ӽ��� Mapping �������� Map
        this.attrMappingMap = new ArrayList<String[]>();
        String[] szAttr = this.getTextBundleArray("TreeColumnAttrs", null, this.dlgClass);
        
        if( szAttr == null || szAttr.length != this.szHeaderNames.length )
            throw new Exception("Tree Table Header �Ӽ� Mapping ������ �ǹٸ��� �ʽ��ϴ�. �����ڿ��� �����ϼ���.");
        
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
     * Template DownLoad ��� Ȱ��ȭ(��κ� �ϰ����ε� ��)
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
     * Target�� Excel ������ ���
     * ���� class���� selectTarget() Method�� Override�Ͽ� ȣ��
     */
    public void selectTargetFile()
    {
        
        FileDialog fDialog = new FileDialog(this.shell, SWT.SINGLE);
        fDialog.setFilterNames(new String[] { "Excel File" });
        // *.xls, *.xlsx Filter ����
        fDialog.setFilterExtensions(new String[] { "*.xls*" });
        fDialog.open();
        
        String strfileName = fDialog.getFileName();
        if ((strfileName == null) || (strfileName.equals("")))
            return;
        
        this.strImageRoot = fDialog.getFilterPath();
        this.fileText.setText(this.strImageRoot + File.separatorChar + strfileName);
        
        // ���õ� Excel File Loading
        // this.load();
        
    }
    
    /**
     * Target�� ���丮�� ���
     * ���� class���� selectTarget() Method�� Override�Ͽ� ȣ��
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
     * Integer �� Return
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
     * CELL_TYPE_NUMERIC�� ��� Integer�� Casting�Ͽ� ��ȯ��
     * Long ������ ���� ���Ұ�� �ٸ��� �����ؾ� ��.
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
                
                // Integer�� Casting�Ͽ� ��ȯ��
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
     * Text ������ ReportFactory Instance�� ������
     * 
     * @param useNumFlag : Number Column ��뿩��
     * @param useLevelFlag : Level Column ��뿩��
     * @return TxtReportFactory
     */
    public TxtReportFactory generateReport(boolean useNumFlag, boolean useLevelFlag)
    {
        
        TxtReportFactory rptFactory = new TxtReportFactory(szHeaderNames, szTxtHeaderWidth, useNumFlag, useLevelFlag);
        
        // Top Tree Items
        TreeItem[] topItems = this.tree.getItems();
        
        for (int i = 0; i < topItems.length; i++)
        {
            // Top TreeItem ���� ������ �����Ͽ� Text Report ����
            this.generateReportData((ManualTreeItem) topItems[i], rptFactory, 0);
        }
        
        return rptFactory;
        
    }
    
    /**
     * ManualTreeItem�� Report ����
     * 
     * @param treeItem
     * @param rptFactory
     * @param nLevel
     */
    private void generateReportData(ManualTreeItem treeItem, TxtReportFactory rptFactory, int nLevel)
    {
        
        ArrayList<String> dataList = new ArrayList<String>();
        
        // TreeTable ������ �о��
        for (int i = 0; i < this.szHeaderNames.length; i++)
        {
            dataList.add(treeItem.getText(i));
        }
        
        rptFactory.setReportData(dataList, nLevel);
        
        TreeItem[] childItems = treeItem.getItems();
        for (int i = 0; i < childItems.length; i++)
        {
            // ���� TreeItem
            ManualTreeItem childItem = (ManualTreeItem) childItems[i];
            // ���ȣ��
            this.generateReportData(childItem, rptFactory, nLevel + 1);
        }
        
    }
    
    /**
     * ��Ӱ��迡 ���� TextBundle Getter
     * ���� Class���� Override�� Text Property �ΰ�� ���� Text Property�� ������
     * 
     * ClassName+"."+MiddleName+"."+BundleName
     * ex) BundleWorkDialog.MSG.ERROR
     * 
     * @param strBundleName : Text Property ������ Name
     * @param strMiddleName : Text Property �߰��� ���ԵǴ� Name
     * @param dlgClass : ���� ����� ȣ���� Java Class Instance
     * @return Text Property
     */
    public String getTextBundle(String strBundleName, String strMiddleName, Class<?> dlgClass)
    {
        // Middle Name ����
        String middleName = ".";
        if (strMiddleName != null && !strMiddleName.equals(""))
            middleName = "." + strMiddleName + ".";
        
        // Full Bundle Name
        String strFullBundleName = dlgClass.getSimpleName() + middleName + strBundleName;
        
        String strBundleText = registry.getString(strFullBundleName);
        
        // ���� Class Instance�� ���� �Ǿ� ���� �ʴٸ� ���� Class���� ã��
        if (strBundleText == null || strBundleText.equals("") || strFullBundleName.equals(strBundleText))
        {
            // BundleWorkDialog(�ֻ���) Class ������ Bundle�� ã�� �� ���ٸ� ������ Return
            if (BundleWorkDialog.class.getSimpleName().equals(dlgClass.getSimpleName()))
            {
                return "";
            }
            else
            {
                // ���� Class���� ã��(���ȣ��)
                Class<?> supDlgClass = dlgClass.getSuperclass();
                strBundleText = this.getTextBundle(strBundleName, strMiddleName, supDlgClass);
            }
        }
        
        return strBundleText;
    }
    
    /**
     * ��Ӱ��迡 ���� TextBundle Array Getter
     * ���� Class���� Override�� Text Property �ΰ�� ���� Text Property�� ������
     * 
     * ClassName+"."+MiddleName+"."+BundleName
     * ex) BundleWorkDialog.MSG.ClassArray
     * 
     * @param strBundleName : Text Property ������ Name
     * @param strMiddleName : Text Property �߰��� ���ԵǴ� Name
     * @param dlgClass : ���� ����� ȣ���� Java Class Instance
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
     * splitter�� �����Ǵ� ���ڿ��� �ɰ��� Vector�� return�Ѵ�. ���ڿ��� ���ڿ��� ���־���.
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
        // ���� �ϰ�Upload class���� ����
    }
    
    /**
     * �ϰ��۾� �Ӽ��� �����ϴ� Custom TreeItem Class
     * Item/DataSet �Ӽ��� ����
     * Upload Status/LogMessage ����
     * 
     */
    public class ManualTreeItem extends TreeItem
    {
        /* Item Type */
        public static final String ITEM_TYPE_TCITEM = "Item";
        /* DataSet Type */
        public static final String ITEM_TYPE_TCDATASET = "Dataset";
        
        // TreeTable�� ǥ�õ��� ������ �ϰ��۾��� �ʿ��� ��� �Ӽ����� ����Ǵ� Class
        private BWItemData stcItems;
        
        // DataSet�� �����ϴ� ��� ������
        private TCComponentDataset dataset;
        
        // Current Item Type
        private String strItemType;
        
        // Item ID
        private String strItemID; //jh ����
        
        // Parent Item ID
        private String strParentItemID; //jh ����
        
        
        // TreeItem level
        private int nLevel = -1;
        
        // TreeItem�� ǥ�õ��� �ʴ� Item �Ӽ����� Map
        private HashMap<String, String> itemExtraData;
        
        // �ش� TreeItem�� ���°�
        private int nStatus = 0;
        
        // Upload Log Message
        private StringBuffer szUploadMessage;
        
        
        private TCComponent tcComp;
        
        


        /**
         * Top TreeItem�� ��� ���Ǵ� ������
         * 
         * @param parent : Tree
         * @param index : Add ��ġ
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
         * ���� TreeItem�� ��� ���Ǵ� ������
         * 
         * @param parent : ���� TreeItem
         * @param index : Add ��ġ
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
         * TreeItem �߰� Data Setter
         * ����ȭ ���� ���� Data����
         * 
         * @param nKey : Column Index
         * @param strValue : Value
         */
        public void setItemExtraData(int nKey, String strValue)
        {
            this.itemExtraData.put(nKey + "", strValue);
        }
        
        /**
         * TreeItem �߰� Data Getter
         * ����ȭ ���� ���� Data����
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
         * TreeTable�� ǥ�õ��� ������ �ϰ��۾��� �ʿ��� ��� �Ӽ����� ����Ǵ� Class
         * 
         * @param stcItems
         */
        public void setBWItemData(BWItemData stcItems)
        {
            this.stcItems = stcItems;
        }
        
        
        /**
         * BWItemData �Ӽ� �� Setter
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
         * BWItemData �Ӽ� List Getter
         * 
         * @param strItemName : Item Type Name
         * @return
         */
        public HashMap<String, String> getSTCItemAttrs(String strItemName)
        {
            return this.stcItems.getItemAttrs(strItemName);
        }
        
        /**
         * BWItemData �Ӽ� �� Getter
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
            // ����� ���°� Error�̸� ���̻� Status�� ������ ����
            if (this.nStatus == STATUS_ERROR)
                return;
            
            // ����� ���°� Warning�̸� �űԻ��°� Error�� �ƴ��̻� ������ ����
            // if (this.nStatus == STATUS_WARNING && nStatus != STATUS_ERROR)
            // return;
            
            this.setText(nStatusIndex, szStatus[nStatus]);
            this.nStatus = nStatus;
            
            // Error �ΰ�� Background ����
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
         * @param nStatus : ���� ��
         * @param strUploadMessage : ���� Message
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
         * Item Type�� Dataset�� ��츸 �ش�
         * 
         * @param dataset
         */
        public void setDataSet(TCComponentDataset dataset)
        {
            this.dataset = dataset;
        }
        
        /**
         * DataSet Getter
         * Item Type�� Dataset�� ��츸 �ش�
         */
        public TCComponentDataset getDataSet()
        {
            return this.dataset;
            
        }
        
        /**
         * �ʼ� Override Method
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
