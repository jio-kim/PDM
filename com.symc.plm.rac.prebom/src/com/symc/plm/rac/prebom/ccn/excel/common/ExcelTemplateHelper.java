package com.symc.plm.rac.prebom.ccn.excel.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCSession;

/**
 * APPS 에서 사용했던 소스
 * @author jwlee
 *
 */
public class ExcelTemplateHelper {

    public static final int EXCEL_OPEN = 0;
    public static final int EXCEL_SAVE = 1;

    public static String exportPath;

    private static Shell parentShell;

    public static File getTemplateFile(int mode, String preferenceName, String defaultFileName) {
        File file = null;
        String itemID = getItemID(preferenceName);

        try {
            TCComponentItem item = getItem(itemID);

            if(item.getReleasedItemRevisions() != null) {
                TCComponentItemRevision revision = item.getReleasedItemRevisions()[0];
                TCComponentDataset dataset = getDataset(revision);
                file = getNamedReference(mode, dataset, defaultFileName);
//                File[] files = SDVPreBOMUtilities.exportDataset(dataset, "");
//                file = files[0];
            }
        } catch (TCException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file;
    }

    public static Workbook getWorkBook(int mode, String preferenceName, String defaultFileName) throws IOException {
        Workbook workbook = null;
        InputStream is = new FileInputStream(ExcelTemplateHelper.getTemplateFile(mode, preferenceName, defaultFileName));
        workbook = new XSSFWorkbook(is);

        return workbook;
    }

    /**
     * TCSession 객체 가져오기
     *
     * @method getTCSession
     * @date 2013. 10. 29.
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
     * preferenceName를 통해 Item ID 가져오기
     *
     * @method getItemID
     * @date 2013. 10. 29.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getItemID(String preferenceName) {
        TCPreferenceService prefService = getTCSession().getPreferenceService();
//        String itemID = prefService.getString(TCPreferenceService.TC_preference_site, preferenceName);
        String itemID = prefService.getStringValueAtLocation(preferenceName, TCPreferenceLocation.OVERLAY_LOCATION);

        return itemID;
    }

    /**
     * Item ID를 통해 Item 가져오기
     *
     * @method getItem
     * @date 2013. 10. 29.
     * @param
     * @return TCComponentItem
     * @exception
     * @throws
     * @see
     */
    public static TCComponentItem getItem(String itemID) throws TCException {
        TCComponentItemType itemType = (TCComponentItemType) getTCSession().getTypeComponent("Item");
        TCComponentItem item;
        if(itemType.findItems(itemID) != null) {
            item = itemType.findItems(itemID)[0];

            return item;
        }

        return null;
    }

    /**
     * revision 하위 Dataset(MSExcel, MSExcelX) 가져오기
     *
     * @method getDataset
     * @date 2013. 10. 29.
     * @param
     * @return TCComponentDataset
     * @exception
     * @throws
     * @see
     */
    public static TCComponentDataset getDataset(TCComponentItemRevision revision) throws TCException {
        AIFComponentContext[] contextList = revision.getChildren();
        TCComponentDataset dataset = null;
        if(contextList != null) {
            for(AIFComponentContext context : contextList) {
                InterfaceAIFComponent component = context.getComponent();
                if(component instanceof TCComponentDataset &&
                        (component.getType().equals("MSExcel") || component.getType().equals("MSExcelX"))) {
                    dataset = (TCComponentDataset) component;
                }
            }

            return dataset;
        }

        return null;
    }

    /**
     * Dataset의 namedReference에 있는 엑셀 파일 가져오기
     * EXCEL_OPEN 경로 : C:\Temp
     * EXCEL_SAVE 경로 : 사용자 선택
     *
     * @method getNamedReference
     * @date 2013. 10. 29.
     * @param
     * @return File
     * @exception
     * @throws
     * @see
     */
    public static File getNamedReference(int mode, TCComponentDataset dataset, String defaultFileName) throws TCException, IOException {
        File template = null;
        TCComponentTcFile[] tcFile = dataset.getTcFiles();
        String fileName = tcFile[0].getProperty("original_file_name");
        String extention = fileName.substring(fileName.lastIndexOf("."));

        if(defaultFileName == null || defaultFileName.equals("")) {
            defaultFileName = fileName;
        }

        if(mode == EXCEL_OPEN) {
            int lastIndex = defaultFileName.lastIndexOf(".");
            if(lastIndex == -1) {
                defaultFileName = defaultFileName + extention;
            } else {
                int length = defaultFileName.length() - extention.length();
                if(!defaultFileName.substring(length).equals(extention)) {
                    defaultFileName = defaultFileName + extention;
                }
            }

            template = new File("C:/Temp/" + defaultFileName);
            if(template.exists()) {
                if(template.delete()) {
                    template = new File("C:/Temp/" + defaultFileName);
                }
            }
        } else if(mode == EXCEL_SAVE) {
            openFileDialog(defaultFileName, extention);

            if(exportPath == null || exportPath.length() == 0) {
                return template;
            }

            template = new File(exportPath);
            if(template.exists()) {
                if(template.delete()) {
                    template = new File(exportPath);
                }
            }
        }
        tcFile[0].getFmsFile().renameTo(template);
        template.setWritable(true);

        return template;
    }

    /**
     * FileDialog Open
     *
     * @method openFileDialog
     * @date 2013. 10. 31.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void openFileDialog(final String defaultFileName, final String extention) {
        parentShell = AIFDesktop.getActiveDesktop().getShell();

        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                FileDialog fileDialog = new FileDialog(parentShell, SWT.SAVE);
                fileDialog.setFileName(defaultFileName);
                fileDialog.setFilterExtensions(new String[]{"*" + extention});
                fileDialog.setOverwrite(true);
                exportPath = fileDialog.open();
            }
        });
    }

    /**
     * Cell Style 가져오기
     * style 정의 후 cellstyles Map 객체에 담는다.
     *
     * @method getCellStyles
     * @date 2013. 10. 31.
     * @param
     * @return Map<String,XSSFCellStyle>
     * @exception
     * @throws
     * @see
     */
    public static Map<String, XSSFCellStyle> getCellStyles(Workbook workbook) {
        Map<String, XSSFCellStyle> cellStyles = new HashMap<String, XSSFCellStyle>();
        XSSFCellStyle style;

        // 가운데 정렬
        style = (XSSFCellStyle) workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        cellStyles.put("center", style);

        // 왼쪽 정렬
        style = (XSSFCellStyle) workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        cellStyles.put("left", style);

        // 오른쪽 정렬
        style = (XSSFCellStyle) workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        cellStyles.put("right", style);

        // Border
        style = (XSSFCellStyle) workbook.createCellStyle();
        style.setBorderTop(XSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        style.setBorderRight(XSSFCellStyle.BORDER_THIN);
        cellStyles.put("border", style);

        style = (XSSFCellStyle) workbook.createCellStyle();
        style.setBorderTop(XSSFCellStyle.BORDER_THIN);
        cellStyles.put("borderTop", style);

        style = (XSSFCellStyle) workbook.createCellStyle();
        style.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        cellStyles.put("borderBottom", style);

        style = (XSSFCellStyle) workbook.createCellStyle();
        style.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        cellStyles.put("borderLeft", style);

        style = (XSSFCellStyle) workbook.createCellStyle();
        style.setBorderRight(XSSFCellStyle.BORDER_THIN);
        cellStyles.put("borderRight", style);

        style = (XSSFCellStyle) workbook.createCellStyle();
        style.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        style.setBorderRight(XSSFCellStyle.BORDER_THIN);
        cellStyles.put("borderLeftRight", style);

        style = (XSSFCellStyle) workbook.createCellStyle();
        style.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        style.setBorderRight(XSSFCellStyle.BORDER_THIN);
        cellStyles.put("borderBottomLeftRight", style);

        style = (XSSFCellStyle) workbook.createCellStyle();
        style.setBorderTop(XSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        style.setBorderRight(XSSFCellStyle.BORDER_THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        cellStyles.put("border_center", style);

        style = (XSSFCellStyle) workbook.createCellStyle();
        style.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        style.setBorderRight(XSSFCellStyle.BORDER_THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        cellStyles.put("borderLeftRight_center", style);

        style = (XSSFCellStyle) workbook.createCellStyle();
        style.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        style.setBorderRight(XSSFCellStyle.BORDER_THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        cellStyles.put("borderBottomLeftRight_center", style);

        style = (XSSFCellStyle) workbook.createCellStyle();
        style.setWrapText(true);
        cellStyles.put("wrapText", style);

        // Excel 출력 일시 cell style
        style = (XSSFCellStyle) workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        style.setFillForegroundColor(new XSSFColor(new byte[]{0, (byte) 204, (byte) 255}));
        cellStyles.put("excelExportDate", style);

        return cellStyles;
    }

    /**
     * 오늘 날짜 가져오기
     *
     * @method getToday
     * @date 2013. 10. 29.
     * @param
     *      format ex) yyyyMMdd, yyyy-MM-dd
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getToday(String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Date date = new Date(System.currentTimeMillis());
        String today = dateFormat.format(date);

        return today;
    }
    
    public static String getDate(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        String today = dateFormat.format(date);

        return today;
    }

}