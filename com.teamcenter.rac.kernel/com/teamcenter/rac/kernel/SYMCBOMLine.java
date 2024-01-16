package com.teamcenter.rac.kernel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;

import com.ssangyong.rac.kernel.InterfaceSYMCECOSelect;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.ConfirmationDialog;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.log.Debug;
import com.teamcenter.services.loose.bom.StructureManagementService;
import com.teamcenter.services.loose.bom._2008_06.StructureManagement;
import com.teamcenter.services.loose.structuremanagement.IncrementalChangeService;
import com.teamcenter.services.loose.structuremanagement._2012_02.IncrementalChange;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;

/**
 * [20170208] Order No 사용자 수정못하도록 메세지 처리함
 */
public class SYMCBOMLine extends TCComponentBOMLine {
	
	private TCComponentItemRevision ecoRev = null;

    private boolean refreshRequired;
    private boolean isAdded = false;
    
    /**
     * [20140113][jclee] 수량 정보 반환 수정.
     * TC10 Upgrade 대응.
     *  - 단위가 EA인 경우 소수점 이하는 표시하지 않는다.
     */
    public String getProperty(String propName) throws TCException {
//    	if(propName == null || propName.equals("") || getCachedWindow().isWindowClosed()){
//    		return "";
//    	}
    	
        if(propName.equals("bl_quantity")) {
        	TCComponentUnitOfMeasure uom = (TCComponentUnitOfMeasure)getReferenceProperty("bl_uom");
        	String sUOM = "";
            String value = super.getProperty("bl_quantity");
            
            if(value.equals("1.0") || value.equals("1.00")) {
                return "1";
            }
            
            if (uom != null) {
            	sUOM = uom.getProperty("symbol");
			}
            
            if (sUOM.equals("EA")) {
            	String sReturnValue = "";
            	int qty = 0;
            	try{
            		if (value.indexOf('.') > -1) {
            			qty = Integer.parseInt(value.substring(0, value.indexOf('.')));
					} else {
						qty = Integer.parseInt(value);
					}
            	}catch(NumberFormatException nfe){
            		qty = 1;
            	}
            	sReturnValue = "" + qty;
//            	sReturnValue = value.substring(0, value.indexOf('.'));
            	
            	return sReturnValue;
			}
            return value;
        } else {
//        	System.out.println("propName : " + propName);
            return super.getProperty(propName);
        }
    }
    
    /*public String[] getProperties(String[] propNames) throws TCException {
        return super.getProperties(propNames);
    }*/
    
    public void setProperty(String propertyName, String value, boolean unCheckTop) throws TCException {
        String oldValue = getProperty(propertyName);
        if(oldValue.equals(value)) {
            return;
        }
        if(unCheckTop) {
        	
        	if (isHistoryTarget(parent())) {
        		if(!isTopFunction() || !isHistoryChildAddOrReplacable(parent(), getItemRevision())) {
        			return;
        		}
        	}
        }
        if(propertyName.equals("bl_sequence_no")) {
            if(!isSequenceChangable(getProperty("bl_item_item_id"), value)) {
                return;
            }
            // [SR150416-009][2015.04.23][jclee] 999999 이후 자동 채번되는 7자리 Find No의 경우 999999로 변경
            if(value != null && value.length() != 6) {
                DecimalFormat fnFormat = new DecimalFormat("000000");
                
                if (value.length() > 6) {
					value = "999999";
				}
                
                value = fnFormat.format(Integer.parseInt(value));
            }
        } else {
            // [SR150416-009][2015.04.23][jclee] 999999 이후 자동 채번되는 7자리 Find No의 경우 999999로 변경
            String seq = getProperty("bl_sequence_no");
            if(seq.length() != 6) {
                DecimalFormat fnFormat = new DecimalFormat("000000");
                
                if (seq.length() > 6) {
					seq = "999999";
				}
                
                super.setProperty("bl_sequence_no", fnFormat.format(Integer.parseInt(seq)));
            }
        }
        if(propertyName.equals("bl_quantity")) {
            TCComponentUnitOfMeasure uom = (TCComponentUnitOfMeasure)getReferenceProperty("bl_uom");
            if(uom == null || uom.getProperty("symbol").equals("EA")) {
                if(!value.equals("1")) {
                    MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                            , "If unit of measure is each, the value of Quantity must be \"1\"."
                            , "BOM Edit", MessageBox.INFORMATION);
                }
                value = "1";
            }
        } else {
            if(getProperty("bl_quantity").equals("")) {
                setQuantity();
            }
        }

        super.setProperty(propertyName, value);
    }
    
    public void setProperty_mig(String propertyName, String value) throws TCException {
        super.setProperty(propertyName, value);
    }
    
    public void setProperty(String propertyName, String value) throws TCException {
        String oldValue = getProperty(propertyName);
        if(oldValue.equals(value)) {
            return;
        }
        if (isHistoryTarget(parent())) {
            if(!isTopFunction() || !isHistoryChildAddOrReplacable(parent(), getItemRevision())) {
                return;
            }
        }
        // [SR150416-009][2015.04.23][jclee] 999999 이후 자동 채번되는 7자리 Find No의 경우 999999로 변경
        if(propertyName.equals("bl_sequence_no")) {
            if(!isSequenceChangable(getProperty("bl_item_item_id"), value)) {
                return;
            }
            if(value != null && value.length() != 6) {
                DecimalFormat fnFormat = new DecimalFormat("000000");
                
                if (value.length() > 6) {
					value = "999999";
				}
                
                value = fnFormat.format(Integer.parseInt(value));
            }
        } else {
            // [SR150416-009][2015.04.23][jclee] 999999 이후 자동 채번되는 7자리 Find No의 경우 999999로 변경
            String seq = getProperty("bl_sequence_no");
            if(seq.length() != 6) {
                DecimalFormat fnFormat = new DecimalFormat("000000");
                
                if (seq.length() > 6) {
					seq = "999999";
				}
                
                super.setProperty("bl_sequence_no", fnFormat.format(Integer.parseInt(seq)));
            }
        }
        if(propertyName.equals("bl_quantity")) {
            TCComponentUnitOfMeasure uom = (TCComponentUnitOfMeasure)getReferenceProperty("bl_uom");
            if(uom == null || uom.getProperty("symbol").equals("EA")) {
                if(!value.equals("1")) {
                    MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                            , "If unit of measure is each, the value of Quantity must be \"1\"."
                            , "BOM Edit", MessageBox.INFORMATION);
                }
                value = "1";
            }
          //[20170208] Order No 사용자 수정못하도록 메세지 처리함
        } else if(propertyName.equals("bl_occ_int_order_no")) {
        	MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                    , "You can't edit Order No"
                    , "BOM Edit", MessageBox.INFORMATION);
        	return;
        } else {
            if(getProperty("bl_quantity").equals("")) {
                setQuantity();
            }
        }

        super.setProperty(propertyName, value);
    }
    
    public void setProperties(String[] propNames, String[] newValues) throws TCException {
        if(getProperty("bl_quantity").equals("")) {
            setQuantity();
        }
        if (isHistoryTarget(parent())) {
            if(!isTopFunction() || !isHistoryChildAddOrReplacable(parent(), getItemRevision())) {
                return;
            }
            //ArrayList<String> props = new ArrayList<String>();
            //ArrayList<String[]> values = new ArrayList<String[]>();
            for(int i = 0 ; i < propNames.length ; i++) {
                String oldValue = getProperty(propNames[i]);
                if(oldValue.equals(newValues[i])) {
                    continue;
                }
                // [SR150416-009][2015.04.23][jclee] 999999 이후 자동 채번되는 7자리 Find No의 경우 999999로 변경
                if (propNames[i].equals("bl_sequence_no")) {
                    if(!isSequenceChangable(getProperty("bl_item_item_id"), newValues[i])) {
                        return;
                    }
                    if(newValues[i] != null && newValues[i].length() != 6) {
                        DecimalFormat fnFormat = new DecimalFormat("000000");
                        
                        if (newValues[i].length() > 6) {
							newValues[i] = "999999";
						}
                        
                        newValues[i] = fnFormat.format(Integer.parseInt(newValues[i]));
                    }
                    //props.add(propNames[i]);
                    //values.add(new String[]{oldValue, newValues[i]});
                } else if(propNames[i].equals("bl_quantity")) {
                    TCComponentUnitOfMeasure uom = (TCComponentUnitOfMeasure)getReferenceProperty("bl_uom");
                    if(uom == null || uom.getProperty("symbol").equals("EA")) {
                        if(!newValues[i].equals("1")) {
                            MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                                    , "If unit of measure is each, the value of Quantity must be \"1\"."
                                    , "BOM Edit", MessageBox.INFORMATION);
                            return;
                        }
                    }
                }
            }
        }
        super.setProperties(propNames, newValues);
        if(getProperty("bl_quantity").equals("")) {
            setQuantity();
        }
        String seq = getProperty("bl_sequence_no");
        
        // [SR150416-009][2015.04.23][jclee] 999999 이후 자동 채번되는 7자리 Find No의 경우 999999로 변경
        if(seq.length() != 6) {
        	DecimalFormat fnFormat = new DecimalFormat("000000");
        	
        	if (seq.length() > 6) {
        		seq = "999999";
        	}
        	
        	super.setProperty("bl_sequence_no", fnFormat.format(Integer.parseInt(seq)));
        }
    }
    
    public void setProperties_mig(String[] propNames, String[] newValues) throws TCException {
        
        super.setProperties(propNames, newValues);
        
    }
    
    /**
     * Validation 없이 수량을 Default 1로 설정
     */
    private void setQuantity() throws TCException {
        super.setProperty("bl_quantity", "1");
        setSequenceFormat();
    }
    
    /**
     * Find No 포맷
     * [SR150416-009][2015.04.23][jclee] 999999 이후 자동 채번되는 7자리 Find No의 경우 999999로 변경
     * @throws TCException
     */
    private void setSequenceFormat() throws TCException {
        String fn = getProperty("bl_sequence_no");
        if(fn.length() != 6) {
        	DecimalFormat fnFormat = new DecimalFormat("000000");
        	fn = fnFormat.format(Integer.parseInt(fn));
        	super.setProperty("bl_sequence_no", fn);
        } else if (fn.length() > 6) {
        	super.setProperty("bl_sequence_no", "999999");
		}
    }
    
    /**
     * Sequence 변경 가능 여부 반환
     * @param childId
     * @param newSeq
     * @return
     * @throws TCException
     */
    public boolean isSequenceChangable(String childId, String newSeq) throws TCException {
        if(newSeq.equals("") || newSeq.length() > 6) {
            MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                    , "FIND NO는 6자리 이하로 설정하여 주십시요."
                    , "BOM Edit", MessageBox.INFORMATION);
            return false;
        }
        TCComponentBOMLine parentLine = parent();
        TCComponent[] childLines = parentLine.getReferenceListProperty("bl_child_lines");
        for(int i = 0 ; i < childLines.length ; i++) {
            TCComponentBOMLine childLine = (TCComponentBOMLine)childLines[i];
            // Seq 가 사용되고 있으면
            if(childLine.getSequenceNumber().equals(newSeq)) {
                String oldChildId = childLine.getProperty("bl_item_item_id");
                // 부자재인 경우 무조껀 안됨.
                if(oldChildId.startsWith("B") || oldChildId.startsWith("D")) {
                    MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                            , "Subsidiary Parts shouldn't be packed. So, Find No. for Subsidiary Part must be unique."
                            , "BOM Edit", MessageBox.INFORMATION);
                    return false;
                    //throw new TCException("부자재는 Find No 가 독립적으로 사용되어야 합니다.");
                }
                // 부자재가 아닌경우 파트가 틀리면 안됨.
                else if(!childId.equals(childLine.getProperty("bl_item_item_id"))) {
                    MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                            , "Find No. you filled in is duplicated. Find No. should be unique."//"Find No(" + newSeq + ")가 이미 " + childLine.toString() + "에서 사용중입니다."
                            , "BOM Edit", MessageBox.INFORMATION);
                    return false;
                    //throw new TCException("Find No(" + newSeq + ")가 이미 " + childLine.toString() + "에서 사용중입니다.");
                }
            }
        }
        
        return true;
    }

    public void setMVLCondition(String newVC) throws TCException {
        /*if (isHistoryTarget(parent())) {
            String oldVC = getProperty("bl_variant_condition");
            TCVariantService svc = getSession().getVariantService();
            svc.setLineMvlCondition(this, newVC);
            ((SYMCBOMWindow) window()).setHistoryVC(this, oldVC, newVC);
        } else {
            TCVariantService svc = getSession().getVariantService();
            svc.setLineMvlCondition(this, newVC);
        }*/
        TCVariantService svc = getSession().getVariantService();
        svc.setLineMvlCondition(this, newVC);
    }

    public TCComponentBOMLine add(TCComponentItem item, TCComponentItemRevision revision, TCComponent tcComp, boolean isSubstitute) throws TCException {
        if(revision == null) {
            revision = item.getLatestItemRevision();
        } else {
            revision = revision.getItem().getLatestItemRevision();
        }
        
        if (isHistoryTarget(this)) {
            if(!isHistoryChildAddOrReplacable(this, revision)) {
                return null;
            }
            SYMCBOMLine bomLine = (SYMCBOMLine)add(item, revision, tcComp, isSubstitute, refreshRequired);
            bomLine.setQuantity();
            /*if(bomLine != null) {
                ((SYMCBOMWindow) window()).addHistory(bomLine);
            }*/
            bomLine.setAdded(true);
            return bomLine;
        } else {
            if(!isAddOrReplacable(this, revision)) {
                return null;
            }
            SYMCBOMLine bomLine = (SYMCBOMLine)add(item, revision, tcComp, isSubstitute, refreshRequired);
            bomLine.setQuantity();
            setUpperBOMProperty();
            bomLine.setAdded(true);
            return bomLine;
        }
    }

    private TCComponentBOMLine add(TCComponentItem item, TCComponentItemRevision revision, TCComponent tcComp, boolean isSubstitute, boolean refresh) throws TCException {
        SYMCBOMLine newBOMLine = (SYMCBOMLine)((TCComponentBOMLineType) getTypeComponent()).add(this, item, revision, tcComp, isSubstitute, null);
        newBOMLine.setRelatedData(window(), this);
        if (refresh) {
            clearCache();
            window().fireComponentChangeEvent();
            if ((newBOMLine != null) && (getChildrenCount() == 1)) {
                TCComponentItem localTCComponentItem = getItem();
                TCComponentItemRevision localTCComponentItemRevision = getItemRevision();
                if (localTCComponentItem != null)
                    localTCComponentItem.fireComponentAddChildEvent(null, null);
                if (localTCComponentItemRevision != null)
                    localTCComponentItemRevision.fireComponentAddChildEvent(null, null);
            }
        }
        newBOMLine.setAdded(true);
        return newBOMLine;
    }

    /**
     * 
     */
    public TCComponentBOMLine add(TCComponentItem item, TCComponentItemRevision revision, TCComponent paramTCComponent, boolean paramBoolean, String paramString) throws TCException {
        if(revision == null) {
            revision = item.getLatestItemRevision();
        }
        if (isHistoryTarget(this)) {
            if(!isHistoryChildAddOrReplacable(this, revision)) {
                return null;
            }
            SYMCBOMLine newBOMLine = (SYMCBOMLine)super.add(item, revision, paramTCComponent, paramBoolean, paramString);
            newBOMLine.setQuantity();
            newBOMLine.setAdded(true);
            return newBOMLine;
        } else {
            if(!isAddOrReplacable(this, revision)) {
                return null;
            }
            SYMCBOMLine newBOMLine = (SYMCBOMLine)super.add(item, revision, paramTCComponent, paramBoolean, paramString);
            newBOMLine.setQuantity();
            setUpperBOMProperty();
            newBOMLine.setAdded(true);
            return newBOMLine;
        }
    }

    public TCComponentBOMLine add(TCComponentBOMLine newLine, boolean paramBoolean) throws TCException {
        if (isHistoryTarget(this)) {
            if(!isHistoryChildAddOrReplacable(this, newLine.getItem().getLatestItemRevision())) {
                return null;
            }
            SYMCBOMLine newBOMLine = (SYMCBOMLine)super.add(newLine, paramBoolean);
            if(newBOMLine.isPacked()) {
                TCComponentBOMLine[] packedLines = newBOMLine.getPackedLines();
                for(TCComponentBOMLine packedLine : packedLines) {
                    if(packedLine.getProperty("bl_quantity").equals("")) {
                        ((SYMCBOMLine)packedLine).setQuantity();
                    } else {
                        ((SYMCBOMLine)packedLine).setSequenceFormat();
                    }
                }
                newBOMLine.unpack();
                if(!newBOMLine.getProperty("bl_quantity").equals("1")) {
                    newBOMLine.setQuantity();
                } else {
                    newBOMLine.setSequenceFormat();
                }
                newBOMLine.pack();
            } else {
                if(newBOMLine.getProperty("bl_quantity").equals("")) {
                    newBOMLine.setQuantity();
                } else {
                    newBOMLine.setSequenceFormat();
                }
            }
            newBOMLine.setAdded(true);
            
            //Pre-BOM에서 사용되는 System Row Key값을 삭제함.
            newBOMLine.setProperty("S7_SYSTEM_ROW_KEY", "");
            
            return newBOMLine;
        } else {
            if(!isAddOrReplacable(this, newLine.getItem().getLatestItemRevision())) {
                return null;
            }
            SYMCBOMLine newBOMLine = (SYMCBOMLine)super.add(newLine, paramBoolean);
            if(newBOMLine.isPacked()) {
                TCComponentBOMLine[] packedLines = newBOMLine.getPackedLines();
                for(TCComponentBOMLine packedLine : packedLines) {
                    if(packedLine.getProperty("bl_quantity").equals("")) {
                        ((SYMCBOMLine)packedLine).setQuantity();
                    } else {
                        ((SYMCBOMLine)packedLine).setSequenceFormat();
                    }
                }
                newBOMLine.unpack();
                if(!newBOMLine.getProperty("bl_quantity").equals("1")) {
                    newBOMLine.setQuantity();
                } else {
                    newBOMLine.setSequenceFormat();
                }
                newBOMLine.pack();
            } else {
                if(newBOMLine.getProperty("bl_quantity").equals("")) {
                    newBOMLine.setQuantity();
                } else {
                    newBOMLine.setSequenceFormat();
                }
            }
            setUpperBOMProperty();
            newBOMLine.setAdded(true);
            
            //Pre-BOM에서 사용되는 System Row Key값을 삭제함.
            newBOMLine.setProperty("S7_SYSTEM_ROW_KEY", "");
            
            return newBOMLine;
        }
    }

    public TCComponentBOMLine add(TCComponentBOMLine newLine, boolean paramBoolean, String paramString) throws TCException {
        if (isHistoryTarget(this)) {
            if(!isHistoryChildAddOrReplacable(this, newLine.getItem().getLatestItemRevision())) {
                return null;
            }
            SYMCBOMLine newBOMLine = (SYMCBOMLine)super.add(newLine, paramBoolean, paramString);
            if(newBOMLine.isPacked()) {
                TCComponentBOMLine[] packedLines = newBOMLine.getPackedLines();
                for(TCComponentBOMLine packedLine : packedLines) {
                    if(packedLine.getProperty("bl_quantity").equals("")) {
                        ((SYMCBOMLine)packedLine).setQuantity();
                    }
                }
                newBOMLine.unpack();
                if(!newBOMLine.getProperty("bl_quantity").equals("1")) {
                    ((SYMCBOMLine)newBOMLine).setQuantity();
                }
                newBOMLine.pack();
            } else {
                if(!newBOMLine.getProperty("bl_quantity").equals("1")) {
                    newBOMLine.setQuantity();
                }
            }
            newBOMLine.setAdded(true);
            
            //Pre-BOM에서 사용되는 System Row Key값을 삭제함.
            newBOMLine.setProperty("S7_SYSTEM_ROW_KEY", "");
            
            return newBOMLine;
            
        } else {
            if(!isAddOrReplacable(this, newLine.getItem().getLatestItemRevision())) {
                return null;
            }
            SYMCBOMLine newBOMLine = (SYMCBOMLine)super.add(newLine, paramBoolean, paramString);
            if(newBOMLine.isPacked()) {
                TCComponentBOMLine[] packedLines = newBOMLine.getPackedLines();
                for(TCComponentBOMLine packedLine : packedLines) {
                    if(packedLine.getProperty("bl_quantity").equals("")) {
                        ((SYMCBOMLine)packedLine).setQuantity();
                    }
                }
                newBOMLine.unpack();
                if(newBOMLine.getProperty("bl_quantity").equals("")) {
                    ((SYMCBOMLine)newBOMLine).setQuantity();
                }
                newBOMLine.pack();
            } else {
                if(newBOMLine.getProperty("bl_quantity").equals("")) {
                    newBOMLine.setQuantity();
                }
            }
            setUpperBOMProperty();
            newBOMLine.setAdded(true);
            
            //Pre-BOM에서 사용되는 System Row Key값을 삭제함.
            newBOMLine.setProperty("S7_SYSTEM_ROW_KEY", "");
            
            return newBOMLine;
        }
    }

    /**
     * 상속이 불가라 그대로 복사
     * @param bomLine
     * @param tcComp
     * @param context
     * @return
     * @throws TCException
     */
    private TCComponentBOMLine addBOMLineNoPostActions(SYMCBOMLine bomLine, TCComponent tcComp, String context) throws TCException {
        boolean isSubstitute = (context != null) && (context.equals("substitute"));
        TCComponentBOMLine localTCComponentBOMLine = null;
        if (tcComp instanceof TCComponentItem) {
            refreshRequired = false;
            localTCComponentBOMLine = bomLine.add((TCComponentItem) tcComp, null, null, isSubstitute);
            refreshRequired = true;
        } else if (tcComp instanceof TCComponentItemRevision) {
            refreshRequired = false;
            localTCComponentBOMLine = bomLine.add(null, (TCComponentItemRevision) tcComp, null, isSubstitute);
            refreshRequired = true;
        } else if (tcComp instanceof TCComponentBOMLine) {
            localTCComponentBOMLine = bomLine.add((TCComponentBOMLine) tcComp, isSubstitute);
        } else {
            throw TCComponent.handleException(Registry.getRegistry(this).getString("bomlineAddError"));
        }
        return localTCComponentBOMLine;

    }

    /**
     * 붙여넣기
     */
    public Object pasteOperation(AIFComponentContext[] componentContexts) throws TCException {
        ArrayList<TCComponentBOMLine> sourceBOMLines = new ArrayList<TCComponentBOMLine>();
        ArrayList<TCComponentBOMLine> targetBOMLines = new ArrayList<TCComponentBOMLine>();
        ArrayList<TCComponentBOMLine> cutPasteBOMLines = new ArrayList<TCComponentBOMLine>();
        int i = 0;
        if (componentContexts.length > 0) {
            TCComponent parentComp = (TCComponent) componentContexts[0].getParentComponent();
            if ((parentComp instanceof TCComponentBOMLine) && (((TCComponentBOMLine) parentComp).window() != null) && (((TCComponentBOMLine) parentComp).window().getMarkupMode())) {
                i = 1;
                addNewMarkupChild(componentContexts);
            }
        }
        if (i == 0) {
            HashMap<TCComponentBOMLine, TCComponent> localObject1 = new HashMap<TCComponentBOMLine, TCComponent>();
            HashMap<TCComponentBOMLine, Boolean> localHashMap = new HashMap<TCComponentBOMLine, Boolean>();
            for (AIFComponentContext componentContext : componentContexts) {
                TCComponent tcComp = (TCComponent) componentContext.getComponent();
                TCComponent parentComp = (TCComponent) componentContext.getParentComponent();
                String str = (String) componentContext.getContext();
                int l = 1;
                if ((str != null) && (str.indexOf(DELAYBOMCUT) > -1)) {
                    str = str.replaceFirst(DELAYBOMCUT, "");
                    if (str.length() == 0)
                        str = null;
                    l = 0;
                }
                if ((Debug.isOn("bom")) && (parentComp != this))
                    throw TCComponent.handleException("Attempt to paste objects to multiple targets");
                if ((tcComp instanceof TCComponentBOMLine) && (!(((TCComponentBOMLine) tcComp).isPasteAllowed(parentComp))))
                    throw TCComponent.handleException(Registry.getRegistry(this).getString("genericPasteError"));
                try {
                    boolean hasChildren = false;
                    if (!(localHashMap.containsKey(parentComp)))
                        hasChildren = ((TCComponentBOMLine) parentComp).hasChildren();
                    // start
                    TCComponentBOMLine localTCComponentBOMLine2 = addBOMLineNoPostActions((SYMCBOMLine) parentComp, tcComp, str);
                    if(localTCComponentBOMLine2 == null) {
                        return null;
                    }
                    // end

                    if (localTCComponentBOMLine2 != null) {
                        localObject1.put(localTCComponentBOMLine2, tcComp);
                        if (!(localHashMap.containsKey(parentComp)))
                            localHashMap.put((TCComponentBOMLine) parentComp, Boolean.valueOf(hasChildren));
                    }
                    if ((tcComp instanceof TCComponentBOMLine) && (!(tcComp instanceof TCComponentGDELine))) {
                        TCComponentBOMLine localTCComponentBOMLine3 = (TCComponentBOMLine) tcComp;
                        boolean bool2 = localTCComponentBOMLine3.isRoot();
                        if (!(bool2)) {
                            sourceBOMLines.add(localTCComponentBOMLine3);
                            targetBOMLines.add(localTCComponentBOMLine2);
                        }
                    }
                    if ((l != 0) && (tcComp.getPendingCut()))
                        cutPasteBOMLines.add((TCComponentBOMLine) tcComp);
                } catch (TCException localTCException) {
                    componentContext.setContext(localTCException);
                }
            }
            Iterator<TCComponentBOMLine> iterator1 = localObject1.keySet().iterator();
            while (iterator1.hasNext()) {
                TCComponentBOMLine localTCComponentBOMLine1 = (TCComponentBOMLine) iterator1.next();
                TCComponentBOMLine localObject3 = getBOMLinePredecessor(localTCComponentBOMLine1.parent(), (TCComponent) localObject1.get(localTCComponentBOMLine1));
                if ((localObject3 != null) && (localTCComponentBOMLine1 != null)) {
                    ((SYMCBOMLine) localTCComponentBOMLine1).addPredecessor((TCComponentBOMLine) localObject3, false);
                    localTCComponentBOMLine1.clearCache();
                    ((TCComponentBOMLine) localObject3).clearCache();
                }
            }
            Iterator<TCComponentBOMLine> iterator2 = localHashMap.keySet().iterator();
            while (iterator2.hasNext()) {
                TCComponentBOMLine localTCComponentBOMLine1 = (TCComponentBOMLine) iterator2.next();
                localTCComponentBOMLine1.clearCache();
                localTCComponentBOMLine1.window().fireComponentChangeEvent();
                if (!(((Boolean) localHashMap.get(localTCComponentBOMLine1)).booleanValue())) {
                    TCComponentItem localObject3 = localTCComponentBOMLine1.getItem();
                    TCComponentItemRevision localTCComponentItemRevision = localTCComponentBOMLine1.getItemRevision();
                    if (localObject3 != null)
                        ((TCComponentItem) localObject3).fireComponentAddChildEvent(null, null);
                    if (localTCComponentItemRevision != null)
                        localTCComponentItemRevision.fireComponentAddChildEvent(null, null);
                }
            }
        }
        carryOverICEs(sourceBOMLines, targetBOMLines);
        if (cutPasteBOMLines.size() > 0)
            processPendingCut(cutPasteBOMLines);
        sourceBOMLines.clear();
        targetBOMLines.clear();
        cutPasteBOMLines.clear();
        return componentContexts;
    }

    /**
     * 상속이 불가라 그대로 복사
     * @param paramTCComponentBOMLine
     * @param paramTCComponent
     * @return
     * @throws TCException
     */
    private TCComponentBOMLine getBOMLinePredecessor(TCComponentBOMLine paramTCComponentBOMLine, TCComponent paramTCComponent) throws TCException {
        if ((paramTCComponentBOMLine == null) || (paramTCComponent == null))
            return null;
        TCComponentItem localTCComponentItem = paramTCComponentBOMLine.getItem();
        if (!(localTCComponentItem instanceof TCComponentMEWorkarea))
            return null;
        if (paramTCComponentBOMLine.getChildrenCount() <= 0)
            return null;
        Object localObject = paramTCComponent;
        if (localObject instanceof TCComponentBOMLine)
            localObject = ((TCComponentBOMLine) paramTCComponent).getItem();
        if ((!(localObject instanceof TCComponentMEWorkarea)) && (!(localObject instanceof TCComponentMEWorkareaRevision)))
            return null;
        AIFComponentContext[] arrayOfAIFComponentContext = paramTCComponentBOMLine.getChildren();
        return ((TCComponentBOMLine) (TCComponentBOMLine) arrayOfAIFComponentContext[(arrayOfAIFComponentContext.length - 1)].getComponent());
    }

    /**
     * 상속이 불가라 그대로 복사
     * @param paramTCComponentBOMLine
     * @param paramBoolean
     * @throws TCException
     */
    private void addPredecessor(TCComponentBOMLine paramTCComponentBOMLine, boolean paramBoolean) throws TCException {
        ((TCComponentBOMLineType) getTypeComponent()).addPredecessor(this, paramTCComponentBOMLine);
        if (!(paramBoolean))
            return;
        clearCache(getPredecessorPropertiesToHandle());
        paramTCComponentBOMLine.clearCache(getSuccessorPropertiesToHandle());
        window().fireComponentChangeEvent();
    }

    /**
     * 상속이 불가라 그대로 복사
     * @param sourceBOMLines
     * @param targetBOMLines
     * @throws TCException
     */
    private void carryOverICEs(ArrayList<TCComponentBOMLine> sourceBOMLines, ArrayList<TCComponentBOMLine> targetBOMLines) throws TCException {
        int i = sourceBOMLines.size();
        if (i <= 0)
            return;
        IncrementalChange.BomLineInfo[] arrayOfBomLineInfo = new IncrementalChange.BomLineInfo[i];
        for (int j = 0; j < i; ++j) {
            arrayOfBomLineInfo[j] = new IncrementalChange.BomLineInfo();
            arrayOfBomLineInfo[j].sourceLine = ((ModelObject) sourceBOMLines.get(j));
            arrayOfBomLineInfo[j].targetLine = ((ModelObject) targetBOMLines.get(j));
        }
        IncrementalChangeService localIncrementalChangeService = IncrementalChangeService.getService(getSession().getSoaConnection());
        ServiceData localServiceData = localIncrementalChangeService.carryOver(arrayOfBomLineInfo);
        SoaUtil.handlePartialErrors(localServiceData, this);
    }

    /**
     * 상속이 불가라 그대로 복사
     * @param paramArrayList
     * @throws TCException
     */
    private void processPendingCut(ArrayList<TCComponentBOMLine> paramArrayList) throws TCException {
        TCComponentBOMLine[] arrayOfTCComponentBOMLine = (TCComponentBOMLine[]) paramArrayList.toArray(new TCComponentBOMLine[paramArrayList.size()]);
        StructureManagementService structureManagementService = StructureManagementService.getService(getSession().getSoaConnection());
        StructureManagement.RemoveChildrenFromParentLineResponse removeChildrenFromParentLineResponse = structureManagementService.removeChildrenFromParentLine(arrayOfTCComponentBOMLine);
        SoaUtil.handlePartialErrors(removeChildrenFromParentLineResponse.serviceData, this);
    }

    /**
     * Replace 판단 수행.
     */
    public void replace(TCComponentItem item, TCComponentItemRevision revision, TCComponent comp) throws TCException {
    	if (replace(item, revision)) {
    		super.replace(item, revision, comp);
		}
    }
    
    /**
     * replace (TC10)
     * @param item
     * @param revision
     * @throws TCException
     */
    public boolean  replace(TCComponentItem item, TCComponentItemRevision revision) throws TCException {
    	if(revision == null) {
            revision = item.getLatestItemRevision();
        } else {
            revision = revision.getItem().getLatestItemRevision();
        }
        if (isHistoryTarget(parent())) {
            if(!isHistoryChildAddOrReplacable(parent(), revision)) {
                return false;
            }
        } else {
            if(!isAddOrReplacable(parent(), revision)) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Cut 판단 수행
     */
    public void cut() throws TCException {
        if (isHistoryTarget(parent())) {
            if(isHistoryCuttable(parent())) {
            	setAdded(false);
                super.cut();
            }
        } else {
            if(isCuttable(parent(), this)) {
            	setAdded(false);
                super.cut();
            }
        }
    }
    
    /**
     * BOM Validation 수행 여부
     * @return
     * @throws TCException
     */
    private boolean isSkipHistory() throws TCException {
        return ((SYMCBOMWindow)window()).isSkipHistory();
    }

    /**
     * BOM ADD 시 이력대상인지 Check
     * Function - Function Master 구성은 이력 사항이나 ECO 번호가 없는 상태
     *      ADD) Function Master 에 ECO 번호 입력 시점(설계에서 Function Master 구성 시)에 이력 생성
     *      
     * @param parent
     * @return
     * @throws TCException
     */
    public boolean isHistoryTarget(TCComponentBOMLine parent) throws TCException {
        if(isSkipHistory()) {
            return false;
        }
        String parentType = parent.getProperty("bl_item_object_type");
        if (parentType.equals("Function") || parentType.equals("Variant") || parentType.equals("Product")) {
            return false;
        }
        if (parentType.equals("Function Master")) {
            return true;
        }
        if (parentType.equals("Standard Part")) {
            return false;
        }
        String parentStage = parent.getItemRevision().getProperty("s7_STAGE");
        if(!parentStage.equals("P")) {
            return false;
        }
        return true;
    }
    
    public boolean isTopFunction() throws TCException {
//    	return true;
        if(window().getTopBOMLine().getProperty("bl_item_object_type").equals("Function") || window().getTopBOMLine().getProperty("bl_item_object_type").equals("Pre Function")) {
            return true;
        }
        MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                , "In order to edit BOM Structure, You should place Function on Top."
                , "BOM Edit", MessageBox.INFORMATION);
        return false;
    }
    
    /**
     * BOM CUT, REPLACE, PROPERTY CHANGE 시 이력대상인지 Check
     * @param parent
     * @return
     * @throws TCException
     
    public boolean isHistoryTarget(TCComponentItemRevision targetRev) throws TCException {
        String type = targetRev.getItem().getType();
        if(type.equals("Function") || type.equals("Variant") || type.equals("Product")) {
            return false;
        }
        if(type.equals("Standard Part")) {
            if(parent() != null) {
                String parentType = parent().getProperty("bl_item_object_type");
                if(parentType.equals("Function Master") || parentType.equals("Standard Part")) {
                    return true;
                } else {
                    if(parent().getItemRevision().getProperty("s7_STAGE").equals("P")) {
                        return true;
                    }
                    return false;
                }
            }
        }
        String childStage = targetRev.getProperty("s7_STAGE");
        if(childStage.equals("P")) {
            return true;
        }
        return false;
    }*/
    
    /**
     * 이력대상 Cut 작업 가능 여부 반환
     * @param parent
     * @return
     * @throws TCException
     */
    public boolean isHistoryCuttable(TCComponentBOMLine parent) throws TCException {
        if(!isTopFunction()) {
            return false;
        }
        TCComponentItemRevision parentRev = parent.getItemRevision();
        TCComponentItemRevision parentECORev = getRevisionECO(parentRev);
        if(parentECORev == null) {
            return false;
            /*MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                    , "This operation is not allowd.\n" + parentRev.toString() + " has't ECO No!"
                    , "BOM Edit", MessageBox.INFORMATION);
            return false;*/
        }
        return true;
    }
    
    /**
     * Item Revision의 ECO 반환
     * @param parentRev
     * @return
     * @throws TCException
     */
	private TCComponentItemRevision getRevisionECO(TCComponentItemRevision parentRev) throws TCException {
		final Shell shell = AIFUtility.getActiveDesktop().getShell();
		final Registry registry = Registry.getRegistry("com.ssangyong.commands.ec.history.history");

		ecoRev = null;
		ecoRev = (TCComponentItemRevision) parentRev.getReferenceProperty("s7_ECO_NO");
		if (ecoRev == null) {
			int response = ConfirmationDialog.post(AIFUtility.getActiveDesktop(), "BOM Edit", "BOM Work With Production Part, ECO is required!\nSelect ECO?");
			if (response != ConfirmationDialog.YES) {
				return null;
			}

			shell.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					InterfaceSYMCECOSelect ecoSelect = (InterfaceSYMCECOSelect) registry.newInstanceFor("bomECOSelectDialog", new Object[] { shell });
					ecoRev = ecoSelect.getECO();
				}

			});

			if (ecoRev == null) {
				// MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Not selected ECO!", "BOM Edit", MessageBox.INFORMATION);
				return null;
			} else {
				parentRev.setReferenceProperty("s7_ECO_NO", ecoRev);

				return ecoRev;
			}
		} else {
			return ecoRev;
		}
	}
    
    /**
     * 모 리비젼의 ECO 와 자 Revision의 ECO 가 동일한지 검사
     * @param parentECORev
     * @param childRev
     * @return
     * @throws TCException
     */
    public boolean isHistoryChildMatchECO(TCComponentItemRevision parentECORev, TCComponentItemRevision childRev) throws TCException {
        if(!isTopFunction()) {
            return false;
        }
        if(childRev.getProperty("release_status_list").equals("")) {
            TCComponentItemRevision childECORev = (TCComponentItemRevision)childRev.getReferenceProperty("s7_ECO_NO");
            if(parentECORev.equals(childECORev)) {
                return true;
            } else {
                if(childECORev == null) {
                    /*int response = ConfirmationDialog.post(AIFUtility.getActiveDesktop()
                            , "BOM Edit"
                            , childRev.toString() + " ECO No set to " + parentECORev.getProperty("item_id") + "\nContinue?");
                    if(response != ConfirmationDialog.YES) {
                        return false;
                    }*/
                    if(!childRev.okToModify()) {
                        MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                                , "This operation is not allowd." + 
                                  "\nChild Part(" + childRev + ") is Owned " + childRev.getProperty("owning_user")
                                , "BOM Edit", MessageBox.INFORMATION);
                        return false;
                    }
                    childRev.setReferenceProperty("s7_ECO_NO", parentECORev);
                    return true;
                } else {
                    MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                            , "This operation is not allowd." + 
                              "\nParent ECO(" + parentECORev.getProperty("item_id") + ") is not matched Child ECO(" + childECORev.getProperty("item_id") + ")"
                            , "BOM Edit", MessageBox.INFORMATION);
                    return false;
                }
            }
        } else {
            return true;
        }
    }

    /**
     * 이력대상 Add, Replace 작업 시 가능 여부 반환(Function Master 이하)
     * @param parentLine
     * @param childRev
     * @return
     * @throws TCException
     */
    public boolean isHistoryChildAddOrReplacable(TCComponentBOMLine parentLine, TCComponentItemRevision childRev) throws TCException {
        if(!isTopFunction()) {
            return false;
        }
        String parentType = parentLine.getProperty("bl_item_object_type");
        String childType = childRev.getItem().getProperty("object_type");
        /**
         * [SR150106-024][20150115][jclee] Obsolete된 Standard Part 사용불가하게 하는 기능
         */
        if(childType.equals("Standard Part")) {
        	String sMaturity = "";
        	String sReleaseStatusList = "";
        	boolean isObsolete = false;
        	
        	sMaturity = childRev.getItem().getLatestItemRevision().getProperty("s7_MATURITY");
        	sReleaseStatusList = childRev.getProperty("release_status_list");
        	if (sMaturity == null || sMaturity.length() == 0 || sMaturity.equals("")
        			|| sReleaseStatusList == null || sReleaseStatusList.length() == 0 || sReleaseStatusList.equals("")) {
//        		MessageBox.post(AIFUtility.getActiveDesktop().getShell()
//        				, "This operation is not allowed.\nCheck the Maturity or Release Status of Standard Part."
//        				, "BOM Edit", MessageBox.INFORMATION);
//        		return false;
        	} else {
        		isObsolete = sMaturity.equals("Obsolete") || sReleaseStatusList.equals("Obsolete");
        	}
        	
        	
        	if (isObsolete) {
        		MessageBox.post(AIFUtility.getActiveDesktop().getShell()
        				, "This operation is not allowd.\nYou can't constitute an Obsoleted Standard Part to BOM."
        				, "BOM Edit", MessageBox.INFORMATION);
        		return false;
        	}
            
            return true;
        }
        
        TCComponentBOMViewRevision bvr = parentLine.getBOMViewRevision();
        
        if(parentType.equals("Function Master")) {
            if(!childRev.getProperty("s7_STAGE").equals("P")) {
                MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                        , "This operation is not allowd.\nRegular production part only can be added to Function Master."
                        , "BOM Edit", MessageBox.INFORMATION);
                return false;
            } else {
                TCComponentItemRevision parentECORev = getRevisionECO(parentLine.getItemRevision());
                if(parentECORev == null) {
                    return false;
                } else {
                	
//                	TCComponentBOMViewRevision bvr = getBOMViewRevision();
                	
                	// Assy 인경우
                    if(bvr != null && !bvr.okToModify()) {
                        MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                                , "This operation is not allowd.\nParent BOMViewRevision is not Modifiable."
                                , "BOM Edit", MessageBox.INFORMATION);
                        return false;
                    }
                    // Component 하위에 추가하는 경우
                    else if( bvr == null && !parentLine.getItemRevision().okToModify() )
                    {
                    	
                        MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                                , "This operation is not allowd.\nParent Revision is not Modifiable."
                                , "BOM Edit", MessageBox.INFORMATION);
                        return false;
                    }
                    else
                    {
                        return isHistoryChildMatchECO(parentECORev, childRev);
                    }
                }
            }
        } else {
            if(childRev.getProperty("s7_STAGE").equals("P")) {
            	
//            	TCComponentBOMViewRevision bvr = getBOMViewRevision();
            	// Assy 인경우
                if(bvr != null && !bvr.okToModify()) {
                    MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                            , "This operation is not allowd.\nParent BOMViewRevision is not Modifiable."
                            , "BOM Edit", MessageBox.INFORMATION);
                    return false;
                }
                // Component 하위에 추가하는 경우
                else if( bvr == null && !parentLine.getItemRevision().okToModify() )
                {
                	
                    MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                            , "This operation is not allowd.\nParent Revision is not Modifiable."
                            , "BOM Edit", MessageBox.INFORMATION);
                    return false;
                }
                else
                {
                    TCComponentItemRevision parentECORev = getRevisionECO(parentLine.getItemRevision());
                    return isHistoryChildMatchECO(parentECORev, childRev);
                }
            	

            } else {
                MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                        , "This operation is not allowd.\nRegular production part only can be added to Reqular production part."
                        , "BOM Edit", MessageBox.INFORMATION);
                return false;
            }
        }
    }
    
    /**
     * 
     * @param parent
     * @param child
     * @return
     * @throws TCException
     */
    public boolean isCuttable(TCComponentBOMLine parent, TCComponentBOMLine child) throws TCException {
        if(isSkipHistory()) {
            return true;
        }
        String parentType = parent.getProperty("bl_item_object_type");
        if(parentType.equals("Function")) {
            // 000 FunctionMaster
            if(child.getProperty("bl_rev_item_revision_id").equals("000")) {
                // 하위가 없는 FunctionMaster 는 제거 가능
                if(child.getProperty("bl_rev_release_status_list").equals("")) {
                    if(!child.getLogicalProperty("bl_has_children")) {
                        return true;
                    } else {
                        MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                                , "This operation is not allowd.\nYou can't cut Function Master has children."
                                , "BOM Edit", MessageBox.INFORMATION);
                    }
                } else {
                    MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                            , "This operation is not allowd.\nYou can't cut released Function Master."
                            , "BOM Edit", MessageBox.INFORMATION);
                }
            } else {
                MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                        , "This operation is not allowd.\nYou can't cut Function Master."
                        , "BOM Edit", MessageBox.INFORMATION);
            }
            return false;
        }
        return true;
    }
    
    public void setUpperBOMProperty() throws TCException {
        String itemType = getProperty("bl_item_object_type");
        if(itemType.equals("Function") || itemType.equals("Variant") || itemType.equals("Product")) {
            if(getBOMViewRevision() != null) {
                getBOMViewRevision().setProperty("ip_classification", "top-secret");
            }
        }
    }
    
    /**
     * none history add or Replace check
     * @param parentLine
     * @param childRev
     * @return
     * @throws TCException
     */
    public boolean isAddOrReplacable(TCComponentBOMLine parentLine, TCComponentItemRevision childRev) throws TCException {
        if(isSkipHistory()) {
            return true;
        }
        String parentType = parentLine.getProperty("bl_item_object_type");
        String childType = childRev.getItem().getProperty("object_type");
        if(parentType.equals("Function")) {
            if(!childType.equals("Function Master")) {
                MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                        , "This operation is not allowd.\nYou can only add Function Master to Function."
                        , "BOM Edit", MessageBox.INFORMATION);
                return false;
            } else {
                String functionId = parentLine.getProperty("bl_item_item_id");
                String fmasterId = childRev.getProperty("item_id");
//                if(!fmasterId.substring(1).startsWith(functionId.substring(1))) {
                //[SR190821-022][CSH]functon 두번째 이하와 fmp 두번째 이하 마지막 제외한 id가 일치하여야함.
                if(!fmasterId.substring(1,fmasterId.length()-1).equals(functionId.substring(1))) {
                    MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                            , "This operation is not allowd.\nFunction Master ID(" + fmasterId + ") is not valid!"
                            , "BOM Edit", MessageBox.INFORMATION);
                    return false;
                }
            }
        } else if(parentType.equals("Variant")) {
            if(!childType.equals("Function")) {
                MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                        , "This operation is not allowd.\nYou can only add Function to Variant."
                        , "BOM Edit", MessageBox.INFORMATION);
                return false;
            }
        } else if(parentType.equals("Product")) {
            if(!childType.equals("Variant")) {
                MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                        , "This operation is not allowd.\nYou can only add Variant to Product."
                        , "BOM Edit", MessageBox.INFORMATION);
                return false;
            }
        } else if(parentType.equals("Standard Part")) {
            MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                    , "This operation is not allowd.\nYou can't add part to Standard Part."
                    , "BOM Edit", MessageBox.INFORMATION);
            return false;
        } else {
            String childRegular = childRev.getProperty("s7_REGULATION");
            if(childRegular.equals("I")) {
                TCComponentItemRevision parentRev = parentLine.getItemRevision();
                String parentRegular = parentRev.getProperty("s7_REGULATION");
                if(!parentRegular.equals("I")) {
                    MessageBox.post(AIFUtility.getActiveDesktop().getShell()
                            , "This operation is not allowd.\nTemp part only can be added to Temp part."
                            , "BOM Edit", MessageBox.INFORMATION);
                    return false;
                }
            }
        }
        
        /**
         * [SR150106-024][20150115][jclee] Obsolete된 Standard Part 사용불가하게 하는 기능
         */
        if(childType.equals("Standard Part")) {
        	String sMaturity = "";
        	String sReleaseStatusList = "";
        	boolean isObsolete = false;
        	
        	sMaturity = childRev.getItem().getLatestItemRevision().getProperty("s7_MATURITY");
        	sReleaseStatusList = childRev.getProperty("release_status_list");
        	if (sMaturity == null || sMaturity.length() == 0 || sMaturity.equals("")
        			|| sReleaseStatusList == null || sReleaseStatusList.length() == 0 || sReleaseStatusList.equals("")) {
//        		MessageBox.post(AIFUtility.getActiveDesktop().getShell()
//        				, "This operation is not allowed.\nCheck the Maturity or Release Status of Standard Part."
//        				, "BOM Edit", MessageBox.INFORMATION);
//        		return false;
        	} else {
        		isObsolete = sMaturity.equals("Obsolete") || sReleaseStatusList.equals("Obsolete");
        	}
        	
        	if (isObsolete) {
        		MessageBox.post(AIFUtility.getActiveDesktop().getShell()
        				, "This operation is not allowd.\nYou can't constitute an Obsoleted Standard Part to BOM."
        				, "BOM Edit", MessageBox.INFORMATION);
        		return false;
        	}
        }
        
        return true;
    }

    /**
     * 신규 추가 BOM Line 여부 설정
     * @param isAdded
     */
    public void setAdded(boolean isAdded) {
    	this.isAdded = isAdded;
    }
    
    /**
     * 신규 추가 BOM Line 여부 반환
     * @return
     */
    public boolean getAdded() {
    	return this.isAdded;
    }
    
//    /**
//     * BOMLine 수량 반환
//     * @return
//     */
//    public String getQuantity() throws Exception {
//    	String sQty = "";
//    	int iQty = 0;
//    	boolean isPacked = false;
//		TCComponentBOMLine parent = parent();
//		SYMCBOMLine topLine = (SYMCBOMLine)window().getTopBOMLine();
//		
//		if (parent == null || this == topLine) {
//			throw new Exception(this.toString() + " is a top line.");
//		}
//
//		if (this.isPacked()) {
//			isPacked = true;
//			this.unpack();
//		}
//		
//		AIFComponentContext[] aifChildren = parent.getChildren();
//		for (int inx = 0; inx < aifChildren.length; inx++) {
//			SYMCBOMLine blChild = (SYMCBOMLine)aifChildren[inx].getComponent();
//			String[] properties = new String[] {"bl_item_item_id", "bl_sequence_no", "", "bl_variant_condition"};
//			
//			String[] saChild = blChild.getProperties(properties);
//			String[] saThis = this.getProperties(properties);
//			
//			
//			
//			if (blChild == this) {
//				iQty++;
//			}
//		}
//		
//		if (isPacked) {
//			this.pack();
//		}
//		
//		
//		sQty = String.valueOf(iQty);
//    	return sQty;
//    }
}
