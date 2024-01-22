package com.symc.plm.me.sdv.handler;

import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.sdv.core.common.ISDVValidator;
import org.sdv.core.util.SDVSpringContextUtil;

import com.symc.plm.me.sdv.handler.validators.ForbiddenSDVActionValitator;
import com.teamcenter.rac.aif.common.actions.AbstractAIFAction;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.handlers.LegacyHandler;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class ValidatableLegacyAction extends AbstractHandler implements IExecutableExtension {

    public static final String DEFAULT_PARAMETER_COMMANDID = "COMMAND_ID";

    private Registry m_registry;
    private String m_actionCommand;
    private ISDVValidator validator;

    public void setInitializationData(IConfigurationElement paramIConfigurationElement, String commandId, Object paramObj) throws CoreException {
        String[] commandParameters = paramObj.toString().split(":");
        try {
            if (commandParameters.length > 2) {
                this.m_actionCommand = commandParameters[0];
                this.m_registry = Registry.getRegistry(commandParameters[1]);
            } else {
                this.m_actionCommand = "";
                this.m_registry = Registry.getRegistry("com.teamcenter.rac.common.common");
            }

            if(commandParameters.length > 2){
                validator = getValidator(commandParameters[2]);
            }

        } catch (MissingResourceException mrex) {
            Logger.getLogger(ValidatableLegacyAction.class).error(paramObj, mrex);
        }
    }


    protected ISDVValidator getValidator(String validatorBeanId){

        try{
            if (validatorBeanId != null && validatorBeanId.trim().length() > 0) {
                return (ISDVValidator)SDVSpringContextUtil.getBean(validatorBeanId);
            }
//            Class validatorClass = Class.forName(validatorClassName);
//            Constructor  validatorConst = validatorClass.getConstructor();
//            return (ILegacyActionValidator)validatorConst.newInstance();
        }catch(Exception ex){
            Logger.getLogger(ValidatableLegacyAction.class).error("Error is occured on Instancing Action Validator as " + validatorBeanId, ex);
            return new ForbiddenSDVActionValitator();
        }
        return null;
    }

    public Object execute(ExecutionEvent event) throws ExecutionException {

//        setInitializationData(paramIConfigurationElement, commandId, paramObj);();

        if ((this.m_registry == null) || (this.m_actionCommand == null))
            return null;

        String commandId = getActionCommand();
        String commandNameKey = commandId + "." + "NAME";
        String localizeCommandName = this.m_registry.getString(commandNameKey, null);

        if (localizeCommandName == null) {
            Logger.getLogger(LegacyHandler.class).warn("No name provided for [" + commandNameKey + "] within registry " + this.m_registry);
            localizeCommandName = "";
        }

        if(this.validator != null){
            try{
                validator.validate(commandId, getParameters(event), event.getApplicationContext());
            }catch(Exception ex){
                MessageBox.post(ex);
                return null;
            }
        }

        IC_Job localIC_Job = new IC_Job(localizeCommandName, event, event.getCommand().getId());
        localIC_Job.schedule();
        return null;
    }

    protected Map<String, Object> getParameters(ExecutionEvent event) {
        String commandId = event.getCommand().getId();
        Map<String, Object> clonedParameters = new HashMap<String, Object>();

        @SuppressWarnings("rawtypes")
        Map eventParameters = event.getParameters();
        if(eventParameters != null){
            for(Object key : eventParameters.keySet()){
                clonedParameters.put(key.toString(), eventParameters.get(key));
            }
        }
        clonedParameters.put(DEFAULT_PARAMETER_COMMANDID, commandId);

        return clonedParameters;
    }


    protected void performLegacyAIFCommand(ExecutionEvent event, final String cmdId) {
        try {
            if ((this.m_registry == null) || (getActionCommand() == null))
                return;

            Object action = AIFUtility.getAction(getActionCommand(), this.m_registry);
            if (action != null) {
                if (action instanceof AbstractAIFAction){
                    ((AbstractAIFAction)action).setEvent(event);
                }

                ((ActionListener)action).actionPerformed(null);
            }

        } catch (Exception ex) {
            MessageBox.post(ex);
        } finally {
            Display display = HandlerUtil.getActiveShell(event).getDisplay();
            display.asyncExec(new Runnable() {
                public void run() {
                    ICommandService commandService = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
                    commandService.refreshElements(cmdId, null);
                }
            });
        }
    }



    protected String getActionCommand() {
        return this.m_actionCommand;
    }

    protected void setActionCommand(String actionCommandId) {
        this.m_actionCommand = actionCommandId;
    }

    public String toString() {
        return getClass().getName() + "\n\tactionCommand=" + m_actionCommand + "\n\tregistry=" + m_registry + "\n\tregistry=" + m_registry.getString(m_actionCommand);
    }

    private class IC_Job extends Job {

        private ExecutionEvent m_event;
        private String m_cmdId;

        public IC_Job(String jobId, ExecutionEvent event, String cmdId) {
            super(jobId);
            this.m_event = event;
            this.m_cmdId = cmdId;
        }

        protected IStatus run(IProgressMonitor paramIProgressMonitor) {
            performLegacyAIFCommand(m_event, m_cmdId);
            return Status.OK_STATUS;
        }
    }
}