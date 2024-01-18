//==================================================
//
//  Copyright 2008 Siemens Product Lifecycle Management Software Inc. All Rights Reserved.
//
//==================================================

package com.ssangyong.soa.biz;

import org.apache.log4j.Logger;

import com.ssangyong.soa.clientx.AppXCredentialManager;
import com.ssangyong.soa.clientx.AppXExceptionHandler;
import com.ssangyong.soa.clientx.AppXModelEventListener;
import com.ssangyong.soa.clientx.AppXPartialErrorListener;
import com.ssangyong.soa.clientx.AppXRequestListener;
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
    private static Connection           connection;
    static Logger logger = Logger.getLogger(Session.class);
    public String contextRoot;

    /**
     * The credentialManager is used both by the Session class and the Teamcenter
     * Services Framework to get user credentials.
     *
     */
    private static AppXCredentialManager credentialManager;

    /**
     * Create an instance of the Session with a connection to the specified
     * server.
     *
     * Add implementations of the ExceptionHandler, PartialErrorListener,
     * ChangeListener, and DeleteListeners.
     *
     * @param host      Address of the host to connect to, http://serverName:port/tc
     */
    public Session(String host,String userID,String password, String contextRoot)
    {
        this.contextRoot = contextRoot;
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
        Connection.addRequestListener( new AppXRequestListener() );

    }
    
    
    /**
     * Login to the Teamcenter Server
     *
     */
    public static boolean login()
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
                 logger.error("##################################");
                 logger.error("###   팀센터 로그인을 합니다. ");
                 logger.error("##################################");
                // *****************************
                // Execute the service operation
                // *****************************
                LoginResponse out = sessionService.login(credentials[0], credentials[1],
                        credentials[2], credentials[3],"", credentials[4]);
                logger.error("###   팀센터 로그에 성공하였습니다. " + out.serviceData.sizeOfPartialErrors());
                logger.error("##################################");
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
    public static Connection getConnection()
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
            logger.error("##################################");
            logger.error("####        로그아웃          ####  ");
            logger.error("##################################");
        }
        catch (ServiceException e){
        	logger.error("팀센터 Logout에 실패 하였습니다.", e);
        }
    }

}
