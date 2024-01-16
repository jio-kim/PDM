package com.ssangyong.common.bundlework.bwutil;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Group;

public class BWUOptionDialog extends Dialog
{
    
    protected Object result;
    protected Shell shlOptionDialog;
    
    private BWUOption bwUOption;
    
    /**
     * Create the dialog.
     * @param parent
     * @param style
     */
    public BWUOptionDialog(Shell parent, int style, BWUOption bwUOption)
    {
        super(parent, style);
        setText("SWT Dialog");
        this.bwUOption = bwUOption;
        
        
    }
    
    /**
     * Open the dialog.
     * @return the result
     */
    public Object open()
    {
        createContents();
        initOption();
        shlOptionDialog.open();
        shlOptionDialog.layout();
        Display display = getParent().getDisplay();
        while (!shlOptionDialog.isDisposed())
        {
            if (!display.readAndDispatch())
            {
                display.sleep();
            }
        }
        
        return result;
    }
    
    Button chkItemIDBlankable = null;
    Button chkItemCreatable = null;
    Button chkItemModifiable = null;
    Button chkRevCreatable = null;
    Button chkRevModifiable = null;
    Button chkAutocadValidation = null;
    Button chkDSAvailable = null;
    Button chkDSChangable = null;
    Button chkBOMAvailable = null;
    Button chkBOMRearrange = null;
    Button chkBOMLineModifiable = null;
    
    /**
     * Create contents of the dialog.
     */
    private void createContents()
    {
        this.shlOptionDialog = new Shell(getParent(), getStyle());
        this.shlOptionDialog.setSize(450, 492);
        this.shlOptionDialog.setText("일괄업로드 Option Dialog");
        
        Group grpItemOption = new Group(shlOptionDialog, SWT.NONE);
        grpItemOption.setText("Item Option");
        grpItemOption.setBounds(10, 10, 412, 168);
        
        this.chkItemIDBlankable = new Button(grpItemOption, SWT.CHECK);
        this.chkItemIDBlankable.setBounds(10, 108, 342, 16);
        this.chkItemIDBlankable.setText("ItemID, Revision 공백 허용여부(생성시 ID 신규발번)");
        
        this.chkItemCreatable = new Button(grpItemOption, SWT.CHECK);
        this.chkItemCreatable.setBounds(10, 20, 267, 16);
        this.chkItemCreatable.setText("Item 생성 여부");
        
        this.chkItemModifiable = new Button(grpItemOption, SWT.CHECK);
        this.chkItemModifiable.setBounds(10, 42, 292, 16);
        this.chkItemModifiable.setText("Item 수정 여부");
        
        this.chkRevCreatable = new Button(grpItemOption, SWT.CHECK);
        this.chkRevCreatable.setBounds(10, 64, 310, 16);
        this.chkRevCreatable.setText("Item Revision 생성 여부");
        
        this.chkRevModifiable = new Button(grpItemOption, SWT.CHECK);
        this.chkRevModifiable.setBounds(10, 86, 292, 16);
        this.chkRevModifiable.setText("Item Revision 수정 여부");
        
        this.chkAutocadValidation = new Button(grpItemOption, SWT.CHECK);
        this.chkAutocadValidation.setBounds(10, 130, 292, 16);
        this.chkAutocadValidation.setText("AutoCAD Validation Check 여부");
        
        Group grpDatasetOption = new Group(shlOptionDialog, SWT.NONE);
        grpDatasetOption.setText("Dataset Option");
        grpDatasetOption.setBounds(10, 195, 412, 74);
        
        this.chkDSAvailable = new Button(grpDatasetOption, SWT.CHECK);
        this.chkDSAvailable.setBounds(10, 21, 310, 16);
        this.chkDSAvailable.setText("DataSet 사용 여부");
        
        this.chkDSChangable = new Button(grpDatasetOption, SWT.CHECK);
        this.chkDSChangable.setBounds(10, 43, 310, 16);
        this.chkDSChangable.setText("DataSet 삭제 후 생성 여부(교체)");
        
        Group grpBomOption = new Group(shlOptionDialog, SWT.NONE);
        grpBomOption.setText("BOM Option");
        grpBomOption.setBounds(10, 289, 412, 92);
        
        this.chkBOMAvailable = new Button(grpBomOption, SWT.CHECK);
        this.chkBOMAvailable.setBounds(10, 20, 292, 16);
        this.chkBOMAvailable.setText("BOM 사용 여부");
        
        this.chkBOMRearrange = new Button(grpBomOption, SWT.CHECK);
        this.chkBOMRearrange.setBounds(10, 42, 342, 16);
        this.chkBOMRearrange.setText("BOM 재구성 여부(BOM Structure 삭제 후 재구성)");
        
        this.chkBOMLineModifiable = new Button(grpBomOption, SWT.CHECK);
        this.chkBOMLineModifiable.setBounds(10, 64, 292, 16);
        this.chkBOMLineModifiable.setText("BOM Line 속성 수정 여부");
        
        Button executeButton = new Button(shlOptionDialog, SWT.NONE);
        executeButton.setBounds(121, 412, 77, 22);
        executeButton.setText("확인");
        executeButton.addSelectionListener(new SelectionListener()
        {
            public void widgetSelected(SelectionEvent e)
            {
                
                setOption();
                shlOptionDialog.dispose();
            }
            
            public void widgetDefaultSelected(SelectionEvent e)
            {
            }
        });
        
        Button cancelButton = new Button(shlOptionDialog, SWT.NONE);
        cancelButton.setBounds(238, 412, 77, 22);
        cancelButton.setText("취소");
        cancelButton.addSelectionListener(new SelectionListener()
        {
            public void widgetSelected(SelectionEvent e)
            {
                // dialog Close
                shlOptionDialog.dispose();
            }
            
            public void widgetDefaultSelected(SelectionEvent e)
            {
            }
        });
     
        
        // dialog를 화면 중앙으로 이동
        Rectangle screen = this.shlOptionDialog.getDisplay().getMonitors()[0].getBounds();
        Rectangle shellBounds = this.shlOptionDialog.getBounds();
        this.shlOptionDialog.setBounds((screen.width - shellBounds.width) / 2, (screen.height - shellBounds.height) / 2, shellBounds.width, shellBounds.height);

    }
    
    public void setOption()
    {
        this.bwUOption.setAutoCADValidatable(this.chkAutocadValidation.getSelection());
        this.bwUOption.setItemIDBlankable(this.chkItemIDBlankable.getSelection());
        this.bwUOption.setItemCreatable(this.chkItemCreatable.getSelection());
        this.bwUOption.setItemModifiable(this.chkItemModifiable.getSelection());
        this.bwUOption.setRevCreatable(this.chkRevCreatable.getSelection());
        this.bwUOption.setRevModifiable(this.chkRevModifiable.getSelection());
        
        this.bwUOption.setDSAvailable(this.chkDSAvailable.getSelection());
        this.bwUOption.setDSChangable(this.chkDSChangable.getSelection());
        
        this.bwUOption.setBOMLineModifiable(this.chkBOMLineModifiable.getSelection());
        this.bwUOption.setBOMRearrange(this.chkBOMRearrange.getSelection());
        this.bwUOption.setBOMAvailable(this.chkBOMAvailable.getSelection());
    }
    
    public void initOption()
    {
        this.chkAutocadValidation.setSelection(this.bwUOption.isAutoCADValidatable());
        this.chkItemIDBlankable.setSelection(this.bwUOption.isItemIDBlankable());
        this.chkItemCreatable.setSelection(this.bwUOption.isItemCreatable());
        this.chkItemModifiable.setSelection(this.bwUOption.isItemModifiable());
        this.chkRevCreatable.setSelection(this.bwUOption.isRevCreatable());
        this.chkRevModifiable.setSelection(this.bwUOption.isRevModifiable());
        this.chkDSAvailable.setSelection(this.bwUOption.isDSAvailable());
        this.chkDSChangable.setSelection(this.bwUOption.isDSChangable());
        this.chkBOMLineModifiable.setSelection(this.bwUOption.isBOMLineModifiable());
        this.chkBOMRearrange.setSelection(this.bwUOption.isBOMRearrange());
        this.chkBOMAvailable.setSelection(this.bwUOption.isBOMAvailable());
    }
    
}
