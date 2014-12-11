package com.houseelectrics.serializer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FieldReflectionNodeExpander implements NodeExpander
{

    public int expand(Object o, NodeExpansionListener listener)
    {
        if (o == null) { return 0; }
        Class theclass = o.getClass();
        Field[] fis = theclass.getDeclaredFields();
        int availableFieldCount=0;
        for (Field fi : fis)
        {
            String fieldName = fi.getName();
            int mods = fi.getModifiers();
            // exclude inner class fields like this$0 etc
            if (Modifier.isPrivate(mods) || Modifier.isStatic(mods)
                    || fieldName.indexOf('$')>=0)
            {
                continue;
            }
            Object value = null;
            try {
                value = fi.get(o);
                availableFieldCount++;
            }
            catch (IllegalAccessException ilex)
            {
                RuntimeException rex = new RuntimeException("failed to get field " + theclass.getName() + "." + fi.getName());
                rex.initCause(ilex);
                throw rex;
            }

            listener.OnChildNode (o, fi.getName(), value);
        }
        return availableFieldCount;
    }
}
