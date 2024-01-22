/**
 *
 */
package com.symc.plm.me.sdv.validate;

import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : RevisionRuleCheckSDVValidator
 * Class Description :
 *
 * @date 2013. 12. 18.
 *
 */
public class RevisionRuleCheckSDVValidator implements ISDVValidator {
    private Registry registry = Registry.getRegistry(RevisionRuleCheckSDVValidator.class);

    private String revisionRuleListPreference;

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
        try {
            boolean result = false;

            // Preference 가져오기
            TCSession tcsession = CustomUtil.getTCSession();
//            String[] revisionRuleList = tcsession.getPreferenceService().getStringArray(0, revisionRuleListPreference);
            String[] revisionRuleList = tcsession.getPreferenceService().getStringValues(revisionRuleListPreference);
            String displayRevisionRule = "";
            if (revisionRuleList.length > 0)
            {
                MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
                TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();
                TCComponentRevisionRule bopRevisionRule = null;
                bopRevisionRule = bomWindow.getRevisionRule();
                for (int i = 0; i < revisionRuleList.length; i++)
                {
                    displayRevisionRule += revisionRuleList[i].toString();
                    if (revisionRuleList[i].equals(bopRevisionRule.toString()))
                    {
                        result = true;
                        break;
                    }
                    if (revisionRuleList.length != (i+1))
                        displayRevisionRule += ",";
                }
                if (!result)
                    throw new ValidateSDVException(registry.getString("RevisionRuleCheck.WrongChoice.MESSAGE").replace("%0", bopRevisionRule.toString()) + "\n" + registry.getString("RevisionRuleCheck.WrongChoice2.MESSAGE").replace("%0", displayRevisionRule));
            }
        } catch (ValidateSDVException ve) {
            throw ve;
        } catch (Exception e) {
            throw new ValidateSDVException(e.getMessage(), e);
        }
    }

    /**
     * @return the types
     */
    public String getRevisionRuleListPreference() {
        return revisionRuleListPreference;
    }

    /**
     * @param types
     *            the types to set
     */
    public void setRevisionRuleListPreference(String revisionRuleListPreference) {
        this.revisionRuleListPreference = revisionRuleListPreference;
    }

}
