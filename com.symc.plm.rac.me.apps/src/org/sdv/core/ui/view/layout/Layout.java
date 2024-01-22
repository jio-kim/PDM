/**
 * 
 */
package org.sdv.core.ui.view.layout;

/**
 * Class Name : Root
 * Class Description : 
 * @date 2013. 10. 15.
 *
 */
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "layout")
public class Layout {
    @XmlAttribute(name="id")
    String id;
    @XmlAttribute(name="type")
    String type;
    @XmlAttribute(name="style")
    String style;
    @XmlAttribute(name="order")
    String order;
    @XmlElement(name = "layout")        
    List<Layout> layouts;
    @XmlElement(name = "view")        
    List<View> views;
    
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
     * @return the type
     */
    public String getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }
    /**
     * @return the style
     */
    public String getStyle() {
        return style;
    }
    /**
     * @param style the style to set
     */
    public void setStyle(String style) {
        this.style = style;
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
     * @return the layouts
     */
    public List<Layout> getLayouts() {
        return layouts;
    }
    /**
     * @param layouts the layouts to set
     */
    public void setLayouts(List<Layout> layouts) {
        this.layouts = layouts;
    }
    /**
     * @return the views
     */
    public List<View> getViews() {
        return views;
    }
    /**
     * @param views the views to set
     */
    public void setViews(List<View> views) {
        this.views = views;
    }
    
    
    
}
