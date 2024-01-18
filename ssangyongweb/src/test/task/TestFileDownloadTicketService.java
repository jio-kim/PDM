package test.task;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;

public class TestFileDownloadTicketService {

    public TestFileDownloadTicketService() {
    }

    @org.junit.Test
    @SuppressWarnings({ "unchecked", "unused" })
    public void testSearchProcessSheet() {
        SYMCRemoteUtil remote = new SYMCRemoteUtil("http://localhost:8080/ssangyongweb/HomeServlet");
        DataSet ds = new DataSet();
        ds.clear();
        ds.put("TARGET_ID", "PTP-A1-PT-PVXA2015");
        ds.put("SHEET_LANGUAGE", "all");
        ds.put("TARGET_DATE", "20140124");
        try {
            ArrayList<HashMap<String, String>> result = (ArrayList<HashMap<String, String>>) remote.execute("com.ssangyong.service.MbomInterfaceService", "searchProcessSheet", ds, true);

            System.out.println("GGGGG");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @org.junit.Test
    @SuppressWarnings("unchecked")
    public void testFileDownloadTicket() {
        // SYMCRemoteUtil remote = new SYMCRemoteUtil("http://10.80.1.99/ssangyongweb/HomeServlet");
        SYMCRemoteUtil remote = new SYMCRemoteUtil("http://localhost:8080/ssangyongweb/HomeServlet");
        DataSet ds = new DataSet();
        ds.clear();
        ArrayList<String> test = new ArrayList<String>();
        test.add("02QJuUzxo1W$GD");
        test.add("EfdJuUzxo1W$GD");
        // test.add("EXaJuUzxo1W$GD");
        // test.add("0_TJuUzxo1W$GD");
        // test.add("TjUJuUzxo1W$GD");
        // test.add("zzUJuUzxo1W$GD");
        // test.add("TZaJuYL1o1W$GD");

        ds.put("puids", test);
        try {
            ArrayList<Object> result = (ArrayList<Object>) remote.execute("com.ssangyong.service.FileDownloadTicketService", "getTicket", ds, true);
            for (Object ticketList : result) {
                System.out.println(((HashMap<String, Object>) ticketList).get("PUID"));
                System.out.println(((HashMap<String, Object>) ticketList).get("TICKET_INFO"));
                System.out.println(((HashMap<String, Object>) ticketList).get("SUCCESS"));
                System.out.println(((HashMap<String, Object>) ticketList).get("ERROR_MESSAGE"));
            }
            System.out.println("GGGGG");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @org.junit.Test
    public void testFileURLDownload() {
        
        URL url;
        FileOutputStream fos = null;
        InputStream inputStream = null;
        try {
            url = new URL("http://10.80.1.99:4544/?ticket=f3f39679cafb1e26ac95b034713526c8c69fa1a0a5c8fa03e5ffdce3a579c114v1004T000000000000510152e6528686d6df6c2014%2f01%2f27+14%3a35%3a00%2D2032738452+++++++++++++++++++++if_system+++++++++++++++++++++++157552dd682b86d6df6c++++++++++++%5cassembly_me_symc_mfg_52dde236%5ckps_35_exc_94309u859o78q.xlsx");
            URLConnection connection = url.openConnection();
            HttpURLConnection exitCode = (HttpURLConnection)connection;
            System.out.println(exitCode.getResponseCode()); //200 이면 존재 404이면 비존재
            
            
            fos = new FileOutputStream("C:/temp/testURLFileDown.xlsx");
            inputStream = connection.getInputStream();
            
            
            
            byte[] buffer = new byte[1024];
            int readBytes;
            while ((readBytes = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, readBytes);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(fos != null) {
                    fos.close();
                }
                if(inputStream != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
