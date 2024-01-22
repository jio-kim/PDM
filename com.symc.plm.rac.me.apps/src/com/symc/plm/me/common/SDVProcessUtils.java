package com.symc.plm.me.common;

import java.util.HashMap;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentProcessType;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentTaskTemplateType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class SDVProcessUtils {

    public static final int ATTACH_AS_TARGET = 1;

    public static HashMap<String, TCComponent[]> getSignOffs(HashMap<String, TCComponent[]> signoffMap, TCComponentTask task) throws TCException {
        String type = task.getTaskType();
        if("EPMReviewTask".equals(type)) {
            TCComponent[] signoffs = task.getReferenceListProperty("valid_signoffs");
            if(signoffs != null && signoffs.length > 0) {
                signoffMap.put(task.getName(), signoffs);
            }
        }

        TCComponentTask[] subTasks = task.getSubtasks();
        if(subTasks != null) {
            for(int i = 0; i < subTasks.length; i++) {
                signoffMap = getSignOffs(signoffMap, subTasks[i]);
            }
        }

        return signoffMap;
    }

    public static TCComponent createProcess(String templateName, String description, TCComponent[] targetComps) throws TCException {
        TCComponentTaskTemplateType templateType = (TCComponentTaskTemplateType) getTCSession().getTypeComponent("EPMTaskTemplate");
//        TCComponentTaskTemplate[] templates = templateType.extentReadyTemplates(false);
        TCComponentTaskTemplate[] templates = templateType.getProcessTemplates(false, false, null, null, null);
        
        TCComponent processComponent = null;
        for(int i = 0; i < templates.length; i++) {
            if(templates[i].getName().equals(templateName)) {
                TCComponentProcessType processType = (TCComponentProcessType) getTCSession().getTypeComponent("Job");
                processComponent = processType.create(templateName, description, templates[i], targetComps, getProcessTargetInt(targetComps));
                break;
            }
        }
        return processComponent;
    }

    public static TCComponentTaskTemplate getTaskTemplate(String templateName) throws TCException {
        TCComponentTaskTemplateType templateType = (TCComponentTaskTemplateType) getTCSession().getTypeComponent("EPMTaskTemplate");
//        TCComponentTaskTemplate[] templates = templateType.extentReadyTemplates(false);
        TCComponentTaskTemplate[] templates = templateType.getProcessTemplates(false, false, null, null, null);
        
        TCComponentTaskTemplate template = null;
        for(int i = 0; i < templates.length; i++) {
            if(templates[i].getName().equals(templateName)) {
                template = templates[i];
                break;
            }
        }
        return template;
    }

    public static int[] getProcessTargetInt(TCComponent[] targetComps) {
        int[] targetInt = new int[targetComps.length];
        for(int i = 0; i < targetComps.length; i++) {
            targetInt[i] = ATTACH_AS_TARGET;
        }

        return targetInt;
    }

    public static TCSession getTCSession() {
        return (TCSession) AIFUtility.getSessionManager().getDefaultSession();
    }
}
