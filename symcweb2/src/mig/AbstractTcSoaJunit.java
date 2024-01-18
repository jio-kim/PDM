/**
 * 
 */
package mig;

import org.junit.After;
import org.junit.Before;

import com.symc.common.soa.biz.Session;
import com.symc.common.soa.biz.TcItemUtil;
import com.symc.common.soa.biz.TcSessionUtil;
import com.symc.work.service.TcFileService;

/**
 * Class Name : TcSoaTest
 * Class Description :
 * 
 * @date 2013. 9. 10.
 * 
 */
public abstract class AbstractTcSoaJunit {
    public Session session = null;
    public TcFileService tcFileService = null; 
    TcItemUtil tcItemUtil = null;
    //FTP
    String ip = "10.80.1.87";
    int port = 21;
    String login = "EAIIF";
    String pass = "123qwer@";
    String cadFtpPath = ".";

    @Before
    public void setBefore() {
        try {
            session = new Session("http://plmwas02/NewPLM", "if_system", "if_system");
            // BYPASS
            TcSessionUtil sessionUtil = new TcSessionUtil(session);
            sessionUtil.setByPass();
            tcItemUtil = new TcItemUtil(session);
            tcFileService = new TcFileService(session);
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
