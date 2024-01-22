package com.symc.plm.rac.cme.biw.apa;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.AbstractAIFSession;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCException;

@SuppressWarnings("unused")
public class TestFunctionHandler extends AbstractHandler
implements IExecutableExtension{

    private final String serviceClassName = "com.ssangyong.service.BopPertService";

    @Override
    public Object execute(ExecutionEvent arg0) throws ExecutionException {

        TCComponentBOPLine selectedTarget;
        AbstractAIFSession session;

        AbstractAIFUIApplication abstractaifuiapplication = AIFDesktop.getActiveDesktop().getCurrentApplication();
        session = abstractaifuiapplication.getSession();
        AIFComponentContext aaifcomponentcontext[] = abstractaifuiapplication.getTargetContexts();

        selectedTarget = (TCComponentBOPLine)aaifcomponentcontext[0].getComponent();

        //selectedTarget.createAssemblyTree(paramBoolean1, paramBoolean2, paramTCComponentAppearanceGroup);

        try {
//            List<TCComponentBOMWindow> bomWindows = selectedTarget.window().getOccurrenceGroupWindows();
//            TCComponentBOMLine occTopBomLine = bomWindows.get(4).getTopBOMLine();



//            TCComponentAppearanceGroup occTopAppearanceGroup = (TCComponentAppearanceGroup)occTopBomLine.getComponentIdentifierInContext();

            //selectedTarget.createAssemblyTree(false, false, occTopAppearanceGroup);

            //TCComponentBOPLine SDF = (TCComponentBOPLine) selectedTarget.add(occTopAppearanceGroup, false, SDVTypeConstant.BOP_PROCESS_OCCURRENCE_GROUP);

//            TCComponent[] succcessorsComponent = selectedTarget.getReferenceListProperty(SDVPropertyConstant.ME_SUCCESSORS);
//            TCComponent[] decessorsComponent = selectedTarget.getReferenceListProperty(SDVPropertyConstant.ME_PREDECESSORS);
//            System.out.println("succcessorsComponent = " + succcessorsComponent.length);
//            System.out.println("decessorsComponent = " + decessorsComponent.length);
//
//            String sucdecc[] = selectedTarget.getSuccessorPredecessorPropertiesToHandle();
//            System.out.println("sucdecc[] = " + sucdecc);
//
//            TCComponentMEAppearancePathNode[] linkedAppearances = selectedTarget.askLinkedAppearances(false);
//
            TCComponentBOMWindow bopWindow = selectedTarget.window();
            TCComponentBOMLine topLine = bopWindow.getTopBOMLine();
            TCComponentBOMWindow bopWindow2 = topLine.window();
            //TCComponentItem s7_vehpart = SDVBOPUtilities.FindItem("5244135000", SDVTypeConstant.EBOM_VEH_PART);
            //TCComponentItemRevision s7_vehpart = CustomUtil.findLatestItemRevision(SDVTypeConstant.EBOM_VEH_PART_REV, "5244135000");

            //TCComponentBOMLine asdf[] = bopWindow2.findConfigedBOMLinesForAbsOccID("zZeJ2dEkoNUN7C", false, s7_vehpart);
            //TCComponentBOMLine[] SDFA = bopWindow2.findAppearance("RaeJ9xkOoNUN7C");
            TCComponentBOMLine[] SDFAa = bopWindow2.findConfigedBOMLinesForAbsOccID("DFRJPbd_o1W$GD", false, null);
            TCComponentBOMLine[] SDFAa3 = bopWindow2.findConfigedBOMLinesForAbsOccID("DFRJPbd_o1W$GD", false, topLine);
            TCComponentBOMLine[] SDFAa2 = bopWindow2.findConfigedBOMLinesForAbsOccID("DFRJPbd_o1W$GD", false, selectedTarget);

            //CustomUtil.getBopline(targetRevision, session);
            System.out.println("ASDSAD");
            //zdQJ2dEkoNUN7C


//            TCComponentBOMLine[] findBomLine = bopWindow.findAppearance("G7QJtT0Ho1W$GD");
//            TCComponentBOMLine[] findBomLine2 = bopWindow.findConfigedBOMLinesForAbsOccID("G7QJtT0Ho1W$GD", false, selectedTarget);
//            TCComponentBOMLine[] findBomLine3 = bopWindow2.findAppearance("G7QJtT0Ho1W$GD");
//            TCComponentBOMLine[] findBomLine4 = bopWindow2.findConfigedBOMLinesForAbsOccID("G7QJtT0Ho1W$GD", false, selectedTarget.getItemRevision());

//            TCComponentBOMLine[] findBomLine5 = bopWindow2.findConfigedBOMLinesForAbsOccID("G7QJtT0Ho1W$GD", false, selectedTarget);

//            for (TCComponentBOMLine tcComponentBOMLine : findBomLine) {
//                System.out.println("니가 찾는게 이게 맞냐 ? " +  tcComponentBOMLine.getObjectString());
//            }
//
//            for (TCComponentBOMLine tcComponentBOMLine2 : findBomLine2) {
//                System.out.println("니가 찾는게 이게 맞냐2 ? " +  tcComponentBOMLine2.getObjectString());
//            }



//            TCSession tcSession = CustomUtil.getTCSession();
//
//            TCPreferenceService tcpreferenceservice = tcSession.getPreferenceService();
//            //String portalWebServer = tcpreferenceservice.getString(0, "PE_IF_HOST_IP");
//            String portalWebServer = "localhost";
//            String WAS_URL = "http://" + portalWebServer + "/symcweb/remote/invoke.do";

//            SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil();
//
//
//            DataSet ds = new DataSet();
//            //ArrayList<HashMap> noTransInfo = null;
//            ds.put("PRODUCT_ID", "PVXA2015");
//            ds.put("SHOP_ID", "PTP-B1-PVXA2015");
//            ds.put("STATION_ID", "PTP-B1-BM-510-PVXA2015-00");
//
//            ArrayList<HashMap<String, Object>> results;
//
//            results = (ArrayList<HashMap<String, Object>>) remoteUtil.execute(serviceClassName, "selectBopPertList", ds);
//            for (HashMap<String, Object> result : results) {
//                System.out.println(result.get("ID") + " = " + result.get("PUID"));
//                System.out.println(result.get("ID") + " = " + result.get("REV_PUID"));
//                System.out.println(result.get("ID") + " = " + result.get("OCC_ID"));
//
//            }

            //ArrayList<HashMap> functionList = (ArrayList<HashMap>)remoteUtil.execute("com.symc.remote.service.TcInterfaceService", "getFunctionList", ds);
            //ArrayList<HashMap> bopList2 = (ArrayList<HashMap>)remoteUtil.execute("com.symc.remote.service.TcInterfaceService", "getBopPertList2", ds);
            //ArrayList<HashMap> bopList = (ArrayList<HashMap>)remoteUtil.execute("com.symc.remote.service.TcInterfaceService", "getBopPertList", ds);
            //ArrayList<HashMap> functionList = (ArrayList<HashMap>)remoteUtil.execute("com.symc.remote.service.TcInterfaceService", "getBopPertList", ds);
//            for (int j = 0; j < functionList.size(); j++) {
//                HashMap sadZ = functionList.get(j);
//                String sadd = (String) sadZ.get("ID");
//                System.out.println("ItemName = " + j + " = " + sadd);
//            }

            System.out.println("END~!");
        } catch (TCException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void setInitializationData(IConfigurationElement arg0, String arg1, Object arg2) throws CoreException {

    }

}
