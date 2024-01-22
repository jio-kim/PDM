// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)

package com.symc.plm.rac.cme.biw.apa;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import com.symc.plm.rac.cme.biw.apa.resulttable.datastructure.ResultData;

class APATableComparator extends ViewerComparator
{

    APATableComparator()
    {
        sortColumn = 0;
        ascending = true;
    }

    @SuppressWarnings({ "unused", "unchecked" })
    public int compare(Viewer viewer, Object obj, Object obj1)
    {
        if((obj instanceof ResultData) && (obj1 instanceof ResultData))
        {
            ResultData resultdata = (ResultData)obj;
            ResultData resultdata1 = (ResultData)obj1;
            String s = null;
            String s1 = null;
            Object obj2 = null;
            try
            {
                String s2 = ResultData.columnIndexToKey(sortColumn);
                s = resultdata.getColumnValue(s2);
                s1 = resultdata1.getColumnValue(s2);
            }
            catch(Exception exception)
            {
                logger.error(exception.getClass().getName(), exception);
            }
            if(s == null)
                s = "";
            if(s1 == null)
                s1 = "";
            int i = getComparator().compare(s, s1);
            return ascending ? i : -1 * i;
        } else
        {
            return super.compare(viewer, obj, obj1);
        }
    }

    public int getSortColumn()
    {
        return sortColumn;
    }

    public void setSortColumn(int i)
    {
        sortColumn = i;
    }

    public boolean isAscending()
    {
        return ascending;
    }

    public void setAscending(boolean flag)
    {
        ascending = flag;
    }

    private int sortColumn;
    private boolean ascending;
    private static final Logger logger = Logger.getLogger(APATableComparator.class);

}
