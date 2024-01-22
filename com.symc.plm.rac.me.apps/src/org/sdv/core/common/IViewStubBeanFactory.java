/**
 * 
 */
package org.sdv.core.common;

/**
 * Class Name : IViewStubBeanFactory
 * Class Description : 
 * @date 2013. 10. 15.
 *
 */
public interface IViewStubBeanFactory {
    public IStubBean getViewStub(String id);
    public IStubBean getViewStub(String id, String context);
}
