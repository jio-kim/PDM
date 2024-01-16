package com.ssangyong.commands.ec.eco;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.ssangyong.common.utils.StringUtil;

public class ECOEPLSWTOptionsDialog extends Dialog {

    protected Object result;
    protected Shell shlEcoOptionsDialog;
    private Text oldText;
    private Text newText;
    
    TableItem oldItem = null;
    TableItem newItem = null;

    /**
     * Create the dialog.
     * @param parent
     * @param style
     */
    public ECOEPLSWTOptionsDialog(Shell parent, int style, TableItem oldItem, TableItem newItem) {
        super(parent, style);        
        setText("SWT Dialog");
        this.oldItem = oldItem;
        this.newItem = newItem;
    }

    /**
     * Open the dialog.
     * @return the result
     */
    public Object open() {
        createContents();
        shlEcoOptionsDialog.open();
        shlEcoOptionsDialog.layout();
        Display display = getParent().getDisplay();
        while (!shlEcoOptionsDialog.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }       
        return result;
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        shlEcoOptionsDialog = new Shell(getParent(), getStyle());
        shlEcoOptionsDialog.setSize(479, 457);
        shlEcoOptionsDialog.setText("ECO Options Dialog");
               
        Group grpOld = new Group(shlEcoOptionsDialog, SWT.NONE);
        grpOld.setText("OLD");
        grpOld.setBounds(10, 10, 449, 174);
        
        ScrolledComposite scrolledOldComposite = new ScrolledComposite(grpOld, SWT.BORDER);
        scrolledOldComposite.setExpandVertical(true);
        scrolledOldComposite.setExpandHorizontal(true);
        scrolledOldComposite.setBounds(10, 20, 429, 144);
        
        oldText = new Text(scrolledOldComposite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        oldText.setText(this.getConvertOrStrOptions(oldItem));
        scrolledOldComposite.setContent(oldText);
        scrolledOldComposite.setMinSize(oldText.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        scrolledOldComposite.setMinSize(new Point(73, 21));
        
        Group grpNew = new Group(shlEcoOptionsDialog, SWT.NONE);
        grpNew.setText("NEW");
        grpNew.setBounds(10, 205, 449, 174);
        
        ScrolledComposite scrolledNewComposite = new ScrolledComposite(grpNew, SWT.BORDER);
        scrolledNewComposite.setExpandVertical(true);
        scrolledNewComposite.setExpandHorizontal(true);
        scrolledNewComposite.setBounds(10, 20, 429, 144);
        
        newText = new Text(scrolledNewComposite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        newText.setText(this.getConvertOrStrOptions(newItem));
        scrolledNewComposite.setContent(newText);
        scrolledNewComposite.setMinSize(newText.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        scrolledNewComposite.setMinSize(new Point(73, 21));
        
        Button btnCloseButton = new Button(shlEcoOptionsDialog, SWT.NONE);
        btnCloseButton.setBounds(383, 392, 76, 25);
        btnCloseButton.setText("Close");
        btnCloseButton.addSelectionListener(new SelectionAdapter () {
            public void widgetSelected(SelectionEvent e) {
                shlEcoOptionsDialog.close();
            }
        });
    }
    
    /**
     * Options 데이터 설정
     * 
     * @method setOptions 
     * @date 2013. 4. 3.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void setOptions(TableItem oldItem, TableItem newItem) {
        oldText.setText(this.getConvertOrStrOptions(oldItem));
        newText.setText(this.getConvertOrStrOptions(newItem));
    }
    
    /**
     * Options의 "OR"구문을 "\n"으로 변경한다.
     * 
     * @method getConvertOrStrOptions 
     * @date 2013. 4. 3.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getConvertOrStrOptions(TableItem item) {
        String options = "";
        if(item != null && !"".equals(StringUtil.nullToString(item.getText(ECOEPLSWTRendering.OPTIONS)))) {
            options = item.getText(ECOEPLSWTRendering.OPTIONS);
            options = options.replaceAll("OR", "\n");
        }
        return options;
    }
}
