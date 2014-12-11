package com.houseelectrics.serializer;

import java.lang.reflect.*;

public class PropertyReflectionNodeExpander implements NodeExpander
{
    private boolean excludeReadOnlyProperties = false;
    public boolean getExcludeReadOnlyProperties() { return excludeReadOnlyProperties; }
    public void setExcludeReadOnlyProperties(boolean value) { this.excludeReadOnlyProperties = value; }
    public int expand(Object o, NodeExpansionListener listener)
    {
        Method[]ms= o.getClass().getMethods();
        int setAbleCount = 0;

        for  (Method m : ms)
        {
            if (!Modifier.isPublic(m.getModifiers())) continue;
            if (!m.getName().startsWith("get")) continue;
            if (m.getDeclaringClass()==Object.class) continue;
            Class []prms = m.getParameterTypes();
            if (prms!=null && prms.length>0) continue;
            if (excludeReadOnlyProperties)
            {
                String setMethodName = "set" + m.getName().substring(3);
                Class []paramTypes = {m.getReturnType()};
                Method setMethod = null;
                try {
                    setMethod = o.getClass().getMethod(setMethodName, paramTypes);
                }
                catch (NoSuchMethodException ex)
                {
                    //todo - eliminate this !
                }
                if (setMethod==null) {continue;}
            }
            Object value = null;
            try
            {
                Object []params={};
                value = m.invoke(o, params);
                setAbleCount++;
            }
            catch (Exception ex)
            {
                RuntimeException exx = new RuntimeException("failed to get value for property " + m.getName() + " declared in "+ m.getDeclaringClass().getName(), ex);
                throw exx;
            }

            String name =   m.getName().substring(3);//Character.toLowerCase(m.getName().charAt(3) )  +   m.getName().substring(4);
            listener.OnChildNode(o, name, value);
        }
        return setAbleCount;
    }
}

