package com.ssangyong.commands.eciecr;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.ssangyong.common.utils.CustomUtil;

public class EciEcrHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			EciEcrCommand command = new EciEcrCommand();
			command.executeModal();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
