package com.houseelectrics.serializer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by roberttodd on 02/12/2014.
 */
public class ReflectionUtil
{
    final static Class[] noargs = {};

    public static Method getMethodForPropertyName(Class theClass, String propertyName) throws NoSuchMethodException
    {
        String methodName = "get" + Character.toUpperCase(propertyName.charAt(0)) +
                propertyName.substring(1);
        Method m = theClass.getMethod(methodName, noargs);
        return m;
    }

    public static Object getPropertyValueReturnExceptions(Object o, String propertyName)
    {
        try {
            Method m = getMethodForPropertyName(o.getClass(), propertyName);
            return m.invoke(o, noargs);
        }
        catch (Exception ex)
        {
            return ex;
        }

    }
    public static Object getFieldValueReturnExceptions(Object o, String fieldName)
    {
        try {
            Field f = o.getClass().getField(fieldName);
            return f.get(o);
        }
        catch (Exception ex)
        {
            return ex;
        }

    }

    public static Type[] getParameterizedTypesForField(Class parentClass, String fieldName)
    {
        try
        {
            Field thefield = parentClass.getDeclaredField(fieldName);
            Type  genericType = thefield.getGenericType();
            if (!(genericType instanceof ParameterizedType))
            {
                return null;
            }
            ParameterizedType pType = (ParameterizedType)genericType ;
            return pType.getActualTypeArguments();
        }
        catch (NoSuchFieldException nex)
        {
            return null;
        }
    }

    public static Type[] getParameterizedTypesForProperty(Class parentClass, String fieldName)
    {
        try
        {
            Method getter = getMethodForPropertyName(parentClass, fieldName);
            Type  genericType = getter.getGenericReturnType();
            if (!(genericType instanceof ParameterizedType))
            {
                return null;
            }
            ParameterizedType pt = (ParameterizedType) genericType;
            return pt.getActualTypeArguments();
        }
        catch (NoSuchMethodException nex)
        {
            return null;
        }
    }

    public static Class getPropertyOrReflectionType(Class theClass, String propertyName)
    {
        Method m = null;
        Class result=null;
        try
        {
            m= ReflectionUtil.getMethodForPropertyName(theClass, propertyName);
        } catch (Exception ex)
        {

        }
        if (m != null)
        {
            result = m.getReturnType();
        } else
        {
            try
            {
                Field f = theClass.getField(propertyName);
                result = f.getType();
            }
            catch (NoSuchFieldException nex)
            {

            }
        }
        return result;
    }


    public static Object valueForKeyPathWithIndexes(Object source, String path, boolean returnNull4OutOfRangeIndex, boolean throwExceptionOnError)

    {
        String[] elements = path.split("\\.");
        Object context = source;
        for (int done=0; done < elements.length; done++)
        {
            String element = elements[done].trim();
            Integer index = null;
            if (element.contains("["))
            {
                int indexBracketIndex = element.indexOf('[');
                String strIndex = element.substring(indexBracketIndex+1, element.length()-1);
                index = Integer.parseInt(strIndex);
                element = element.substring(0, indexBracketIndex);
            }
            element = element.substring(0,1).toUpperCase() + element.substring(1);
            Object parent = context;
            if (element.equals("Count") && context instanceof List)
            {
                context = ((List)context).size();
            }
            else
            {
                context = ReflectionUtil.getPropertyValueReturnExceptions(context, element);
                if (index!=null)
                {
                    List lcontext = (List)context;
                    if ((lcontext== null && returnNull4OutOfRangeIndex)  || index>=lcontext.size()) context = null;
                    else context = lcontext.get(index);
                }
            }
            if (context instanceof Throwable)
            {
                if (throwExceptionOnError)
                {
                    String message = "failed to extract " + parent.getClass().getName() + "." + element;
                    RuntimeException rex = new RuntimeException(message, (Exception) context);
                    throw rex;
                }
                else
                {
                    context = null;
                }
            }
            if (context==null) break;
        }

        return context;
    }


}
