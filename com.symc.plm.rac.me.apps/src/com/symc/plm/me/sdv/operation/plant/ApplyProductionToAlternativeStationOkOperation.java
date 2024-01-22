package com.symc.plm.me.sdv.operation.plant;

import java.util.Map;

import javax.xml.bind.ValidationException;

import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.sdv.service.Plant.PlantUtilities;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.Registry;

public class ApplyProductionToAlternativeStationOkOperation extends AbstractSDVActionOperation {

    private boolean isValidOk = true;
    @SuppressWarnings("unused")
    private Registry registry;

    // private Boolean isAltPlant;
    private TCComponentBOMLine targetBOMLine;
    private String opareaMessage;

    public ApplyProductionToAlternativeStationOkOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
        registry = Registry.getRegistry(this);
    }

    public ApplyProductionToAlternativeStationOkOperation(String actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
        registry = Registry.getRegistry(this);
    }

    public ApplyProductionToAlternativeStationOkOperation(int actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, ownerId, parameters, dataset);
        registry = Registry.getRegistry(this);
    }

    @Override
    public void startOperation(String commandId) {
        IDataSet dataset = getDataSet();
        if (dataset.containsMap("ApplyAlternativeStationView")) {
            if (dataset.getDataMap("ApplyAlternativeStationView") != null) {
                RawDataMap rawDataMap = (RawDataMap) dataset.getDataMap("ApplyAlternativeStationView");

                targetBOMLine = (TCComponentBOMLine) rawDataMap.getValue("tcComponentBOMLine");

                if (targetBOMLine == null) {
                    opareaMessage = "Cant't find selected target BOMLine.";
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

            PlantUtilities.applyProductionBOMLine(targetBOMLine, targetBOMLine.parent());

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
