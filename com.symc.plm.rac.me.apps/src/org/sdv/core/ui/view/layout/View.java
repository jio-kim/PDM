/**
 * 
 */
package org.sdv.core.ui.view.layout;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class Name : View
 * Class Description : 
 * @date 2013. 10. 15.
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "view")
public class View {
    @XmlAttribute(name="id")
    String id;
    @XmlAttribute(name="order")
    String order;
    @XmlAttribute(name="configId")
    int configId = 0;
    
    /**
     * @return the id
     */    
    public String getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * @return the order
     */
    public String getOrder() {
        return order;
    }
    /**
     * @param order the order to set
     */
    public void setOrder(String order) {
        this.order = order;
    }
    /**
     * @return the configId
     */
    public int getConfigId() {
        return configId;
    }
    /**
     * @param configId the configId to set
     */
    public void setConfigId(int configId) {
        this.configId = configId;
    }
    
}
