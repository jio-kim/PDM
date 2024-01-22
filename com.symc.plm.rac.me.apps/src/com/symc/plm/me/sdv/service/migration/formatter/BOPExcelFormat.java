/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.formatter;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

import com.symc.plm.me.sdv.service.migration.ImportExcelServce;
import com.symc.plm.me.sdv.service.migration.model.tcdata.basic.DatasetData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.basic.ItemData;

/**
 * Class Name : BOPExcelFormat
 * Class Description :
 * 
 * @date 2013. 11. 21.
 * 
 */
public class BOPExcelFormat extends BaseExcelFormat {

    /**
     * @param shell
     * @param tree
     * @param formatFilePath
     */
    public BOPExcelFormat(Shell shell, Tree tree, String formatFilePath) {
        super(shell, tree, formatFilePath);
    }

    public final static int ROW_START_COLUMN_INDEX = 0;
    public final static int ROW_ITEM_CLASS_TYPE_INDEX = 0;
    public final static int ROW_TYPE_INDEX = 2;
    public final static int ROW_ATTR_ID_INDEX = 3;
    public final static int ROW_DESC_INDEX = 4;
    public final static int COLUMN_DESC_INDEX = 2;
    public final static int ROW_START_ITEM_INDEX = 5;

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.formatter.BaseExcelFormat#createTreeColumn(org.apache.poi.ss.usermodel.Workbook)
     */
    @Override
    protected void createTreeColumn(Workbook workbook) throws Exception {
        Sheet sheet = workbook.getSheetAt(0);
        Row rowColumnDesc = sheet.getRow(ROW_DESC_INDEX);
        Row rowColumnId = sheet.getRow(ROW_ATTR_ID_INDEX);
        LinkedHashMap<String, HashMap<String, String>> columnInfos = new LinkedHashMap<String, HashMap<String, String>>();
        for (int i = COLUMN_DESC_INDEX; i < rowColumnDesc.getLastCellNum(); i++) {
            HashMap<String, String> columnDisplayInfo = new HashMap<String, String>();
            columnDisplayInfo.put("ID", ImportExcelServce.getCellText(rowColumnId.getCell(i)));
            columnDisplayInfo.put("NAME", ImportExcelServce.getCellText(rowColumnDesc.getCell(i)));
            // DISPLAY 출력 정보에 컬럼정보가 있으면 추가.
            if (displayOrderData.containsKey(columnDisplayInfo.get("ID"))) {
                columnInfos.put(columnDisplayInfo.get("ID"), columnDisplayInfo);
            }
        }
        creatColumn(tree, columnInfos, displayOrderData);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.formatter.BaseExcelFormat#createTreeItems(org.apache.poi.ss.usermodel.Workbook)
     */
    @Override
    protected void createTreeItems(Workbook workbook) throws Exception {
        createNode(tree);

    }
   

    /**
     * 현재 임시...
     * 
     * @method createNode
     * @date 2013. 11. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    protected void createNode(final Tree tree) {
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                ItemData itemData = new ItemData(tree, 0, "Item", tree.getColumns());
                itemData.setText(new String[] { "A", "Item" });
                for (int i = 0; i < 1000; i++) {
                    DatasetData datasetData = new DatasetData(itemData, i, "Dataset", tree.getColumns());
                    datasetData.setText(new String[] { "A-" + (i + 1), "Dataset" });
                }
            }
        });
    }
}
