package com.symc.plm.me.sdv.operation.plant;

import java.util.Map;

import javax.xml.bind.ValidationException;

import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.sdv.service.Plant.PlantUtilities;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.Registry;

public class CopyProductionToAlternativeStationOkOperation extends AbstractSDVActionOperation {

    private boolean isValidOk = true;
    private Registry registry;

    private String altPrefix = "";
    // private Boolean isAltPlant;
    private TCComponentBOMLine targetBOMLine;
    private String opareaMessage;

    public CopyProductionToAlternativeStationOkOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
        registry = Registry.getRegistry(this);
    }

    public CopyProductionToAlternativeStationOkOperation(String actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
        registry = Registry.getRegistry(this);
    }

    public CopyProductionToAlternativeStationOkOperation(int actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, ownerId, parameters, dataset);
        registry = Registry.getRegistry(this);
    }

    @Override
    public void startOperation(String commandId) {
        IDataSet dataset = getDataSet();
        if (dataset.containsMap("CopyAlternativeStationView")) {
            if (dataset.getDataMap("CopyAlternativeStationView") != null) {
                RawDataMap rawDataMap = (RawDataMap) dataset.getDataMap("CopyAlternativeStationView");

                targetBOMLine = (TCComponentBOMLine) rawDataMap.getValue("tcComponentBOMLine");
                // isAltPlant = (Boolean) rawDataMap.getValue(registry.getString("Plant.IsAltPlant.Name", "Is Alt Plant"));
                altPrefix = rawDataMap.getStringValue(registry.getString("Plant.AltPrefix.NAME", "Alt Prefix"));

                if (targetBOMLine == null) {
                    opareaMessage = "Cant't find selected target BOMLine.";
                    isValidOk = false;
                }

                if (altPrefix == null || altPrefix.length() == 0) {
                    opareaMessage = "Not loaded Alt Prefix.";
                    isValidOk = false;
                }
                
                if(altPrefix.length() != 4) {
                    opareaMessage = "Alt Prefix character length must be 4.";
                    isValidOk = false;
                }
            }
        }
    }

    @Override
    public void executeOperation() throws Exception {
        try {
            if (!isValidOk) {
                throw new ValidationException((opareaMessage == null) ? "Failed to create the Alternative Station" : opareaMessage);
            }

            PlantUtilities.createAlternativeBOMLine(targetBOMLine, targetBOMLine.parent(), altPrefix);

        } catch (Exception e) {
            this.setExecuteResult(FAIL);
            this.setExecuteError(e);
            this.setErrorMessage("오류가 발생했습니다.");
        }
    }

    @Override
    public void endOperation() {

    }
}
