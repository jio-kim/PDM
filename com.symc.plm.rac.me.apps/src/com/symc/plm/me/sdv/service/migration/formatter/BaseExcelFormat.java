/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.formatter;

import org.apache.poi.ss.usermodel.Workbook;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

import com.symc.plm.me.sdv.service.migration.ImportExcelServce;

/**
 * Class Name : BOPExcelFormat
 * Class Description :
 * 
 * @date 2013. 11. 21.
 * 
 */
public abstract class BaseExcelFormat extends BaseFormat {

    /**
     * @param shell
     * @param tree
     * @param formatFilePath
     */
    public BaseExcelFormat(Shell shell, Tree tree, String formatFilePath) {
        super(shell, tree, formatFilePath);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.formatter.IImportFormat#getImportTree()
     */
    @Override
    public Tree getImportTree() throws Exception {
        Workbook workbook = ImportExcelServce.getWorkBook(this.formatFilePath);
        createTreeColumn(workbook);
        createTreeItems(workbook);
        return tree;
    }

    /**
     * Excel 파일의 정보를 읽어 Tree Columnn 설정
     * 
     * @method createTreeColumn
     * @date 2013. 11. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    protected abstract void createTreeColumn(Workbook workbook) throws Exception;

    /**
     * Excel 파일의 정보를읽어 Tree Item 설정
     * 
     * @method createTreeItems
     * @date 2013. 11. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    protected abstract void createTreeItems(Workbook workbook) throws Exception;
}
