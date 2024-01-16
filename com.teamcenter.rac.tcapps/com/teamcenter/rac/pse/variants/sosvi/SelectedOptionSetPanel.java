// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.teamcenter.rac.pse.variants.sosvi;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.teamcenter.rac.util.MessageBox;

// Referenced classes of package com.teamcenter.rac.pse.variants.sosvi:
//            SelectedOptionSetStatusPanel, SelectedOptionSetDialog, SelectedOptionSetTablePanel, SelectedOptionSetButtonPanel

@SuppressWarnings("serial")
class SelectedOptionSetPanel extends JPanel
{

    SelectedOptionSetPanel(SelectedOptionSetDialog selectedoptionsetdialog)
    {
        super(new BorderLayout());
        setBorder(new EmptyBorder(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE));
        sosStatusPanel = new SelectedOptionSetStatusPanel(selectedoptionsetdialog.getSOS());
        add(sosStatusPanel, "South");
        sosTablePanel = new SelectedOptionSetTablePanel(selectedoptionsetdialog);
        try {
            /**
             * [SR150522-021][2015.06.01][jclee] 속도 개선
             */
        	sosTablePanel.parseCondition();
		} catch (Exception e) {
			MessageBox.post(e);
		}
        add(sosTablePanel, "Center");
        sosButtonPanel = new SelectedOptionSetButtonPanel(selectedoptionsetdialog);
        add(sosButtonPanel, "East");
    }

    void refresh()
    {
        sosTablePanel.refresh();
        sosStatusPanel.refresh();
        sosButtonPanel.refresh();
    }

    void refreshForNewBOMLine()
    {
        sosTablePanel.refreshForNewBOMLine();
        sosStatusPanel.refresh();
        sosButtonPanel.refresh();
    }

    public SelectedOptionSetTablePanel getSosTablePanel() {
        return sosTablePanel;
    }

    static final int MARGIN_SIZE = 15;
    private SelectedOptionSetStatusPanel sosStatusPanel;
    private SelectedOptionSetTablePanel sosTablePanel;
    private SelectedOptionSetButtonPanel sosButtonPanel;
}
