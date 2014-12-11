package com.houseelectrics.serializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by roberttodd on 30/11/2014.
 */
public class ObjectIDGenerator
{
    public static class IdResult
    {
        int id;
        boolean isNew;
    }

    Map<Integer, Integer> id2external = new HashMap<Integer, Integer>();
    public IdResult getId(Object o)
    {
        IdResult result = new IdResult();
        // assume identity hash code is unique per object
        int internalId = System.identityHashCode(o);
        if (!id2external.containsKey(internalId))
        {
           result.isNew=true;
           result.id = id2external.size();
            id2external.put(internalId, result.id);
        }
        else
        {
            result.isNew = false;
            result.id = id2external.get(internalId).intValue();
        }
        return result;
    }
}
