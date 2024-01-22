package com.symc.plm.me.sdv.excel.transformer;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.sdv.core.common.IExcelTransformer;

import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.teamcenter.rac.aif.AIFShell;

public abstract class AbstractExcelTransformer implements IExcelTransformer {

    protected File templateFile;

    @Override
    public File getTemplateFile(int mode, String preferenceName, String defaultFileName) {

        return ExcelTemplateHelper.getTemplateFile(mode, preferenceName, defaultFileName);
    }

    @Override
    public void openFile() {
        try {
//            Desktop.getDesktop().open(templateFile);
            AIFShell aif = new AIFShell("application/vnd.ms-excel", templateFile.getAbsolutePath());
        	aif.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getTemplateFile() {
        return templateFile;
    }

}
