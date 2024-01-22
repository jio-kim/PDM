package com.symc.plm.me.sdv.dialog.common;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * [SR151207-041][20151215] taeku.jeong 용접점의 끊어지기전 Occurrence Name을 찾아서 표시해주는 메뉴추가
 * 간단하게 한줄정도 되는 User 입력을 받는 Dialog를 쉽게 쓸수 있도록 구현한 Class
 * @author Taeku
 *
 */
public class SimpleUserInputDialog {
	
	// User 입력 사항을 임시저장하기위한 전역변수
	static private String tempString = null;
	
	/**
	 * User로 부터 간단한 입력 사항을 입력받아서 Return 해주는 함수 
	 * 
	 * @param dialogTieleString 사용자 입력창의 Title
	 * @param dialogMessageString 사용자 입력창에 표시될 간단한 설명을 위한 메시지
	 * @param initialValueString 초기값으로 입력되어 있을 내용
	 * @return
	 */
	static public String getUserInputString(String dialogTieleString, String dialogMessageString, String initialValueString){

	    	String userInputString = null;
	    	
			final String title = dialogTieleString;
			final String messageString = dialogMessageString;
			final String initialValue = initialValueString;
			final Shell shell = AIFUtility.getActiveDesktop().getShell();
			
			// 아래의 UserInputLengthValidator 참조해서 구체적인 구현이 필요할때 구현 할것.
			final IInputValidator inputValidator = null;
			
			shell.getDisplay().syncExec(new Runnable() {
				
				public void run()
				{
					//UserInputLengthValidator validator = new LengthValidator();
					//UserInputLengthValidator validator = null;
					
					InputDialog dlg = new InputDialog(shell, title, messageString, initialValue, inputValidator);

					if (dlg.open() == Window.OK) {
				          // User clicked OK; update the label with the input
						tempString = dlg.getValue();
				    }
					
				}
			});
			
			if(tempString!=null && tempString.trim().length()>0){
				userInputString = tempString.trim();
			}
			
			return userInputString;
	    	
	    }
	
    class UserInputLengthValidator implements IInputValidator {
    	
    	private int minLength = 0;
    	private int maxLength = 0;
    	
    	private UserInputLengthValidator(int minLength, int maxLength){
    		this.maxLength = maxLength;
    		this.minLength = minLength;
    	}

    	/**
    	 * Validates the String. Returns null for no error, or an error message
    	 * 
    	 * @param newText the String to validate
    	 * @return String
    	 */
    	public String isValid(String newText) {
    		int len = newText.length();

    		String message = null;
    		// Determine if input is too short or too long
    		if (minLength!=0 && len < minLength){
    			message = "Too short";
    		}
    		
    		if (maxLength!=0 && len > maxLength){
    			message = "Too long";
    		}

    		// Input must be OK
    		return message;
    	}
	}
	 
}
