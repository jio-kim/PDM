package com.symc.plm.rac.prebom.handlers;

import java.lang.reflect.Constructor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.AbstractAIFCommand;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class CommonHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public CommonHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
    public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			
			String commandInfo = event.getCommand().getId();
			
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
