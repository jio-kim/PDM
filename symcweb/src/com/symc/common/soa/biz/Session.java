//==================================================
//
//  Copyright 2008 Siemens Product Lifecycle Management Software Inc. All Rights Reserved.
//
//==================================================

package com.symc.common.soa.biz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.symc.common.soa.clientx.AppXCredentialManager;
import com.symc.common.soa.clientx.AppXExceptionHandler;
import com.symc.common.soa.clientx.AppXModelEventListener;
import com.symc.common.soa.clientx.AppXPartialErrorListener;
import com.symc.common.soa.clientx.AppXRequestListener;
import com.teamcenter.schemas.soa._2006_03.exceptions.InvalidCredentialsException;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.strong.core.SessionService;
import com.teamcenter.services.strong.core._2006_03.Session.LoginResponse;
import com.teamcenter.soa.SoaConstants;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.exceptions.CanceledOperationException;


public class Session
{
    /**
     * Single instance of the Connection object that is shared throughtout
     * the application. This Connection object is needed whenever a Service
     * stub is instantiated.
     */
    private Connection           connection;
    Logger logger = Logger.getLogger(Session.class);
    
    /**
     * The credentialManager is used both by the Session class and the Teamcenter
     * Services Framework to get user credentials.
     *
     */
    private AppXCredentialManager credentialManager;
    private AppXRequestListener appXRequestListener;

    /**
     * Create an instance of the Session with a connection to the specified
     * server.
     *
     * Add implementations of the ExceptionHandler, PartialErrorListener,
     * ChangeListener, and DeleteListeners.
     *
     * @param host      Address of the host to connect to, http://serverName:port/tc
     */
    public Session(String host,String userID,String password)
    {
        // FCC Start...
        startFcc();
        
        // Create an instance of the CredentialManager, this is used
        // by the SOA Framework to get the user's credentials when
        // challanged by the server (sesioin timeout on the web tier).
        credentialManager = new AppXCredentialManager();

        String protocol = (host.startsWith("http"))? SoaConstants.HTTP: SoaConstants.IIOP;

        // Create the Connection object, no contact is made with the server
        // until a service request is made

        credentialManager.setUserPassword(userID, password, "");
//        connection = new Connection(host, new HttpState(), credentialManager, SoaConstants.REST,
//                                    protocol, false);

        String envNameTccs = null;
        if ( host.startsWith("http") )
        {
            protocol   = SoaConstants.HTTP;
        }
        else if ( host.startsWith("tccs") )
        {
            protocol   = SoaConstants.TCCS;
            host = host.trim();
            int envNameStart = host.indexOf('/') + 2;
            envNameTccs = host.substring( envNameStart, host.length() );
            host = "";
        }
        else
        {
            protocol   = SoaConstants.IIOP;
        }
        connection = new Connection(host, credentialManager, SoaConstants.REST, protocol);
        if( protocol == SoaConstants.TCCS )
        {
           connection.setOption(  Connection.TCCS_ENV_NAME, envNameTccs );
        }

        // Add an ExceptionHandler to the Connection, this will handle any
        // InternalServerException, communication errors, xml marshalling errors
        // .etc
        connection.setExceptionHandler(new AppXExceptionHandler());

        // While the above ExceptionHandler is required, all of the following
        // Listeners are optional. Client application can add as many or as few Listeners
        // of each type that they want.

        // Add a Partial Error Listener, this will be notified when ever a
        // a service returns partial errors.
        connection.getModelManager().addPartialErrorListener(new AppXPartialErrorListener());

        // Add a Change and Delete Listener, this will be notified when ever a
        // a service returns model objects that have been updated or deleted.
        connection.getModelManager().addModelEventListener(new AppXModelEventListener());

        // Add a Request Listener, this will be notified before and after each
        // service request is sent to the server.
        appXRequestListener = new AppXRequestListener();
        Connection.addRequestListener(appXRequestListener);

    }


    /**
     * Login to the Teamcenter Server
     *
     */
    public boolean login()
    {
        // Get the service stub
        SessionService sessionService = SessionService.getService(connection);

        try
        {
            // Prompt for credentials until they are right, or until user
            // cancels
            String[] credentials = credentialManager.promptForCredentials();
            try
            {
                 logger.info("##################################");
                 logger.info("###   팀센터 로그인을 합니다. ");
                 logger.info("##################################");
                // *****************************
                // Execute the service operation
                // *****************************
                LoginResponse out = sessionService.login(credentials[0], credentials[1],
                        credentials[2], credentials[3],"", credentials[4]);
                logger.info("###   팀센터 로그에 성공하였습니다. ");
                logger.info("###   로그인 : " + out.user);
                logger.info("##################################");
                return true;
            }
            catch (InvalidCredentialsException e)
            {
                credentials = credentialManager.getCredentials(e);
                logger.error(e);
            }
        }
        // User canceled the operation, don't need to tell him again
        catch (CanceledOperationException e) {
        	logger.error(e);
        }
        // Exit the application
        //System.exit(0);
        return false;
    }


    /**
     * Get the single Connection object for the application
     *
     * @return  connection
     */
    public Connection getConnection()
    {
        return connection;
    }

    /**
     * Terminate the session with the Teamcenter Server
     *
     */
    public void logout()
    {
        // Get the service stub
        SessionService sessionService = SessionService.getService(connection);
        try
        {
            // *****************************
            // Execute the service operation
            // *****************************
            sessionService.logout();
            //Remove a Request Listener
            Connection.removeRequestListener(appXRequestListener);
            logger.info("##################################");
            logger.info("####        로그아웃          ####  ");
            logger.info("##################################");
        }
        catch (ServiceException e){
        	logger.error("팀센터 Logout에 실패 하였습니다.", e);
        }
    }
    
    /**
     * FCC Start
     * 
     * @method startFcc 
     * @date 2013. 10. 18.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void startFcc() {
        try {
//            Process oProcess = new ProcessBuilder("C:\\siemens\\TC\\OTW9\\tccs\\bin\\fccstat.exe", "-start").start();
            
        	// [TC10 Upgrade] fccstat.exe 파일 Path를 환경변수 FMS_HOME을 통해서 가져옴
            String path = "C:\\Siemens\\TC13\\tccs\\bin\\fccstat.exe";
            String fmsHome = System.getenv("FMS_HOME");
        	System.out.println("FMS_HOME [" + fmsHome + "]");
            if(fmsHome != null && !fmsHome.isEmpty()) {
            	path = fmsHome + "\\bin\\fccstat.exe";
            }
            
            Process oProcess = new ProcessBuilder(path, "-start").start();
            
            // 외부 프로그램 출력 읽기
            BufferedReader stdOut = new BufferedReader(new InputStreamReader(oProcess.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(oProcess.getErrorStream()));

            String s = "";
            // "표준 출력"과 "표준 에러 출력"을 출력
            while ((s = stdOut.readLine()) != null) {
                System.out.println(s);
            }
            while ((s = stdError.readLine()) != null) {
                System.err.println(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
