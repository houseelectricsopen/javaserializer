package com.houseelectrics.serializer.test.security;

import com.houseelectrics.serializer.*;
import com.houseelectrics.serializer.security.PermissionFilterExplorer;
import com.houseelectrics.serializer.security.PermissionRequirement;
import com.houseelectrics.serializer.test.TestUtil;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by roberttodd on 04/12/2014.
 */
public class SecurityTest extends EasyMockSupport
{

    public static class LogonDetails
    {
        @PermissionRequirement(permissionName = "ViewPasswords")
        public String password;
        @PermissionRequirement(permissionName = "ViewPasswords")
        public String getPassword() {return password;}
        public void setPassword(String value) {this.password = value;}

        public String username;
        public String getUsername() {return username;}
        public void setUsername(String value) {this.username = value;}

    }

    public static class User
    {
        public String id;
        public String getId() {return id;}
        public void setId(String value) {this.id = value;}
        public String nickname;
        public String getNickname() {return nickname;}
        public void setNickname(String value) {this.nickname = value;}
        @PermissionRequirement( permissionName = "PasswordAdministration")
        public LogonDetails logonDetails;
        @PermissionRequirement(permissionName = "PasswordAdministration")
        public LogonDetails getLogonDetails() {return logonDetails;}
        public void setLogonDetails(LogonDetails value) { this.logonDetails=value; }
    }

    Object2Json createSecureObjectToJson(final String[] arrCurrentPermissions, boolean throwPermissionOnDenial, NodeExpander nodeExpander)
    {
        final HashMap<String, String> currentPermissions  = new HashMap<String, String>();
        currentPermissions.clear();
        for (String p : arrCurrentPermissions) {currentPermissions.put(p,p);}

        Object2Json o2j = new Object2Json();
        o2j.setNodeExpander(nodeExpander);
        final PermissionFilterExplorer pfe = new PermissionFilterExplorer();
        pfe.setThrowException( throwPermissionOnDenial );
        o2j.setExplorerFactory(
            new Object2Json.ExplorerFactory()
            {
                @Override
                public Explorer create()
                {
                    pfe.setUnderlyingExplorer(new ObjectExplorerImpl());
                    pfe.setCheckPermissionByName(
                            new PermissionFilterExplorer.CheckPermissionByName()
                            {
                                @Override
                                public boolean check(String name)
                                {
                                    return currentPermissions.containsKey(name);
                                }
                            }
                    );
                    return pfe;
                }
            }
        );

        return o2j;
    }

    public class LogonDetailsUserguideTestData
    {
        @PermissionRequirement(permissionName = "ViewPasswords")
        public String password;

        @PermissionRequirement(permissionName = "ViewUsernames")
        public String username;
    }


    @Test
    public void testFieldLeafDenialNoExceptionForUserGuide()
    {

        //create sensitive data
        LogonDetailsUserguideTestData sensitiveData = new LogonDetailsUserguideTestData();
        sensitiveData.password = "secret123";
        sensitiveData.username = "Andrew Eldritch";

        Object2Json o2j = new Object2Json();
        o2j.setNodeExpander( new FieldReflectionNodeExpander());

        // inject a simple security check
        final Set<String> currentPermissions=new HashSet<String>();
        PermissionFilterExplorer.CheckPermissionByName checkPermissionByName= new PermissionFilterExplorer.CheckPermissionByName()
        {
            public boolean check(String name) {return currentPermissions.contains(name);}
        };

        boolean throwExceptionOnPermissionDenial = false;
        PermissionFilterExplorer.injectPermissionFilter(o2j, checkPermissionByName, throwExceptionOnPermissionDenial);

        currentPermissions.clear();
        currentPermissions.add("ViewUsernames");
        String json = o2j.toJson(sensitiveData);
        System.out.println("json=" + json);
        Assert.assertTrue(json.indexOf(sensitiveData.username) > 0);
        Assert.assertTrue(json.indexOf(sensitiveData.password) < 0);

        currentPermissions.clear();
        currentPermissions.add("ViewUsernames");
        currentPermissions.add("ViewPasswords");
        //create simple permission check for test purposes
        json = o2j.toJson(sensitiveData);
        System.out.println("json=" + json);
        Assert.assertTrue(json.indexOf(sensitiveData.username) > 0);
        Assert.assertTrue(json.indexOf(sensitiveData.password) > 0);

    }

    @Test
    public void testFieldLeafDenialWithExceptionForUserGuide()
    {

        //create sensitive data
        LogonDetailsUserguideTestData sensitiveData = new LogonDetailsUserguideTestData();
        sensitiveData.password = "secret432";
        sensitiveData.username = "Tyrion Lannister";

        Object2Json o2j = new Object2Json();
        o2j.setNodeExpander( new FieldReflectionNodeExpander());

        // inject a simple security check
        final Set<String> currentPermissions=new HashSet<String>();
        PermissionFilterExplorer.CheckPermissionByName checkPermissionByName= new PermissionFilterExplorer.CheckPermissionByName()
        {
            public boolean check(String name) {return currentPermissions.contains(name);}
        };

        boolean throwExceptionOnPermissionDenial = true;
        PermissionFilterExplorer.injectPermissionFilter(o2j, checkPermissionByName, throwExceptionOnPermissionDenial);

        currentPermissions.clear();
        currentPermissions.add("ViewUsernames" );

        Exception exception=null;
        String json=null;
        try   {
                json = o2j.toJson(sensitiveData);
        }
        catch (Exception ex)
        {
            exception=ex;
        }

            /*expecting an exception like this:
 cannot access property com.houseelectrics.serializer.test.security.SecurityTest+LogonDetailsUserguideTestData.password
             *    without permisson ViewPasswords
            */
        Assert.assertEquals("expect security exception", SecurityException.class, exception==null?null:exception.getClass() );
        Assert.assertTrue(exception.getMessage().indexOf("ViewPasswords") >= 0);
        System.out.println("failed with exception: " + exception.getMessage());
    }

    @Test
    public void testFieldLeafDenialNoException()
    {
        String []strPermissions = {"ViewUsernames"};
        Object2Json o2j;

        final LogonDetails sensitiveData = new LogonDetails();
        sensitiveData.password = "secret123";
        sensitiveData.username = "Andrew Eldritch";
        //to do check no check code specified situation
        String json;

        JsonExploreListener jsonListener;
        jsonListener = createMock(JsonExploreListener.class);
        o2j = createSecureObjectToJson(strPermissions, false, new FieldReflectionNodeExpander());
        json = o2j.toJson(sensitiveData);

        jsonListener.JsonStartObject(null, 0);
        jsonListener.JsonLeaf("username", sensitiveData.username, true);
        jsonListener.JsonEndObject(json.lastIndexOf("}"));

        replayAll();
        (new JSONExplorerImpl()).explore(json, jsonListener);
        verifyAll();

        resetAll();
        //check that the right permission gives access
        strPermissions = new String[] {"ViewPasswords"};
        jsonListener = createMock(JsonExploreListener.class);
        o2j = createSecureObjectToJson(strPermissions, false, new FieldReflectionNodeExpander());
        json = o2j.toJson(sensitiveData);
        //test json structure

        jsonListener.JsonStartObject(null, 0);
        jsonListener.JsonLeaf("password", sensitiveData.password, true);
        jsonListener.JsonLeaf("username", sensitiveData.username, true);
        jsonListener.JsonEndObject(json.lastIndexOf("}"));
        replayAll();
        (new JSONExplorerImpl()).explore(json, jsonListener);
        verifyAll();
        //testUtil.testJsonStructure(json, expectation, "testFieldLeafDenialNoException (permit)");
    }

    @Test
    public void testPropertyLeafDenialNoException()
    {
        String[] strPermissions = { "ViewUsernames" };
        Object2Json o2j;

        final LogonDetails sensitiveData = new LogonDetails();
        sensitiveData.setPassword("secret123");
        sensitiveData.setUsername("Andrew Eldritch");
        //to do check no check code specified situation
        String json;
        TestUtil.JsonExpectationBlock expectation;
        JsonExploreListener jsonListener;

        o2j = createSecureObjectToJson(strPermissions, false, new PropertyReflectionNodeExpander());
        json = o2j.toJson(sensitiveData);
        jsonListener = createMock(JsonExploreListener.class);

        jsonListener.JsonStartObject(null, 0);
        jsonListener.JsonLeaf("Username", sensitiveData.getUsername(), true);
        jsonListener.JsonEndObject(json.lastIndexOf("}"));

        replayAll();
        (new JSONExplorerImpl()).explore(json, jsonListener);
        verifyAll();
        resetAll();

        //check that the right permission gives access
        strPermissions = new String[] { "ViewPasswords" };
        o2j = createSecureObjectToJson(strPermissions, false, new PropertyReflectionNodeExpander());
        json = o2j.toJson(sensitiveData);
        jsonListener = createMock(JsonExploreListener.class);
        //test json structure
        jsonListener.JsonStartObject(null, 0);
        jsonListener.JsonLeaf("Password", sensitiveData.getPassword(), true);
        jsonListener.JsonLeaf("Username", sensitiveData.getUsername(), true);
        jsonListener.JsonEndObject(json.lastIndexOf("}"));

        replayAll();
        (new JSONExplorerImpl()).explore(json, jsonListener);
        verifyAll();
    }


    @Test
    public void testLeafDenialException()
    {
        String[] strPermissions = { "viewUsernames" };
        Object2Json o2j = createSecureObjectToJson(strPermissions, true, new FieldReflectionNodeExpander());

        LogonDetails sensitiveData = new LogonDetails();
        sensitiveData.password = "secret123";
        //to do check no check code specified situation
        String json=null;
        Exception exception=null;
        try
        {
            json = o2j.toJson(sensitiveData);
        }
        catch (Exception ex)
        {
            exception = ex;
        }

        Assert.assertEquals("expect security exception", SecurityException.class, exception==null?null:exception.getClass());
        Assert.assertTrue(exception.getMessage().indexOf("ViewPasswords") >= 0);

    }

    @Test
    public void testPropertyDenialException()
    {
        String[] strPermissions = { "viewUsernames" };
        Object2Json o2j = createSecureObjectToJson(strPermissions, true, new PropertyReflectionNodeExpander());

        LogonDetails sensitiveData = new LogonDetails();
        sensitiveData.setPassword("secret123");
        //to do check no check code specified situation
        Exception exception=null;
        String json=null;
        try
        {
            json = o2j.toJson(sensitiveData);
        }
        catch (Exception ex)
        {
            exception = ex;
        }

        Assert.assertEquals( "expect security exception", SecurityException.class, exception==null?null:exception.getClass());
        Assert.assertTrue(exception.getMessage().indexOf("ViewPasswords") >= 0);

    }

    @Test
    public void testFieldReferenceDenialNoException()
    {
        String[] strPermissions = { "ViewUsernames" };
        Object2Json o2j;

        LogonDetails sensitiveData = new LogonDetails();
        sensitiveData.password = "secret123";
        sensitiveData.username = "Andrew Eldritch";
        final User user = new User();
        user.id = "123";
        user.nickname = "Dark Lord";
        user.logonDetails = sensitiveData;
        //to do check no check code specified situation
        String json;
        TestUtil.JsonExpectationBlock expectation;

        o2j = createSecureObjectToJson(strPermissions, false, new FieldReflectionNodeExpander());
        json = o2j.toJson(user);

        JsonExploreListener jsonListener;
        jsonListener = createMock(JsonExploreListener.class);
        jsonListener.JsonStartObject(null, 0);
        jsonListener.JsonLeaf("id", user.id, true);
        jsonListener.JsonLeaf("nickname", user.nickname, true);
        jsonListener.JsonEndObject(json.lastIndexOf("}"));

        replayAll();
        (new JSONExplorerImpl()).explore(json, jsonListener);
        verifyAll();

        //check that the right permission gives access
        strPermissions = new String[] { "PasswordAdministration" };
        o2j = createSecureObjectToJson(strPermissions, false, new FieldReflectionNodeExpander());
        json = o2j.toJson(user);
        //test json structure
        resetAll();

        jsonListener = createMock(JsonExploreListener.class);

        jsonListener.JsonStartObject(null, 0);
        jsonListener.JsonLeaf("id", user.id, true);
        jsonListener.JsonLeaf("nickname", user.nickname, true);
        jsonListener.JsonStartObject("logonDetails", json.indexOf('{', 2));
        jsonListener.JsonLeaf("username", user.logonDetails.username, true);
        jsonListener.JsonEndObject(json.indexOf("}"));
        jsonListener.JsonEndObject(json.lastIndexOf("}"));
        replayAll();
        (new JSONExplorerImpl()).explore(json, jsonListener);
        verifyAll();
    }

    @Test
    public void testPropertyReferenceDenialNoException()
    {
        String[] strPermissions = { "ViewUsernames" };
        Object2Json o2j;

        LogonDetails sensitiveData = new LogonDetails();
        sensitiveData.setPassword("secret123");
        sensitiveData.setUsername("Andrew Eldritch");
        final User user = new User();
        user.setId( "123");
        user.setNickname( "Dark Lord");
        user.setLogonDetails( sensitiveData );
        //to do check no check code specified situation
        String json;
        TestUtil.JsonExpectationBlock expectation;

        o2j = createSecureObjectToJson(strPermissions, false, new PropertyReflectionNodeExpander());
        json = o2j.toJson(user);

        JsonExploreListener jsonListener;
        jsonListener = createMock(JsonExploreListener.class);
        jsonListener.JsonStartObject(null, 0);
        jsonListener.JsonLeaf("Id", user.id, true);
        jsonListener.JsonLeaf("Nickname", user.nickname, true);
        jsonListener.JsonEndObject(json.lastIndexOf("}"));

        replayAll();
        (new JSONExplorerImpl()).explore(json, jsonListener);
        verifyAll();

        resetAll();
        //check that the right permission gives access
        strPermissions = new String[] { "PasswordAdministration" };
        o2j = createSecureObjectToJson(strPermissions, false, new PropertyReflectionNodeExpander());
        json = o2j.toJson(user);
        //test json structure
        jsonListener = createMock(JsonExploreListener.class);

        jsonListener.JsonStartObject(null, 0);
        jsonListener.JsonLeaf("Id", user.id, true);
        jsonListener.JsonLeaf("Nickname", user.nickname, true);
        jsonListener.JsonStartObject("LogonDetails", json.indexOf('{', 2));
        jsonListener.JsonLeaf("Username", user.logonDetails.username, true);
        //theMock.JsonLeaf("password", user.logonDetails.password, true);
        jsonListener.JsonEndObject(json.indexOf("}"));
        jsonListener.JsonEndObject(json.lastIndexOf("}"));

        replayAll();
        (new JSONExplorerImpl()).explore(json, jsonListener);
        verifyAll();
    }

}
