package com.kgm.dto;

import java.io.Serializable;

public class TCEcoModel implements Serializable {

    private static final long serialVersionUID = 1L;
    private String seq;
    private String plantCode;
    private String ecoNo;
    private String changeReason;
    private String owningTeam;
    private String owningUser;
    private String ownerTel;
    private String approvalUser;
    private String approvalTel;
    private String ecoStatus;
    private String releaseDate;
    private String ecrNo;
    private String ecrDate;
    private String ecrDept;
    private String eciNo;
    private String eciReleaseDate;
    private String eciDept;
    private String createDate;
    //2018 04-24 add laho been
    private String ecoDesc;
    
    private String regNsafe;
    private String cfgEffectPoint;
    private String concurrentImpl;
    private String ecoTitle;
    
    private String requestDate;
    private String requestLT;
    private String costDate;
    private String costLT;
    private String techDate;
    private String techLT;
    private String purcDate;
    private String purcLT;
    private String inApprovalDate;
    private String inApprovalLT;
    private String approvedDate;
    private String approvedLT;
    private String completedDate;
    private String completedLT;
    
    private String ecotype;
    
    private String affectedProject;
    private String costUser;
    private String costTeam;
    private String costTel;
    private String techUser;
    private String techTeam;
    private String techTel;
    private String purcUser;
    private String purcTeam;
    private String purcTel;
    private String inApprovalUser;
    private String inApprovalTeam;
    private String inApprovalTel;
    private String approvedUser;
    private String approvedTeam;
    private String approvedTel;
    private String completedUser;
    private String completedTeam;
    private String completedTel;

    /**
     * @return Returns the approvedTeam.
     */
    public String getApprovedTeam() {
        if (this.approvedTeam == null) {
            this.approvedTeam = "";
        }
    
        return this.approvedTeam;
    }
    /**
     * @param approvedTeam The approvedTeam to set.
     */
    public void setApprovedTeam(String approvedTeam) {
        if (approvedTeam == null)
            this.approvedTeam = "";
        else
            this.approvedTeam = approvedTeam;
    }
    /**
     * @return Returns the approvedTel.
     */
    public String getApprovedTel() {
        if (this.approvedTel == null) {
            this.approvedTel = "";
        }
    
        return this.approvedTel;
    }
    /**
     * @param approvedTel The approvedTel to set.
     */
    public void setApprovedTel(String approvedTel) {
        if (approvedTel == null)
            this.approvedTel = "";
        else
            this.approvedTel = approvedTel;
    }
    /**
     * @return Returns the approvedUser.
     */
    public String getApprovedUser() {
        if (this.approvedUser == null) {
            this.approvedUser = "";
        }
    
        return this.approvedUser;
    }
    /**
     * @param approvedUser The approvedUser to set.
     */
    public void setApprovedUser(String approvedUser) {
        if (approvedUser == null)
            this.approvedUser = "";
        else
            this.approvedUser = approvedUser;
    }
    /**
     * @return Returns the completedTeam.
     */
    public String getCompletedTeam() {
        if (this.completedTeam == null) {
            this.completedTeam = "";
        }
    
        return this.completedTeam;
    }
    /**
     * @param completedTeam The completedTeam to set.
     */
    public void setCompletedTeam(String completedTeam) {
        if (completedTeam == null)
            this.completedTeam = "";
        else
            this.completedTeam = completedTeam;
    }
    /**
     * @return Returns the completedTel.
     */
    public String getCompletedTel() {
        if (this.completedTel == null) {
            this.completedTel = "";
        }
    
        return this.completedTel;
    }
    /**
     * @param completedTel The completedTel to set.
     */
    public void setCompletedTel(String completedTel) {
        if (completedTel == null)
            this.completedTel = "";
        else
            this.completedTel = completedTel;
    }
    /**
     * @return Returns the completedUser.
     */
    public String getCompletedUser() {
        if (this.completedUser == null) {
            this.completedUser = "";
        }
    
        return this.completedUser;
    }
    /**
     * @param completedUser The completedUser to set.
     */
    public void setCompletedUser(String completedUser) {
        if (completedUser == null)
            this.completedUser = "";
        else
            this.completedUser = completedUser;
    }
    /**
     * @return Returns the costTeam.
     */
    public String getCostTeam() {
        if (this.costTeam == null) {
            this.costTeam = "";
        }
    
        return this.costTeam;
    }
    /**
     * @param costTeam The costTeam to set.
     */
    public void setCostTeam(String costTeam) {
        if (costTeam == null)
            this.costTeam = "";
        else
            this.costTeam = costTeam;
    }
    /**
     * @return Returns the costTel.
     */
    public String getCostTel() {
        if (this.costTel == null) {
            this.costTel = "";
        }
    
        return this.costTel;
    }
    /**
     * @param costTel The costTel to set.
     */
    public void setCostTel(String costTel) {
        if (costTel == null)
            this.costTel = "";
        else
            this.costTel = costTel;
    }
    /**
     * @return Returns the costUser.
     */
    public String getCostUser() {
        if (this.costUser == null) {
            this.costUser = "";
        }
    
        return this.costUser;
    }
    /**
     * @param costUser The costUser to set.
     */
    public void setCostUser(String costUser) {
        if (costUser == null)
            this.costUser = "";
        else
            this.costUser = costUser;
    }
    /**
     * @return Returns the inApprovalTeam.
     */
    public String getInApprovalTeam() {
        if (this.inApprovalTeam == null) {
            this.inApprovalTeam = "";
        }
    
        return this.inApprovalTeam;
    }
    /**
     * @param inApprovalTeam The inApprovalTeam to set.
     */
    public void setInApprovalTeam(String inApprovalTeam) {
        if (inApprovalTeam == null)
            this.inApprovalTeam = "";
        else
            this.inApprovalTeam = inApprovalTeam;
    }
    /**
     * @return Returns the inApprovalTel.
     */
    public String getInApprovalTel() {
        if (this.inApprovalTel == null) {
            this.inApprovalTel = "";
        }
    
        return this.inApprovalTel;
    }
    /**
     * @param inApprovalTel The inApprovalTel to set.
     */
    public void setInApprovalTel(String inApprovalTel) {
        if (inApprovalTel == null)
            this.inApprovalTel = "";
        else
            this.inApprovalTel = inApprovalTel;
    }
    /**
     * @return Returns the inApprovalUser.
     */
    public String getInApprovalUser() {
        if (this.inApprovalUser == null) {
            this.inApprovalUser = "";
        }
    
        return this.inApprovalUser;
    }
    /**
     * @param inApprovalUser The inApprovalUser to set.
     */
    public void setInApprovalUser(String inApprovalUser) {
        if (inApprovalUser == null)
            this.inApprovalUser = "";
        else
            this.inApprovalUser = inApprovalUser;
    }
    /**
     * @return Returns the purcTeam.
     */
    public String getPurcTeam() {
        if (this.purcTeam == null) {
            this.purcTeam = "";
        }
    
        return this.purcTeam;
    }
    /**
     * @param purcTeam The purcTeam to set.
     */
    public void setPurcTeam(String purcTeam) {
        if (purcTeam == null)
            this.purcTeam = "";
        else
            this.purcTeam = purcTeam;
    }
    /**
     * @return Returns the purcTel.
     */
    public String getPurcTel() {
        if (this.purcTel == null) {
            this.purcTel = "";
        }
    
        return this.purcTel;
    }
    /**
     * @param purcTel The purcTel to set.
     */
    public void setPurcTel(String purcTel) {
        if (purcTel == null)
            this.purcTel = "";
        else
            this.purcTel = purcTel;
    }
    /**
     * @return Returns the purcUser.
     */
    public String getPurcUser() {
        if (this.purcUser == null) {
            this.purcUser = "";
        }
    
        return this.purcUser;
    }
    /**
     * @param purcUser The purcUser to set.
     */
    public void setPurcUser(String purcUser) {
        if (purcUser == null)
            this.purcUser = "";
        else
            this.purcUser = purcUser;
    }
    /**
     * @return Returns the techTeam.
     */
    public String getTechTeam() {
        if (this.techTeam == null) {
            this.techTeam = "";
        }
    
        return this.techTeam;
    }
    /**
     * @param techTeam The techTeam to set.
     */
    public void setTechTeam(String techTeam) {
        if (techTeam == null)
            this.techTeam = "";
        else
            this.techTeam = techTeam;
    }
    /**
     * @return Returns the techTel.
     */
    public String getTechTel() {
        if (this.techTel == null) {
            this.techTel = "";
        }
    
        return this.techTel;
    }
    /**
     * @param techTel The techTel to set.
     */
    public void setTechTel(String techTel) {
        if (techTel == null)
            this.techTel = "";
        else
            this.techTel = techTel;
    }
    /**
     * @return Returns the techUser.
     */
    public String getTechUser() {
        if (this.techUser == null) {
            this.techUser = "";
        }
    
        return this.techUser;
    }
    /**
     * @param techUser The techUser to set.
     */
    public void setTechUser(String techUser) {
        if (techUser == null)
            this.techUser = "";
        else
            this.techUser = techUser;
    }
    /**
     * @return Returns the affectedProject.
     */
    public String getAffectedProject() {
        if (this.affectedProject == null) {
            this.affectedProject = "";
        }
    
        return this.affectedProject;
    }
    /**
     * @param affectedProject The affectedProject to set.
     */
    public void setAffectedProject(String affectedProject) {
        if (affectedProject == null)
            this.affectedProject = "";
        else
            this.affectedProject = affectedProject;
    }
    /**
     * @return Returns the ecotype.
     */
    public String getEcotype() {
        if (this.ecotype == null) {
            this.ecotype = "";
        }
    
        return this.ecotype;
    }
    /**
     * @param ecotype The ecotype to set.
     */
    public void setEcotype(String ecotype) {
        if (ecotype == null)
            this.ecotype = "";
        else
            this.ecotype = ecotype;
    }
    /**
     * @return Returns the approvalTel.
     */
    public String getApprovalTel() {
        if (this.approvalTel == null) {
            this.approvalTel = "";
        }
    
        return this.approvalTel;
    }
    /**
     * @param approvalTel The approvalTel to set.
     */
    public void setApprovalTel(String approvalTel) {
        if (approvalTel == null)
            this.approvalTel = "";
        else
            this.approvalTel = approvalTel;
    }
    /**
     * @return Returns the approvalUser.
     */
    public String getApprovalUser() {
        if (this.approvalUser == null) {
            this.approvalUser = "";
        }
    
        return this.approvalUser;
    }
    /**
     * @param approvalUser The approvalUser to set.
     */
    public void setApprovalUser(String approvalUser) {
        if (approvalUser == null)
            this.approvalUser = "";
        else
            this.approvalUser = approvalUser;
    }
    /**
     * @return Returns the approvedDate.
     */
    public String getApprovedDate() {
        if (this.approvedDate == null) {
            this.approvedDate = "";
        }
    
        return this.approvedDate;
    }
    /**
     * @param approvedDate The approvedDate to set.
     */
    public void setApprovedDate(String approvedDate) {
        if (approvedDate == null)
            this.approvedDate = "";
        else
            this.approvedDate = approvedDate;
    }
    /**
     * @return Returns the approvedLT.
     */
    public String getApprovedLT() {
        if (this.approvedLT == null) {
            this.approvedLT = "";
        }
    
        return this.approvedLT;
    }
    /**
     * @param approvedLT The approvedLT to set.
     */
    public void setApprovedLT(String approvedLT) {
        if (approvedLT == null)
            this.approvedLT = "";
        else
            this.approvedLT = approvedLT;
    }
    /**
     * @return Returns the cfgEffectPoint.
     */
    public String getCfgEffectPoint() {
        if (this.cfgEffectPoint == null) {
            this.cfgEffectPoint = "";
        }
    
        return this.cfgEffectPoint;
    }
    /**
     * @param cfgEffectPoint The cfgEffectPoint to set.
     */
    public void setCfgEffectPoint(String cfgEffectPoint) {
        if (cfgEffectPoint == null)
            this.cfgEffectPoint = "";
        else
            this.cfgEffectPoint = cfgEffectPoint;
    }
    /**
     * @return Returns the changeReason.
     */
    public String getChangeReason() {
        if (this.changeReason == null) {
            this.changeReason = "";
        }
    
        return this.changeReason;
    }
    /**
     * @param changeReason The changeReason to set.
     */
    public void setChangeReason(String changeReason) {
        if (changeReason == null)
            this.changeReason = "";
        else
            this.changeReason = changeReason;
    }
    /**
     * @return Returns the completedDate.
     */
    public String getCompletedDate() {
        if (this.completedDate == null) {
            this.completedDate = "";
        }
    
        return this.completedDate;
    }
    /**
     * @param completedDate The completedDate to set.
     */
    public void setCompletedDate(String completedDate) {
        if (completedDate == null)
            this.completedDate = "";
        else
            this.completedDate = completedDate;
    }
    /**
     * @return Returns the completedLT.
     */
    public String getCompletedLT() {
        if (this.completedLT == null) {
            this.completedLT = "";
        }
    
        return this.completedLT;
    }
    /**
     * @param completedLT The completedLT to set.
     */
    public void setCompletedLT(String completedLT) {
        if (completedLT == null)
            this.completedLT = "";
        else
            this.completedLT = completedLT;
    }
    /**
     * @return Returns the concurrentImpl.
     */
    public String getConcurrentImpl() {
        if (this.concurrentImpl == null) {
            this.concurrentImpl = "";
        }
    
        return this.concurrentImpl;
    }
    /**
     * @param concurrentImpl The concurrentImpl to set.
     */
    public void setConcurrentImpl(String concurrentImpl) {
        if (concurrentImpl == null)
            this.concurrentImpl = "";
        else
            this.concurrentImpl = concurrentImpl;
    }
    /**
     * @return Returns the costDate.
     */
    public String getCostDate() {
        if (this.costDate == null) {
            this.costDate = "";
        }
    
        return this.costDate;
    }
    /**
     * @param costDate The costDate to set.
     */
    public void setCostDate(String costDate) {
        if (costDate == null)
            this.costDate = "";
        else
            this.costDate = costDate;
    }
    /**
     * @return Returns the costLT.
     */
    public String getCostLT() {
        if (this.costLT == null) {
            this.costLT = "";
        }
    
        return this.costLT;
    }
    /**
     * @param costLT The costLT to set.
     */
    public void setCostLT(String costLT) {
        if (costLT == null)
            this.costLT = "";
        else
            this.costLT = costLT;
    }
    /**
     * @return Returns the createDate.
     */
    public String getCreateDate() {
        if (this.createDate == null) {
            this.createDate = "";
        }
    
        return this.createDate;
    }
    /**
     * @param createDate The createDate to set.
     */
    public void setCreateDate(String createDate) {
        if (createDate == null)
            this.createDate = "";
        else
            this.createDate = createDate;
    }
    /**
     * @return Returns the eciDept.
     */
    public String getEciDept() {
        if (this.eciDept == null) {
            this.eciDept = "";
        }
    
        return this.eciDept;
    }
    /**
     * @param eciDept The eciDept to set.
     */
    public void setEciDept(String eciDept) {
        if (eciDept == null)
            this.eciDept = "";
        else
            this.eciDept = eciDept;
    }
    /**
     * @return Returns the eciNo.
     */
    public String getEciNo() {
        if (this.eciNo == null) {
            this.eciNo = "";
        }
    
        return this.eciNo;
    }
    /**
     * @param eciNo The eciNo to set.
     */
    public void setEciNo(String eciNo) {
        if (eciNo == null)
            this.eciNo = "";
        else
            this.eciNo = eciNo;
    }
    /**
     * @return Returns the eciReleaseDate.
     */
    public String getEciReleaseDate() {
        if (this.eciReleaseDate == null) {
            this.eciReleaseDate = "";
        }
    
        return this.eciReleaseDate;
    }
    /**
     * @param eciReleaseDate The eciReleaseDate to set.
     */
    public void setEciReleaseDate(String eciReleaseDate) {
        if (eciReleaseDate == null)
            this.eciReleaseDate = "";
        else
            this.eciReleaseDate = eciReleaseDate;
    }
    /**
     * @return Returns the ecoNo.
     */
    public String getEcoNo() {
        if (this.ecoNo == null) {
            this.ecoNo = "";
        }
    
        return this.ecoNo;
    }
    /**
     * @param ecoNo The ecoNo to set.
     */
    public void setEcoNo(String ecoNo) {
        if (ecoNo == null)
            this.ecoNo = "";
        else
            this.ecoNo = ecoNo;
    }
    /**
     * @return Returns the ecoStatus.
     */
    public String getEcoStatus() {
        if (this.ecoStatus == null) {
            this.ecoStatus = "";
        }
    
        return this.ecoStatus;
    }
    /**
     * @param ecoStatus The ecoStatus to set.
     */
    public void setEcoStatus(String ecoStatus) {
        if (ecoStatus == null)
            this.ecoStatus = "";
        else
            this.ecoStatus = ecoStatus;
    }
    /**
     * @return Returns the ecoTitle.
     */
    public String getEcoTitle() {
        if (this.ecoTitle == null) {
            this.ecoTitle = "";
        }
    
        return this.ecoTitle;
    }
    /**
     * @param ecoTitle The ecoTitle to set.
     */
    public void setEcoTitle(String ecoTitle) {
        if (ecoTitle == null)
            this.ecoTitle = "";
        else
            this.ecoTitle = ecoTitle;
    }
    /**
     * @return Returns the ecrDate.
     */
    public String getEcrDate() {
        if (this.ecrDate == null) {
            this.ecrDate = "";
        }
    
        return this.ecrDate;
    }
    /**
     * @param ecrDate The ecrDate to set.
     */
    public void setEcrDate(String ecrDate) {
        if (ecrDate == null)
            this.ecrDate = "";
        else
            this.ecrDate = ecrDate;
    }
    /**
     * @return Returns the ecrDept.
     */
    public String getEcrDept() {
        if (this.ecrDept == null) {
            this.ecrDept = "";
        }
    
        return this.ecrDept;
    }
    /**
     * @param ecrDept The ecrDept to set.
     */
    public void setEcrDept(String ecrDept) {
        if (ecrDept == null)
            this.ecrDept = "";
        else
            this.ecrDept = ecrDept;
    }
    /**
     * @return Returns the ecrNo.
     */
    public String getEcrNo() {
        if (this.ecrNo == null) {
            this.ecrNo = "";
        }
    
        return this.ecrNo;
    }
    /**
     * @param ecrNo The ecrNo to set.
     */
    public void setEcrNo(String ecrNo) {
        if (ecrNo == null)
            this.ecrNo = "";
        else
            this.ecrNo = ecrNo;
    }
    /**
     * @return Returns the inApprovalDate.
     */
    public String getInApprovalDate() {
        if (this.inApprovalDate == null) {
            this.inApprovalDate = "";
        }
    
        return this.inApprovalDate;
    }
    /**
     * @param inApprovalDate The inApprovalDate to set.
     */
    public void setInApprovalDate(String inApprovalDate) {
        if (inApprovalDate == null)
            this.inApprovalDate = "";
        else
            this.inApprovalDate = inApprovalDate;
    }
    /**
     * @return Returns the inApprovalLT.
     */
    public String getInApprovalLT() {
        if (this.inApprovalLT == null) {
            this.inApprovalLT = "";
        }
    
        return this.inApprovalLT;
    }
    /**
     * @param inApprovalLT The inApprovalLT to set.
     */
    public void setInApprovalLT(String inApprovalLT) {
        if (inApprovalLT == null)
            this.inApprovalLT = "";
        else
            this.inApprovalLT = inApprovalLT;
    }
    /**
     * @return Returns the ownerTel.
     */
    public String getOwnerTel() {
        if (this.ownerTel == null) {
            this.ownerTel = "";
        }
    
        return this.ownerTel;
    }
    /**
     * @param ownerTel The ownerTel to set.
     */
    public void setOwnerTel(String ownerTel) {
        if (ownerTel == null)
            this.ownerTel = "";
        else
            this.ownerTel = ownerTel;
    }
    /**
     * @return Returns the owningTeam.
     */
    public String getOwningTeam() {
        if (this.owningTeam == null) {
            this.owningTeam = "";
        }
    
        return this.owningTeam;
    }
    /**
     * @param owningTeam The owningTeam to set.
     */
    public void setOwningTeam(String owningTeam) {
        if (owningTeam == null)
            this.owningTeam = "";
        else
            this.owningTeam = owningTeam;
    }
    /**
     * @return Returns the owningUser.
     */
    public String getOwningUser() {
        if (this.owningUser == null) {
            this.owningUser = "";
        }
    
        return this.owningUser;
    }
    /**
     * @param owningUser The owningUser to set.
     */
    public void setOwningUser(String owningUser) {
        if (owningUser == null)
            this.owningUser = "";
        else
            this.owningUser = owningUser;
    }
    /**
     * @return Returns the plantCode.
     */
    public String getPlantCode() {
        if (this.plantCode == null) {
            this.plantCode = "";
        }
    
        return this.plantCode;
    }
    /**
     * @param plantCode The plantCode to set.
     */
    public void setPlantCode(String plantCode) {
        if (plantCode == null)
            this.plantCode = "";
        else
            this.plantCode = plantCode;
    }
    /**
     * @return Returns the purcDate.
     */
    public String getPurcDate() {
        if (this.purcDate == null) {
            this.purcDate = "";
        }
    
        return this.purcDate;
    }
    /**
     * @param purcDate The purcDate to set.
     */
    public void setPurcDate(String purcDate) {
        if (purcDate == null)
            this.purcDate = "";
        else
            this.purcDate = purcDate;
    }
    /**
     * @return Returns the purcLT.
     */
    public String getPurcLT() {
        if (this.purcLT == null) {
            this.purcLT = "";
        }
    
        return this.purcLT;
    }
    /**
     * @param purcLT The purcLT to set.
     */
    public void setPurcLT(String purcLT) {
        if (purcLT == null)
            this.purcLT = "";
        else
            this.purcLT = purcLT;
    }
    /**
     * @return Returns the regNsafe.
     */
    public String getRegNsafe() {
        if (this.regNsafe == null) {
            this.regNsafe = "";
        }
    
        return this.regNsafe;
    }
    /**
     * @param regNsafe The regNsafe to set.
     */
    public void setRegNsafe(String regNsafe) {
        if (regNsafe == null)
            this.regNsafe = "";
        else
            this.regNsafe = regNsafe;
    }
    /**
     * @return Returns the releaseDate.
     */
    public String getReleaseDate() {
        if (this.releaseDate == null) {
            this.releaseDate = "";
        }
    
        return this.releaseDate;
    }
    /**
     * @param releaseDate The releaseDate to set.
     */
    public void setReleaseDate(String releaseDate) {
        if (releaseDate == null)
            this.releaseDate = "";
        else
            this.releaseDate = releaseDate;
    }
    /**
     * @return Returns the requestDate.
     */
    public String getRequestDate() {
        if (this.requestDate == null) {
            this.requestDate = "";
        }
    
        return this.requestDate;
    }
    /**
     * @param requestDate The requestDate to set.
     */
    public void setRequestDate(String requestDate) {
        if (requestDate == null)
            this.requestDate = "";
        else
            this.requestDate = requestDate;
    }
    /**
     * @return Returns the requestLT.
     */
    public String getRequestLT() {
        if (this.requestLT == null) {
            this.requestLT = "";
        }
    
        return this.requestLT;
    }
    /**
     * @param requestLT The requestLT to set.
     */
    public void setRequestLT(String requestLT) {
        if (requestLT == null)
            this.requestLT = "";
        else
            this.requestLT = requestLT;
    }
    /**
     * @return Returns the seq.
     */
    public String getSeq() {
        if (this.seq == null) {
            this.seq = "";
        }
    
        return this.seq;
    }
    /**
     * @param seq The seq to set.
     */
    public void setSeq(String seq) {
        if (seq == null)
            this.seq = "";
        else
            this.seq = seq;
    }
    /**
     * @return Returns the techDate.
     */
    public String getTechDate() {
        if (this.techDate == null) {
            this.techDate = "";
        }
    
        return this.techDate;
    }
    /**
     * @param techDate The techDate to set.
     */
    public void setTechDate(String techDate) {
        if (techDate == null)
            this.techDate = "";
        else
            this.techDate = techDate;
    }
    /**
     * @return Returns the techLT.
     */
    public String getTechLT() {
        if (this.techLT == null) {
            this.techLT = "";
        }
    
        return this.techLT;
    }
    /**
     * @param techLT The techLT to set.
     */
    public void setTechLT(String techLT) {
        if (techLT == null)
            this.techLT = "";
        else
            this.techLT = techLT;
    }
	/**
	 * @return the ecoDesc
	 */
	public String getEcoDesc() {
		return ecoDesc;
	}
	/**
	 * @param ecoDesc the ecoDesc to set
	 */
	public void setEcoDesc(String ecoDesc) {
		if (ecoDesc == null)
            this.ecoDesc = "";
        else
            this.ecoDesc = ecoDesc;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TCEcoModel [seq=" + seq + ", plantCode=" + plantCode + ", ecoNo=" + ecoNo + ", changeReason=" + changeReason + ", owningTeam=" + owningTeam
				+ ", owningUser=" + owningUser + ", ownerTel=" + ownerTel + ", approvalUser=" + approvalUser + ", approvalTel=" + approvalTel + ", ecoStatus="
				+ ecoStatus + ", releaseDate=" + releaseDate + ", ecrNo=" + ecrNo + ", ecrDate=" + ecrDate + ", ecrDept=" + ecrDept + ", eciNo=" + eciNo
				+ ", eciReleaseDate=" + eciReleaseDate + ", eciDept=" + eciDept + ", createDate=" + createDate + ", ecoDesc=" + ecoDesc + ", regNsafe="
				+ regNsafe + ", cfgEffectPoint=" + cfgEffectPoint + ", concurrentImpl=" + concurrentImpl + ", ecoTitle=" + ecoTitle + ", requestDate="
				+ requestDate + ", requestLT=" + requestLT + ", costDate=" + costDate + ", costLT=" + costLT + ", techDate=" + techDate + ", techLT=" + techLT
				+ ", purcDate=" + purcDate + ", purcLT=" + purcLT + ", inApprovalDate=" + inApprovalDate + ", inApprovalLT=" + inApprovalLT + ", approvedDate="
				+ approvedDate + ", approvedLT=" + approvedLT + ", completedDate=" + completedDate + ", completedLT=" + completedLT + ", ecotype=" + ecotype
				+ ", affectedProject=" + affectedProject + ", costUser=" + costUser + ", costTeam=" + costTeam + ", costTel=" + costTel + ", techUser="
				+ techUser + ", techTeam=" + techTeam + ", techTel=" + techTel + ", purcUser=" + purcUser + ", purcTeam=" + purcTeam + ", purcTel=" + purcTel
				+ ", inApprovalUser=" + inApprovalUser + ", inApprovalTeam=" + inApprovalTeam + ", inApprovalTel=" + inApprovalTel + ", approvedUser="
				+ approvedUser + ", approvedTeam=" + approvedTeam + ", approvedTel=" + approvedTel + ", completedUser=" + completedUser + ", completedTeam="
				+ completedTeam + ", completedTel=" + completedTel + "]";
	}
	
	
    
    
    
}
