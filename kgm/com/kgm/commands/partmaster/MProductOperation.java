package com.kgm.commands.partmaster;

import java.util.HashMap;

import com.kgm.common.SYMCClass;
import com.kgm.common.utils.CustomUtil;
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
 * [SR140702-057][20140617] shcho Class �ű� ����. (M-Product �ڵ� ���� ��� �߰� : E-BOM�� New Product�� �����Ǹ� �ڵ�����  M-Product ���� (F605/M605 ����Function����))
 * [SR140724-013][20140725] shcho, Product�� M-Product Function Sync ��� �߰��� ���� BOMView Revision ���Ѱ��� �Ӽ�������Ʈ ��� �߰�
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
     * M-Product ����
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


        // �Ӽ� Update
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
     * Function ����
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

        // �Ӽ� Update
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
        
        //[SR140724-013][20140725] shcho, Product�� M-Product Function Sync ��� �߰��� ���� BOMView Revision ���Ѱ��� �Ӽ�������Ʈ ��� �߰�
        //BOMView Revision �Ӽ� Update
        if(funcBOMLine != null) {
            updateBOMViewRevisionProperty(mProductBOMLine);
        }
        
        return s7FunctionComp;
    }

    /**
     * FMP ����
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

        // �Ӽ� Update
        HashMap<String, Object> attrMap = new HashMap<String, Object>();
        attrMap.put("s7_MATURITY", s7ProductComp.getLatestItemRevision().getProperty("s7_MATURITY"));
        attrMap.put("s7_PROJECT_CODE", s7ProjectCode);

        setProperties(s7FMPComp.getLatestItemRevision(), attrMap);
        
        // BOMLine Add
        if(funcBOMLine == null) {
            throw new Exception("M-Product BOM (M605 FMP) can not be configured.");
        }
        
        TCComponentBOMLine fmpBOMLine = funcBOMLine.add(null, s7FMPComp.getLatestItemRevision(), null, false);
        
        //[SR140724-013][20140725] shcho, Product�� M-Product Function Sync ��� �߰��� ���� BOMView Revision ���Ѱ��� �Ӽ�������Ʈ ��� �߰�
        //BOMView Revision �Ӽ� Update
        if(fmpBOMLine != null) {
            updateBOMViewRevisionProperty(funcBOMLine);
        }
        
        //������ ����
        bomWindow.save();
        bomWindow.close();
        
        return s7FMPComp;
    }

    /**
     * Item ���� �Լ�
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

        // Unit(uom_tag)�� Item �Ӽ�
        if (!CustomUtil.isEmpty(itemUOM))
            itemComp.setProperty("uom_tag", itemUOM);

        // ���� ������ ���� ip_classification �� ����
        if (itemComp.getTypeComponent().getType().equals(MPROD_ITEM_TYPE) || itemComp.getTypeComponent().getType().equals(SYMCClass.S7_FNCPARTTYPE)) {
            itemComp.setProperty("ip_classification", "top-secret");
            itemComp.getLatestItemRevision().setProperty("ip_classification", "top-secret");
        }

        return itemComp;
    }

    /**
     * Item ���� �� Revision�� �Ӽ��� ����
     * 
     * @param attrMap (�Ӽ� Map)
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

        // �Ӽ� �ϰ� �ݿ�
        tcComponentItemRevision.setTCProperties(props);
        tcComponentItemRevision.refresh();
    }

    
    /**
     * Project Code�� Vehicle No ã�ƿ��� �Լ�
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
     * BOM Window �� ������
     * 
     * @param itemRevision
     *            ������ ������
     * @return
     * @throws Exception
     */
    public static TCComponentBOMWindow getBOMWindow(TCComponentItemRevision itemRevision, String ruleName, String viewType) throws Exception {        
        TCComponentBOMWindow bomWindow = null;
        TCSession session = (TCSession)AIFUtility.getCurrentApplication().getSession();
        TCComponentBOMViewRevision viewRevision = getBOMViewRevision(itemRevision, viewType);
        // ������ ���� ������
        TCComponentRevisionRule revRule = CustomUtil.getRevisionRule(session, ruleName);
        // BOMWindow�� ����
        TCComponentBOMWindowType windowType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
        bomWindow = windowType.create(revRule);
        bomWindow.setWindowTopLine(itemRevision.getItem(), itemRevision, null, viewRevision);
        
        return bomWindow;
    }
    
    
    
    /**
     * ������ ������ view�� Ÿ���� ��ġ�ϴ� BOMViewRevision �˻��Ͽ� ��ȯ�Ѵ�.
     * [SR140724-013][20140725] shcho, Product�� M-Product Function Sync ��� �߰��� ���� BOMView Revision ���Ѱ��� �Ӽ�������Ʈ ��� �߰�
     * 
     * @param revision
     *            ItemRevision TCComponent
     * @param viewType
     *            ��Ÿ�� String
     * @return bomViewRevision TCComponentBOMViewRevision
     * @throws TCException
     */
    public static TCComponentBOMViewRevision getBOMViewRevision(TCComponent comp, String viewType) throws Exception {
        comp.refresh();
        
        //Component Ÿ���� TCComponentBOMLine�� ��쿡�� getRelatedComponents�� �������� ���ؼ� TCComponentItemRevision ���� �����Ѵ�.
        if(comp.getType().equals("BOMLine")) {
            comp = ((TCComponentBOMLine) comp).getItemRevision();
        }
        
        TCComponent[] arrayStructureRevision = comp.getRelatedComponents("structure_revisions");
        for (TCComponent bvr : arrayStructureRevision) {
            TCComponentBOMViewRevision bomViewRevision = (TCComponentBOMViewRevision) bvr;
            // bomViewRevision.getReferenceProperty("bom_view").getProperty("view_type") ����� "View" �̰� �Ķ���ͷ� �Ѿ�� viewType�� "view" 
            if (bomViewRevision.getReferenceProperty("bom_view").getProperty("view_type").equalsIgnoreCase(viewType)) {
                return bomViewRevision;
            }
        }

        return null;
    }
    

    /**
     * BOMView Revision �Ӽ� Update (BOPADMIN���� ���� �ο��� ���� ip_classification �Ӽ��� top-secret�� ���)
     * [SR140724-013][20140725] shcho, Product�� M-Product Function Sync ��� �߰��� ���� BOMView Revision ���Ѱ��� �Ӽ�������Ʈ ��� �߰�
     * 
     * @param 
     * @throws Exception
     * @throws TCException
     */
    public void updateBOMViewRevisionProperty(TCComponent component) throws Exception, TCException {
        //BOMView Revision �Ӽ� Update
        TCComponentBOMViewRevision bomViewRevision = getBOMViewRevision(component, "view");
        bomViewRevision.setProperty("ip_classification", "top-secret");
    }
}
