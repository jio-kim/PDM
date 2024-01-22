/**
 * 
 */
package com.symc.plm.me.utils.variant;

import java.util.Vector;

import com.teamcenter.rac.pse.variants.modularvariants.ConditionElement;

/**
 * Condition Element를 담아둘 새로운 타입을 정의함.
 * 
 * @author slobbie
 * 
 */
public class ConditionVector extends Vector<ConditionElement> {

    private static final long serialVersionUID = -7455584080923098742L;

    @Override
    public synchronized String toString() {
        String s = "";
        ConditionElement[] conditions = new ConditionElement[elementCount];
        System.arraycopy(elementData, 0, conditions, 0, elementCount);
        for (int i = 0; i < elementCount; i++) {
            ConditionElement condition = conditions[i];
            String opStr = "";
            if (condition.ifOrAnd == null || condition.ifOrAnd.equals("")) {
                opStr = "";
            } else {
                if (condition.ifOrAnd.equalsIgnoreCase("and")) {
                    opStr = condition.ifOrAnd.toUpperCase();
                }
            }
            s += " " + opStr + " " + condition.value;
        }
        return s;
    }

}
