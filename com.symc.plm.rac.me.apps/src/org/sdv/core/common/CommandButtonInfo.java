/**
 * 
 */
package org.sdv.core.common;

import org.eclipse.swt.widgets.Button;


/**
 * Class Name : CommandButtonInfo
 * Class Description : 
 * @date 	2013. 12. 16.
 * @author  CS.Park
 * 
 */
public class CommandButtonInfo implements IButtonInfo {

	private Button button;

	private int	commandButtonId;
	private String	buttonId;
	private String actionId;
	private String operationId;
	private String buttonImage;

	public CommandButtonInfo(int buttonId, String actionId, String operationId, String buttonImage, Button button) {
		this.commandButtonId = buttonId;
		this.buttonId = String.valueOf(buttonId);
		this.actionId = actionId;
		this.operationId = operationId;
		this.buttonImage = buttonImage;
		this.button = button;
	}
	

	/**
	 * @return the button
	 */
	public Button getButton() {
		return button;
	}

	
	/**
	 * @return the buttonId
	 */
	public String getButtonId() {
		return buttonId;
	}
	
	public int getCommandButtonId(){
		return commandButtonId;
	}

	
	/**
	 * @return the actionId
	 */
	public String getActionId() {
		return actionId;
	}

	
	/**
	 * @return the operationId
	 */
	public String getOperationId() {
		return operationId;
	}

	
	/**
	 * @return the buttonImage
	 */
	public String getButtonImage() {
		return buttonImage;
	}


	@Override
	public String toString() {
		return String.format("[CommandButtonInfo] buttonId= %s , actionId=%s, operationId=%s, buttonText=%s, buttonImage=%s", buttonId, actionId, operationId, button.getText(), buttonImage);
	}


}
