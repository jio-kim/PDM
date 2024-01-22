package com.teamcenter.ets.translator.ugs.weldpointexport;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.teamcenter.ets.soa.SoaHelper;
import com.teamcenter.ets.soa.TeamcenterServerProxy;
import com.teamcenter.ets.util.DataSetHelper;
import com.teamcenter.ets.util.Registry;
import com.teamcenter.services.loose.core._2006_03.FileManagement;
import com.teamcenter.services.strong.core._2007_06.DataManagement;
import com.teamcenter.services.strong.core._2007_06.DataManagement.DatasetTypeInfo;
import com.teamcenter.services.strong.core._2007_06.DataManagement.RelationAndTypesFilter2;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.DatasetType;
import com.teamcenter.soa.client.model.strong.Group;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.Tool;
import com.teamcenter.soa.client.model.strong.User;
import com.teamcenter.tstk.util.log.ITaskLogger;

/**
 * Co2 용접을 추가하면서 Catia Script가 수행되면서 생성되는 CSV 파일을 Dataset으로 추가해주는 기능을 수행하는 Class
 * 용접점의 Data를 가진 CSV 파일은 WeldGroup Item Revision에 Dataset으로 추가된다.
 * 해당 Dataset은 WeldPart/rev, WeldPart_CO2/rev 형태의 이름을 가지며  "IMAN_reference" Relation, "WeldPointSet" Type으로 생성된다.
 * [NON-SR][20160503] Taeku.Jeong 
 * @author Taeku
 *
 */
public class WpDataSetHelper extends DataSetHelper {
	
	private boolean isSpotWeldPointCSV = true;
	private Registry m_zRegistry = null;
	
	ITaskLogger m_zTaskLogger;
	StringBuffer buffer;
	boolean isDebug = false;
	
	public WpDataSetHelper(ITaskLogger zTaskLogger, StringBuffer buffer, boolean isDebug, boolean isSpotWeldPointCSV){
		super(zTaskLogger, true, false);

		this.m_zTaskLogger = zTaskLogger;
		this.buffer = buffer;
		this.isDebug = isDebug;
		this.isSpotWeldPointCSV = isSpotWeldPointCSV;
		this.m_zRegistry = Registry.getRegistry("com.teamcenter.ets.ets");
	}
	
	private void addLog(String msg){
		if( isDebug ){
			m_zTaskLogger.info(msg);
			buffer.append(msg);
			buffer.append("\r\n");
		}
	}

	protected boolean isSpotWeldPointCSV() {
		return isSpotWeldPointCSV;
	}


	protected void setSpotWeldPointCSV(boolean isSpotWeldPointCSV) {
		this.isSpotWeldPointCSV = isSpotWeldPointCSV;
	}

	private String getNewSourceDatasetName(Dataset sourceDataset){
		
		String datasetName = null;
		String sourceDatasetName = null; 
		try {
			sourceDataset = (Dataset) SoaHelper.getProperties(
					sourceDataset, "object_name");
			sourceDatasetName = sourceDataset.get_object_name();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(isSpotWeldPointCSV==false){
			
			String[] sourceDatasetNameTemps = sourceDatasetName.split("/");
			
			//for (int i = 0; i < sourceDatasetNameTemps.length; i++) {
			//	System.out.println("sourceDatasetNameTemps["+i+"] = "+sourceDatasetNameTemps[i]);
			//}
			
			String newSourceName = null;
			if(sourceDatasetNameTemps!=null && sourceDatasetNameTemps.length==2){
				datasetName = sourceDatasetNameTemps[0].trim()+"_Co2/"+sourceDatasetNameTemps[1].trim();
			}
		}else{
			datasetName = sourceDatasetName;
		}
	
		return datasetName;
	}

	@Override
	public Dataset findDataset(ModelObject sourceComponent,
			Dataset sourceDataset, String relationType, String datasetType)
			throws Exception {

		this.m_zTaskLogger.trace("  findDataset() begin...", 0);

		if (sourceComponent == null) {
			this.m_zTaskLogger.error("    input param sourceComponent is null");
			throw new NullPointerException("sourceComponent");
		}
		if (relationType == null) {
			this.m_zTaskLogger.error("    input param relationType is null");
			throw new NullPointerException("relationType");
		}
		if (datasetType == null) {
			this.m_zTaskLogger.error("    input param datasetType is null");
			throw new NullPointerException("datasetType");
		}

		ModelObject sourceComponentToCheck = sourceComponent;
		if (sourceComponent instanceof Dataset) {
			Dataset sourceDS = (Dataset) sourceComponent;

			if (sourceDS.get_revision_number() != 0) {
				ModelObject[] datasetVersions = getAllRevisions(sourceDS);

				if ((datasetVersions != null) && (datasetVersions.length > 0)) {
					sourceComponentToCheck = datasetVersions[0];
				}
			}
		}

		DataManagement.RelationAndTypesFilter2[] relationFilters = new DataManagement.RelationAndTypesFilter2[1];
		relationFilters[0] = new DataManagement.RelationAndTypesFilter2();
		relationFilters[0].relationName = relationType;
		ModelObject[] childComps = SoaHelper.getChildrenPrimary(
				sourceComponentToCheck, relationFilters);

		Dataset foundDataset = null;

		if ((childComps != null) && (childComps.length > 0)) {
			for (int i = 0; i < childComps.length; ++i) {
				if (!(childComps[i] instanceof Dataset))
					continue;
				Dataset targetDataset = (Dataset) childComps[i];
				targetDataset = (Dataset) SoaHelper.getProperties(
						targetDataset, new String[] { "dataset_type",
								"object_name" });
				DatasetType zDsType = targetDataset.get_dataset_type();
				zDsType = (DatasetType) SoaHelper.getProperties(zDsType,
						"datasettype_name");
				String dsType = zDsType.get_datasettype_name();

				
				String sourceDatasetName = getNewSourceDatasetName(sourceDataset);
//				sourceDataset = (Dataset) SoaHelper.getProperties(
//						sourceDataset, "object_name");
//				String sourceDatasetName = sourceDataset.get_object_name();

				String targetDatasetName = targetDataset.get_object_name();

				if ((!(sourceDatasetName.equals(targetDatasetName)))
						|| (!(dsType.equals(datasetType)))) {
					continue;
				}
				this.m_zTaskLogger.debug("    dataset found");
				foundDataset = targetDataset;
				SoaHelper.refresh(foundDataset);
				break;
			}

		} else {
			this.m_zTaskLogger
					.debug("No child datasets related to source component "
							+ sourceDataset.get_object_string());
		}

		this.m_zTaskLogger.trace("  findDataset() end", 0);
		return foundDataset;
	}
	
	@Override
	protected Dataset[] createDataset(ModelObject sourceComponent,
			Dataset sourceDataset, String datasetType, String relationType)
			throws Exception {

		this.m_zTaskLogger.trace("createDataset() begin...", 0);

		if (sourceComponent == null) {
			this.m_zTaskLogger.error("  input param sourceComponent is null");
			throw new NullPointerException("sourceComponent");
		}
		if (sourceDataset == null) {
			this.m_zTaskLogger.error("  input param sourceDataset is null");
			throw new NullPointerException("sourceDataset");
		}
		if (datasetType == null) {
			this.m_zTaskLogger.error("  input param datasetType is null");
			throw new NullPointerException("datasetType");
		}
		if (relationType == null) {
			this.m_zTaskLogger.error("  input param relationType is null");
			throw new NullPointerException("relationType");
		}

		this.m_zTaskLogger.debug("sourceComponent     "
				+ sourceComponent.getPropertyObject("object_string")
						.getStringValue());
		this.m_zTaskLogger.debug("  type              "
				+ sourceComponent.getTypeObject().getName());
		this.m_zTaskLogger.debug("sourceDataset       "
				+ sourceDataset.get_object_string());
		this.m_zTaskLogger.debug("  type              "
				+ sourceDataset.getTypeObject().getName());
		this.m_zTaskLogger.debug("datasetType         " + datasetType);
		this.m_zTaskLogger.debug("relationType        " + relationType);

		//String name = sourceDataset.get_object_name();
		String name = getNewSourceDatasetName(sourceDataset);
		int version = sourceDataset.get_revision_number();

		if ((name == null) || (name.length() == 0)) {
			this.m_zTaskLogger
					.error("  sourceDataset name is null or zero length");
			throw new NullPointerException("sourceDataset name");
		}

		if (version <= 0) {
			this.m_zTaskLogger.error("  sourceDataset version <= 0");
			throw new NullPointerException("sourceDataset version");
		}

		String typeName = null;
		Tool tool = null;

		DatasetTypeInfo[] zInfos = SoaHelper
				.getDatasetTypeInfo(datasetType);
		DatasetType dsType = zInfos[0].tag;

		dsType = (DatasetType) SoaHelper.getProperties(dsType, new String[] {
				"datasettype_name", "list_of_tools" });

		typeName = dsType.get_datasettype_name();

		if ((typeName == null) || (typeName.length() == 0)) {
			this.m_zTaskLogger
					.error("Dataset data type name is null or zero length");
			throw new NullPointerException("DatasetTypeInfoResponse typeName");
		}
		tool = dsType.get_list_of_tools()[0];

		if ((tool == null) || (tool.get_object_string().length() == 0)) {
			this.m_zTaskLogger
					.error("Dataset data tool name is null or zero length");
			throw new NullPointerException("DatasetTypeInfoResponse tool");
		}

		Dataset[] returnedObjects = null;

		ItemRevision sourceItemRevision = null;

		if (sourceComponent instanceof ItemRevision) {
			this.m_zTaskLogger
					.debug("Source component IS an instance of an Item Revision");
			sourceItemRevision = (ItemRevision) sourceComponent;
		} else {
			this.m_zTaskLogger
					.debug("Source component IS NOT an instance of an Item Revision.");
			this.m_zTaskLogger
					.debug("Checking for a related Item Revision using source dataset.");

			ModelObject[] arSourceDatasetsVersions = getAllRevisions((Dataset) sourceComponent);

			if ((arSourceDatasetsVersions.length > 0)
					&& (arSourceDatasetsVersions[0] instanceof Dataset)) {
				arSourceDatasetsVersions[0] = ((Dataset) SoaHelper
						.getProperties(arSourceDatasetsVersions[0],
								"item_revision"));

				sourceItemRevision = (ItemRevision) ((Dataset) arSourceDatasetsVersions[0])
						.get_item_revision();
			}
		}

		if (this.m_zTaskLogger.isDebugEnabled()) {
			this.m_zTaskLogger
					.debug("Calling Service - createDatasetOfVersion - START");
			this.m_zTaskLogger.debug("    source item revision = "
					+ sourceItemRevision.get_object_string());
			this.m_zTaskLogger.debug("    target dataset name  = " + name);
			this.m_zTaskLogger.debug("    target dataset type  = " + typeName);
			this.m_zTaskLogger.debug("    target dataset tool  = "
					+ tool.get_object_string());
			this.m_zTaskLogger.debug("    target dataset ver   = " + version);
		}

		returnedObjects = SoaHelper.createDatasetOfVersion(sourceItemRevision,
				name, typeName, tool, version);

		if (this.m_zTaskLogger.isDebugEnabled()) {
			this.m_zTaskLogger.debug("\tService returned "
					+ returnedObjects.length + " created datasets.");
			this.m_zTaskLogger
					.debug("Calling Service - createDatasetOfVersion - COMPLETE");
		}

		if ((returnedObjects == null) || (returnedObjects.length != 2)
				|| (returnedObjects[0] == null) || (returnedObjects[1] == null)) {
			this.m_zTaskLogger
					.error("SOA createDatasetOfVersion Service Failed. In some cases this could be because the DispatcherClient proxy user does not have adequate permissions to modify the ItemRev or Dataset.");

			throw new NullPointerException("createDatasetOfVersion");
		}

		Exception changeOwnerException = null;

		if (this.m_scOwner.equalsIgnoreCase("CAD")) {
			User user = (User) sourceDataset.get_owning_user();
			Group group = (Group) sourceDataset.get_owning_group();
			try {
				SoaHelper.changeOwner(returnedObjects[1], user, group);
			} catch (Exception e) {
				String scMsg = this.m_zRegistry
						.getString("DatasetHelper.chgNewRsltsDsOwnerFailed")
						+ " (" + returnedObjects[1].get_object_string() + ")";

				this.m_zTaskLogger.error(scMsg, e);

				changeOwnerException = e;
			}
		}

		try {
			this.m_zTaskLogger.debug("  adding to sourceComponent with "
					+ relationType + " relation");

			ModelObject[] datasetVersions = null;

			if (sourceComponent instanceof Dataset) {
				Dataset sourceComponentAsDataset = (Dataset) sourceComponent;

				this.m_zTaskLogger.debug("\tSource Component ("
						+ sourceComponentAsDataset.get_object_string()
						+ ") is of type: DATASET");

				datasetVersions = getAllRevisions(sourceComponentAsDataset);

				SoaHelper.createRelation(datasetVersions[0], relationType,
						returnedObjects[0]);
			} else if (sourceComponent instanceof ItemRevision) {
				datasetVersions = getAllRevisions(sourceDataset);

				this.m_zTaskLogger
						.debug("\tsource component "
								+ sourceComponent.getPropertyObject(
										"object_string").getStringValue()
								+ " is NOT a dataset, relating to all related item revisions.");
				
				RelationAndTypesFilter2[] azFilters = new RelationAndTypesFilter2[1];
				azFilters[0] = new RelationAndTypesFilter2();
				azFilters[0].objectTypeNames = new String[] { "ItemRevision" };
				ModelObject[] primaryComponents = SoaHelper.getChildrenPrimary(
						datasetVersions[0], azFilters);

				List sourceDatasetItemRevs = new ArrayList();

				boolean relatedSourceDataset = false;

				for (int iIndex = 0; iIndex < primaryComponents.length; ++iIndex) {
					if (!(primaryComponents[iIndex] instanceof ItemRevision))
						continue;
					if (sourceComponent == primaryComponents[iIndex]) {
						relatedSourceDataset = true;
					}

					sourceDatasetItemRevs
							.add((ItemRevision) primaryComponents[iIndex]);
				}

				if (relatedSourceDataset) {
					Iterator iter = sourceDatasetItemRevs.iterator();

					while (iter.hasNext()) {
						SoaHelper.createRelation((ModelObject) iter.next(),
								relationType, returnedObjects[0]);
					}

				} else {
					SoaHelper.createRelation(sourceComponent, relationType,
							returnedObjects[0]);
				}

			} else {
				throw new Exception(
						"Source Component is not a DATASET or an ITEM REVISION.");
			}
		} catch (Exception e) {
			this.m_zTaskLogger
					.error(this.m_zRegistry
							.getString("DatasetHelper.relationCreationFailed1")
							+ " "
							+ relationType
							+ " "
							+ this.m_zRegistry
									.getString("DatasetHelper.relationCreationFailed2")
							+ " "
							+ datasetType
							+ " "
							+ this.m_zRegistry
									.getString("DatasetHelper.relationCreationFailed3")
							+ " ("
							+ sourceComponent
									.getPropertyObject("object_string")
									.getStringValue()
							+ ","
							+ returnedObjects[0] + ")", e);

			throw new Exception(e.toString());
		}
		this.m_zTaskLogger.debug("  relation create complete");

		if (changeOwnerException != null) {
			throw new Exception(changeOwnerException.toString());
		}

		this.m_zTaskLogger.trace("createDataset() end", 0);

		return returnedObjects;
	}

	@Override
	protected void addNRFiles(Dataset dataset, String namedReference,
			String resultsDir, List<String> fileList, Dataset sourceDataset)
			throws Exception {

		this.m_zTaskLogger.trace("  addNRFiles() begin...", 0);

		String[] namedRefList = null;
		String[] refFormatList = null;
		String[] refTemplateList = null;

		dataset = (Dataset) SoaHelper.getProperties(dataset, "dataset_type");
		DatasetType zDsType = dataset.get_dataset_type();
		zDsType = (DatasetType) SoaHelper.getProperties(zDsType,
				"datasettype_name");
		String scDatasetType = zDsType.get_datasettype_name();

		DataManagement.DatasetTypeInfo[] zInfos = SoaHelper
				.getDatasetTypeInfo(scDatasetType);

		DataManagement.ReferenceInfo[] listOfNamedRefContext = null;
		if (zInfos.length > 0) {
			listOfNamedRefContext = zInfos[0].refInfos;
		}

		if (listOfNamedRefContext != null) {
			namedRefList = new String[listOfNamedRefContext.length];
			refFormatList = new String[listOfNamedRefContext.length];
			refTemplateList = new String[listOfNamedRefContext.length];

			for (int i = 0; i < listOfNamedRefContext.length; ++i) {
				namedRefList[i] = listOfNamedRefContext[i].referenceName;
				refFormatList[i] = listOfNamedRefContext[i].fileFormat;
				refTemplateList[i] = listOfNamedRefContext[i].fileExtension;
			}
		}

		int iCount = fileList.size();
		String[] filePathnames = new String[iCount];
		String[] fileTypes = new String[iCount];
		String[] namedReferences = new String[iCount];
		for (int i = 0; i < iCount; ++i) {
			String visFileName = (String) fileList.get(i);
			if (visFileName.regionMatches(0, File.separator, 0, 1)) {
				filePathnames[i] = resultsDir + visFileName;
			} else {
				filePathnames[i] = resultsDir + File.separator + visFileName;
			}

			this.m_zTaskLogger.debug("    filePathnames[" + i + "] = "
					+ filePathnames[i]);

			String incomingFileFormat = "BINARY";

			String incomingExtension = visFileName.substring(visFileName
					.lastIndexOf("."));

			if (listOfNamedRefContext != null) {
				for (int j = 0; j < listOfNamedRefContext.length; ++j) {
					int iIndex = refTemplateList[j].lastIndexOf(".");
					if (iIndex <= 0)
						continue;
					String namedReferenceExtension = refTemplateList[j]
							.substring(iIndex);

					if (!(incomingExtension
							.equalsIgnoreCase(namedReferenceExtension)))
						continue;
					incomingFileFormat = refFormatList[j];
					break;
				}

			}

			fileTypes[i] = incomingFileFormat;

			namedReferences[i] = namedReference;
		}

		if (this.m_qStoreInSrcVol) {
			this.m_zTaskLogger
					.debug("    Storing files into source data volume");
			setTCSessionVolume(sourceDataset);
		}

		try {
			dataset = (Dataset) SoaHelper.getProperties(dataset,
					"revision_number");
			int version = dataset.get_revision_number();
			this.m_zTaskLogger.debug("    setting files on "
					+ dataset.get_object_string() + ";" + version + "...");
			FileManagement.DatasetFileInfo[] azDFI = new FileManagement.DatasetFileInfo[filePathnames.length];
			for (int i = 0; i < filePathnames.length; ++i) {
				this.m_zTaskLogger.debug("   filePathnames[" + i + "]    "
						+ filePathnames[i]);
				this.m_zTaskLogger.debug("   fileTypes[" + i + "]        "
						+ fileTypes[i]);
				this.m_zTaskLogger.debug("   namedReferences[" + i + "]  "
						+ namedReferences[i]);
				azDFI[i] = new FileManagement.DatasetFileInfo();
				azDFI[i].allowReplace = this.m_qUpdateExistingVisData;
				azDFI[i].fileName = filePathnames[i];
				if (fileTypes[i].equalsIgnoreCase("BINARY")) {
					azDFI[i].isText = false;
				} else {
					azDFI[i].isText = true;
				}
				azDFI[i].namedReferencedName = namedReferences[i];
			}

			FileManagement.GetDatasetWriteTicketsInputData[] arg0 = new FileManagement.GetDatasetWriteTicketsInputData[1];
			arg0[0] = new FileManagement.GetDatasetWriteTicketsInputData();
			arg0[0].createNewVersion = false;
			arg0[0].dataset = dataset;
			arg0[0].datasetFileInfos = azDFI;

			TeamcenterServerProxy.getInstance().getFileMgtUtil().putFiles(arg0);
			this.m_zTaskLogger
					.debug("    Files attached to dataset through named references");
		} catch (Exception e) {
		} finally {
			this.m_zTaskLogger.debug("Restoring volume to default");
			restoreTCSessionVolume();
		}

		this.m_zTaskLogger.trace("  addNRFiles() end", 0);
	}

	@Override
	public int createInsertDataset(ItemRevision sourceItemRev,
			Dataset sourceDataset, String datasetType, String relationType,
			String namedReference, String resultsDir, List<String> fileList,
			boolean qOnSourceDataset) throws Exception {

		return super.createInsertDataset(sourceItemRev, sourceDataset, datasetType,
				relationType, namedReference, resultsDir, fileList, qOnSourceDataset);
	}

	@Override
	public void createInsertDataset(Vector<Vector> vctDatasetsAndRevs,
			String datasetType, String relationType, boolean qOnSourceDataset)
			throws Exception {

		super.createInsertDataset(vctDatasetsAndRevs, datasetType, relationType,
				qOnSourceDataset);
	}
	
	
}
