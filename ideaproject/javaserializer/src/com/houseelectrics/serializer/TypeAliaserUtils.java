package com.houseelectrics.serializer;
import java.util.*;
/**
 * Created by roberttodd on 29/11/2014.
 */
public class TypeAliaserUtils
{
    public static TypeAntiAliaser createSimpleTypeAntiAliaser()
    {
        TypeAntiAliaser result = new TypeAntiAliaser()
        {
            @Override
            public Class antiAlias(String alias)
            {
              /*  IEnumerable<Class> typeHints = AppDomain.CurrentDomain.GetAssemblies().Select(assembly => assembly.GetType(value)).Where(t => t != null);
                Type type = typeHints.Count() == 0 ? null : typeHints.First();
                return type;*/
                //throw new RuntimeException(" createSimpleTypeAntiAliaser not implemented ");
                try
                {
                    return Class.forName(alias);
                }
                catch (ClassNotFoundException cnfe)
                {
                    return null;
                }
            }
        };

        return result;
    }


    public static TypeAliaser createNumericTypeNameAliaser()
    {
        final Map<Class, String> type2Alias = new HashMap<Class, String>();
        TypeAliaser aliaser = new TypeAliaser()
        {
            @Override
            public String alias(Class type)
            {
                String alias;
                if (!type2Alias.containsKey(type))
                {
                    alias = "_" + type2Alias.keySet().size();
                    type2Alias.put(type, alias);
                }
                else
                {
                    alias = type2Alias.get(type);
                }
                return alias;
            }
        };


        return aliaser;
    }


}
