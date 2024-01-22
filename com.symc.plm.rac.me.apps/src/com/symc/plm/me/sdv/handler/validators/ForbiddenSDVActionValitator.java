package com.symc.plm.me.sdv.handler.validators;

import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;

public class ForbiddenSDVActionValitator implements ISDVValidator {

    @Override
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        throw new SDVException("Not Supported this action. \nPlease contact to System Administor");
    }

}
