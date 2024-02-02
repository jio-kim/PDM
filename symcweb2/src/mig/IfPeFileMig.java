/**
 * 
 */
package mig;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.symc.common.exception.BaseException;
import com.symc.common.soa.biz.Session;
import com.symc.common.soa.biz.TcItemUtil;
import com.symc.common.util.IFConstants;
import com.symc.common.util.NetworkUtil;
import com.symc.work.model.PartInfoVO;
import com.symc.work.service.TcPeIFService;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.ItemRevision;

/**
 * Class Name : IfPeFileMig
 * Class Description :
 * 
 * @date 2013. 10. 10.
 * 
 */
public class IfPeFileMig extends AbstractTcSoaJunit {

    // 마이그레이션을 실행하려면 아래 주석을 해제하세요..
    //@org.junit.Test
    public void execute() throws Exception {
        ArrayList<String> itemList = new ArrayList<String>();
        BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/mig/items.txt")));
        String line = null;
        while ((line = in.readLine()) != null) {
            itemList.add(line.trim());
            System.out.println(line.trim());
        }
        in.close();

        for (String itemId : itemList) {
            ItemRevision itemRev = tcItemUtil.getLastReleaseRevItem(itemId);
            PartInfoVO partInfoVO = new PartInfoVO();
            tcItemUtil.getProperties(new ModelObject[] { itemRev }, new String[] { "item_id", "item_revision_id", "object_name" });
            partInfoVO.setPartNumber(itemRev.get_item_id());
            partInfoVO.setVersion(itemRev.get_item_revision_id());
            partInfoVO.setPartName(itemRev.get_object_name());
            setDatasetFileSave(session, itemRev, partInfoVO);
        }
    }

    public void setDatasetFileSave(Session session, ItemRevision itemRev, PartInfoVO partInfoVO) throws Exception {
        // 중복등록(IF_PE_FILE_PATH - existFileTypeMap에 File Type이 존재하지않으면 등록) 체크 & CAD File FTP Upload & DB 저장
        HashMap<String, ImanFile[]> cadFilesMap = tcFileService.getImanFiles(itemRev);
        if (cadFilesMap.containsKey(IFConstants.TYPE_DATASET_CATCACHE)) {
            ImanFile[] fileObjects = cadFilesMap.get(IFConstants.TYPE_DATASET_CATCACHE);
            this.fileUploadAndSave(session, IFConstants.TYPE_DATASET_CATCACHE, partInfoVO, fileObjects);
        }
        if (cadFilesMap.containsKey(IFConstants.TYPE_DATASET_CATDRAWING)) {
            ImanFile[] fileObjects = cadFilesMap.get(IFConstants.TYPE_DATASET_CATDRAWING);
            this.fileUploadAndSave(session, IFConstants.TYPE_DATASET_CATDRAWING, partInfoVO, fileObjects);
        }
    }

    private void fileUploadAndSave(Session session, String fileType, PartInfoVO partInfoVO, ImanFile[] fileObject) {
        TcPeIFService tcPeIFService = new TcPeIFService();
        String filePath = "";
        String realFileName = "";
        try {
            if (fileObject == null || fileObject.length == 0) {
                return;
            }
            ImanFile imanFile = fileObject[0];
            File file = tcFileService.getFile(imanFile);
            if (file == null) {
                throw new BaseException("Dataset Filie is empty");
            }           
            // TC 파일명 변경
            file = tcPeIFService.renameFile(file, fileType, partInfoVO);
            // Type별 폴더 이동
            String cadFtpSavePath = cadFtpPath + "/" + fileType; // 파일 Type별로 폴더를 생성 또는 이동
            // FTP 전송
            NetworkUtil.uploadFtpFile(ip, port, login, pass, "/", cadFtpSavePath, new File[] { file });
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
            }
        }
    }

    private void createFilePath(String fileType, PartInfoVO partInfoVO, String filePath, String realFileName, String stat) throws Exception {
        String[] bindingDatas = new String[13];
        bindingDatas[0] = partInfoVO.getPartNumber();
        bindingDatas[1] = partInfoVO.getVersion();
        bindingDatas[2] = fileType;

        bindingDatas[3] = filePath;
        bindingDatas[4] = realFileName;
        bindingDatas[5] = stat;

        bindingDatas[6] = partInfoVO.getPartNumber();
        bindingDatas[7] = partInfoVO.getVersion();
        bindingDatas[8] = partInfoVO.getPartName();
        bindingDatas[9] = fileType;
        bindingDatas[10] = filePath;
        bindingDatas[11] = realFileName;
        bindingDatas[12] = stat;

        executeQuery(getCreateFilePathQueryString(), bindingDatas);
    }

    private String getCreateFilePathQueryString() {
        StringBuilder createFilePathQuery = new StringBuilder();
        createFilePathQuery.append("MERGE INTO IF_PE_FILE_PATH ");
        createFilePathQuery.append("USING DUAL ");
        createFilePathQuery.append("ON (PART_NUMBER = ? AND VERSION = ? AND FILE_TYPE = ?) ");
        createFilePathQuery.append("WHEN MATCHED THEN ");
        createFilePathQuery.append("UPDATE SET ");
        createFilePathQuery.append("FILE_PATH = ?, ");
        createFilePathQuery.append("REAL_FILE_NAME = ?, ");
        createFilePathQuery.append("CREATION_DATE = SYSDATE, ");
        createFilePathQuery.append("STAT = ? ");
        createFilePathQuery.append("WHEN NOT MATCHED THEN ");
        createFilePathQuery.append("INSERT (PART_NUMBER, VERSION, PART_NAME, FILE_TYPE, FILE_PATH, REAL_FILE_NAME, CREATION_DATE, STAT) ");
        createFilePathQuery.append("VALUES (?, ?, ?, ?, ?, ?, SYSDATE, ?) ");
        return createFilePathQuery.toString();
    }

    private void executeQuery(String query, String[] bindingDatas) {
    	// REAL
//        String DB_URL = "jdbc:oracle:thin:@10.80.1.90:1521:NEWPLM";
//        String DB_USER = "if_user";
//        String DB_PASSWORD = "IF_USER";
    	
    	// DEV
        String DB_URL = "jdbc:oracle:thin:@10.80.8.52:1521:tc";
        String DB_USER = "if_user";
        String DB_PASSWORD = "if_user";
        Connection conn = null;
        PreparedStatement preparedStatement = null;        
        try {
            // 드라이버를 로딩한다.
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            // 데이터베이스의 연결을 설정한다.
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            preparedStatement = conn.prepareStatement(query);
            for (int i = 0; i < bindingDatas.length; i++) {
                preparedStatement.setString((i + 1), bindingDatas[i]);
            }
            @SuppressWarnings("unused")
			int count = preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {                
                // Statement를 닫는다.
                preparedStatement.close();
                // Connection를 닫는다.
                conn.close();
            } catch (SQLException e) {
            }
        }
    }

}
