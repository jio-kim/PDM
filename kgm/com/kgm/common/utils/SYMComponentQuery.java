package com.kgm.common.utils;

import java.util.ArrayList;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

// IMANComponentQuery �� Ȯ���� Ŭ����, attribute name�� TextService�� �̿��Ͽ� ó���� ���
// SavedQuery �̿�� �ڵ����� ó���ϰ� �ϴ� AdapterClass
public class SYMComponentQuery {
    private TCComponentQuery query;
    private String queryName;
    private TCSession session;

    public SYMComponentQuery(TCSession session, String queryName)
            throws TCException {
        TCComponentQueryType queryType = (TCComponentQueryType) session
                .getTypeComponent("ImanQuery");
        query = (TCComponentQuery) queryType.find(queryName);
        this.session = session;
        this.queryName = queryName;

        // System.out.println("=====================================");
        // System.out.println("query name:" + queryName);
        // System.out.println("-------------------------------------");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TCComponent[] execute(String[] entryNames, String[] entryValues)
            throws TCException {
        // �˻����ǿ� �ʵ尪�� ���� ��� entry���� ���ܽ�Ų��.
        ArrayList entryNameList = new ArrayList();
        ArrayList entryValueList = new ArrayList();
        for (int i = 0; i < entryValues.length; i++) {
            if (entryValues[i].length() > 0) {
                entryNameList.add(entryNames[i]);
                entryValueList.add(entryValues[i]);
            }
        }
        entryNames = (String[]) entryNameList.toArray(new String[entryNameList
                .size()]);
        entryValues = (String[]) entryValueList
                .toArray(new String[entryValueList.size()]);

        if (entryNames.length != entryValues.length)
            throw new TCException("wrong number of entry values...");

        // �˻��ʵ���� �ý��ۿ��� ����ϴ� ������ ��ȯ
        for (int i = 0; i < entryNames.length; i++) {
            entryNames[i] = SYMTcUtil.getTextServerString(session,
                    entryNames[i]);
            // System.out.println(entryNames[i] + ":" + entryValues[i]);
        }

        TCComponent[] result = null;
        try {
            result = query.execute(entryNames, entryValues);
        } catch (NullPointerException e) {
            System.out.println("Error: There is no saved query named as '"
                    + queryName + "'");
        }

        return result;
    }
}