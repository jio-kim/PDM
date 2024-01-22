/**
 *
 */
package com.symc.plm.me.sdv.handler;

import java.lang.reflect.Constructor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.AbstractAIFCommand;

public class WPartBlankHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			
			String commandInfo = event.getCommand().getId();
			System.out.println("commandInfo = "+commandInfo);
			Class commandClass = Class.forName(commandInfo);
			Constructor constructor = commandClass.getConstructor() ;
			AbstractAIFCommand aifCommand = (AbstractAIFCommand) constructor.newInstance() ;
			aifCommand.executeModal();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
