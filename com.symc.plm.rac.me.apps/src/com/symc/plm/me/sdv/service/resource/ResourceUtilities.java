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

import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.SYMDisplayUtil;
import com.ssangyong.common.utils.SYMStringUtil;
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
     * 자리수별 숫자
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
     * ID Validation 하는 함수
     * 
     * @param itemId
     * @param arrIdLength
     * @throws ValidateSDVException
     */
    public static void checkResourceId(String itemId, int[] arrIdLength) throws ValidateSDVException {
        String[] splitItemIds = itemId.split("-");

        // ID 전체 구성 비교
        if (splitItemIds.length != arrIdLength.length) {
            throw ResourceUtilities.checkIdErrorMessage(itemId);
        }

        // ID의 각 자리별 문자열 길이 비교
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
     * Item 및 Revision Create/Revise 함수
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

        // item 이 이미 존재하는지 검색한다.
        TCComponentItem item = SYMTcUtil.findItem(getTCSession(), itemId);
        TCComponentItemRevision itemRevision = null;
        // Revise시 사용할 옵션, 만약 처음 생성시 있으면 오류다.
        if (item != null) {
            if (createMode) {
                throw new Exception(getResourceRegistry().getString("Item.Check.MSG").replace("%0", itemId));
                // MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("Item.Check.MSG").replace("%0", itemId), "WARNING", MessageBox.WARNING);
            }
            // Revision생성
            if (!createMode) {
                itemRevision = SYMTcUtil.getLatestReleasedRevision(item);
                // Revision 생성
                TCComponentItemRevision newItemRevision = SYMTcUtil.createItemRevision(itemRevision, revisionId);
                if (newItemRevision != null) {
                    // Revision 속성 업데이트
                    newItemRevision.setProperties(revisionProperties);
                    // Item 속성 업데이트
                    item.setProperty(SDVPropertyConstant.EQUIP_ENG_NAME, itemEngName);
                    /*
                     * KoreanName은 Item속성이 아닌 Revision속성으로 관리한다. (2014.01.28)
                     * item.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, itemName);
                     */
                    
                    // Revision 속성에 이름 업데이트
                    newItemRevision.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, revisionName);
                    return newItemRevision;
                }
            }

            // Item이 없을경우 Item 생성(등록화면)
        } else {
            TCComponentItemType itemType = (TCComponentItemType) getTCSession().getTypeComponent(itemTCCompType);
            if (itemType == null || itemType.equals("")) {
                // 적합한 Item Class 유형이 없습니다.
                throw new Exception(getResourceRegistry().getString("ItemCompType.Check.MSG"));
            }

            TCComponentItem newItem = SDVBOPUtilities.createItem(itemTCCompType, itemId, revisionId, revisionName, "");
            if (newItem == null) {
                // Item생성에 실패하였습니다
                throw new Exception(getResourceRegistry().getString("ItemCreated.Check.MSG"));
            }

            newItem.setProperty(SDVPropertyConstant.EQUIP_ENG_NAME, itemEngName);
            // 부자재인 경우에만 uom_tag에 EA값을 준다. -> 변경 : 모든 자원에 uom_tag 값을 주지 않는다. 2014.01.08
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

        // PDF(Thumbnail_Source) 가 존재하면 삭제
        deletePDFThumbnail(itemRevision);

        for (String key : fileMap.keySet()) {
            if (key.equalsIgnoreCase("isModified")) {
                continue;
            }

            // 기본 Relation Type 은 IMAN_specification, zip 파일의 경우 IMAN_reference로 변경
            String relationType = getResourceRegistry().getString("Dataset.Relation.TYPE");
            if (key.equalsIgnoreCase("zip")) {
                relationType = "IMAN_reference";
            } else if (key.equalsIgnoreCase("jt")) {
                relationType = "IMAN_Rendering";
            }

            // ItemRevision에 동일 Type의 Dataset이 존재 하는지 확인
            TCComponentDataset existDataset = null;
            Vector vecExistDatasets = DatasetUtilities.getDatasets(itemRevision, relationType, key);
            if (vecExistDatasets != null && vecExistDatasets.size() > 0) {
                existDataset = (TCComponentDataset) vecExistDatasets.get(0);
            }

            String filePath = fileMap.getStringValue(key);
            // 삭제 대상 Dataset 삭제 수행
            if (filePath.length() == 0) {
                if (existDataset != null) {
                    itemRevision.remove(relationType, existDataset);
                    if (!key.equalsIgnoreCase("zip")) {
                        existDataset.delete();
                    }
                }
            }

            // 등록 대상 Dataset 등록
            else {
                File uploadFile = ResourceUtilities.getFile(filePath);

                if (existDataset != null && relationType.equals("IMAN_reference")) {
                    // 동일 Type의 Dataset이 존재하면 namedReference 교체
                    String targetFileName = "";
                    TCComponent[] tcFile = existDataset.getNamedReferences();
                    if (tcFile != null) {
                        targetFileName = tcFile[0].getProperty("original_file_name");
                    }

                    String namedRef = CustomUtil.getNamedRefType(existDataset, "zip");
                    existDataset.replaceFiles(new File[] { uploadFile }, new String[] { (targetFileName.equals("")) ? uploadFile.getName() : targetFileName }, new String[] { "BINARY" }, new String[] { namedRef });
                } else {
                    // 동일 Type의 Dataset이 존재하면 삭제
                    if (existDataset != null) {
                        itemRevision.remove(relationType, existDataset);
                        existDataset.delete();
                    }

                    // Dataset 생성
                    String datasetName = itemRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID) + "/" + itemRevision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
                    TCComponentDataset tcComponentDataset = SDVBOPUtilities.createDataset(uploadFile.getAbsolutePath());

                    // Dataset 이름 변경
                    tcComponentDataset.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, datasetName);

                    // Dataset upload대상 File
                    Vector<File> uploadFiles = new Vector<File>();
                    uploadFiles.add(uploadFile);

                    // DataSet에 파일 첨부
                    SYMTcUtil.importFiles(tcComponentDataset, uploadFiles);

                    // ItemRevision에 Dataset추가
                    itemRevision.add(relationType, tcComponentDataset);
                }
            }
        }
    }

    /*
     * Revision생성시 PDF 파일의 경우 Thumbnail_Source Relation Type의 PDF Dataset이 자동으로 생성된다.
     * 불필요한 이 Dataset을 제거하는 함수임.
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
     * File List를 만들어 Return
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
     * Viewpart의 Table에 선택된 ItemRevision을 Return 하는 함수
     * 
     * @param resourceSearchViewPart
     * @return
     */
    public static TCComponentItemRevision getSelectedItemRevision(ResourceSearchViewPart resourceSearchViewPart) throws Exception {
        TCComponentItemRevision targetItemRevision = null;
        InterfaceAIFComponent[] arrTargetLine = resourceSearchViewPart.getCurrentTable().getSelectedItems();
        // 검색 TcTable의 ItemRevision을 선택 한 경우
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
     * Revise Dialog Open시 사용하기 위한 Map Parameter 생성 (Item Properties 정보)
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
     * Revise Dialog Open시 사용하기 위한 ItemRevision에 첨부된 Dataset 가져오는 함수
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
     * 활성된 ResourceSearchViewPart를 찾아서 return 하는 함수
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
     * 자원의 ID로 자원이 차체, 도장, 조립인지 판별하는 함수
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
     * Create/Revise 서비스 Execute (이 함수에서 View의 Data들을 수집 가공하여 Create Service로 보낸다.)
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

            // 처음 생성 또는 변경사항 발생시 Create 서비스 실행
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
     * ID validation 후 오류시 Error Exception 처리
     * 
     * @param itemId
     * @return
     */
    public static ValidateSDVException checkIdErrorMessage(String itemId) {
        return new ValidateSDVException("[" + itemId + "] " + getResourceRegistry().getString("ItemID.Check.MSG"));
    }
}
