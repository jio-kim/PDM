/**
 * �ϰ��۾��� �ʿ��� ��� �� Interface
 */

package com.kgm.common.bundlework;

import com.kgm.common.bundlework.BundleWorkDialog.ManualTreeItem;

public interface BundleWorkImpl
{
    /**
     *  Dialog Open ��� ����
     * @throws Exception
     */
    public abstract void dialogOpen() throws Exception;
    
    
    /**
     *  Target(����,���丮,TCComponent.. etc) ���ý� ��� ����
     */
    public abstract void selectTarget() throws Exception;
    
    /**
     * Data Loading ��ó��
     */
    public abstract void loadPre() throws Exception;
    
    /**
     * �ϰ��۾��� �ʿ���
     * Data Loading ��� ����
     */
    public abstract void load() throws Exception;
    
    /**
     * Data Loading ��ó��
     */
    public abstract void loadPost() throws Exception;
    
    
    /**
     * �ϰ��۾� Validate ��ó��
     */
    public abstract void validatePre() throws Exception;
    
    /**
     * �ϰ��۾� Validate ��� ����
     */
    public abstract boolean validate() throws Exception;
    
    /**
     * �ϰ��۾� Validate ��ó��
     */
    public abstract void validatePost() throws Exception;

    
    /**
     * �ϰ��۾� ��ó��
     */
    public abstract void executePre() throws Exception;

    /**
     * �ϰ��۾� ���� ��� ����
     */
    public abstract void execute() throws Exception;
    
    /**
     * �ϰ��۾� ��ó��
     */
    public abstract void executePost() throws Exception;
    
    /**
     * �ϰ��۾��� �ʿ��� Excel Template Download ��� ����
     */
    public abstract void downLoadTemplate() throws Exception;
    
    
    /**
     * Item Import �� ó��
     */
    public abstract void importDataPost(ManualTreeItem treeItem) throws Exception;
    
    
}
