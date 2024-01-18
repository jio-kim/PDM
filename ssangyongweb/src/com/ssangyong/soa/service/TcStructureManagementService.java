package com.ssangyong.soa.service;

import com.ssangyong.soa.biz.Session;
import com.ssangyong.soa.util.TcConstants;
import com.ssangyong.soa.util.TcUtil;
import com.teamcenter.services.strong.cad.StructureManagementService;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.CloseBOMWindowsResponse;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.CreateBOMWindowsInfo;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.CreateBOMWindowsResponse;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.SaveBOMWindowsResponse;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.ItemRevision;

/**
 * 
 * Desc :
 * @author yunjae.jung
 */
public class TcStructureManagementService {

    private Session tcSession = null;
    private TcServiceManager tcServiceManager;
    
    public TcStructureManagementService(Session tcSession) {
        this.tcSession = tcSession;
        tcServiceManager = new TcServiceManager(tcSession);
    }
    
    @SuppressWarnings("static-access")
    public StructureManagementService getService() {
        return StructureManagementService.getService(tcSession.getConnection());
    }
    
    /**
     * Create TOPLine BOMWindow
     * 
     * @method createTopLineBOMLine 
     * @date 2013. 5. 14.
     * @param
     * @return CreateBOMWindowsResponse
     * @exception
     * @throws
     * @see
     */
    public CreateBOMWindowsResponse createTopLineBOMWindow(ItemRevision  parentItemRev) throws Exception {        
        CreateBOMWindowsInfo[] createBOMWindowsInfo = populateBOMWindowInfo(parentItemRev);
        CreateBOMWindowsResponse createBOMWindowsResponse = getService().createBOMWindows( createBOMWindowsInfo );
        if (tcServiceManager.getDataService().ServiceDataError(createBOMWindowsResponse.serviceData)) {
            throw new Exception(TcUtil.makeMessageOfFail(createBOMWindowsResponse.serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
        }
        if(createBOMWindowsResponse.output != null && createBOMWindowsResponse.output.length > 0) {            
            return createBOMWindowsResponse;
        } else {
            return null;
        }
    }
    
    /**
     * Populate BOMWindow Information
     */
    public static CreateBOMWindowsInfo[] populateBOMWindowInfo( ItemRevision itemRev )
    {
        CreateBOMWindowsInfo[] bomInfo = new CreateBOMWindowsInfo[1];
        bomInfo[0] = new CreateBOMWindowsInfo();
        bomInfo[0].itemRev = itemRev;
        return bomInfo;
    }
    
    /**
     * Save BOMWindow
     * 
     * @method saveBOMWindow 
     * @date 2013. 5. 14.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void saveBOMWindow(BOMWindow bomWindow) throws Exception {        
       SaveBOMWindowsResponse rsp = getService().saveBOMWindows(new BOMWindow[] { bomWindow });
       if (tcServiceManager.getDataService().ServiceDataError(rsp.serviceData)) {
           throw new Exception(TcUtil.makeMessageOfFail(rsp.serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
       }
    }
    
    public void closeBOMWindow(BOMWindow bomWindow) throws Exception {
        CloseBOMWindowsResponse rsp = getService().closeBOMWindows(new BOMWindow[] { bomWindow });
        if (tcServiceManager.getDataService().ServiceDataError(rsp.serviceData)) {
            throw new Exception(TcUtil.makeMessageOfFail(rsp.serviceData).get(TcConstants.TC_RETURN_FAIL_REASON).toString());
        }
    }
}
