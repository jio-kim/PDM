/**
 * 일괄작업에 필요한 기능 명세 Interface
 */

package com.kgm.common.bundlework;

import com.kgm.common.bundlework.BundleWorkDialog.ManualTreeItem;

public interface BundleWorkImpl
{
    /**
     *  Dialog Open 기능 구현
     * @throws Exception
     */
    public abstract void dialogOpen() throws Exception;
    
    
    /**
     *  Target(파일,디렉토리,TCComponent.. etc) 선택시 기능 구현
     */
    public abstract void selectTarget() throws Exception;
    
    /**
     * Data Loading 전처리
     */
    public abstract void loadPre() throws Exception;
    
    /**
     * 일괄작업에 필요한
     * Data Loading 기능 구현
     */
    public abstract void load() throws Exception;
    
    /**
     * Data Loading 후처리
     */
    public abstract void loadPost() throws Exception;
    
    
    /**
     * 일괄작업 Validate 전처리
     */
    public abstract void validatePre() throws Exception;
    
    /**
     * 일괄작업 Validate 기능 구현
     */
    public abstract boolean validate() throws Exception;
    
    /**
     * 일괄작업 Validate 후처리
     */
    public abstract void validatePost() throws Exception;

    
    /**
     * 일괄작업 전처리
     */
    public abstract void executePre() throws Exception;

    /**
     * 일괄작업 실제 기능 구현
     */
    public abstract void execute() throws Exception;
    
    /**
     * 일괄작업 후처리
     */
    public abstract void executePost() throws Exception;
    
    /**
     * 일괄작업에 필요한 Excel Template Download 기능 구현
     */
    public abstract void downLoadTemplate() throws Exception;
    
    
    /**
     * Item Import 후 처리
     */
    public abstract void importDataPost(ManualTreeItem treeItem) throws Exception;
    
    
}
