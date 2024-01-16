/**
 * 일괄 업로드 작업에 필요한 기능 명세 Interface
 */

package com.ssangyong.common.bundlework;

public interface BWImportImpl
{
    /**
     *  Item 신규 ID 발번
     * @throws Exception
     */
    public abstract String generateNewID() throws Exception;
    
}
