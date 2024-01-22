/**
 * 
 */
package com.symc.plm.me.sdv.dialog.template;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;

/**
 * Class Name : SimpleDialogTemplate
 * Class Description : 
 * @date 2013. 10. 23.
 *
 */
public class SimpleDialogTemplate extends Dialog {
    
    protected Composite paramComposite;
    protected Composite container;
    private int scrollBarWidthSize = 0;
    private Point parentShellSize;
    
    protected Button okButton;
    protected Button applyButton;
    protected Button cancelButton;
    
    public Control actionBar;
    
    /**
     * Create the dialog.
     * @param parentShell
     */
    public SimpleDialogTemplate(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Create contents of the dialog.
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite paramComposite) {
        Composite localComposite = (Composite) super.createDialogArea(paramComposite);
        createDialogWindow(localComposite);
        return localComposite;
    }   

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(450, 300);
    }
    
    protected void createDialogWindow(Composite paramComposite) {
        this.paramComposite = paramComposite;
        Shell dialogParentShell = getShell();
        //dialogParentShell.setBackground(dialogParentShell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        dialogParentShell.setBackgroundMode(SWT.INHERIT_FORCE);
        FillLayout fillLayout = new FillLayout(SWT.NONE);

        paramComposite.setLayout(fillLayout);

        final ScrolledComposite scrolledComposite = new ScrolledComposite(paramComposite, SWT.NONE | SWT.V_SCROLL | SWT.H_SCROLL);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.getVerticalBar().setIncrement(10);
        scrolledComposite.setLayout(fillLayout);
        if (parentShellSize != null) {
            scrolledComposite.setSize(parentShellSize);
            scrollBarWidthSize = parentShellSize.x;
        }
        final Composite createDialogPanel = createDialogPanel(scrolledComposite);
        scrolledComposite.setContent(createDialogPanel);
        scrolledComposite.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                int x;
                if (scrollBarWidthSize != 0) {
                    x = scrollBarWidthSize;
                } else {
                    x = createDialogPanel.getClientArea().x;
                }
                scrolledComposite.setMinSize(createDialogPanel.computeSize(x, SWT.DEFAULT));
            }
        });

    }
    
    protected Control createButtonBar(Composite paramComposite) {

        Composite localMainComposite = new Composite(paramComposite, 0);
        GridLayout localMainGridLayout = new GridLayout();
        localMainGridLayout.numColumns = 1;
        localMainGridLayout.makeColumnsEqualWidth = false;
        localMainGridLayout.marginWidth = 5;
        localMainGridLayout.marginHeight = 0;
        localMainGridLayout.marginTop = 0;
        localMainGridLayout.marginBottom = 0;

        GridData localGridData = new GridData(GridData.FILL_HORIZONTAL);
        localMainComposite.setLayout(localMainGridLayout);
        localMainComposite.setLayoutData(localGridData);

        Label separator = new Label(localMainComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite localComposite = new Composite(paramComposite, 0);
        GridLayout localGridLayout = new GridLayout();
        localGridLayout.numColumns = 1;
        localGridLayout.makeColumnsEqualWidth = false;
        localGridLayout.marginWidth = 0;
        localGridLayout.marginHeight = 0;
        localGridLayout.marginTop = 5;
        localGridLayout.marginBottom = 5;
        localGridLayout.horizontalSpacing = convertHorizontalDLUsToPixels(4);
        localGridLayout.verticalSpacing = convertVerticalDLUsToPixels(4);

        localComposite.setLayout(localGridLayout);
        localGridData = new GridData(128);
        localComposite.setLayoutData(localGridData);

        createButtonsForButtonBar(localComposite);

        return localMainComposite;
    }
    
    protected Control createActionBar(Composite paramComposite) {

        Composite localMainComposite = new Composite(paramComposite, 0);
        GridLayout localMainGridLayout = new GridLayout();
        localMainGridLayout.numColumns = 1;
        localMainGridLayout.makeColumnsEqualWidth = false;
        localMainGridLayout.marginWidth = 5;
        localMainGridLayout.marginHeight = 0;
        localMainGridLayout.marginTop = 0;
        localMainGridLayout.marginBottom = 0;

        GridData localGridData = new GridData(GridData.FILL_HORIZONTAL);
        localMainComposite.setLayout(localMainGridLayout);
        localMainComposite.setLayoutData(localGridData);

        Label separator = new Label(localMainComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite localComposite = new Composite(paramComposite, 0);
        GridLayout localGridLayout = new GridLayout();
        localGridLayout.numColumns = 1;
        localGridLayout.makeColumnsEqualWidth = false;
        localGridLayout.marginWidth = 0;
        localGridLayout.marginHeight = 0;
        localGridLayout.marginTop = 5;
        localGridLayout.marginBottom = 5;
        localGridLayout.horizontalSpacing = convertHorizontalDLUsToPixels(4);
        localGridLayout.verticalSpacing = convertVerticalDLUsToPixels(4);

        localComposite.setLayout(localGridLayout);
        localGridData = new GridData(128);
        localComposite.setLayoutData(localGridData);

        createButtonsForActionBar(localComposite);

        return localMainComposite;
    }

    protected Composite getParentDialogComposite() {
        return this.paramComposite;
    }

    @Override
    protected Control createContents(Composite parent) {
        if (this.parentShellSize != null) {
            parent.setSize(this.parentShellSize.x + 40, this.parentShellSize.y + 30);
            // SYMDisplayUtil.centerToParent(parent.getParent().getShell(), (Shell)parent);
        }
        Composite composite = new Composite(parent, 0);
        GridLayout layout = new GridLayout();
        layout.makeColumnsEqualWidth = true;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(1808));
        applyDialogFont(composite);

        initializeDialogUnits(composite);
        this.actionBar = createActionBar(composite);
        this.dialogArea = createDialogArea(composite);
        this.buttonBar = createButtonBar(composite);
        afterCreateContents();
        return composite;
    }
    
    /**
     * DefaultButton에 이미지 추가 및 적용버튼 추가
     * 
     * @Copyright : S-PALM
     * @author : 권오규
     * @since : 2013. 1. 10.
     * @override
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        okButton = createButton(parent, 0, IDialogConstants.OK_LABEL, true);        
        applyButton = createButton(parent, 2, "Apply", false);        
        cancelButton = createButton(parent, 1, IDialogConstants.CANCEL_LABEL, false);        
        cancelButton.setFocus();
    }
    
    protected void createButtonsForActionBar(Composite parent) {
        //Button editButton = createButton(parent, 0, "Clear", true);
        
    }
    
    protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
        container = new Composite(parentScrolledComposite, SWT.NONE);
        container.setLayout(new FillLayout());         
        try {
            initComponent(container);
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.post(AIFUtility.getActiveDesktop().getShell(), e.toString(), "ERROR", MessageBox.ERROR);
        }       
        return container;
    }
    
    /**
     * View & Compnent Init...
     * 
     * @method initComponent 
     * @date 2013. 10. 23.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    protected void initComponent(Composite container) throws Exception {        
        // 여기에 사용자 View & Component 추가
        Button test = new Button(container, SWT.NONE); 
        test.setText("Test");
    }

    protected void afterCreateContents() {
        
    }
}
