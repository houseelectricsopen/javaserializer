package com.houseelectrics.serializer.test;
import org.junit.*;
import com.houseelectrics.serializer.JsonExploreListener;
import com.houseelectrics.serializer.JSONExplorerImpl;
import java.util.*;
import org.easymock.*;

/**
 * Created by roberttodd on 29/11/2014.
 */
public class JsonExplorationTest extends EasyMockSupport
{
    public class JsonExploreEvent
    {

    }

    public class LeafEvent extends JsonExploreEvent
    {
        public String propertyName;
        public String value;
        public boolean isQuoted;
        public LeafEvent(String propertyName, String value, boolean isQuoted) { this.propertyName = propertyName; this.value = value; this.isQuoted = isQuoted; }
    }

    @Test
    public void testEscapedDoubleQuoteJsonValue()
    {
        LeafEvent[] argsjsonexp;
        argsjsonexp = new LeafEvent[] { new LeafEvent("a", "\"1 \"2", true) };
        testJsonExplorationParameterised("{ a:\"\\\"1 \\\"2\" }", 1, 1, 1, argsjsonexp);
    }

    @Test
    public void testDoubleQuoteJsonValue()
    {
        LeafEvent[] argsjsonexp;
        argsjsonexp = new LeafEvent[] { new LeafEvent("a", "2 3", true) };
        testJsonExplorationParameterised("{ a:\"2 3\" }", 1, 1, 1, argsjsonexp);
    }

    @Test
    public void testSingleQuoteJsonValue()
    {
        LeafEvent[] argsjsonexp;
        argsjsonexp = new LeafEvent[] { new LeafEvent("a", "2 3", true) };
        testJsonExplorationParameterised("{ a:'2 3' }", 1, 1, 1, argsjsonexp);
    }

    @Test
    public void testBasicJsonExplore()
    {
        testJsonExplorationParameterised("{}", 1, 1, 0);
    }

    @Test
    public void testJsonExploreBasicLeaf()
    {
        testJsonExplorationParameterised("{a:2}", 1, 1, 1);
    }

    @Test
    public void testBasicJsonPropertyExplore()
    {
        LeafEvent[] argsjsonexp;
        argsjsonexp  = new LeafEvent[] { new LeafEvent("a", "2", false), new LeafEvent("b", "3", false) };
        testJsonExplorationParameterised("{a:2, b:3}", 1, 1, 2, argsjsonexp);
    }

    @Test
    public void testJsonExploreNestedObjects()
    {
        LeafEvent[] argsjsonexp;
        argsjsonexp = new LeafEvent[] { new LeafEvent("a", "2", false), new LeafEvent("d", "5", false) };
        testJsonExplorationParameterised("{a:2, b:{ c: { d:5} } }", 3, 3, 2, argsjsonexp);
    }

    @Test
    public void testEmptyObject()
    {
        String json = "{ }";
        JsonExploreListener theMock =  createMock(JsonExploreListener.class);
            theMock.JsonStartObject(null, 0);
            theMock.JsonEndObject(2);
        replayAll();

        JSONExplorerImpl jsonExplorerImpl = new JSONExplorerImpl();
        jsonExplorerImpl.explore(json, theMock);
        verifyAll();
    }

   @Test
    public void testArrayWithfunction()
    {
        String classname = "TestClassName";
        String json = "{@class:'" + classname+ "'," + "\r\n" +
        "id:'123'," + "\r\n" +
        "        objectListPropA:null," + "\r\n" +
        "    intListPropB:    _a_([3, 4, 5])  }";
        JsonExploreListener theMock =  createMock(JsonExploreListener.class);
    //RhinoMocks.Logger = new TextWriterExpectationLogger(Console.Out);
    System.out.println("***testArrayWithfunction json: " + json);

        theMock.JsonStartObject(null, 0);
        theMock.JsonLeaf("@class", classname, true);
        theMock.JsonLeaf("id", "123", true);
        theMock.JsonLeaf("objectListPropA", null, false);
        theMock.JsonStartFunction("_a_", json.indexOf("("), "intListPropB" );
        theMock.JsonStartArray(null, json.indexOf("["));
        theMock.JsonLeaf(null, "3", false);
        theMock.JsonLeaf(null, "4", false);
        theMock.JsonLeaf(null, "5", false);
        theMock.JsonEndArray(json.indexOf("]"));
        theMock.JsonEndFunction(json.indexOf(")"));
        theMock.JsonEndObject(json.indexOf("}"));

    replayAll();

    JSONExplorerImpl jsonExplorerImpl = new JSONExplorerImpl();
    jsonExplorerImpl.explore(json, theMock);

     verifyAll();
}


@Test
public void testArray()
        {
        String json = "{ propB:[1,2,3, \"abc\"]}";
            JsonExploreListener theMock =  createMock(JsonExploreListener.class);
//            RhinoMocks.Logger = new TextWriterExpectationLogger(Console.Out);
        System.out.println("***testArray json: " + json);
        theMock.JsonStartObject(null, 0);
        theMock.JsonStartArray("propB", json.indexOf("["));
        theMock.JsonLeaf(null, "1", false);
        theMock.JsonLeaf(null, "2", false);
        theMock.JsonLeaf(null, "3", false);
        theMock.JsonLeaf(null, "abc", true);
        theMock.JsonEndArray(json.lastIndexOf("]"));
        theMock.JsonEndObject(json.length() - 1);
            replayAll();
        JSONExplorerImpl jsonExplorerImpl = new JSONExplorerImpl();
        jsonExplorerImpl.explore(json, theMock);
        verifyAll();
        }

        @Test
public void testEmbeddedFunction()
        {
        String functionName = "funcA";
        String propertyName = "propA";
        String json = "{ " + propertyName + ":" + functionName + "({ a:3, b:\"a b c\" }) }";

            JsonExploreListener theMock =  createMock(JsonExploreListener.class);

  //          RhinoMocks.Logger = new TextWriterExpectationLogger(Console.Out);
        System.out.println("***testEmbeddedFunction json: " + json);
        theMock.JsonStartObject(null, 0);
        theMock.JsonStartFunction(functionName, json.indexOf(functionName) + functionName.length(), propertyName);
        theMock.JsonStartObject(null, json.lastIndexOf("{"));
        theMock.JsonLeaf("a", "3", false);
        theMock.JsonLeaf("b", "a b c", true);
        theMock.JsonEndObject(json.indexOf("}"));
        theMock.JsonEndFunction(json.indexOf(")"));
        theMock.JsonEndObject(json.length() - 1);
        replayAll();
        JSONExplorerImpl jsonExplorerImpl = new JSONExplorerImpl();
        jsonExplorerImpl.explore(json, theMock);
        verifyAll();
        }


class ExplorationTestListener implements JsonExploreListener
        {
public ArrayList<LeafEvent> actualLeafEvents = new ArrayList<LeafEvent>();
public int startCount = 0, endCount = 0, leafCount = 0;
public void JsonStartObject(String propertyName, int pos)
        {
            System.out.println("down " +  propertyName + " " + pos);
        startCount++;
        }
public void JsonLeaf(String propertyName, String value, boolean isQuoted)
        {
        actualLeafEvents.add(new LeafEvent(propertyName, value, isQuoted));
            System.out.println("leaf " + propertyName+ "==" + value+ " isQuoted:" + isQuoted);
        leafCount++;
        }
public void JsonEndObject(int pos)
        {
            System.out.println("up  " + pos);
        endCount++;
        }
public void JsonStartFunction(String functionName, int pos, String propertyName)
        {

        }
public void JsonEndFunction(int pos)
        {

        }

public void JsonStartArray(String propertyName, int pos)
        {

        }
public void JsonEndArray(int pos)
        {

        }

        }

    public void testJsonExplorationParameterised(String json, int expectedStartCount, int expectedEndCount, int expectedLeafCount)
    {
        testJsonExplorationParameterised(json, expectedStartCount, expectedEndCount, expectedLeafCount, null);
    }

public void testJsonExplorationParameterised(String json, int expectedStartCount, int expectedEndCount, int expectedLeafCount, LeafEvent[] expectedLeafEvents)
        {
        System.out.println("json: " + json);
        JSONExplorerImpl jsonExplorerImpl = new JSONExplorerImpl();
        ExplorationTestListener etl = new ExplorationTestListener();
        jsonExplorerImpl.explore(json, etl);
        Assert.assertEquals("expected " + expectedStartCount + " object start", expectedStartCount, etl.startCount   );
        Assert.assertEquals("expected " + expectedEndCount + " object end", expectedEndCount, etl.endCount);
        Assert.assertEquals("expected " + expectedLeafCount + "leaves" , expectedLeafCount, etl.leafCount );
        if (expectedLeafEvents!=null)
        {
        for ( int done=0; done<expectedLeafEvents.length; done++)
        {
        Assert.assertEquals("leaf name " + done,  expectedLeafEvents[done].propertyName, etl.actualLeafEvents.get(done).propertyName );
        Assert.assertEquals("leaf value "+ done,  expectedLeafEvents[done].value, etl.actualLeafEvents.get(done).value );
        Assert.assertEquals("leaf isQuoted " + done,   expectedLeafEvents[done].isQuoted, etl.actualLeafEvents.get(done).isQuoted  );
        }
        }
        }


        }
