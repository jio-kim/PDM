package com.symc.plm.rac.prebom.ccn.excel.transformer;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.sdv.core.common.IExcelTransformer;

//import org.sdv.core.common.IExcelTransformer;

import com.symc.plm.rac.prebom.ccn.excel.common.ExcelTemplateHelper;

/**
 * APPS 에서 사용했던 소스
 * @author jwlee
 *
 */
public abstract class AbstractExcelTransformer implements IExcelTransformer {

    protected File templateFile;

    @Override
    public File getTemplateFile(int mode, String preferenceName, String defaultFileName) {

        return ExcelTemplateHelper.getTemplateFile(mode, preferenceName, defaultFileName);
    }

    @Override
    public void openFile() {
        try {
            Desktop.getDesktop().open(templateFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getTemplateFile() {
        return templateFile;
    }

}
