package com.symc.plm.me.sdv.service.resource;

import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.NamedReferenceContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinition;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentFolderType;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.util.Registry;

public class DatasetUtilities {


    /**
     * Title: Item Revision 아래의 DataSet을 가져옴<br>
     * Usage: getDatasets(itemRevision,"IMAN_specification","MSExcel")
     * 
     * @param itemRevision
     * @param relationType
     * @param dataType
     *            dataType이 "All"이면 모든 dataSet을 가져옴
     * @return datasets
     */
    @SuppressWarnings("rawtypes")
    public static Vector getDatasets(TCComponent itemRevision, String relationType,
            String dataType) throws Exception {
        Vector<TCComponentDataset> datasets = new Vector<TCComponentDataset>();
        TCComponentDataset tmpdataset = null;
        if (itemRevision == null) {
            return null;
        }
        TCComponent[] tmpComponent = itemRevision.getRelatedComponents(relationType);
        for (int i = 0; i < tmpComponent.length; i++) {
            if (tmpComponent[i] instanceof TCComponentDataset) {
                tmpdataset = (TCComponentDataset) tmpComponent[i];

                if (!dataType.equalsIgnoreCase("All")) {
                    if (tmpdataset.getType().equalsIgnoreCase(dataType)) {
                        if (datasets == null) {
                            datasets = new Vector<TCComponentDataset>();
                        }
                        datasets.addElement(tmpdataset);
                    }
                } else {
                    if (datasets == null) {
                        datasets = new Vector<TCComponentDataset>();
                    }
                    datasets.addElement(tmpdataset);
                }
            }
        }
        return datasets;
    }

    /**
     * 파일을 읽어 byte array로 변환시켜주는 method이다.
     * 
     * @param file
     *            File
     * @return byte[]
     * @throws Exception
     */
    public static byte[] getFile(File file) throws Exception {
        byte[] buffer = new byte[512];
        byte[] fis = new byte[(int) file.length()];
        int c, i = 0;

        FileInputStream file_in = new FileInputStream(file);

        while ((c = file_in.read(buffer)) != -1) {
            System.arraycopy(buffer, 0, fis, i, c);
            i = i + c;
        }

        file_in.close();

        return fis;
    }

    /**
     * Dataset을 생성하고 target에 특정 releation으로 붙인다.
     * 
     * @param session
     *            TCSession
     * @param targetComp
     *            TCComponent 붙이고자하는 대상
     * @param datasetName
     *            String dataset의 이름
     * @param description
     *            String dataset의 설명
     * @param datasetType
     *            String dataset의 Type
     * @param relation
     *            String target에 붙일때의 관계
     * @return TCComponentDataset 생성된 Dataset
     * @throws Exception
     */
    public static TCComponentDataset createPasteDataset(TCComponent targetComp, String datasetName,
            String description, String datasetType, String relation) throws Exception {
        TCSession session = targetComp.getSession();
        TCComponentDataset newDataset = null;
        String s = null;
        try {
            TCPreferenceService imanpreferenceservice = session.getPreferenceService();
            String s1 = "IMAN_" + datasetType + "_Tool";
//            s = imanpreferenceservice.getString(0, s1);
            s = imanpreferenceservice.getStringValue(s1);
        } catch (Exception ex) {
            s = null;
        }
        try {
            TCComponentDatasetType TCComponentdatasettype = (TCComponentDatasetType) session
                    .getTypeComponent(datasetType);
            newDataset = TCComponentdatasettype.create(datasetName, description, null, null, datasetType, s);
            if (targetComp != null) {
                targetComp.add(relation, (TCComponent) newDataset);
            }
        } catch (Exception TCException) {
            TCException.printStackTrace();
            throw TCException;
        }
        return newDataset;
    }

    /**
     * Dataset 내의 named reference들의 original file name을 변경한다.<br>
     * 변경은 reference name단위로 일괄 변경되므로 반드시 주의 해야 한다.<br>
     * 여러개의 named reference가 있을 경우에는 반드시 주의하여 사용하여야 한다.
     * 
     * @param dataset
     *            TCComponentDataset 변경하고자 하는 dataset
     * @param ref_names
     *            String 변경하고자 하는 reference type name
     * @param originalFileName
     *            String 변경할 이름. 예) aaa.txt
     * @throws Exception
     * @see getNamedRefType(TCComponentDataset datasetComponent, TCComponent TCComponent) throws
     *      Exception
     */
    public static void changeNamedRef(TCComponentDataset dataset, String ref_names, String originalFileName)
            throws Exception {
        TCComponentTcFile[] imanFiles = dataset.getTcFiles();
        for (int i = 0; i < imanFiles.length; i++) {
            String referenceName = getNamedRefType(dataset, imanFiles[i]);
            if (referenceName.equalsIgnoreCase(ref_names)) {
                imanFiles[i].setOriginalFileName(dataset, originalFileName);
            }
        }
    }


    /**
     * 이 method는 dataset안에 있는 모든 file을 내려받는 method이다. Leehonghee[2/8] Add if (imanFile.length > 0 )
     * 를 추가하여 dataset.getFiles(...) 에서 발생하는 Null Point Dialog를 방지한다.
     * 
     * @param dataset
     *            TCComponentDataset
     * @param export_subDir
     *            String
     * @return File[]
     * @throws Exception
     * 
     */
    public static File[] exportDataset(TCComponentDataset dataset, String export_subDir) throws Exception {
        Registry registry = Registry.getRegistry("client_specific");
        String export_dir = registry.getString("TCExportDir") + File.separator + export_subDir;
        File folder = new File(export_dir);
        folder.mkdirs();
        if (!folder.exists()) {
            throw new Exception("Export folder not found.");
        }
        TCComponentTcFile[] imanFile = dataset.getTcFiles();
        File[] file = null;
        if (imanFile.length > 0) {
            file = dataset.getFiles(getNamedRefType(dataset, imanFile[0]), export_dir);
        }
        return file;
    }

    /**
     * 특정 dataset안에 있는 named reference의 reference type name을 받아오는 method이다.
     * 
     * @param datasetComponent
     *            TCComponentDataset
     * @param TCComponent
     *            TCComponent named TCComponentfile 객체
     * @return String named reference type name
     * @throws Exception
     */
    public static String getNamedRefType(TCComponentDataset datasetComponent, TCComponent TCComponent)
            throws Exception {
        String s = "";
        TCProperty imanproperty = datasetComponent.getTCProperty("ref_list");
        TCProperty imanproperty1 = datasetComponent.getTCProperty("ref_names");
        if (imanproperty == null || imanproperty1 == null) {
            return s;
        }
        TCComponent aTCComponent[] = imanproperty.getReferenceValueArray();
        String as[] = imanproperty1.getStringValueArray();
        if (aTCComponent == null || as == null) {
            return s;
        }
        int i = aTCComponent.length;
        if (i != as.length) {
            return s;
        }
        int j = -1;
        for (int k = 0; k < i; k++) {
            if (TCComponent != aTCComponent[k]) {
                continue;
            }
            j = k;
            break;
        }

        if (j != -1) {
            s = as[j];
        }
        return s;
    }
    
    /**
     * dataset에 일반 파일 객체를 올릴수 있는 method이다.
     * 
     * @param newDataset
     *            TCComponentDataset
     * @param file
     *            File
     * @throws Exception
     */
    public static void importFiles(TCComponentDataset newDataset, File file) throws Exception {
        String[] as = new String[1];
        String[] as1 = new String[1];
        String[] as2 = new String[1];
        String[] as3 = new String[1];

        String filePath = file.getPath();
        String fileName = file.getName();
        int p = fileName.lastIndexOf(".");
        String fileExtendsName = fileName.substring(p + 1);
        String namedRefName = getNamedRefType(newDataset, fileExtendsName.toLowerCase());
        // importFileName
        as[0] = filePath;
        // importRefType
        as1[0] = namedRefName;
        // importFileType
        as2[0] = "File";
        as3[0] = "Plain";
        Registry registry = Registry.getRegistry("com.ugsolutions.iman.kernel.kernel");
        int l = registry.getInt("IMANFile_transfer_buf_size", 512);
        newDataset.setFiles(as, as2, as3, as1, l);
    }

    /**
     * Dataset 내에 있는 모든 named reference를 삭제하는 method이다.
     * 
     * @param dataset
     *            TCComponentDataset
     * @throws Exception
     */

    @SuppressWarnings("rawtypes")
    public static void removeAllNamedReference(TCComponentDataset dataset) throws Exception {
        TCComponentTcFile[] imanFile = dataset.getTcFiles();
        Vector refNameVector = getAllNamedRefTypeArray(dataset);
        for (int i = 0; i < refNameVector.size(); i++) {
            dataset.removeNamedReference(refNameVector.elementAt(i).toString());
        }
        for (int j = 0; j < imanFile.length; j++) {
            imanFile[j].delete();
        }
    }

    /**
     * dataset에 대하여 file의 확장자에 해당하는 named reference type을 찾는 method이다.
     * 
     * @param datasetComponent
     *            TCComponentDataset
     * @param extendsName
     *            String 파일 확장자 예) txt
     * @return String
     * @throws Exception
     */
    public static String getNamedRefType(TCComponentDataset datasetComponent, String extendsName)
            throws Exception {
        String s = "";
        NamedReferenceContext[] namedRefContext = null;
        namedRefContext = datasetComponent.getDatasetDefinitionComponent().getNamedReferenceContexts();
        for (int i = 0; i < namedRefContext.length; i++) {
            String s1 = namedRefContext[i].getNamedReference();
            String s2 = namedRefContext[i].getFileTemplate();
            if (s2.equalsIgnoreCase("*") || s2.equalsIgnoreCase("*.*")) {
                s = s1;
                break;
            } else if (s2.equalsIgnoreCase("*." + extendsName)) {
                s = s1;
                break;
            }
        }
        return s;
    }

    /**
     * Dataset이 가지고 있는 모든 reference name을 찾는 method
     * 
     * @param datasetComponent
     *            TCComponentDataset
     * @return Vector
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    public static Vector getAllNamedRefTypeArray(TCComponentDataset datasetComponent) throws Exception {
        Vector<String> s = new Vector<String>();
        NamedReferenceContext[] namedRefContext = null;
        try {
            namedRefContext = datasetComponent.getDatasetDefinitionComponent().getNamedReferenceContexts();
            for (int i = 0; i < namedRefContext.length; i++) {
                String s1 = namedRefContext[i].getNamedReference();
                s.addElement(s1);
            }
        } catch (Exception e) {
            throw e;
        }
        return s;
    }

    /**
     * 현재 로그인한 TCSession 객체를 가져온다.
     * 
     * @return TCSession 현재 로그인 객체를 리턴한다.
     */
    public static TCSession getTCSession() {
        return (TCSession) AIFUtility.getSessionManager().getDefaultSession();
    }


    /**
     * 원본 파일을 원하는 이름의 파일로 바꾸려고 할때 사용한다.
     * 
     * @param sourceFile
     *            File 바꾸고자 하는 원본 파일
     * @param newFileName
     *            String 바꾸고자 하는 이름
     * @return File 최종 바뀐 파일
     */
    public static File renameFile(File sourceFile, String newFileName) {
        File newFile = new File(sourceFile.getParent(), newFileName
                + sourceFile.getName().substring(sourceFile.getName().indexOf(".")));
        sourceFile.renameTo(newFile);
        return newFile;
    }

    /**
     * 파일로 데이터셋 생성
     * 
     * @Copyright : S-PALM
     * @author : 이정건
     * @since : 2011. 8. 29.
     * @param session
     * @param file
     * @return TCComponentDataset
     * @throws Exception
     */
    public static TCComponentDataset createDataset(TCSession session, File file) throws Exception {

        TCPreferenceService preferenceService = session.getPreferenceService();
        TCComponentDatasetType datasetType = (TCComponentDatasetType) session.getTypeComponent("Dataset");

        TCComponentDataset dataset = null;
        String extension = getExtension(file);
//        String[] dataset_Per_Extension = preferenceService.getStringArray(TCPreferenceService.TC_preference_site, "SPALM_DragNDropCopy_Dataset_Extension_List");
        String[] dataset_Per_Extension = preferenceService.getStringValuesAtLocation("SPALM_DragNDropCopy_Dataset_Extension_List", TCPreferenceLocation.OVERLAY_LOCATION);

        for (String string : dataset_Per_Extension) {
            if (extension.equals(string.substring(0, string.indexOf("=")))) {
                try {
                    dataset = datasetType.create(getFileName(file), "", string
                            .substring(string.indexOf("=") + 1));
                    String namedReference = getNamedReference(session, dataset, extension);
                    dataset
                            .setFiles(new String[] { file.getAbsolutePath() },
                                    new String[] { namedReference });
                } catch (Exception e) {
                    System.out.println("Dataset Type No Found! Select DatasetType Dialog Open...");
                    return dataset;
                }
            }
        }
        return dataset;
    }

    public static TCComponentDataset createDataset(TCSession session, File file, String type)
            throws Exception {
        TCComponentDatasetType datasetType = (TCComponentDatasetType) session.getTypeComponent("Dataset");
        TCComponentDataset dataset = datasetType.create(getFileName(file), "", type);
        String extension = getExtension(file);
        String namedReference = getNamedReference(session, dataset, extension);
        dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { namedReference });
        return dataset;
    }

    public static TCComponent createFolder(TCSession session, File file, String type) throws Exception {
        TCComponentFolderType fType = (TCComponentFolderType) session.getTypeComponent("Folder");
        TCComponentFolder newObjectg = fType.create(getFileName(file), "", type);
        return newObjectg;
    }

    public static String getFileName(File file) throws Exception {
        if (file.isDirectory()) {
            return file.getName();
        }
        String filename = file.getName();
        int i = filename.lastIndexOf('.');
        if (i > 0) {
            return filename.substring(0, i);
        }
        return null;
    }

    public static String getExtension(File file) throws Exception {
        if (file.isDirectory())
            return null;
        String filename = file.getName();
        int i = filename.lastIndexOf('.');
        if (i > 0 && i < filename.length() - 1) {
            return filename.substring(i + 1).toLowerCase();
        }
        return null;
    }

    /**
     * 
     * @Copyright : S-PALM
     * @author : 이정건
     * @since : 2011. 8. 29.
     * @param session
     * @param dataset
     * @param ext
     * @return String
     */
    public static String getNamedReference(TCSession session, TCComponentDataset dataset, String ext) {

        String namedReference = null;

        try {
            TCComponentDatasetDefinition def = dataset.getDatasetDefinitionComponent();
            NamedReferenceContext namedRefTypes[] = def.getNamedReferenceContexts();
            for (int i = 0; i < namedRefTypes.length; i++) {
                if (namedRefTypes[i].getFileTemplate().equalsIgnoreCase("*." + ext)) {
                    namedReference = namedRefTypes[i].getNamedReference();
                }
                if (namedRefTypes[i].getFileFormat().equals("TEXT")
                        && namedRefTypes[i].getFileTemplate().equalsIgnoreCase("*.*")) {
                    namedReference = namedRefTypes[i].getNamedReference();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return namedReference;
    }

}
