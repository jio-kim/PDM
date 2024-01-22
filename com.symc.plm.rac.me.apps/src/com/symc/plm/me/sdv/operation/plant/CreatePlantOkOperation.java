package com.symc.plm.me.sdv.operation.plant;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.ValidationException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Display;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.view.plant.CreatePlantView;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.Registry;

public class CreatePlantOkOperation extends AbstractSDVActionOperation {

    private boolean isValidOk = true;
    private Registry registry;
    private String itemId;
    private String itemType;
    private String altPrefix = "";

    public CreatePlantOkOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
        registry = Registry.getRegistry(this);
    }

    public CreatePlantOkOperation(String actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
        registry = Registry.getRegistry(this);
    }

    public CreatePlantOkOperation(int actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, ownerId, parameters, dataset);
        registry = Registry.getRegistry(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void startOperation(String commandId) {
        IDataSet dataset = getDataSet();
        if (dataset.containsMap("CreatePlantView")) {
            if (dataset.getDataMap("CreatePlantView") != null) {
                RawDataMap rawDataMap = (RawDataMap) dataset.getDataMap("CreatePlantView");
                LinkedHashMap<String, String> idStrValueMap = (LinkedHashMap<String, String>) rawDataMap.getValue("id");
                LinkedHashMap<String, String> propertyStrValueMap = (LinkedHashMap<String, String>) rawDataMap.getValue("property");

                if (idStrValueMap != null && propertyStrValueMap != null) {
                    Set<String> keys = idStrValueMap.keySet();
                    itemType = getItemRevisionType(keys);
                    itemId = checkItemId(idStrValueMap);

                    // Alt Plant인 경우 itemId 앞에 altPrefix 추가
                    if (!propertyStrValueMap.isEmpty()) {
                        // Is Alt Plant 속성이 있을경우 필수값인 altPrefix 체크 (Station 생성시 ID validation)
                        String isAltPlant = propertyStrValueMap.get(registry.getString("Plant.IsAltPlant.Name", "Is Alt Plant"));
                        altPrefix = propertyStrValueMap.get(registry.getString("Plant.AltPrefix.NAME", "Alt Prefix"));
                        if (isAltPlant != null && isAltPlant.equals("true")) {
                            if (altPrefix == null || altPrefix.length() <= 0) {
                                isValidOk = false;
                            }
                        }

                        // altPrefix가 있을 경우 ItemID 조합 하기 (Station, Workarea 생성시)
                        if (altPrefix != null && altPrefix.length() > 0) {
                            if (altPrefix.length() < 4) {
                                isValidOk = false;
                            } else if (!StringUtils.contains(altPrefix, "ALT")) {
                                isValidOk = false;
                            }

                            itemId = altPrefix + "-" + itemId;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void executeOperation() throws Exception {
        try {
            if (!isValidOk) {
                throw new ValidationException(registry.getString("ItemID.Check.MSG"));
            }

            if (!itemId.equals("") && itemId != null && itemType != null && !itemType.equals("")) {
                createPlant(itemId, itemType, altPrefix);
            }

        } catch (Exception e) {
            this.setExecuteResult(FAIL);
            this.setExecuteError(e);
            this.setErrorMessage("오류가 발생했습니다.");
        }
    }

    @Override
    public void endOperation() {
        // Apply 버튼 클릭시 화면의 선택값 초기화
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                CreatePlantView currentView = (CreatePlantView) UIManager.getCurrentDialog().getView("CreatePlantView");
                if (currentView.getShopCodeCombo() != null) {
                    if (currentView.getShopCodeCombo().getEnabled()) {
                        currentView.setShopCodeComboValue(-1);
                    }
                }

                if (currentView.getLineCodeCombo() != null) {
                    if (currentView.getLineCodeCombo().getEnabled()) {
                        currentView.setLineCodeComboValue(-1);
                    }
                }

                if (currentView.getStationCodeText() != null) {
                    if (currentView.getStationCodeText().getEditable()) {
                        currentView.setStationCodeTextValue("");
                    }
                }

                if (currentView.getWorkareaCodeText() != null) {
                    if (currentView.getWorkareaCodeText().getEditable()) {
                        currentView.setWorkareaCodeTextValue("");
                    }
                }
            }
        });
    }

    /**
     * ID Validation 함수
     * 
     * @param idMap
     */
    public String checkItemId(LinkedHashMap<String, String> idMap) {
        String itemId = "";
        int count = 0;

        for (String key : idMap.keySet()) {
            String idCode = idMap.get(key);
            if (idCode == null || idCode.equals("")) {
                isValidOk = false;
                return null;
            } else {
                itemId += idCode;

                if (count < (idMap.size() - 1)) {
                    itemId += "-";
                }
                count++;
            }
        }
        return itemId;
    }

    /**
     * 생성할 ItemRevision의 Type을 찾아 리턴하는 함수
     * 
     * @param idCodeKeys
     * @return String
     */
    public String getItemRevisionType(Set<String> idCodeKeys) {
        String itemRevType = null;
        if (idCodeKeys.contains(registry.getString("Plant.Workarea.NAME"))) {
            itemRevType = SDVTypeConstant.PLANT_OPAREA_ITEM;
        } else if (idCodeKeys.contains(registry.getString("Plant.StationCode.NAME"))) {
            itemRevType = SDVTypeConstant.PLANT_STATION_ITEM;
        } else if (idCodeKeys.contains(registry.getString("Plant.LineCode.NAME"))) {
            itemRevType = SDVTypeConstant.PLANT_LINE_ITEM;
        } else if (idCodeKeys.contains(registry.getString("Plant.ShopCode.NAME"))) {
            itemRevType = SDVTypeConstant.PLANT_SHOP_ITEM;
        }

        if (itemRevType == null) {
            isValidOk = false;
        }

        return itemRevType;
    }

    /**
     * Plant 생성 함수
     * 
     * @param itemId
     * @param altPrefixProperty
     * @throws Exception
     */
    private void createPlant(String itemId, String itemType, String altPrefixProperty) throws Exception {
        // Shop인 경우 Name에 "공장"추가 (요청사항에 의해)
        String itemName = null;
        if (itemType.equals(SDVTypeConstant.PLANT_SHOP_ITEM)) {
            itemName = itemId + " 공장";
        }

        //현재는 Item이 중복으로 붙는경우가 없도록 무조건 생성해서 붙이도록만 되어있음. (이럴경우 끊어져 떠도는 Item을 생성하려고 할 경우 오류 발생)
        //이를 해결하기 위해서 Item을 찾아서 무조건 붙일 경우 이미 붙어있는것을 또 붙이는 문제가 됨.
        //그러므로 끊어낸 뒤에 떠도는 Item을 찾아서 붙여줄 필요가 있을 경우 아래 소스에 메시지 박스 추가해서 사용자가 판단하도록 반영하면 됨.
        // // Plant Item 존재여부 검색
        // TCComponentItem tcComponentItem = null;
        // TCComponentItem findedItem = SDVBOPUtilities.FindItem(itemId, itemType);
        // if (findedItem != null) {
        // tcComponentItem = findedItem;
        // }
        //
        // // Plant Item 생성
        // if (tcComponentItem == null) {
        // tcComponentItem = SDVBOPUtilities.createItem(itemType, itemId, "000", (itemName != null) ? itemName : "", "");
        // }
        
        // Plant Item 생성
        TCComponentItem tcComponentItem = SDVBOPUtilities.createItem(itemType, itemId, "000", (itemName != null) ? itemName : "", "");
        TCComponentItemRevision newItemRevision = tcComponentItem.getLatestItemRevision();

        // Plant ItemRevision 속성 입력
        newItemRevision.setProperty(SDVPropertyConstant.PLANT_REV_ALT_PREFIX, altPrefixProperty);
        newItemRevision.setLogicalProperty(SDVPropertyConstant.PLANT_REV_IS_ALTBOP, (altPrefixProperty.length() > 0) ? true : false);

        // Shop 생성시 Application View Open
        MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
        if (itemType.equals(SDVTypeConstant.PLANT_SHOP_ITEM)) {
            mfgApp.open(newItemRevision);
        }

        // Shop이 아닌경우
        else {
            // (체크) targetComponent 가 : Mfg0BvrWorkarea가 아니면 오류처리
            InterfaceAIFComponent interfaceAIFComponent = mfgApp.getTargetComponent();
            if (!interfaceAIFComponent.getType().equals("Mfg0BvrWorkarea")) {
                throw new Exception(itemType.substring(3) + " can only be added to the Workarea.");
            }

            // BOMLine에 생성한 ItemRevision 추가
            TCComponentBOMLine[] tcComponentBOMLines = mfgApp.getSelectedBOMLines();
            TCComponentBOMLine targetBOMLine = tcComponentBOMLines[0];
            targetBOMLine.add(null, newItemRevision);
            targetBOMLine.window().save();

            // 붙여넣은 BOMLine 펼치기
            SDVBOPUtilities.executeExpandOneLevel();
        }
    }
}
