package com.houseelectrics.serializer.test;
import com.houseelectrics.serializer.*;
import org.junit.*;

import java.util.*;
/**
 * Created by roberttodd on 01/12/2014.
 */
public class TestToJson
{
    JSExecuteUtil util = new JSExecuteUtil();
    public JSExecuteUtil getUtil() { return util; }

    public class IndexedTestContainer
    {
         public String[] strings;
         public List<Integer> ints;
    }

    @Test
    @TestDescription(description="properties demo for userguide")
    public void testDemoIndexedPropertiesForUserguide()
    {
        IndexedTestContainer testData = new IndexedTestContainer();
        testData.strings = new String[] { "eee", "ddd", "ccc", "bbb" };

        testData.ints = new ArrayList<Integer>(Arrays.asList(new Integer[] {6,5,4,3,2,1}) );
        Object2Json o2J = new Object2Json();
        o2J.setNodeExpander(new FieldReflectionNodeExpander());
        // make the format prettier
        o2J.setIndentSize(2);
        String json = o2J.toJson(testData);
        System.out.println("json=" + json);
        String[] expressions = { "strings[3]", "strings[0]", "ints[0]", "ints[5]"};
        Object[] expectedValues = {testData.strings[3], testData.strings[0], testData.ints.get(0), testData.ints.get(5) };
        validateJSON(json, expressions, expectedValues, "testDemoIndexedPropertiesForUserguide");
    }

    @Test
    @TestDescription(description = "properties demo for userguide")
    public void testIndexedWithNull()
    {
        IndexedTestContainer testData = new IndexedTestContainer();
        testData.strings = new String[] { null, "ddd", "ccc", null };
        Object2Json o2J = new Object2Json();
        o2J.setNodeExpander(new FieldReflectionNodeExpander());
        // make the format prettier
        o2J.setIndentSize(2);
        String json = o2J.toJson(testData);
        System.out.println("json=" + json);
        String[] expressions = { "strings[0]", "strings[1]", "strings[2]", "strings[3]" };
        Object[] expectedValues = { testData.strings[0], testData.strings[1], testData.strings[2], testData.strings[3] };
        validateJSON(json, expressions, expectedValues, "testIndexedWithNull");
    }


    public class HashedTestContainer
    {
        Map<String, Integer> _name2Number;
        public Map<String, Integer> getName2Number()  { return _name2Number; }
        public void setName2Number( Map<String, Integer> value) { _name2Number=value; }
        Map<Integer, String> _number2Name;
        public Map<Integer, String> getNumber2Name()  { return _number2Name; }
        public void setNumber2Name( Map<Integer, String> value) { _number2Name=value; }
    }


    @Test
    @TestDescription(description = "hashed properties demo for userguide")
    public void testDemoHashedPropertiesForUserguide()
    {
        HashedTestContainer testData = new HashedTestContainer();
        testData.setName2Number(new HashMap<String, Integer>());
        testData.getName2Number().put("one", 1);
        testData.getName2Number().put("two", 2);
        testData.getName2Number().put("three", 3);
        testData.getName2Number().put("four", 4);

        testData.setNumber2Name(new HashMap<Integer, String>());
        testData.getNumber2Name().put(1, "one");
        testData.getNumber2Name().put(2, "two");
        testData.getNumber2Name().put(3, "three");
        testData.getNumber2Name().put(4, "four");

        Object2Json o2J = new Object2Json();
        o2J.setNodeExpander(new PropertyReflectionNodeExpander());
        // make the format prettier
        o2J.setIndentSize(2);
        String json = o2J.toJson(testData);
        System.out.println("json=" + json);
        String[] expressions = { "Name2Number['one']"};
        Object[] expectedValues = { testData.getName2Number().get("one")};
        validateJSON(json, expressions, expectedValues, "testDemoHashedPropertiesForUserguide");
    }

    @Test
    @TestDescription(description="try different property types")
    public void testToJsonLeafTypesViaProperties()
    {
        // todo - this fails with FieldReflectionNodeExpander
        NodeExpander nodeExpander = new PropertyReflectionNodeExpander();
        AllPrimitiveLeafTypes testData = new AllPrimitiveLeafTypes();
        String[] expressions = AllPrimitiveLeafTypes.testPropertyExpressions;
        Object[] expectedValues = AllPrimitiveLeafTypes.testExpectedPropertyValues(testData);
        Object2Json o2J = new Object2Json();
        o2J.setNodeExpander(nodeExpander);
        o2J.setIndentSize(2);
        String json = o2J.toJson(testData);
        validateJSON(json, expressions, expectedValues, "testToJsonLeafTypesViaProperties");
    }


    @Test
    @TestDescription(description="try different field types")
    public void testToJsonLeafTypesViaFields()
    {
        // todo - this fails with FieldReflectionNodeExpander
        NodeExpander nodeExpander = new FieldReflectionNodeExpander();
        AllPrimitiveLeafTypes testData = new AllPrimitiveLeafTypes();
        String[] expressions = AllPrimitiveLeafTypes.testFieldExpressions;
        Object[] expectedValues = AllPrimitiveLeafTypes.testExpectedFieldValues(testData);
        Object2Json o2J = new Object2Json();
        o2J.setNodeExpander(nodeExpander);
        o2J.setIndentSize( 2);
        String json = o2J.toJson(testData);
        validateJSON(json, expressions, expectedValues, "testToJsonLeafTypesViaFields");
    }


    @Test
    @TestDescription(description="try fields")
    public void testToJsonFields()
    {
        NodeExpander nodeExpander = new FieldReflectionNodeExpander();
        TestData testData = new TestData();
        String[] expressions = { "sub.subSub.greeting", "sub.subSub.ageYears", "sub.subSub.heightMetres" };
        Object[] expectedValues = { testData.getTheSub().getTheSubSub().getGreeting(), testData.getTheSub().getTheSubSub().getAgeYears(),
                testData.getTheSub().getTheSubSub().getHeightMetres() };
        Object2Json o2J = new Object2Json();
        o2J.setNodeExpander(nodeExpander);
        o2J.setIndentSize(2);
        String json = o2J.toJson(testData);
        validateJSON(json, expressions, expectedValues, "testToJsonFields");
    }

    @Test
    @TestDescription(description = "try properties")
    public void testToJsonProperties()
    {
        NodeExpander nodeExpander = new PropertyReflectionNodeExpander();
        TestData testData = new TestData();
        String[] expressions = { "TheSub.TheSubSub.Greeting", "TheSub.TheSubSub.AgeYears", "TheSub.TheSubSub.HeightMetres" };
        Object[] expectedValues = { testData.getTheSub().getTheSubSub().getGreeting(), testData.getTheSub().getTheSubSub().getAgeYears(), testData.getTheSub().getTheSubSub().getHeightMetres() };
        Object2Json o2J = new Object2Json();
        o2J.setNodeExpander( nodeExpander);
        o2J.setIndentSize(2);
        String json = o2J.toJson(testData);
        validateJSON(json, expressions, expectedValues, "testToJsonProperties");
    }

    @Test
    @TestDescription(description = "test many to 1 fields")
    public void testToJsonMany2Ones()
    {
        NodeExpander nodeExpander = new FieldReflectionNodeExpander();
        TestData testData = new TestData();
        String[] expressions = { "sub.subSub.seasons[0]", "sub.subSub.seasons[1]", "sub.subSub.seasons.length" };
        Object[] expectedValues = { testData.getTheSub().getTheSubSub().getSeasons()[0], testData.getTheSub().getTheSubSub().getSeasons()[1],
                testData.getTheSub().getTheSubSub().getSeasons().length};
        Object2Json o2J = new Object2Json();
        o2J.setNodeExpander(nodeExpander);
        o2J.setIndentSize( 2);
        String json = o2J.toJson(testData);
        validateJSON(json, expressions, expectedValues, "testToJsonMany2Ones");
    }


    @Test
    @TestDescription(description="try indexed properties")
    public void testToJsonPropertiesIndexed()
    {
        NodeExpander nodeExpander = new PropertyReflectionNodeExpander();
        TestData testData = new TestData();
        String[] expressions = { "TheSub.TheSubSub.Seasons[1]", "TheSub.TheSubSub.GoodYears[1]" };
        Object[] expectedValues = {  testData.getTheSub().getTheSubSub().getSeasons()[1], testData.getTheSub().getTheSubSub().getGoodYears().get(1) };
        Object2Json o2J = new Object2Json();
        //o2J.OmitMarkAsArrayFunction = true;
        o2J.setNodeExpander(nodeExpander);
        o2J.setIndentSize(2);
        String json = o2J.toJson(testData);
        validateJSON(json, expressions, expectedValues, "testToJsonPropertiesIndexed");
    }

    protected void validateJSON(String json, String[] expressions, Object[] expectedValues, String testid, String significance)
    {
        validateJSON(json, null, expressions, expectedValues ,  testid, significance);
    }

    protected void validateJSON(String json, String[] expressions, Object[] expectedValues, String testid)
    {
        validateJSON(json, null, expressions, expectedValues ,  testid, null/*significance*/);
    }

    protected void validateJSON(String json, String []functions, String[] expressions, Object[] expectedValues, String testid
            )
    {
        validateJSON( json,  functions, expressions, expectedValues, testid, null);
    }
    protected void validateJSON(String json, String []functions, String[] expressions, Object[] expectedValues, String testid
            , String significance)
    {

        JSExecuteUtil.JsonValueSet jvset = new JSExecuteUtil.JsonValueSet();
        jvset.varname = "testdata";
        for (int expdone = 0; expdone < expressions.length; expdone++)
        {
            String expression = expressions[expdone];
            jvset.expressions2ExpectedValue.put(expression, expectedValues[expdone]);
        }

        if (functions!=null)
        {
            jvset.extraFunctions = Arrays.asList(functions);
        }

        jvset.json = json;
        jvset.significance = significance;
        Map<String, Object> results = util.extractValuesFromJson(jvset, getClass().getName() + "." +testid);
        for (int done = 0; done < expressions.length; done++ )
        {
            Object expectedValue = expectedValues[done];
            String expression = expressions[done];
            Object actualValue = results.get(expression);

            expectedValue = convertExpectedValueForComparison(actualValue, expectedValue);

            Assert.assertEquals("json=" + json + " expression=" + expression, expectedValue, actualValue);
        }
    }

    Object convertExpectedValueForComparison(Object actualValue, Object expectedValue)
    {
        if (expectedValue !=null && (expectedValue instanceof Character /*|| expectedValue instanceof char)*/))
        {
            expectedValue = expectedValue.toString();
        }
        if (expectedValue!=null && expectedValue instanceof Number && actualValue!=null && actualValue instanceof Double)
        {
            expectedValue = ((Number) expectedValue).doubleValue();
        }
        return expectedValue;
    }


    @Test
    @TestDescription(description="try hierarchical structure")
    public void testToJsonHierarchy()
    {
        int requiredDepth =  4;
        int branchesPerParent = 2;
        TestTreeNode rootNode = TestTreeNode.createTestHierarchy(requiredDepth, branchesPerParent);
        String[] expressions = { "branches[0].branches[0].branches[0].name" };
        Object[] expectedValues = { rootNode.getBranches().get(0).getBranches().get(0).getBranches().get(0).getName() };
        NodeExpander nodeExpander = new FieldReflectionNodeExpander();
        Object2Json o2J = new Object2Json();
        o2J.setNodeExpander(nodeExpander);
        o2J.setIndentSize(2);
        String json = o2J.toJson(rootNode);
        validateJSON(json, expressions, expectedValues, "testToJsonHierarchy");
    }


    public static class ObjectWithRefs
    {
        public PotentiallyHugeSubObject subo1;
        public PotentiallyHugeSubObject subo2;
        public PotentiallyHugeSubObject subo3;
    };
    public static class PotentiallyHugeSubObject
    {
        public String stringVal;
        public int intVal;
    };

    @Test
    @TestDescription(description = "try references for Guide - repeated objects are included by reference")
    public void testUseReferencesSimple()
    {
        ObjectWithRefs root = new ObjectWithRefs();
        root.subo1 = new PotentiallyHugeSubObject();
        root.subo1.stringVal="123456";
        root.subo1.intVal =4321;
        root.subo2=root.subo1;
        root.subo3=root.subo2;
        Object2Json o2j = new Object2Json();
        o2j.setNodeExpander(new FieldReflectionNodeExpander());
        o2j.setUseReferences(true);
        String json = o2j.toJson(root);

        o2j.setUseReferences(false);
        String jsonWithoutReferenceSupport = o2j.toJson(root);
        System.out.println("testUseReferences1 without references: " + jsonWithoutReferenceSupport);

        //Object value;
        JSExecuteUtil.JsonValueSet valueSet = new JSExecuteUtil.JsonValueSet();
        valueSet.json = o2j.getObjectResolverFunctionName() + "(" + json + ")";
        valueSet.varname = "root";
        valueSet.expressions2ExpectedValue.put("subo3.intVal",root.subo3.intVal);
        valueSet.expressions2ExpectedValue.put("subo3.stringVal",root.subo3.stringVal);
        valueSet.extraFunctions.add(o2j.getObjectResolverJS());
        Map<String, Object> values = util.extractValuesFromJson(valueSet, getClass().getName() + "." + "testUseReferencesSimple");
        for (String expression : valueSet.expressions2ExpectedValue.keySet())
              {
              Object expectedValue = valueSet.expressions2ExpectedValue.get(expression);
              Object actualValue = values.get(expression);

              expectedValue = convertExpectedValueForComparison(actualValue, expectedValue);

              Assert.assertEquals("expression=" + expression + " with reference json= " + json, expectedValue, actualValue);
              }
    }



    @Test
    @TestDescription(description="try references - repeated objects are included by reference")
    public void testUseReferences()
    {
        TestTreeNode root = new TestTreeNode();
        root.setName("root");
        TestTreeNode child = TestTreeNode.createTestHierarchy(3, 3);
        Object2Json o2j = new Object2Json();
        o2j.setNodeExpander(new PropertyReflectionNodeExpander());
        root.getBranches().add(child);
        root.getBranches().add(child);
        root.getBranches().add(child);
        o2j.setIndentSize( 2);
        o2j.setUseReferences(false);
        String noRefJson = o2j.toJson(root);
        o2j.setUseReferences(true);

        String withRefJson = o2j.toJson(root);
        double minimumCompression = 2;
        double compression=((double)noRefJson.length())/((double)withRefJson.length());
        System.out.println("withRefJson=" + withRefJson);

        Assert.assertEquals( "noRefJson length= " + noRefJson.length() +
                " withRefJson length=" + withRefJson.length() +
                " compression ratio = " + compression + " minumum ratio=" + minimumCompression, (compression>minimumCompression), true)
               ;

        Object expectedValue = root.getBranches().get(2).getBranches().get(1).getName();
        String expression = "Branches[2].Branches[1].Name";
        //Object value;
        JSExecuteUtil.JsonValueSet valueSet = new JSExecuteUtil.JsonValueSet();
        valueSet.json = o2j.getObjectResolverFunctionName() + "(" + withRefJson + ")";
        valueSet.varname = "withRefRoot";
        valueSet.expressions2ExpectedValue.put(expression, expectedValue);
        valueSet.extraFunctions.add(o2j.getObjectResolverJS());
        valueSet.extraFunctions.add(o2j.getAttachId2ArrayJSFunction());
        valueSet.extraFunctions.add(o2j.getMarkAsArrayJSFunction());
        Map<String, Object> values = util.extractValuesFromJson(valueSet, getClass().getName() + "." + "testUseReferences");
        Assert.assertEquals("expression=" + expression + " with reference json= " + withRefJson, expectedValue, values.get(expression));
    }

    @Test
    @TestDescription(description="try a simple map structure (c# dictionary)")
    public void testSimpleMap()
    {
        Map<String, String> map = new HashMap<String, String>();
        String[] expressions = { "abc", "xyz", "cba", "ooo" };
        String[] expectedValues = {"hello", "goodday", "goodnight", null };
        for (int done = 0; done < expressions.length; done++ )
        {
            map.put(expressions[done], expectedValues[done]);
        }
        NodeExpander nodeExpander = new FieldReflectionNodeExpander();
        Object2Json o2J = new Object2Json();
        o2J.setNodeExpander(nodeExpander);
        o2J.setIndentSize(2);
        String json = o2J.toJson(map);
        validateJSON(json, expressions, expectedValues, "testSimpleMap");

    }

    public static class TypedMapTestData
    {
        public Map<Integer, String> int2String = new HashMap<Integer, String>();
    }


    @Test
    @TestDescription(description="try a typed map structure (java Map)")
    //todo add this to csharp
    public void testTypedMap()
    {
        TypedMapTestData typedMapTestData = new TypedMapTestData();
        typedMapTestData.int2String = new HashMap<Integer, String>();
        Integer[] keys = { 1, 2, 3, 5 };
        String []expressions = new String[keys.length];
        String[] expectedValues = {"uno", "zwei", "trois", "vier" };
        for (int done = 0; done < expressions.length; done++ )
        {
            expressions[done] = "int2String[" + keys[done] + "]";
            typedMapTestData.int2String.put(keys[done], expectedValues[done]);
        }
        NodeExpander nodeExpander = new FieldReflectionNodeExpander();
        Object2Json o2J = new Object2Json();
        o2J.setNodeExpander(nodeExpander);
        o2J.setIndentSize(2);
        String json = o2J.toJson(typedMapTestData);
        validateJSON(json, expressions, expectedValues, "testTypedMap");

    }


    public static class ManyDefaultsTestData
    {
        public int i1 = 123456789;
        public int i2 = 23456789;
        public int i4 = 432102345;
        public class StringValues
        {
            public String str1="abcdefghijk";
            public String str2="bcdefghijklmno";
            public String str3=null;
            public String aReallyReallyReallyLongName="John Malcom Eldritch Ebeneezer Ozwald Smith";
        }
        public StringValues stringValues = new StringValues();
        public StringValues[] stringValuesArr = { new StringValues(), new StringValues(), new StringValues() };
    }

    @Test
    @TestDescription(description="check writing of defaults works")
    public void testDefaultingWritingDefaultsBit()
    {
        //todo autoreject invalid js aliases e.g. numberot dot contained
        Object2Json o2j = new Object2Json();
        DefaultFinder df = new DefaultFinder();
        ManyDefaultsTestData md = new ManyDefaultsTestData();
        TypeAliaser aliaser = new TypeAliaser()
        {
            @Override
            public String alias(Class type)
            {
                return type.getName().replaceAll("\\.", "_").replaceAll("$", "_");
            }
        };


        // need to pass in class name aliasing scheme
        // this will fail if aliases are not valid js propert names
        Map<String, Defaults4Class> defaultsJS;
        String[] expressions;
        Object[] expectedValues;
        NodeExpander nodeExpander = new FieldReflectionNodeExpander();
        LeafDefaultSet lds = df.getDefaultsForAllLinkedObjects(md, new FieldReflectionNodeExpander());
        defaultsJS = lds.getAlias2Defaults(aliaser);
        expressions = new String[] { aliaser.alias(md.getClass()) + ".propertyName2DefaultValue"  +".i1",
                aliaser.alias(ManyDefaultsTestData.StringValues.class) + ".propertyName2DefaultValue"  + ".str3"};
        expectedValues= new Object[] { md.i1, md.stringValues.str3 };
        //check that the defaults serialise OK first
        Object2Json o2J = new Object2Json();
        o2J.setNodeExpander(nodeExpander);
        o2J.setIndentSize(2);
        String json = o2J.toJson(defaultsJS);
        validateJSON(json, expressions, expectedValues, "testDefaultingWritingDefaultsBit.simplealias","using a verbose class aliaser");
        // try a numeric aliaser
        aliaser =  TypeAliaserUtils.createNumericTypeNameAliaser();
        lds = df.getDefaultsForAllLinkedObjects(md, new FieldReflectionNodeExpander());
        defaultsJS = lds.getAlias2Defaults(aliaser);
        expressions = new String[] { aliaser.alias(md.getClass()) + ".propertyName2DefaultValue"  +".i1",
                aliaser.alias(ManyDefaultsTestData.StringValues.class) + ".propertyName2DefaultValue"  + ".str3"};
        o2J.setIndentSize(2);
        json = o2J.toJson(defaultsJS);
        validateJSON(json, expressions, expectedValues, "testDefaultingWritingDefaultsBit.numericliaser", "using a compact numeric aliaser");

    }




    //check that defaults can be written out ok.
    @Test
    @TestDescription(description="test use of defaults")
    public void testDefaulting()
    {
        //todo autoreject invalid js aliases e.g. numberot dot contained
        //Object2Json o2j = new Object2Json();
        DefaultFinder df = new DefaultFinder();
        ManyDefaultsTestData md = new ManyDefaultsTestData();
        TypeAliaser aliaser = new TypeAliaser()
        {
            @Override
            public String alias(Class type)
            {
                return type.getName().replace('.', '_').replace('$', '_');
            }
        };

        // need to pass in class name aliasing scheme
        // this will fail if aliases are not valid js propert names
        Map<String, Defaults4Class> defaultsJS;
        String[] expressions;
        Object[] expectedValues;
        NodeExpander nodeExpander = new FieldReflectionNodeExpander();
        LeafDefaultSet lds = df.getDefaultsForAllLinkedObjects(md, new FieldReflectionNodeExpander());
        defaultsJS = lds.getAlias2Defaults(aliaser);
        expressions = new String[] { aliaser.alias(md.getClass()) + ".propertyName2DefaultValue"  +".i1",
                aliaser.alias(ManyDefaultsTestData.StringValues.class) + ".propertyName2DefaultValue"  + ".str3"};
        expectedValues = new Object[] { md.i1, md.stringValues.str3 };
        //check that the defaults serialise OK first
        Object2Json o2J = new Object2Json();
        // should test with this as false
        o2J.setUseReferences(true);
        o2J.setNodeExpander(nodeExpander);
        o2J.setIndentSize( 2);
        String json = o2J.toJson(defaultsJS);
        validateJSON(json, expressions, expectedValues, "testDefaulting.defaultMap",  " check access to serialised defaults, class names are aliased replacing '.' with '_'");
        aliaser = TypeAliaserUtils.createNumericTypeNameAliaser();
        lds = df.getDefaultsForAllLinkedObjects(md, new FieldReflectionNodeExpander() );
        defaultsJS = lds.getAlias2Defaults(aliaser);
        expressions = new String[] { aliaser.alias(md.getClass()) + ".propertyName2DefaultValue"  +".i1",
                aliaser.alias (ManyDefaultsTestData.StringValues.class) + ".propertyName2DefaultValue"  + ".str3"};
        o2J.setIndentSize(2);
        json = o2J.toJson(defaultsJS);
        validateJSON(json, expressions, expectedValues, "testDefaulting", "check access to serialised defaults, classnames are aliases with numbers");

        // serialise with defaults
        o2J.setOmitDefaultLeafValuesInJs(false);
        String withDefaultLeafValuesJS = o2J.toJson(md);
        // serialise without defaults
        o2J.setOmitDefaultLeafValuesInJs(true);
        o2J.setLeafDefaultSet(lds);
        o2J.setOmitMarkAsArrayFunction(false);
        o2J.setTypeAliaser(aliaser);
        String withoutDefaultsLeafValuesJS = o2J.toJson(md);
        float minimumDefaultingCompression = 2.0F;
        // check sizes is much smaller with defaults
        int maximumCompressedLength= (int) (((float) withDefaultLeafValuesJS.length()) / minimumDefaultingCompression   );
        Object []vals = {withDefaultLeafValuesJS.length()};
        Assert.assertEquals("without default compression length=" + withDefaultLeafValuesJS.length() +
                " without default leaf values length=" + withoutDefaultsLeafValuesJS.length() + " but expected less than " +
                maximumCompressedLength + " (compression better than " +minimumDefaultingCompression +")", withoutDefaultsLeafValuesJS.length() < maximumCompressedLength, true);
        // check defaulted values are undefined if undefault is not called
        expressions = new String[] { "i2", "stringValues.str3", "stringValues.str2", "stringValues.str1", "i1", "i4", "stringValuesArr[0].str1" };
        expectedValues = new Object[expressions.length]; // assume initialised to null !!
        md.i2 = 6789;
        expectedValues[0] = md.i2;
        withoutDefaultsLeafValuesJS = o2J.toJson(md);
        String[] extraFunctions = { o2J.getAttachId2ArrayJSFunction() };
        validateJSON(withoutDefaultsLeafValuesJS, extraFunctions, expressions, expectedValues, "testDefaulting.unrestoredvalues", "serialise ommitting defaults and in js test do not restore defaults");

        expectedValues = new Object[] {md.i2, md.stringValues.str3, md.stringValues.str2, md.stringValues.str1, md.i1, md.i4, md.stringValuesArr[0].str1 };
        //Object value;
        JSExecuteUtil.JsonValueSet valueSet = new JSExecuteUtil.JsonValueSet();
        valueSet.json = o2J.getLeafDefaultResolverFunctionName() + "(" + withoutDefaultsLeafValuesJS + ")";
        valueSet.varname = "defaulted";
        for (int done = 0; done < expressions.length; done++)
        {
            valueSet.expressions2ExpectedValue.put(expressions[done], expectedValues[done]);
        }
        valueSet.extraFunctions.add(o2J.getLeafDefaultResolverJS(aliaser));
        valueSet.extraFunctions.add(o2J.getMarkAsArrayJSFunction());
        valueSet.extraFunctions.add(o2J.getAttachId2ArrayJSFunction());
        valueSet.significance = "serialised without default values and restore with standard restore js function";
        Map<String, Object> values = util.extractValuesFromJson(valueSet, getClass().getName() + "." + "testDefaulting.resolveLeaves");

        for (int done = 0; done < expressions.length; done++)
        {
            String expression = expressions[done];
            Object expectedValue = expectedValues[done];
            Object actualValue =  values.get(expression);
            expectedValue = convertExpectedValueForComparison(actualValue, expectedValue);
            valueSet.expressions2ExpectedValue.put(expression, actualValue);
            //"expression=" + expression + " with reference json= " + withoutDefaultsLeafValuesJS

            Assert.assertEquals("valueSet==" + valueSet.toString(), expectedValue, actualValue);
        }

    }

    @Test
    @TestDescription(description = "test defaulting")
    public void demoDefaultingForUserguide()
    {

        // create the test data
        ManyDefaultsTestData md = new ManyDefaultsTestData();

        // create an aliaser to create identifiers for types
        // must emit valid js property name distinct for each class
        // typically a number is used to reduce size
        TypeAliaser aliaser = TypeAliaserUtils.createNumericTypeNameAliaser();

        //focus on serializing fields
        NodeExpander nodeExpander = new FieldReflectionNodeExpander();
        // find the defaults - this is a one off
        // only necessary once per schemachange
        DefaultFinder df = new DefaultFinder();
        // this method just searches through the type structure
        LeafDefaultSet lds = df.getDefaultsForAllLinkedObjects(md, nodeExpander);
        // create a dictionary of default values - this is a one off per schemachange
        Map<String, Defaults4Class> defaultValues = lds.getAlias2Defaults(aliaser);

        Object2Json o2J = new Object2Json();
        o2J.setTypeAliaser(aliaser);
        o2J.setNodeExpander(nodeExpander);
        // make the format prettier
        o2J.setIndentSize(2);
        // create javascript representing the dictionary of defaults
        String defaultValuesJson = o2J.toJson(defaultValues);
        System.out.println("defaultValuesJson=" + defaultValuesJson);

        // serialise without defaults
        o2J.setOmitDefaultLeafValuesInJs(true);
        o2J.setLeafDefaultSet(lds);
        o2J.setOmitMarkAsArrayFunction(false);
        String withoutDefaultsLeafValuesJS = o2J.toJson(md);
        System.out.println("withoutDefaultsLeafValuesJS=" + withoutDefaultsLeafValuesJS);

        // serialise with defaults
        o2J.setOmitDefaultLeafValuesInJs(false);
        o2J.setLeafDefaultSet(lds);
        o2J.setTypeAliaser (aliaser);
        String withDefaultLeafValuesJS = o2J.toJson(md);

        System.out.println("withDefaultLeafValuesJS=" + withDefaultLeafValuesJS);

        JSExecuteUtil.JsonValueSet valueSet = new JSExecuteUtil.JsonValueSet();
        valueSet.json = o2J.getLeafDefaultResolverFunctionName() + "(" + withoutDefaultsLeafValuesJS + ")";
        valueSet.varname = "defaulted";
        valueSet.extraFunctions.add(o2J.getLeafDefaultResolverJS(aliaser));
        valueSet.extraFunctions.add(o2J.getMarkAsArrayJSFunction());
        valueSet.significance = "serialised without default values and restore with standard restore js function";
        valueSet.expressions2ExpectedValue.put("stringValuesArr[1].str2", md.stringValuesArr[1].str2);
        valueSet.expressions2ExpectedValue.put("stringValuesArr[2].aReallyReallyReallyLongName", md.stringValuesArr[2].aReallyReallyReallyLongName);
        Map<String, Object> values = util.extractValuesFromJson(valueSet, getClass().getName() + "." + "demoDefaultingForUserguide");
    }
}
