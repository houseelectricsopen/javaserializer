package com.houseelectrics.serializer.test;

/**
 * Created by roberttodd on 02/12/2014.
 */
//todo add this to csharp
public class CreateCompatibilityPage
{
public static void main(String args[]) throws Exception
{
    TestToJson toJsonTest = new TestToJson();
    toJsonTest.demoDefaultingForUserguide();
    toJsonTest.testIndexedWithNull();
    toJsonTest.testDemoIndexedPropertiesForUserguide();
    toJsonTest.testDemoHashedPropertiesForUserguide();
    toJsonTest.testToJsonFields();
    toJsonTest.testToJsonProperties();
    toJsonTest.testToJsonLeafTypesViaProperties();
    toJsonTest.testToJsonLeafTypesViaFields();
    toJsonTest.testToJsonPropertiesIndexed();
    toJsonTest.testSimpleMap();
    toJsonTest.testTypedMap();
    toJsonTest.testToJsonHierarchy();
    toJsonTest.testUseReferencesSimple();
    toJsonTest.testUseReferences();
    toJsonTest.testDefaulting();
    toJsonTest.testDefaultingWritingDefaultsBit();

    toJsonTest.getUtil().writeTestTablePage();
}
}

