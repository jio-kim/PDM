/**
 * 
 */
package com.symc.plm.me.sdv.validate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.util.Registry;

/**
 *
 */
public class SelectedBodyBOPLineSDVValidator implements ISDVValidator {
	private Registry registry = Registry.getRegistry(SelectedBodyBOPLineSDVValidator.class);

	/* (non-Javadoc)
	 * @see org.sdv.core.common.ISDVValidator#validate(java.lang.String, java.util.Map, java.lang.Object)
	 */
	@Override
	public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
		try
		{
			InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();

	        for (InterfaceAIFComponent selectedTarget : selectedTargets)
	        {
	        	if (! (selectedTarget instanceof TCComponentBOPLine))
	        	{
	        		throw new ValidateSDVException(registry.getString("SelectBOPLine.MESSAGE", "Please select BOP line."));
	        	}

	        	TCComponentBOPLine selectedLine = (TCComponentBOPLine) selectedTarget;
	        	TCComponentType selectedItemType = selectedLine.getItem().getTypeComponent();
	        	TCComponentType selectedItemParentType = selectedItemType.getParent();
	        	if (selectedItemParentType.getTypeName().equals(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM))
	        	{
	        		if (! selectedItemType.getTypeName().equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM))
		        		throw new ValidateSDVException(registry.getString("SelectBodyBOP.MESSAGE", "Please select Body BOP line."));
	        	}
	        	else
	        	{
	        		if (! selectedLine.getItemRevision().isValidPropertyName(SDVPropertyConstant.SHOP_REV_PROCESS_TYPE))
		        		throw new ValidateSDVException(registry.getString("SelectBodyBOP.MESSAGE", "Please select Body BOP line."));

	        		List<String> bodyTypeList = Arrays.asList(registry.getStringArray("Property.BodyBOP.Type"));
	        		if (! bodyTypeList.contains(selectedLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PROCESS_TYPE).toUpperCase()))
		        		throw new ValidateSDVException(registry.getString("SelectBodyBOP.MESSAGE", "Please select Body BOP line."));
	        	}
	        }
		}
		catch (Exception ex)
		{
			throw new ValidateSDVException(ex.getMessage(), ex);
		}
	}
}
