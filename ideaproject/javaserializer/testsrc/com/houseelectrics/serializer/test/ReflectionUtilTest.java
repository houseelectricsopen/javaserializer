package com.houseelectrics.serializer.test;

import com.houseelectrics.serializer.ReflectionUtil;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Type;
import java.sql.Ref;
import java.util.*;

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
        Assert.assertEquals(Integer.class, types[0]);
        Assert.assertEquals(String.class, types[1]);

        types = ReflectionUtil.getParameterizedTypesForProperty(testData.getClass(), "IntegerStringMap");
        Assert.assertEquals(Integer.class, types[0]);
        Assert.assertEquals(String.class, types[1]);
        System.out.println("" + ((Class) types[0]).getName());

    }


    public static class TestDetail
    {
        private String _detailName;
        public String getDetailName() {return _detailName;}
        public void setDetailName(String value) {this._detailName=value;}

        private int _id;
        public int getId() {return _id;}
        public void setId(int value) {this._id = value;}
    }

    public static class TestMaster
    {
        private List<TestDetail> _details = new ArrayList<TestDetail>();
        public List<TestDetail> getDetails() {return _details;}
        public void setDetails(List<TestDetail> value) {this._details=value;}

        private List<TestDetail> _emptydetails = null;
        public List<TestDetail> getEmptyDetails() {return _emptydetails;}

        private TestDetail _detail;
        public void setDetail(TestDetail value) {this._detail = value;}
        public TestDetail getDetail() {return this._detail;}

    }

    //todo add this to csharp
    @Test
    public void testValueForKeypath()
    {
        TestMaster data = new TestMaster();
        TestDetail detail = new TestDetail();
        detail.setDetailName("erertyer");
        data.getDetails().add(detail);
        String paths[] = {"details.Count", "details[0].DetailName", "details[1].DetailName", "emptyDetails[0]", ""};
        Object []expectedResults= {data.getDetails().size(), data.getDetails().get(0).getDetailName(), null, null, data };
        for (int done=0; done<paths.length; done++)
        {
            Object expected = expectedResults[done];
            String path = paths[done];
                Object actualValue = ReflectionUtil.valueForKeyPathWithIndexes(data, path, true, true);
            Assert.assertEquals("value at path " + data.getClass().getSimpleName() + "." + path, expected, actualValue);
        }
    }

    @Test
    public void testPropertyReference() throws Exception
    {
        TestMaster data = new TestMaster();
        TestDetail detail = new TestDetail();
        data.getDetails().add(detail);
        ReflectionUtil.PropertyReference reference;
        reference = ReflectionUtil.getPropertyReferenceForKeyPathWithIndexes(data, "details[0].DetailName", true, true, null);
        Assert.assertNotNull("reference should be found", reference);
        String detailValue = "abcdefg";
        reference.set(detailValue);
        Assert.assertEquals("called setter", detailValue, data.getDetails().get(0).getDetailName());
        String gotValue = (String)reference.get();
        Assert.assertEquals("called getter", detailValue, gotValue);

        reference = ReflectionUtil.getPropertyReferenceForKeyPathWithIndexes(data, "details", true, true, null);
        Assert.assertEquals("checking detail type", List.class, reference.getType());
        Assert.assertEquals("checking generic type", TestDetail.class, reference.getParametrisedTypes()[0]);


    }

    @Test
    public void testPropertyReferenceAutocreateParent() throws Exception
    {
        TestMaster data = new TestMaster();
        TestDetail detail = new TestDetail();
        data.getDetails().add(detail);

        ReflectionUtil.ObjectCreator autocreate = new ReflectionUtil.ObjectCreator()
        {
            @Override
            public Object newInstance(Object parentContext, String propertyName, Class theClass)
               {
                   try
                   {
                       return theClass.newInstance();
                   }
                   catch (Exception ex)
                   {
                       RuntimeException rex = new RuntimeException("cant create a " + theClass.getName(), ex);
                       throw rex;
                   }
               }
        };

        ReflectionUtil.PropertyReference reference;
        reference = ReflectionUtil.getPropertyReferenceForKeyPathWithIndexes(data, "detail.detailName", true, true, autocreate);
        Assert.assertNotNull("reference should be found", reference);
        String detailValue = "abcdefg";
        reference.set(detailValue);
        Assert.assertEquals("did the setter set?", detailValue, data.getDetail().getDetailName());
        String gotValue = (String)reference.get();
        Assert.assertEquals("did the getter get?", detailValue, gotValue);
    }

    @Test
    public void testGetPublicReadWriteableProperties()
    {
       Class theClass = TestMaster.class;
       List<ReflectionUtil.PropertyReference> refs =  ReflectionUtil.getPublicReadWriteableProperties(theClass);
       Assert.assertEquals("should be 1 ref in " + theClass.getName(), refs.size(), 2);
       ReflectionUtil.PropertyReference ref = refs.get(0);
       Assert.assertEquals(ref.getPropertyName(), "Detail");
       Assert.assertEquals(ref.getGetterMethod().getName(), "getDetail");
       Assert.assertEquals(ref.getGetterMethod().getReturnType(), TestDetail.class);
       Assert.assertEquals(ref.getSetterMethod().getName(), "setDetail");

    }


    @Test
    public void testDeepCompare() throws Exception
    {
        TestMaster tm1 = new TestMaster();
        TestMaster tm2 = new TestMaster();

        List<ReflectionUtil.DeepCompareDifference> diffsDetected = new ArrayList<ReflectionUtil.DeepCompareDifference>();

        boolean match;
        diffsDetected  = ReflectionUtil.deepCompareViaReadWriteableProperties(tm1, tm2);
        Assert.assertEquals(diffsDetected.size(), 0);

        tm1.setDetail(new TestDetail());
        diffsDetected = ReflectionUtil.deepCompareViaReadWriteableProperties(tm1, tm2);
        Assert.assertEquals(diffsDetected.size(), 1);
        Assert.assertEquals(diffsDetected.get(0).propertyPath, "Detail");

        tm2.setDetail(new TestDetail());
        diffsDetected = ReflectionUtil.deepCompareViaReadWriteableProperties(tm1, tm2);

        Assert.assertEquals(diffsDetected.size(), 0);

        tm1.getDetails().add(new TestDetail());
        diffsDetected = ReflectionUtil.deepCompareViaReadWriteableProperties(tm1, tm2);
        Assert.assertEquals(diffsDetected.size(), 1);
        Assert.assertEquals(diffsDetected.get(0).propertyPath, "Details");

        tm2.getDetails().add(new TestDetail());
        diffsDetected = ReflectionUtil.deepCompareViaReadWriteableProperties(tm1, tm2);
        Assert.assertEquals(diffsDetected.size(), 0);

        tm2.getDetails().get(0).setDetailName("hello");
        diffsDetected = ReflectionUtil.deepCompareViaReadWriteableProperties(tm1, tm2);
        Assert.assertEquals(diffsDetected.size(), 1);
        Assert.assertEquals(diffsDetected.get(0).propertyPath, "Details.0.DetailName");

        tm1.getDetails().get(0).setDetailName("goodbye");
        diffsDetected = ReflectionUtil.deepCompareViaReadWriteableProperties(tm1, tm2);
        Assert.assertEquals(diffsDetected.size(), 1);
        Assert.assertEquals(diffsDetected.get(0).propertyPath, "Details.0.DetailName");

        tm1.getDetails().get(0).setId(321);
        diffsDetected = ReflectionUtil.deepCompareViaReadWriteableProperties(tm1, tm2);
        Assert.assertEquals(diffsDetected.size(), 2);
        Assert.assertEquals(diffsDetected.get(0).propertyPath, "Details.0.DetailName");
        Assert.assertEquals(diffsDetected.get(1).propertyPath, "Details.0.Id");

    }


    public static class MapContainerTestClass
    {
        private Map mapField;
        public Map getMapField() {return mapField;}
        public void setMapField(Map value) {this.mapField=value;}
    }

    @Test
    public void testMapCompare() throws Exception
    {
        MapContainerTestClass m1 = new MapContainerTestClass();
        m1.setMapField(new HashMap());
        m1.getMapField().put("one", 1);
        m1.getMapField().put("two", "zwei");
        MapContainerTestClass m2 = new MapContainerTestClass();
        m2.setMapField(new HashMap());
        m2.getMapField().put("one", 1);
        m2.getMapField().put(3, "three");
        List<ReflectionUtil.DeepCompareDifference> diffs;
        diffs = ReflectionUtil.deepCompareViaReadWriteableProperties(m1, m2);
        for (int done=0; done< diffs.size(); done++)
        {
            ReflectionUtil.DeepCompareDifference diff = diffs.get(done);
            System.out.println("Diff: " + diff.propertyPath + " " + diff.description);
        }
        Assert.assertEquals(2, diffs.size());
        Assert.assertEquals(diffs.get(0).propertyPath, "MapField.two");
        Assert.assertEquals(diffs.get(1).propertyPath, "MapField.3");


    }

}
