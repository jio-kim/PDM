/**
 *
 */
package org.sdv.core.ui.operation;

import java.util.List;
import java.util.Map;

import org.sdv.core.common.ISDVActionOperation;
import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.data.IDataSet;
import org.springframework.util.StringUtils;

import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;

/**
 * Class Name : SampleActionOperation
 * Class Description :
 * @date 2013. 11. 13.
 *
 */
public abstract class AbstractSDVActionOperation extends AbstractAIFOperation implements ISDVActionOperation, InterfaceAIFOperationListener {

    public static final Object PARAM_TARGET_ID_KEY = "targetId";

    public enum JobStatus {
        WORKING("working"),
        COMPLETED("completed"),
        CANCELED("canceled");

        private String description;

        JobStatus(String description){
            this.description = description;
        };

        public String getDescription(){
            return description;
        }
    }

    private String operationId;
    private String actionId;
    private String ownerId;
    private Map<String, Object> parameters;
    private String targetId;
    private IDataSet dataSet;

    //처리 결과와 오류를 저장하는 필드 ( 기본은 성공을 가르키고 오류가 날경우 FAIL로 등록한다.)
    private int executeResult = SUCCESS;
    private String errorMessage;
    private Throwable error;
    
    private List<ISDVValidator> validators;


    public static String getOperationStatus(String jobName, JobStatus status){
        return jobName + " " + status.getDescription();
    }

    public AbstractSDVActionOperation(int actionId, String ownerId, IDataSet dataset) {
        this(actionId, ownerId + ":" + String.valueOf(actionId), ownerId, dataset);
    }

    public AbstractSDVActionOperation(int actionId, String operationId, String ownerId, IDataSet dataset) {
        this(operationId, operationId + ":" + String.valueOf(actionId), ownerId, null, dataset);
    }

    public AbstractSDVActionOperation(String actionId, String ownerId, IDataSet dataset) {
       this(actionId, actionId, ownerId, null, dataset);
    }

    public AbstractSDVActionOperation(String actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        this(actionId, actionId, ownerId, parameters, dataset);
    }

    public AbstractSDVActionOperation(int actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        this(String.valueOf(actionId), String.valueOf(actionId), ownerId, parameters, dataset);
    }

    public AbstractSDVActionOperation(String operationId, String actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(getOperationStatus(operationId, JobStatus.WORKING));
        this.operationId = operationId;
        this.actionId = actionId;
        this.ownerId = ownerId;
        this.parameters = parameters;
        this.dataSet = dataset;
        if(parameters != null && parameters.containsKey(PARAM_TARGET_ID_KEY)){
            this.targetId = (String)parameters.get(PARAM_TARGET_ID_KEY);
        }
        this.addOperationListener(this);
    }


    public String getTargetId(){
        return (StringUtils.isEmpty(this.targetId)?this.ownerId : this.targetId);
    }

    protected void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getActionId(){
        return actionId;
    }

    @Override
    public String getOperationId(){
        return this.operationId;
    }

    @Override
    public void setOperationId(String operationId) {
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.ISDVOperation#setParameter(java.lang.String, java.util.Map, java.lang.Object)
     */
    @Override
    final public void setParameter(String commandId, Map<String, Object> paramters, Object applicationContext) {
        throw new UnsupportedOperationException("ActionOperation is not Supperted methode as \"setParameter\"");
    }

    public Map<String, Object> getParameters(){
        return this.parameters;
    }

    protected void setParameter(Map<String, Object> paramters){
        this.parameters = paramters;
    }

    /**
     * @return the ownerId
     */
    public String getOwnerId() {
        return ownerId;
    }

    /**
     * @return the dataSet
     */
    public IDataSet getDataSet() {
        return this.dataSet;
    }

    protected void setDataSet(IDataSet resultData){
        storeOperationResult(resultData);
    }

    @Override
    public void storeOperationResult(Object result){
        super.storeOperationResult(result);
        if(result != null && result instanceof IDataSet){
            this.dataSet = (IDataSet)result;
        }
    }

    /**
     *
     * @method getExecuteResult
     * @date 2013. 12. 5.
     * @author CS.Park
     * @param
     * @return int
     * @throws
     * @see
     */
    public int getExecuteResult(){
        return executeResult;
    }

    protected void setExecuteResult(int result){
        this.executeResult = result;
    }
    /**
     *
     * @method getExecuteException
     * @date 2013. 12. 5.
     * @author CS.Park
     * @param
     * @return Throwable
     * @throws
     * @see
     */
    public Throwable getExecuteError(){
        return this.error;
    }

    protected void setExecuteError(Throwable th){
        this.error = th;
        setExecuteResult(FAIL);
    }

    public String getErrorMessage(){
        return (this.errorMessage != null)?this.errorMessage:(this.error !=null?this.error.getMessage():"");
    }

    protected void setErrorMessage(String errorMessage){
        this.errorMessage = errorMessage;
    }

	/**
	 * @return the validators
	 */
	public List<ISDVValidator> getValidators() {
		return validators;
	}

	/**
	 * @param validators the validators to set
	 */
	public void setValidators(List<ISDVValidator> validators) {
		this.validators = validators;
	}


}
