// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package com.ssangyong.commands.namedreferences;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.kernel.TCComponentDataset;
import java.awt.Dialog;
import java.awt.Frame;

// Referenced classes of package com.teamcenter.rac.commands.namedreferences:
//            NamedReferencesDialog

public class SYMCNamedReferencesCommand extends AbstractAIFCommand
{

    public SYMCNamedReferencesCommand(TCComponentDataset tccomponentdataset, Frame frame)
    {
        setRunnable(new SYMCNamedReferencesDialog(tccomponentdataset, frame));
    }

    public SYMCNamedReferencesCommand(TCComponentDataset tccomponentdataset, Dialog dialog)
    {
        if(dialog == null)
            setRunnable(new SYMCNamedReferencesDialog(tccomponentdataset));
        else
            setRunnable(new SYMCNamedReferencesDialog(tccomponentdataset, dialog));
    }

    public SYMCNamedReferencesCommand(TCComponentDataset tccomponentdataset)
    {
        setRunnable(new SYMCNamedReferencesDialog(tccomponentdataset));
    }
}
