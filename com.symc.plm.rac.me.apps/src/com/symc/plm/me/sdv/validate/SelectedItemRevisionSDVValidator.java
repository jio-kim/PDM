/**
 *
 */
package com.symc.plm.me.sdv.validate;

import java.util.List;
import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : LineSelectedSDVValidator Class Description :
 *
 * @date 2013. 12. 18.
 *
 */
public class SelectedItemRevisionSDVValidator implements ISDVValidator {

    @SuppressWarnings("unused")
    private static final Registry registry = Registry.getRegistry(SelectedItemRevisionSDVValidator.class);

    private List<String> types;

    /**
     * Description :
     *
     * @method :
     * @date : 2013. 12. 18.
     * @param :
     * @return :
     * @see org.sdv.core.common.ISDVValidator#validate(java.lang.String, java.util.Map)
     */
    @Override
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {

        // Type 유형을 지정하지 않았다면 검증처리를 하지 않는다.
//        if (types == null || types.size() == 0) {
//            return;
//        }
//
//        String typeList = Joiner.on(", ").join(types);
        
        
//        TCComponentItemRevision component = null;
        try {
            InterfaceAIFComponent comp = AIFUtility.getCurrentApplication().getTargetComponent();
            if (!(comp instanceof TCComponentItemRevision||comp instanceof TCComponentBOPLine)) {
                throw new ValidateSDVException("Please Select ItemRevision.");
            } 
            
        } catch (Exception e) {
            throw new ValidateSDVException(e.getMessage(), e);
        }

    }

    /**
     * @return the types
     */
    public List<String> getTypes() {
        return types;
    }

    /**
     * @param types
     *            the types to set
     */
    public void setTypes(List<String> types) {
        this.types = types;
    }

}
