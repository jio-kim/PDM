// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)

package com.symc.plm.rac.cme.biw.apa;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import com.symc.plm.rac.cme.biw.apa.resulttable.datastructure.PartData;
import com.symc.plm.rac.cme.biw.apa.resulttable.datastructure.ResultData;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.common.TCTypeRenderer;

public class APAColumnLabelProvider extends ColumnLabelProvider
{

    public APAColumnLabelProvider(int i)
    {
        try
        {
            key = ResultData.columnIndexToKey(i);
        }
        catch(Exception exception)
        {
            logger.error(exception.getClass().getName(), exception);
        }
    }

    public Color getForeground(Object obj)
    {
        IWorkbenchWindow iworkbenchwindow = AIFDesktop.getActiveDesktop().getDesktopWindow();
        if(iworkbenchwindow != null)
        {
            Shell shell = iworkbenchwindow.getShell();
            Display display = shell.getDisplay();
            if(obj instanceof ResultData)
            {
                ResultData resultdata = (ResultData)obj;
                Object obj1 = resultdata.getColumnObjectValue(key);
                if(obj1 instanceof PartData)
                {
                    PartData partdata = (PartData)obj1;
                    return getPartColor(partdata, display);
                }
            }
        }
        return null;
    }

    private Color getPartColor(PartData partdata, Display display)
    {
        boolean flag = partdata.isDoConnection();
        boolean flag1 = partdata.isSuggested();
        if(flag && flag1)
            return display.getSystemColor(2);
        if(flag && !flag1)
            return display.getSystemColor(6);
        else
            return display.getSystemColor(15);
    }

    public String getText(Object obj)
    {
        if(obj instanceof ResultData)
            return internalGetText(((ResultData)obj).getColumnValue(key));
        else
            return null;
    }

    public Font getFont(Object obj)
    {
        return internalGetFont(obj);
    }

    private Font internalGetFont(Object obj)
    {
        IWorkbenchWindow iworkbenchwindow = AIFDesktop.getActiveDesktop().getDesktopWindow();
        if(iworkbenchwindow != null)
        {
            Shell shell = iworkbenchwindow.getShell();
            Display display = shell.getDisplay();
            if(obj instanceof ResultData)
            {
                ResultData resultdata = (ResultData)obj;
                Object obj1 = resultdata.getColumnObjectValue(key);
                if(obj1 instanceof PartData)
                {
                    PartData partdata = (PartData)obj1;
                    return getPartFont(partdata, display);
                }
            }
        }
        return null;
    }

    private Font getPartFont(PartData partdata, Display display)
    {
        boolean flag = partdata.isDoConnection();
        boolean flag1 = partdata.isSuggested();
        if(JFaceResources.getFontRegistry().get("APAFont") == null)
        {
            Font font = display.getSystemFont();
            org.eclipse.swt.graphics.FontData afontdata[] = font.getFontData();
            JFaceResources.getFontRegistry().put("APAFont", afontdata);
        }
        if(flag && flag1)
            return JFaceResources.getFontRegistry().get("APAFont");
        if(flag && !flag1)
            return JFaceResources.getFontRegistry().getBold("APAFont");
        else
            return JFaceResources.getFontRegistry().getItalic("APAFont");
    }

    private String internalGetText(String s)
    {
        return super.getText(s);
    }

    public Image getImage(Object obj)
    {
        if(obj instanceof ResultData)
            return internalGetImage(((ResultData)obj).getRowContextObject(key));
        else
            return null;
    }

    private Image internalGetImage(Object obj)
    {
        return TCTypeRenderer.getImage(obj);
    }

    private String key;
    private static final Logger logger = Logger.getLogger(APAColumnLabelProvider.class);

}
