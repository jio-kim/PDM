/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.model;

/**
 * Class Name : TreeColumnInfo
 * Class Description :
 * 
 * Tree 컬럼의 정보를 가지는 VO
 * 
 * @date 2013. 11. 25.
 * 
 */
public class TreeColumnInfo {
    private String id;
    private String name;
    private int width;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
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

}
