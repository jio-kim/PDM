package com.kgm.common.bundlework.bwutil;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Group;

public class BWDOptionDialog extends Dialog
{
    
    protected Object result;
    protected Shell shlOptionDialog;
    
    /**
     * Create the dialog.
     * @param parent
     * @param style
     */
    public BWDOptionDialog(Shell parent, int style)
    {
        super(parent, style);
        setText("SWT Dialog");
    }
    
    /**
     * Open the dialog.
     * @return the result
     */
    public Object open()
    {
        createContents();
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
    
    /**
     * Create contents of the dialog.
     */
    private void createContents()
    {
        shlOptionDialog = new Shell(getParent(), getStyle());
        shlOptionDialog.setSize(450, 440);
        shlOptionDialog.setText("�ϰ����ε� Option Dialog");
        
        Group grpItemOption = new Group(shlOptionDialog, SWT.NONE);
        grpItemOption.setText("Item Option");
        grpItemOption.setBounds(10, 10, 412, 141);
        
        Button chkItemIDBlankable = new Button(grpItemOption, SWT.CHECK);
        chkItemIDBlankable.setBounds(10, 23, 342, 16);
        chkItemIDBlankable.setText("ItemID, Revision ���� ��뿩��(������ ID �űԹ߹�)");
        
        Button chkItemCreatable = new Button(grpItemOption, SWT.CHECK);
        chkItemCreatable.setBounds(10, 45, 267, 16);
        chkItemCreatable.setText("Item ���� ����");
        
        Button chkItemModifiable = new Button(grpItemOption, SWT.CHECK);
        chkItemModifiable.setBounds(10, 67, 292, 16);
        chkItemModifiable.setText("Item ���� ����");
        
        Button chkRevCreatable = new Button(grpItemOption, SWT.CHECK);
        chkRevCreatable.setBounds(10, 89, 310, 16);
        chkRevCreatable.setText("Item Revision ���� ����");
        
        Button chkRevModifiable = new Button(grpItemOption, SWT.CHECK);
        chkRevModifiable.setBounds(10, 111, 292, 16);
        chkRevModifiable.setText("Item Revision ���� ����");
        
        Group grpDatasetOption = new Group(shlOptionDialog, SWT.NONE);
        grpDatasetOption.setText("Dataset Option");
        grpDatasetOption.setBounds(10, 157, 412, 74);
        
        Button chkDSAvailable = new Button(grpDatasetOption, SWT.CHECK);
        chkDSAvailable.setBounds(10, 21, 310, 16);
        chkDSAvailable.setText("DataSet ��� ����");
        
        Button chkDSChangable = new Button(grpDatasetOption, SWT.CHECK);
        chkDSChangable.setBounds(10, 43, 310, 16);
        chkDSChangable.setText("DataSet ���� �� ���� ����(��ü)");
        
        Group grpBomOption = new Group(shlOptionDialog, SWT.NONE);
        grpBomOption.setText("BOM Option");
        grpBomOption.setBounds(10, 237, 412, 92);
        
        Button chkBOMAvailable = new Button(grpBomOption, SWT.CHECK);
        chkBOMAvailable.setBounds(10, 20, 292, 16);
        chkBOMAvailable.setText("BOM ��� ����");
        
        Button chkBOMRearrange = new Button(grpBomOption, SWT.CHECK);
        chkBOMRearrange.setBounds(10, 42, 342, 16);
        chkBOMRearrange.setText("BOM �籸�� ����(BOM Structure ���� �� �籸��)");
        
        Button chkBOMLineModifiable = new Button(grpBomOption, SWT.CHECK);
        chkBOMLineModifiable.setBounds(10, 64, 292, 16);
        chkBOMLineModifiable.setText("BOM Line �Ӽ� ���� ����");
        
        Button executeButton = new Button(shlOptionDialog, SWT.NONE);
        executeButton.setBounds(121, 357, 77, 22);
        executeButton.setText("Ȯ��");
        
        Button cancelButton = new Button(shlOptionDialog, SWT.NONE);
        cancelButton.setBounds(236, 357, 77, 22);
        cancelButton.setText("���");
        
    }
}
