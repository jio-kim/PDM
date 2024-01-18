package com.symc.work.model;

public class PartBOMInfoVO {
    private PartInfoVO partInfoVO;
    private BOMChangeInfoVO bomChangeInfoVO;
    public PartInfoVO getPartInfoVO() {
        return partInfoVO;
    }
    public void setPartInfoVO(PartInfoVO partInfoVO) {
        this.partInfoVO = partInfoVO;
    }
    public BOMChangeInfoVO getBomChangeInfoVO() {
        return bomChangeInfoVO;
    }
    public void setBomChangeInfoVO(BOMChangeInfoVO bomChangeInfoVO) {
        this.bomChangeInfoVO = bomChangeInfoVO;
    }
}
