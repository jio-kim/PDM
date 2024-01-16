package com.ssangyong.commands.partmaster;

import java.util.HashMap;

import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;

/**
 * [SR140702-057][20140617] shcho Class 신규 생성. (M-Product 자동 생성 기능 추가 : E-BOM에 New Product가 생성되면 자동으로  M-Product 생성 (F605/M605 용접Function포함))
 * [SR140724-013][20140725] shcho, Product와 M-Product Function Sync 기능 추가에 따른 BOMView Revision 권한관련 속성업데이트 기능 추가
 * 
 */
public class MProductOperation {

    public static final String MPROD_ITEM_TYPE = "M7_MfgProduct";
    public static final String MPROD_ITEM_REVISION_TYPE = "M7_MfgProductRevision";
    public static final String SFUNC_ITEM_TYPE = SYMCClass.S7_FNCPARTTYPE;
    public static final String SFMP_ITEM_TYPE = SYMCClass.S7_FNCMASTPARTTYPE;

    /** EBOM Product Item */
    private TCComponentItem s7ProductComp;
    
    private String s7ProductID;
    private String s7ProductRev;
    private String s7ProductName;
    private String s7ProductDesc;
    private String s7ProductUOM;
    private String s7ProjectCode;
    private TCComponentBOMWindow bomWindow;
    private TCComponentBOMLine funcBOMLine;

    public MProductOperation(TCComponentItem s7ProductComp) {
        this.s7ProductComp = s7ProductComp;
    }

    /**
     * M-Product 생성
     * 
     * @throws Exception
     */
    public TCComponentItem createMproduct() throws Exception {
        s7ProductID = s7ProductComp.getProperty(Constants.ATTR_NAME_ITEMID);
        s7ProductRev = s7ProductComp.getLatestItemRevision().getProperty("item_revision_id");
        s7ProductName = s7ProductComp.getProperty(Constants.ATTR_NAME_ITEMNAME);
        s7ProductDesc = s7ProductComp.getProperty(Constants.ATTR_NAME_ITEMDESC);
        s7ProductUOM = (String) s7ProductComp.getProperty("uom_tag");

        // M-Product Create
        TCComponentItem m7ProductComp = createItem(MPROD_ITEM_TYPE, "M".concat(s7ProductID.substring(1)), s7ProductRev, s7ProductName, s7ProductDesc, s7ProductUOM);

        if (m7ProductComp == null) {
            throw new Exception("M-Product (" + "M".concat(s7ProductID.substring(1)) + ") does not exist.");
        }


        // 속성 Update
        HashMap<String, Object> attrMap = new HashMap<String, Object>();
        String s7VehicleNo = getVehicleNo(s7ProductComp.getLatestItemRevision().getProperty("s7_PROJECT_CODE"));
        
        attrMap.put("m7_VEHICLE_CODE", s7VehicleNo);
        attrMap.put("m7_PRODUCT_CODE", s7ProductID);
        attrMap.put("s7_MATURITY", s7ProductComp.getLatestItemRevision().getProperty("s7_MATURITY"));

        setProperties(m7ProductComp.getLatestItemRevision(), attrMap);
        
        // BOMLine Add
        bomWindow = getBOMWindow(m7ProductComp.getLatestItemRevision(), "Latest Working For ME", "bom_view");
        
        return m7ProductComp;
    }

    /**
     * Function 생성
     * 
     * @throws Exception
     */
    public TCComponentItem createFunction() throws Exception {
        s7ProjectCode = s7ProductComp.getLatestItemRevision().getProperty("s7_PROJECT_CODE");
        String s7FuncName = "ME WELDING POINT-" + s7ProjectCode + " " + s7ProductID.substring(s7ProductID.length() - 4) + " M/Y";

        // Function Create
        TCComponentItem s7FunctionComp = createItem(SFUNC_ITEM_TYPE, "F605".concat(s7ProductID.substring(2)), "000", s7FuncName, "", s7ProductUOM);

        if (s7FunctionComp == null) {
            throw new Exception("s7_Function (" + "F605".concat(s7ProductID.substring(2)) + ") does not exist.");
        }

        // 속성 Update
        HashMap<String, Object> attrMap = new HashMap<String, Object>();
        attrMap.put("s7_MATURITY", s7ProductComp.getLatestItemRevision().getProperty("s7_MATURITY"));
        attrMap.put("s7_PROJECT_CODE", s7ProjectCode);

        setProperties(s7FunctionComp.getLatestItemRevision(), attrMap);
        
        // BOMLine Add
        if(bomWindow == null) {
            throw new Exception("M-Product BOM (F605 Function) can not be configured.");
        }
        
        TCComponentBOMLine mProductBOMLine = bomWindow.getTopBOMLine();
        funcBOMLine = mProductBOMLine.add(null, s7FunctionComp.getLatestItemRevision(), null, false);
        
        //[SR140724-013][20140725] shcho, Product와 M-Product Function Sync 기능 추가에 따른 BOMView Revision 권한관련 속성업데이트 기능 추가
        //BOMView Revision 속성 Update
        if(funcBOMLine != null) {
            updateBOMViewRevisionProperty(mProductBOMLine);
        }
        
        return s7FunctionComp;
    }

    /**
     * FMP 생성
     * 
     * @throws
     */
    public TCComponentItem createFMP() throws Exception {
        String s7FMPName = "ME WELDING POINT-" + s7ProjectCode + " " +s7ProductID.substring(s7ProductID.length() - 4) + " M/Y";

        // FMP Create
        TCComponentItem s7FMPComp = createItem(SFMP_ITEM_TYPE, "M605".concat(s7ProductID.substring(2))+"A", "000", s7FMPName, "", s7ProductUOM);

        if (s7FMPComp == null) {
            throw new Exception("s7_Function (" + "M605".concat(s7ProductID.substring(2)) + "A) does not exist.");
        }

        // 속성 Update
        HashMap<String, Object> attrMap = new HashMap<String, Object>();
        attrMap.put("s7_MATURITY", s7ProductComp.getLatestItemRevision().getProperty("s7_MATURITY"));
        attrMap.put("s7_PROJECT_CODE", s7ProjectCode);

        setProperties(s7FMPComp.getLatestItemRevision(), attrMap);
        
        // BOMLine Add
        if(funcBOMLine == null) {
            throw new Exception("M-Product BOM (M605 FMP) can not be configured.");
        }
        
        TCComponentBOMLine fmpBOMLine = funcBOMLine.add(null, s7FMPComp.getLatestItemRevision(), null, false);
        
        //[SR140724-013][20140725] shcho, Product와 M-Product Function Sync 기능 추가에 따른 BOMView Revision 권한관련 속성업데이트 기능 추가
        //BOMView Revision 속성 Update
        if(fmpBOMLine != null) {
            updateBOMViewRevisionProperty(funcBOMLine);
        }
        
        //윈도우 저장
        bomWindow.save();
        bomWindow.close();
        
        return s7FMPComp;
    }

    /**
     * Item 생성 함수
     * 
     * @param itemID
     * @param itemRev
     * @param itemName
     * @param itemDesc
     * @param itemUOM
     * @throws TCException
     */
    public TCComponentItem createItem(String compType, String itemID, String itemRev, String itemName, String itemDesc, String itemUOM) throws TCException {
        TCComponentItem itemComp = CustomUtil.createItem(compType, itemID, itemRev, itemName, itemDesc);

        // Unit(uom_tag)은 Item 속성
        if (!CustomUtil.isEmpty(itemUOM))
            itemComp.setProperty("uom_tag", itemUOM);

        // 권한 관리를 위해 ip_classification 값 설정
        if (itemComp.getTypeComponent().getType().equals(MPROD_ITEM_TYPE) || itemComp.getTypeComponent().getType().equals(SYMCClass.S7_FNCPARTTYPE)) {
            itemComp.setProperty("ip_classification", "top-secret");
            itemComp.getLatestItemRevision().setProperty("ip_classification", "top-secret");
        }

        return itemComp;
    }

    /**
     * Item 생성 후 Revision에 속성값 저장
     * 
     * @param attrMap (속성 Map)
     * @param tcComponentItemRevision
     */
    public void setProperties(TCComponentItemRevision tcComponentItemRevision, HashMap<String, Object> attrMap) throws Exception {
        String[] szKey = attrMap.keySet().toArray(new String[attrMap.size()]);
        TCProperty[] props = tcComponentItemRevision.getTCProperties(szKey);

        for (int i = 0; i < props.length; i++) {

            if (props[i] == null) {
                System.out.println(szKey[i] + " is Null");
                continue;
            }

            Object value = attrMap.get(props[i].getPropertyName());

            CustomUtil.setObjectToPropertyValue(props[i], value);
        }

        // 속성 일괄 반영
        tcComponentItemRevision.setTCProperties(props);
        tcComponentItemRevision.refresh();
    }

    
    /**
     * Project Code로 Vehicle No 찾아오는 함수
     * 
     * @param s7ProjectCode
     * @return
     * @throws TCException
     */
    public String getVehicleNo(String s7ProjectCode) throws TCException {
        TCComponentItem s7ProjectComp = CustomUtil.findItem("S7_PROJECT", s7ProjectCode);

        if (s7ProjectComp == null) {
            throw new TCException("S7_PROJECT Item could not be found.");
        }

        String s7VehicleNo = s7ProjectComp.getLatestItemRevision().getProperty("s7_VEHICLE_NO");

        if (s7VehicleNo == "" || s7VehicleNo == null) {
            throw new TCException("The value of the 'Vehicle No.' property can not be found from '" + s7ProjectComp.getLatestItemRevision().toDisplayString() + "' ItemRevision. \n" + "Please set the value of 'Vehicle No.'property first.");
        }

        return s7VehicleNo;
    }

    
    /**
     * BOM Window 를 가져옴
     * 
     * @param itemRevision
     *            아이템 리비전
     * @return
     * @throws Exception
     */
    public static TCComponentBOMWindow getBOMWindow(TCComponentItemRevision itemRevision, String ruleName, String viewType) throws Exception {        
        TCComponentBOMWindow bomWindow = null;
        TCSession session = (TCSession)AIFUtility.getCurrentApplication().getSession();
        TCComponentBOMViewRevision viewRevision = getBOMViewRevision(itemRevision, viewType);
        // 리비전 룰을 가져옴
        TCComponentRevisionRule revRule = CustomUtil.getRevisionRule(session, ruleName);
        // BOMWindow를 생성
        TCComponentBOMWindowType windowType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
        bomWindow = windowType.create(revRule);
        bomWindow.setWindowTopLine(itemRevision.getItem(), itemRevision, null, viewRevision);
        
        return bomWindow;
    }
    
    
    
    /**
     * 리비전 하위에 view와 타입이 일치하는 BOMViewRevision 검색하여 반환한다.
     * [SR140724-013][20140725] shcho, Product와 M-Product Function Sync 기능 추가에 따른 BOMView Revision 권한관련 속성업데이트 기능 추가
     * 
     * @param revision
     *            ItemRevision TCComponent
     * @param viewType
     *            뷰타입 String
     * @return bomViewRevision TCComponentBOMViewRevision
     * @throws TCException
     */
    public static TCComponentBOMViewRevision getBOMViewRevision(TCComponent comp, String viewType) throws Exception {
        comp.refresh();
        
        //Component 타입이 TCComponentBOMLine인 경우에는 getRelatedComponents를 가져오기 위해서 TCComponentItemRevision 으로 변경한다.
        if(comp.getType().equals("BOMLine")) {
            comp = ((TCComponentBOMLine) comp).getItemRevision();
        }
        
        TCComponent[] arrayStructureRevision = comp.getRelatedComponents("structure_revisions");
        for (TCComponent bvr : arrayStructureRevision) {
            TCComponentBOMViewRevision bomViewRevision = (TCComponentBOMViewRevision) bvr;
            // bomViewRevision.getReferenceProperty("bom_view").getProperty("view_type") 결과가 "View" 이고 파라미터로 넘어온 viewType은 "view" 
            if (bomViewRevision.getReferenceProperty("bom_view").getProperty("view_type").equalsIgnoreCase(viewType)) {
                return bomViewRevision;
            }
        }

        return null;
    }
    

    /**
     * BOMView Revision 속성 Update (BOPADMIN에게 권한 부여를 위한 ip_classification 속성에 top-secret값 등록)
     * [SR140724-013][20140725] shcho, Product와 M-Product Function Sync 기능 추가에 따른 BOMView Revision 권한관련 속성업데이트 기능 추가
     * 
     * @param 
     * @throws Exception
     * @throws TCException
     */
    public void updateBOMViewRevisionProperty(TCComponent component) throws Exception, TCException {
        //BOMView Revision 속성 Update
        TCComponentBOMViewRevision bomViewRevision = getBOMViewRevision(component, "view");
        bomViewRevision.setProperty("ip_classification", "top-secret");
    }
}
