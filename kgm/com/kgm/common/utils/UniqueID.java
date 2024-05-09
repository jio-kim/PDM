package com.kgm.common.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.teamcenter.rac.common.create.BOCreateDefinitionFactory;
import com.teamcenter.rac.common.create.CreateInstanceInput;
import com.teamcenter.rac.common.create.IBOCreateDefinition;
import com.teamcenter.rac.common.create.SOAGenericCreateHelper;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentContextList;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

@SuppressWarnings({"unused", "rawtypes", "unchecked"})
public class UniqueID {
    static public String getDayID(TCSession pTCSession, String prefix, String subfix, int digit) throws TCException
    {
        String ymd = "";
        Date date = new Date();
        SimpleDateFormat fmt = new SimpleDateFormat("yyMMdd");
        ymd = fmt.format(date);
        return getID(pTCSession, prefix+ymd+subfix, digit);
    }

    static public String getID(TCSession pTCSession, String prefix, int digit) throws TCException
    {
        int seq = 1;
        TCComponentQueryType tccomponentquerytype = (TCComponentQueryType)pTCSession.getTypeComponent("ImanQuery");
        TCComponentQuery tccomponentquery = (TCComponentQuery)tccomponentquerytype.find("STCUniqueID");
        String props[] = { "STC_prefix" };
        String values[] = { prefix };
        TCComponentContextList tccomponentcontextlist = tccomponentquery.getExecuteResultsList(props, values);
        TCComponent atccomponent[] = tccomponentcontextlist.toTCComponentArray();
        System.out.println("Result : "+atccomponent.length);
        if ( atccomponent.length > 0 ) {
            TCComponent uniqueID = atccomponent[0];
            uniqueID.lock();
            //seq = Integer.parseInt(uniqueID.getStringProperty("STC_seq"));
            seq = uniqueID.getIntProperty("STC_seq");
            seq++;
            uniqueID.setIntProperty("STC_seq", seq);
            uniqueID.save();
            uniqueID.unlock();
        }
        else {
            TCComponent localTCComponent = createUniqueID(pTCSession, prefix, seq);
        }
        String sSeq = new String("0000000000"+seq);
        return prefix+sSeq.substring(sSeq.length()-digit);
    }
    
    // Create Business Object Using SOA Create Helper
    static TCComponent createUniqueID(TCSession pTCSession, String prefix, int seq) throws TCException
    {
        IBOCreateDefinition createDefinition = BOCreateDefinitionFactory.getInstance().getCreateDefinition(pTCSession, "STCUniqueID");
        //unfilledRequiredList = getRequiredFields();
        CreateInstanceInput createInput = new CreateInstanceInput(createDefinition);
        createInput.add("STC_prefix", prefix);
        createInput.add("STC_seq", seq);
        ArrayList list = new ArrayList();
        list.add(createInput);
        TCComponent localTCComponent = null;
        List localList = SOAGenericCreateHelper.create(pTCSession, createDefinition, list );
        if ((localList != null) && (localList.size() > 0))
          localTCComponent = (TCComponent)localList.get(0);
        return localTCComponent;
    }
}
