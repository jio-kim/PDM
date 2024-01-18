/**
 * 
 */
package test.task;

import org.junit.Assert;

import test.common.AbstractTcSoaTest;

import com.symc.common.soa.biz.TcVariantUtil;
import com.teamcenter.services.internal.strong.structuremanagement._2011_06.VariantManagement.ModularOption;

/**
 * Class Name : Test
 * Class Description : 
 * @date 2013. 9. 10.
 *
 */
public class CorpOptionItemTest extends AbstractTcSoaTest {

    @org.junit.Test
    public void test() {
        try {
            TcVariantUtil tcVariantUtil = new TcVariantUtil(session);            
            ModularOption[] test = tcVariantUtil.getOptionMaster();
            Assert.assertNotNull(test);
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }

}
