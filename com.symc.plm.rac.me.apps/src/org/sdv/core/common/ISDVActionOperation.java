/**
 *
 */
package org.sdv.core.common;

import org.sdv.core.common.data.IDataSet;

import com.teamcenter.rac.aif.InterfaceAIFOperationListener;

/**
 * Class Name : ISDVActionOperation
 * Class Description :
 * @date 	2013. 12. 1.
 * @author  CS.Park
 *
 */
public interface ISDVActionOperation extends ISDVOperation {

    public static final int FAIL = -1;
    public static final int SUCCESS = 0;
    // 사용자가 Cancel 했을 경우 메세지 없이 종료 처리를 위해 추가
    public static final int CANCEL = -2;

    /**
     *
     * @method addOperationListener
     * @date 2013. 12. 1.
     * @author CS.Park
     * @param
     * @return void
     * @throws
     * @see
     */
    void addOperationListener(InterfaceAIFOperationListener interfaceAIFOperationListener);

    /**
     *
     * @method getData
     * @date 2013. 12. 1.
     * @author CS.Park
     * @param
     * @return IDataSet
     * @throws
     * @see
     */
    IDataSet getDataSet();

    /**
     *
     * @method cancel
     * @date 2013. 12. 1.
     * @author CS.Park
     * @param
     * @return void
     * @throws
     * @see
     */
    public boolean cancel();


    public String getTargetId();

    /**
     *
     * @method getOwnerId
     * @date 2013. 12. 1.
     * @author CS.Park
     * @param
     * @return String
     * @throws
     * @see
     */
    public String getOwnerId();

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
    public int getExecuteResult();

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
    public Throwable getExecuteError();


    public String getErrorMessage();

}
