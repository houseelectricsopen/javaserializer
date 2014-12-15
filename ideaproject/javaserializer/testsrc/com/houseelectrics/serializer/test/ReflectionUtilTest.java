package com.houseelectrics.serializer.test;

import com.houseelectrics.serializer.ReflectionUtil;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
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


    public static class TestDetail
    {
        private String _detailName;
        public String getDetailName() {return _detailName;}
        public void setDetailName(String value) {this._detailName=value;}
    }

    public static class TestMaster
    {
        private List<TestDetail> _details = new ArrayList<TestDetail>();
        public List<TestDetail> getDetails() {return _details;}

        private List<TestDetail> _emptydetails = null;
        public List<TestDetail> getEmptyDetails() {return _emptydetails;}
    }

    //todo add this to csharp
    @Test
    public void testValueForKeypath()
    {
        TestMaster data = new TestMaster();
        TestDetail detail = new TestDetail();
        detail.setDetailName("erertyer");
        data.getDetails().add(detail);
        String paths[] = {"details.Count", "details[0].DetailName", "details[1].DetailName", "emptyDetails[0]"};
        Object []expectedResults= {data.getDetails().size(), data.getDetails().get(0).getDetailName(), null, null };
        for (int done=0; done<paths.length; done++)
        {
            Object expected = expectedResults[done];
            String path = paths[done];
                Object actualValue = ReflectionUtil.valueForKeyPathWithIndexes(data, path, true, true);
            Assert.assertEquals("value at path " + data.getClass().getSimpleName() + "." + path, expected, actualValue);
        }
    }

}
