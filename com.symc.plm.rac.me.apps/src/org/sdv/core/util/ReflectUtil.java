/**
 * 
 */
package org.sdv.core.util;

import java.lang.reflect.Method;

/**
 * Class Name : ReflectUtil
 * Class Description : 
 * @date 	2013. 12. 5.
 * @author  CS.Park
 * 
 */
public class ReflectUtil {
    

    public static Method getLocalMethod(Class<?> current, String methodName, Class<?> [] argumentTypes) {
        Method method = null;

        while (current != Object.class) {
             try {
                 if(argumentTypes == null) {
                     method = current.getDeclaredMethod(methodName);
                     method.setAccessible(true);
                 } else {
                     method = current.getDeclaredMethod(methodName, argumentTypes);
                     method.setAccessible(true);
                 }
                 break;
             } catch (NoSuchMethodException ex) {
                  current = current.getSuperclass();
             }
        }
       return method;
    }


}
