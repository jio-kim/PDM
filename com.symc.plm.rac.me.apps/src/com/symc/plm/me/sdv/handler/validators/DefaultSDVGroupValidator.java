package com.symc.plm.me.sdv.handler.validators;

import java.util.List;
import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;

public class DefaultSDVGroupValidator implements ISDVValidator {

    protected List<ISDVValidator> validators;

    @Override
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        if (validators != null) {
            for(ISDVValidator validator : validators){
                validator.validate(commandId, parameter, applicationCtx);
            }
        }
    }

    public List<ISDVValidator> getValidators(String validatorGroupId) {
        return validators;
    }

    public void setValidators(List<ISDVValidator> validators){
        this.validators = validators;

    }


}
