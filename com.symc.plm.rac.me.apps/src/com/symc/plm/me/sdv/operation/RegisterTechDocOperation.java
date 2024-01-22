package com.symc.plm.me.sdv.operation;

import java.io.File;
import java.util.Map;
import java.util.Vector;

import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

@SuppressWarnings("unused")
public class RegisterTechDocOperation extends AbstractSDVActionOperation {

    private IDataSet dataSet = null;
    private TCComponentItemRevision createdItem = null;
    private TCComponentDataset tcDataSet = null;
    private static String DEFAULT_REV_ID = "A";
    private static String strItemType = "DataSet";
    private static String itemId = "";
    private static String itemType = "";
    private static String dataSetType = "";

    public RegisterTechDocOperation(int actionId, String ownerId, IDataSet dataset) {
        super(actionId, ownerId, dataset);
    }
    
    

    public RegisterTechDocOperation(int actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, ownerId, parameters, dataset);
    }



    @Override
    public void startOperation(String commandId) {

    }

    @Override
    public void endOperation() {

    }

    @Override
    public void executeOperation() throws Exception {
        createDataSet();

    }

    private void createDataSet() throws Exception {

        dataSet = getDataSet();

        TCComponentItemRevision component = null;
        // TCComponentItemRevision component = (TCComponentItemRevision) AIFUtility.getCurrentApplication().getTargetComponent();
        InterfaceAIFComponent comp = AIFUtility.getCurrentApplication().getTargetComponent();
        if (comp instanceof TCComponentItemRevision) {
            component = (TCComponentItemRevision) comp;
        } else if (comp instanceof TCComponentBOPLine) {
            component = ((TCComponentBOPLine) comp).getItemRevision();
        }
        
        
        if(component == null){
            MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Please Select Item Revsion.", "Warning", MessageBox.WARNING);
        }else{

            itemType = component.getType();
            itemId = component.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);

            String docType = dataSet.getStringValue("registerTechDocView", SDVPropertyConstant.M7_TECH_DOC_TYPE);
            String ipClass = dataSet.getStringValue("registerTechDocView", SDVPropertyConstant.IP_CLASSIFICATION);
            String desc = dataSet.getStringValue("registerTechDocView", SDVPropertyConstant.ITEM_OBJECT_DESC);

            File file = null;

            String filePath = dataSet.getStringValue("registerTechDocView", "filePath");

            String itemName = docType + "-" + itemId;

            if (filePath != null) {

                String[] splitData = filePath.split("/");

                for (String path : splitData) {

                    file = new File(path);

                    // Object Type
                    createDataset(path);

                    // DataSet »ý¼º & File Import
                    Vector<File> importFiles = new Vector<File>();
                    importFiles.add(file);
                    // tcDataSet = SYMTcUtil.makeDataSet((TCSession) AIFUtility.getDefaultSession(), component, dataSetType, itemName, importFiles);
                    tcDataSet = SYMTcUtil.createPasteDataset((TCSession) AIFUtility.getDefaultSession(), component, itemName, dataSetType, "IMAN_reference");
                    SYMTcUtil.importFiles(tcDataSet, importFiles);

                    // setProperties
                    tcDataSet.setProperty(SDVPropertyConstant.IP_CLASSIFICATION, ipClass);
                    tcDataSet.setProperty(SDVPropertyConstant.ITEM_OBJECT_DESC, desc);

                }
            }
        }
        return;

    }

    public static TCComponentDataset createDataset(String path) throws Exception {
        TCComponentDataset dataset = null;
        File file = new File(path);
        if (file != null) {
            String extension = getExtension(file);
            if (extension != null && !extension.equals("")) {
                if (extension.equals("xls")) {
                    dataSetType = "MSExcel";
                } else if (extension.equals("xlsx")) {
                    dataSetType = "MSExcelX";
                } else if (extension.equals("doc")) {
                    dataSetType = "MSWord";
                } else if (extension.equals("docx")) {
                    dataSetType = "MSWordX";
                } else if (extension.equals("ppt")) {
                    dataSetType = "MSPowerPoint";
                } else if (extension.equals("pptx")) {
                    dataSetType = "MSPowerPointX";
                } else if (extension.equals("txt")) {
                    dataSetType = "Text";
                } else if (extension.equals("pdf")) {
                    dataSetType = "PDF";
                } else if (extension.equals("jpg")) {
                    dataSetType = "JPEG";
                } else if (extension.equals("gif")) {
                    dataSetType = "GIF";
                } else if (extension.equals("jpeg") || extension.equals("png") || extension.equals("tif") || extension.equals("tiff") || extension.equals("bmp")) {
                    dataSetType = "Image";
                } else if (extension.equals("dwg")) {
                    dataSetType = "DWG";
                } else if (extension.equals("zip")) {
                    dataSetType = "Zip";
                } else if (extension.equals("htm") || extension.equals("html")) {
                    dataSetType = "HTML";
                } else if (extension.equals("eml")) {
                    dataSetType = "EML";
                } else {
                    dataSetType = "MISC";
                }
            }
        }
        return dataset;
    }

    private static String getExtension(File file) throws Exception {
        if (file.isDirectory())
            return null;
        String filename = file.getName();
        int i = filename.lastIndexOf(".");
        if (i > 0 && i < filename.length() - 1) {
            return filename.substring(i + 1).toLowerCase();
        }
        return null;
    }

}
