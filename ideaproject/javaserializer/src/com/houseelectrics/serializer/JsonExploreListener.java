package com.houseelectrics.serializer;

/**
 * Created by roberttodd on 29/11/2014.
 */
public interface JsonExploreListener
{

    void JsonStartObject(String propertyName, int pos);
    void JsonLeaf(String propertyName, String value, boolean isQuoted);
    void JsonEndObject(int pos);
    void JsonStartFunction(String functionName, int pos, String propertyName);
    void JsonEndFunction(int pos);
    void JsonStartArray(String propertyName, int pos);
    void JsonEndArray(int pos);

}
