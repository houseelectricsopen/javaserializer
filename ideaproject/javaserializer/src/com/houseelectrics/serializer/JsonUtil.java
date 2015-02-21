package com.houseelectrics.serializer;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by roberttodd on 16/01/2015.
 */
public class JsonUtil
{
    public static Object loadObjectFromJsonFileViaClassLoader(Class theClass, String filename, Class rootType) throws IOException
    {
        InputStream is = theClass.getResourceAsStream(filename);
        byte[] encoded = new byte[is.available()];
        is.read(encoded);
        String json =  new String(encoded/*, StandardCharsets.UTF_8*/);
        Json2Object j2o = new Json2Object();
        Object result = j2o.toObject(json, rootType);
        return result;
    }

}
