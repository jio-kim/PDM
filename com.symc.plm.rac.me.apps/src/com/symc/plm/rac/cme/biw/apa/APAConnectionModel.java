// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)

package com.symc.plm.rac.cme.biw.apa;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.rac.cme.biw.apa.resulttable.datastructure.PartData;
import com.symc.plm.rac.cme.biw.apa.resulttable.datastructure.ResultData;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

// Referenced classes of package com.teamcenter.rac.cme.biw.apa:
//            APADialog, APAContentProvider

/**
 * [NON-SR][20150703] shcho, 검색시 BOP Line에서 VehiclePart를 찾아오던 것을, Accept시 찾도록 변경 (검색이 오래 걸리는 문제제기로 인하여 변경 - 정윤재 수석 지시사항임)
 * [NON-SR][20150708] shcho, 용접점 ConnectedPart 속도 및 오류 개선 1) VehiclePart BOP Line 사용하던것을 VehiclePart Revision을 바로 사용하도록  변경. 2) 저장 위치(속성)를 bl_connected_lines_tags 에서 m7_CONNECTED_PART 로 변경
 * 
 */
public class APAConnectionModel
{

    @SuppressWarnings("rawtypes")
    public APAConnectionModel(APAContentProvider apacontentprovider, java.util.List list)
    {
        rowsData = null;
//        notAssignedMfg = null;
        notAssignedMfgCnt = 0;
        contentProvider = apacontentprovider;
        rowsData = list;
    }

    @SuppressWarnings({ "rawtypes", "unused" })
    public void handleAssign(boolean flag)
    {
        notAssignedMfgCnt = 0;
        ResultData aresultdata[] = null;
        aresultdata = getContentProvider().getDisplayData();
        int i = aresultdata.length;
        if(flag)
        {
            Object obj = null;
            if(aresultdata != null)
            {
                for(int j = 0; j < i; j++)
                {
                    ResultData resultdata = aresultdata[j];
                    try
                    {
                        processAssign(resultdata);
                    }
                    catch(Exception exception)
                    {
                        exception.printStackTrace();
                    }
                }

            }
        } else
        {
            Iterator iterator = rowsData.iterator();
            do
            {
                if(!iterator.hasNext())
                    break;
                Object obj1 = iterator.next();
                if(obj1 instanceof ResultData)
                    try
                    {
                        processAssign((ResultData)obj1);
                    }
                    catch(Exception exception1)
                    {
                        exception1.printStackTrace();
                    }
            } while(true);
        }
    }

    private APAContentProvider getContentProvider()
    {
        return contentProvider;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void processAssign(ResultData resultdata)
        throws Exception
    {
        TCComponent tcCompWeldPoint = null;
        Hashtable hashtable = null;
        Hashtable hashtable1 = null;
        hashtable = resultdata.getRowContext();
        hashtable1 = resultdata.getColumnValues();
        TCComponentBOMLine weldPointBOMLine = null;
        
        Enumeration hashKey1 = hashtable.keys();
        int keyIndexA = 0;
        while (hashKey1!=null && hashKey1.hasMoreElements()) {
			Object object = (Object)hashKey1.nextElement();
			System.out.println("hashKey1["+keyIndexA+"] = "+object);
			keyIndexA++;
		}
        Enumeration hashKey2 = hashtable1.keys();
        int keyIndexB = 0;
        while (hashKey2!=null && hashKey2.hasMoreElements()) {
			Object object = (Object)hashKey2.nextElement();
			System.out.println("hashKey2["+keyIndexB+"] = "+object);
			keyIndexB++;
		}
        
        try {
            int i = hashtable1.size() - 3;
            tcCompWeldPoint = (TCComponent)hashtable.get("MFGName");
           
            if (i > 0) {
                PartData arrPartData[] = new PartData[i];
                TCComponent arrTCComponent[] = new TCComponent[i];
                ArrayList<TCComponent> connectTCComponentList = new ArrayList<TCComponent>();

                for (int k = 0; k < i; k++) {

                    String key = (new StringBuilder()).append("Part").append(k + 1).toString();
                    arrPartData[k] = (PartData) hashtable1.get(key);
                    arrTCComponent[k] = (TCComponent) hashtable.get(key);

                    // 여기 이부분 Null Point Exception 난다.
                    boolean connectFlag = false;
                    
                    if(arrPartData[k]!=null){
                    	connectFlag = arrPartData[k].isDoConnection();
                    	if (connectFlag) {
                    		connectTCComponentList.add(arrTCComponent[k]);
                    	} else {
                    		arrPartData[k].setRemovalCandidate(true);
                    	}
                    	
                    	arrPartData[k].setSuggested(false);
                    	hashtable1.put(key, arrPartData[k]);
                    }

                }

                /** BOPLine 속성 Update **/
                weldPointBOMLine = (TCComponentBOMLine) tcCompWeldPoint;
                StringBuffer strBuff = new StringBuffer();
                int j = 0;
                for (TCComponent connectedPart : connectTCComponentList) {
                    if (j > 0) {
                        strBuff.append(",");
                    }
                    strBuff.append(connectedPart.getStringProperty(SDVPropertyConstant.ITEM_ITEM_ID));
                    strBuff.append("/");
                    strBuff.append(connectedPart.getStringProperty(SDVPropertyConstant.ITEM_REVISION_ID));
                    j++;
                }
                String connectedPartNoteValue = strBuff.toString();
                weldPointBOMLine.setProperty(SDVPropertyConstant.BL_CONNECTED_PARTS, connectedPartNoteValue);

                /** Dialog 갱신 **/
                resultdata.setColumnValues(hashtable1); // Partdata만 갱신하면 됨.
                // resultdata.setRowContexts(hashtable);
            }
        
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(),  weldPointBOMLine.toDisplayString() + " \n "+ e.getMessage(), "ERROR", MessageBox.ERROR);
        }
    }

    /**
     * Connected Part 검색시 BomLine 의 LastModifiedDate 의 날짜가 업데이트가 되지 않아서
     * 날짜 컬럼을 추가해서 업데이트 시킴
     */
    public void checkLastModifidate(TCComponentBOMLine tccomponentBomline){
        try {
            if (weldOPRevison == null || !weldOPRevison.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID).equals(tccomponentBomline.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID))) {
                Date date = new Date();
                weldOPRevison = tccomponentBomline;
                weldOPRevison.getItemRevision().setDateProperty(SDVPropertyConstant.WELDOP_REV_LAST_MOD_DATE, date);
            }
        } catch (TCException e) {
            e.printStackTrace();
        }
    }
    
/*
    @SuppressWarnings("unused")
    private boolean makeSuggestedPartsConnection(int i, PartData apartdata[], TCComponent atccomponent[], TCComponentBOMLine tccomponentbomline)
    {
        TCComponentBOMLine atccomponentbomline[] = new TCComponentBOMLine[2];
        atccomponentbomline[1] = tccomponentbomline;
        for(int j = 0; j < i; j++)
        {
            boolean flag = apartdata[j].isDoConnection();
            boolean flag1 = apartdata[j].isSuggested();
            if(!flag || !flag1)
                continue;
            atccomponentbomline[0] = (TCComponentBOMLine)atccomponent[j];
            if(addToConnection(atccomponentbomline))
                apartdata[j].setSuggested(false);
            else
                return false;
        }

        return true;
    }

    @SuppressWarnings({ "rawtypes", "unchecked", "unused"})
    private boolean addToConnection(TCComponentBOMLine atccomponentbomline[])
    {
        TCComponentBOMLine atccomponentbomline2[];
        int i;
        int j;
        Frame frame = AIFUtility.getActiveDesktop().getFrame();
        ConnectionOperation connectionoperation = new ConnectionOperation(frame, atccomponentbomline);
        connectionoperation.executeOperation();
        try
        {
            TCComponentBOMLine atccomponentbomline1[] = atccomponentbomline[1].listConnectedPorts();
            atccomponentbomline2 = atccomponentbomline1;
            i = atccomponentbomline2.length;
            for (j = 0;j < i; j++)
            {
                TCComponentBOMLine tccomponentbomline = atccomponentbomline2[j];
                if(tccomponentbomline.equals(atccomponentbomline[0])){
                    checkLastModifidate(atccomponentbomline[1].parent());
                    return true;
                }
            }
        }catch(Exception ex)
        {
            ex.printStackTrace();
        }
        ArrayList arraylist = new ArrayList();
        arraylist.add(atccomponentbomline[1].toString());
        arraylist.add(atccomponentbomline[0].toString());
        notAssignedMfg.add(arraylist);
        notAssignedMfgCnt++;

        return false;
    }

    
    @SuppressWarnings("unused")
    private boolean makeDesiredPartsConnection(int i, PartData apartdata[], TCComponent atccomponent[], TCComponentBOMLine tccomponentbomline)
    {
        TCComponentBOMLine atccomponentbomline[] = new TCComponentBOMLine[2];
        try {
            checkLastModifidate(tccomponentbomline.parent());
        } catch (TCException e) {
            e.printStackTrace();
        }
        atccomponentbomline[1] = tccomponentbomline;
        for(int j = 0; j < i; j++)
        {
            apartdata[j].setRemovalCandidate(false);
            boolean flag = apartdata[j].isDoConnection();
            if(!flag)
                continue;
            atccomponentbomline[0] = (TCComponentBOMLine)atccomponent[j];
            if(addToConnection(atccomponentbomline))
                apartdata[j].setSuggested(false);
            else
                return false;
            apartdata[j].setRemovalCandidate(false);
        }

        return true;
    }

    @SuppressWarnings("unused")
    private boolean removeFromConnection(TCComponentBOMLine tccomponentbomline)
    {
        TCComponentBOMLine atccomponentbomline[] = new TCComponentBOMLine[1];
        atccomponentbomline[0] = tccomponentbomline;
        try
        {
            Frame frame = AIFUtility.getActiveDesktop().getFrame();
            DisconnectOperation disconnectoperation = new DisconnectOperation(frame, atccomponentbomline);
            disconnectoperation.executeOperation();
            atccomponentbomline = tccomponentbomline.listConnectedPorts();
            if(atccomponentbomline == null || atccomponentbomline.length == 0)
                return true;
        }
        catch(Exception exception)
        {
            System.out.println(exception.getMessage());
        }
        return false;
    }

    @SuppressWarnings("unused")
    private boolean isAskedforDisconnectionofConnectedPart(int i, PartData apartdata[])
    {
        for(int j = 0; j < i; j++)
        {
            boolean flag = apartdata[j].isDoConnection();
            boolean flag1 = apartdata[j].isSuggested();
            if(!flag && !flag1)
                return true;
        }

        return false;
    }

    @SuppressWarnings("unused")
    private boolean isAnyPartConnected(TCComponent tccomponent)
        throws TCException
    {
        TCProperty tcproperty = ((TCComponentBOMLine)tccomponent).getTCProperty("bl_connected_lines_tags");
        TCComponent atccomponent[] = tcproperty.getReferenceValueArray();
        return atccomponent.length > 0;
    }

    @SuppressWarnings("rawtypes")
    public void handleUnAssigned()
    {
        if(notAssignedMfgCnt > 0)
        {
            Object aobj[][] = new Object[notAssignedMfgCnt][3];
            for(int i = 0; i < notAssignedMfgCnt; i++)
            {
                ArrayList arraylist = (ArrayList)notAssignedMfg.get(i);
                aobj[i][0] = arraylist.get(0);
                aobj[i][1] = arraylist.get(1);
                aobj[i][2] = arraylist.get(2);
            }

            String as[] = {
                "MFG Name", "Number of Sheet", "Part Name"
            };
            Frame frame = AIFUtility.getActiveDesktop().getFrame();
            Registry registry1 = getReg();
            PropertyTablePrintDialog propertytableprintdialog = new PropertyTablePrintDialog(frame, new JTable(aobj, as), registry1.getString("APAConnectionDialog.errMsg"));
            propertytableprintdialog.setTitle(getReg().getString("APADialog.Print_Title"));
            propertytableprintdialog.setVisible(true);
        }
    }
*/
    protected Registry getReg()
    {
        if(registry == null)
            registry = Registry.getRegistry(APADialog.class);
        return registry;
    }

    private APAContentProvider contentProvider;
    @SuppressWarnings("rawtypes")
    private java.util.List rowsData;
    @SuppressWarnings("rawtypes")
//    private ArrayList notAssignedMfg;
    private int notAssignedMfgCnt;
    private Registry registry;
    private TCComponentBOMLine weldOPRevison;
}
