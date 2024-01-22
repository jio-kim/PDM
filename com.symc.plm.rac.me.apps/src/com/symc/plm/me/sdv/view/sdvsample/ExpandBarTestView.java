package com.symc.plm.me.sdv.view.sdvsample;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

public class ExpandBarTestView extends Composite {
    private Text txtText;
    private Text txtText_1;
    private Table table;

    /**
     * Create the composite.
     * @param parent
     * @param style
     */
    public ExpandBarTestView(Composite parent, int style) {
        super(parent, style);
        setLayout(new FormLayout());
        
        Composite composite = new Composite(this, SWT.NONE);
        FormData fd_composite = new FormData();
        fd_composite.bottom = new FormAttachment(0, 300);
        fd_composite.right = new FormAttachment(0, 450);
        fd_composite.top = new FormAttachment(0);
        fd_composite.left = new FormAttachment(0);
        composite.setLayoutData(fd_composite);
        composite.setLayout(null);
        
        final ExpandBar expandBar = new ExpandBar(composite, SWT.NONE);
        expandBar.setBounds(0, 0, 450, 30);
        
        final ExpandItem xpndtmSample = new ExpandItem(expandBar, SWT.NONE);
        xpndtmSample.setExpanded(true);
        xpndtmSample.setText("ExpandItem 1");
        if(!xpndtmSample.getExpanded()) {
            expandBar.setSize(450, 30);
            expandBar.redraw();
        }

        
        Composite composite_1 = new Composite(expandBar, SWT.NONE);
        xpndtmSample.setControl(composite_1);
        int xpndtmSampleHeight = xpndtmSample.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        xpndtmSample.setHeight(xpndtmSampleHeight);
//        expandBar.setHeight(xpndtmSampleHeight);
        expandBar.setSize(450, xpndtmSampleHeight + 30);
        expandBar.redraw();
        
        
        composite_1.setLayout(new GridLayout(1, false));
        txtText = new Text(composite_1, SWT.BORDER);
        txtText.setText("text1");
        
        txtText_1 = new Text(composite_1, SWT.BORDER);
        txtText_1.setText("text2");
        
        table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        table.setBounds(0, 100, 450, 200);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        
//        ExpandBar bar = new ExpandBar(parent, SWT.V_SCROLL);
//        Image image = new Image(getDisplay(), "yourFile.gif");
//
//        // First item
//        Composite composite = new Composite(bar, SWT.NONE);
//        GridLayout layout = new GridLayout (2, false);
//        layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
//        layout.verticalSpacing = 10;
//        composite.setLayout(layout);  
//        Label label = new Label (composite, SWT.NONE);
//        label.setImage(getDisplay().getSystemImage(SWT.ICON_ERROR));
//        label = new Label (composite, SWT.NONE);
//        label.setText("SWT.ICON_ERROR");
//        label = new Label (composite, SWT.NONE);
//        label.setImage(getDisplay().getSystemImage(SWT.ICON_INFORMATION));
//        label = new Label (composite, SWT.NONE);
//        label.setText("SWT.ICON_INFORMATION");
//        label = new Label (composite, SWT.NONE);
//        label.setImage(getDisplay().getSystemImage(SWT.ICON_WARNING));
//        label = new Label (composite, SWT.NONE);
//        label.setText("SWT.ICON_WARNING");
//        label = new Label (composite, SWT.NONE);
//        label.setImage(getDisplay().getSystemImage(SWT.ICON_QUESTION));
//        label = new Label (composite, SWT.NONE);
//        label.setText("SWT.ICON_QUESTION");
//        
//        ExpandItem item0 = new ExpandItem(bar, SWT.NONE, 0);
//        item0.setText("What is your favorite button");
//        item0.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
//        item0.setControl(composite);
//        item0.setImage(image);
//
//        item0.setExpanded(true);
//        
//        ExpandItem item1 = new ExpandItem(bar, SWT.NONE, 0);
//        item1.setText("What is your favorite Icon");
//        item1.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
//        item1.setControl(composite);
//        item1.setImage(image);
//        
//        item1.setExpanded(true);
//
//        bar.setSpacing(8);

    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
