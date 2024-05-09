package com.kgm.handler;

import java.lang.reflect.Constructor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.AbstractAIFCommand;

/**
 * 1.Command Class �� �����ڴ� public ���� �Ѵ�.
 * 2.Command Class �� �����ڴ� String parameter �ϳ��� �������� �Ѵ�.
 * 3.Command ID = Command Class�� Ǯ ��Ű�� �� + Ŭ���� �̸�
 * ex) Command Class �� com.pungkang.newprocess.NewProcessCommand.java �� ��� Command ID �� "com.pungkang.newprocess.NewProcessCommand" �� �Ǿ� �� �Ѵ�.
 * 4.Command Class ���� �ݵ�� Default �����ڰ� ���� �Ͽ��� �Ѵ�. 
 * @Copyright : S-PALM
 * @author   : ������
 * @since    : 2012. 03. 22
 * Package ID : com.pungkang.handlers.SpalmCommonHandler.java
 */
public class PeInterfaceHandler extends AbstractHandler {

	@SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
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
