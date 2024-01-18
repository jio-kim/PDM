//==================================================
//
//  Copyright 2010 Siemens Product Lifecycle Management Software Inc. All Rights Reserved.
//
//==================================================


package com.ssangyong.soa.clientx;

import com.teamcenter.soa.client.model.DeleteListener;

/**
 * Implementation of the DeleteListener, simply prints out list of all objects
 * that are deleted.
 *
 */
public class AppXDeletedObjectListener implements DeleteListener
{

    public void modelObjectDelete(String[] uids)
    {
        if (uids.length == 0)
            return;

        System.out.println("");
        System.out.println("Deleted Objects handled in com.teamcenter.clientx.AppXDeletedObjectListener.modelObjectDelete");
        System.out.println("The following objects have been deleted from the server and removed from the client data model:");
        for (int i = 0; i < uids.length; i++)
        {
            System.out.println("    " + uids[i]);
        }

    }

}
