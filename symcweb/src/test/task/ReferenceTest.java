/**
 * 
 */
package test.task;

import org.junit.Assert;

import test.common.AbstractTcSoaTest;

import com.symc.common.soa.biz.TcDatasetUtil;
import com.symc.common.soa.biz.TcItemUtil;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.DatasetType;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.ItemRevision;

/**
 * Class Name : ReferenceTest
 * Class Description :
 * 
 * @date 2013. 9. 11.
 * 
 */
public class ReferenceTest extends AbstractTcSoaTest {
    @org.junit.Test
    public void test() {
        TcItemUtil tcItemUtil = new TcItemUtil(session);
        try {
            ItemRevision itemRev = tcItemUtil.getLatestRevItem("4781008D00");
            tcItemUtil.getProperties(new ModelObject[] { itemRev }, new String[] { "item_id" });
            System.out.println(itemRev.get_item_id());
            Assert.assertNotNull(itemRev.get_item_id());
            // 2. Revise
            ItemRevision reviseRev = tcItemUtil.reviseVPMIf(itemRev.getUid(), session);
            this.reviseDatasetFileList(itemRev, reviseRev);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reviseDatasetFileList(ItemRevision itemRev, ItemRevision reviseRev) throws Exception {
        TcItemUtil tcItemUtil = new TcItemUtil(session);
        TcDatasetUtil tcDatasetUtil = new TcDatasetUtil(session);
        // 이전 리비전 존재 여부 확인
        Dataset[] prevDatasets = null;
        if (itemRev != null) {
            prevDatasets = tcDatasetUtil.retrieveDatasetObjects(itemRev);
        }
        for (int i = 0; i < prevDatasets.length; i++) {
            Dataset prvDataset = prevDatasets[i];
            DatasetType prvDatasetType = prevDatasets[i].get_dataset_type();
            if (prvDatasetType == null) {
                throw new Exception("createDatasetFile check DatasetType Error..");
            }

            tcItemUtil.getProperties(new ModelObject[] { prvDatasetType }, new String[] { "datasettype_name" });
            Dataset reviseDataset = tcDatasetUtil.createDataSetOnItemRevision(reviseRev, prvDatasetType.get_datasettype_name(), null);

            tcItemUtil.getProperties(new ModelObject[] { prvDataset }, new String[] { "ref_list", "ref_names" });
            ModelObject[] refs = prvDataset.get_ref_list();
            for (int j = 0; i < refs.length; j++) {
                if (refs[j] instanceof ImanFile) {
                    ImanFile imanFile = (ImanFile) refs[j];
                    tcItemUtil.getProperties(new ModelObject[] { imanFile }, new String[] { "original_file_name" });
                    String catiaFileName = imanFile.get_original_file_name();
                    System.out.println(prvDatasetType.get_datasettype_name() + " :: " +catiaFileName);
                    String refName = tcDatasetUtil.getReferenecedTypeFromDataset(prvDataset, imanFile);
                    System.out.println(refName);
                    tcDatasetUtil.createRelation(reviseDataset, imanFile, refName);
                }
            }

        }
    }

}
