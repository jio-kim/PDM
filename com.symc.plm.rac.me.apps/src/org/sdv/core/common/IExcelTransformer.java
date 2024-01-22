package org.sdv.core.common;

import java.io.File;

import org.sdv.core.common.data.IDataSet;


public interface IExcelTransformer {

    public File getTemplateFile();

    public File getTemplateFile(int mode, String preferenceName, String defaultFileName);

    public void print(int mode, String templatePreference, String defaultFileName, IDataSet dataSet) throws Exception;

    public void openFile();

}
