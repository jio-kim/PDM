package com.kgm.commands.mig.service;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.commands.mig.MigrationDialog;
import com.kgm.commands.mig.exception.MigrationException;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
//import org.apache.log4j.Logger;

public class MigrationValidationService {    
    private MigrationDialog migrationDialog;
    private static HashMap<String, String> itemTypeMap;
    private static HashMap<String, String> itemTypeCheckMap;
    @SuppressWarnings("rawtypes")
    public ArrayList<HashMap> validationList;
    public String objectType;
    public String batchType;
    public String seqType;
    //private static Logger logger = Logger.getLogger(MigrationDialog.class);

    public MigrationValidationService(MigrationDialog migrationDialog) {
        this.migrationDialog = migrationDialog;        
    }

    /**
     * Validation
     * 
     * @method validation
     * @date 2013. 2. 14.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void validation(String objectType, String batchType, String seqType) throws MigrationException {
        this.objectType = objectType;
        this.batchType = batchType;
        this.seqType = seqType;
        // Log화면 Clear
        this.clearProgressLog();
        if (objectType == null || "".equals(StringUtil.nullToString(objectType))) {
            throw new MigrationException("need Object Type!!");
        }
        if (batchType == null || "".equals(StringUtil.nullToString(batchType))) {
            throw new MigrationException("need Batch Type!!");
        }
        SYMCRemoteUtil remote = new SYMCRemoteUtil();
        try {
            TCSession session = CustomUtil.getTCSession();
            DataSet ds = new DataSet();
            ds.put("userId", session.getUser().getUserId());
            ds.put("objectType", objectType);
            ds.put("seqType", seqType);
            this.validationList = (ArrayList<HashMap>) remote.execute("com.kgm.service.MigrationService", "getValidationList", ds);
            // Executing Job 버튼 enable
            this.controlButton(true);
        } catch (ConnectException ce) {
            // Executing Job 버튼 disable
            this.controlButton(false);
            ce.printStackTrace();
            throw new MigrationException("Migration Data DB connect Error occurred!");
        } catch (Exception e) {
            // Executing Job 버튼 disable
            this.controlButton(false);
            e.printStackTrace();
            throw new MigrationException("Migration Data DB import Error occurred!");
        }
        if (this.validationList == null || this.validationList.size() == 0) {
            // Executing Job 버튼 disable
            this.controlButton(false);
            throw new MigrationException("Migration DB Data empty!");
        }
        if (!getItemTypeMap().containsKey(objectType)) {
            // Executing Job 버튼 disable
            this.controlButton(false);
            throw new MigrationException("Not supperted Type!");
        }
        if ("ITEM".equals(getItemTypeCheckMap().get(objectType))) {
            this.checkItemTypeValidation();
        }
    }

    /**
     * 'ITEM' Type Migration Validation Check
     * 
     * @method checkItemTypeValidation
     * @date 2013. 2. 18.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private void checkItemTypeValidation() throws MigrationException {
        for (int i = 0; i < this.validationList.size(); i++) {
            TCComponentItemRevision itemRevision = null;
            try {
                String status = "";
                itemRevision = CustomUtil.findLatestItemRevision(getItemTypeMap().get(objectType), (String) this.validationList.get(i).get("ITEM_ID"));
                if ("".equals(status) && itemRevision != null) {
                    if ("Modify".equals(batchType)) {
                        status = "Modify";
                        // Item Delete 로직..
                    } else {
                        // Create 상태에서는 Item생성을 Skip
                        status = "Skip";
                    }
                }
                if ("".equals(status)) {
                    status = "Create";
                    // Success Flag 설정
                    this.validationList.get(i).put("MIG_CREATE_FLAG", "T");
                } else {
                    this.validationList.get(i).put("MIG_CREATE_FLAG", "F");
                }
                this.setValidationProgressing(i, status);
            } catch (TCException e) {
                this.setValidationProgressing(i, "[Validation] TCException occurred!");
                e.printStackTrace();
            }
        }
    }

    /**
     * Validation 진행상태 출력
     * 
     * @method setValidationProgressing
     * @date 2013. 2. 15.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("rawtypes")
    private void setValidationProgressing(int index, String msg) throws MigrationException {
        final int progressing = index;
        final String progressMsg = StringUtil.nullToString(msg);
        migrationDialog.shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                if (validationList == null || validationList.size() == 0) {
                    migrationDialog.progressBar.setSelection(100);
                }
                if (progressing == 0) {
                    migrationDialog.progressingText.append("Start Validation..\n");
                }
                migrationDialog.progressBar.setSelection((int) (((double) (progressing + 1) / (double) validationList.size()) * 100));
                HashMap object = validationList.get(progressing);
                if (object.containsKey("ITEM_ID")) {
                    migrationDialog.progressingText.append((progressing + 1) + " >> " + object.get("ITEM_ID") + " : " + object.get("NAME") + " -- " + progressMsg + "\n");
                }
                if (progressing == (validationList.size() - 1)) {
                    migrationDialog.progressingText.append("Completed Validation.. OK\n");
                }
            }
        });
    }

    /**
     * Executing Job 진행상태 출력
     * 
     * @method setExecutingJobProgressing
     * @date 2013. 2. 15.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("rawtypes")
    private void setExecutingJobProgressing(int index, String msg) throws MigrationException {
        final int progressing = index;
        final String progressMsg = StringUtil.nullToString(msg);
        migrationDialog.shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                if (validationList == null || validationList.size() == 0) {
                    migrationDialog.progressBar.setSelection(100);
                }
                if (progressing == 0) {
                    migrationDialog.progressingText.append("Start Executing Job..\n");
                }
                migrationDialog.progressBar.setSelection((int) (((double) (progressing + 1) / (double) validationList.size()) * 100));
                HashMap object = validationList.get(progressing);
                if (object.containsKey("ITEM_ID")) {
                    migrationDialog.progressingText.append((progressing + 1) + " >> " + object.get("ITEM_ID") + " : " + object.get("NAME") + " -- " + progressMsg + "\n");
                }
                if (progressing == (validationList.size() - 1)) {
                    migrationDialog.progressingText.append("Completed Executing Job.. OK\n");
                }
            }
        });
    }

    /**
     * Migration executing
     * 
     * @method executingJob
     * @date 2013. 2. 15.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    public void executingJob(String objectType, String batchType, String seqType) throws MigrationException {
        if (this.validationList == null || this.validationList.size() == 0) {
            // Executing Job 버튼 disable
            this.controlButton(false);
            throw new MigrationException("Validation Data empty!");
        }
        // Validation 한 데이터와 Executing Job하는 Combo데이터가 일치하는지 사전체크
        if (!(this.objectType.equals(objectType) && this.batchType.equals(batchType) && this.seqType.equals(seqType))) {
            throw new MigrationException("retry validation - do not touch combobox!");
        }
        // Type 속성 체크 (Item, Dataset, Relation 구분)
        if ("ITEM".equals(getItemTypeCheckMap().get(objectType))) {
            for (int i = 0; i < this.validationList.size(); i++) {
                String status = "created";
                try {
                    // Item 생성 Flag를 체크한다.
                    if (this.validationList.get(i).containsKey("MIG_CREATE_FLAG") && "T".equals(this.validationList.get(i).get("MIG_CREATE_FLAG"))) {
                        this.createItem(this.validationList.get(i), objectType);
                    } else {
                        status = "skipped";
                    }
                    // MIG_FLAG 상태 변경
                    this.validationList.get(i).put("MIG_FLAG", "C");
                } catch (TCException te) {
                    status = "[Executing Job] TCException occurred!";
                    te.printStackTrace();
                    // 아이템 에러 처리
                    this.validationList.get(i).put("MIG_FLAG", "F");
                } catch (MigrationException me) {
                    status = me.getMessage();
                    me.printStackTrace();
                    // 아이템 에러 처리
                    this.validationList.get(i).put("MIG_FLAG", "F");
                } finally {
                    this.setExecutingJobProgressing(i, status);
                }
            }
        }
        // MIGRATION 테이블 변경대상 상태 변경
        this.updateMigrationStatus();
    }

    /**
     * Item 생성시 Object Type을 구분하여 속성 데이터를 생성한다.
     * 
     * @method createItem
     * @date 2013. 2. 15.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({ "rawtypes" })
    private void createItem(HashMap createData, String objectType) throws TCException, MigrationException {
        // MIG_VEHPART
        if ("MIG_VEHPART".equals(objectType)) {
            TCComponentItem item = CustomUtil.createItem(getItemTypeMap().get(objectType), (String) createData.get("ITEM_ID"), (String) createData.get("REVISION_ID"), (String) createData.get("NAME"),
                    (String) createData.get("DESCRIPTION"));
            this.setVehPartProperties(item);
        } else {
            throw new MigrationException("Not supperted Item Type!");
        }
    }

    /**
     * DB 상태(MIG_FLAG)를 변경한다.
     * 
     * @method updateMigrationStatus
     * @date 2013. 2. 18.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void updateMigrationStatus() {
        SYMCRemoteUtil remote = new SYMCRemoteUtil();
        try {
            DataSet ds = new DataSet();
            ds.put("updateDataList", this.validationList);
            ds.put("objectType", objectType);
            remote.execute("com.kgm.service.MigrationService", "updateMigrationStatus", ds);
        } catch (ConnectException ce) {
            ce.printStackTrace();
            // throw new
            // MigrationException("Migration Data DB connect Error occurred!");
        } catch (Exception e) {
            e.printStackTrace();
            // throw new
            // MigrationException("Migration Data DB import Error occurred!");
        }
    }

    /**
     * VEHPART 의 속성 데이터를 등록한다.
     * 
     * @method setVehPartProperties
     * @date 2013. 2. 15.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setVehPartProperties(TCComponentItem item) throws TCException {
        /*
         * s7_PROJECT_CODE s7_PART_TYPE ITEM_ID s7_DISPLAY_PART_NO s7_MATURITY
         * NAME s7_KOR_NAME s7_MAIN_NAME s7_SUB_NAME Description s7_LOC1_FR
         * s7_LOC2_IO s7_LOC3_UL s7_LOC4_EE s7_LOC5_LR s7_REFERENCE s7_UNIT
         * s7_ECO_NO s7_COLOR s7_COLOR_ID s7_REGULATION s7_MATTERIAL
         * s7_ALT_MATERIAL s7_THICKNESS s7_ALT_THICKNESS s7_FINISH s7_DRW_STAT
         * s7_SHOW_PART_NO s7_EST_WEIGHT s7_CAL_WEIGHT s7_ACT_WEIGHT
         * s7_CAL_SURFACE s7_BOUNDINGBOX s7_DVP_RESULT s7_Responsibility
         * s7_STAGE s7_REGULAR_PART s7_AS_END_ITEM s7_SELECTIVE_PART
         * s7_CHANGE_DESCRIPTION s7_DRW_SIZE s7_SYSTEM_CODE s7_SUB_SYSTEM_CODE
         * REVISION_ID PSTATUS PLAST_MOD_DATE RLAST_MOD_USER ROWING_GROUP
         * ROWNIG_USER PCREATION_DATE V_CREATOR MIG_FLAG
         */
        // item.getLatestItemRevision().setProperty(arg0, arg1)
    }

    /**
     * Executing Job 버튼 제어
     * 
     * @method controlButton
     * @date 2013. 2. 15.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void controlButton(boolean enable) {
        final boolean check = enable;
        migrationDialog.shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                migrationDialog.btnExecutingJob.setEnabled(check);
            }
        });
    }

    /**
     * 로그화면 초기화
     * 
     * @method clearProgressLog
     * @date 2013. 2. 15.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void clearProgressLog() {
        migrationDialog.shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                migrationDialog.progressingText.setText("");
                migrationDialog.progressBar.setSelection(0);
            }
        });
    }

    /**
     * OBJECT 테이블과 Item Type간 맵핑 정보
     * 
     * @method getItemTypeMap
     * @date 2013. 2. 15.
     * @param
     * @return HashMap<String,String>
     * @exception
     * @throws
     * @see
     */
    public static HashMap<String, String> getItemTypeMap() {
        if (itemTypeMap == null) {
            itemTypeMap = new HashMap<String, String>();
            itemTypeMap.put("MIG_PRODUCT", "S7_Product");
            // itemTypeMap.put("MIG_PROJECT", "NOT");
            // itemTypeMap.put("MIG_ENGDOC", "NOT");
            // itemTypeMap.put("MIG_FILE", "NOT");
            // itemTypeMap.put("MIG_FUNCTION", "NOT");
            // itemTypeMap.put("MIG_MATERIAL", "NOT");
            // itemTypeMap.put("MIG_STDPART", "NOT");
            // itemTypeMap.put("MIG_VARIANT", "NOT");
            itemTypeMap.put("MIG_VEHPART", "S7_Vehpart");
        }
        return itemTypeMap;
    }

    /**
     * Object의 Type 속성을 알아내는 Map
     * 
     * @method getItemTypeCheckMap
     * @date 2013. 2. 15.
     * @param
     * @return HashMap<String,String>
     * @exception
     * @throws
     * @see
     */
    public static HashMap<String, String> getItemTypeCheckMap() {
        if (itemTypeCheckMap == null) {
            itemTypeCheckMap = new HashMap<String, String>();
            itemTypeCheckMap.put("MIG_PRODUCT", "ITEM");
            // itemTypeCheckMap.put("MIG_PROJECT", "NOT");
            // itemTypeCheckMap.put("MIG_ENGDOC", "NOT");
            // itemTypeCheckMap.put("MIG_FILE", "NOT");
            // itemTypeCheckMap.put("MIG_FUNCTION", "NOT");
            // itemTypeCheckMap.put("MIG_MATERIAL", "NOT");
            // itemTypeCheckMap.put("MIG_STDPART", "NOT");
            // itemTypeCheckMap.put("MIG_VARIANT", "NOT");
            itemTypeCheckMap.put("MIG_VEHPART", "ITEM");
        }
        return itemTypeCheckMap;
    }
    
}
