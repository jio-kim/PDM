/**
 *
 */
package com.symc.plm.me.sdv.view.sdvsample;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.sdv.core.util.UIUtil;

import com.teamcenter.rac.util.MessageBox;

/**
 * Class Name : SDVSampleView
 * Class Description :
 * @date 2013. 10. 11.
 *
 */
public class SDVSampleView extends AbstractSDVViewPane
{
    
    private static final String DEFAULT_VIEW_ID = "__defaultViewId__";
    private Label lblNewLabel;
    private Text text;
    @SuppressWarnings("unused")
    private Text text_1;

    /**
     * Create the composite.
     * @param parent
     * @param style
     */
    public SDVSampleView(Composite parent, int style) {
        super(parent, style, DEFAULT_VIEW_ID);
    }

    /**
     * Create the composite.
     * @param parent
     * @param style
     */
    public SDVSampleView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    @Override
    protected void initUI(Composite parent){
        parent.setBackground(UIUtil.getColor(SWT.COLOR_WIDGET_BACKGROUND));
        
        FillLayout fillLayout = (FillLayout) parent.getLayout();
        fillLayout.type = SWT.VERTICAL;
        fillLayout.spacing = 20;
        Composite composite_1 = new Composite(parent, SWT.NONE);
        composite_1.setLayout(new GridLayout(1, true));
        
        Composite composite_2 = new Composite(composite_1, SWT.NONE);
        composite_2.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite_2.setLayout(new FillLayout());
        lblNewLabel = new Label(composite_2, SWT.BORDER | SWT.BORDER_SOLID);
        lblNewLabel.setBackground(UIUtil.getColor(SWT.COLOR_BLACK));
        lblNewLabel.setForeground(UIUtil.getColor(SWT.COLOR_YELLOW));
        lblNewLabel.setText("VIEWID = " + getId());
        
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(4, false));
        
        Label lblNewLabel_1 = new Label(composite, SWT.NONE);
        lblNewLabel_1.setBounds(0, 0, 56, 15);
        lblNewLabel_1.setText("New Label");
        
        text = new Text(composite, SWT.BORDER);
        text.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
        
        Label lblNewLabel_2 = new Label(composite, SWT.NONE);
        lblNewLabel_2.setBounds(0, 0, 56, 15);
        lblNewLabel_2.setText("New Label");
        
        text_1 = new Text(composite, SWT.BORDER);
        
        Label lblNewLabel_3 = new Label(composite, SWT.NONE);
        lblNewLabel_3.setBounds(0, 0, 56, 15);
        lblNewLabel_3.setText("New Label");
        
        Combo combo = new Combo(composite, SWT.NONE);
        combo.setBounds(0, 0, 450, 23);
        
        DateTime dateTime = new DateTime(composite, SWT.BORDER);
        dateTime.setBounds(0, 0, 88, 24);
        
        Label lblNewLabel_4 = new Label(composite, SWT.NONE);
        lblNewLabel_4.setBounds(0, 0, 56, 15);
        lblNewLabel_4.setText("New Label");
       // fireUICompleteEvent();
    }

    public void callAction1(){
        MessageBox.post(getShell(), "[ " + getId() + " ]===>callAction1 method call", "action method called", MessageBox.INFORMATION);
    }

    public void callAction2(){
        MessageBox.post(getShell(), "[ " + getId() + " ]===>callAction2 method call", "action method called", MessageBox.INFORMATION);
    }

    /**
     * Description :
     * @method :
     * @date : 2013. 12. 4.
     * @author : CS.Park
     * @param :
     * @return : 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#setLocalDataMap(org.sdv.core.common.data.IDataMap)
     */
    @Override
    public void setLocalDataMap(IDataMap dataMap) {
    }

    /**
     * Description :
     * @method :
     * @date : 2013. 12. 4.
     * @author : CS.Park
     * @param :
     * @return : 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalDataMap()
     */
    @Override
    public IDataMap getLocalDataMap() {
        return null;
    }

    /**
     * Description :
     * @method :
     * @date : 2013. 12. 4.
     * @author : CS.Park
     * @param :
     * @return : 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalSelectDataMap()
     */
    @Override
    public IDataMap getLocalSelectDataMap() {
        return null;
    }

    /**
     * Description :
     * @method :
     * @date : 2013. 12. 4.
     * @author : CS.Park
     * @param :
     * @return : 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#getInitOperation()
     */
    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return null;
    }

    /**
     * Description :
     * @method :
     * @date : 2013. 12. 4.
     * @author : CS.Park
     * @param :
     * @return : 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initalizeLocalData(int, org.sdv.core.common.IViewPane, org.sdv.core.common.data.IDataSet)
     */
    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
    }

    /**
     * Description :
     * @method :
     * @date : 2013. 12. 4.
     * @author : CS.Park
     * @param :
     * @return : 
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#uiLoadCompleted()
     */
    @Override
    public void uiLoadCompleted() {
    }
}
