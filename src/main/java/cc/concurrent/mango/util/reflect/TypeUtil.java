package cc.concurrent.mango.util.reflect;

import com.google.common.base.Strings;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author ash
 */
public class TypeUtil {

    public static Type getPropertyType(Type type, String propertyPath) {
        if (Strings.isNullOrEmpty(propertyPath)) { // 如果propertyPath为空，直接返回clazz
            return type;
        }
        int pos = propertyPath.indexOf('.');
        while (pos > -1) {
            String propertyName = propertyPath.substring(0, pos);
            try {
                if (type instanceof Class<?>) {
                    PropertyDescriptor pd = new PropertyDescriptor(propertyName, (Class<?>) type);
                    Method method = pd.getReadMethod();
                    type = method.getGenericReturnType();
                } else {
                    // TODO Exception
                }
            } catch (IntrospectionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            propertyPath = propertyPath.substring(pos + 1);
            pos = propertyPath.indexOf('.');
        }
        try {
            if (type instanceof Class<?>) {
                PropertyDescriptor pd = new PropertyDescriptor(propertyPath, (Class<?>) type);
                Method method = pd.getReadMethod();
                type = method.getGenericReturnType();
            } else {
                // TODO Exception
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return type;
    }

}
