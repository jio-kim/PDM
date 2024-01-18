package com.ssangyong.soa.service;

import java.util.Calendar;

import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.strong.workflow.WorkflowService;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.EPMAssignmentList;
import com.teamcenter.soa.client.model.strong.EPMJob;
import com.teamcenter.soa.client.model.strong.EPMTask;
import com.teamcenter.soa.client.model.strong.Signoff;
import com.teamcenter.soa.client.model.strong.User;
import com.teamcenter.soa.client.model.strong.WorkspaceObject;

import com.ssangyong.soa.biz.Session;

public class TcWorkflowManagementService implements
			com.teamcenter.services.strong.workflow._2008_06.Workflow,
			com.teamcenter.services.strong.workflow._2007_06.Workflow,
			com.teamcenter.services.strong.workflow._2010_09.Workflow,
			com.teamcenter.services.strong.workflow._2012_10.Workflow,
			com.teamcenter.services.strong.workflow._2013_05.Workflow,
			com.teamcenter.services.strong.workflow._2014_06.Workflow
			{
    private Session tcSession = null;

    public TcWorkflowManagementService(Session tcSession) {
        this.tcSession = tcSession;
    }

    @SuppressWarnings("static-access")
    public WorkflowService getService() {
        return WorkflowService.getService(tcSession.getConnection());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.teamcenter.services.strong.workflow._2010_09.Workflow#
     * applyTemplateToProcesses
     * (com.teamcenter.services.strong.workflow._2010_09.
     * Workflow.ApplyTemplateInput[], int)
     */
    @Override
    public ApplyTemplateResponse applyTemplateToProcesses(ApplyTemplateInput[] aapplytemplateinput, int arg1) throws ServiceException {
        return getService().applyTemplateToProcesses(aapplytemplateinput, arg1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.teamcenter.services.strong.workflow._2007_06.Workflow#setReleaseStatus
     * (
     * com.teamcenter.services.strong.workflow._2007_06.Workflow.ReleaseStatusInput
     * [])
     */
    @Override
    public SetReleaseStatusResponse setReleaseStatus(ReleaseStatusInput[] arg0) throws ServiceException {
        return getService().setReleaseStatus(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.teamcenter.services.strong.workflow._2008_06.Workflow#addAttachments
     * (com.teamcenter.soa.client.model.strong.EPMTask,
     * com.teamcenter.services.strong.workflow._2008_06.Workflow.AttachmentInfo)
     */
    @Override
    public ServiceData addAttachments(EPMTask arg0, AttachmentInfo arg1) {
        return getService().addAttachments(arg0, arg1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.teamcenter.services.strong.workflow._2008_06.Workflow#addSignoffs
     * (com.
     * teamcenter.services.strong.workflow._2008_06.Workflow.CreateSignoffs[])
     */
    @Override
    public ServiceData addSignoffs(CreateSignoffs[] acreatesignoffs) throws ServiceException {
        return getService().addSignoffs(acreatesignoffs);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.teamcenter.services.strong.workflow._2008_06.Workflow#assignAllTasks
     * (com.teamcenter.soa.client.model.strong.EPMJob,
     * com.teamcenter.soa.client.model.strong.EPMAssignmentList,
     * com.teamcenter.services.strong.workflow._2008_06.Workflow.Resources[])
     */
    @Override
    public ServiceData assignAllTasks(EPMJob epmjob, EPMAssignmentList epmassignmentlist, Resources[] aresources) throws ServiceException {
        return getService().assignAllTasks(epmjob, epmassignmentlist, aresources);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.teamcenter.services.strong.workflow._2008_06.Workflow#changeState
     * (com.
     * teamcenter.services.strong.workflow._2008_06.Workflow.ChangeStateInputInfo
     * )
     */
    @Override
    public ChangeStateOutput changeState(ChangeStateInputInfo changestateinputinfo) {
        return getService().changeState(changestateinputinfo);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.teamcenter.services.strong.workflow._2008_06.Workflow#createInstance
     * (boolean, java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String,
     * com.teamcenter.services.strong.workflow._2008_06.Workflow.ContextData)
     */
    @Override
    public InstanceInfo createInstance(boolean arg0, String arg1, String arg2, String arg3, String arg4, ContextData contextdata) {
        return getService().createInstance(arg0, arg1, arg2, arg3, arg4, contextdata);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.teamcenter.services.strong.workflow._2008_06.Workflow#delegateSignoff
     * (com.teamcenter.soa.client.model.ModelObject,
     * com.teamcenter.soa.client.model.strong.Signoff)
     */
    @Override
    public ServiceData delegateSignoff(ModelObject modelobject, Signoff signoff) {
        return getService().delegateSignoff(modelobject, signoff);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.teamcenter.services.strong.workflow._2008_06.Workflow#getAllTasks
     * (com.teamcenter.soa.client.model.strong.EPMJob, int)
     */
    @Override
    public Tasks getAllTasks(EPMJob epmjob, int i) {
        return getService().getAllTasks(epmjob, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.teamcenter.services.strong.workflow._2008_06.Workflow#getAssignmentLists
     * (java.lang.String[])
     */
    @Override
    public AssignmentLists getAssignmentLists(String[] as) throws ServiceException {
        return getService().getAssignmentLists(as);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.teamcenter.services.strong.workflow._2008_06.Workflow#getResourcePool
     * (
     * com.teamcenter.services.strong.workflow._2008_06.Workflow.GroupRoleRef[])
     */
    @Override
    public GetResourcePoolOutput getResourcePool(GroupRoleRef[] agrouproleref) {
        return getService().getResourcePool(agrouproleref);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.teamcenter.services.strong.workflow._2008_06.Workflow#
     * getWorkflowTemplates
     * (com.teamcenter.soa.client.model.strong.WorkspaceObject[],
     * java.lang.String)
     */
    @Override
    @Deprecated
    public Templates getWorkflowTemplates(WorkspaceObject[] aworkspaceobject, String s) {
        return getService().getWorkflowTemplates(aworkspaceobject, s);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.teamcenter.services.strong.workflow._2008_06.Workflow#listDefinitions
     * (java.lang.String, int)
     */
    @Override
    public ProcessTemplates listDefinitions(String s, int i) {
        return getService().listDefinitions(s, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.teamcenter.services.strong.workflow._2008_06.Workflow#performAction
     * (com.teamcenter.soa.client.model.strong.EPMTask, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String,
     * com.teamcenter.soa.client.model.ModelObject)
     */
    @Override
    public ServiceData performAction(EPMTask epmtask, String s, String s1, String s2, String s3, ModelObject modelobject) throws ServiceException {
        return getService().performAction(epmtask, s, s1, s2, s3, modelobject);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.teamcenter.services.strong.workflow._2008_06.Workflow#removeAttachments
     * (com.teamcenter.soa.client.model.strong.EPMTask,
     * com.teamcenter.soa.client.model.ModelObject[])
     */
    @Override
    public ServiceData removeAttachments(EPMTask epmtask, ModelObject[] amodelobject) {
        return getService().removeAttachments(epmtask, amodelobject);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.teamcenter.services.strong.workflow._2008_06.Workflow#removeSignoffs
     * (com
     * .teamcenter.services.strong.workflow._2008_06.Workflow.RemoveSignoffsInfo
     * [])
     */
    @Override
    public ServiceData removeSignoffs(RemoveSignoffsInfo[] aremovesignoffsinfo) throws ServiceException {
        return getService().removeSignoffs(aremovesignoffsinfo);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.teamcenter.services.strong.workflow._2008_06.Workflow#viewAuditFile
     * (com.teamcenter.soa.client.model.ModelObject, boolean)
     */
    @Override
    public AuditFile viewAuditFile(ModelObject modelobject, boolean flag) {
        return getService().viewAuditFile(modelobject, flag);
    }

	@Override
	public ServiceData performAction3(
			PerformActionInputInfo[] paramArrayOfPerformActionInputInfo) {
		return getService().performAction3(paramArrayOfPerformActionInputInfo);
	}

	@Override
	public ServiceData performActionWithSignature(EPMTask paramEPMTask,
			String paramString1, String paramString2, String paramString3,
			String paramString4, ModelObject paramModelObject,
			ApplySignatureInput[] paramArrayOfApplySignatureInput) {
		return getService().performActionWithSignature(paramEPMTask, paramString1, paramString2, paramString3, paramString4, paramModelObject, paramArrayOfApplySignatureInput);
	}

	@Override
	public ServiceData setActiveSurrogate(
			SetActiveSurrogateInputInfo[] paramArrayOfSetActiveSurrogateInputInfo) {
		return getService().setActiveSurrogate(paramArrayOfSetActiveSurrogateInputInfo);
	}

	@Override
	public ServiceData setOutOfOffice(User paramUser,
			ModelObject paramModelObject, Calendar paramCalendar1,
			Calendar paramCalendar2) {
		return getService().setOutOfOffice(paramUser, paramModelObject, paramCalendar1, paramCalendar2);
	}

	@Override
	public ServiceData setSurrogate(SurrogateInput[] paramArrayOfSurrogateInput) {
		return getService().setSurrogate(paramArrayOfSurrogateInput);
	}

	@Override
	public GetWorkflowTemplatesResponse getWorkflowTemplates(
			GetWorkflowTemplatesInputInfo[] paramArrayOfGetWorkflowTemplatesInputInfo) {
		return getWorkflowTemplates(paramArrayOfGetWorkflowTemplatesInputInfo);
	}

	@Override
	public ServiceData performAction2(EPMTask paramEPMTask,
			String paramString1, String paramString2, String paramString3,
			String paramString4, ModelObject paramModelObject) {
		return getService().performAction2(paramEPMTask, paramString1, paramString2, paramString3, paramString4, paramModelObject);
	}
}
