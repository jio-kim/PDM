package com.kgm.commands.commonpartcheck;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * 1. 최초 작성
 * @Copyright : Plmsoft
 * @author   : 이정건
 * @since    : 2018. 4. 6.
 * Package ID : com.kgm.commands.commonpartcheck.CommonPartCheckHandler.java
 */
public class CommonPartCheckHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			CommonPartCheckCommand command = new CommonPartCheckCommand();
			command.executeModal();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
