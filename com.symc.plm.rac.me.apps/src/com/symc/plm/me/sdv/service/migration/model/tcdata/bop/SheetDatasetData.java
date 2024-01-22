/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.model.tcdata.bop;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.basic.DatasetData;

/**
 * Class Name : SheetDatasetData
 * Class Description :
 * 
 * @date 2013. 12. 11.
 * 
 */
public class SheetDatasetData extends DatasetData {
    // 작업표준서 I/F 유무
    private boolean isIf;

    public SheetDatasetData(Tree parentTree, int index, String classType, TreeColumn[] columns) {
        super(parentTree, index, classType, columns);
    }

    public SheetDatasetData(TCData parentItem, int index, String classType, TreeColumn[] columns) {
        super(parentItem, index, classType, columns);
    }

    /**
     * @return the isIf
     */
    public boolean isIf() {
        return isIf;
    }

    /**
     * @param isIf
     *            the isIf to set
     */
    public void setIf(boolean isIf) {
        this.isIf = isIf;
    }

}
