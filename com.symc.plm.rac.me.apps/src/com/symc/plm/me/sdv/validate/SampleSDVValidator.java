/**
 * 
 */
package com.symc.plm.me.sdv.validate;

import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;


/**
 * Class Name : SampleSDVValidator
 * Class Description : 
 * @date 	2013. 12. 18.
 * @author  CS.Park
 * 
 */
public class SampleSDVValidator implements ISDVValidator {


	/**
	 * Description :
	 * @method :
	 * @date : 2013. 12. 18.
	 * @author : CS.Park
	 * @param :
	 * @return : 
	 * @see org.sdv.core.common.ISDVValidator#validate(java.lang.String, java.util.Map)
	 */
	@Override
	public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
		throw new SDVException("Test Exception");
	}

}
