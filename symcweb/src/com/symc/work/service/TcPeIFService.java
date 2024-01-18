package com.symc.work.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.swing.tree.DefaultMutableTreeNode;

import com.symc.common.dao.TcCommonDao;
import com.symc.common.exception.BaseException;
import com.symc.common.exception.NotLoadedChildLineException;
import com.symc.common.soa.biz.Session;
import com.symc.common.soa.biz.TcItemUtil;
import com.symc.common.soa.biz.TcVariantUtil;
import com.symc.common.soa.service.TcServiceManager;
import com.symc.common.util.ContextUtil;
import com.symc.common.util.DateUtil;
import com.symc.common.util.IFConstants;
import com.symc.common.util.NetworkUtil;
import com.symc.common.util.StringUtil;
import com.symc.work.model.BOMChangeInfoVO;
import com.symc.work.model.EcoWhereUsedVO;
import com.symc.work.model.FunctionInfoVO;
import com.symc.work.model.PartBOMInfoVO;
import com.symc.work.model.PartInfoVO;
import com.symc.work.model.ProductInfoVO;
import com.teamcenter.services.internal.strong.structuremanagement._2011_06.VariantManagement.ModularOption;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.Group;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.UnitOfMeasure;
import com.teamcenter.soa.client.model.strong.User;
import com.teamcenter.soa.exceptions.NotLoadedException;

public class TcPeIFService {

    public TcPeIFService() {
    }

    /**
     * Product하위 Fuction List 조회
     *
     * @method getFunctionList
     * @date 2013. 7. 26.
     * @param
     * @return List<ProductInfoVO>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    public List<FunctionInfoVO> getFunctionList(String productId) throws Exception {
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("productId", productId);
        return (List<FunctionInfoVO>) TcCommonDao.getTcCommonDao().selectList("com.symc.ifpe.getFunctionList", param);
    }

    /**
     * PE Product 조회(IF_PE_PRODUCT) 쿼리 (TcPeIFJob 에서 사용)
     *
     * @method getPeProductList
     * @date 2013. 7. 19.
     * @param
     * @return HashMap<String,?>
     * @exception
     * @throws
     * @see com.symc.work.job.TcPeIFJob
     */
    @SuppressWarnings("unchecked")
    public List<ProductInfoVO> getPeProductList(String[] status, String transType) throws Exception {
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("status", status);
        param.put("transType", transType);
        return (List<ProductInfoVO>) TcCommonDao.getTcCommonDao().selectList("com.symc.ifpe.getPeProductList", param);
    }

    /**
     * I/F Product 정보 테이블(IF_PE_PRODUCT) 상태를 변경한다.
     *
     * @method changePeProductStatus
     * @date 2013. 7. 22.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void changePeProductStatus(String ifId, String status, Date waitDate) throws Exception {
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("ifId", ifId);
        param.put("status", status);
        if(waitDate != null) {
            param.put("waitDate", waitDate);
        }
        TcCommonDao.getTcCommonDao().update("com.symc.ifpe.changePeProductStatus", param);
    }

    /**
     * I/F Product LOG정보 테이블(IF_PE_PRODUCT_LOG) LOG를 등록한다.
     *
     * @method createPeProductLog
     * @date 2013. 7. 25.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void createPeProductLog(String ifId, String productId, String log) throws Exception {
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("ifId", ifId);
        param.put("productId", productId);
        param.put("log", log);
        // 이전 LOG 삭제
        TcCommonDao.getTcCommonDao().delete("com.symc.ifpe.deletePeProductLog", param);
        // LOG 등록
        TcCommonDao.getTcCommonDao().insert("com.symc.ifpe.createPeProductLog", param);
    }

    /**
     * Part정보를 IF_PE_PART_INFO 테이블에 등록한다.
     *
     * @method createPartInfo
     * @date 2013. 7. 22.
     * @param
     * @return void
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    public void createPePartInfo(PartInfoVO partInfoVO) throws Exception {
        TcCommonDao.getTcCommonDao().insert("com.symc.ifpe.createPePartInfo", partInfoVO);
    }

    /**
     * Class Type별 속성정보를 셋팅한다.
     *
     * @method getTypeAttrItemVO
     * @date 2013. 7. 22.
     * @param
     * @return PartInfoVO
     * @exception
     * @throws
     * @see
     */
    public PartInfoVO getTypeAttrItemVO(Session session, ProductInfoVO productInfoVO, ItemRevision itemRev, StringBuffer log) throws Exception {
        TcItemUtil tcItemUtil = new TcItemUtil(session);
        ArrayList<String> revisionProperties = new ArrayList<String>();
        PartInfoVO partInfoVO = null;
        if (IFConstants.CLASS_TYPE_S7_PRODUCT_REVISION.equals(itemRev.getTypeObject().getClassName())) {
            partInfoVO = this.setDefaultProperties(session, productInfoVO, itemRev, revisionProperties);
        } else if (IFConstants.CLASS_TYPE_S7_VARIANT_REVISION.equals(itemRev.getTypeObject().getClassName())) {
            partInfoVO = this.setDefaultProperties(session, productInfoVO, itemRev, revisionProperties);
        } else if (IFConstants.CLASS_TYPE_S7_FUNCTION_REVISION.equals(itemRev.getTypeObject().getClassName())) {
            partInfoVO = this.setDefaultProperties(session, productInfoVO, itemRev, revisionProperties);
        } else if (IFConstants.CLASS_TYPE_S7_FUNCTION_MAST_REVISION.equals(itemRev.getTypeObject().getClassName())) {
        	// [2013.10.02]FMP에 ECO No가져오도록 추가
        	revisionProperties.add("s7_ECO_NO");
            partInfoVO = this.setDefaultProperties(session, productInfoVO, itemRev, revisionProperties);
            // [2013.10.02]FMP에 ECO No가져오도록 추가
            ItemRevision ecoRevision = (ItemRevision) itemRev.getPropertyObject("s7_ECO_NO").getModelObjectValue();
            if( ecoRevision != null){
                tcItemUtil.getProperties(new ModelObject[] { ecoRevision }, new String[] { "item_id" });
                partInfoVO.setEcoNo(ecoRevision.get_item_id());
            }
        } else if (IFConstants.CLASS_TYPE_S7_VEH_PART_REVISION.equals(itemRev.getTypeObject().getClassName())) {
            // VEH_PART 속성
            revisionProperties.add("s7_SHOWN_PART_NO");
            revisionProperties.add("s7_CAL_WEIGHT");
            revisionProperties.add("s7_EST_WEIGHT");
            revisionProperties.add("s7_COLOR");
            revisionProperties.add("s7_CHANGE_DESCRIPTION");
            revisionProperties.add("s7_SELECTIVE_PART");
            // [2013.09.30]CATEGORY 코드 추가
            revisionProperties.add("s7_REGULATION");
            // VEH/STD 공통 속성
            this.setCommonPartAttr(revisionProperties);
            /////////////////////
            partInfoVO = this.setDefaultProperties(session, productInfoVO, itemRev, revisionProperties);
            // ShownPartNo 조회
            Item shownPart = (Item) itemRev.getPropertyObject("s7_SHOWN_PART_NO").getModelObjectValue();
            if (shownPart != null) {
                tcItemUtil.getProperties(new ModelObject[] { shownPart }, new String[] { "item_id" });
                partInfoVO.setShownOnNo(shownPart.get_item_id());
            }
            partInfoVO.setCalWeight(itemRev.getPropertyObject("s7_CAL_WEIGHT").getDoubleValue());
            partInfoVO.setEstWeight(itemRev.getPropertyObject("s7_EST_WEIGHT").getDoubleValue());
            partInfoVO.setColor(itemRev.getPropertyDisplayableValue("s7_COLOR"));
            partInfoVO.setChangeDesc(itemRev.getPropertyDisplayableValue("s7_CHANGE_DESCRIPTION"));
            partInfoVO.setSelectivePart(itemRev.getPropertyDisplayableValue("s7_SELECTIVE_PART"));
            // [2013.09.30]CATEGORY 코드 추가
            partInfoVO.setCategory(itemRev.getPropertyDisplayableValue("s7_REGULATION"));
            // VEH/STD 공통 속성 SET
            this.setCommonPartVO(itemRev, tcItemUtil, partInfoVO);
        } else if (IFConstants.CLASS_TYPE_S7_STD_PART_REVISION.equals(itemRev.getTypeObject().getClassName())) {
            // VEH/STD 공통 속성
            this.setCommonPartAttr(revisionProperties);
            partInfoVO = this.setDefaultProperties(session, productInfoVO, itemRev, revisionProperties);
            // VEH/STD 공통 속성 SET
            this.setCommonPartVO(itemRev, tcItemUtil, partInfoVO);
        } else {
            partInfoVO = this.setDefaultProperties(session, productInfoVO, itemRev, revisionProperties);
        }
        partInfoVO.setPartType(itemRev.getTypeObject().getClassName());
        return partInfoVO;
    }

    /**
     * VEH/STD 파트의 공통 속성을 설정
     *
     * @method setCommonPartAttr
     * @date 2013. 9. 4.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setCommonPartAttr(ArrayList<String> revisionProperties) {
        revisionProperties.add("s7_PART_TYPE");
        revisionProperties.add("s7_REFERENCE");
        revisionProperties.add("s7_MATERIAL");
        revisionProperties.add("s7_ALT_MATERIAL");
        revisionProperties.add("s7_FINISH");
        revisionProperties.add("s7_KOR_NAME");
        revisionProperties.add("s7_ACT_WEIGHT");
        revisionProperties.add("s7_ECO_NO");
    }

    /**
     *  VEH/STD 파트의 속성을 가져와 PartInfoVO에 바인딩
     *
     * @method setCommonPartVO
     * @date 2013. 9. 4.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setCommonPartVO(ItemRevision itemRev, TcItemUtil tcItemUtil, PartInfoVO partInfoVO) throws NotLoadedException, Exception {
        partInfoVO.setPartOrigin(itemRev.getPropertyDisplayableValue("s7_PART_TYPE"));
        partInfoVO.setReference(itemRev.getPropertyDisplayableValue("s7_REFERENCE"));
        partInfoVO.setKoreanName(itemRev.getPropertyDisplayableValue("s7_KOR_NAME"));
        partInfoVO.setActualWeight(itemRev.getPropertyObject("s7_ACT_WEIGHT").getDoubleValue());
        ItemRevision material = (ItemRevision) itemRev.getPropertyObject("s7_MATERIAL").getModelObjectValue();
        ItemRevision altMaterial = (ItemRevision) itemRev.getPropertyObject("s7_ALT_MATERIAL").getModelObjectValue();
        tcItemUtil.getProperties(new ModelObject[] { material, altMaterial }, new String[] { "item_id" });
        if (material != null) {
            partInfoVO.setMaterial(material.get_item_id());
        }
        if (altMaterial != null) {
            partInfoVO.setAltMaterial(altMaterial.get_item_id());
        }
        partInfoVO.setFinish(itemRev.getPropertyDisplayableValue("s7_FINISH"));
        //ECO No가져오도록 추가
        ItemRevision ecoRevision = (ItemRevision) itemRev.getPropertyObject("s7_ECO_NO").getModelObjectValue();
        if( ecoRevision != null){
            tcItemUtil.getProperties(new ModelObject[] { ecoRevision }, new String[] { "item_id" });
            partInfoVO.setEcoNo(ecoRevision.get_item_id());
        }
    }

    /**
     * Datset을 조회하여 DB에 등록 및 FTP 전송한다.
     *
     * @method setVoDatasetFile
     * @date 2013. 8. 6.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void setVoDatasetFile(Session session, ItemRevision itemRev, PartInfoVO partInfoVO) throws Exception {
        TcFileService tcFileService = new TcFileService(session);
        // 1. CAD 파일(CATPart, CGR) 유무 체크
        String hasCatpart = "N";
        String hasCatdwg = "N";
        String hasCgr = "N";
        // 2. CAD File Download
        HashMap<String, ImanFile[]> cadFilesMap = tcFileService.getImanFiles(itemRev);
        if (cadFilesMap.containsKey(IFConstants.TYPE_DATASET_CATCACHE)) {
            ImanFile[] fileObjects = cadFilesMap.get(IFConstants.TYPE_DATASET_CATCACHE);
            if (fileObjects != null && fileObjects.length > 0) {
                hasCgr = "Y";
            }
        }
        if (cadFilesMap.containsKey(IFConstants.TYPE_DATASET_CATDRAWING)) {
            ImanFile[] fileObjects = cadFilesMap.get(IFConstants.TYPE_DATASET_CATDRAWING);
            if (fileObjects != null && fileObjects.length > 0) {
                hasCatdwg = "Y";
            }
        }
        if (cadFilesMap.containsKey(IFConstants.TYPE_DATASET_CATPART)) {
            ImanFile[] fileObjects = cadFilesMap.get(IFConstants.TYPE_DATASET_CATPART);
            if (fileObjects != null && fileObjects.length > 0) {
                hasCatpart = "Y";
            }
        }
        partInfoVO.setHasCgr(hasCgr);
        partInfoVO.setHasCatdwg(hasCatdwg);
        partInfoVO.setHasCatpart(hasCatpart);
    }

    /**
     * PART Dataset FTP 전송
     *
     * @method setDatasetFileSave
     * @date 2013. 8. 12.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    public void setDatasetFileSave(Session session, ItemRevision itemRev, PartInfoVO partInfoVO, StringBuffer log) throws Exception {
        // IF_PE_FILE_PATH 중복 체크 - File Type 별 등록 확인
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("partNumber", partInfoVO.getPartNumber());
        paramMap.put("version", partInfoVO.getVersion());
        // 해당 아이템이 FILE정보가 Type 별로 등록 되있는지 확인한다. (STAT = 'SUCCESS' - 성공 상태인 것만 가져옴)
        // 만약 에러 상태(STAT <> 'SUCCESS')이면 다시 등록하여 업데이트 한다. - createFilePath 함수에서 쿼리가 MERGE문(UPDATE)을 수행함.
        List<HashMap<String, String>> existFileTypeList = (List<HashMap<String, String>>)TcCommonDao.getTcCommonDao().selectList("com.symc.ifpe.getFileType", paramMap);
        HashMap<String, String> existFileTypeMap = new HashMap<String, String>();
        for (int i = 0; existFileTypeList != null && i < existFileTypeList.size(); i++) {
            existFileTypeMap.put(existFileTypeList.get(i).get("FILE_TYPE"), existFileTypeList.get(i).get("FILE_TYPE"));
        }
        TcFileService tcFileService = new TcFileService(session);
        // 중복등록(IF_PE_FILE_PATH - existFileTypeMap에 File Type이 존재하지않으면 등록) 체크 & CAD File FTP Upload & DB 저장
        HashMap<String, ImanFile[]> cadFilesMap = tcFileService.getImanFiles(itemRev);
        if ((!existFileTypeMap.containsKey(IFConstants.TYPE_DATASET_CATCACHE)) && cadFilesMap.containsKey(IFConstants.TYPE_DATASET_CATCACHE)) {
            ImanFile[] fileObjects = cadFilesMap.get(IFConstants.TYPE_DATASET_CATCACHE);
            this.fileUploadAndSave(session, IFConstants.TYPE_DATASET_CATCACHE, partInfoVO, fileObjects, log);
        }
        if ((!existFileTypeMap.containsKey(IFConstants.TYPE_DATASET_CATDRAWING)) && cadFilesMap.containsKey(IFConstants.TYPE_DATASET_CATDRAWING)) {
            ImanFile[] fileObjects = cadFilesMap.get(IFConstants.TYPE_DATASET_CATDRAWING);
            this.fileUploadAndSave(session, IFConstants.TYPE_DATASET_CATDRAWING, partInfoVO, fileObjects, log);
        }
        if ((!existFileTypeMap.containsKey(IFConstants.TYPE_DATASET_CATPART)) && cadFilesMap.containsKey(IFConstants.TYPE_DATASET_CATPART)) {
            //ImanFile[] fileObjects = cadFilesMap.get(IFConstants.TYPE_DATASET_CATPART);
            // CATPart는 전송대상에서 제외
            // this.fileUploadAndSave(session, IFConstants.TYPE_DATASET_CATPART,
            // partInfoVO, fileObjects, log);
        }
        try {
            // ETS CGR파일 생성
            tcFileService.createCGR(itemRev);
        } catch (Exception e) {
            this.appandLog(IFConstants.TEXT_RETURN, log);
            this.appandLog("** ETS CGR 생성 에러 : " + partInfoVO.getPartNumber() + " **", log);
            this.appandLog(IFConstants.TEXT_RETURN, log);
            this.appandLog(StringUtil.getStackTraceString(e), log);
            this.appandLog(IFConstants.TEXT_RETURN, log);
            this.appandLog(IFConstants.TEXT_RETURN, log);
        }
    }

    /**
     * FTP 전송 후 파일정보(IF_PE_FILE_PATH) DB 저장
     *
     * @method fileUploadAndSave
     * @date 2013. 8. 6.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void fileUploadAndSave(Session session, String fileType, PartInfoVO partInfoVO, ImanFile[] fileObject, StringBuffer log) {
        String filePath = "";
        String realFileName = "";
        try {
            if (fileObject == null || fileObject.length == 0) {
                return;
            }
            TcFileService tcFileService = new TcFileService(session);
            ImanFile imanFile = fileObject[0];
            File file = tcFileService.getFile(imanFile);
            if (file == null) {
                throw new BaseException("Dataset Filie is empty");
            }
            Properties contextProperties = (Properties)ContextUtil.getBean("contextProperties");
            String ip = contextProperties.getProperty("cadFTP.ip");
            int port = Integer.parseInt(contextProperties.getProperty("cadFTP.port"));
            String login = contextProperties.getProperty("cadFTP.login");
            String pass = contextProperties.getProperty("cadFTP.pass");
            String cadFtpPath = contextProperties.getProperty("cadFTP.cadFtpPath") + "/" + fileType; // 파일 Type별로 폴더를 생성 또는 이동
            // TC 파일명 변경
            file = this.renameFile(file, fileType, partInfoVO);
            // FTP 전송
            NetworkUtil.uploadFtpFile(ip, port, login, pass, "/", cadFtpPath, new File[] { file });
            TcItemUtil tcItemUtil = new TcItemUtil(session);
            tcItemUtil.getProperties(new ModelObject[] { imanFile }, new String[] { "file_name", "original_file_name" });
            filePath = file.getName();
            realFileName = imanFile.get_original_file_name();
            // 파일정보(IF_PE_FILE_PATH) DB 저장
            this.createFilePath(fileType, partInfoVO, filePath, realFileName, IFConstants.SUCCESS);
            // FTP 전송 후 Download 받은 Dataset File은 삭제한다.
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                this.createFilePath(fileType, partInfoVO, filePath, realFileName, IFConstants.FTP_TRANSPER_ERROR);
            } catch (Exception ie) {
                e.printStackTrace();
                log.append(IFConstants.TEXT_RETURN);
                log.append("** TcPeIFService.fileUploadAndSave - FIL_PATH DB ERROR : " + partInfoVO.getPartNumber() + " **");
                log.append(IFConstants.TEXT_RETURN);
                log.append(StringUtil.getStackTraceString(ie));
                log.append(IFConstants.TEXT_RETURN);
            }
            log.append(IFConstants.TEXT_RETURN);
            log.append("** TcPeIFService.fileUploadAndSave - FTP UPLOAD ERROR : " + partInfoVO.getPartNumber() + " **");
            log.append(IFConstants.TEXT_RETURN);
            log.append(e.getMessage());
            log.append(IFConstants.TEXT_RETURN);
        }
    }

    /**
     * TC Dataset File을 이름 변경 (티켓ID -> 아이템ID_리비젼ID.파일명)
     *
     * @method renameFile
     * @date 2013. 8. 9.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public File renameFile(File file, String fileType, PartInfoVO partInfoVO) throws Exception {
        //
        String extName = "";
        if(IFConstants.TYPE_DATASET_CATCACHE.equals(fileType)) {
            extName = ".cgr";
        } else if(IFConstants.TYPE_DATASET_CATDRAWING.equals(fileType)) {
            extName = ".CATDrawing";
        }
        File renameFile = new File(file.getParent() + "/" + partInfoVO.getPartNumber() + "_" + partInfoVO.getVersion() + extName);
        file.renameTo(renameFile);
        return renameFile;
    }

    /**
     * 파일정보(IF_PE_FILE_PATH) DB 저장
     *
     * @method createFilePath
     * @date 2013. 8. 6.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createFilePath(String fileType, PartInfoVO partInfoVO, String filePath, String realFileName, String stat) throws Exception {
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("partNumber", partInfoVO.getPartNumber());
        paramMap.put("version", partInfoVO.getVersion());
        paramMap.put("partName", partInfoVO.getPartName());
        paramMap.put("fileType", fileType);
        paramMap.put("filePath", filePath);
        paramMap.put("realFileName", realFileName);
        paramMap.put("stat", stat);
        TcCommonDao.getTcCommonDao().insert("com.symc.ifpe.createFilePath", paramMap);
    }

    /**
     * PartInfoVO에 Revision 기본속성 데이터 바인딩
     *
     * @method setDefaultProperties
     * @date 2013. 7. 23.
     * @param
     * @return PartInfoVO
     * @exception
     * @throws
     * @see
     */
    public PartInfoVO setDefaultProperties(Session session, ProductInfoVO productInfoVO, ItemRevision itemRev, ArrayList<String> revisionProperties) throws Exception {
        TcItemUtil tcItemUtil = new TcItemUtil(session);
        // 기본속성
        revisionProperties.add("item_id");
        revisionProperties.add("item_revision_id");
        revisionProperties.add("object_name");
        revisionProperties.add("items_tag");
        revisionProperties.add("s7_PART_TYPE");
        revisionProperties.add("s7_PROJECT_CODE");
        revisionProperties.add("s7_MATURITY");
        revisionProperties.add("creation_date");
        revisionProperties.add("last_mod_date");
        revisionProperties.add("last_mod_user");
        revisionProperties.add("owning_user");
        revisionProperties.add("date_released");
        tcItemUtil.getProperties(new ModelObject[] { itemRev }, revisionProperties.toArray(new String[revisionProperties.size()]));
        PartInfoVO partInfoVO = new PartInfoVO();
        partInfoVO.setIfId(productInfoVO.getIfId());
        // Item ID를 읽는 경우 NotLoadedException이 종종 발생하여 에러 발생시 다시 리비젼을 로드하여 Propertis 설정 방식으로 변경
        try {
            itemRev.get_item_id();
        } catch (NotLoadedException ne) {
            // Revision을 다시 로드하여 Properties 설정
            TcServiceManager tcServiceManager = new TcServiceManager(session);
            itemRev = (ItemRevision) tcServiceManager.getDataService().loadModelObject(itemRev.getUid());
            tcItemUtil.getProperties(new ModelObject[] { itemRev }, revisionProperties.toArray(new String[revisionProperties.size()]));
        }
        partInfoVO.setObjectId(itemRev.getUid());
        partInfoVO.setPartNumber(itemRev.get_item_id());
        partInfoVO.setPartName(itemRev.get_object_name());
        partInfoVO.setVersion(itemRev.get_item_revision_id());
        partInfoVO.setCreationDate(itemRev.get_creation_date().getTime());
        partInfoVO.setLastModDate(itemRev.get_last_mod_date().getTime());
        // 최종 수정자
        User lastModifyUser = (User) itemRev.get_last_mod_user();
        tcItemUtil.getProperties(new ModelObject[] { lastModifyUser }, new String[] { "user_name" });
        partInfoVO.setLastModUser(lastModifyUser.get_user_name());
        // Owner
        User owingUser = (User) itemRev.get_owning_user();
        tcItemUtil.getProperties(new ModelObject[] { owingUser }, new String[] { "user_name", "login_group" });
        partInfoVO.setOwner(owingUser.get_user_name());
        Group loginGroup = (Group) owingUser.get_login_group();
        tcItemUtil.getProperties(new ModelObject[] { loginGroup }, new String[] { "object_name" });
        // Owning Group
        partInfoVO.setOwningGroup(loginGroup.get_object_name());
        // UOM 설정
        Item item = itemRev.get_items_tag();
        if (item != null) {
            tcItemUtil.getProperties(new ModelObject[] { item }, new String[] { "item_id", "uom_tag" });
            UnitOfMeasure uom = item.get_uom_tag();
            if (uom != null) {
                tcItemUtil.getProperties(new ModelObject[] { uom }, new String[] { "symbol", "unit" });
                if (!"".equals(StringUtil.nullToString(uom.get_symbol()))) {
                    partInfoVO.setUnit(uom.get_symbol());
                } else {
                    partInfoVO.setUnit("EA");
                }
            } else {
                partInfoVO.setUnit("EA");
            }
        }
        partInfoVO.setProjectCode(itemRev.getPropertyDisplayableValue("s7_PROJECT_CODE"));
        partInfoVO.setPartType(itemRev.getPropertyDisplayableValue("s7_PART_TYPE"));
        partInfoVO.setProjectCode(itemRev.getPropertyDisplayableValue("s7_PROJECT_CODE"));
        partInfoVO.setMaturity(itemRev.getPropertyDisplayableValue("s7_MATURITY"));
        partInfoVO.setReleasedDate(itemRev.get_date_released().getTime());
        return partInfoVO;
    }

    /**
     * BOMLine에서 BOM정보를 추출하고 BOM 정보를 IF_PE_BOM_CHANGE 테이블에 등록한다. [초도 배포용]
     *
     * @method createPeBOMChange
     * @date 2013. 7. 22.
     * @param
     * @return void
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    public void createPeBOMChange(Session session, ProductInfoVO productInfoVO, ModularOption[] modularOptions, BOMLine bomLine, String constrainsOptions) throws Exception {
        BOMChangeInfoVO bomChangeInfoVO = this.getPeBOMChangeInfo(productInfoVO, bomLine, constrainsOptions);
        TcCommonDao.getTcCommonDao().insert("com.symc.ifpe.createBOMChangeInfo", bomChangeInfoVO);
    }

    /**
     * BOMLine에서 BOMChange 정보를 얻어온다.
     *
     * @method getPeBOMChangeInfo
     * @date 2013. 8. 12.
     * @param
     * @return BOMChangeInfoVO
     * @exception
     * @throws
     * @see
     */
    private BOMChangeInfoVO getPeBOMChangeInfo(ProductInfoVO productInfoVO, BOMLine bomLine, String constrainsOptions) throws NotLoadedException, Exception {
        BOMChangeInfoVO bomChangeInfoVO = new BOMChangeInfoVO();
        ItemRevision childItemRevision = (ItemRevision) bomLine.get_bl_revision();
        if(bomLine.get_bl_parent() != null) {
            BOMLine parentBOMLine = (BOMLine) bomLine.get_bl_parent();
            ItemRevision parentItemRevision = (ItemRevision) parentBOMLine.get_bl_revision();
            bomChangeInfoVO.setParentId(parentItemRevision.get_item_id());
            bomChangeInfoVO.setParentRevId(parentItemRevision.get_item_revision_id());
            bomChangeInfoVO.setParentType(parentItemRevision.getTypeObject().getClassName());
        }
        bomChangeInfoVO.setIfId(productInfoVO.getIfId());
        bomChangeInfoVO.setOccPuid(bomLine.get_bl_occurrence_uid());
        bomChangeInfoVO.setFindNo(bomLine.get_bl_sequence_no());
        bomChangeInfoVO.setAbsOccPuid(this.getOccPathFunctionUnderItem(bomLine));
        bomChangeInfoVO.setChildId(childItemRevision.get_item_id());
        bomChangeInfoVO.setChildRevId(childItemRevision.get_item_revision_id());
        bomChangeInfoVO.setChildType(childItemRevision.getTypeObject().getClassName());
        //bomChangeInfoVO.setAbsMatrix(bomLine.get_bl_plmxml_abs_xform());
        bomChangeInfoVO.setAbsMatrix(bomLine.get_bl_plmxml_occ_xform());
        if (bomLine.get_bl_parent() != null && !"".equals(StringUtil.nullToString(((BOMLine) bomLine.get_bl_parent()).get_bl_rev_mvl_text()))) {
            // bomChangeInfoVO.setOptions(((BOMLine)bomLine.get_bl_parent()).get_bl_rev_mvl_text()
            // + "\r\n\r\n\r\n\r\n" + constrainsOptions);
            bomChangeInfoVO.setOptions(constrainsOptions);
        }
        if (!"".equals(StringUtil.nullToString(bomLine.get_bl_variant_condition()))) {
            // bomChangeInfoVO.setOptionCondition(bomLine.get_bl_variant_condition()
            // + "\r\n\r\n\r\n\r\n" +
            // this.getOptionConvertingFormat(bomLine.get_bl_variant_condition()));
            bomChangeInfoVO.setOptionCondition(this.getOptionConvertingFormat(bomLine.get_bl_variant_condition()));
        }
        bomChangeInfoVO.setSupplyMode(bomLine.getPropertyDisplayableValue("S7_SUPPLY_MODE"));
        bomChangeInfoVO.setModuleCode(bomLine.getPropertyDisplayableValue("S7_MODULE_CODE"));
        bomChangeInfoVO.setPositionDesc(bomLine.getPropertyDisplayableValue("S7_POSITION_DESC"));
        //[20131002] Alternative Part 추가
        bomChangeInfoVO.setAlterPart(bomLine.getPropertyDisplayableValue("S7_ALTER_PART"));
        return bomChangeInfoVO;
    }

    /**
     * BOM 정보를 IF_PE_BOM_CHANGE 테이블에 등록한다. [EC 등록용]
     *
     * 만약 BOM CHANGE 정보가 있으면 ECO_BOM_LIST정보를 기준 정보가 존재하면 업데이트한다.
     *
     * [순서]
     * 1. Sample A Assy ECO BOM CHANGE 등록
     * 2. Sample A Assy ECO 하위를 전개하여 BOM CHANGE 등록
     * 3. Sample B Assy ECO BOM CHANGE 등록
     *    - 만약 Sample B Assy 가 Sample A Assy의 자식으로 존재할 경우
     *      등록 하지않고 ECO_BOM_LIST(CHANGE_TYPE, EPL_ID, OLD_OCC_PUID) 정보만 업데이트 한다.
     * 4. Sample B Assy ECO 하위를 전개하여 BOM CHANGE 등록
     *
     *
     * @method createPeBOMChangeInfo
     * @date 2013. 8. 12.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    public void createPeBOMChangeInfo(BOMChangeInfoVO bomChangeInfoVO) throws Exception {
        List<BOMChangeInfoVO> findBomChangeInfoList = (List<BOMChangeInfoVO>)TcCommonDao.getTcCommonDao().selectList("com.symc.ifpe.getBomChangeAbsOccInfo", bomChangeInfoVO);
        if(findBomChangeInfoList != null && findBomChangeInfoList.size() == 0) {
            TcCommonDao.getTcCommonDao().insert("com.symc.ifpe.createBOMChangeInfo", bomChangeInfoVO);
        } else {
            // EPL ID가 Null이 아니면 ECO 변경 등록 이므로 업데이트한다.
            if(!"".equals(StringUtil.nullToString(bomChangeInfoVO.getEplId()))) {
                TcCommonDao.getTcCommonDao().update("com.symc.ifpe.changeBOMChangeInfo", bomChangeInfoVO);
            }
        }
    }

    /**
     * Parent Path 정보를 얻어온다.
     *
     * @method getOccPathFunctionUnderItem
     * @date 2013. 7. 24.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public String getOccPathFunctionUnderItem(BOMLine bomLine) throws Exception {
        ArrayList<String> paths = new ArrayList<String>();
        reculsiveFunction(bomLine, paths);
        String path = "";
        for (int i = (paths.size() - 1); paths.size() > 0 && i >= 0; i--) {
            if ("".equals(path)) {
                path = paths.get(i);
            } else {
                path = path + "+" + paths.get(i);
            }
        }
        return path;
    }

    /**
     * Parent Node Reculsive.
     *
     * @method reculsiveFunction
     * @date 2013. 7. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void reculsiveFunction(BOMLine bomLine, ArrayList<String> paths) throws Exception {
        if (bomLine == null) {
            return;
        }
        BOMLine parent = (BOMLine) bomLine.get_bl_parent();
        if (IFConstants.CLASS_TYPE_S7_VARIANT_REVISION.equals(bomLine.get_bl_revision().getTypeObject().getClassName())) {
            return;
        }
        paths.add(bomLine.get_bl_occurrence_uid());
        reculsiveFunction(parent, paths);
    }

    /**
     * PE Format에 맞게 TC Option Combination을 Converting한다.
     *
     * @method getOptionConvertingFormat
     * @date 2013. 7. 24.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public String getOptionConvertingFormat(String optionCombination) throws Exception {
        if ("".equals(StringUtil.nullToString(optionCombination))) {
            return "";
        }
        StringBuffer convertOptionCombination = new StringBuffer();
        boolean isStartFindCode = false;
        String findCode = "";
        ArrayList<String> findCodes = new ArrayList<String>();
        ArrayList<String> modifications = new ArrayList<String>();
        for (int i = 0; i < optionCombination.length(); i++) {
            char checkChar = optionCombination.charAt(i);
            String setpStr = String.valueOf(checkChar);
            if (!isStartFindCode && "\"".equals(setpStr)) {
                isStartFindCode = true;
                continue;
            } else if (isStartFindCode && "\"".equals(setpStr)) {
                isStartFindCode = false;
                findCodes.add(findCode);
                findCode = "";
                continue;
            }
            if (isStartFindCode) {
                findCode = findCode + setpStr;
            }
            if (i - 3 > 0) {
                String findModify = optionCombination.substring(i - 3, i);
                System.out.println(findModify);
                if ("and".equals(findModify)) {
                    modifications.add("+");
                }
                findModify = optionCombination.substring(i - 2, i);
                if ("or".equals(findModify)) {
                    modifications.add("/");
                }
            }
        }
        int modificationCnt = 0;
        for (int i = 0; i < findCodes.size(); i++) {
            if (i == 0) {
                convertOptionCombination.append(findCodes.get(i));
            } else {
                convertOptionCombination.append(modifications.get(modificationCnt));
                convertOptionCombination.append(findCodes.get(i));
                modificationCnt = modificationCnt + 1;
            }
        }
        return convertOptionCombination.toString();
    }

    /**
     * 배치실행 시간 DB 등록
     *
     * @method createBatchTime
     * @date 2013. 8. 7.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void createBatchTime(long startTime, long endTime) throws Exception {
        TcCommonDao.getTcCommonDao().delete("com.symc.ifpe.deleteBatchTimeInfo", null);
        HashMap<String, Date> batchTimeMap = new HashMap<String, Date>();
        batchTimeMap.put("startDate", new Date(startTime));
        batchTimeMap.put("endDate", new Date(endTime));
        TcCommonDao.getTcCommonDao().insert("com.symc.ifpe.createBatchTimeInfo", batchTimeMap);
    }

    /**
     * Product ID를 가지고 초도 배포이력이 있는지 확인한다.
     *
     * @method checkFirstDistribute
     * @date 2013. 8. 8.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    public boolean checkFirstDistribute(String productId) throws Exception {
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("productId", productId);
        int cnt = (Integer)TcCommonDao.getTcCommonDao().selectOne("com.symc.ifpe.checkFirstDistribute", paramMap);
        if(cnt > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Product ID를 가지고 PRODUCT I/F 에러가 있었는지 확인한다.
     *
     * ERROR : true
     * NON ERROR : false
     *
     * @method checkProductError
     * @date 2013. 8. 21.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    public boolean checkProductError(String productId) throws Exception {
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("productId", productId);
        int cnt = (Integer)TcCommonDao.getTcCommonDao().selectOne("com.symc.ifpe.checkProductError", paramMap);
        if(cnt > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 변경Part의 parent FUNCTION 리스트가 PRODUCT 하위 FUNCTION 리스트에 존재하는지 확인
     *
     * @method getEcoFunctions
     * @date 2013. 8. 9.
     * @param
     * @return FunctionInfoVO[]
     * @exception
     * @throws
     * @see
     */
    public FunctionInfoVO[] getEcoFunctions(ProductInfoVO productInfoVO, List<FunctionInfoVO> productFunctionList, EcoWhereUsedVO[] affectedFunctions) throws Exception {
        if(productFunctionList == null || productFunctionList.size() == 0 || affectedFunctions == null || affectedFunctions.length == 0) {
            return new FunctionInfoVO[0];
        }
        HashMap<String, FunctionInfoVO> affectedEcoFunctionMap = new HashMap<String, FunctionInfoVO>();
        HashMap<String, FunctionInfoVO> productFunctionMap = new HashMap<String, FunctionInfoVO>();
        // DB에서 조회한 PRODUCT 하위 FUNCTION 리스트를 Map으로 변경한다.
        for (int i = 0; productFunctionList != null && i < productFunctionList.size(); i++) {
            productFunctionMap.put(productFunctionList.get(i).getFunctionId(), productFunctionList.get(i));
        }
        // affected FUNCTION 리스트가 PRODUCT 하위 FUNCTION 리스트에 존재하는지 확인
        for (int i = 0; i < affectedFunctions.length; i++) {
            if(productFunctionMap.containsKey(affectedFunctions[i].getItemId())) {
                affectedEcoFunctionMap.put(affectedFunctions[i].getItemId(), productFunctionMap.get(affectedFunctions[i].getItemId()));
            }
        }
        // Map을 배열로 변환
        String[] ecoFunctionIds = affectedEcoFunctionMap.keySet().toArray(new String[affectedEcoFunctionMap.size()]);
        FunctionInfoVO[] ecoFunctions = new FunctionInfoVO[ecoFunctionIds.length];
        for (int i = 0; i < ecoFunctionIds.length; i++) {
            ecoFunctions[i] = affectedEcoFunctionMap.get(ecoFunctionIds[i]);
        }
        return ecoFunctions;
    }

    /**
     * [EC 등록에서 사용]
     * FUNCTION(ROOT)을 가지고 하위 자식 BOMLine을 Tree 구조로 생성한다.
     *
     * @method makeTreeExpandAllLevel
     * @date 2013. 8. 12.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public DefaultMutableTreeNode makeTreeExpandAllLevel(Session session, ProductInfoVO productInfoVO, ItemRevision productItemRev, ModularOption[] modularOptions, StringBuffer log, BOMLine bomLine, DefaultMutableTreeNode treeNode) throws Exception {
        TcItemUtil tcItemUtil = new TcItemUtil(session);
        TcVariantUtil tcVariantUtil = new TcVariantUtil(session);

        try{
	        // BOMLine 속성 Setting..
	        tcItemUtil.getProperties(new ModelObject[] { bomLine }, BOMLineService.DEFAULT_BOMLINE_PROPERTIES);
	    }catch(Exception e){
	    	tcItemUtil.getProperties(new ModelObject[]{bomLine}, new String[]{"bl_revision"});
    		ItemRevision tRev = (ItemRevision)((BOMLine)bomLine).get_bl_revision();
    		tcItemUtil.getProperties(new ModelObject[]{tRev}, BOMLineService.DEFAULT_ITEM_REVISION_PROPERTIES);
    		System.out.println("에러 아이템 아이디 : " + tRev.get_item_id() + "/" + tRev.get_item_revision_id());

    		NotLoadedChildLineException exception = new NotLoadedChildLineException(e);
    		exception.addInfo(NotLoadedChildLineException.ITEM_ID, tRev.get_item_id());
    		exception.addInfo(NotLoadedChildLineException.ITEM_REVISION_ID, tRev.get_item_revision_id());

    		throw exception;
		}
        // Class Type에 따른 속성정보 조회 후 PartInfo 생성
        ItemRevision itemRevision = (ItemRevision) bomLine.getPropertyObject("bl_revision").getModelObjectValue();
        PartInfoVO partInfoVO = this.getTypeAttrItemVO(session, productInfoVO, itemRevision, log);
        // BOM 정보를 IF_PE_BOM_CHANG 테이블에 BOM 정보를 등록
        // Parent(부모의) Option정보를 등록한다.
        String constrainsOptions = tcVariantUtil.getConstrainsOptions(modularOptions, (BOMLine)bomLine.get_bl_parent());
        BOMChangeInfoVO bomChangeInfoVO = this.getPeBOMChangeInfo(productInfoVO, bomLine, constrainsOptions);
        PartBOMInfoVO partBOMInfoVO = new PartBOMInfoVO();
        partBOMInfoVO.setPartInfoVO(partInfoVO);
        partBOMInfoVO.setBomChangeInfoVO(bomChangeInfoVO);
        // Dataset VO설정
        this.setVoDatasetFile(session, itemRevision, partInfoVO);
        // Node에 Part, BOM정보 등록
        treeNode.setUserObject(partBOMInfoVO);
        ModelObject[] child = bomLine.getPropertyObject("bl_child_lines").getModelObjectArrayValue();
        for (int i = 0; child != null && i < child.length; i++) {
            ItemRevision childItemRevision = (ItemRevision) child[i].getPropertyObject("bl_revision").getModelObjectValue();
            if (childItemRevision == null) {
                continue;
            }
            // Child Node 생성
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
            // Child Node Add
            treeNode.add(childNode);
            // Reculsive
            this.makeTreeExpandAllLevel(session, productInfoVO, productItemRev, modularOptions, log, (BOMLine)child[i], childNode);
        }
        return treeNode;
    }

    /**
     * IF_PE_ECO_BOM_LIST - PRODUCT ECO I/F 등록 [EC 등록]
     *
     * @method createEcoBomListInfo
     * @date 2013. 8. 13.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void createEcoBomListInfo(String ifId, String ecoNo) throws Exception {
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("ifId", ifId);
        paramMap.put("ecoNo", ecoNo);
        TcCommonDao.getTcCommonDao().insert("com.symc.ifpe.createEcoBomListInfo", paramMap);
    }

    /**
     * I/F PRODUCT 정보 테이블(IF_PE_PRODUCT)에 PART, BOM COUNT를 업데이트한다.
     *
     * @method changePeProductPartBomCount
     * @date 2013. 8. 14.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void changePeProductPartBomCount(String ifId) throws Exception {
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("ifId", ifId);
        TcCommonDao.getTcCommonDao().insert("com.symc.ifpe.changePeProductPartBomCount", paramMap);
    }

    /**
     * IF_PE_PRODUCT 진행중(PROCESSING) 상태변경 및 Return Log 등록 (IF_PE_PRODUCT_LOG 테이블 저장)
     *
     * @method changeStatusProcessing
     * @date 2013. 8. 8.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void changeStatusProcessing(ProductInfoVO productInfoVO, StringBuffer log) throws Exception {
        // 상태 변경 - 진행중
        String completeDate = "PROCESSING - DATE : " + DateUtil.formatTime(new Date());
        this.changePeProductStatus(productInfoVO.getIfId(), IFConstants.PROCESSING, null);
        // LOG 등록
        this.createPeProductLog(productInfoVO.getIfId(), productInfoVO.getProductId(), completeDate);
    }

    /**
     * IF_PE_PRODUCT 성공(WAITING) 상태변경 및 Return Log 등록 (파일 + IF_PE_PRODUCT_LOG 테이블 저장)
     *
     * @method changeStatusWaiting
     * @date 2013. 8. 8.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void changeStatusWaiting(ProductInfoVO productInfoVO, StringBuffer log) throws Exception {
        // 상태 변경 - 성공
        Date waitDate = new Date();
        String completeDate = "WAITING - DATE : " + DateUtil.formatTime(waitDate);
        this.changePeProductStatus(productInfoVO.getIfId(), IFConstants.WAITING, waitDate);
        // LOG 등록
        this.createPeProductLog(productInfoVO.getIfId(), productInfoVO.getProductId(), completeDate);
        //log.append(IFConstants.TEXT_RETURN);
        //log.append("************* IF ID : " + productInfoVO.getIfId() + " *********************");
        //log.append(IFConstants.TEXT_RETURN);
        //log.append(completeDate);
        //log.append(IFConstants.TEXT_RETURN);
    }

    /**
     * IF_PE_PRODUCT 에러(ERROR) 상태변경 및 Return Log 등록 (파일 + IF_PE_PRODUCT_LOG 테이블 저장)
     *
     * @method changeStatusErrorAndStackTraceString
     * @date 2013. 8. 8.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void changeStatusErrorAndStackTraceString(ProductInfoVO productInfoVO, Exception e, StringBuffer log) throws Exception {
        log.append(IFConstants.TEXT_RETURN);
        log.append("************* IF ID : " + productInfoVO.getIfId() + " *********************");
        log.append(IFConstants.TEXT_RETURN);
        log.append(StringUtil.getStackTraceString(e));
        log.append(IFConstants.TEXT_RETURN);
        log.append("************************************************************************");
        log.append(IFConstants.TEXT_RETURN);
        // 상태 변경 - 실패
        this.changePeProductStatus(productInfoVO.getIfId(), IFConstants.ERROR, null);
        // LOG 등록
        this.createPeProductLog(productInfoVO.getIfId(), productInfoVO.getProductId(), StringUtil.getStackTraceString(e));
    }

    /**
     * 초도가 배포되지않은 ECO SKIP 메세지
     * - IF_PE_PRODUCT DB (STAT / LOG)는 변경하지않고 파일 로그에만 기록
     *
     * @method ecoSkipLog
     * @date 2013. 8. 8.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void ecoSkipLog(ProductInfoVO productInfoVO, StringBuffer log) throws Exception {
        // LOG 등록
        log.append(IFConstants.TEXT_RETURN);
        log.append("************* IF ID : " + productInfoVO.getIfId() + " *********************");
        log.append(IFConstants.TEXT_RETURN);
        log.append("[" + productInfoVO.getProductId() + "] Product가 초도배포가 되지않아 ECO등록이 Skip 처리되었습니다.");
        log.append(IFConstants.TEXT_RETURN);
    }

    /**
     * 이전 등록이 ERROR 상태로 인한 SKIP 메세지
     * - IF_PE_PRODUCT DB (STAT / LOG)는 변경하지않고 파일 로그에만 기록
     *
     * @method errorSkipLog
     * @date 2013. 8. 8.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void errorSkipLog(ProductInfoVO productInfoVO, StringBuffer log) throws Exception {
        // LOG 등록
        log.append(IFConstants.TEXT_RETURN);
        log.append("************* IF ID : " + productInfoVO.getIfId() + " *********************");
        log.append(IFConstants.TEXT_RETURN);
        log.append("[" + productInfoVO.getProductId() + "] Product가 이전 I/F 등록상태가 에러(ERROR) 이므로 Skip 처리되었습니다.");
        log.append(IFConstants.TEXT_RETURN);
    }

    /**
     * 사용자 Log 추가 - Task 파일에만 기록되는 Log
     *
     * @method appandLog
     * @date 2013. 8. 8.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void appandLog(String logStr, StringBuffer log) {
        log.append(IFConstants.TEXT_RETURN);
        log.append(logStr);
        log.append(IFConstants.TEXT_RETURN);
    }

    /**
     * PRODUCT I/F 정보 등록 후 EAI에 통보하기 위한 테이블(IF_PE_EAI_JOB) 정보 업데이트
     *
     * @method createEaiJob
     * @date 2013. 8. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void createEaiJob(ProductInfoVO productInfoVO) throws Exception {
        HashMap<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("ifId", productInfoVO.getIfId());
        paramMap.put("ifDate", productInfoVO.getIfDate());
        TcCommonDao.getTcCommonDao().insert("com.symc.ifpe.createEaiJob", paramMap);
    }
    
    /**
     * FUNCTION I/F 배포를 위한 Product와 연계된 Function List
     * 
     * @method getIfPproductFunctionList 
     * @date 2014. 2. 10.
     * @param
     * @return List<HashMap<String,String>>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    public List<HashMap<String, String>> getIfPproductFunctionList(String ifId) throws Exception {
     // IF_PE_FILE_PATH 중복 체크 - File Type 별 등록 확인
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("ifId", ifId);        
        return (List<HashMap<String, String>>)TcCommonDao.getTcCommonDao().selectList("com.symc.ifpe.getIfPproductFunctionList", paramMap);
    }
}
