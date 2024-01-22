// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)

package com.symc.plm.rac.cme.biw.apa.resulttable.datastructure;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

@SuppressWarnings("rawtypes")
public class PartData
    implements Comparable
{

    public PartData(TCComponent tccomponent, boolean flag, boolean flag1)
    {
        suggested = false;
        removalCandidate = false;
        TCComponent partComp = tccomponent;
        //String sheetNumber = "";
        // jwlee 원래 소스
        // 기존에 name 이 안나오던 부분을 name 까지 표시 하기 위해 ItemRevision 으로 변환 후 name 까지 표시
        //setName(tccomponent.toString());
        try {
            if(tccomponent.isTypeOf("BOMLine")){
                TCComponentItemRevision itemRev = ((TCComponentBOMLine)tccomponent).getItemRevision();
                partComp = itemRev;
            }
        } catch (TCException e) {
            e.printStackTrace();
        }
        setName(partComp.toString());
        setDoConnection(flag);
        setSuggested(flag1);
    }

    public boolean isDoConnection()
    {
        return doConnection;
    }

    public boolean isSuggested()
    {
        return suggested;
    }

    public String getName()
    {
        return name;
    }

    public String getNumberOfSheet()
    {
        return NumberOfSheet;
    }

    public void setDoConnection(boolean flag)
    {
        doConnection = flag;
        if(!isSuggested() && !flag)
            setRemovalCandidate(true);
    }

    public void setRemovalCandidate(boolean flag)
    {
        removalCandidate = flag;
    }

    public boolean isRemovalCandidate()
    {
        return removalCandidate;
    }

    public void setSuggested(boolean flag)
    {
        suggested = flag;
    }

    public void setName(String s)
    {
        name = s;
    }

    public void setNumberOfSheet(String s)
    {
        NumberOfSheet = s;
    }

    public String toString()
    {
        return getName();
    }

    public int compareTo(Object obj)
    {
        int i = 1;
        PartData partdata = (PartData)obj;
        if(getName() != null && partdata.getName() != null)
            i = getName().compareTo(partdata.getName());
        else
        if(getName().equals(partdata.getName()))
            i = 0;
        else
        if(getName() == null)
            i = -1;
        else
            i = 1;
        return i;
    }

    public int hashCode()
    {
        if(getName() == null)
            return super.hashCode();
        else
            return getName().hashCode();
    }

    public boolean equals(Object obj)
    {
        if(!(obj instanceof PartData))
            return false;
        if(getName() == null)
            return false;
        else
            return getName().equals(((PartData)obj).getName());
    }

    private String name;
    private String NumberOfSheet;
    private boolean doConnection;
    private boolean suggested;
    private boolean removalCandidate;
}
