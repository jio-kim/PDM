package com.ssangyong.common.utils.progressbar;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.ModernGroupByThemeExtension;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.theme.ThemeConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import ca.odell.glazedlists.EventList;



/**
 * Progress Dialog
 * 
 * @author Administrator
 * 
 */
public class WaitProgressDialog extends Dialog
{

    private Label mainMessageLabel = null; // Main 메세지 Label

    private boolean isShowMessageTable = false; // 메세지 Table Show 유무
    private CGifLabel lblImageLabel = null; // 이미지 Label

    private int width = 0, height = 0; // Dialog 너비, 길이
    private Button okButton = null;
    private Button cancelButton = null;

    private static final String PROP_NAME_MSG_TYPE = "msgType"; // 메세지 유형
    private static final String PROP_NAME_TARGET_ID = "targetId"; // 대상 Id
    private static final String PROP_NAME_MESSAGE = "message"; // 메세지 타입

    private static final int COLUMN_DEFAULT_SIZE_MSG_TYPE = 45; // 메세지 유형 Column 사이즈
    private static final int COLUMN_DEFAULT_SIZE_TARGET_ID = 140;// 대상 Id Column 사이즈
    private static final int COLUMN_DEFAULT_SIZE_MESSAGE = 380;// 메세지 타입 Column 사이즈

    private static final int DIAOG_DEFALUT_WIDTH = 500; // DIALOG 기본 너비
    private static final int DIAOG_DEFALUT_HEIGHT = 150; // DIALOG 기본 높이
    private static final int DIAOG_INCLUDED_TABLE_DEFALUT_WIDTH = 640; // Table DIALOG 기본 너비
    private static final int DIAOG_INCLUDED_TABLE_DEFALUT_HEIGHT = 300; // Table DIALOG 기본 높이

    private EventList<MessageData> tableDataList; // 메세지 Table Data 리스트
    private int[] msgTableVisibleColumnIndexs = null; // Message Table 보여지는 Column 인텍스

    WaitProgressCancelImpl cancelImpl;

    private NatTable messageTable = null;
    
    /**
     * 생성자
     * 
     * @param parentShell
     * @param shellStyle
     * @param width 너비
     * @param height 높이
     * @param isMessageTable 메세지 Table
     * @param msgTableVisibleColumnIndexs
     */
    public WaitProgressDialog(Shell parentShell, int width, int height, boolean isShowMessageTable, int[] msgTableVisibleColumnIndexs, WaitProgressCancelImpl cancelImpl)
    {
        this(parentShell, isShowMessageTable, msgTableVisibleColumnIndexs, cancelImpl);
        this.width = width;
        this.height = height;
        
        
        
    }

    /**
     * 생성자
     * 
     * @wbp.parser.constructor
     */
    public WaitProgressDialog(Shell parentShell, WaitProgressCancelImpl cancelImpl)
    {
        this(parentShell, true, null, cancelImpl);
    }

    /**
     * Create the dialog.
     * 
     * @param parentShell
     */
    public WaitProgressDialog(Shell parentShell, boolean isShowMessageTable, int[] msgTableVisibleColumnIndexs, WaitProgressCancelImpl cancelImpl)
    {
        super(parentShell);
        
        setShellStyle(SWT.PRIMARY_MODAL);
        
        this.cancelImpl = cancelImpl;
        
        this.isShowMessageTable = isShowMessageTable;
        this.msgTableVisibleColumnIndexs = msgTableVisibleColumnIndexs;
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        centerDialogOnScreen(getShell());

        final Composite rootComposite = (Composite) super.createDialogArea(parent);
        rootComposite.setLayout(new FillLayout(SWT.HORIZONTAL));

        final SashForm sashForm = new SashForm(rootComposite, SWT.VERTICAL);

        Composite composite = new Composite(sashForm, SWT.NONE);
        composite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        composite.setLayout(new GridLayout(2, false));

        lblImageLabel = new CGifLabel(composite, SWT.NONE);
        GridData gd_lblImageLabel = new GridData(SWT.CENTER, SWT.CENTER, false, false, 0, 1);
        gd_lblImageLabel.widthHint = 50;
        gd_lblImageLabel.heightHint = 50;
        lblImageLabel.setLayoutData(gd_lblImageLabel);
        InputStream imageIS = getClass().getResourceAsStream("progress48.gif");

        lblImageLabel.setGifImage(imageIS);

        lblImageLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        lblImageLabel.setLayout(null);

        
        
        mainMessageLabel = new Label(composite, SWT.WRAP);

        mainMessageLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));

        mainMessageLabel.setFont(SWTResourceManager.getFont("Arial", 10, SWT.NO));
        mainMessageLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        if (isShowMessageTable)
        {
            CreateMeassageTable(sashForm);
            sashForm.setWeights(new int[] { 3, 7 });
        }

        sashForm.addListener(SWT.Resize, new Listener()
        {
            @Override
            public void handleEvent(Event arg0)
            {
                org.eclipse.swt.graphics.Point size = sashForm.getSize();
                int bottomSize = size.y - 60;

                if (isShowMessageTable)
                {
                    if (bottomSize < 0)
                        return;
                    sashForm.setWeights(new int[] { 60, bottomSize });
                    rootComposite.layout();
                }
            }
        });
        this.setReturnCode(-1);
        return rootComposite;
    }

    /**
     * Message Table
     * 
     * @param parent
     * @return
     */
    private NatTable CreateMeassageTable(Composite parent)
    {
        
        
        // Column Property
        String[] propertyNames = getPropertyNames();
        Map<String, String> propertyToLabelMap = getPropertyToLabelMap();
        ConfigRegistry configRegistry = new ConfigRegistry();
        BasicGridEditorGridLayer<MessageData> gridLayer = new BasicGridEditorGridLayer<MessageData>(new ArrayList<MessageData>(), configRegistry,
                propertyNames, propertyToLabelMap);
        DataLayer bodyDataLayer = gridLayer.getBodyLayer().getDataLayer();
        tableDataList = gridLayer.getTableDataList();

        ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
        bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);

        messageTable  = new NatTable(parent, gridLayer, false);
//        GridData tableData = new GridData();
//        tableData.widthHint = 100;
//        messageTable.setLayoutData(tableData);
        messageTable.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
        messageTable.setBackgroundMode(SWT.INHERIT_FORCE);
        
        messageTable.setConfigRegistry(configRegistry);
        messageTable.addConfiguration(new DefaultNatTableStyleConfiguration()); // 기본 Style 설정
        messageTable.addConfiguration(setModelEditorConfiguration(columnLabelAccumulator, bodyDataLayer));
        messageTable.addConfiguration(new SingleClickSortConfiguration()); // Single Click 시 Sort 설정
        messageTable.configure();
        
        bodyDataLayer.setDefaultColumnWidthByPosition(0, 20);
        bodyDataLayer.setDefaultColumnWidthByPosition(getColumnIndexOfProperty(PROP_NAME_MSG_TYPE), COLUMN_DEFAULT_SIZE_MSG_TYPE);
        bodyDataLayer.setDefaultColumnWidthByPosition(getColumnIndexOfProperty(PROP_NAME_TARGET_ID), COLUMN_DEFAULT_SIZE_TARGET_ID);
        bodyDataLayer.setDefaultColumnWidthByPosition(getColumnIndexOfProperty(PROP_NAME_MESSAGE), COLUMN_DEFAULT_SIZE_MESSAGE);

        if (msgTableVisibleColumnIndexs != null)
        {
            if (msgTableVisibleColumnIndexs.length == 1)
            {
                bodyDataLayer.setDefaultColumnWidthByPosition(getColumnIndexOfProperty(PROP_NAME_MSG_TYPE), 0);
                bodyDataLayer.setDefaultColumnWidthByPosition(getColumnIndexOfProperty(PROP_NAME_TARGET_ID), 0);
            }
            else
            {
                boolean isVisibleMsgType = false, isVisibleTargetId = false;
                for (int msgTableVisibleColumnIndex : msgTableVisibleColumnIndexs)
                {
                    if (msgTableVisibleColumnIndex == getColumnIndexOfProperty(PROP_NAME_MSG_TYPE))
                        isVisibleMsgType = true;
                    else if (msgTableVisibleColumnIndex == getColumnIndexOfProperty(PROP_NAME_MSG_TYPE))
                        isVisibleTargetId = true;
                }
                if (!isVisibleMsgType)
                    bodyDataLayer.setDefaultColumnWidthByPosition(getColumnIndexOfProperty(PROP_NAME_MSG_TYPE), 0);
                if (!isVisibleTargetId)
                    bodyDataLayer.setDefaultColumnWidthByPosition(getColumnIndexOfProperty(PROP_NAME_TARGET_ID), 0);
            }
        }
        


        return messageTable;
    }

    /**
     * Create contents of the button bar.
     * 
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent)
    {
        okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        okButton.setEnabled(false);
        
        
        if( this.cancelImpl != null)
        {
            cancelButton = createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
            cancelButton.setEnabled(true);
        }
    }

    /**
     * Center 로 위치
     * 
     * @param shell
     */
    private void centerDialogOnScreen(Shell shell)
    {
        if (width != 0)
            shell.setSize(width, height);
        else
        {
            if (isShowMessageTable)
            {
                shell.setSize(DIAOG_INCLUDED_TABLE_DEFALUT_WIDTH, DIAOG_INCLUDED_TABLE_DEFALUT_HEIGHT);
            }
            else
            {
                shell.setSize(DIAOG_DEFALUT_WIDTH, DIAOG_DEFALUT_HEIGHT);
            }
        }
        Rectangle parentSize = getParentShell().getBounds();
        Rectangle mySize = shell.getBounds();
        int locationX, locationY;
        locationX = (parentSize.width - mySize.width) / 2 + parentSize.x;
        locationY = (parentSize.height - mySize.height) / 2 + parentSize.y;
        shell.setLocation(locationX, locationY);
    }

    
    
    /**
     * Main Message 입력
     * 
     * @param msg
     */
    public void setTitle(final String msg)
    {
        try
        {
            Display.getDefault().syncExec(new Runnable()
            {
                public void run()
                {
                    if (WaitProgressDialog.this.getShell() == null || WaitProgressDialog.this.getShell().isDisposed())
                        return;


                    ((Composite)WaitProgressDialog.this.dialogArea).getShell().setText(msg);

                    //getShell().layout(new Control[] { mainMessageLabel });
                }
            });
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * Main Message 입력
     * 
     * @param msg
     */
    public void setMainMessage(final String msg)
    {
        if( msg == null)
            return;
        
        try
        {
            Display.getDefault().syncExec(new Runnable()
            {
                public void run()
                {
                    if (WaitProgressDialog.this.getShell() == null || WaitProgressDialog.this.getShell().isDisposed())
                        return;
                    
                    mainMessageLabel.setText(msg);

                    getShell().layout(new Control[] { mainMessageLabel });
                }
            });
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Table Message 입력 처리
     * 
     * @param msg
     */
    public void setTableMessage(final String msgType, final String targetId, final String message)
    {
        Display.getDefault().syncExec(new Runnable()
        {
            public void run()
            {
                if (WaitProgressDialog.this.getShell() == null || WaitProgressDialog.this.getShell().isDisposed())
                    return;
                MessageData rowData = new MessageData(msgType, targetId, message);
                tableDataList.add(rowData);

            }
        });
    }

    /**
     * Progress 완료 후 처리
     */
    public void setCompleteAction()
    {
        // Image iconImage = ResourceManager.getPluginImageDescriptor("com.dh", "icons/progress.gif").createImage();
        // 버튼 활성화
        this.getShell().getDisplay().asyncExec(new Runnable()
        {
            public void run()
            {
                if (WaitProgressDialog.this.getShell() == null || WaitProgressDialog.this.getShell().isDisposed())
                {

                    System.out.println("####WaitProgressDialog.this.getShell() == null || WaitProgressDialog.this.getShell().isDisposed()");
                    return;
                }
                // 완료 이미지 처리
                //lblImageLabel.setImage(new Image(WaitProgressDialog.this.getShell().getDisplay(),"./complete48.gif"));
                //ImageDescriptor.createFromImage("complete48.gif");
                
                InputStream imageIS = getClass().getResourceAsStream("warning_32.png");
                
                
                ImageData data=new ImageData(imageIS);
                
                lblImageLabel.setImage(new Image(WaitProgressDialog.this.getShell().getDisplay(),data));
                
                
                
                
                
                okButton.setEnabled(true);
            }
        });
    }

    @Override
    protected void okPressed()
    {

        if (!isValidateOk())
            return;

        this.close();

    }
    
    
    @Override
    protected void cancelPressed()
    {
        
        this.setCompleteAction();
        
        if( cancelImpl != null )
            cancelImpl.cancelOperation();
        
        
    }
    


    /**
     * Validation 처리
     * 
     * @return
     */
    private boolean isValidateOk()
    {
        return true;
    }

    private String[] getPropertyNames()
    {
        return new String[] { PROP_NAME_MSG_TYPE, PROP_NAME_TARGET_ID, PROP_NAME_MESSAGE };
    }

    public List<String> getPropertyNamesAsList()
    {
        return Arrays.asList(getPropertyNames());
    }

    public int getColumnIndexOfProperty(String propertyName)
    {
        return getPropertyNamesAsList().indexOf(propertyName);
    }

    private Map<String, String> getPropertyToLabelMap()
    {
        Map<String, String> propertyToLabelMap = new LinkedHashMap<String, String>();
        propertyToLabelMap.put(PROP_NAME_MSG_TYPE, "Type");
        propertyToLabelMap.put(PROP_NAME_TARGET_ID, "Item ID");
        propertyToLabelMap.put(PROP_NAME_MESSAGE, "Message");
        return propertyToLabelMap;

    }

    /**
     * Table Data List
     * 
     * @return the tableDataList
     */
    public EventList<MessageData> getTableDataList()
    {
        return tableDataList;
    }
    
    public AbstractRegistryConfiguration setModelEditorConfiguration(final ColumnOverrideLabelAccumulator columnLabelAccumulator, final DataLayer bodyDataLayer) {

        return new AbstractRegistryConfiguration() {
            @Override
            public void configureRegistry(IConfigRegistry configRegistry) {
                registerConfigLabelsOnColumns(columnLabelAccumulator);
                registerColumnOptionPainter(configRegistry);
            }
        };
    }

    private void registerConfigLabelsOnColumns(ColumnOverrideLabelAccumulator columnLabelAccumulator) {
        columnLabelAccumulator
                .registerColumnOverrides(getColumnIndexOfProperty(PROP_NAME_MESSAGE), PROP_NAME_MESSAGE);
    }

    private void registerColumnOptionPainter(IConfigRegistry configRegistry) {
        Style style = new Style();
        style.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.NORMAL, PROP_NAME_MESSAGE);
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, new TextPainter(true, true, false, true), DisplayMode.NORMAL,
                PROP_NAME_MESSAGE);
    }

    /**
     * 메세지 데이터
     * 
     * @author Administrator
     * 
     */
    public class MessageData
    {
        String msgType = null;
        String message = null;
        String targetId = null;

        public MessageData(String msgType, String targetId, String message)
        {
            this.targetId = targetId;
            this.msgType = msgType;
            this.message = message;
        }

        /**
         * @return the targetId
         */
        public String getTargetId()
        {
            return targetId;
        }

        /**
         * @param targetId the targetId to set
         */
        public void setTargetId(String targetId)
        {
            this.targetId = targetId;
        }

        /**
         * @return the msgType
         */
        public String getMsgType()
        {
            return msgType;
        }

        /**
         * @param msgType the msgType to set
         */
        public void setMsgType(String msgType)
        {
            this.msgType = msgType;
        }

        /**
         * @return the message
         */
        public String getMessage()
        {
            return message;
        }

        /**
         * @param message the message to set
         */
        public void setMessage(String message)
        {
            this.message = message;
        }
    }
}
