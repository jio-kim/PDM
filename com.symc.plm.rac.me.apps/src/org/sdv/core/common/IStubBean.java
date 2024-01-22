/**
 * 
 */
package org.sdv.core.common;

/**
 * Class Name : IStubBean
 * Class Description : 
 * @date 2013. 10. 14.
 *
 */
public interface IStubBean extends Cloneable {
    
    public String getId();
    
    public String getImplement();
    
    public Object clone();
    
}
