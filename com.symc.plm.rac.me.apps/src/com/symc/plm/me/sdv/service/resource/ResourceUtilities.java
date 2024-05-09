package com.symc.plm.me.sdv.service.resource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.common.exception.ExecuteSDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMDisplayUtil;
import com.kgm.common.utils.SYMStringUtil;
import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVProcessUtils;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVText;
import com.symc.plm.me.sdv.service.migration.ImportCoreService;
import com.symc.plm.me.sdv.service.resource.service.create.ResourceCreateService;
import com.symc.plm.me.sdv.viewpart.resource.ResourceSearchViewPart;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PlatformHelper;
import com.teamcenter.rac.util.Registry;

public class ResourceUtilities {

    /**
     * 
     * @method getTCSession
     * @date 2013. 10. 18.
     * @param
     * @return TCSession
     * @exception
     * @throws
     * @see
     */
    public static TCSession getTCSession() {
        return (TCSession) AIFUtility.getSessionManager().getDefaultSession();
    }

    /**
     * @method getResourceRegistry
     * @return Registry
     */
    public static Registry getResourceRegistry() {
        Registry registry = Registry.getRegistry("com.symc.plm.me.sdv.view.resource.resource");
        return registry;
    }

    /**
     * Get Resource Default LOV Name
     * 
     * @return String
     */
    public static String getDefaultLovName(String bopType, String resourceType) {
        String lovName = "M7_" + bopType + "_" + resourceType;
        return lovName;
    }

    /**
     * checkID for Create Sequential ID Number
     * 
     * �ڸ����� ����
     * 
     * @param length1
     *            , length2, length3, length4, length5, length6
     * @return boolean
     */
    public static boolean checkID(HashMap<Integer, SDVText> idMap, int length1, int length2) {
        if (idMap.get(1).getText().length() == length1) {
            if (idMap.get(2).getText().length() == length2) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkID(HashMap<Integer, SDVText> idMap, int length1, int length2, int length3) {
        if (checkID(idMap, length1, length2)) {
            if (idMap.get(3).getText().length() == length3) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkID(HashMap<Integer, SDVText> idMap, int length1, int length2, int length3, int length4) {
        if (checkID(idMap, length1, length2, length3)) {
            if (idMap.get(4).getText().length() == length4) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkID(HashMap<Integer, SDVText> idMap, int length1, int length2, int length3, int length4, int length5) {
        if (checkID(idMap, length1, length2, length3, length4)) {
            if (idMap.get(5).getText().length() == length5) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkID(HashMap<Integer, SDVText> idMap, int length1, int length2, int length3, int length4, int length5, int length6) {
        if (checkID(idMap, length1, length2, length3, length4, length5)) {
            if (idMap.get(6).getText().length() == length6) {
                return true;
            }
        }
        return false;
    }

    /**
     * ID Validation �ϴ� �Լ�
     * 
     * @param itemId
     * @param arrIdLength
     * @throws ValidateSDVException
     */
    public static void checkResourceId(String itemId, int[] arrIdLength) throws ValidateSDVException {
        String[] splitItemIds = itemId.split("-");

        // ID ��ü ���� ��
        if (splitItemIds.length != arrIdLength.length) {
            throw ResourceUtilities.checkIdErrorMessage(itemId);
        }

        // ID�� �� �ڸ��� ���ڿ� ���� ��
        for (int i = 0; i < splitItemIds.length; i++) {
            if (arrIdLength[i] == 0) {
                if (splitItemIds[i].length() > 0) {
                    continue;
                } else {
                    throw ResourceUtilities.checkIdErrorMessage(itemId);
                }
            }

            if (arrIdLength[i] != splitItemIds[i].length()) {
                throw ResourceUtilities.checkIdErrorMessage(itemId);
            }
        }
    }

    /**
     * Item �� Revision Create/Revise �Լ�
     * 
     * @throws Exception
     */
    public static TCComponentItemRevision createItem(Boolean createMode, String itemTCCompType, Map<String, String> itemProperties, Map<String, String> revisionProperties) throws Exception {
        // boolean createFlag = false;

        String itemId = itemProperties.get(SDVPropertyConstant.ITEM_ITEM_ID);
        String itemEngName = itemProperties.get(SDVPropertyConstant.EQUIP_ENG_NAME);
        String revisionName = revisionProperties.get(SDVPropertyConstant.ITEM_OBJECT_NAME);

        String revisionId = revisionProperties.get(SDVPropertyConstant.ITEM_REVISION_ID);
        revisionProperties.remove(SDVPropertyConstant.ITEM_REVISION_ID);

        // item �� �̹� �����ϴ��� �˻��Ѵ�.
        TCComponentItem item = SYMTcUtil.findItem(getTCSession(), itemId);
        TCComponentItemRevision itemRevision = null;
        // Revise�� ����� �ɼ�, ���� ó�� ������ ������ ������.
        if (item != null) {
            if (createMode) {
                throw new Exception(getResourceRegistry().getString("Item.Check.MSG").replace("%0", itemId));
                // MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("Item.Check.MSG").replace("%0", itemId), "WARNING", MessageBox.WARNING);
            }
            // Revision����
            if (!createMode) {
                itemRevision = SYMTcUtil.getLatestReleasedRevision(item);
                // Revision ����
                TCComponentItemRevision newItemRevision = SYMTcUtil.createItemRevision(itemRevision, revisionId);
                if (newItemRevision != null) {
                    // Revision �Ӽ� ������Ʈ
                    newItemRevision.setProperties(revisionProperties);
                    // Item �Ӽ� ������Ʈ
                    item.setProperty(SDVPropertyConstant.EQUIP_ENG_NAME, itemEngName);
                    /*
                     * KoreanName�� Item�Ӽ��� �ƴ� Revision�Ӽ����� �����Ѵ�. (2014.01.28)
                     * item.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, itemName);
                     */
                    
                    // Revision �Ӽ��� �̸� ������Ʈ
                    newItemRevision.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, revisionName);
                    return newItemRevision;
                }
            }

            // Item�� ������� Item ����(���ȭ��)
        } else {
            TCComponentItemType itemType = (TCComponentItemType) getTCSession().getTypeComponent(itemTCCompType);
            if (itemType == null || itemType.equals("")) {
                // ������ Item Class ������ �����ϴ�.
                throw new Exception(getResourceRegistry().getString("ItemCompType.Check.MSG"));
            }

            TCComponentItem newItem = SDVBOPUtilities.createItem(itemTCCompType, itemId, revisionId, revisionName, "");
            if (newItem == null) {
                // Item������ �����Ͽ����ϴ�
                throw new Exception(getResourceRegistry().getString("ItemCreated.Check.MSG"));
            }

            newItem.setProperty(SDVPropertyConstant.EQUIP_ENG_NAME, itemEngName);
            // �������� ��쿡�� uom_tag�� EA���� �ش�. -> ���� : ��� �ڿ��� uom_tag ���� ���� �ʴ´�. 2014.01.08
            // if (itemTCCompType.equals("M7_Subsidiary")) {
            // newItem.setProperty("uom_tag", "EA");
            // }

            itemRevision = newItem.getLatestItemRevision();
            itemRevision.setProperties(revisionProperties);
        }

        // createFlag = true;
        // return createFlag;
        return itemRevision;
    }

    /**
     * Create Dataset And Add to ItemRevision
     * 
     * @param dataset
     *            (IDataSet)
     * @param itemRevision
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    public static void createAndAddDataset(RawDataMap fileMap, TCComponentItemRevision itemRevision) throws Exception {
        if (fileMap == null) {
            throw new Exception("Dataset FileMap : File Null Exception");
        }

        if (itemRevision == null) {
            throw new Exception("ItemRevision : There is no itemrevision to add Dataset.");
        }

        // PDF(Thumbnail_Source) �� �����ϸ� ����
        deletePDFThumbnail(itemRevision);

        for (String key : fileMap.keySet()) {
            if (key.equalsIgnoreCase("isModified")) {
                continue;
            }

            // �⺻ Relation Type �� IMAN_specification, zip ������ ��� IMAN_reference�� ����
            String relationType = getResourceRegistry().getString("Dataset.Relation.TYPE");
            if (key.equalsIgnoreCase("zip")) {
                relationType = "IMAN_reference";
            } else if (key.equalsIgnoreCase("jt")) {
                relationType = "IMAN_Rendering";
            }

            // ItemRevision�� ���� Type�� Dataset�� ���� �ϴ��� Ȯ��
            TCComponentDataset existDataset = null;
            Vector vecExistDatasets = DatasetUtilities.getDatasets(itemRevision, relationType, key);
            if (vecExistDatasets != null && vecExistDatasets.size() > 0) {
                existDataset = (TCComponentDataset) vecExistDatasets.get(0);
            }

            String filePath = fileMap.getStringValue(key);
            // ���� ��� Dataset ���� ����
            if (filePath.length() == 0) {
                if (existDataset != null) {
                    itemRevision.remove(relationType, existDataset);
                    if (!key.equalsIgnoreCase("zip")) {
                        existDataset.delete();
                    }
                }
            }

            // ��� ��� Dataset ���
            else {
                File uploadFile = ResourceUtilities.getFile(filePath);

                if (existDataset != null && relationType.equals("IMAN_reference")) {
                    // ���� Type�� Dataset�� �����ϸ� namedReference ��ü
                    String targetFileName = "";
                    TCComponent[] tcFile = existDataset.getNamedReferences();
                    if (tcFile != null) {
                        targetFileName = tcFile[0].getProperty("original_file_name");
                    }

                    String namedRef = CustomUtil.getNamedRefType(existDataset, "zip");
                    existDataset.replaceFiles(new File[] { uploadFile }, new String[] { (targetFileName.equals("")) ? uploadFile.getName() : targetFileName }, new String[] { "BINARY" }, new String[] { namedRef });
                } else {
                    // ���� Type�� Dataset�� �����ϸ� ����
                    if (existDataset != null) {
                        itemRevision.remove(relationType, existDataset);
                        existDataset.delete();
                    }

                    // Dataset ����
                    String datasetName = itemRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID) + "/" + itemRevision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
                    TCComponentDataset tcComponentDataset = SDVBOPUtilities.createDataset(uploadFile.getAbsolutePath());

                    // Dataset �̸� ����
                    tcComponentDataset.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, datasetName);

                    // Dataset upload��� File
                    Vector<File> uploadFiles = new Vector<File>();
                    uploadFiles.add(uploadFile);

                    // DataSet�� ���� ÷��
                    SYMTcUtil.importFiles(tcComponentDataset, uploadFiles);

                    // ItemRevision�� Dataset�߰�
                    itemRevision.add(relationType, tcComponentDataset);
                }
            }
        }
    }

    /*
     * Revision������ PDF ������ ��� Thumbnail_Source Relation Type�� PDF Dataset�� �ڵ����� �����ȴ�.
     * ���ʿ��� �� Dataset�� �����ϴ� �Լ���.
     */
    @SuppressWarnings("unchecked")
    private static void deletePDFThumbnail(TCComponentItemRevision itemRevision) throws Exception {
        String relationType = "Thumbnail_Source";
        TCComponentDataset pdfDataset = null;
        Vector<TCComponentDataset> vecExistDatasets = DatasetUtilities.getDatasets(itemRevision, relationType, "PDF");
        if (vecExistDatasets != null && vecExistDatasets.size() > 0) {
            pdfDataset = vecExistDatasets.get(0);
        }

        if (pdfDataset != null) {
            itemRevision.remove(relationType, pdfDataset);
            // pdfDataset.delete();
        }
    }

    /**
     * File List�� ����� Return
     * 
     * @param fileList
     * @throws Exception
     * @throws ExecuteSDVException
     */
    public static File getFile(String filePath) throws Exception, ExecuteSDVException {
        File uploadFile = ImportCoreService.getPathFile(filePath);
        if (uploadFile == null) {
            throw new ExecuteSDVException("Not Founded File Exception");
        }

        return uploadFile;
    }

    /**
     * ItemRevision Release
     * 
     * @param registry
     * @param itemRevision
     * @return
     * @throws Exception
     */
    public static TCComponent releaseItemRevision(TCComponentItemRevision itemRevision) throws Exception {
        String prefName = getResourceRegistry().getString("Resource.WorkflowTemplateName.PreferenceName");
        String templateName = getPreferenceValue(prefName);
        if (templateName == null || templateName.length() <= 0) {
            throw new Exception(getResourceRegistry().getString("TemplateName.Check.MSG"));
        }
        TCComponent processComponent = SDVProcessUtils.createProcess(templateName, "", new TCComponent[] { itemRevision });
        return processComponent;
    }

    /**
     * @param prefName
     * @return String
     */
    public static String getPreferenceValue(String prefName) {
        TCPreferenceService prefService = getTCSession().getPreferenceService();
//        String templateName = prefService.getString(TCPreferenceService.TC_preference_site, prefName);
        String templateName = prefService.getStringValueAtLocation(prefName, TCPreferenceLocation.OVERLAY_LOCATION);
        return templateName;
    }

    /**
     * Viewpart�� Table�� ���õ� ItemRevision�� Return �ϴ� �Լ�
     * 
     * @param resourceSearchViewPart
     * @return
     */
    public static TCComponentItemRevision getSelectedItemRevision(ResourceSearchViewPart resourceSearchViewPart) throws Exception {
        TCComponentItemRevision targetItemRevision = null;
        InterfaceAIFComponent[] arrTargetLine = resourceSearchViewPart.getCurrentTable().getSelectedItems();
        // �˻� TcTable�� ItemRevision�� ���� �� ���
        if (arrTargetLine == null) {
            throw new Exception(getResourceRegistry().getString("Select.Check.MSG"));
        }

        if (arrTargetLine.length > 1) {
            throw new Exception(getResourceRegistry().getString("SingleSelect.Check.MSG"));
        }

        InterfaceAIFComponent targetComponent = arrTargetLine[0];
        if (targetComponent instanceof TCComponentItemRevision) {
            targetItemRevision = (TCComponentItemRevision) targetComponent;
            return targetItemRevision;
        }
        return targetItemRevision;
    }

    /**
     * Revise Dialog Open�� ����ϱ� ���� Map Parameter ���� (Item Properties ����)
     * 
     * @param targetItemRevision
     * @return
     * @throws TCException
     */
    public static Map<String, Object> getItemPropMap(TCComponentItemRevision targetItemRevision) throws TCException {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        TCComponentItem targetItem = targetItemRevision.getItem();
        Map<String, String> itemPropertyMap = targetItem.getProperties();
        Map<String, String> revisionPropertyMap = targetItemRevision.getProperties();
        if (itemPropertyMap != null) {
            paramMap.put("itemProperties", itemPropertyMap);
        }
        if (revisionPropertyMap != null) {
            paramMap.put("revisionProperties", revisionPropertyMap);
        }
        return paramMap;
    }

    /**
     * Revise Dialog Open�� ����ϱ� ���� ItemRevision�� ÷�ε� Dataset �������� �Լ�
     * 
     * @param targetItemRevision
     * @return
     * @throws Exception
     * @throws TCException
     */
    @SuppressWarnings("rawtypes")
    public static HashMap<String, TCComponentDataset> getDatasetMap(TCComponentItemRevision targetItemRevision) throws Exception, TCException {
        HashMap<String, TCComponentDataset> datasetMap = new HashMap<String, TCComponentDataset>();
        Vector vecDataset = DatasetUtilities.getDatasets(targetItemRevision, getResourceRegistry().getString("Dataset.Relation.TYPE"), "All");
        setDatasetMap(vecDataset, datasetMap);
        Vector vecDataset2 = DatasetUtilities.getDatasets(targetItemRevision, getResourceRegistry().getString("IMAN_reference"), "All");
        setDatasetMap(vecDataset2, datasetMap);
        Vector vecDataset3 = DatasetUtilities.getDatasets(targetItemRevision, getResourceRegistry().getString("IMAN_Rendering"), "All");
        setDatasetMap(vecDataset3, datasetMap);
        return datasetMap;
    }

    /**
     * @param vecDataset
     * @param datasetMap
     * @throws TCException
     */
    private static void setDatasetMap(Vector<?> vecDataset, HashMap<String, TCComponentDataset> datasetMap) throws TCException {
        for (int i = 0; i < vecDataset.size(); i++) {
            TCComponentDataset tcComponentDataset = ((TCComponentDataset) vecDataset.get(i));
            TCComponentTcFile[] arrTCComponentTcFile = tcComponentDataset.getTcFiles();
            if (arrTCComponentTcFile.length > 0) {
                TCComponentTcFile componentTcFile = arrTCComponentTcFile[0];
                String fileName = componentTcFile.getProperty("original_file_name");
                if (fileName != null && fileName.length() != 0) {
                    datasetMap.put(fileName, tcComponentDataset);
                }
            }
        }
    }

    /**
     * Ȱ���� ResourceSearchViewPart�� ã�Ƽ� return �ϴ� �Լ�
     * 
     * @return ResourceSearchViewPart
     */
    public static ResourceSearchViewPart getResourceSearchViewPart() {
        IViewReference[] arrayOfIViewReference = PlatformHelper.getCurrentPage().getViewReferences();
        ResourceSearchViewPart resourceSearchViewPart = null;
        for (IViewReference viewRerence : arrayOfIViewReference) {
            IViewPart localIViewPart = viewRerence.getView(false);
            if (localIViewPart instanceof ResourceSearchViewPart) {
                resourceSearchViewPart = (ResourceSearchViewPart) localIViewPart;
            }
        }
        return resourceSearchViewPart;
    }

    /**
     * @param revisionId
     * @return
     */
    public static String getNewRevisionId(String revisionId) {
        int intRevisionId = Integer.parseInt(revisionId) + 1;
        String strNewRevisionId = Integer.toString(intRevisionId);
        if (strNewRevisionId.length() < 2)
            strNewRevisionId = "00" + strNewRevisionId;
        if (strNewRevisionId.length() >= 2 && strNewRevisionId.length() < 3)
            strNewRevisionId = "0" + strNewRevisionId;
        return strNewRevisionId;
    }

    /**
     * Set SDVtext "addVerifyListener" to Numeric adn Length
     * 
     * @param sdvText
     * @param length
     */
    public static void setNumeric(final SDVText sdvText, final int length) {
        sdvText.addVerifyKeyListener(new VerifyKeyListener() {
            @Override
            public void verifyKey(VerifyEvent event) {
                if (event.character == SWT.BS || event.character == SWT.TAB) {
                    event.doit = true;
                    return;
                }

                if (sdvText.getText().length() >= length) {
                    event.doit = false;
                    return;
                }

                if (!('0' <= event.character && event.character <= '9')) {
                    event.doit = false;
                    return;
                }
            }
        });
    }

    public static void setSDVTextListener(final SDVText sdvText, boolean tabKey, boolean enterKey, final String length) {
        if (tabKey) {
            setTabKeyListener(sdvText);
        }

        if (enterKey) {
            setEnterKeyListener(sdvText);
        }

        if (length != null && !length.equals(""))
            setTextLength(sdvText, Integer.parseInt(length));
    }

    /**
     * Set SDVtext "addVerifyListener" to Text Lengh
     * 
     * @param sdvText
     * @param length
     */
    public static void setTextLength(final SDVText sdvText, final int length) {
        sdvText.addVerifyKeyListener(new VerifyKeyListener() {
            @Override
            public void verifyKey(VerifyEvent event) {
                if (event.character == SWT.BS || event.character == SWT.TAB) {
                    event.doit = true;
                    return;
                }

                if (sdvText.getText().length() >= length) {
                    event.doit = false;
                    return;
                }
            }
        });
    }

    public static void setTabKeyListener(final SDVText sdvText) {
        sdvText.addTraverseListener(new TraverseListener() {
            @Override
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_TAB_NEXT) {
                    e.doit = true;
                }
            }
        });
    }

    public static void setEnterKeyListener(SDVText sdvText) {
        sdvText.addVerifyKeyListener(new VerifyKeyListener() {
            @Override
            public void verifyKey(VerifyEvent event) {
                if (event.keyCode == 13 || event.keyCode == 16777296) {
                    event.doit = false;
                }
            }
        });
    }

    /**
     * @param composite
     * @param previousText
     * @return SDVText
     */
    public static SDVText createText(Composite composite, FormData formdata, Color color, boolean editable, boolean mandatory, boolean tabKey, boolean enterKey, final String length) {
        SDVText text = new SDVText(composite, SWT.BORDER | SWT.SINGLE);
        text.setLayoutData(formdata);

        if (!editable) {
            text.setEditable(editable);
            text.setBackground(color);
        }

        text.setMandatory(mandatory);

        ResourceUtilities.setSDVTextListener(text, tabKey, enterKey, length);

        return text;
    }

    /**
     * @param mandatory
     * @param combo
     * @return
     */
    public static Control setMandatory(Control control, boolean mandatory) {
        ControlDecoration decoration = null;
        if (mandatory && decoration == null) {
            decoration = SYMDisplayUtil.setRequiredFieldSymbol(control);
        }
        if (decoration == null) {
            return control;
        }
        if (mandatory) {
            decoration.show();
        } else {
            decoration.hide();
        }
        control.redraw();

        return control;
    }

    /**
     * �ڿ��� ID�� �ڿ��� ��ü, ����, �������� �Ǻ��ϴ� �Լ�
     * 
     * @param bopTypeKey
     * @param string
     * @return
     */
    public static String getBOPType(String itemId) {
        String bopTypeKey = itemId.substring(0, 1);
        String bopType = null;

        if (SYMStringUtil.isNumber(itemId)) {
            return bopType = "Subsidiary";
        }

        if (bopTypeKey.equals("B")) {
            bopType = "Body";
        } else if (bopTypeKey.equals("P")) {
            bopType = "Paint";
        } else {
            bopType = "Assy";
        }

        return bopType;
    }

    /**
     * Create/Revise ���� Execute (�� �Լ����� View�� Data���� ���� �����Ͽ� Create Service�� ������.)
     * 
     * @param dataset
     * @param itemDatamap
     * @throws Exception
     * @throws ValidateSDVException
     */
    public static void excuteResourceCreateService(IDataSet dataset) throws Exception, ValidateSDVException {
        String viewId = "";
        Set<String> ids = dataset.getDataMapIDs();

        for (String id : ids) {
            if (id.lastIndexOf(":Create") > 0) {
                viewId = id;
                break;
            } else if (id.lastIndexOf(":Revise") > 0) {
                viewId = id;
                break;
            }
        }

        if ("".equals(viewId)) {
            throw new ValidateSDVException("Not Founded Resource View");
        }

        RawDataMap itemDatamap = (RawDataMap) dataset.getDataMap(viewId);
        RawDataMap fileDatamap = (RawDataMap) dataset.getDataMap("File");

        if (itemDatamap != null && fileDatamap != null) {
            boolean isPropertyModified = false;
            boolean isFileModified = false;
            isPropertyModified = (Boolean) itemDatamap.getValue("isModified");
            isFileModified = (Boolean) fileDatamap.getValue("isModified");

            // ó�� ���� �Ǵ� ������� �߻��� Create ���� ����
            if (isPropertyModified || isFileModified) {
                itemDatamap.put("File", fileDatamap, IData.OBJECT_FIELD);
                TCComponentItem tcComponentItem = ResourceCreateService.createResourceItem(itemDatamap);
                if (tcComponentItem != null) {
                    MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), getResourceRegistry().getString("ItemCreated.Success.MSG"), "SUCCESS", MessageBox.INFORMATION);
                }
            } else {
                throw new ValidateSDVException("No Change.");
            }
        } else {
            throw new ValidateSDVException("Not Founded DataMap(Create or Revise)");
        }
    }

    /**
     * ID validation �� ������ Error Exception ó��
     * 
     * @param itemId
     * @return
     */
    public static ValidateSDVException checkIdErrorMessage(String itemId) {
        return new ValidateSDVException("[" + itemId + "] " + getResourceRegistry().getString("ItemID.Check.MSG"));
    }
}
