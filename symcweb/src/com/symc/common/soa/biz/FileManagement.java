package com.symc.common.soa.biz;

import java.io.File;

import com.teamcenter.services.loose.core._2006_03.FileManagement.DatasetFileInfo;
import com.teamcenter.services.loose.core._2006_03.FileManagement.GetDatasetWriteTicketsInputData;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core._2006_03.DataManagement.CreateDatasetsResponse;
import com.teamcenter.services.strong.core._2008_06.DataManagement.DatasetProperties2;
import com.teamcenter.soa.client.FileManagementUtility;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.Dataset;

/**
 * Use the FileManagementService to transfer files
 *
 */
public class FileManagement
{
    private Session  tcSession = null;

    public FileManagement(Session tcSession) {
        this.tcSession = tcSession;
    }

    /**
     * Upload some files using the FileManagement utilities
     *
     */
    public void uploadFiles( )
    {
        FileManagementUtility fMSFileManagement = new FileManagementUtility(this.tcSession.getConnection());

        GetDatasetWriteTicketsInputData[] inputs  = new GetDatasetWriteTicketsInputData[1];
        inputs[0] = getGetDatasetWriteTicketsInputData();

        ServiceData response = fMSFileManagement.putFiles(inputs);

        if (response.sizeOfPartialErrors() > 0)
            System.out.println("FileManagementService upload returned partial errors: " + response.sizeOfPartialErrors());

        // Delete all objects created
        DataManagementService dmService = DataManagementService.getService(this.tcSession.getConnection());
        ModelObject [] datasets = new ModelObject[1];
        datasets[0] = inputs[0].dataset;
        dmService.deleteObjects(datasets);

        // Close FMS connection since done
        fMSFileManagement.term();
    }


    private GetDatasetWriteTicketsInputData  getGetDatasetWriteTicketsInputData()
    {
        DatasetProperties2 props = new DatasetProperties2();
        props.clientId = "datasetWriteTixTestClientId";
        props.type = "Text";
        props.name = "Sample-FMS-Upload";
        props.description = "Testing put File";

        DatasetProperties2[] currProps = {props};

        //create a datset
        DataManagementService dmService = DataManagementService.getService(this.tcSession.getConnection());
        CreateDatasetsResponse resp =  dmService.createDatasets2(currProps);

        //get the dataset
        Dataset dataset = resp.output[0].dataset;

        //create a file to associate with dataset
        DatasetFileInfo fileInfo = new DatasetFileInfo();
        DatasetFileInfo[] fileInfos = new DatasetFileInfo[1];

        // assume this file is in current dir
        File file1 = new File("ReadMe.txt");

        fileInfo.clientId            = "file_1";
        fileInfo.fileName            = file1.getAbsolutePath();
        fileInfo.namedReferencedName = "Text";
        fileInfo.isText              = true;
        fileInfo.allowReplace        = false;
        fileInfos[0] = fileInfo;

        GetDatasetWriteTicketsInputData inputData = new GetDatasetWriteTicketsInputData();
        inputData.dataset = dataset;
        inputData.createNewVersion = false;
        inputData.datasetFileInfos = fileInfos;

        return inputData;

    }
}
