/**
 * 
 */
package test.common;

import org.junit.After;
import org.junit.Before;

import com.symc.common.soa.biz.Session;
import com.symc.common.soa.biz.TcSessionUtil;

/**
 * Class Name : TcSoaTest
 * Class Description :
 * 
 * @date 2013. 9. 10.
 * 
 */
public abstract class AbstractTcSoaTest {
    public Session session = null;

    @Before
    public void setBefore() {
        try {
            session = new Session("http://plmwasdev/NewPLM", "if_system", "if_system");
            // BYPASS
            TcSessionUtil sessionUtil = new TcSessionUtil(session);
            sessionUtil.setByPass();
        } catch (Exception e) {
            e.printStackTrace();
            if (session != null) {
                session.logout();
            }
        }
    }

    @After
    public void setAfter() {
        if (session != null) {
            session.logout();
        }
    }

}
