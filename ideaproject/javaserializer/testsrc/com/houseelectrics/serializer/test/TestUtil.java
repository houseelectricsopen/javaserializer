package com.houseelectrics.serializer.test;

import com.houseelectrics.serializer.JSONExplorerImpl;
import com.houseelectrics.serializer.JsonExploreListener;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;

/**
 * Created by roberttodd on 04/12/2014.
 */
public class TestUtil extends EasyMockSupport
{
    public interface JsonExpectationBlock
    {
       public void run(JsonExploreListener jsonListener, String json);
    }

    public void testJsonStructure(String json, JsonExpectationBlock expectation, String testname)
    {
        System.out.println("test: " + testname + "testing json=" + json);
        JsonExploreListener theMock = createMock(JsonExploreListener.class);
        expectation.run(theMock, json);
        //replayAll();

        (new JSONExplorerImpl()).explore(json, theMock);
        verifyAll();
    }


}
