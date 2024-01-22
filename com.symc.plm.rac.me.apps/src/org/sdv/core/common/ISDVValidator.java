/**
 * 
 */
package org.sdv.core.common;

import java.util.Map;

import org.sdv.core.common.exception.SDVException;


/**
 * Class Name : ISDVValidator
 * Class Description : 
 * @date 	2013. 12. 18.
 * @author  CS.Park
 * 
 */
public interface ISDVValidator {
		
	public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException;

}
