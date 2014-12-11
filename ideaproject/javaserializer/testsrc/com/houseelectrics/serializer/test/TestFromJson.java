package com.houseelectrics.serializer.test;
import com.houseelectrics.serializer.*;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.*;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Ref;
import java.util.*;

/**
 * Created by roberttodd on 30/11/2014.
 */
public class TestFromJson extends EasyMockSupport
{
    public static class SimpleTestObject
    {
        public String stringFieldA;
        public String stringPropertyB;
        public String getStringPropertyB() { return stringPropertyB; }
        public void setStringPropertyB(String value) { this.stringPropertyB = value; }

        public int intFieldC;
        public int intPropertyD;
        public int getIntPropertyD() {return intPropertyD; }
        public void setIntPropertyD(int value) { this.intPropertyD = value; }
    }

    @Test
    public void testSimpleObjectNoTypeHints()
    {
        Throwable ex=null;
        Object result;
        String json = "{}";
        try { Json2Object j2O = new Json2Object(); result = j2O.toObject(json); }
        catch (Throwable tex)
        {
            ex=tex;
        }
        Assert.assertEquals ("expected no type hint indication", Json2Object.NoClueForTypeException.class, ex == null ? null : ex.getClass());
    }


    @Test
    public void testSimpleObjectHintAsParameter()
    {
        String json = "{}";
        Json2Object j2O = new Json2Object();
        Class type = SimpleTestObject.class;
        Object o = j2O.toObject(json, type );
        Assert.assertEquals("expected type", type, o.getClass() );
    }


    @Test
    public void testSimpleObjectHintAsAttribute()
    {
        Class type = SimpleTestObject.class;
        Json2Object j2O = new Json2Object();
        String json = "{" + j2O.getTypeSpecifier() + ":" + type.getName()  + "}";
        System.out.println("TestSimpleObjectHintAsAttribute json:" + json);
        Object o = j2O.toObject(json);
        Assert.assertEquals( "expected type", type, o.getClass());
    }


    // test as above but with incorrect classname
    @Test
    public void testSimpleObjectIncorrectHintAsAttribute()
    {
        Exception ex=null;
        Object result;
        Json2Object j2O = new Json2Object();
        Class type = SimpleTestObject.class;
        String json = "{" + j2O.getTypeSpecifier() + ":" + type.getName() + ".invalid}";
        System.out.println("TestSimpleObjectHintAsAttribute json:" + json);
        try {
            result = j2O.toObject(json);
        }
        catch (Exception nex)
        {
            ex=nex;
        }
        Assert.assertEquals("expected no type hint indication",  Json2Object.NoClueForTypeException.class, ex == null ? null : ex.getClass() );
    }

   @Test
    public void testFields()
    {
        SimpleTestObject result;
        Json2Object j2O = new Json2Object();
        Class type = SimpleTestObject.class;
        String stringFieldAvalue = "abcd";
        int intFieldValue = 9876;
        String json = "{" + j2O.getTypeSpecifier() + ":" + type.getName() + " stringFieldA:\"" + stringFieldAvalue + "\"  intFieldC:" + intFieldValue+ " }";
        System.out.println("TestSimpleObjectHintAsAttribute json:" + json);
        result = (SimpleTestObject) j2O.toObject(json);

        Assert.assertEquals  ( "matching string value", result.stringFieldA, stringFieldAvalue);
        Assert.assertEquals("matching int value", result.intFieldC, intFieldValue );

    }

    @Test
    @TestDescription(description = "Derived from testFields test")
    public void testFields4UserGuide()
    {
        Json2Object j2O = new Json2Object();
        String json = "{_class:com.houseelectrics.serializer.test.TestFromJson$SimpleTestObject stringFieldA:\"abcd\"  intFieldC:9876 }";
        SimpleTestObject result = (SimpleTestObject)j2O.toObject(json);
        Assert.assertEquals("matching string value", result.stringFieldA, "abcd");
        Assert.assertEquals( "matching int value", result.intFieldC, 9876);
    }


    @Test
    @TestDescription(description="Derived from testFields test")
    public void testFields4UserGuideTypeHint()
    {
        Json2Object j2O = new Json2Object();
        String json = "{stringFieldA:\"abcd\"  intFieldC:9876 }";
        SimpleTestObject result = (SimpleTestObject)j2O.toObject(json, SimpleTestObject.class);
        Assert.assertEquals("matching string value", result.stringFieldA, "abcd");
        Assert.assertEquals("matching int value", result.intFieldC, 9876 );
    }

    @Test
    public void testProperties()
    {
        SimpleTestObject result;
        Json2Object j2O = new Json2Object();
        //j2O.setToUseProperties();
        Class type = SimpleTestObject.class;
        String stringPropertyBvalue = "abcd123";
        int intPropertyValue = 98765;
        String json = "{" + j2O.getTypeSpecifier() + ":" + type.getName() + " StringPropertyB:\"" + stringPropertyBvalue + "\" IntPropertyD:" + intPropertyValue +" }";
        System.out.println("TestSimpleObjectHintAsAttribute json:" + json);
        result = (SimpleTestObject)j2O.toObject(json);
        Assert.assertEquals("propertyB value", stringPropertyBvalue, result.getStringPropertyB() );
        Assert.assertEquals("IntPropertyD value", intPropertyValue, result.getIntPropertyD() );
    }


    @Test
    @TestDescription(description = "try different Property types")
    public void testToJsonLeafTypesViaProperties()
    {
        AllPrimitiveLeafTypes template = new AllPrimitiveLeafTypes();
        String[] expressions = AllPrimitiveLeafTypes.testPropertyExpressions;
        //{ "ALong", "AShort", "ALongRef", "AString", "AChar", "AString2" };
        Object[] expectedValues = AllPrimitiveLeafTypes.testExpectedPropertyValues(template);
        //{ template.ALong, template.AShort, template.ALongRef, template.AString, template.AChar, template.AString2, template.AUint32, template.AUint64 };

        AllPrimitiveLeafTypes result;
        Json2Object j2O = new Json2Object();
        //j2O.setToUseProperties();
        Class type = AllPrimitiveLeafTypes.class;
        Object2Json o2J = new Object2Json();
        o2J.setNodeExpander(new PropertyReflectionNodeExpander());
        //todo json 2 Object should understand TypeAliaser
        o2J.setTypeAliaser(
              new TypeAliaser()
              {
                  @Override
                  public String alias(Class type)
                  {
                      return type.getName();
                  }
              }
        );

        o2J.setTypeAliasProperty(j2O.getTypeSpecifier());
        String json = o2J.toJson(template);
        System.out.println("testToJsonLeafTypesViaFields json:" + json);
        result = (AllPrimitiveLeafTypes)j2O.toObject(json);

        for (int done = 0; done < expressions.length; done++ )
        {
            String expression = expressions[done];
            String getMethod = "get" + Character.toUpperCase(expression.charAt(0)) + expression.substring(1);
            Object value = ReflectionUtil.getPropertyValueReturnExceptions(result, expression);
            Assert.assertEquals(expression + " value", expectedValues[done], value );
        }

    }

    public static class SubObject
    {
        public String fieldA;
    }
    public static class MasterObject
    {
        public SubObject subObject = null;
        public Object []theTopArray=null;
        public String[] theStringArray = null;
    }


    @Test
    @TestDescription(description="try embedded Object")
    public void testToJsonEmbedded()
    {
        MasterObject masterObject = new MasterObject();
        masterObject.subObject = new SubObject();
        masterObject.subObject.fieldA = "abc";

        MasterObject result;
        Json2Object j2O = new Json2Object();
        //j2O.setToUseFields();
        Class type = MasterObject.class;
        Object2Json o2J = new Object2Json();
        o2J.setNodeExpander(new FieldReflectionNodeExpander());
        //todo json 2 Object should understand TypeAliaser
        o2J.setTypeAliaser(
          new TypeAliaser()
          {
              @Override
              public String alias(Class type)
              {
                  return type.getName();
              }
          }
        );

        o2J.setTypeAliasProperty( j2O.getTypeSpecifier());
        String json = o2J.toJson(masterObject);
        System.out.println("testToJsonLeafTypesViaFields json:" + json);
        result = (MasterObject)j2O.toObject(json);

        String expression = "subObject.fieldA";
        Assert.assertEquals(expression + " value", masterObject.subObject.fieldA, result.subObject.fieldA );

    }

    @Test
    @TestDescription(description="unknown Function")
    public void testUnknownFunction()
    {
        String json = "{ propa:nonexistantF() }";
        Json2Object j2O = new Json2Object();
        Object result;
        Exception ex=null;
        try { result= j2O.toObject(json, Object.class); }
        catch (Exception exx)
        {
            ex=exx;
        }
        Assert.assertEquals("expected UnknownFunctionException", Json2Object.UnknownFunctionException.class,
                ex==null?null:ex.getClass() );
    }

    @Test
    @TestDescription(description="try array Object")
    public void testToJsonArray()
    {
        MasterObject masterObject = new MasterObject();
        masterObject.theTopArray = new String[] {"a", "b", "b"};
        masterObject.theStringArray = new String[] { "iii", "iv", "v", "vi"};

        MasterObject result;
        Json2Object j2O = new Json2Object();
        //j2O.setToUseFields();
        Class type = MasterObject.class;
        Object2Json o2J = new Object2Json();
        o2J.setNodeExpander(new FieldReflectionNodeExpander());
        //todo json 2 Object should understand TypeAliaser
        o2J.setTypeAliaser( new TypeAliaser()
        {
            @Override
            public String alias(Class type)
            {
                return type.getName();
            }
        });

        o2J.setTypeAliasProperty( j2O.getTypeSpecifier());
        String json = o2J.toJson(masterObject);
        System.out.println("testToJsonLeafTypesViaFields json:" + json);
        result = (MasterObject)j2O.toObject(json);

        String expression = "theTopArray[0]";
        Assert.assertEquals(" Length", masterObject.theTopArray.length, result.theTopArray.length );
        Assert.assertEquals(expression + " value", masterObject.theTopArray[0], result.theTopArray[0] );
        Assert.assertEquals(" Length", masterObject.theStringArray.length, result.theStringArray.length);
        Assert.assertEquals(expression + " value", masterObject.theStringArray[3], result.theStringArray[3]);

    }

    @Test
    @TestDescription(description = "try different field types")
    public void testToJsonLeafTypesViaFields()
    {
        AllPrimitiveLeafTypes template = new AllPrimitiveLeafTypes();

        String []expressions = AllPrimitiveLeafTypes.testFieldExpressions;
        Object[] expectedValues = AllPrimitiveLeafTypes.testExpectedFieldValues(template);

        AllPrimitiveLeafTypes result;
        Json2Object j2O = new Json2Object();
        //j2O.setToUseFields();
        Class type = AllPrimitiveLeafTypes.class;
        Object2Json o2J = new Object2Json();
        o2J.setNodeExpander(new FieldReflectionNodeExpander());
        //todo json 2 Object should understand TypeAliaser
        o2J.setTypeAliaser (
                new TypeAliaser()
                {
                    @Override
                    public String alias(Class type)
                    {
                        return type.getName();
                    }
                }
        );

        o2J.setTypeAliasProperty(j2O.getTypeSpecifier());
        String json = o2J.toJson(template);
        System.out.println("testToJsonLeafTypesViaFields json:" + json);
        result = (AllPrimitiveLeafTypes)j2O.toObject(json);

        for (int done = 0; done < expressions.length; done++)
        {
            String expression = expressions[done];
            Object value = ReflectionUtil.getFieldValueReturnExceptions(result, expression);
            Assert.assertEquals(expression + " value", expectedValues[done], value );
        }

    }


    public static class BooleanContainer
    {
        public boolean yesOrNo;
    }

    @Test
    @TestDescription(description = "test mapping from string to boolean")
    //todo add this test to csharp
    public void testBooleanPrimitiveFromString()
    {
        Json2Object j20 = new Json2Object();
        BooleanContainer bc;
        bc = (BooleanContainer) j20.toObject("{yesOrNo:'true'}", BooleanContainer.class);
        Assert.assertTrue(bc.yesOrNo);
        bc = (BooleanContainer) j20.toObject("{yesOrNo:'false'}", BooleanContainer.class);
        Assert.assertFalse(bc.yesOrNo);
        bc = (BooleanContainer) j20.toObject("{yesOrNo:'0'}", BooleanContainer.class);
        Assert.assertFalse(bc.yesOrNo);
        bc = (BooleanContainer) j20.toObject("{yesOrNo:'1'}", BooleanContainer.class);
        Assert.assertTrue(bc.yesOrNo);

    }

    public static class DateContainer
    {
        public Date date;
    }

    @Test
    @TestDescription(description = "test mapping from string to boolean")
    //todo add this test to csharp
    public void testDateFromString()
    {
        Json2Object j2o = new Json2Object();
        BooleanContainer bc;
        long theTime = System.currentTimeMillis();
        String json = "{ date:{ "  + NodeExpanderConstants.UnixEpochTimeMillisPropertyName + ":" + theTime  +  " }  }";
        DateContainer dateContainer = (DateContainer) j2o.toObject(json, DateContainer.class);
        Assert.assertEquals(dateContainer.date.getTime(), theTime);

    }


    @Test
    @TestDescription(description = "try hierarchical structure - relates to com.houseelectrics.serializer.test.TestToJson.testToJsonHierarchy")
    public void testFromJsonHierarchy()
    {

        TestTreeNode rootNode = new TestTreeNode();
        rootNode.setName("top");
        rootNode.branches = new ArrayList<TestTreeNode>();
        TestTreeNode child = new TestTreeNode();
        child.setName("child");
        child.branches = null;
        rootNode.branches.add(child);
        NodeExpander nodeExpander = new FieldReflectionNodeExpander();
        Object2Json o2J = new Object2Json();
        o2J.setNodeExpander(nodeExpander);
        o2J.setIndentSize( 2);
        o2J.setTypeAliaser(
          new TypeAliaser()
          {
              @Override
              public String alias(Class type)
              {
                  return type.getName();
              }
          }
        );
        Json2Object j2O = new Json2Object();
        o2J.setTypeAliasProperty( j2O.getTypeSpecifier());
        String json = o2J.toJson(rootNode);
        System.out.println("testToJsonLeafTypesViaFields json:" + json);
        TestTreeNode result = (TestTreeNode) j2O.toObject(json, TestTreeNode.class);
        Assert.assertEquals ("check nested values" , rootNode.getBranches().get(0).getName(), result.getBranches().get(0).getName());

    }



    JsonMapping mapping = new DefaultJsonMapping();

    public static class GenericListContainer
    {
        public GenericListContainer(String id) { this.id = id; }
        public GenericListContainer()  {  this("0"); }
        public String id;
        public List<Object> objectListPropA;
        public List<Integer> intListPropB;
        public String toString() { return id; }
        public Map<String, String> stringToStringMapPropC;
        public Map<Integer, String> intToStringMapPropC;
    }

    @Test
    @TestDescription(description = "test Map objects")
    //todo add this to csharp tests
    public void testMapObjects()
    {
        GenericListContainer input = new GenericListContainer("");

        input.stringToStringMapPropC = new HashMap<String, String>();
        input.stringToStringMapPropC.put("0", "zero");
        input.stringToStringMapPropC.put("1","one");
        input.stringToStringMapPropC.put("2","two");
        input.stringToStringMapPropC.put("3","three");

        input.intToStringMapPropC = new HashMap<Integer, String>();
        input.intToStringMapPropC.put(0,"zero");
        input.intToStringMapPropC.put(1, "one");
        input.intToStringMapPropC.put(2, "two");
        input.intToStringMapPropC.put(3, "three");

        DefaultJsonMapping localMapping = new DefaultJsonMapping();
        //localMapping.getJson2Object().setTypeSpecifier(null);
        String json = mapping.getObject2Json().toJson(input);
        GenericListContainer output = (GenericListContainer)mapping.getJson2Object().toObject(json, GenericListContainer.class);


        for (String strKey : input.stringToStringMapPropC.keySet())
        {
            Object expectedValue = input.stringToStringMapPropC.get(strKey);
            Object actualValue = output.stringToStringMapPropC.get(strKey);
            Assert.assertEquals("checking key " + strKey, expectedValue, actualValue);
        }

        for (Integer iKey : input.intToStringMapPropC.keySet())
        {
            Object expectedValue = input.intToStringMapPropC.get(iKey);
            Object actualValue = output.intToStringMapPropC.get(iKey);
            Assert.assertEquals("checking key " + iKey, expectedValue, actualValue);
        }
    }


    @Test
    @TestDescription(description="test Generic List Object fields")
    public void testGenericListObjectFields()
    {
        GenericListContainer input = new GenericListContainer("");

        input.objectListPropA = new ArrayList<Object>();
        input.objectListPropA.add(new GenericListContainer("0"));
        input.objectListPropA.add(new GenericListContainer("b"));
        input.objectListPropA.add(new GenericListContainer("iii"));
        String json;
        GenericListContainer output;
        Object2Json o2j = mapping.getObject2Json();
        Json2Object j2o = mapping.getJson2Object();

        json = o2j.toJson(input);
        output = (GenericListContainer)j2o.toObject(json, GenericListContainer.class);
        for (int done=0; done<output.objectListPropA.size(); done++)
        {
            Object actualValue = output.objectListPropA.get(done).toString();
            Object expectedValue = input.objectListPropA.get(done).toString();
            System.out.println("checking index " + done + " expectedValue: " + expectedValue + " actualValue:" + actualValue);
            Assert.assertEquals ("checking index " + done, expectedValue, actualValue );
        }
    }



    public static class TypeInferenceData
    {
        List<SimpleTestObject> details;
        public List<SimpleTestObject> getDetails() {return details;}
        public void setDetails(List<SimpleTestObject> value) {this.details = value;}

        private InferenceDetail detail;
        public InferenceDetail getDetail() {return detail;}
        public void setDetail(InferenceDetail detail) {this.detail=detail;}

        public static class InferenceDetail
        {
            private String name;
            public String getName() {return name;}
            public void setName(String value) {this.name=value;}

        }
    }


    //todo add this to csharp
    @Test
    @TestDescription(description = "test type inference")
    public void testTypeInferenceArray()
    {
        String json;
        Object2Json o2j = mapping.getObject2Json();
        o2j.setNodeExpander(new PropertyReflectionNodeExpander());
        Json2Object j2o = mapping.getJson2Object();
        //remove type information
        o2j.setTypeAliaser(null);
        TypeInferenceData input = new TypeInferenceData();
        input.setDetails(new ArrayList<SimpleTestObject>());
        SimpleTestObject detail = new SimpleTestObject();
        input.getDetails().add(detail);
        detail.setIntPropertyD(123);
        json = o2j.toJson(input);

        TypeInferenceData output = (TypeInferenceData)j2o.toObject(json, TypeInferenceData.class);
        for (int done=0; done<output.getDetails().size(); done++)
        {
            Object actualValue = output.getDetails().get(done).getIntPropertyD();
            Object expectedValue = input.getDetails().get(done).getIntPropertyD();
            System.out.println("checking index " + done + " expectedValue: " + expectedValue + " actualValue:" + actualValue);
            Assert.assertEquals ("checking index " + done, expectedValue, actualValue );
        }
    }

    //todo add this to csharp
    @Test
    @TestDescription(description = "test type inference")
    public void testTypeInference()
    {
        String json;
        Json2Object j2o = mapping.getJson2Object();
        json = "{ Detail:{ Name:'Bilbo Baggins' }  }";

        TypeInferenceData output = (TypeInferenceData)j2o.toObject(json, TypeInferenceData.class);
        Assert.assertEquals("expected detail property to be inferred" , "Bilbo Baggins", output.getDetail().getName());

    }


    @Test
    @TestDescription(description = "test Generic List primitive fields")
    public void testGenericListPrimitiveFields()
    {
        GenericListContainer input = new GenericListContainer("");

        input.intListPropB = new ArrayList<Integer>();
        input.intListPropB.add(3);
        input.intListPropB.add(4);
        input.intListPropB.add(5);
        String json = mapping.getObject2Json().toJson(input);

        GenericListContainer output = (GenericListContainer)mapping.getJson2Object().toObject(json, GenericListContainer.class);

        for (int done = 0; done < output.intListPropB.size(); done++)
        {
            Object actualValue = output.intListPropB.get(done);
            Object expectedValue = input.intListPropB.get(done);
            Assert.assertEquals ("checking index " + done, expectedValue, actualValue);
        }
    }


    //@Test
    //@TestDescription(description = "test mixed list")
    public void testMixedListField()
    {
        boolean isAssignable = Object.class.isAssignableFrom(String.class);
        isAssignable = String.class.isAssignableFrom(Object.class);

        GenericListContainer input = new GenericListContainer("");

        input.objectListPropA = new ArrayList<Object>();
        //input.objectListPropA.Add('3');
        //input.objectListPropA.Add(4);
        input.objectListPropA.add("5");
        final Object2Json o2j = mapping.getObject2Json();

        o2j.setLeafWriter(
                new Object2Json.LeafWriter()
                {
                    @Override
                    public void writeLeafValue(Writer writer, Object to, String propertyName) throws IOException
                    {
                        if (to!=null)
                        {
                            writer.write("/*");
                            writer.write(to.getClass().getName());
                            writer.write("*" + "/");
                        }
                        o2j.getDefaultLeafWriter().writeLeafValue(writer, to, propertyName);
                    }
                }
        );

        String json = o2j.toJson(input);

        GenericListContainer output = (GenericListContainer)mapping.getJson2Object().toObject(json, GenericListContainer.class);

        for (int done = 0; done < output.objectListPropA.size(); done++)
        {
            Object expectedValue = input.objectListPropA.get(done);
            Object actualValue = output.objectListPropA.get(done);
            Assert.assertEquals("checking index " + done, expectedValue, actualValue);
        }
    }


    @Test
    @TestDescription(description = "try hierarchical structure - relates to com.houseelectrics.serializer.test.TestToJson.testToJsonHierarchy")
    public void testFromJsonHierarchyDeep()
    {
        TestTreeNode rootNode = TestTreeNode.createTestHierarchy(4, 3);
        NodeExpander nodeExpander = new FieldReflectionNodeExpander();
        Object2Json o2J = new Object2Json();
        o2J.setNodeExpander( nodeExpander);
        o2J.setIndentSize(2);
        o2J.setTypeAliaser(
                new TypeAliaser()
                {
                    @Override
                    public String alias(Class type)
                    {
                        return type.getName();
                    }
                }
        );

        Json2Object j2O = new Json2Object();
        o2J.setTypeAliasProperty(j2O.getTypeSpecifier());
        String json = o2J.toJson(rootNode);
        System.out.println("testToJsonLeafTypesViaFields json:" + json);
        TestTreeNode result = (TestTreeNode)j2O.toObject(json, TestTreeNode.class);
        Object expectedValue = rootNode.getBranches().get(2).getBranches().get(2).getBranches().get(2).getName();
        Object actualValue = result.getBranches().get(2).getBranches().get(2).getBranches().get(2).getName();

        Assert.assertEquals("check nested values", expectedValue, actualValue);

    }


    @Test
    @TestDescription(description = "try notifications")
    public void testNotifications()
    {
        SimpleTestObject sto = new SimpleTestObject();
        sto.stringFieldA = "AA";
        String json = "{ stringFieldA:AA }";

        Json2Object.DeserializationListener theMock =  createMock(Json2Object.DeserializationListener.class);
        theMock.onCreateObject(EasyMock.isA(SimpleTestObject.class));
        theMock.onSetValue(EasyMock.isA(SimpleTestObject.class),  EasyMock.eq("stringFieldA"), EasyMock.eq("AA"));
        theMock.onEndObject(EasyMock.isA(SimpleTestObject.class));
        replayAll();
        Json2Object j2o = new Json2Object();
        j2o.Add(theMock);
        j2o.toObject(json, SimpleTestObject.class);
        verifyAll();

    }


}
