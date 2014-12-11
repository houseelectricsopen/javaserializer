package com.houseelectrics.serializer.test;

import com.houseelectrics.serializer.ReflectionUtil;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by roberttodd on 03/12/2014.
 */
public class ReflectionUtilTest
{
    public static class ReflectionTestData
    {
        public Map<Integer, String> integerStringMap;
        public Map<Integer, String> getIntegerStringMap() {return integerStringMap;}
    }

    @Test
    public void testParameterisedTypes()
    {
        ReflectionTestData testData = new ReflectionTestData();
        Type[] types = ReflectionUtil.getParameterizedTypesForField(testData.getClass(), "integerStringMap");
        System.out.println("" + ((Class) types[0]).getName());

        types = ReflectionUtil.getParameterizedTypesForProperty(testData.getClass(), "IntegerStringMap");
        System.out.println("" + ((Class) types[0]).getName());


    }
}
