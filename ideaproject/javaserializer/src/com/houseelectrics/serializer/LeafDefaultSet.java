package com.houseelectrics.serializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by roberttodd on 30/11/2014.
 */
public class LeafDefaultSet
{
    Map<Class, Defaults4Class> type2Defaults = new HashMap<Class,Defaults4Class>();
    public Map<Class, Defaults4Class> getType2Defaults() { return type2Defaults; }
    public void setType2Defaults(Map<Class, Defaults4Class> value) { type2Defaults = value; }

    public Map<String, Defaults4Class> getAlias2Defaults(TypeAliaser typeAliaser)
    {
        Map<String, Defaults4Class> result = new HashMap<String, Defaults4Class>();
        for (Class type : getType2Defaults().keySet())
        {
            String alias = typeAliaser.alias(type);
            result.put(alias, getType2Defaults().get(type));
        }
        return result;
    }

    public LeafDefaultSet Add(LeafDefaultSet right)
    {
        for (Class t :  right.type2Defaults.keySet())
        {
            this.type2Defaults.put(t, right.type2Defaults.get(t));
        }
        return this;
    }
}
