/**
 * 
 */
package org.sdv.core.beans;

import java.util.Map;

import org.eclipse.swt.graphics.Image;

/**
 * Class Name : DialogStubBean
 * Class Description :
 * 
 * @date 2013. 10. 14.
 * 
 */
public class DialogStubBean extends ViewPaneStubBean {
    
    private Map<String, String> commandBarActions;
    private int width;
    private int height;
    private boolean scrolledDialog;
    private String imagePath;
    

    /**
     * @return the commandBarActions
     */
    public Map<String, String> getCommandBarActions() {
        return commandBarActions;
    }

    /**
     * @param commandBarActions
     *            the commandBarActions to set
     */
    public void setCommandBarActions(Map<String, String> commandBarActions) {
        this.commandBarActions = commandBarActions;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width
     *            the width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height
     *            the height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * 
     * @method isScrolledDialog 
     * @date 2013. 11. 12.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    public boolean isScrolledDialog() {
        return this.scrolledDialog;
    }

    /**
     * @param scrolledDialog the scrolledDialog to set
     */
    public void setScrolledDialog(boolean scrolledDialog) {
        this.scrolledDialog = scrolledDialog;
    }

    /**
     * @return the imagePath
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * @param imagePath the imagePath to set
     */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    
    public Image getTitleImage(){
        if(this.imagePath == null) return null;
        //TODO: 이미지 경로정보로부터 이미지를 찾아와 연결해주는 로직 추가 (2013.12.01) cspark
        return null;
    }

}
