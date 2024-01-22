/**
 *
 */
package org.sdv.core.ui;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.apache.log4j.Logger;
import org.sdv.core.common.IOperationBeanFactory;
import org.sdv.core.common.ISDVOperation;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.util.SDVSpringContextUtil;

/**
 * Class Name : OperationBeanFactory
 * Class Description :
 * @date 2013. 10. 15.
 *
 */
public class OperationBeanFactory implements IOperationBeanFactory {

    private static Logger logger = Logger.getLogger(OperationBeanFactory.class);

    public static ISDVOperation getOperator(String commandId) {
        //TODO: getBean 시 operation이 없는 경우 Exception 처리
        String operationId = commandId.substring(0, commandId.length() - "Command".length()) + "Operation";
        ISDVOperation operation = (ISDVOperation)SDVSpringContextUtil.getBean(operationId);
        operation.setOperationId(operationId);
        return operation;
    }

    public static ISDVOperation getCommandOperator(String operationClassName, int operationId, String ownerId, Map<String,Object> parameter, IDataSet dataSet) throws Exception {
        logger.info("Operation Class = " + operationClassName);
        if(parameter == null) {
            Class<?> cls = Class.forName(operationClassName);
            Class<?> partypes[] = new Class[3];
            partypes[0] = int.class;
            partypes[1] = String.class;
            partypes[2] = IDataSet.class;
            Constructor<?> ct = cls.getConstructor(partypes);
            return (ISDVOperation) ct.newInstance(operationId, ownerId, dataSet);
        } else {
            Class<?> cls = Class.forName(operationClassName);
            Class<?> partypes[] = new Class[4];
            partypes[0] = int.class;
            partypes[1] = String.class;
            partypes[2] = Map.class;
            partypes[3] = IDataSet.class;
            Constructor<?> ct = cls.getConstructor(partypes);
            return (ISDVOperation) ct.newInstance(operationId, ownerId, parameter, dataSet);
        }
    }
    public static ISDVOperation getActionOperator(String operationClassName, String operationId, String ownerId, IDataSet dataSet) throws Exception {
        return getActionOperator(operationClassName, operationId, ownerId, null, dataSet);
    }

    public static ISDVOperation getActionOperator(String operationClassName, String operationId, String ownerId, Map<String, Object> parameters,  IDataSet dataSet) throws Exception {
        logger.info("Operation Class = " + operationClassName);
        Class<?> cls = Class.forName(operationClassName);
        if(parameters == null){
            Class<?> partypes[] = new Class[3];
            partypes[0] = String.class;
            partypes[1] = String.class;
            partypes[2] = IDataSet.class;
            Constructor<?> ct = cls.getConstructor(partypes);
            return (ISDVOperation)ct.newInstance(operationId, ownerId, dataSet);
        }else{
            Class<?> partypes[] = new Class[4];
            partypes[0] = String.class;
            partypes[1] = String.class;
            partypes[2] = Map.class;
            partypes[3] = IDataSet.class;
            Constructor<?> ct = cls.getConstructor(partypes);
            return  (ISDVOperation)ct.newInstance(operationId, ownerId, parameters, dataSet);
        }
    }


}
