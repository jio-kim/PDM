/**
 * 
 */
package org.sdv.core.ui.view.layout;

/**
 * Class Name : Demo
 * Class Description : 
 * @date 2013. 10. 15.
 *
 */
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class Demo {
    @org.junit.Test
    public void test() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "+
        "<layout type=\"borderLayoutView\"> "+
            "<view id=\"sampleView1\" order=\"NORTH\" /> "+
            "<layout type=\"splitLayout\" style=\"VERTICAL\" order=\"CENTER\" /> "+
            "<layout type=\"fillLayout\" order=\"LEFT\"> "+
                "<view id=\"sampleView2\" /> "+
            "</layout> "+
            "<layout type=\"tabLayout\" order=\"RIGHT\"> "+
                "<view id=\"sampleView3\" order=\"0\" /> "+
                "<view id=\"sampleView4\" order=\"1\" /> "+
            "</layout> "+
        "</layout> ";
        JAXBContext jaxbContext = JAXBContext.newInstance(Layout.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Layout layout = (Layout) unmarshaller.unmarshal(new StringReader(xml));
        //Layout layout = (Layout) unmarshaller.unmarshal(new File("D:\\dev\\ssangyong\\workspace\\core\\src\\com\\symc\\plm\\me\\sdv\\context\\NewFile.xml"));
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(layout, System.out);
    }
}
