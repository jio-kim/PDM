/**
 * 
 */
package com.symc.plm.me.sdv.service.migration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.sdv.core.common.exception.ValidateSDVException;
import org.springframework.util.StringUtils;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.symc.plm.me.sdv.service.migration.util.FileUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.pse.variants.modularvariants.MVLLexer;

/**
 * Class Name : ImportCoreService
 * Class Description :
 * 
 * @date 2013. 11. 14.
 * 
 */
public class ImportCoreService {

    /**
     * TCData(Tree Item)을 동기적(Synch)으로 상태 변경 및 메세지 출력
     * 
     * @method syncItemState
     * @date 2013. 11. 20.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void syncItemState(Shell shell, final TCData treeItem, final int nStatus, final String strMessage) {
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                if (treeItem == null) {
                    return;
                }
                if (strMessage == null) {
                    treeItem.setStatus(nStatus);
                } else {
                    treeItem.setStatus(nStatus, strMessage);
                }
            }
        });
    }

    /**
     * TCData(Tree Item)을 동기적(Synch)으로 Selection
     * 
     * @method syncSetItemSelection
     * @date 2013. 11. 20.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void syncSetItemSelection(Shell shell, final Tree tree, final TCData tcData) {
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                tree.setSelection(tcData);
            }
        });
    }

    /**
     * Job과 UI Thread간의 충돌을 피해 UI Update
     * 
     * @param treeItem
     */
    public static void syncSetItemTextField(Shell shell, final Text text, final String strText) {
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                text.append(strText + "\n");
            }
        });
    }

    /**
     * Tree를 가지고 하위 1Level 자식 Item을 가져온다.
     * 
     * @method syncGetChildItem
     * @date 2013. 11. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void syncGetChildItem(Shell shell, final Tree tree, final ArrayList<TCData> itemList) {
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                TreeItem[] childItems = tree.getItems();
                for (int i = 0; i < childItems.length; i++) {
                    itemList.add((TCData) childItems[i]);
                }
            }
        });
    }

    /**
     * Item을 가지고 하위 1Level 자식 Item을 가져온다.
     * 
     * @method syncGetItems
     * @date 2013. 11. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void syncGetItems(Shell shell, final TCData treeItem, final ArrayList<TCData> itemList) {
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                TreeItem[] childItems = treeItem.getItems();
                for (int i = 0; i < childItems.length; i++) {
                    itemList.add((TCData) childItems[i]);
                }
            }
        });
    }

    /**
     * TreeItem(TCData)의 Item Text를 가져온다.
     * 
     * @method syncGetItemText
     * @date 2013. 11. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void syncGetItemText(Shell shell, final TCData treeItem, final int nCulumn, final StringBuffer szBuf) {
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                szBuf.append(treeItem.getText(nCulumn));
            }
        });
    }

    /**
     * Tree의 Item을 전체 삭제한다.
     * 
     * @method syncRemoveAllTreeItems
     * @date 2013. 11. 22.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void syncRemoveAllTreeItems(Shell shell, final Tree tree) {
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                tree.removeAll();
            }
        });
    }

    /**
     * Log 파일을 저장한다.
     * 
     * @method saveLogFile
     * @date 2013. 11. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void saveLogFile(String logFilePath, String logMessage) throws Exception {
        if (StringUtils.isEmpty(logMessage)) {
            return;
        }
        // \n 구문을 \r\n 개행문자로 변경
        logMessage = logMessage.replaceAll("\n", "\r\n");
        // Log 파일 처리..
        FileUtil.appandFileText(logFilePath, logMessage);
    }

    /**
     * Log Text와 File을 동시 출력
     * 
     * @method saveLogFile
     * @date 2013. 11. 29.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void saveLogFile(String logFilePath, Text logText, String logMessage) throws Exception {
        // Log Text 처리.
        logText.append(logMessage + "\n");
        // Log 파일 처리..
        saveLogFile(logFilePath, logMessage);
    }

    /**
     * Exception을 String으로 찍는다.
     * 
     * @method getStackTraceString
     * @date 2013. 11. 28.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getStackTraceString(Exception ex) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream pinrtStream = new PrintStream(out);
        ex.printStackTrace(pinrtStream);
        return out.toString();
    }

    /**
     * Option Condition String을 TC에 맞게 변환한다.
     * 
     * H10S+H10E
     * -->
     * 'PTP-A1-PVXA2016':H10 = "H10S" or 'PTP-A1-PVXA2016':H10 = "H10E"
     * 
     * @method conversionOptionCondition
     * @date 2013. 12. 10.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String conversionOptionCondition(TCComponentBOMWindow bomwindow, String condition) throws Exception {
        if (StringUtils.isEmpty(condition)) {
            return "";
        }
        String topItemID = bomwindow.getTopBOMLine().getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
        Pattern p = Pattern.compile("AND|@");
        Matcher m = p.matcher(condition);
        ArrayList<String> options = new ArrayList<String>();
        int dividerCount = 0;
        String str = "";
        while (m.find()) {
            str = condition.substring(dividerCount, m.start());
            // StringUtils.startsWithIgnoreCase(condition.substring(13, m.start()), "AND");
            options.add(str.trim());
            options.add(m.group());
            if ("AND".equals(m.group())) {
                dividerCount = m.start() + 3;
            } else {
                dividerCount = m.start() + 1;
            }
        }
        if (condition.length() > dividerCount) {
            str = condition.substring(dividerCount, condition.length());
            options.add(str.trim());
        }
        StringBuffer optionBuffer = new StringBuffer();
        for (String string : options) {
            if ("AND".equals(string)) {
                optionBuffer.append(" and ");
            } else if ("@".equals(string)) {
                optionBuffer.append(" or ");
            } else {
                optionBuffer.append(MVLLexer.mvlQuoteId(topItemID, true) + ":" + MVLLexer.mvlQuoteId(string.substring(0, string.length() - 1), false) + " = " + "\"" + string + "\"");
            }
        }
        return optionBuffer.toString();
    }

    /**
     * Option Condition String을 TC에서 PE 맞게 변환한다.
     * 
     * 'PTP-A1-PVXA2016':H10 = "H10S" or 'PTP-A1-PVXA2016':H10 = "H10E"
     * -->
     * H10S+H10E
     * 
     * @method conversionOptionConditionFormTC
     * @date 2013. 12. 24.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String conversionOptionConditionFormTC(String condition) throws Exception {
        if (StringUtils.isEmpty(condition)) {
            return "";
        }
        Pattern p = Pattern.compile("or|and");
        Matcher m = p.matcher(condition);
        ArrayList<String> options = new ArrayList<String>();
        int dividerCount = 0;
        String str = "";
        while (m.find()) {
            str = condition.substring(dividerCount, m.start());
            options.add(str.trim());
            options.add(m.group().trim());
            dividerCount = m.start() + m.group().length();
        }
        if (condition.length() > dividerCount) {
            str = condition.substring(dividerCount, condition.length());
            options.add(str.trim());
        }
        StringBuffer optionBuffer = new StringBuffer();
        for (String string : options) {
            if ("and".equals(string)) {
                optionBuffer.append(" AND ");
            } else if ("or".equals(string)) {
                optionBuffer.append("@");
            } else {
                optionBuffer.append(string.split("=")[1].replace("\"", "").trim());
            }
        }
        return optionBuffer.toString();
    }

    /**
     * Activity Category를 LOV로 변환한다.
     * 
     * @method getActivityCategolyLOV
     * @date 2013. 12. 10.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getPEActivityCategolyLOV(String category) {
        if ("정미".equals(category)) {
            category = "01";
        } else if ("자동".equals(category)) {
            category = "02";
        } else if ("보조".equals(category)) {
            category = "03";
        } else {
            category = "";
        }
        return category;
    }

    /**
     * Activity Category를 LOV를 String으로 변환한다.
     * 
     * @method getActivityCategolyLOVToString
     * @date 2014. 1. 2.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getActivityCategolyLOVToString(String categoryLov) {
        if ("01".equals(categoryLov)) {
            categoryLov = "작업자정미";
        } else if ("02".equals(categoryLov)) {
            categoryLov = "자동";
        } else if ("03".equals(categoryLov)) {
            categoryLov = "보조";
        } else {
            categoryLov = "";
        }
        return categoryLov;
    }

    /**
     * 로그 처리..
     * 
     * @method saveLog
     * @date 2013. 12. 11.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void saveLog(Shell shell, final TCData tcData, final Text text, final String logFilePath, final String logMessage, final boolean outputClassType) throws Exception {
        final ArrayList<Exception> exception = new ArrayList<Exception>();
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                // Class Type log 출력
                String outMessage = (outputClassType) ? "<" + tcData.getClassType() + ">\t\t\t" + logMessage : logMessage;
                // Log Text 출력
                text.append(outMessage + "\n");
                try {
                    ImportCoreService.saveLogFile(logFilePath, outMessage);
                } catch (Exception e) {
                    exception.add(e);
                }
            }
        });
        if (exception.size() > 0) {
            throw exception.get(0);
        }
    }

    /**
     * 파일 Path로 File을 가져온다.
     * 
     * @method fileCheck
     * @date 2013. 12. 11.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    public static File getPathFile(String filePath) throws Exception {
        File file = new File(filePath);
        if (file.exists()) {
            return file;
        }
        return null;
    }

    /**
     * SEQ No.를 가지고 Find No.를 생성한다.
     * 
     * - 기본 자리수를 3자리(001, 056, 201 ...)로 고정되어있으므로 3자리 이상이면 로직을 검토할 것
     * 
     * 
     * 주의:
     * -> 현재 100번 이하 0붙이는 로직 제거함. (1 -> 001 사용하지 않음) -> 1, 10, 100 ,200 이런 식으로 출력
     * 
     * @method getFindNoFromSeq
     * @date 2013. 12. 20.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getFindNoFromSeq(String seq) {
        String findNo = "";
        if (StringUtils.isEmpty(seq)) {
            return findNo;
        }
        int seqNo = Integer.parseInt(seq);
        // if (seqNo < 10) {
        // findNo = "00" + seqNo;
        // } else if (seqNo < 100) {
        // findNo = "0" + seqNo;
        // } else {
        findNo = "" + seqNo;
        // }
        return findNo;
    }

    /**
     * 부자재 Find No.를 가져온다. (Import) (a=>510 ~ z=>760)
     * (a=510 ~ z=760)
     * 
     * @method conversionSubsidiaryFindNo
     * @date 2014. 1. 6.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String conversionSubsidiaryFindNo(String seqNo) {
        if (StringUtils.isEmpty(seqNo)) {
            return "";
        }
        // trim, lowerCase
        seqNo = seqNo.trim().toLowerCase();
        if (seqNo.length() != 1) {
            return seqNo;
        }
        int charCode = seqNo.charAt(0);
        // a=97 ~ z=122 사이의 SeqNo 를 Find No.로 변경
        if (charCode >= 97 && charCode <= 122) {
            int findNo = charCode - 97;// a=0 ~ z=25
            findNo = findNo + 1;// a=1 ~ z=26
            findNo = findNo * 10; // a=10 ~ z=260
            findNo = findNo + 500;// a=510 ~ z=760
            return ImportCoreService.getFindNoFromSeq(findNo + "");
        }
        return seqNo;
    }

    /**
     * 부자재 Find No.를 가져온다. (Export) (510=>a ~ 760=>z)
     * 
     * @method conversionSubsidiaryFindNoToTc
     * @date 2014. 1. 29.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String conversionSubsidiaryFindNoToTc(String findNo) {
        if (StringUtils.isEmpty(findNo)) {
            return "";
        }
        // trim, lowerCase
        findNo = findNo.trim();
        try {
            int number = Integer.parseInt(findNo);
            if (number >= 510 && number <= 760) {
                if (number % 10 != 0) {
                    return findNo;
                }
                number = number / 10; // 510(a) -> 51
                number = number - 51; // 51(a) -> 0
                number = number + 97;// a=97 ~ z=122
                return new String(Character.toChars(number));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return findNo;
        }
        return findNo;
    }

    /**
     * MECO No.를 가지고 Revision을 알아낸다.
     * 
     * @method getMecoRevision
     * @date 2014. 1. 14.
     * @param
     * @return TCComponentItemRevision
     * @exception
     * @throws
     * @see
     */
    public static TCComponentItemRevision getMecoRevision(String mecoNo) throws Exception {
        TCComponentItemRevision mecoRevision = SYMTcUtil.getLatestedRevItem(mecoNo);
        if (mecoRevision == null) {
            throw new ValidateSDVException("MECO Revision 정보가 없습니다.");
        }
        return mecoRevision;
    }

}
