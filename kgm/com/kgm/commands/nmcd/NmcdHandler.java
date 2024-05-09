package com.kgm.commands.nmcd;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * 1. ���� �ۼ�
 * @Copyright : Plmsoft
 * @author   : ������
 * @since    : 2018. 8. 28.
 * Package ID : com.kgm.commands.nmcd.NmcdHandler.java
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
