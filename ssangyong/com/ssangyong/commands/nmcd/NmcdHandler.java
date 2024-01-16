package com.ssangyong.commands.nmcd;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * 1. 최초 작성
 * @Copyright : Plmsoft
 * @author   : 조석훈
 * @since    : 2018. 8. 28.
 * Package ID : com.ssangyong.commands.nmcd.NmcdHandler.java
 */
public class NmcdHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			NmcdCommand command = new NmcdCommand();
			command.executeModal();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
